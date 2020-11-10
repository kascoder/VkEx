package org.kascoder.vkex.core;

import org.kascoder.vkex.core.model.Principal;
import org.kascoder.vkex.core.model.UserPreferences;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class SimpleApplicationContext implements ApplicationContext {
    private Principal principal;
    private UserPreferences userPreferences;

    @Override
    public boolean hasPrincipal() {
        return principal != null;
    }
}
