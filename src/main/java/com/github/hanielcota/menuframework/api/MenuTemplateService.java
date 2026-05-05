package com.github.hanielcota.menuframework.api;

import com.github.hanielcota.menuframework.definition.ItemTemplate;
import java.util.Optional;
import org.jspecify.annotations.NonNull;

public interface MenuTemplateService {

  /** Registers a reusable item template, commonly used by pagination navigation. */
  void registerTemplate(@NonNull String id, @NonNull ItemTemplate template);

  /** Finds a registered item template by id. */
  @NonNull Optional<@NonNull ItemTemplate> getTemplate(@NonNull String id);
}
