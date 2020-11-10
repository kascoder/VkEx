package org.kascoder.vkex.desktop.controller;

import com.google.inject.Inject;
import javafx.scene.control.*;
import org.kascoder.vkex.core.ApplicationContext;
import org.kascoder.vkex.core.model.ExportResultObject;
import org.kascoder.vkex.core.model.attachment.Attachment;
import org.kascoder.vkex.core.model.attachment.download.AttachmentDownloadResult;
import org.kascoder.vkex.core.model.attachment.download.UnsuccessfulAttachmentDownloadResult;
import org.kascoder.vkex.core.model.options.StorageOptions;
import org.kascoder.vkex.core.service.io.attachment.AttachmentDownloadService;
import org.kascoder.vkex.core.service.io.attachment.AttachmentDownloadServiceFactory;
import org.kascoder.vkex.desktop.util.FXSceneRouter;
import org.kascoder.vkex.desktop.util.UiUtils;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toList;

public class ExportResultWindowController implements ApplicationController {

    private static final int MAX_DOWNLOAD_THREAD_NUMBER = 5;

    private final ExecutorService downloadService;
    private final StorageOptions storageOptions;
    private final AttachmentDownloadService attachmentDownloadService;
    private final List<AttachmentDownloadResult> attachmentDownloadResults;

    @FXML
    private Label folderPath;
    @FXML
    private Label attachmentsDownloadResult;
    @FXML
    private TableView<AttachmentProblem> attachmentProblemsTable;
    @FXML
    private ToolBar attachmentProblemsTableToolbar;
    @FXML
    private TableColumn<AttachmentProblem, String> attachmentProblemsTableTypeColumn;
    @FXML
    private TableColumn<AttachmentProblem, String> attachmentProblemsTableErrorColumn;
    @FXML
    private TableColumn<AttachmentProblem, DownloadStatus> attachmentProblemsTableStatusColumn;

    @Inject
    public ExportResultWindowController(ApplicationContext applicationContext) {
        this.downloadService = Executors.newFixedThreadPool(MAX_DOWNLOAD_THREAD_NUMBER);

        var properties = FXSceneRouter.currentStage().getProperties();
        this.storageOptions = (StorageOptions) properties.get("storageOptions");
        var exportStatistics = (ExportResultObject) properties.get("resultObject");
        this.attachmentDownloadResults = exportStatistics.getAttachmentDownloadResults();
        this.attachmentDownloadService = AttachmentDownloadServiceFactory.build(storageOptions);

        FXSceneRouter.currentStage()
                .setOnCloseRequest(event -> downloadService.shutdown());
    }

    @Override
    public void initialize() {
        var problems = FXCollections.observableArrayList(attachmentDownloadResults.stream()
                .filter(Predicate.not(AttachmentDownloadResult::isSuccessful))
                .map(AttachmentProblem::new)
                .collect(toList()));

        this.folderPath.setText(storageOptions.getPath());
        this.attachmentProblemsTable.setItems(problems);

        refreshAttachmentDownloadResultLabel();

        var selectionModel = this.attachmentProblemsTable.getSelectionModel();
        selectionModel.setSelectionMode(SelectionMode.MULTIPLE);
        var selectedItems = selectionModel.getSelectedItems();
        selectedItems.addListener((ListChangeListener<AttachmentProblem>) c -> refreshTableToolbar());

        this.attachmentProblemsTableTypeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        this.attachmentProblemsTableErrorColumn.setCellValueFactory(new PropertyValueFactory<>("message"));
        this.attachmentProblemsTableErrorColumn.setCellFactory(tableColumn -> new TableCell<>() {
            @Override
            public void updateItem(String message, boolean empty) {
                super.updateItem(message, empty);
                if (empty || message == null) {
                    setGraphic(null);
                } else {
                    Node node;
                    int limit = 45;
                    if (message.length() > limit) {
                        var substring = message.substring(0, limit) + "...";
                        var errorBtn = new Label(substring);
                        var more = new Hyperlink("more");
                        more.setFocusTraversable(false);
                        more.setOnAction(event -> UiUtils.notifyError(message));
                        var container = new HBox(errorBtn, more);
                        container.setSpacing(15);
                        node = container;
                    } else {
                        node = new Label(message);
                    }
                    setGraphic(node);
                }
            }
        });
        this.attachmentProblemsTableStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        this.attachmentProblemsTableStatusColumn.setCellFactory(tableColumn -> new TableCell<>() {
            @Override
            public void updateItem(DownloadStatus downloadStatus, boolean empty) {
                super.updateItem(downloadStatus, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    var node = switch (downloadStatus) {
                        case FAIL -> new Label(downloadStatus.getCaption());
                        case IN_PROGRESS -> {
                            var indicator = new ProgressIndicator();
                            indicator.setPrefSize(20, 20);
                            yield indicator;
                        }
                    };
                    setGraphic(node);
                }
            }
        });

        refreshTableToolbar();
    }

    private void refreshAttachmentDownloadResultLabel() {
        var totalAttachmentCount = attachmentDownloadResults.size();
        var problemNumber = this.attachmentProblemsTable.getItems().size();
        this.attachmentsDownloadResult.setText(String.format("%d/%d", totalAttachmentCount - problemNumber, totalAttachmentCount));

        var hasProblems = problemNumber > 0;
        this.attachmentProblemsTableToolbar.setDisable(!hasProblems);
    }

    public void openFolder() {
        try {
            UiUtils.openDirectoryInExplorer(this.folderPath.getText());
        } catch (Exception e) {
            UiUtils.notifyError(e);
        }
    }

    private void refreshTableToolbar() {
        attachmentProblemsTableToolbar.getItems().clear();

        var selectionModel = attachmentProblemsTable.getSelectionModel();
        var clearSelectionBtn = new Button("Clear Selection");
        clearSelectionBtn.setFocusTraversable(false);
        clearSelectionBtn.getStyleClass().add("outline-button");
        clearSelectionBtn.setOnAction(event -> selectionModel.clearSelection());
        attachmentProblemsTableToolbar.getItems().add(clearSelectionBtn);

        var selectAllBtn = new Button("Select All");
        selectAllBtn.setFocusTraversable(false);
        selectAllBtn.getStyleClass().add("outline-button");
        selectAllBtn.setOnAction(event -> selectionModel.selectAll());
        attachmentProblemsTableToolbar.getItems().add(selectAllBtn);

        var selectedItems = selectionModel.getSelectedItems();
        if (selectedItems.isEmpty()) {
            return;
        }

        var itemsToDownload = selectedItems.stream()
                .filter(adapter -> DownloadStatus.FAIL.equals(adapter.getStatus()))
                .collect(toList());
        if (itemsToDownload.size() > 0) {
            var downloadBtn = new Button("Download");
            downloadBtn.setFocusTraversable(false);
            downloadBtn.getStyleClass().add("outline-button");
            downloadBtn.setOnAction(event -> {
                itemsToDownload.forEach(attachmentProblem -> {
                    attachmentProblem.setStatus(DownloadStatus.IN_PROGRESS);
                    var downloadTask = createDownloadTask(attachmentProblem.getAttachment());
                    CompletableFuture.supplyAsync(downloadTask, downloadService)
                            .whenComplete((result, error) -> {
                                Platform.runLater(() -> {
                                    Throwable e;
                                    if (!result.isSuccessful()) {
                                        var failure = (UnsuccessfulAttachmentDownloadResult) result;
                                        e = failure.getError();
                                    } else {
                                        e = error;
                                    }

                                    if (e != null) {
                                        attachmentProblem.setMessage(e.getMessage());
                                        attachmentProblem.setStatus(DownloadStatus.FAIL);
                                        return;
                                    }

                                    attachmentProblemsTable.getItems().remove(attachmentProblem);
                                    refreshAttachmentDownloadResultLabel();
                                });
                            });
                });
                clearSelectionBtn.fire();
            });
            attachmentProblemsTableToolbar.getItems().add(downloadBtn);
        }
    }

    private Supplier<AttachmentDownloadResult> createDownloadTask(Attachment attachment) {
        return () -> {
            attachmentDownloadService.prepareEnvironment();
            var result = attachmentDownloadService.downloadAttachment(attachment);
            attachmentDownloadService.cleanUpEnvironment();
            return result;
        };
    }

    public static class AttachmentProblem {
        private final String type;
        private final Attachment attachment;
        private final StringProperty message;
        private final ObjectProperty<DownloadStatus> status;

        public AttachmentProblem(AttachmentDownloadResult downloadResult) {
            assert !downloadResult.isSuccessful();

            this.attachment = downloadResult.getAttachment();
            this.type = attachment.getType().getCaption();
            this.status = new SimpleObjectProperty<>(DownloadStatus.FAIL);

            UnsuccessfulAttachmentDownloadResult unsuccessfulAttachmentDownloadResult = (UnsuccessfulAttachmentDownloadResult) downloadResult;
            var error = unsuccessfulAttachmentDownloadResult.getError();
            this.message = new SimpleStringProperty(error.getMessage());
        }

        public String getType() {
            return type;
        }

        public Attachment getAttachment() {
            return attachment;
        }

        public String getMessage() {
            return message.get();
        }

        public StringProperty messageProperty() {
            return message;
        }

        public void setMessage(String message) {
            this.message.set(message);
        }

        public DownloadStatus getStatus() {
            return status.get();
        }

        public ObjectProperty<DownloadStatus> statusProperty() {
            return status;
        }

        public void setStatus(DownloadStatus status) {
            this.status.set(status);
        }
    }

    public enum DownloadStatus {
        FAIL("Fail"), IN_PROGRESS("In Progress");

        private final String caption;

        DownloadStatus(String caption) {
            this.caption = caption;
        }

        public String getCaption() {
            return caption;
        }
    }
}
