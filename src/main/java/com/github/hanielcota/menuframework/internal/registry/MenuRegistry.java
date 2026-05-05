package com.github.hanielcota.menuframework.internal.registry;

import com.github.hanielcota.menuframework.api.DynamicContentProvider;
import com.github.hanielcota.menuframework.definition.ItemTemplate;
import com.github.hanielcota.menuframework.definition.MenuDefinition;
import com.github.hanielcota.menuframework.definition.SlotDefinition;
import com.github.hanielcota.menuframework.pagination.PaginationEngine;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;

@RequiredArgsConstructor
public final class MenuRegistry
    implements MenuDefinitionRegistry, ItemTemplateRegistry, DynamicContentRegistry {

  private final MenuData data = new MenuData();
  @NonNull private final PaginationEngine paginationEngine;

  public @NonNull PaginationEngine paginationEngine() {
    return paginationEngine;
  }

  @Override
  public void registerDefinition(@NonNull MenuDefinition definition) {
    data.registerDefinition(definition);
    paginationEngine.invalidate(definition.id());
  }

  @Override
  public void unregisterDefinition(@NonNull String id) {
    data.unregisterDefinition(id);
    paginationEngine.invalidate(id);
  }

  @Override
  public @NonNull Optional<@NonNull MenuDefinition> getDefinition(@NonNull String id) {
    return data.getDefinition(id);
  }

  @Override
  public void registerTemplate(@NonNull String id, @NonNull ItemTemplate template) {
    data.registerTemplate(id, template);
  }

  @Override
  public @NonNull Optional<@NonNull ItemTemplate> getTemplate(@NonNull String id) {
    return data.getTemplate(id);
  }

  @Override
  public void setDynamicContent(@NonNull String menuId, @NonNull List<SlotDefinition> items) {
    data.setDynamicContent(menuId, items);
    paginationEngine.invalidate(menuId);
  }

  @Override
  public void setDynamicContentProvider(
      @NonNull String menuId, @NonNull DynamicContentProvider provider) {
    data.setDynamicContentProvider(menuId, provider);
    paginationEngine.invalidate(menuId);
  }

  @Override
  public int getDynamicContentHash(@NonNull String menuId) {
    return data.getDynamicContentHash(menuId);
  }

  @Override
  public @NonNull List<SlotDefinition> getDynamicContent(@NonNull String menuId) {
    return data.getDynamicContent(menuId);
  }

  @Override
  public @NonNull Optional<@NonNull DynamicContentProvider> getDynamicContentProvider(
      @NonNull String menuId) {
    return data.getDynamicContentProvider(menuId);
  }

  public void invalidateAll() {
    data.clearAll();
    paginationEngine.invalidateAll();
  }

  @Override
  public long estimatedDefinitionCount() {
    return data.definitionCount();
  }
}
