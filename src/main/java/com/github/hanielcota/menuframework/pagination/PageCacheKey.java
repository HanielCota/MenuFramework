package com.github.hanielcota.menuframework.pagination;

import java.util.Objects;
import org.jspecify.annotations.NonNull;

public record PageCacheKey(@NonNull String menuId, int pageNumber, int contentHash) {

  public PageCacheKey {
    Objects.requireNonNull(menuId, "menuId");
    if (pageNumber < 0) {
      throw new IllegalArgumentException("pageNumber cannot be negative: " + pageNumber);
    }
  }
}
