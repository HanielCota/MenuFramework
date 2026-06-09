package dev.haniel.menu.paper.placeholder;

import dev.haniel.menu.domain.PlayerId;
import dev.haniel.menu.placeholder.PlaceholderResolver;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * A {@link PlaceholderResolver} backed by PlaceholderAPI, as a soft dependency.
 *
 * <p>When PlaceholderAPI is not installed, or the text has no placeholder, or the viewer is
 * offline, the text is returned unchanged. The PlaceholderAPI types are touched only through {@link
 * Papi}, which is class-loaded lazily, so the framework runs fine without the plugin on the
 * classpath.
 *
 * <p><strong>Security:</strong> each {@code %token%} is resolved on its own and its value is
 * MiniMessage-escaped before being substituted back, so player-controlled data (names, nicknames,
 * chat-derived placeholders) cannot inject live MiniMessage tags such as {@code <click>} or {@code
 * <hover>} into another viewer's menu. The surrounding template text — including the author's own
 * tags — is left untouched and still parsed normally.
 */
public final class PapiPlaceholders implements PlaceholderResolver {

  private static final String PLUGIN = "PlaceholderAPI";
  private static final Pattern TOKEN = Pattern.compile("%[^%]+%");

  private final MiniMessage miniMessage;

  /** Creates a resolver that escapes placeholder values with the shared MiniMessage instance. */
  public PapiPlaceholders() {
    this(MiniMessage.miniMessage());
  }

  /**
   * Creates a resolver that escapes placeholder values with the given MiniMessage instance.
   *
   * @param miniMessage the serializer whose escaping makes resolved values inert; never null
   */
  public PapiPlaceholders(MiniMessage miniMessage) {
    this.miniMessage = Objects.requireNonNull(miniMessage, "miniMessage");
  }

  @Override
  public String resolve(PlayerId player, String text) {
    if (text.indexOf('%') < 0 || !available()) {
      return text;
    }
    Player online = Bukkit.getPlayer(player.value());
    if (online == null) {
      return text;
    }
    return substitute(online, text);
  }

  private String substitute(Player online, String text) {
    Matcher matcher = TOKEN.matcher(text);
    StringBuilder out = new StringBuilder();
    while (matcher.find()) {
      String value = miniMessage.escapeTags(Papi.apply(online, matcher.group()));
      matcher.appendReplacement(out, Matcher.quoteReplacement(value));
    }
    matcher.appendTail(out);
    return out.toString();
  }

  private static boolean available() {
    return Bukkit.getPluginManager().isPluginEnabled(PLUGIN);
  }
}
