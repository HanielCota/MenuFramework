package com.github.hanielcota.menuframework.api;

import com.github.hanielcota.menuframework.definition.SlotDefinition;
import java.util.List;
import org.jspecify.annotations.NonNull;

public interface DynamicMenuContentService {

  /**
   * Replaces the dynamic content for a menu.
   *
   * <p>For paginated menus, these items are projected into the configured content slots.
   */
  void setDynamicContent(@NonNull String menuId, @NonNull List<SlotDefinition> items);

  /** Sets a dynamic content provider for a menu. */
  void setDynamicContentProvider(@NonNull String menuId, @NonNull DynamicContentProvider provider);

  /** Returns the dynamic content currently registered for a menu. */
  @NonNull List<SlotDefinition> getDynamicContent(@NonNull String menuId);
}
