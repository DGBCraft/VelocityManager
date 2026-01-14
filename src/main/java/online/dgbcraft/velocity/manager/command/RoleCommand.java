package online.dgbcraft.velocity.manager.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.VelocityBrigadierMessage;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.util.GameProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import online.dgbcraft.velocity.manager.VelocityManager;
import online.dgbcraft.velocity.manager.playermanager.data.PlayerData;
import online.dgbcraft.velocity.manager.playermanager.exception.PlayerDataSaveException;
import online.dgbcraft.velocity.manager.util.mojangapi.MojangApiUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static online.dgbcraft.velocity.manager.constant.LocaleKeys.*;
import static online.dgbcraft.velocity.manager.constant.PermissionKeys.PERMISSION_PLUGIN_COMMAND_ROLE;
import static online.dgbcraft.velocity.manager.constant.PermissionKeys.ROLE_EVERYONE;

/**
 * @author Sanluli36li
 */
public class RoleCommand {
    private static final String COMMAND_NAME = "role";
    private static final String COMMAND_PLAYER_NODE = "player";
    private static final String COMMAND_PLAYER_ADD_NODE = "add";
    private static final String COMMAND_PLAYER_REMOVE_NODE = "remove";
    private static final String COMMAND_PLAYER_RELOAD_NODE = "reload";
    private static final String COMMAND_PLAYER_ARGUMENT_NODE = "target";
    private static final String COMMAND_ROLE_ARGUMENT_NODE = "role";
    private static final SimpleCommandExceptionType PLAYER_NOT_FOUNT_EXCEPTION = new SimpleCommandExceptionType(
        VelocityBrigadierMessage.tooltip(Component.translatable(PLUGIN_CMD_EXCEPTION_PLAYER_NOT_FOUND)));
    private static final SimpleCommandExceptionType ROLE_NOT_FOUND_EXCEPTION = new SimpleCommandExceptionType(
        VelocityBrigadierMessage.tooltip(Component.translatable(PLUGIN_CMD_ROLE_EXCEPTION_ROLE_NOT_FOUND)));
    private static final SimpleCommandExceptionType ADD_EXIST_EXCEPTION = new SimpleCommandExceptionType(
        VelocityBrigadierMessage.tooltip(Component.translatable(PLUGIN_CMD_ROLE_EXCEPTION_PLAYER_ADD_EXIST)));
    private static final SimpleCommandExceptionType REMOVE_NOT_EXIST_EXCEPTION = new SimpleCommandExceptionType(
        VelocityBrigadierMessage.tooltip(Component.translatable(PLUGIN_CMD_ROLE_EXCEPTION_PLAYER_REMOVE_NOT_EXIST)));
    private static final SimpleCommandExceptionType SAVE_FAIL_EXCEPTION = new SimpleCommandExceptionType(
        VelocityBrigadierMessage.tooltip(Component.translatable(PLUGIN_CMD_ROLE_EXCEPTION_SAVE_FAIL)));
    private final VelocityManager plugin;

    public RoleCommand(VelocityManager plugin) {
        this.plugin = plugin;
    }

    public void register() {
        // 构建命令结构
        LiteralCommandNode<CommandSource> rootNode = BrigadierCommand.literalArgumentBuilder(COMMAND_NAME)
            .requires(source -> source.hasPermission(PERMISSION_PLUGIN_COMMAND_ROLE)).build();

        LiteralCommandNode<CommandSource> playerNode = BrigadierCommand.literalArgumentBuilder(COMMAND_PLAYER_NODE)
            .build();
        rootNode.addChild(playerNode);

        ArgumentCommandNode<CommandSource, String> playerArgumentNode = BrigadierCommand
            .requiredArgumentBuilder(COMMAND_PLAYER_ARGUMENT_NODE, StringArgumentType.word())
            .suggests((context, suggestionsBuilder) -> {
                plugin.getProxy().getAllPlayers().forEach(player ->
                    suggestionsBuilder.suggest(player.getUsername())
                );
                return suggestionsBuilder.buildFuture();
            })
            .executes(context -> playerRoleList(
                context.getSource(), StringArgumentType.getString(context, COMMAND_PLAYER_ARGUMENT_NODE)
            ))
            .build();
        playerNode.addChild(playerArgumentNode);

        LiteralCommandNode<CommandSource> playerAddNode = BrigadierCommand
            .literalArgumentBuilder(COMMAND_PLAYER_ADD_NODE).build();
        playerArgumentNode.addChild(playerAddNode);

        LiteralCommandNode<CommandSource> playerRemoveNode = BrigadierCommand
            .literalArgumentBuilder(COMMAND_PLAYER_REMOVE_NODE).build();
        playerArgumentNode.addChild(playerRemoveNode);

        LiteralCommandNode<CommandSource> playerReloadNode = BrigadierCommand
            .literalArgumentBuilder(COMMAND_PLAYER_RELOAD_NODE)
            .executes(context -> playerReload(
                context.getSource(),
                StringArgumentType.getString(context, COMMAND_PLAYER_ARGUMENT_NODE)
            ))
            .build();
        playerArgumentNode.addChild(playerReloadNode);

        ArgumentCommandNode<CommandSource, String> roleArgumentNode = BrigadierCommand
            .requiredArgumentBuilder(COMMAND_ROLE_ARGUMENT_NODE, StringArgumentType.word())
            .suggests((context, suggestionsBuilder2) -> {
                plugin.getConfiguration().getRoles().keySet().forEach(role -> {
                    if (!ROLE_EVERYONE.equals(role)) {
                        suggestionsBuilder2.suggest(role);
                    }
                });
                return suggestionsBuilder2.buildFuture();
            })
            .executes(this::executePlayerRole)
            .build();
        playerAddNode.addChild(roleArgumentNode);
        playerRemoveNode.addChild(roleArgumentNode);

        // 注册命令
        BrigadierCommand cmd = new BrigadierCommand(rootNode);
        CommandMeta meta = plugin.getProxy().getCommandManager().metaBuilder(cmd).build();
        plugin.getProxy().getCommandManager().register(meta, cmd);
    }

    private int executePlayerRole(CommandContext<CommandSource> context) throws CommandSyntaxException {
        String playerName = StringArgumentType.getString(context, COMMAND_PLAYER_ARGUMENT_NODE);
        String role = StringArgumentType.getString(context, "role");
        if (!plugin.getConfiguration().getRoles().containsKey(role)) {
            throw ROLE_NOT_FOUND_EXCEPTION.create();
        }
        boolean executeAdd = false;
        boolean executeRemove = false;
        for (ParsedCommandNode<CommandSource> node : context.getNodes()) {
            String name = node.getNode().getName();
            if (COMMAND_PLAYER_ADD_NODE.equals(name)) {
                executeAdd = true;
            } else if (COMMAND_PLAYER_REMOVE_NODE.equals(name)) {
                executeRemove = true;
            }
        }
        if (executeAdd) {
            return playerAddRole(context.getSource(), playerName, role);
        }
        if (executeRemove) {
            return playerRemoveRole(context.getSource(), playerName, role);
        }
        return 0;
    }

    private int playerAddRole(CommandSource source, String playerName, String role) throws CommandSyntaxException {
        GameProfile profile = getGameProfileByName(playerName);
        if (profile == null) {
            throw PLAYER_NOT_FOUNT_EXCEPTION.create();
        }
        try {
            if (plugin.getPlayerManager().addRole(profile, role)) {
                source.sendMessage(Component.translatable(
                    PLUGIN_CMD_ROLE_PLAYER_ADD,
                    Component.text(role),
                    Component.text(profile.getName())
                ));
                return 1;
            }
            throw ADD_EXIST_EXCEPTION.create();
        } catch (PlayerDataSaveException e) {
            throw SAVE_FAIL_EXCEPTION.create();
        }
    }

    private int playerRemoveRole(CommandSource source, String playerName, String role) throws CommandSyntaxException {
        GameProfile profile = getGameProfileByName(playerName);
        if (profile == null) {
            throw PLAYER_NOT_FOUNT_EXCEPTION.create();
        }
        try {
            if (plugin.getPlayerManager().removeRole(profile, role)) {
                source.sendMessage(Component.translatable(
                    PLUGIN_CMD_ROLE_PLAYER_REMOVE,
                    Component.text(role),
                    Component.text(profile.getName()))
                );
                return 1;
            }
            throw REMOVE_NOT_EXIST_EXCEPTION.create();
        } catch (PlayerDataSaveException e) {
            throw SAVE_FAIL_EXCEPTION.create();
        }
    }

    private int playerReload(CommandSource source, String playerName) throws CommandSyntaxException {
        Optional<Player> player = plugin.getProxy().getPlayer(playerName);
        if (player.isPresent()) {
            plugin.getPlayerManager().loadPlayer(player.get());
            plugin.getPlayerManager().loadRoles(player.get());
            source.sendMessage(Component.translatable(PLUGIN_CMD_ROLE_PLAYER_RELOAD, Component.text(playerName)));
            return 1;
        }
        throw PLAYER_NOT_FOUNT_EXCEPTION.create();
    }

    private int playerRoleList(CommandSource source, String playerName) throws CommandSyntaxException {
        GameProfile profile = getGameProfileByName(playerName);
        if (profile == null) {
            throw PLAYER_NOT_FOUNT_EXCEPTION.create();
        }
        PlayerData data = plugin.getPlayerManager().getPlayerData(profile.getId());
        if (data == null || data.getRoles() == null || data.getRoles().isEmpty()) {
            source.sendMessage(Component.translatable(
                PLUGIN_CMD_ROLE_PLAYER_LIST_NO_ROLE,
                Component.text(profile.getName())
            ));
            return 0;
        }

        List<Component> roleComponents = new ArrayList<>();
        for (String role : data.getRoles()) {
            if (plugin.getConfiguration().getRoles().containsKey(role)) {
                roleComponents.add(
                    Component.text(role).color(plugin.getConfiguration().getRoles().get(role).getTextColor())
                );
            }
        }



        source.sendMessage(Component.translatable(
            PLUGIN_CMD_ROLE_PLAYER_LIST,
            Component.text(profile.getName()),
            Component.text(data.getRoles().size()),
            Component.join(
                JoinConfiguration.builder().separator(Component.text(", ")),
                roleComponents
            )
        ));
        return data.getRoles().size();
    }

    private GameProfile getGameProfileByName(String playerName) {
        GameProfile profile;
        Optional<Player> player = plugin.getProxy().getPlayer(playerName);
        if (player.isPresent()) {
            profile = player.get().getGameProfile();
        } else if (plugin.getProxy().getConfiguration().isOnlineMode()) {
            try {
                profile = MojangApiUtil.getGameProfileByName(playerName);
            } catch (Exception e) {
                return null;
            }
        } else {
            profile = GameProfile.forOfflinePlayer(playerName);
        }
        return profile;
    }
}