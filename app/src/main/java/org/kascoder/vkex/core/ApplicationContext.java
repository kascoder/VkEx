package org.kascoder.vkex.core;

import org.kascoder.vkex.core.model.Principal;
import org.kascoder.vkex.core.model.UserPreferences;

public interface ApplicationContext {
    Principal getPrincipal();
    void setPrincipal(Principal principal);
    void setUserPreferences(UserPreferences userPreferences);
    UserPreferences getUserPreferences();

    boolean hasPrincipal();
}
