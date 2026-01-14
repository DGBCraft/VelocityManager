package online.dgbcraft.velocity.manager.command;

import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;

import static online.dgbcraft.velocity.manager.constant.PermissionKeys.PERMISSION_PLUGIN_COMMAND_KICK;

/**
 * @author Sanluli36li
 */
public class BanCommand {
    private static final String COMMAND_NAME = "ban";
    private static final String COMMAND_PLAYER_NODE = "player";
    private static final String COMMAND_IP_NODE = "ip";
    private static final String COMMAND_PLAYER_ARGUMENT_NODE = "target";
    private static final String COMMAND_REASON_ARGUMENT_NODE = "reason";

    public void register() {
        // 构建命令结构
        LiteralCommandNode<CommandSource> rootNode = BrigadierCommand.literalArgumentBuilder(COMMAND_NAME)
            .requires(source -> source.hasPermission(PERMISSION_PLUGIN_COMMAND_KICK)).build();

        LiteralCommandNode<CommandSource> playerNode = BrigadierCommand.literalArgumentBuilder(COMMAND_PLAYER_NODE)
            .build();
        rootNode.addChild(playerNode);

        LiteralCommandNode<CommandSource> ipNode = BrigadierCommand.literalArgumentBuilder(COMMAND_IP_NODE)
            .build();
        rootNode.addChild(ipNode);
    }
}
