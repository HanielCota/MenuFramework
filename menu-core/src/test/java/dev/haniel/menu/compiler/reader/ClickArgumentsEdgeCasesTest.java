package dev.haniel.menu.compiler.reader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.haniel.menu.action.ButtonArguments;
import dev.haniel.menu.action.ClickArgumentResolver;
import dev.haniel.menu.click.ClickContext;
import dev.haniel.menu.click.ClickType;
import dev.haniel.menu.compiler.InvalidMenuException;
import dev.haniel.menu.domain.PlayerId;
import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Adversarial cases for {@link ClickArguments}: resolver precedence (built-in first), per-click
 * resolution returning the live context, primitive parameters, and the failure modes around
 * unsupported/over-arity signatures.
 */
class ClickArgumentsEdgeCasesTest {

  @Test
  void builtinContextResolverWinsOverAPlatformResolverForTheSameType() {
    // A rogue platform resolver also claims ClickContext, but the built-in is registered first.
    ClickArgumentResolver rogue =
        new ClickArgumentResolver() {
          @Override
          public boolean supports(Class<?> parameterType) {
            return parameterType == ClickContext.class;
          }

          @Override
          public Object resolve(ClickContext context) {
            return "ROGUE";
          }
        };
    ClickArguments registry = new ClickArguments(List.of(rogue));
    ClickContext context = context();

    Object[] resolved =
        registry.bindingFor(method("withContext", ClickContext.class)).forContext(context);

    assertSame(context, resolved[0], "the built-in ClickContext resolver must take precedence");
  }

  @Test
  void resolvesTheLiveContextPerClickNotABootSnapshot() {
    ClickArguments registry = new ClickArguments(List.of());
    ButtonArguments arguments = registry.bindingFor(method("withContext", ClickContext.class));
    ClickContext firstClick = context();
    ClickContext secondClick = context();

    assertSame(firstClick, arguments.forContext(firstClick)[0]);
    assertSame(secondClick, arguments.forContext(secondClick)[0]);
  }

  @Test
  void firstMatchingResolverWinsAmongSeveralPlatformResolvers() {
    ClickArguments registry =
        new ClickArguments(
            List.of(new TaggedStringResolver("first"), new TaggedStringResolver("second")));

    Object[] resolved =
        registry.bindingFor(method("withString", String.class)).forContext(context());

    assertEquals("first", resolved[0], "findFirst must keep registration order");
  }

  @Test
  void rejectsPrimitiveParameterWhenNoResolverSupportsIt() {
    ClickArguments registry = new ClickArguments(List.of());

    InvalidMenuException error =
        assertThrows(
            InvalidMenuException.class, () -> registry.bindingFor(method("withInt", int.class)));
    assertTrue(error.getMessage().contains("not injectable"));
  }

  @Test
  void nonVoidIsCheckedBeforeArityOrResolver() {
    // A method that both returns a value AND has an unsupported param: void check fires first.
    ClickArguments registry = new ClickArguments(List.of());

    InvalidMenuException error =
        assertThrows(
            InvalidMenuException.class,
            () -> registry.bindingFor(method("returnsAndTakesString", String.class)));
    assertTrue(error.getMessage().contains("must return void"));
  }

  private static Method method(String name, Class<?>... parameters) {
    try {
      return Buttons.class.getDeclaredMethod(name, parameters);
    } catch (NoSuchMethodException exception) {
      throw new IllegalStateException(exception);
    }
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

  private static final class TaggedStringResolver implements ClickArgumentResolver {
    private final String tag;

    TaggedStringResolver(String tag) {
      this.tag = tag;
    }

    @Override
    public boolean supports(Class<?> parameterType) {
      return parameterType == String.class;
    }

    @Override
    public Object resolve(ClickContext context) {
      return tag;
    }
  }

  @SuppressWarnings("unused")
  static final class Buttons {

    void withContext(ClickContext context) {}

    void withString(String value) {}

    void withInt(int value) {}

    String returnsAndTakesString(String value) {
      return "";
    }
  }
}
