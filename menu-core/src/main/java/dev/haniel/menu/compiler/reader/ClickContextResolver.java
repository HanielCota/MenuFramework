package dev.haniel.menu.compiler.reader;

import dev.haniel.menu.action.ClickArgumentResolver;
import dev.haniel.menu.click.ClickContext;

/** Built-in resolver that passes the {@link ClickContext} itself to a button method. */
final class ClickContextResolver implements ClickArgumentResolver {

  @Override
  public boolean supports(Class<?> parameterType) {
    return parameterType == ClickContext.class;
  }

  @Override
  public Object resolve(ClickContext context) {
    return context;
  }
}
