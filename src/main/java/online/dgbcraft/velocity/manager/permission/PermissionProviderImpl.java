package online.dgbcraft.velocity.manager.permission;

import com.velocitypowered.api.permission.PermissionFunction;
import com.velocitypowered.api.permission.PermissionProvider;
import com.velocitypowered.api.permission.PermissionSubject;
import online.dgbcraft.velocity.manager.VelocityManager;

/**
 * @author Sanluli36li
 */
public class PermissionProviderImpl implements PermissionProvider {
    private final VelocityManager plugin;

    public PermissionProviderImpl(VelocityManager plugin) {
        this.plugin = plugin;
    }

    @Override
    public PermissionFunction createFunction(PermissionSubject subject) {
        return new PermissionFunctionImpl(plugin, subject);
    }
}