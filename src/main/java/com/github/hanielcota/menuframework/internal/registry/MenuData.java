package com.github.hanielcota.menuframework.internal.registry;

import com.github.hanielcota.menuframework.api.DynamicContentProvider;
import com.github.hanielcota.menuframework.definition.ItemTemplate;
import com.github.hanielcota.menuframework.definition.MenuDefinition;
import com.github.hanielcota.menuframework.definition.SlotDefinition;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.jspecify.annotations.NonNull;

/**
 * In-memory storage for all menu-related data.
 *
 * <p>Groups related maps (definitions, templates, dynamic content) to ensure consistent cleanup
 * and lifecycle management.
 */
final class MenuData {

  private final Map<String, List<SlotDefinition>> dynamicContent = new ConcurrentHashMap<>();
  private final Map<String, DynamicContentProvider> dynamicContentProviders = new ConcurrentHashMap<>();
  private final Map<String, Integer> dynamicContentHash = new ConcurrentHashMap<>();
  private final Map<String, MenuDefinition> definitions = new ConcurrentHashMap<>();
  private final Map<String, ItemTemplate> templates = new ConcurrentHashMap<>();

  void registerDefinition(@NonNull MenuDefinition definition) {
    definitions.put(definition.id(), definition);
  }

  void unregisterDefinition(@NonNull String id) {
    definitions.remove(id);
    dynamicContent.remove(id);
    dynamicContentProviders.remove(id);
    dynamicContentHash.remove(id);
  }

  @NonNull Optional<MenuDefinition> getDefinition(@NonNull String id) {
    return Optional.ofNullable(definitions.get(id));
  }

  void registerTemplate(@NonNull String id, @NonNull ItemTemplate template) {
    templates.put(id, template);
  }

  @NonNull Optional<ItemTemplate> getTemplate(@NonNull String id) {
    return Optional.ofNullable(templates.get(id));
  }

  void setDynamicContent(@NonNull String menuId, @NonNull List<SlotDefinition> items) {
    var copy = List.copyOf(items);
    dynamicContent.put(menuId, copy);
    dynamicContentHash.put(menuId, computeContentHash(copy));
  }

  private static int computeContentHash(@NonNull List<SlotDefinition> items) {
    int result = 1;
    for (SlotDefinition item : items) {
      result = 31 * result + (item == null ? 0 : item.hashCode());
    }
    return result;
  }

  void setDynamicContentProvider(@NonNull String menuId, @NonNull DynamicContentProvider provider) {
    dynamicContentProviders.put(menuId, provider);
  }

  int getDynamicContentHash(@NonNull String menuId) {
    return dynamicContentHash.getOrDefault(menuId, 0);
  }

  @NonNull List<SlotDefinition> getDynamicContent(@NonNull String menuId) {
    return dynamicContent.getOrDefault(menuId, List.of());
  }

  @NonNull Optional<DynamicContentProvider> getDynamicContentProvider(@NonNull String menuId) {
    return Optional.ofNullable(dynamicContentProviders.get(menuId));
  }

  void clearAll() {
    definitions.clear();
    templates.clear();
    dynamicContent.clear();
    dynamicContentProviders.clear();
    dynamicContentHash.clear();
  }

  long definitionCount() {
    return definitions.size();
  }
}
