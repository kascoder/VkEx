package org.kascoder.vkex.core.model;

import org.kascoder.vkex.core.model.attachment.Attachment;
import org.kascoder.vkex.core.model.attachment.AttachmentType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Message {
    private Integer id;
    private Integer senderId;
    private LocalDateTime date;
    private String content;
    private List<Attachment> attachmentList;

    public boolean hasAttachments() {
        return attachmentList != null && !attachmentList.isEmpty();
    }

    public boolean hasVideos() {
        return hasAttachments() && attachmentList.stream().anyMatch(attachment -> AttachmentType.VIDEO.equals(attachment.getType()));
    }

    public List<Attachment> getAttachmentList() {
        if (attachmentList == null) {
            attachmentList = new ArrayList<>();
        }

        return attachmentList;
    }

    public int attachmentCount() {
        return getAttachmentList().size();
    }
}
