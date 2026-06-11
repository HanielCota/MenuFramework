package dev.haniel.menu.paper.refresh;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.haniel.menu.paper.annotation.RefreshOn;
import java.util.Set;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.junit.jupiter.api.Test;

class RefreshEventsTest {

  @Test
  void readsEveryDeclaredEvent() {
    Set<Class<? extends Event>> events = RefreshEvents.of(TwoEvents.class);

    assertEquals(Set.of(PlayerJoinEvent.class, PlayerQuitEvent.class), events);
  }

  @Test
  void isEmptyWhenAnnotationAbsent() {
    assertTrue(RefreshEvents.of(NoAnnotation.class).isEmpty());
  }

  @Test
  void deduplicatesRepeatedEvents() {
    assertEquals(Set.of(PlayerJoinEvent.class), RefreshEvents.of(DuplicateEvent.class));
  }

  @RefreshOn({PlayerJoinEvent.class, PlayerQuitEvent.class})
  private static final class TwoEvents {}

  private static final class NoAnnotation {}

  @RefreshOn({PlayerJoinEvent.class, PlayerJoinEvent.class})
  private static final class DuplicateEvent {}
}
