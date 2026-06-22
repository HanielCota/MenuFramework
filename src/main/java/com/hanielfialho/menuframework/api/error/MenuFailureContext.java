package com.hanielfialho.menuframework.api.error;

import com.hanielfialho.menuframework.api.task.MenuTaskKey;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * Immutable structural context for a non-fatal runtime failure.
 *
 * <p>The context deliberately contains no {@code Player}, inventory or session state. Its metadata
 * can therefore be forwarded to logging, metrics or error tracking from any thread. {@link
 * #cause()} remains the original throwable.
 */
public final class MenuFailureContext {

  private final MenuFailureOperation operation;
  private final Throwable cause;
  private final UUID viewerId;
  private final Class<?> menuType;
  private final @Nullable UUID sessionId;
  private final @Nullable Long revision;
  private final @Nullable MenuTaskKey taskKey;
  private final @Nullable Long taskGeneration;
  private final @Nullable Long taskExecution;

  private MenuFailureContext(Builder builder) {
    this.operation = builder.operation;
    this.cause = builder.cause;
    this.viewerId = builder.viewerId;
    this.menuType = builder.menuType;
    this.sessionId = builder.sessionId;
    this.revision = builder.revision;
    this.taskKey = builder.taskKey;
    this.taskGeneration = builder.taskGeneration;
    this.taskExecution = builder.taskExecution;
  }

  /**
   * Creates a builder containing the required metadata.
   *
   * @param operation runtime stage that observed the failure
   * @param cause original failure
   * @param viewerId affected viewer UUID
   * @param menuType concrete menu-definition class
   * @return a new builder
   * @throws NullPointerException if an argument is {@code null}
   */
  public static Builder builder(
      MenuFailureOperation operation, Throwable cause, UUID viewerId, Class<?> menuType) {
    return new Builder(operation, cause, viewerId, menuType);
  }

  private static OptionalLong optionalLong(@Nullable Long value) {
    return value == null ? OptionalLong.empty() : OptionalLong.of(value);
  }

  /**
   * Returns the runtime stage that observed the failure.
   *
   * @return failure operation
   */
  public MenuFailureOperation operation() {
    return this.operation;
  }

  /**
   * Returns the original failure.
   *
   * @return original throwable
   */
  public Throwable cause() {
    return this.cause;
  }

  /**
   * Returns the affected viewer UUID.
   *
   * @return viewer UUID
   */
  public UUID viewerId() {
    return this.viewerId;
  }

  /**
   * Returns the concrete menu-definition class.
   *
   * @return menu type
   */
  public Class<?> menuType() {
    return this.menuType;
  }

  /**
   * Returns the fully qualified menu class name.
   *
   * @return fully qualified class name
   */
  public String menuTypeName() {
    return this.menuType.getName();
  }

  /**
   * Returns the session identifier when the failure was associated with a published session.
   *
   * @return optional session identifier
   */
  public Optional<UUID> sessionId() {
    return Optional.ofNullable(this.sessionId);
  }

  /**
   * Returns the session revision when available.
   *
   * @return optional revision
   */
  public OptionalLong revision() {
    return optionalLong(this.revision);
  }

  /**
   * Returns the task key associated with the failure, when any.
   *
   * @return optional task key
   */
  public Optional<MenuTaskKey> taskKey() {
    return Optional.ofNullable(this.taskKey);
  }

  /**
   * Returns the task generation when available.
   *
   * @return optional task generation
   */
  public OptionalLong taskGeneration() {
    return optionalLong(this.taskGeneration);
  }

  /**
   * Returns the periodic execution number when available.
   *
   * @return optional execution number
   */
  public OptionalLong taskExecution() {
    return optionalLong(this.taskExecution);
  }

  /**
   * Returns whether session metadata is present.
   *
   * @return {@code true} when a session identifier is available
   */
  public boolean hasSession() {
    return this.sessionId != null;
  }

  /**
   * Returns whether task metadata is present.
   *
   * @return {@code true} when a task key is available
   */
  public boolean hasTask() {
    return this.taskKey != null;
  }

  /**
   * Mutable builder for an immutable {@link MenuFailureContext}.
   *
   * <p>The builder is not thread-safe.
   */
  public static final class Builder {

    private final MenuFailureOperation operation;
    private final Throwable cause;
    private final UUID viewerId;
    private final Class<?> menuType;

    private @Nullable UUID sessionId;
    private @Nullable Long revision;
    private @Nullable MenuTaskKey taskKey;
    private @Nullable Long taskGeneration;
    private @Nullable Long taskExecution;

    private Builder(
        MenuFailureOperation operation, Throwable cause, UUID viewerId, Class<?> menuType) {
      this.operation = Objects.requireNonNull(operation, "operation");
      this.cause = Objects.requireNonNull(cause, "cause");
      this.viewerId = Objects.requireNonNull(viewerId, "viewerId");
      this.menuType = Objects.requireNonNull(menuType, "menuType");
    }

    /**
     * Adds session metadata.
     *
     * @param sessionId non-null session identifier
     * @param revision positive session revision
     * @return this builder
     */
    public Builder session(UUID sessionId, long revision) {
      this.sessionId = Objects.requireNonNull(sessionId, "sessionId");

      if (revision <= 0L) {
        throw new IllegalArgumentException("revision must be greater than zero: " + revision);
      }

      this.revision = revision;
      return this;
    }

    /**
     * Adds only a task key.
     *
     * @param taskKey non-null task key
     * @return this builder
     */
    public Builder task(MenuTaskKey taskKey) {
      this.taskKey = Objects.requireNonNull(taskKey, "taskKey");
      this.taskGeneration = null;
      this.taskExecution = null;
      return this;
    }

    /**
     * Adds task key and generation metadata.
     *
     * @param taskKey non-null task key
     * @param generation positive task generation
     * @return this builder
     */
    public Builder task(MenuTaskKey taskKey, long generation) {
      this.task(taskKey);

      if (generation <= 0L) {
        throw new IllegalArgumentException("generation must be greater than zero: " + generation);
      }

      this.taskGeneration = generation;
      return this;
    }

    /**
     * Adds task key, generation and periodic execution metadata.
     *
     * @param taskKey non-null task key
     * @param generation positive task generation
     * @param execution positive execution number
     * @return this builder
     */
    public Builder task(MenuTaskKey taskKey, long generation, long execution) {
      this.task(taskKey, generation);

      if (execution <= 0L) {
        throw new IllegalArgumentException("execution must be greater than zero: " + execution);
      }

      this.taskExecution = execution;
      return this;
    }

    /**
     * Validates and builds the immutable context.
     *
     * @return a new failure context
     */
    public MenuFailureContext build() {
      if (this.revision != null && this.sessionId == null) {
        throw new IllegalStateException("A revision requires a session");
      }

      if ((this.taskGeneration != null || this.taskExecution != null) && this.taskKey == null) {
        throw new IllegalStateException("Task metadata requires a task key");
      }

      if (this.taskExecution != null && this.taskGeneration == null) {
        throw new IllegalStateException("Task execution requires a generation");
      }

      return new MenuFailureContext(this);
    }
  }
}
