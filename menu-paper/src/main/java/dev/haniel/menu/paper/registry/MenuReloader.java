package dev.haniel.menu.paper.registry;

import dev.haniel.menu.compiler.MenuCompiler;
import dev.haniel.menu.compiler.model.CompiledMenu;
import dev.haniel.menu.compiler.model.CompiledPagedMenu;
import dev.haniel.menu.config.MenuConfig;
import dev.haniel.menu.domain.MenuId;
import dev.haniel.menu.paper.hook.HookDefinitions;
import dev.haniel.menu.paper.view.MenuFactory;
import dev.haniel.menu.paper.view.PaperMenu;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.inventory.ItemStack;

/**
 * Recompiles registered menus and swaps the openable atomically.
 *
 * <p>Splits the reload concern out of {@link MenuRegistry}: it owns the synchronous, all-at-once
 * and split async (load off-thread, apply on-thread) reload flows, each aggregating per-menu
 * failures into a {@link ReloadReport} so one bad YAML never aborts the rest.
 */
final class MenuReloader {

  private final MenuCompiler<ItemStack> compiler;
  private final MenuFactory factory;
  private final MenuCatalog catalog;
  private final Logger logger;

  MenuReloader(
      MenuCompiler<ItemStack> compiler, MenuFactory factory, MenuCatalog catalog, Logger logger) {
    this.compiler = Objects.requireNonNull(compiler, "compiler");
    this.factory = Objects.requireNonNull(factory, "factory");
    this.catalog = Objects.requireNonNull(catalog, "catalog");
    this.logger = Objects.requireNonNull(logger, "logger");
  }

  boolean reload(MenuId id) {
    Optional<RegisteredMenu> found = catalog.find(id);
    if (found.isEmpty()) {
      logger.warning("Cannot reload unknown menu '" + id.value() + "'");
      return false;
    }
    return reloadOne(found.get()).successful();
  }

  ReloadReport reloadReport(MenuId id) {
    return catalog.find(id).map(this::reloadOne).orElseGet(ReloadReport::empty);
  }

  ReloadReport reloadAllReport() {
    List<MenuId> reloaded = new ArrayList<>();
    List<ReloadFailure> failures = new ArrayList<>();
    catalog.all().forEach(menu -> reloadInto(menu, reloaded, failures));
    return new ReloadReport(reloaded, failures);
  }

  CompletableFuture<ReloadReport> reloadAllReportAsync(
      Executor asyncExecutor, Executor syncExecutor) {
    return CompletableFuture.supplyAsync(this::prepareAll, asyncExecutor)
        .thenApplyAsync(this::applyPrepared, syncExecutor);
  }

  private ReloadReport reloadOne(RegisteredMenu menu) {
    List<MenuId> reloaded = new ArrayList<>();
    List<ReloadFailure> failures = new ArrayList<>();
    reloadInto(menu, reloaded, failures);
    return new ReloadReport(reloaded, failures);
  }

  private void reloadInto(
      RegisteredMenu menu, List<MenuId> reloaded, List<ReloadFailure> failures) {
    try {
      menu.swap(openable(menu.sourceType(), compile(menu)));
      reloaded.add(menu.id());
    } catch (RuntimeException exception) {
      logger.log(Level.WARNING, "Failed to reload menu '" + menu.id().value() + "'", exception);
      failures.add(ReloadFailure.from(menu.id(), exception));
    }
  }

  private PreparedReloads prepareAll() {
    List<PreparedReload> prepared = new ArrayList<>();
    List<ReloadFailure> failures = new ArrayList<>();
    catalog.all().forEach(menu -> prepareInto(menu, prepared, failures));
    return new PreparedReloads(prepared, failures);
  }

  private void prepareInto(
      RegisteredMenu menu, List<PreparedReload> prepared, List<ReloadFailure> failures) {
    try {
      prepared.add(new PreparedReload(menu, compiler.load(menu.id())));
    } catch (RuntimeException exception) {
      logger.log(Level.WARNING, "Failed to load menu '" + menu.id().value() + "'", exception);
      failures.add(ReloadFailure.from(menu.id(), exception));
    }
  }

  private ReloadReport applyPrepared(PreparedReloads prepared) {
    List<MenuId> reloaded = new ArrayList<>();
    List<ReloadFailure> failures = new ArrayList<>(prepared.failures());
    prepared.reloads().forEach(reload -> applyInto(reload, reloaded, failures));
    return new ReloadReport(reloaded, failures);
  }

  private void applyInto(
      PreparedReload reload, List<MenuId> reloaded, List<ReloadFailure> failures) {
    try {
      RegisteredMenu menu = reload.menu();
      menu.swap(openable(menu.sourceType(), compile(menu, reload.config())));
      reloaded.add(menu.id());
    } catch (RuntimeException exception) {
      MenuId id = reload.menu().id();
      logger.log(Level.WARNING, "Failed to apply reloaded menu '" + id.value() + "'", exception);
      failures.add(ReloadFailure.from(id, exception));
    }
  }

  private PaperMenu openable(Class<?> sourceType, CompiledMenu<ItemStack> compiled) {
    if (compiled instanceof CompiledPagedMenu<?>) {
      HookDefinitions.of(sourceType);
    }
    return factory.create(compiled);
  }

  private CompiledMenu<ItemStack> compile(RegisteredMenu menu) {
    if (menu.source() != null) {
      return compiler.compile(menu.source());
    }
    return compiler.compile(menu.sourceType(), ignored -> menu.createSource());
  }

  private CompiledMenu<ItemStack> compile(RegisteredMenu menu, MenuConfig config) {
    if (menu.source() != null) {
      return compiler.compile(menu.source(), config);
    }
    return compiler.compile(menu.sourceType(), ignored -> menu.createSource(), config);
  }

  private record PreparedReload(RegisteredMenu menu, MenuConfig config) {}

  private record PreparedReloads(List<PreparedReload> reloads, List<ReloadFailure> failures) {}
}
