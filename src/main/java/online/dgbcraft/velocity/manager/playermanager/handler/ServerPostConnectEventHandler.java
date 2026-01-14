package online.dgbcraft.velocity.manager.playermanager.handler;

import com.velocitypowered.api.event.EventHandler;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import online.dgbcraft.velocity.manager.playermanager.PlayerManager;
import online.dgbcraft.velocity.manager.playermanager.exception.PlayerDataSaveException;

/**
 * @author Sanluli36li
 */
public class ServerPostConnectEventHandler implements EventHandler<ServerPostConnectEvent> {
    private final PlayerManager module;

    public ServerPostConnectEventHandler(PlayerManager module) {
        this.module = module;
    }

    @Override
    public void execute(ServerPostConnectEvent event) {
        // 移除服务器尝试队列
        module.getPlayerTryServer().remove(event.getPlayer().getUniqueId());

        if (event.getPlayer().getCurrentServer().isPresent()) {
            module.getPlayerData(event.getPlayer().getUniqueId())
                .setLastServer(event.getPlayer().getCurrentServer().get().getServerInfo().getName());

            try {
                module.savePlayerData(event.getPlayer());
            } catch (PlayerDataSaveException ignored) {
                // 最后加入的服务器并不是很重要, 不考虑存不住这种极端情况
            }
        }


    }
}
