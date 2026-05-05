package com.github.hanielcota.menuframework.internal.item;

import com.github.hanielcota.menuframework.definition.ItemTemplate;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;

public interface ItemStackFactory {

  @NonNull ItemStack create(@NonNull ItemTemplate template);

  void clearCache();
}
