package online.dgbcraft.velocity.manager.information;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.player.TabListEntry;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;
import online.dgbcraft.velocity.manager.Config;
import online.dgbcraft.velocity.manager.VelocityManager;
import online.dgbcraft.velocity.manager.playermanager.event.PlayerColorUpdateEvent;

import java.util.concurrent.TimeUnit;

/**
 * @author Sanluli36li
 */
public class GlobalTabList {
    private final VelocityManager plugin;
    private Component displayName;

    public GlobalTabList(VelocityManager plugin) {
        this.plugin = plugin;
        this.plugin.getProxy().getEventManager().register(plugin, this);
    }

    public void reload() {
        displayName = plugin.getConfiguration().getTabList().getPlayerNameFormat();
    }

    @Subscribe
    public void onServerPostConnect(ServerPostConnectEvent event) {
        if (plugin.getConfiguration().getTabList().isClearHeaderAndFooterWhenSwitchServer()) {
            // 清除Tab列表的页眉和页脚
            // 避免前一个服务器的Tab页眉和页脚影响后一个服务器
            event.getPlayer().getTabList().clearHeaderAndFooter();
        }

        if (plugin.getConfiguration().getTabList().isEnableGlobalTabList()) {
            Player player = event.getPlayer();
            if (player.getCurrentServer().isPresent()) {
                // 1秒后执行 避免出问题
                plugin.getProxy().getScheduler().buildTask(plugin, () -> {
                    for (Player player2 : plugin.getProxy().getAllPlayers()) {
                        setPlayerTabEntry(player, player2);
                        setPlayerTabEntry(player2, player);
                    }
                }).delay(1000L, TimeUnit.MILLISECONDS).schedule();
            }
        }
    }

    @Subscribe
    public void onPlayerColorUpdate(PlayerColorUpdateEvent event) {
        if (plugin.getConfiguration().getTabList().isEnableGlobalTabList()) {
            Player player = event.getPlayer();
            if (player.getCurrentServer().isPresent()) {
                for (Player player2 : plugin.getProxy().getAllPlayers()) {
                    setPlayerTabEntry(player, player2);
                    setPlayerTabEntry(player2, player);
                }
            }
        }
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        if (plugin.getConfiguration().getTabList().isEnableGlobalTabList()) {
            Player player = event.getPlayer();
            for (Player player2 : plugin.getProxy().getAllPlayers()) {
                player2.getTabList().removeEntry(player.getUniqueId());
            }
        }
    }

    private Component buildPlayerNameComponent(Player player) {
        return Component.text(player.getUsername()).color(plugin.getPlayerManager().getPlayerColor(player));
    }

    private Component buildPlayerEntryComponent(Player player) {
        if (player.getCurrentServer().isPresent() && player.getCurrentServer().isPresent()) {
            String serverName = player.getCurrentServer().get().getServerInfo().getName();

            return this.displayName
                .replaceText(builder -> builder.matchLiteral("%player%").replacement(
                    Component.text(player.getUsername())
                        .color(plugin.getPlayerManager().getPlayerColor(player))
                ))
                .replaceText(builder -> builder.matchLiteral("%server%").replacement(
                    Component.text(serverName)
                        .color(plugin.getConfiguration().getServerColor(serverName))
                ))
                .replaceText(builder -> builder.matchLiteral("%serverAlias%").replacement(
                    Component.text(plugin.getConfiguration().getServerAlias(serverName))
                        .color(plugin.getConfiguration().getServerColor(serverName))
                ));
        }
        return buildPlayerNameComponent(player);
    }

    private void setPlayerTabEntry(Player player, Player playerEntry) {
        if (playerEntry.getCurrentServer().isPresent() && player.getCurrentServer().isPresent()) {
            RegisteredServer server = playerEntry.getCurrentServer().get().getServer();
            Config.GlobalTabListMode mode = plugin.getConfiguration()
                .getServerTabListMode(player.getCurrentServer().get().getServerInfo().getName());

            if (mode == Config.GlobalTabListMode.DISABLED) {
                // 禁用 不做任何操作
                return;
            }

            Component displayName = buildPlayerEntryComponent(playerEntry);

            if (player.getTabList().getEntry(playerEntry.getUniqueId()).isPresent()) {

                if (mode == Config.GlobalTabListMode.GLOBAL
                    || player.getCurrentServer().get() != playerEntry.getCurrentServer().get()
                ) {
                    TabListEntry entry = player.getTabList().getEntry(playerEntry.getUniqueId()).get();

                    entry.setDisplayName(displayName);

                    if (player.getCurrentServer().get().getServer()
                        != playerEntry.getCurrentServer().get().getServer()) {
                        entry.setGameMode(3);
                    }
                }
            } else {
                // 不存在的条目
                if (player.getCurrentServer().get().getServer() == playerEntry.getCurrentServer().get().getServer()) {
                    player.getTabList().addEntry(
                        TabListEntry.builder().tabList(player.getTabList()).displayName(displayName)
                            .profile(playerEntry.getGameProfile()).build()
                    );
                } else {
                    player.getTabList().addEntry(
                        TabListEntry.builder().tabList(player.getTabList()).displayName(displayName)
                            .profile(playerEntry.getGameProfile()).gameMode(3).build()
                    );
                }
            }
        }
    }
}