package com.github.hanielcota.menuframework.internal.render;

import com.github.hanielcota.menuframework.definition.ItemTemplate;
import com.github.hanielcota.menuframework.definition.SlotDefinition;
import com.github.hanielcota.menuframework.internal.item.ItemStackFactory;
import com.github.hanielcota.menuframework.internal.registry.ItemTemplateRegistry;
import com.github.hanielcota.menuframework.internal.render.navigation.NavigationButtonStrategy;
import com.github.hanielcota.menuframework.internal.render.navigation.NextButtonStrategy;
import com.github.hanielcota.menuframework.internal.render.navigation.PreviousButtonStrategy;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class NavigationRenderer {

  @NonNull private final ItemTemplateRegistry templateRegistry;
  @NonNull private final ItemStackFactory itemStackFactory;
  @NonNull private final List<NavigationButtonStrategy> buttons;

  public NavigationRenderer(
      @NonNull ItemTemplateRegistry templateRegistry, @NonNull ItemStackFactory itemStackFactory) {
    this(
        templateRegistry,
        itemStackFactory,
        List.of(new PreviousButtonStrategy(), new NextButtonStrategy()));
  }

  public NavigationRenderer(
      @NonNull ItemTemplateRegistry templateRegistry,
      @NonNull ItemStackFactory itemStackFactory,
      @NonNull List<NavigationButtonStrategy> buttons) {
    this.templateRegistry = templateRegistry;
    this.itemStackFactory = itemStackFactory;
    this.buttons = List.copyOf(buttons);
  }

  public void render(@NonNull NavigationRenderContext context) {
    if (context.navSlots().isEmpty()) return;

    for (var button : buttons) {
      renderNavigationButton(context, button);
    }
  }

  private void renderNavigationButton(
      @NonNull NavigationRenderContext context, @NonNull NavigationButtonStrategy button) {
    var navSlots = context.navSlots();
    if (navSlots.size() <= button.navigationSlotIndex()) return;

    int slot = navSlots.get(button.navigationSlotIndex());
    var maxSlots = context.view().getTopInventory().getSize();
    if (slot < 0 || slot >= maxSlots) return;

    var canNavigate = button.canNavigate(context.currentPage(), context.totalPages());
    var template = resolveTemplate(context, slot, button);

    if (template == null) return;
    var item = itemStackFactory.create(template);

    if (!canNavigate) {
      var cloned = item.clone();
      cloned.editMeta(meta -> meta.setEnchantmentGlintOverride(false));
      item = cloned;
    }

    context.view().setItem(slot, item);
    if (canNavigate) {
      context
          .activeHandlers()
          .put(slot, SlotDefinition.navigational(slot, template, button.handler()));
    }
  }

  private @Nullable ItemTemplate resolveTemplate(
      @NonNull NavigationRenderContext context,
      int slot,
      @NonNull NavigationButtonStrategy button) {
    var slotDef = context.definition().slots().get(slot);
    if (slotDef != null && slotDef.template() != null) {
      return slotDef.template();
    }

    String templateId =
        button.resolveTemplateId(context.definition()).orElse(button.defaultTemplateId());
    var templateOpt = templateRegistry.getTemplate(templateId);
    return templateOpt.isPresent() ? templateOpt.get() : null;
  }
}
