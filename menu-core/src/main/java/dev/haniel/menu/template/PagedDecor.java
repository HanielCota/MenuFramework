package dev.haniel.menu.template;

/**
 * The pre-rendered, static visuals of a paginated menu: navigation and border.
 *
 * <p>Built once at merge time and reused for every page and player.
 *
 * @param previous the previous-page control visual, or null when the mask has no previous control
 * @param next the next-page control visual, or null when the mask has no next control
 * @param border the static border filler visual
 * @param <V> the platform visual type
 */
public record PagedDecor<V>(V previous, V next, V border) {}
