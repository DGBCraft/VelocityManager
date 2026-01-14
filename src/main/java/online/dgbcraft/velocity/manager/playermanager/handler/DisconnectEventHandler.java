package online.dgbcraft.velocity.manager.playermanager.handler;

import com.velocitypowered.api.event.EventHandler;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import online.dgbcraft.velocity.manager.playermanager.PlayerManager;
import online.dgbcraft.velocity.manager.playermanager.exception.PlayerDataSaveException;

/**
 * @author Sanluli36li
 */
public class DisconnectEventHandler implements EventHandler<DisconnectEvent> {
    private final PlayerManager module;

    public DisconnectEventHandler(PlayerManager module) {
        this.module = module;
    }

    @Override
    public void execute(DisconnectEvent event) {
        // 保存并卸载已储存的玩家信息
        try {
            module.savePlayerData(event.getPlayer());
        } catch (PlayerDataSaveException e) {
            module.getPlugin().getLogger().warn("Can't save player data!", e);
        }

        module.unloadPlayerData(event.getPlayer());
        module.unloadRoles(event.getPlayer());
        module.getPlayerTryServer().remove(event.getPlayer().getUniqueId());
    }
}