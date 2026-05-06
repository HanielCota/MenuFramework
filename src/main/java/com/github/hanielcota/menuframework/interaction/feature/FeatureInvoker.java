package com.github.hanielcota.menuframework.interaction.feature;

import com.github.hanielcota.menuframework.api.ClickContext;
import com.github.hanielcota.menuframework.definition.MenuDefinition;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jspecify.annotations.NonNull;

/** Invokes MenuFeature callbacks during click handling. */
public final class FeatureInvoker {

  private static final Logger log = Logger.getLogger(FeatureInvoker.class.getName());

  /** Invokes {@code onClick} for all features attached to the menu definition. */
  public void invokeOnClick(
      @NonNull MenuDefinition definition, @NonNull ClickContext clickContext) {
    for (var feature : definition.features()) {
      try {
        feature.onClick(clickContext);
      } catch (Exception exception) {
        log.log(
            Level.WARNING,
            exception,
            () ->
                "menu.feature.onClick_failed menuId=%s featureType=%s"
                    .formatted(definition.id(), feature.getClass().getSimpleName()));
      }
    }
  }
}
