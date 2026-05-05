package com.github.hanielcota.menuframework.internal.render.navigation;

import com.github.hanielcota.menuframework.api.ClickHandler;
import com.github.hanielcota.menuframework.definition.MenuDefinition;
import java.util.Optional;
import org.jspecify.annotations.NonNull;

/**
 * Strategy for rendering a pagination navigation button.
 */
public interface NavigationButtonStrategy {

  /**
   * Returns the index within the navigation slots list for this button.
   */
  int navigationSlotIndex();

  /**
   * Returns the default template ID to use if no custom template is configured.
   */
  @NonNull String defaultTemplateId();

  /**
   * Returns the click handler to invoke when this button is clicked.
   */
  @NonNull ClickHandler handler();

  /**
   * Returns true if navigation is possible in the current state.
   */
  boolean canNavigate(int currentPage, int totalPages);

  /**
   * Resolves the custom template ID from the menu definition, if configured.
   */
  @NonNull Optional<@NonNull String> resolveTemplateId(@NonNull MenuDefinition definition);
}
