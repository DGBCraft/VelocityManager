package online.dgbcraft.velocity.manager.information;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.ServerPing;
import online.dgbcraft.velocity.manager.Config;
import online.dgbcraft.velocity.manager.VelocityManager;

import java.util.UUID;

/**
 * @author Sanluli36li
 */
public class AdvancedPing {
    private static final String ANONYMOUS_PLAYER = "Anonymous Player";
    private static final String DAY_REPLACEMENT = "%day%";
    VelocityManager plugin;

    public AdvancedPing(VelocityManager plugin) {
        this.plugin = plugin;
        plugin.getProxy().getEventManager().register(plugin, this);
    }

    public void reload() {}

    @Subscribe
    public void onProxyPing(ProxyPingEvent event) {
        if (!plugin.getConfiguration().getAdvancedPing().isEnable()) {
            return;
        }
        ServerPing.Builder builder = ServerPing.builder();
        Config.AdvancedPingConfig config;

        // 检查是否存在 ForceHost 相关的 AdvancedPing 配置
        if (event.getConnection().getVirtualHost().isPresent()
            && plugin.getConfiguration().getAdvancedPing().getForceHosts() != null
            && plugin.getConfiguration().getAdvancedPing().getForceHosts()
                .containsKey(event.getConnection().getVirtualHost().get().getHostName())
        ) {
            config = plugin.getConfiguration().getAdvancedPing().getForceHosts()
                .get(event.getConnection().getVirtualHost().get().getHostName());
        } else {
            config = plugin.getConfiguration().getAdvancedPing();
        }

        if (plugin.getProxy().getConfiguration().getFavicon().isPresent()) {
            builder.favicon(plugin.getProxy().getConfiguration().getFavicon().get());
        }

        builder.description(config.getDescription().replaceText(replaceBuilder ->
            replaceBuilder.matchLiteral(DAY_REPLACEMENT).replacement(plugin.getConfiguration().getDayCount() + "")
        ));

        if (config.getCurrentPlayerCount() >= 0) {
            // 固定的在线玩家数
            builder.onlinePlayers(config.getCurrentPlayerCount());
        } else {
            // 在线玩家数设置为负数: 显示当前实际在线人数
            builder.onlinePlayers(plugin.getProxy().getPlayerCount());
        }
        if (config.getMaximumPlayerCount() >= 0) {
            // 固定的最大玩家数
            builder.maximumPlayers(config.getMaximumPlayerCount());
        } else {
            // 如果将最大玩家设置为负数, 则显示的最大玩家数量为 当前玩家数 - 设置的最大玩家数
            // 例如: 设置最大玩家数为 -1, 当前服务器 10 人, 显示最大玩家数为 11 人
            builder.maximumPlayers(plugin.getProxy().getPlayerCount() + (-1 * config.getMaximumPlayerCount()));
        }

        // 构建在线玩家列表
        builder.clearSamplePlayers();
        if (config.isEnablePlayerList()) {
            int i = 0;
            for (Player player : plugin.getProxy().getAllPlayers()) {
                if (config.isAllowAnonymous()
                    && player.getProtocolVersion().getProtocol() >= ProtocolVersion.MINECRAFT_1_18.getProtocol()
                    && !player.getPlayerSettings().isClientListingAllowed()
                ) {
                    // 处理匿名玩家
                    if (config.isPlayerListDisplayServer() && player.getCurrentServer().isPresent()) {
                        builder.samplePlayers(new ServerPing.SamplePlayer(
                            "[" + player.getCurrentServer().get().getServerInfo().getName() + "] " + ANONYMOUS_PLAYER,
                            new UUID(0L, 0L)
                        ));
                    } else {
                        builder.samplePlayers(ServerPing.SamplePlayer.ANONYMOUS);
                    }
                } else {
                    if (config.isPlayerListDisplayServer() && player.getCurrentServer().isPresent()) {
                        builder.samplePlayers(new ServerPing.SamplePlayer(
                            "[" + player.getCurrentServer().get().getServerInfo().getName() + "] "
                                + player.getUsername(),
                            player.getUniqueId()
                        ));
                    } else {
                        builder.samplePlayers(new ServerPing.SamplePlayer(player.getUsername(), player.getUniqueId()));
                    }
                }
                i++;
                if (i >= 10) {
                    break;
                }
            }
        }

        // 处理玩家版本信息
        int protocol = event.getConnection().getProtocolVersion().getProtocol();
        if (protocol == -1 || protocol > config.getMaximumProtocolVersion()) {
            // 客户端版本较新 或 未知客户端版本: 返回支持的最新版本
            builder.version(new ServerPing.Version(config.getMaximumProtocolVersion(), config.getVersionName()));
        } else if (protocol < config.getMinimumProtocolVersion()) {
            // 客户端版本较旧: 返回支持的最旧版本
            builder.version(new ServerPing.Version(config.getMinimumProtocolVersion(), config.getVersionName()));
        } else {
            // 返回客户端当前版本
            builder.version(new ServerPing.Version(protocol, config.getVersionName()));
        }

        event.setPing(builder.build());
    }
}