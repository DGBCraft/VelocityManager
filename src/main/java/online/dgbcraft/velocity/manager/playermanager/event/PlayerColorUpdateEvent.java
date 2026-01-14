package online.dgbcraft.velocity.manager.playermanager.event;

import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.format.TextColor;

/**
 * @author Sanluli36li
 */
public class PlayerColorUpdateEvent {

    private final Player player;
    private final TextColor color;

    public PlayerColorUpdateEvent(Player player, TextColor color) {
        this.player = player;
        this.color = color;
    }

    public Player getPlayer() {
        return player;
    }

    public TextColor getColor() {
        return color;
    }
}
