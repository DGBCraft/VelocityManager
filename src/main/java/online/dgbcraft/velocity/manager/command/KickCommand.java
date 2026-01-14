package online.dgbcraft.velocity.manager.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.VelocityBrigadierMessage;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.ComponentSerializer;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import online.dgbcraft.velocity.manager.VelocityManager;

import java.util.Optional;

import static online.dgbcraft.velocity.manager.constant.LocaleKeys.*;
import static online.dgbcraft.velocity.manager.constant.PermissionKeys.PERMISSION_PLUGIN_COMMAND_KICK;

/**
 * @author Sanluli36li
 */
public class KickCommand {
    private static final String COMMAND_NAME = "gkick";
    private static final String COMMAND_PLAYER_ARGUMENT_NODE = "target";
    private static final String COMMAND_REASON_ARGUMENT_NODE = "reason";
    private static final SimpleCommandExceptionType PLAYER_NOT_FOUNT_EXCEPTION = new SimpleCommandExceptionType(
        VelocityBrigadierMessage.tooltip(Component.translatable(PLUGIN_CMD_EXCEPTION_PLAYER_NOT_FOUND)));
    private final VelocityManager plugin;

    public KickCommand(VelocityManager plugin) {
        this.plugin = plugin;
    }

    public void register() {
        // 构建命令结构
        LiteralCommandNode<CommandSource> rootNode = BrigadierCommand.literalArgumentBuilder(COMMAND_NAME)
            .requires(source -> source.hasPermission(PERMISSION_PLUGIN_COMMAND_KICK)).build();

        ArgumentCommandNode<CommandSource, String> playerArgumentNode = BrigadierCommand
            .requiredArgumentBuilder(COMMAND_PLAYER_ARGUMENT_NODE, StringArgumentType.word())
            .suggests((commandContext, suggestionsBuilder) -> {
                plugin.getProxy().getAllPlayers().forEach(player ->
                    suggestionsBuilder.suggest(player.getUsername())
                );
                return suggestionsBuilder.buildFuture();
            })
            .executes(context -> {
                String playerName = StringArgumentType.getString(context, COMMAND_PLAYER_ARGUMENT_NODE);
                return executeKick(context.getSource(), playerName, null);
            })
            .build();

        ArgumentCommandNode<CommandSource, String> reasonArgumentNode = BrigadierCommand
            .requiredArgumentBuilder(COMMAND_REASON_ARGUMENT_NODE, StringArgumentType.greedyString())
            .executes(context -> {
                String playerName = StringArgumentType.getString(context, COMMAND_PLAYER_ARGUMENT_NODE);
                String reason = StringArgumentType.getString(context, COMMAND_REASON_ARGUMENT_NODE);
                return executeKick(context.getSource(), playerName, reason);
            })
            .build();

        playerArgumentNode.addChild(reasonArgumentNode);
        rootNode.addChild(playerArgumentNode);

        // 注册命令
        BrigadierCommand cmd = new BrigadierCommand(rootNode);
        CommandMeta meta = plugin.getProxy().getCommandManager().metaBuilder(cmd).build();
        plugin.getProxy().getCommandManager().register(meta, cmd);
    }

    private int executeKick(CommandSource source, String playerName, String reason) throws CommandSyntaxException {
        Component text;
        Optional<Player> player = plugin.getProxy().getPlayer(playerName);

        if (player.isEmpty()) {
            throw PLAYER_NOT_FOUNT_EXCEPTION.create();
        }

        if (reason == null) {
            text = Component.translatable(DISCONNECT_KECKED);
        } else {
            try {
                text = GsonComponentSerializer.gson().deserialize(reason);
            } catch (Exception e) {
                text = Component.text(reason);
            }
        }

        player.get().disconnect(text);

        source.sendMessage(Component.translatable(
            COMMAND_KICK_SUCCESS,
            Component.text(player.get().getUsername()),
            text
        ));
        return 1;
    }
}