package com.github.hanielcota.menuframework.internal.render;

import org.jspecify.annotations.NonNull;

/**
 * Strategy for rendering menu content into an inventory view.
 */
public sealed interface RenderStrategy
    permits StaticRenderStrategy, PaginatedRenderStrategy {

  @NonNull RenderResult render(@NonNull RenderRequest request);
}
