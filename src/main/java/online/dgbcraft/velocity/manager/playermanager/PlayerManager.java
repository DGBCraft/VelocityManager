package online.dgbcraft.velocity.manager.playermanager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.player.*;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.util.GameProfile;
import net.kyori.adventure.text.format.TextColor;
import online.dgbcraft.velocity.manager.VelocityManager;
import online.dgbcraft.velocity.manager.playermanager.data.BannedData;
import online.dgbcraft.velocity.manager.playermanager.data.PlayerData;
import online.dgbcraft.velocity.manager.playermanager.event.PlayerRoleUpdateEvent;
import online.dgbcraft.velocity.manager.playermanager.exception.PlayerDataSaveException;
import online.dgbcraft.velocity.manager.playermanager.handler.*;
import online.dgbcraft.velocity.manager.util.gson.typeadapter.DateTypeAdapter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;

import static online.dgbcraft.velocity.manager.constant.PermissionKeys.ROLE_EVERYONE;

/**
 * @author Sanluli36li
 */
public class PlayerManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting()
        .registerTypeAdapter(Date.class, new DateTypeAdapter()).create();
    private static final SimpleDateFormat ARCHIVE_DATA_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");
    private final Path archiveBannedPath;

    private final VelocityManager plugin;
    private final Map<UUID, PlayerData> playerData = new HashMap<>();
    private final Map<UUID, Set<String>> playerPermissions = new HashMap<>();
    private final Map<UUID, LinkedHashSet<RegisteredServer>> playerTryServer = new HashMap<>();

    private final Map<UUID, TextColor> playerColor = new HashMap<>();

    public PlayerManager(VelocityManager plugin) {
        this.plugin = plugin;
        archiveBannedPath = plugin.getBannedDataPath().resolve("archived");
        plugin.getProxy().getEventManager().register(plugin, LoginEvent.class, new LoginEventHandler(this));
        plugin.getProxy().getEventManager().register(plugin, PlayerChooseInitialServerEvent.class,
            new PlayerChooseInitialServerEventHandler(this));
        plugin.getProxy().getEventManager().register(plugin, ServerPreConnectEvent.class,
            new ServerPreConnectEventHandler(this));
        plugin.getProxy().getEventManager().register(plugin, ServerPostConnectEvent.class,
            new ServerPostConnectEventHandler(this));
        plugin.getProxy().getEventManager().register(plugin, KickedFromServerEvent.class,
            new KickedFromServerEventHandler(this));
        plugin.getProxy().getEventManager().register(plugin, DisconnectEvent.class, (short)-30000,
            new DisconnectEventHandler(this));

        plugin.getProxy().getEventManager().register(plugin, PlayerRoleUpdateEvent.class,
            new PlayerRoleUpdateEventHandler(this));
    }

    public VelocityManager getPlugin() {
        return plugin;
    }

    public void reload() {
        for (Player player : plugin.getProxy().getAllPlayers()) {
            loadPlayer(player);
            loadRoles(player);
        }
    }

    public void loadPlayer(Player player) {
        PlayerData data = getSavedPlayerData(player.getUniqueId());
        boolean modified = false;
        if (data != null) {
            if (!data.getName().equals(player.getUsername())) {
                // 可能是玩家改名了
                data.setName(player.getUsername());
                modified = true;
            }
            if (data.getUuid() != player.getUniqueId()) {
                // UUID 都错了肯定是记错了
                data.setUuid(player.getUniqueId());
                modified = true;
            }
            if (data.getRoles().removeIf(role ->
                // 移除无效的身份
                !plugin.getConfiguration().getRoles().containsKey(role) || ROLE_EVERYONE.equals(role)
            )) {
                modified = true;
            }

        } else {
            data = new PlayerData(player.getGameProfile());
        }

        playerData.put(player.getUniqueId(), data);

        if (modified) {
            try {
                savePlayerData(player.getUniqueId(), data);
            } catch (PlayerDataSaveException ignored) {
            }
        }
    }

    public PlayerData getPlayerData(Player player) {
        return getPlayerData(player.getUniqueId());
    }

    public PlayerData getPlayerData(UUID id) {
        if (playerData.containsKey(id)) {
            return playerData.get(id);
        } else {
            return getSavedPlayerData(id);
        }
    }

    public PlayerData getSavedPlayerData(UUID id) {
        Path path = plugin.getPlayerDataPath().resolve(id.toString() + ".json");
        if (Files.exists(path)) {
            try (BufferedReader bufferedReader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                // 读取 Json
                return GSON.fromJson(bufferedReader, PlayerData.class);
            } catch (Throwable e) {
                plugin.getLogger().warn("Can't load player data!", e);
                return null;
            }
        } else {
            return null;
        }
    }

    public void savePlayerData(Player player) throws PlayerDataSaveException {
        if (playerData.containsKey(player.getUniqueId())) {
            savePlayerData(player.getUniqueId(), playerData.get(player.getUniqueId()));
        }
    }

    public void savePlayerData(UUID id, PlayerData data) throws PlayerDataSaveException {
        if (id == null || data == null) {
            return;
        }

        Path path = plugin.getPlayerDataPath().resolve(id + ".json");
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            GSON.toJson(data, writer);
        } catch (IOException e) {
            plugin.getLogger().warn("Can't save player data!", e);
            throw new PlayerDataSaveException(e);
        }
    }

    public void unloadPlayerData(Player player) {
        unloadPlayerData(player.getUniqueId());
    }

    private void unloadPlayerData(UUID id) {
        playerData.remove(id);
    }

    private static String stringifyAddress (InetSocketAddress address) {
        String string = address.toString();
        if (string.contains("/")) {
            string = string.substring(string.indexOf('/') + 1);
        }

        if (string.contains(":")) {
            string = string.substring(0, string.indexOf(':'));
        }

        return string;
    }

    public BannedData getBannedData(UUID id) {
        return getBannedData(id.toString());
    }

    public BannedData getBannedData(InetSocketAddress address) {
        return getBannedData(stringifyAddress(address));
    }

    public BannedData getBannedData(String fileName) {
        Path path = plugin.getBannedDataPath().resolve(fileName + ".json");
        if (Files.exists(path)) {
            try (BufferedReader bufferedReader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                // 读取 Json
                return GSON.fromJson(bufferedReader, BannedData.class);
            } catch (Throwable e) {
                plugin.getLogger().warn("Can't load banned data!", e);
                return null;
            }
        } else {
            return null;
        }
    }

    public void saveBannedData(String fileName, BannedData data) throws PlayerDataSaveException {
        if (fileName == null || data == null) {
            return;
        }

        Path path = plugin.getBannedDataPath().resolve(fileName + ".json");
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            GSON.toJson(data, writer);
        } catch (IOException e) {
            plugin.getLogger().warn("Can't save banned data!", e);
            throw new PlayerDataSaveException(e);
        }
    }

    public void archiveBannedData(UUID id) throws PlayerDataSaveException {
        archiveBannedData(id.toString());
    }

    public void archiveBannedData(InetSocketAddress address) throws PlayerDataSaveException {
        archiveBannedData(stringifyAddress(address));
    }

    public void archiveBannedData(String fileName) throws PlayerDataSaveException {
        if (fileName == null) {
            return;
        }
        Path path = plugin.getBannedDataPath().resolve(fileName + ".json");
        if (!Files.exists(path)) {
            return;
        }

        Date date = new Date();
        Path archivedPath = archiveBannedPath.resolve("archived_" + ARCHIVE_DATA_FORMAT.format(date) + "_" + fileName + ".json");
        try  {
            // 将封禁信息归档
            if (!Files.exists(archiveBannedPath)) {
                Files.createDirectories(archiveBannedPath);
            }
            Files.move(path, archivedPath);
            // Files.delete(path);
        } catch (IOException e) {
            plugin.getLogger().warn("Can't save banned data!", e);
            throw new PlayerDataSaveException(e);
        }
    }

    public boolean addRole(GameProfile profile, String role) throws PlayerDataSaveException {
        UUID id = profile.getId();
        PlayerData data;
        if (playerData.containsKey(id)) {
            data = playerData.get(id);
        } else {
            data = getSavedPlayerData(id);
            if (data == null) {
                data = new PlayerData(profile);
            }
        }

        if (data.getRoles().contains(role)) {
            // 已有身份, 返回失败
            return false;
        } else {
            // 添加身份, 并保存数据 / 并重载玩家身份组信息
            data.getRoles().add(role);
            savePlayerData(id, data);
            if (plugin.getProxy().getPlayer(id).isPresent()) {
                loadRoles(plugin.getProxy().getPlayer(id).get());
            }

            return true;
        }
    }

    public boolean removeRole(GameProfile profile, String role) throws PlayerDataSaveException {
        UUID id = profile.getId();
        PlayerData data;
        if (playerData.containsKey(id)) {
            data = playerData.get(id);
        } else {
            data = getSavedPlayerData(id);
            if (data == null) {
                // 没有记录的玩家, 想必也没有身份, 无视即可
                return false;
            }
        }

        if (data.getRoles().contains(role)) {
            // 移除身份, 并保存数据 / 并重载玩家身份组信息
            data.getRoles().remove(role);
            savePlayerData(id, data);
            if (plugin.getProxy().getPlayer(id).isPresent()) {
                loadRoles(plugin.getProxy().getPlayer(id).get());
            }

            return true;
        } else {
            // 没有身份, 返回失败
            return false;
        }
    }

    public void loadRoles(Player player) {
        loadRoles(player.getUniqueId());

        if (playerData.containsKey(player.getUniqueId())) {
            plugin.getProxy().getEventManager().fire(
                new PlayerRoleUpdateEvent(player, playerData.get(player.getUniqueId()).getRoles())
            );
        }
    }

    public void loadRoles(UUID id) {
        PlayerData data;
        Set<String> permissions = new HashSet<>();
        if (playerData.containsKey(id)
            && (data = playerData.get(id)) != null && data.getRoles() != null) {
            Set<String> roles = data.getRoles();
            for (String role : roles) {
                if (plugin.getConfiguration().getRoles().containsKey(role) &&
                    plugin.getConfiguration().getRoles().get(role).getPermissions() != null
                ) {
                    permissions.addAll(plugin.getConfiguration().getRoles().get(role).getPermissions());
                }
            }
        }
        if (plugin.getConfiguration().getRoles().containsKey(ROLE_EVERYONE)
            && plugin.getConfiguration().getRoles().get(ROLE_EVERYONE).getPermissions() != null
        ) {
            permissions.addAll(plugin.getConfiguration().getRoles().get(ROLE_EVERYONE).getPermissions());
        }
        playerPermissions.put(id, permissions);
    }

    public void unloadRoles(Player player) {
        unloadRoles(player.getUniqueId());
    }

    public void unloadRoles(UUID id) {
        playerPermissions.remove(id);
        playerColor.remove(id);
    }

    public boolean hasPermission(Player player, String permission) {
        return hasPermission(player.getUniqueId(), permission);
    }

    public boolean hasPermission(UUID id, String permission) {
        return playerPermissions.containsKey(id) && playerPermissions.get(id).contains(permission);
    }

    public void setPlayerColor(Player player, TextColor color) {
        if (color == null) {
            playerColor.remove(player.getUniqueId());
        } else {
            playerColor.put(player.getUniqueId(), color);
        }


    }

    public TextColor getPlayerColor(Player player) {
        return playerColor.getOrDefault(player.getUniqueId(), null);
    }

    public Map<UUID, LinkedHashSet<RegisteredServer>> getPlayerTryServer() {
        return playerTryServer;
    }

    public RegisteredServer getTryServer(Player player) {
        return getTryServer(player, null);
    }

    public RegisteredServer getTryServer(Player player, RegisteredServer kickedServer) {
        LinkedHashSet<RegisteredServer> tryList;

        if (playerTryServer.containsKey(player.getUniqueId())) {
            tryList = playerTryServer.get(player.getUniqueId());
        } else {
            tryList = new LinkedHashSet<>();
            List<String> tryServers;

            if (player.getVirtualHost().isPresent() && plugin.getProxy().getConfiguration().getForcedHosts()
                .containsKey(player.getVirtualHost().get().getHostName())
            ) {
                // ForceHosts: 使用当前host的服务器列表
                tryServers = plugin.getProxy().getConfiguration().getForcedHosts()
                    .get(player.getVirtualHost().get().getHostName());
            } else {
                // 使用默认的服务器列表
                tryServers = plugin.getProxy().getConfiguration().getAttemptConnectionOrder();
            }

            // 如果玩家上次加入的服务器在try列表中, 首先尝试加入
            if (plugin.getConfiguration().isSaveLastServer()
                && kickedServer == null
                && getPlayerData(player).getLastServer() != null
                && tryServers.contains(getPlayerData(player).getLastServer())
                && plugin.getProxy().getServer(getPlayerData(player).getLastServer()).isPresent()
            ) {
                tryList.add(plugin.getProxy().getServer(getPlayerData(player).getLastServer()).get());
            }

            // try 列表
            for (String serverName : tryServers) {
                if (plugin.getProxy().getServer(serverName).isPresent()) {
                    tryList.add(plugin.getProxy().getServer(serverName).get());
                }
            }

            // 我都已经被踢了, 就不要试着重新进去了
            // 如果不这么做, 当玩家在try列表里的第一个服务器被踢出, 随后就会马上尝试重新连接至该服务器
            if (kickedServer != null) {
                tryList.remove(kickedServer);
            }

            playerTryServer.put(player.getUniqueId(), tryList);
        }

        while (!tryList.isEmpty()) {
            RegisteredServer server = tryList.getFirst();

            String permission = plugin.getConfiguration().getServerPermission(server.getServerInfo().getName());

            if (permission == null || player.hasPermission(permission)) {
                // 服务器不需要权限或玩家有加入权限: 尝试加入
                tryList.removeFirst();

                return server;
            } else {
                // 没有权限就直接删掉试下一个
                tryList.removeFirst();
            }
        }
        // 所有备选都用完了, 返回 null 断开玩家, 并移除尝试列表
        // 注: 如果不把尝试列表删掉, 当玩家在try列表中唯一的服务器中被踢出后, 将永远无法重连直到代理重启
        playerTryServer.remove(player.getUniqueId());
        return null;
    }
}