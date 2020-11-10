package org.kascoder.vkex.core.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ConversationList {
    private int total;
    private List<Conversation> items;
}
