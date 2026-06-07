package dev.haniel.menu.paper.render.cache;

import dev.haniel.menu.domain.MenuId;

/**
 * The cache key of a rendered page: which menu, which page, which data version.
 *
 * <p>The version lets an explicit data change invalidate cached visuals without clearing the whole
 * cache. The content hash makes implicit content changes miss as well, so a provider that changes
 * icons without bumping reactive state cannot reuse stale visuals.
 *
 * @param menuId the menu the page belongs to
 * @param page the zero-based page index
 * @param version the data version the visuals were rendered from
 * @param contentHash the ordered hash of the page's item icons
 */
public record PageKey(MenuId menuId, int page, long version, int contentHash) {}
