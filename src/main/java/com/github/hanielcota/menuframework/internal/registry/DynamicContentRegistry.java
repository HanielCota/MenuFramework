package com.github.hanielcota.menuframework.internal.registry;

import com.github.hanielcota.menuframework.api.DynamicContentProvider;
import com.github.hanielcota.menuframework.definition.SlotDefinition;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.NonNull;

public interface DynamicContentRegistry {

  void setDynamicContent(@NonNull String menuId, @NonNull List<SlotDefinition> items);

  void setDynamicContentProvider(@NonNull String menuId, @NonNull DynamicContentProvider provider);

  @NonNull List<SlotDefinition> getDynamicContent(@NonNull String menuId);

  @NonNull Optional<@NonNull DynamicContentProvider> getDynamicContentProvider(
      @NonNull String menuId);

  int getDynamicContentHash(@NonNull String menuId);
}
