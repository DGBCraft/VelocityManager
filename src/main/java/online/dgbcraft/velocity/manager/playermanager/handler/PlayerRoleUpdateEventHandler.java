package online.dgbcraft.velocity.manager.playermanager.handler;

import com.velocitypowered.api.event.EventHandler;
import net.kyori.adventure.text.format.TextColor;
import online.dgbcraft.velocity.manager.Config;
import online.dgbcraft.velocity.manager.playermanager.PlayerManager;
import online.dgbcraft.velocity.manager.playermanager.event.PlayerColorUpdateEvent;
import online.dgbcraft.velocity.manager.playermanager.event.PlayerRoleUpdateEvent;

import java.util.Map;

/**
 * @author Sanluli36li
 */
public class PlayerRoleUpdateEventHandler implements EventHandler<PlayerRoleUpdateEvent> {
    private final PlayerManager module;

    public PlayerRoleUpdateEventHandler(PlayerManager module) {
        this.module = module;
    }

    @Override
    public void execute(PlayerRoleUpdateEvent event) {
        TextColor oldColor = module.getPlayerColor(event.getPlayer());
        // 处理玩家颜色
        for(
            Map.Entry<String, Config.RoleConfig> roleEntry : module.getPlugin().getConfiguration().getRoles().entrySet()
        ) {
            if (event.getRoles().contains(roleEntry.getKey())) {
                TextColor newColor = roleEntry.getValue().getTextColor();
                if (newColor != null) {
                    if (oldColor == null || newColor.compareTo(oldColor) != 0) {
                        // 触发颜色更新
                        module.getPlugin().getProxy().getEventManager().fire(
                            new PlayerColorUpdateEvent(event.getPlayer(), newColor)
                        );
                        module.setPlayerColor(event.getPlayer(), newColor);
                    }
                    return;
                }

            }
        }

        if (oldColor != null) {
            module.setPlayerColor(event.getPlayer(), null);
        }
    }
}
