package com.github.hanielcota.menuframework.internal.render.navigation;

import com.github.hanielcota.menuframework.api.ClickHandler;
import com.github.hanielcota.menuframework.definition.MenuDefinition;
import java.util.Optional;
import org.jspecify.annotations.NonNull;

/**
 * Next page navigation button.
 */
public final class NextButtonStrategy implements NavigationButtonStrategy {

  private static final int INDEX = 1;
  private static final String DEFAULT_TEMPLATE_ID = "next_button";
  private static final ClickHandler HANDLER = ctx -> {
    var session = ctx.session();
    session.setPage(session.currentPage() + 1);
  };

  @Override
  public int navigationSlotIndex() {
    return INDEX;
  }

  @Override
  public @NonNull String defaultTemplateId() {
    return DEFAULT_TEMPLATE_ID;
  }

  @Override
  public @NonNull ClickHandler handler() {
    return HANDLER;
  }

  @Override
  public boolean canNavigate(int currentPage, int totalPages) {
    return currentPage < totalPages - 1;
  }

  @Override
  public @NonNull Optional<@NonNull String> resolveTemplateId(@NonNull MenuDefinition definition) {
    return definition.pagination().nextTemplateId();
  }
}
