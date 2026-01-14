package online.dgbcraft.velocity.manager.playermanager.event;

import com.velocitypowered.api.proxy.Player;
import online.dgbcraft.velocity.manager.playermanager.PlayerManager;

import java.util.Set;

/**
 * @author Sanluli36li
 */
public class PlayerRoleUpdateEvent {
    private Player player;
    private Set<String> roles;

    public PlayerRoleUpdateEvent(Player player, Set<String> roles) {
        this.player = player;
        this.roles = roles;
    }

    public Player getPlayer() {
        return player;
    }

    public Set<String> getRoles() {
        return roles;
    }
}
