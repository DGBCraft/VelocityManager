package online.dgbcraft.velocity.manager.permission;

import com.velocitypowered.api.permission.PermissionFunction;
import com.velocitypowered.api.permission.PermissionSubject;
import com.velocitypowered.api.permission.Tristate;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.Player;
import online.dgbcraft.velocity.manager.VelocityManager;

/**
 * @author Sanluli36li
 */
public class PermissionFunctionImpl implements PermissionFunction {
    private final VelocityManager plugin;
    private final PermissionSubject subject;

    public PermissionFunctionImpl(VelocityManager plugin, PermissionSubject subject) {
        this.plugin = plugin;
        this.subject = subject;
    }

    @Override
    public Tristate getPermissionValue(String permission) {
        if (subject instanceof Player) {
            return plugin.playerHasPermission((Player) subject, permission);
        }
        if (subject instanceof ConsoleCommandSource) {
            return Tristate.TRUE;
        }
        return Tristate.UNDEFINED;
    }
}