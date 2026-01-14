package online.dgbcraft.velocity.manager.command;

import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandSource;
import net.kyori.adventure.text.Component;
import online.dgbcraft.velocity.manager.VelocityManager;

import static online.dgbcraft.velocity.manager.constant.PermissionKeys.PERMISSION_PROXY_COMMAND_PLUGINS;

/**
 * @author Sanluli36li
 */
public class VManagerCommand {
    private static final String COMMAND_NAME = "vmanager";
    private static final String COMMAND_RELOAD_NODE = "reload";
    private final VelocityManager plugin;

    public VManagerCommand(VelocityManager plugin) {
        this.plugin = plugin;
    }

    public void register() {
        // 构建命令结构
        LiteralCommandNode<CommandSource> rootNode = BrigadierCommand.literalArgumentBuilder(COMMAND_NAME)
            .requires(source -> source.hasPermission(PERMISSION_PROXY_COMMAND_PLUGINS)).build();

        LiteralCommandNode<CommandSource> reloadNode = BrigadierCommand.literalArgumentBuilder(COMMAND_RELOAD_NODE)
            .executes(commandContext -> {
                plugin.reload();
                commandContext.getSource().sendMessage(Component.text("reloaded!"));
                return 1;
            }).build();
        rootNode.addChild(reloadNode);

        // 注册命令
        BrigadierCommand cmd = new BrigadierCommand(rootNode);
        CommandMeta meta = plugin.getProxy().getCommandManager().metaBuilder(cmd).build();
        plugin.getProxy().getCommandManager().register(meta, cmd);
    }
}