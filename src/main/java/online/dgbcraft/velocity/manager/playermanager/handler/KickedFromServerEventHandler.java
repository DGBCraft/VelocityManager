package online.dgbcraft.velocity.manager.playermanager.handler;

import com.velocitypowered.api.event.EventHandler;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;
import online.dgbcraft.velocity.manager.playermanager.PlayerManager;

import static online.dgbcraft.velocity.manager.constant.LocaleKeys.PROXY_ERROR_NO_AVAILABLE_SERVER;

/**
 * @author Sanluli36li
 */
public class KickedFromServerEventHandler implements EventHandler<KickedFromServerEvent> {
    private final PlayerManager module;

    public KickedFromServerEventHandler(PlayerManager module) {
        this.module = module;
    }

    @Override
    public void execute(KickedFromServerEvent event) {
        if (event.kickedDuringServerConnect()) {
            //
        } else {
            // 游玩过程中被踢: 从尝试序列中选择服务器
            RegisteredServer server = module.getTryServer(event.getPlayer(), event.getServer());

            if (server == null) {
                // 无路可退了, 断开玩家
                event.setResult(KickedFromServerEvent.DisconnectPlayer.create(
                    Component.translatable(PROXY_ERROR_NO_AVAILABLE_SERVER)
                ));
            } else {
                event.setResult(KickedFromServerEvent.RedirectPlayer.create(server));
            }
        }
    }
}
