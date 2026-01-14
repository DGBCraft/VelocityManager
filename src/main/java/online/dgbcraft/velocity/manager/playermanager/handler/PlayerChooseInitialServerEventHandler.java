package online.dgbcraft.velocity.manager.playermanager.handler;

import com.velocitypowered.api.event.EventHandler;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;
import online.dgbcraft.velocity.manager.playermanager.PlayerManager;

import static online.dgbcraft.velocity.manager.constant.LocaleKeys.PROXY_ERROR_NO_AVAILABLE_SERVER;

/**
 * @author Sanluli36li
 */
public class PlayerChooseInitialServerEventHandler implements EventHandler<PlayerChooseInitialServerEvent> {
    private final PlayerManager module;

    public PlayerChooseInitialServerEventHandler(PlayerManager module) {
        this.module = module;
    }

    @Override
    public void execute(PlayerChooseInitialServerEvent event) {
        RegisteredServer server = module.getTryServer(event.getPlayer());

        if (server == null) {
            // 无路可退了, 断开玩家
            event.getPlayer().disconnect(Component.translatable(PROXY_ERROR_NO_AVAILABLE_SERVER));
        } else {
            event.setInitialServer(server);
        }
    }
}
