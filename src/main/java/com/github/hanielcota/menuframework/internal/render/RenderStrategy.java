package com.github.hanielcota.menuframework.internal.render;

import org.jspecify.annotations.NonNull;

@FunctionalInterface
public interface RenderStrategy {

  @NonNull RenderResult render(@NonNull RenderRequest request);
}
