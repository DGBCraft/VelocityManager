package online.dgbcraft.velocity.manager.playermanager.handler;

import com.velocitypowered.api.event.EventHandler;
import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import online.dgbcraft.velocity.manager.playermanager.PlayerManager;
import online.dgbcraft.velocity.manager.playermanager.data.BannedData;
import online.dgbcraft.velocity.manager.playermanager.exception.PlayerDataSaveException;

import static online.dgbcraft.velocity.manager.constant.LocaleKeys.*;
import static online.dgbcraft.velocity.manager.constant.PermissionKeys.PERMISSION_PLUGIN_LOGIN;
import static online.dgbcraft.velocity.manager.util.gson.typeadapter.DateTypeAdapter.DATE_FORMAT;

/**
 * @author Sanluli36li
 */
public class LoginEventHandler implements EventHandler<LoginEvent> {
    private final PlayerManager module;

    public LoginEventHandler(PlayerManager module) {
        this.module = module;
    }

    private Component checkBanned(Player player) {
        Component component;
        // 检查玩家封禁情况
        BannedData banData = module.getBannedData(player.getUniqueId());
        if (banData != null) {
            // 封禁信息验证: 要求文件名和封禁内容一致, 如果内容不一致或已过期,
            if ((banData.getBannedType() == BannedData.BannedType.PLAYER && banData.getUuid() == player.getUniqueId())
                || banData.isExpired()
            ) {
                try {
                    module.archiveBannedData(player.getUniqueId());
                } catch (PlayerDataSaveException ignored) {}
            } else {
                if (banData.getReason() != null) {
                    component = Component.translatable(DISCONNECT_BANNED_REASON, Component.text(banData.getReason()));
                } else {
                    component = Component.translatable(DISCONNECT_BANNED);
                }
                if (banData.getExpires() != null) {
                    return component.append(Component.translatable(
                        DISCONNECT_BANNED_EXPIRATION,
                        Component.text(DATE_FORMAT.format(banData.getExpires()))
                    ));
                } else {
                    return component;
                }
            }
        }
        // 检查IP封禁情况
        banData = module.getBannedData(player.getRemoteAddress());
        if (banData != null) {
            if ((banData.getBannedType() == BannedData.BannedType.IP) || banData.isExpired()) {
                try {
                    module.archiveBannedData(player.getRemoteAddress());
                } catch (PlayerDataSaveException ignored) {}
            } else {
                if (banData.getReason() != null) {
                    component = Component.translatable(DISCONNECT_BANNED_IP_REASON, Component.text(banData.getReason()));
                } else {
                    component = Component.translatable(DISCONNECT_BANNED_IP_REASON, Component.text("Unknown"));
                }
                if (banData.getExpires() != null) {
                    return component.append(Component.translatable(
                        DISCONNECT_BANNED_IP_EXPIRATION,
                        Component.text(DATE_FORMAT.format(banData.getExpires()))
                    ));
                } else {
                    return component;
                }
            }
        }

        return null;
    }

    @Override
    public void execute(LoginEvent event) {
        Player player = event.getPlayer();

        // 检查玩家是否被封禁
        Component banComponent = checkBanned(player);
        if (banComponent != null) {
            event.setResult(ResultedEvent.ComponentResult.denied(banComponent));
        }

        //载入玩家信息
        module.loadPlayer(player);
        module.loadRoles(player);

        // 检查玩家是否有登录权限, 否则将玩家踢出并提示"未被服务器列入白名单"
        // 如果服务器公开, 将`vmanager.login`权限赋予给`@everyone`身份即可
        if (!module.hasPermission(player, PERMISSION_PLUGIN_LOGIN)) {
            event.setResult(ResultedEvent.ComponentResult.denied(Component.translatable(DISCONNECT_NOT_WHITELISTED)));
        }
    }
}