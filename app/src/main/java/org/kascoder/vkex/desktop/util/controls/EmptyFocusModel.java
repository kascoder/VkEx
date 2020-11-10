package org.kascoder.vkex.desktop.util.controls;

import javafx.scene.control.FocusModel;

public class EmptyFocusModel<T> extends FocusModel<T> {
    @Override
    protected int getItemCount() {
        return 0;
    }

    @Override
    public boolean isFocused(int index) {
        return false;
    }

    @Override
    protected T getModelItem(int index) {
        return null;
    }
}
