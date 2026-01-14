package online.dgbcraft.velocity.manager.api;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.format.TextColor;
import online.dgbcraft.velocity.manager.VelocityManager;

/**
 * @author Sanluli36li
 */
public class VelocityManagerAPI {


    public static TextColor getPlayerColor(Player player) {
        return VelocityManager.instance.getPlayerManager().getPlayerColor(player);
    }

    public static TextColor getServerColor(RegisteredServer server) {
        return VelocityManager.instance.getConfiguration().getServerColor(server.getServerInfo().getName());
    }

    private VelocityManagerAPI() {}
}
