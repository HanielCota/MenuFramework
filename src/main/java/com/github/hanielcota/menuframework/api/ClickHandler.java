package com.github.hanielcota.menuframework.api;

import org.jspecify.annotations.NonNull;

@FunctionalInterface
public interface ClickHandler {

  void onClick(@NonNull ClickContext context);
}
