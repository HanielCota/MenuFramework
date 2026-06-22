package com.hanielfialho.menuframework.api.error;

/**
 * Runtime stage at which a non-fatal failure was observed.
 *
 * <p>Enum constants are stable identifiers suitable for metrics, alerting and log aggregation.
 */
public enum MenuFailureOperation {

  /** Opening a new session. */
  OPEN("open a menu"),

  /** Rendering requested through {@code MenuManager#refresh}. */
  REFRESH_RENDER("render a menu refresh"),

  /** Preparing forward navigation. */
  NAVIGATION("prepare forward menu navigation"),

  /** Preparing restoration of the previous history entry. */
  BACK_NAVIGATION("prepare backward menu navigation"),

  /** Closing the session-owned inventory view. */
  INVENTORY_CLOSE("close a menu inventory"),

  /** Cleaning up after an unsuccessful open transition. */
  FAILED_OPEN_CLEANUP("clean up a failed menu opening"),

  /** Executing {@code Menu#onOpen}. */
  OPEN_CALLBACK("execute Menu#onOpen"),

  /** Executing {@code Menu#onClose}. */
  CLOSE_CALLBACK("execute Menu#onClose"),

  /** Executing a click handler. */
  CLICK_HANDLER("execute a menu click handler"),

  /** Rendering requested by a click handler. */
  CLICK_RENDER("render a menu after a click"),

  /** Emitting transactional menu feedback. */
  FEEDBACK("emit menu feedback"),

  /** Reserving and starting a session-owned asynchronous task. */
  ASYNC_TASK_START("start an asynchronous menu task"),

  /** Applying an asynchronous start transition. */
  ASYNC_START_TRANSITION("apply an asynchronous start transition"),

  /** Rendering an asynchronous loading state. */
  ASYNC_LOADING_RENDER("render an asynchronous loading state"),

  /** Executing the consumer-provided asynchronous operation. */
  ASYNC_OPERATION("execute an asynchronous menu operation"),

  /** Scheduling asynchronous completion on the entity scheduler. */
  ASYNC_COMPLETION_SCHEDULING("schedule asynchronous menu completion"),

  /** Applying an asynchronous success transition. */
  ASYNC_SUCCESS_TRANSITION("apply an asynchronous success transition"),

  /** Rendering an asynchronous success state. */
  ASYNC_SUCCESS_RENDER("render an asynchronous success state"),

  /** Applying an asynchronous failure transition. */
  ASYNC_FAILURE_TRANSITION("apply an asynchronous failure transition"),

  /** Rendering an asynchronous failure state. */
  ASYNC_FAILURE_RENDER("render an asynchronous failure state"),

  /** Reserving and starting a session-owned periodic task. */
  PERIODIC_TASK_START("start a periodic menu task"),

  /** Scheduling a periodic task. */
  PERIODIC_TASK_SCHEDULING("schedule a periodic menu task"),

  /** Incrementing a periodic execution counter. */
  PERIODIC_EXECUTION_COUNTER("increment a periodic task execution counter"),

  /** Executing a periodic task callback. */
  PERIODIC_EXECUTION("execute a periodic menu task"),

  /** Rendering requested by a periodic task. */
  PERIODIC_RENDER("render a periodic menu task");

  private final String description;

  MenuFailureOperation(String description) {
    this.description = description;
  }

  /**
   * Returns a stable human-readable English description.
   *
   * @return operation description
   */
  public String description() {
    return this.description;
  }
}
