package online.dgbcraft.velocity.manager.util;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

public final class TextColorUtil {
    public static TextColor textColorFromString(String color) {
        if (color == null) {
            return null;
        }
        color = color.toLowerCase();
        if (NamedTextColor.NAMES.keys().contains(color)) {
            return NamedTextColor.NAMES.value(color);
        } else {
            return TextColor.fromHexString(color);
        }
    }
}
