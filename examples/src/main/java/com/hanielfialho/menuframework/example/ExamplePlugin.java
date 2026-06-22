package com.hanielfialho.menuframework.example;

import com.hanielfialho.menuframework.MenuFramework;
import com.hanielfialho.menuframework.MenuManager;
import com.hanielfialho.menuframework.api.pagination.PageRequest;
import com.hanielfialho.menuframework.api.pagination.PageSlice;
import com.hanielfialho.menuframework.example.menu.AsyncProductMenu;
import com.hanielfialho.menuframework.example.menu.ConfirmationMenu;
import com.hanielfialho.menuframework.example.menu.CountdownMenu;
import com.hanielfialho.menuframework.example.menu.CounterMenu;
import com.hanielfialho.menuframework.example.menu.Product;
import com.hanielfialho.menuframework.example.menu.SettingsMenu;
import com.hanielfialho.menuframework.example.menu.SynchronousProductMenu;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/** Exemplo de integração do framework com o ciclo de vida do plugin. */
public final class ExamplePlugin extends JavaPlugin {

  private MenuFramework menuFramework;
  private CounterMenu counterMenu;
  private AsyncProductMenu asyncProductMenu;
  private SettingsMenu settingsMenu;
  private CountdownMenu countdownMenu;
  private ConfirmationMenu confirmationMenu;
  private List<Product> products;

  @Override
  public void onEnable() {
    this.menuFramework = MenuFramework.create(this);

    List<Product> products = new ArrayList<>(100);

    for (int index = 1; index <= 100; index++) {
      products.add(new Product(index, Material.DIAMOND, "Produto " + index, index * 10));
    }

    this.products = List.copyOf(products);

    SynchronousProductMenu productMenu = new SynchronousProductMenu(this.products);

    this.counterMenu = new CounterMenu(productMenu);
    this.asyncProductMenu =
        new AsyncProductMenu(
            (context, request) -> CompletableFuture.completedFuture(this.loadProductPage(request)));
    this.settingsMenu = new SettingsMenu();
    this.countdownMenu = new CountdownMenu();
    this.confirmationMenu =
        new ConfirmationMenu(
            player -> player.sendMessage(Component.text("Ação confirmada", NamedTextColor.GREEN)));
  }

  @Override
  public void onDisable() {
    if (this.menuFramework != null) {
      this.menuFramework.shutdown();
    }
  }

  public MenuManager menus() {
    return this.menuFramework.menus();
  }

  public void openCounter(Player player) {
    this.menus().open(player, this.counterMenu, new CounterMenu.State(0));
  }

  public void openAsyncProducts(Player player) {
    this.menus().open(player, this.asyncProductMenu, AsyncProductMenu.State.initial(""));
  }

  public void openSettings(Player player) {
    this.menus().open(player, this.settingsMenu, new SettingsMenu.State(true, true, false));
  }

  public void openCountdown(Player player) {
    this.menus().open(player, this.countdownMenu, new CountdownMenu.State(10));
  }

  public void openConfirmation(Player player) {
    this.menus().open(player, this.confirmationMenu, new ConfirmationMenu.State("Confirmar ação?"));
  }

  /*
   * Este método é chamado pelo scheduler assíncrono do framework. Em um
   * plugin real, faça aqui a consulta JDBC/HTTP e retorne somente DTOs
   * imutáveis, sem acessar Player, World, Inventory ou ItemStack compartilhado.
   */
  private PageSlice<Product> loadProductPage(PageRequest request) {
    if (this.products.isEmpty()) {
      return PageSlice.knownTotal(request, List.of(), 0L);
    }

    int from = Math.toIntExact(request.offset());
    int to = Math.min(this.products.size(), from + request.size());

    if (from >= this.products.size()) {
      throw new IllegalArgumentException("Requested page starts outside the result set");
    }

    return PageSlice.knownTotal(request, this.products.subList(from, to), this.products.size());
  }
}
