package org.kascoder.vkex.core.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
public class Conversation {
    private Integer id;
    private String title;
    private Integer memberCount;
    private Integer lastMessageId;
    private final Map<Integer, UserProfile> profiles;
}
