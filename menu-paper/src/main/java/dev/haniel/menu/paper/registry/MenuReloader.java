package dev.haniel.menu.paper.registry;

import dev.haniel.menu.compiler.MenuCompiler;
import dev.haniel.menu.compiler.model.CompiledMenu;
import dev.haniel.menu.compiler.model.CompiledStaticMenu;
import dev.haniel.menu.config.MenuConfig;
import dev.haniel.menu.domain.MenuId;
import dev.haniel.menu.paper.hook.HookDefinitions;
import dev.haniel.menu.paper.view.MenuFactory;
import dev.haniel.menu.paper.view.PaperMenu;
import dev.haniel.menu.paper.view.StaticPaperMenu;
import dev.haniel.menu.paper.visibility.StaticVisibility;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
        .thenApplyAsync(this::applyPrepared, syncExecutor)
        .whenComplete(this::logPipelineFailure);
  }

  // Per-menu failures land in the report; a pipeline-level failure (executor rejected after
  // shutdown, unexpected throwable) would otherwise vanish unless every caller attaches a handler.
  private void logPipelineFailure(ReloadReport report, Throwable failure) {
    if (failure != null) {
      logger.log(Level.WARNING, "Async menu reload did not complete", failure);
    }
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
      menu.swap(openable(menu, compile(menu)));
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
      menu.swap(openable(menu, compile(menu, reload.config())));
      reloaded.add(menu.id());
    } catch (RuntimeException exception) {
      MenuId id = reload.menu().id();
      logger.log(Level.WARNING, "Failed to apply reloaded menu '" + id.value() + "'", exception);
      failures.add(ReloadFailure.from(id, exception));
    }
  }

  private PaperMenu openable(RegisteredMenu menu, CompiledMenu<ItemStack> compiled) {
    if (compiled instanceof CompiledStaticMenu<ItemStack> staticMenu) {
      return withVisibility(factory.create(compiled), menu, staticMenu.buttonSlots());
    }
    HookDefinitions.of(menu.sourceType());
    return factory.create(compiled);
  }

  private PaperMenu withVisibility(
      PaperMenu openable, RegisteredMenu menu, Map<String, Integer> buttonSlots) {
    if (openable instanceof StaticPaperMenu staticView) {
      return staticView.withVisibility(
          StaticVisibility.of(menu.sourceType(), menu.createSource(), buttonSlots));
    }
    return openable;
  }

  private CompiledMenu<ItemStack> compile(RegisteredMenu menu) {
    Object source = menu.source();
    if (source != null) {
      return compiler.compile(source);
    }
    return compiler.compile(menu.sourceType(), ignored -> menu.createSource());
  }

  private CompiledMenu<ItemStack> compile(RegisteredMenu menu, MenuConfig config) {
    Object source = menu.source();
    if (source != null) {
      return compiler.compile(source, config);
    }
    return compiler.compile(menu.sourceType(), ignored -> menu.createSource(), config);
  }

  private record PreparedReload(RegisteredMenu menu, MenuConfig config) {}

  private record PreparedReloads(List<PreparedReload> reloads, List<ReloadFailure> failures) {}
}
