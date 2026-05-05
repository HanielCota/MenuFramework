package com.github.hanielcota.menuframework.definition;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.jspecify.annotations.NonNull;

public record PaginationConfig(
    @NonNull List<Integer> contentSlots,
    @NonNull List<Integer> navigationSlots,
    @NonNull Optional<@NonNull String> previousTemplateId,
    @NonNull Optional<@NonNull String> nextTemplateId,
    boolean enabled) {

  public PaginationConfig {
    Objects.requireNonNull(contentSlots, "contentSlots");
    Objects.requireNonNull(navigationSlots, "navigationSlots");
    Objects.requireNonNull(previousTemplateId, "previousTemplateId");
    Objects.requireNonNull(nextTemplateId, "nextTemplateId");

    contentSlots = List.copyOf(contentSlots);
    navigationSlots = List.copyOf(navigationSlots);
    validateSlots(contentSlots, "content");
    validateSlots(navigationSlots, "navigation");

    if (enabled && contentSlots.isEmpty()) {
      throw new IllegalArgumentException("Enabled pagination requires at least one content slot");
    }
  }

  private static void validateSlots(@NonNull List<Integer> slots, @NonNull String label) {
    for (Integer slot : slots) {

      if (slot == null || slot < 0) {
        throw new IllegalArgumentException("Invalid " + label + " slot: " + slot);
      }
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private boolean enabled = false;
    private List<Integer> contentSlots = List.of();
    private List<Integer> navigationSlots = List.of();
    private String previousTemplateId;
    private String nextTemplateId;

    public Builder enabled(boolean enabled) {
      this.enabled = enabled;
      return this;
    }

    public Builder contentSlots(@NonNull List<Integer> slots) {
      this.contentSlots = List.copyOf(Objects.requireNonNull(slots, "slots"));
      return this;
    }

    public Builder contentSlots(@NonNull SlotPattern pattern) {
      this.contentSlots = Objects.requireNonNull(pattern, "pattern").slots();
      return this;
    }

    public Builder navigationSlots(@NonNull List<Integer> slots) {
      this.navigationSlots = List.copyOf(Objects.requireNonNull(slots, "slots"));
      return this;
    }

    public Builder previousTemplate(@NonNull String id) {
      this.previousTemplateId = Objects.requireNonNull(id, "id");
      return this;
    }

    public Builder nextTemplate(@NonNull String id) {
      this.nextTemplateId = Objects.requireNonNull(id, "id");
      return this;
    }

    public PaginationConfig build() {
      return new PaginationConfig(
          contentSlots,
          navigationSlots,
          Optional.ofNullable(previousTemplateId),
          Optional.ofNullable(nextTemplateId),
          enabled || !contentSlots.isEmpty());
    }
  }
}
