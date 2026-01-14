package online.dgbcraft.velocity.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.permission.PermissionsSetupEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.permission.Tristate;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import online.dgbcraft.velocity.manager.command.KickCommand;
import online.dgbcraft.velocity.manager.command.RoleCommand;
import online.dgbcraft.velocity.manager.command.VManagerCommand;
import online.dgbcraft.velocity.manager.information.AdvancedPing;
import online.dgbcraft.velocity.manager.information.GlobalTabList;
import online.dgbcraft.velocity.manager.permission.PermissionProviderImpl;
import online.dgbcraft.velocity.manager.playermanager.PlayerManager;
import online.dgbcraft.velocity.manager.util.L10nUtil;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Sanluli36li
 */
@Plugin(
    id = "velocitymanager",
    name = "VelocityManager",
    version = BuildConstants.VERSION,
    description = "Velocity Proxy Player Manager and Player Information Enhanced",
    url = "https://github.com/DGBCraft/VelocityManager",
    authors = {"Sanluli36li"}
)
public class VelocityManager {
    public static VelocityManager instance;
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final String PLUGIN_NAMESPACE = "vmanager";
    private final ProxyServer proxy;
    private final Logger logger;
    private final Path dataPath;
    private final Path playerDataPath;
    private final Path bannedDataPath;
    private final Path configPath;
    private final PermissionProviderImpl permissionProvider = new PermissionProviderImpl(this);
    private Config configuration;
    private PlayerManager playerManager;
    private AdvancedPing advancedPing;
    private GlobalTabList globalTab;

    @Inject
    public VelocityManager(ProxyServer proxy, Logger logger, @DataDirectory Path dataDir) {
        instance = this;
        this.proxy = proxy;
        this.logger = logger;
        dataPath = dataDir;
        playerDataPath = dataPath.resolve("player");
        bannedDataPath = dataPath.resolve("banned");
        configPath = dataPath.resolve("config.json");
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        L10nUtil.registerTranslations();
        advancedPing = new AdvancedPing(this);
        playerManager = new PlayerManager(this);
        globalTab = new GlobalTabList(this);
        new RoleCommand(this).register();
        new KickCommand(this).register();
        new VManagerCommand(this).register();
        reload();
    }

    public void reload() {
        try {
            if (!Files.exists(dataPath)) {
                Files.createDirectories(dataPath);
            }
            if (!Files.exists(playerDataPath)) {
                Files.createDirectories(playerDataPath);
            }
            if (!Files.exists(bannedDataPath)) {
                Files.createDirectories(bannedDataPath);
            }
            if (Files.exists(configPath)) {
                try (BufferedReader bufferedReader = Files.newBufferedReader(configPath, StandardCharsets.UTF_8)) {
                    configuration = GSON.fromJson(bufferedReader, Config.class);
                } catch (Throwable e) {
                    logger.warn("Read config FAIL! Use default config", e);
                    configuration = Config.defaultConfig();
                }
            } else {
                configuration = saveDefaultConfig();
            }

        } catch (IOException e2) {
            logger.warn("Unable create data directory!", e2);
            logger.warn("Velocity Manager Initialize fail!");
        }

        advancedPing.reload();
        playerManager.reload();
        globalTab.reload();
    }

    private Config saveDefaultConfig() {
        Config config = Config.defaultConfig();
        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(configPath, StandardCharsets.UTF_8)) {
            GSON.toJson(config, bufferedWriter);
        } catch (Throwable e) {
            logger.warn("Can't create default config.json!", e);
        }
        return config;
    }

    @Subscribe
    public void onPermissionsSetup(PermissionsSetupEvent event) {
        event.setProvider(permissionProvider);
    }

    public ProxyServer getProxy() {
        return proxy;
    }

    public Logger getLogger() {
        return logger;
    }

    public Path getDataPath() {
        return dataPath;
    }

    public Path getPlayerDataPath() {
        return playerDataPath;
    }

    public Config getConfiguration() {
        return configuration;
    }

    public AdvancedPing getAdvancedPing() {
        return advancedPing;
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public Tristate playerHasPermission(Player player, String permission) {
        return Tristate.fromBoolean(playerManager.hasPermission(player, permission));
    }

    public Path getBannedDataPath() {
        return bannedDataPath;
    }
}
