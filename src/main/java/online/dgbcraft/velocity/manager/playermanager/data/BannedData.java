package online.dgbcraft.velocity.manager.playermanager.data;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;

import java.util.Date;
import java.util.UUID;

/**
 * @author Sanluli36li
 */
public class BannedData {
    private String name;
    private UUID uuid;
    private String ip;
    private Date created;
    private Date expires;
    private String source;
    private String reason;

    public enum BannedType {
        IP,
        PLAYER
    }

    public BannedData(Player player) {
        this.name = player.getUsername();
        this.uuid = player.getUniqueId();
        this.ip = null;



        this.created = new Date();
    }

    public BannedType getBannedType() {
        if (uuid != null) {
            return BannedType.PLAYER;
        } else if (ip != null) {
            return BannedType.IP;
        } else {
            return null;
        }
    }

    public String getName() {
        return name;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getIp() {
        return ip;
    }

    public Date getCreated() {
        return created;
    }

    public Date getExpires() {
        return expires;
    }

    public boolean isExpired() {
        if (expires == null) {
            return false;
        } else {
            return expires.before(new Date());
        }
    }

    public String getSource() {
        return source;
    }

    public String getReason() {
        return reason;
    }
}
