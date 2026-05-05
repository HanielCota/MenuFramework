package com.github.hanielcota.menuframework.internal.registry;

import com.github.hanielcota.menuframework.definition.ItemTemplate;
import java.util.Optional;
import org.jspecify.annotations.NonNull;

public interface ItemTemplateRegistry {

  void registerTemplate(@NonNull String id, @NonNull ItemTemplate template);

  @NonNull Optional<@NonNull ItemTemplate> getTemplate(@NonNull String id);
}
