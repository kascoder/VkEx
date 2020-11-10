package org.kascoder.vkex.updater;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;

public class UpdateFrame extends JFrame implements PropertyChangeListener {

    private final URL downloadUrl;
    private final File targetJarFile;
    private final UpdateResultListener listener;

    private final JProgressBar progressBar = new JProgressBar(0, 100);

    public UpdateFrame(URL downloadUrl, File targetJarFile, UpdateResultListener listener) {
        super("VkEx Updater");
        this.downloadUrl = downloadUrl;
        this.targetJarFile = targetJarFile;
        this.listener = listener;

        JLabel explanationFileName = new JLabel("<html>Downloading required files. This may take a few minutes. Please wait...");
        JLabel labelProgress = new JLabel("Progress:");

        setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(5, 10, 5, 10);
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 2;
        add(explanationFileName, constraints);

        progressBar.setPreferredSize(new Dimension(200, 30));
        progressBar.setStringPainted(true);

        constraints.gridx = 0;
        constraints.gridy = 5;
        constraints.gridwidth = 1;
        constraints.anchor = GridBagConstraints.WEST;
        add(labelProgress, constraints);

        constraints.gridx = 1;
        constraints.weightx = 1.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        add(progressBar, constraints);

        pack();
        setLocationRelativeTo(null);    // center on screen
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
    }

    public void doDownload() {
        try {
            progressBar.setValue(0);
            DownloadTask task = new DownloadTask(this, this.downloadUrl, this.targetJarFile);
            task.addPropertyChangeListener(this);
            task.execute();
        } catch (Exception ex) {
            listener.completed(false, ex);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("progress")) {
            int progress = (Integer) evt.getNewValue();
            progressBar.setValue(progress);
        }
    }

    public void onError(Exception e) {
        listener.completed(false, e);
        this.setVisible(false);
    }

    public void done() {
        listener.completed(true, null);
        this.setVisible(false);
    }

    public static class DownloadTask extends SwingWorker<Void, Void> {
        private static final int BUFFER_SIZE = 4096;
        private final UpdateFrame gui;
        private final URL downloadUrl;
        private final File targetJarFile;

        public DownloadTask(UpdateFrame gui, URL downloadUrl, File targetJarFile) {
            this.gui = gui;
            this.downloadUrl = downloadUrl;
            this.targetJarFile = targetJarFile;
        }

        @Override
        protected Void doInBackground() {
            try {
                HttpDownloadUtils util = new HttpDownloadUtils();
                util.downloadFile(this.downloadUrl);

                InputStream inputStream = util.getInputStream();
                FileOutputStream outputStream = new FileOutputStream(this.targetJarFile);

                byte[] buffer = new byte[BUFFER_SIZE];
                int bytesRead = -1;
                long totalBytesRead = 0;
                int percentCompleted = 0;
                long fileSize = util.getContentLength();

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;
                    percentCompleted = (int) (totalBytesRead * 100 / fileSize);

                    setProgress(percentCompleted);
                }

                outputStream.close();

                util.disconnect();
            } catch (Exception ex) {
                setProgress(0);
                cancel(true);
                this.gui.onError(ex);
            }

            return null;
        }

        @Override
        protected void done() {
            if (!isCancelled()) {
                this.gui.done();
            }
        }
    }
}
