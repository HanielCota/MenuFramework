package com.github.hanielcota.menuframework.internal.registry;

import com.github.hanielcota.menuframework.definition.MenuDefinition;
import java.util.Optional;
import org.jspecify.annotations.NonNull;

public interface MenuDefinitionRegistry {

  void registerDefinition(@NonNull MenuDefinition definition);

  @NonNull Optional<@NonNull MenuDefinition> getDefinition(@NonNull String id);

  void unregisterDefinition(@NonNull String id);

  long estimatedDefinitionCount();
}
