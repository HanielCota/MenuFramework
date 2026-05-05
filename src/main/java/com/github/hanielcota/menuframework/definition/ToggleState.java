package com.github.hanielcota.menuframework.definition;

import org.jspecify.annotations.NonNull;

/**
 * Internal state holder for toggle slots.
 */
public final class ToggleState {
    private final ItemTemplate enabledTemplate;
    private final ItemTemplate disabledTemplate;
    private boolean enabled;

    public ToggleState(
            @NonNull ItemTemplate enabledTemplate,
            @NonNull ItemTemplate disabledTemplate,
            boolean initialState) {
        this.enabledTemplate = enabledTemplate;
        this.disabledTemplate = disabledTemplate;
        this.enabled = initialState;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public @NonNull ItemTemplate currentTemplate() {
        return enabled ? enabledTemplate : disabledTemplate;
    }

    public @NonNull ItemTemplate enabledTemplate() {
        return enabledTemplate;
    }

    public @NonNull ItemTemplate disabledTemplate() {
        return disabledTemplate;
    }
}
