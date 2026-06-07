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

class ClickArgumentsTest {

  private final ClickArguments builtins = new ClickArguments(List.of());

  @Test
  void yieldsEmptyArgumentsForNoArgButton() {
    ButtonArguments arguments = builtins.bindingFor(method("noArgs"));

    assertEquals(0, arguments.forContext(context()).length);
  }

  @Test
  void resolvesClickContextParameterFromTheClick() {
    ClickContext context = context();
    ButtonArguments arguments = builtins.bindingFor(method("withContext", ClickContext.class));

    Object[] resolved = arguments.forContext(context);

    assertEquals(1, resolved.length);
    assertSame(context, resolved[0]);
  }

  @Test
  void usesPlatformResolverForCustomParameterType() {
    ClickArguments registry = new ClickArguments(List.of(new StringResolver()));
    ButtonArguments arguments = registry.bindingFor(method("withString", String.class));

    assertEquals("resolved", arguments.forContext(context())[0]);
  }

  @Test
  void rejectsNonVoidButton() {
    InvalidMenuException error =
        assertThrows(InvalidMenuException.class, () -> builtins.bindingFor(method("returnsValue")));
    assertTrue(error.getMessage().contains("must return void"));
  }

  @Test
  void rejectsButtonWithMoreThanOneParameter() {
    Method method = method("twoParams", ClickContext.class, ClickContext.class);
    InvalidMenuException error =
        assertThrows(InvalidMenuException.class, () -> builtins.bindingFor(method));
    assertTrue(error.getMessage().contains("no parameter or a single one"));
  }

  @Test
  void rejectsUnsupportedParameterType() {
    InvalidMenuException error =
        assertThrows(
            InvalidMenuException.class,
            () -> builtins.bindingFor(method("withString", String.class)));
    assertTrue(error.getMessage().contains("not injectable"));
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

  static final class StringResolver implements ClickArgumentResolver {

    @Override
    public boolean supports(Class<?> parameterType) {
      return parameterType == String.class;
    }

    @Override
    public Object resolve(ClickContext context) {
      return "resolved";
    }
  }

  @SuppressWarnings("unused")
  static final class Buttons {

    void noArgs() {}

    void withContext(ClickContext context) {}

    void withString(String value) {}

    String returnsValue() {
      return "";
    }

    void twoParams(ClickContext first, ClickContext second) {}
  }
}
