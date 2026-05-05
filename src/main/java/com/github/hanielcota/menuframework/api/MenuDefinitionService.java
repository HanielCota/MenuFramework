package com.github.hanielcota.menuframework.api;

import com.github.hanielcota.menuframework.definition.MenuDefinition;
import java.util.Optional;
import org.jspecify.annotations.NonNull;

public interface MenuDefinitionService {

  /** Registers or replaces a menu definition. */
  void registerDefinition(@NonNull MenuDefinition definition);

  /** Finds a registered menu definition by id. */
  @NonNull Optional<@NonNull MenuDefinition> getDefinition(@NonNull String id);

  /** Unregisters a menu definition and clears its dynamic content and cached pages. */
  void unregisterDefinition(@NonNull String menuId);
}
