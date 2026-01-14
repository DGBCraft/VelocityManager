package online.dgbcraft.velocity.manager.playermanager.data;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.util.GameProfile;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * @author Sanluli36li
 */
public class PlayerData {
    private String name;
    private UUID uuid;
    private Set<String> roles;
    private String lastServer;

    public PlayerData(String name, Set<String> roles) {
        this.roles = new HashSet<>();
        this.name = name;
        this.roles = roles;
    }

    public PlayerData(Player player) {
        this.roles = new HashSet<>();
        this.name = player.getUsername();
        this.uuid = player.getUniqueId();
    }

    public PlayerData(GameProfile profile) {
        this.roles = new HashSet<>();
        this.name = profile.getName();
        this.uuid = profile.getId();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public String getLastServer() {
        return lastServer;
    }

    public void setLastServer(String lastServer) {
        this.lastServer = lastServer;
    }
}