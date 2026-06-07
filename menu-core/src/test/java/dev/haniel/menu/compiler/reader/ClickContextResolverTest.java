package dev.haniel.menu.compiler.reader;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.haniel.menu.click.ClickContext;
import dev.haniel.menu.click.ClickType;
import dev.haniel.menu.domain.PlayerId;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ClickContextResolverTest {

  private final ClickContextResolver resolver = new ClickContextResolver();

  @Test
  void supportsTheClickContextType() {
    assertTrue(resolver.supports(ClickContext.class));
  }

  @Test
  void rejectsOtherTypes() {
    assertFalse(resolver.supports(String.class));
    assertFalse(resolver.supports(PlayerId.class));
  }

  @Test
  void resolvesToTheClickContextItself() {
    ClickContext context = context();

    assertSame(context, resolver.resolve(context));
  }

  private static ClickContext context() {
    return new ClickContext() {
      @Override
      public PlayerId player() {
        return new PlayerId(UUID.randomUUID());
      }

      @Override
      public ClickType clickType() {
        return ClickType.LEFT;
      }
    };
  }
}
