package com.github.hanielcota.menuframework.internal.render;

import com.github.hanielcota.menuframework.api.ClickHandler;
import com.github.hanielcota.menuframework.definition.ItemTemplate;
import com.github.hanielcota.menuframework.definition.MenuDefinition;
import com.github.hanielcota.menuframework.internal.item.ItemStackFactory;
import com.github.hanielcota.menuframework.internal.registry.ItemTemplateRegistry;
import java.util.Arrays;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@RequiredArgsConstructor
public final class NavigationRenderer {

  @NonNull private final ItemTemplateRegistry templateRegistry;
  @NonNull private final ItemStackFactory itemStackFactory;

  public void render(@NonNull NavigationRenderContext context) {
    if (context.navSlots().isEmpty()) return;

    Arrays.stream(NavigationButton.values())
        .forEach(button -> renderNavigationButton(context, button));
  }

  private void renderNavigationButton(
      @NonNull NavigationRenderContext context, @NonNull NavigationButton button) {
    var navSlots = context.navSlots();
    if (navSlots.size() <= button.navigationSlotIndex()) return;

    var slot = navSlots.get(button.navigationSlotIndex());
    var maxSlots = context.view().getTopInventory().getSize();
    if (slot < 0 || slot >= maxSlots) return;

    var canNavigate = button.canNavigate(context.currentPage(), context.totalPages());
    var template = resolveTemplate(context, slot, button);

    if (template == null) return;
    var item = itemStackFactory.create(template);

    if (!canNavigate) {
      item.editMeta(meta -> meta.setEnchantmentGlintOverride(false));
    }

    context.view().setItem(slot, item);
    if (canNavigate) {
      context.activeHandlers().put(slot, button.handler());
    }
  }

  private @Nullable ItemTemplate resolveTemplate(
      @NonNull NavigationRenderContext context, int slot, @NonNull NavigationButton button) {
    var slotDef = context.definition().slots().get(slot);
    if (slotDef != null && slotDef.template() != null) {
      return slotDef.template();
    }

    String templateId = button.resolveTemplateId(context.definition()).orElse(button.defaultTemplateId());
    return templateRegistry.getTemplate(templateId).orElse(null);
  }

  private enum NavigationButton {
    PREVIOUS(0, "prev_button", ctx -> ctx.session().setPage(ctx.session().currentPage() - 1)) {
      @Override
      boolean canNavigate(int currentPage, int totalPages) {
        return currentPage > 0;
      }

      @Override
      Optional<@NonNull String> resolveTemplateId(MenuDefinition definition) {
        return definition.pagination().previousTemplateId();
      }
    },
    NEXT(1, "next_button", ctx -> ctx.session().setPage(ctx.session().currentPage() + 1)) {
      @Override
      boolean canNavigate(int currentPage, int totalPages) {
        return currentPage < totalPages - 1;
      }

      @Override
      Optional<@NonNull String> resolveTemplateId(MenuDefinition definition) {
        return definition.pagination().nextTemplateId();
      }
    };

    private final int navigationSlotIndex;
    @NonNull private final String defaultTemplateId;
    @NonNull private final ClickHandler handler;

    NavigationButton(int navigationSlotIndex, @NonNull String defaultTemplateId, @NonNull ClickHandler handler) {
      this.navigationSlotIndex = navigationSlotIndex;
      this.defaultTemplateId = defaultTemplateId;
      this.handler = handler;
    }

    int navigationSlotIndex() {
      return navigationSlotIndex;
    }

    @NonNull String defaultTemplateId() {
      return defaultTemplateId;
    }

    @NonNull ClickHandler handler() {
      return handler;
    }

    abstract boolean canNavigate(int currentPage, int totalPages);

    abstract Optional<@NonNull String> resolveTemplateId(MenuDefinition definition);
  }
}
