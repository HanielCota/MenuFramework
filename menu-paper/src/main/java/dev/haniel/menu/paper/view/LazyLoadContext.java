package dev.haniel.menu.paper.view;

import dev.haniel.menu.scheduler.PlayerScheduler;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

/**
 * The platform threads a {@link LazyPageLoad} needs: where to load off-thread and where to apply.
 *
 * @param async the executor for the blocking page load; never the main/region thread
 * @param viewThread the player's scheduler, used to apply the loaded page on the view's thread
 * @param logger the logger for a failed load
 */
public record LazyLoadContext(Executor async, PlayerScheduler viewThread, Logger logger) {}
