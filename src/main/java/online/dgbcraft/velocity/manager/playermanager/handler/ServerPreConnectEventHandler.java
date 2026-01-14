package online.dgbcraft.velocity.manager.playermanager.handler;

import com.velocitypowered.api.event.EventHandler;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import net.kyori.adventure.text.Component;
import online.dgbcraft.velocity.manager.playermanager.PlayerManager;

/**
 * @author Sanluli36li
 */
public class ServerPreConnectEventHandler implements EventHandler<ServerPreConnectEvent> {
    private final PlayerManager module;

    public ServerPreConnectEventHandler(PlayerManager module) {
        this.module = module;
    }

    @Override
    public void execute(ServerPreConnectEvent event) {
        String permission = module.getPlugin().getConfiguration()
            .getServerPermission(event.getOriginalServer().getServerInfo().getName());

        // 需要权限, 且玩家没有该权限
        if (permission != null && !event.getPlayer().hasPermission(permission)) {
            // 阻止玩家进入服务器
            event.setResult(ServerPreConnectEvent.ServerResult.denied());
            if (event.getPreviousServer() == null) {

            } else {
                // 告知玩家不被此服务器允许进入
                event.getPlayer().sendMessage(Component.translatable("multiplayer.disconnect.not_whitelisted"));
            }
        }
    }
}
