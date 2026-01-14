package online.dgbcraft.velocity.manager;

import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import online.dgbcraft.velocity.manager.util.TextColorUtil;

import java.util.*;

import static com.velocitypowered.api.network.ProtocolVersion.MAXIMUM_VERSION;
import static online.dgbcraft.velocity.manager.constant.PermissionKeys.*;

/**
 * @author Sanluli36li
 */
public class Config {
    private long openTimestamp = System.currentTimeMillis() / 1000;
    private LinkedHashMap<String, RoleConfig> roles = new LinkedHashMap<>();
    private AdvancedPingConfigAndForceHosts advancedPing = AdvancedPingConfigAndForceHosts.defaultConfig();
    private TabListConfig tabList = new TabListConfig();
    private Map<String, ServerConfig> serverConfig = new HashMap<>();
    private boolean saveLastServer = false;

    public static Config defaultConfig() {
        Config config = new Config();
        config.tabList.playerNameFormat = VelocityManager.GSON.toJsonTree("%player%");
        config.roles.put(ROLE_EVERYONE, new RoleConfig(Sets.newTreeSet(Arrays.asList(
            PERMISSION_PROXY_COMMAND_SERVER,
            PERMISSION_PROXY_COMMAND_GLIST,
            PERMISSION_PLUGIN_LOGIN
        ))));
        config.roles.put(ROLE_ADMIN, new RoleConfig(Sets.newTreeSet(Arrays.asList(
            PERMISSION_PROXY_COMMAND_PLUGINS,
            PERMISSION_PROXY_COMMAND_RELOAD,
            PERMISSION_PROXY_COMMAND_SEND,
            PERMISSION_PLUGIN_COMMAND_PLUGIN,
            PERMISSION_PLUGIN_COMMAND_BAN,
            PERMISSION_PLUGIN_COMMAND_KICK,
            PERMISSION_PLUGIN_COMMAND_ROLE
        ))));
        return config;
    }

    public long getOpenTimestamp() {
        return openTimestamp;
    }

    public int getDayCount() {
        return ((int) (((System.currentTimeMillis() / 1000) - openTimestamp) / 86400)) + 1;
    }

    public AdvancedPingConfigAndForceHosts getAdvancedPing() {
        return advancedPing;
    }

    public TabListConfig getTabList() {
        return tabList;
    }

    public Map<String, RoleConfig> getRoles() {
        return roles;
    }

    public boolean isSaveLastServer() {
        return saveLastServer;
    }

    public String getServerAlias(String serverName) {
        if (serverConfig.containsKey(serverName)) {
            String alias = serverConfig.get(serverName).getAlias();
            return alias == null ? serverName : alias;
        } else {
            return serverName;
        }
    }

    public TextColor getServerColor(String serverName) {
        if (serverConfig.containsKey(serverName)) {
            return serverConfig.get(serverName).getTextColor();
        } else {
            return null;
        }
    }

    public String getServerPermission(String serverName) {
        if (serverConfig.containsKey(serverName)) {
            return serverConfig.get(serverName).getPermission();
        } else {
            return null;
        }
    }

    public GlobalTabListMode getServerTabListMode(String serverName) {
        if (serverConfig.containsKey(serverName)) {
            GlobalTabListMode mode = serverConfig.get(serverName).getTabListMode();
            return mode == null ? tabList.getGlobalTabListMode() : mode;
        } else {
            return tabList.getGlobalTabListMode();
        }
    }

    public static class RoleConfig {
        private String color;
        private Set<String> permissions = new HashSet<>();

        public RoleConfig() {}
        public RoleConfig(Set<String> permissions) {
            this.permissions = permissions;
        }

        public String getColor() {
            return color;
        }

        public Set<String> getPermissions() {
            return permissions;
        }

        public TextColor getTextColor() {
            return TextColorUtil.textColorFromString(color);
        }
    }

    public static class TabListConfig {
        private boolean enableGlobalTabList = true;
        private GlobalTabListMode globalTabListMode = GlobalTabListMode.GLOBAL;
        private boolean clearHeaderAndFooterWhenSwitchServer = false;
        private JsonElement playerNameFormat;

        public boolean isEnableGlobalTabList() {
            return enableGlobalTabList;
        }

        public GlobalTabListMode getGlobalTabListMode() {
            return globalTabListMode;
        }

        public boolean isClearHeaderAndFooterWhenSwitchServer() {
            return clearHeaderAndFooterWhenSwitchServer;
        }

        public Component getPlayerNameFormat() {
            return GsonComponentSerializer.gson().deserializeFromTree(playerNameFormat);
        }
    }

    public static class AdvancedPingConfig {
        private boolean enable = true;
        private boolean playerList = true;
        private boolean playerListDisplayServer = true;
        private boolean allowAnonymous = true;
        private int currentPlayerCount = -1;
        private int maximumPlayerCount = 20;
        private int minimumProtocolVersion = 107;
        private int maximumProtocolVersion = MAXIMUM_VERSION.getProtocol();
        private String versionName = "Velocity";
        protected JsonElement description;

        public static AdvancedPingConfig defaultConfig() {
            AdvancedPingConfig config = new AdvancedPingConfig();
            config.description = VelocityManager.GSON.toJsonTree("A Minecraft Server");
            return config;
        }

        public boolean isEnable() {
            return enable;
        }

        public boolean isEnablePlayerList() {
            return playerList;
        }

        public boolean isPlayerListDisplayServer() {
            return playerListDisplayServer;
        }

        public boolean isAllowAnonymous() {
            return allowAnonymous;
        }

        public int getCurrentPlayerCount() {
            return currentPlayerCount;
        }

        public int getMaximumPlayerCount() {
            return maximumPlayerCount;
        }

        public int getMinimumProtocolVersion() {
            return minimumProtocolVersion;
        }

        public int getMaximumProtocolVersion() {
            return maximumProtocolVersion;
        }

        public String getVersionName() {
            return versionName;
        }

        public Component getDescription() {
            return GsonComponentSerializer.gson().deserializeFromTree(description);
        }
    }

    public enum GlobalTabListMode {
        // 将其他服务器的玩家添加到Tab列表, 并修改Tab列表中已有的玩家
        GLOBAL,
        // 将其他服务器的玩家添加到Tab列表, 不处理Tab列表中已有的玩家. 此模式下, 相同服务器的玩家在Tab列表中的颜色不生效
        ADD_OTHER,
        // 
        COLOR,
        // 禁用 TabList
        DISABLED
    }

    public static class AdvancedPingConfigAndForceHosts extends AdvancedPingConfig {
        private Map<String, AdvancedPingConfig> forceHosts = new HashMap<>();

        public static AdvancedPingConfigAndForceHosts defaultConfig() {
            AdvancedPingConfigAndForceHosts config = new AdvancedPingConfigAndForceHosts();
            config.description = VelocityManager.GSON.toJsonTree("A Minecraft Server");
            return config;
        }

        public Map<String, AdvancedPingConfig> getForceHosts() {
            return forceHosts;
        }
    }

    public static class ServerConfig {
        private String alias;
        private String permission;
        private String color;
        private GlobalTabListMode tabListMode = null;

        public String getAlias() {
            return alias;
        }

        public String getPermission() {
            return permission;
        }

        public String getColor() {
            return color;
        }

        public TextColor getTextColor() {
            return TextColorUtil.textColorFromString(color);
        }

        public GlobalTabListMode getTabListMode() {
            return tabListMode;
        }
    }
}