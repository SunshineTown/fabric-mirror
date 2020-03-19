package com.extclp.mirror.command;

import com.extclp.mirror.MirrorMod;
import com.extclp.mirror.config.ButtonText;
import com.extclp.mirror.config.MirrorInfo;
import com.extclp.mirror.utils.Compress;
import com.extclp.mirror.utils.Texts;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.arguments.MessageArgumentType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.net.URI;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static com.extclp.mirror.MirrorMod.*;

public class MirrorCommand {

    public static Thread backUpThread;
    public static Thread returnMirrorThread;
    public static boolean cancel = false;

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("mirror")
                .then(CommandManager.literal("reload")
                        .then(CommandManager.literal("config")
                                .executes(context -> reloadConfig(context.getSource())))
                        .then(CommandManager.literal("message")
                                .executes(context -> reloadMessage(context.getSource()))))
                .then(CommandManager.literal("list").executes(context -> listMirror(context.getSource())))
                .then(CommandManager.literal("create")
                        .then(CommandManager.argument("name", StringArgumentType.string())
                                .then(CommandManager.argument("description", MessageArgumentType.message())
                                        .executes(context -> createMirror(context.getSource(),
                                                StringArgumentType.getString(context, "name"),
                                                MessageArgumentType.getMessage(context, "description").getString()
                                                , false)))
                                .then(CommandManager.literal("overwrite")
                                        .executes(context -> createMirror(context.getSource(),
                                                StringArgumentType.getString(context, "name"),
                                                MessageArgumentType.getMessage(context, "description").getString(), true))
                                        .then(CommandManager.argument("description", MessageArgumentType.message())
                                                .executes(context -> createMirror(context.getSource(),
                                                        StringArgumentType.getString(context, "name"),
                                                        MessageArgumentType.getMessage(context, "description").getString(), true))))))
                .then(CommandManager.literal("lock")
                        .requires(source -> source.hasPermissionLevel(1))
                        .then(CommandManager.argument("name", StringArgumentType.string())
                                .suggests(MirrorCommand::suggest)
                                .executes(context -> lock(context.getSource(), StringArgumentType.getString(context, "name")))))
                .then(CommandManager.literal("unlock")
                        .requires(source -> source.hasPermissionLevel(1))
                        .then(CommandManager.argument("name", StringArgumentType.string())
                                 .suggests(MirrorCommand::suggest)
                                 .executes(context -> unlock(context.getSource(), StringArgumentType.getString(context, "name")))))
                .then(CommandManager.literal("delete")
                        .executes(context -> listMirror(context.getSource()))
                        .then(CommandManager.argument("name", StringArgumentType.string())
                                .suggests(MirrorCommand::suggest)
                                .executes(context -> deleteMirror(context.getSource(), StringArgumentType.getString(context, "name"), false))
                                .then(CommandManager.literal("confirm")
                                        .executes(context -> deleteMirror(context.getSource(), StringArgumentType.getString(context, "name"), true)))))
                .then(CommandManager.literal("return")
                        .executes(context -> listMirror(context.getSource()))
                        .then(CommandManager.argument("name", StringArgumentType.string())
                                .suggests(MirrorCommand::suggest)
                                .executes(context -> returnMirror(context.getSource(), StringArgumentType.getString(context, "name"), false))
                                .then(CommandManager.literal("confirm")
                                        .executes(context -> returnMirror(context.getSource(), StringArgumentType.getString(context, "name"), true)))))
                .then(CommandManager.literal("cancel").executes(context -> cancelTask(context.getSource())))
        );
    }

    public static CompletableFuture<Suggestions> suggest(CommandContext<ServerCommandSource> context,
                                                         SuggestionsBuilder suggestionsBuilder){
        getMirrors().keySet().forEach(suggestionsBuilder::suggest);
        return suggestionsBuilder.buildFuture();
    }

    public static String getMirrorName(MirrorInfo mirrorInfo){
        return  (mirrorInfo.isLock() ? getConfig().lockMirrorNameColor : getConfig().mirrorNameColor) + mirrorInfo.getName();
    }

    public static LiteralText createMirrorText(MinecraftServer server, MirrorInfo mirrorInfo){
        LiteralText text = Texts.of(getMirrorName(mirrorInfo));

        StringBuilder descBuilder = new StringBuilder();
        descBuilder.append(String.format(getMessages().listMirrorCreatedTime, getConfig().mirrorDateFormat.
                format(mirrorInfo.getCreateDate()), mirrorInfo.getDescription()));
        descBuilder.append('\n');
        descBuilder.append(Formatting.RESET);
        descBuilder.append(String.format(getMessages().listMirrorDescription, mirrorInfo.getDescription()));
        if(mirrorInfo.getOwner() != null){
            descBuilder.append('\n');
            descBuilder.append(Formatting.RESET);
            GameProfile creator = server.getUserCache().getByUuid(mirrorInfo.getOwner());
            if(creator != null){
                descBuilder.append(String.format(getMessages().listMirrorOwner, creator.getName()));
            } else {
                descBuilder.append(String.format(getMessages().listMirrorOwner, mirrorInfo.getOwner()));
            }
        }
        if(mirrorInfo.isLock()){
            descBuilder.append('\n');
            descBuilder.append(Formatting.RESET);
            descBuilder.append(getMessages().listMirrorLock);
        }
        Text showText = Texts.of(descBuilder.toString(), getConfig().mirrorDateFormat.
                format(mirrorInfo.getCreateDate()), mirrorInfo.getDescription());
        text.setStyle(new Style().setHoverEvent(
                new HoverEvent(HoverEvent.Action.SHOW_TEXT, showText)));
        return text;
    }

    public static int reloadConfig(ServerCommandSource source) {
        try {
            setupConfig();
            source.sendFeedback(Texts.of(getMessages().reloadConfig),false);
            return 1;
        }catch (Exception e){
            e.printStackTrace();
        }
        return 0;
    }

    public static int reloadMessage(ServerCommandSource source) {
        try {
            setupMessage();
            source.sendFeedback(Texts.of(getMessages().reloadMessage),false);
            return 1;
        }catch (Exception e){
            e.printStackTrace();
        }
        return 0;
    }

    public static int listMirror(ServerCommandSource source) {
        source.sendFeedback(Texts.of(getMessages().listMirrorHeader), false);
        for (MirrorInfo mirrorInfo : getMirrors().values()) {
            LiteralText mirrorText = createMirrorText(source.getMinecraftServer(), mirrorInfo);
            LiteralText returnButtonText = buttonText(getMessages().listMirrorReturnButton, String.format("/mirror return %s", mirrorInfo.getName()), mirrorInfo.getName());
            LiteralText deleteButtonText = buttonText(getMessages().listMirrorDeleteButton, String.format("/mirror delete %s", mirrorInfo.getName()), mirrorInfo.getName());
            source.sendFeedback(Texts.of(getMessages().listMirror, mirrorText, returnButtonText, deleteButtonText), false);
        }

        source.sendFeedback(Texts.of(getMessages().listMirrorFooter, getMirrors().size()), false);
        return 1;
    }

    public static int returnMirror(ServerCommandSource source, String name, boolean confirm) {

        String failurePrefix = getMessages().returnMirrorFailurePrefix + Formatting.RESET;

        if(backUpThread != null || returnMirrorThread != null){
            sendToAll(source, Texts.of(failurePrefix + MirrorMod.getMessages().otherTaskRunning));
            return 0;
        }
        MirrorInfo mirrorInfo = MirrorMod.getMirrors().get(name);
        if(mirrorInfo == null){
            sendToAll(source, (Texts.of(failurePrefix + getMessages().unknownMirror, name)));
            return 0;
        }
        File backupFile = getBackFile(mirrorInfo);
        if(!backupFile.exists()){
            getMirrors().remove(name);
            saveMirrors();
            sendToAll(source, (Texts.of(failurePrefix + getMessages().mirrorNotFound, createMirrorText(source.getMinecraftServer(), mirrorInfo))));
        }
        File worldFolder = new File(source.getMinecraftServer().getLevelName());

        if(checkDiskIsFull(worldFolder)){
            sendToAll(source, (Texts.of(failurePrefix + getMessages().diskFull)));
            return 0;
        }
        if (!confirm) {
            LiteralText confirmButtonText = buttonText(getMessages().returnConfirmButton, String.format("/mirror return %s confirm", name), name);
            Text text = Texts.of(getMessages().returnConfirm, createMirrorText(source.getMinecraftServer(), mirrorInfo), confirmButtonText);
            source.sendFeedback(text, false);
            return 0;
        }

        File tempWorldFolder = getConfig().tempWorldFolder;
        if(tempWorldFolder.exists()){
            tempWorldFolder.delete();
        }
        Thread shutdownHookThread = new Thread(() -> {
            File oldWorldFolder = null;
            for (int i = 0; i < Integer.MAX_VALUE; i++) {
                oldWorldFolder = new File(getConfig().oldWorldFolder, "world.old." + i);
                if (!oldWorldFolder.exists()) {
                    break;
                }
            }
            oldWorldFolder.getParentFile().mkdirs();
            worldFolder.renameTo(oldWorldFolder);
            tempWorldFolder.renameTo(worldFolder);
        });
        shutdownHookThread.setDaemon(true);
        Runtime.getRuntime().addShutdownHook(shutdownHookThread);
        returnMirrorThread = new Thread(() -> {
            try {
                cancel = false;
                sendToAll(source, Texts.of(getMessages().unzipMirror, createMirrorText(source.getMinecraftServer(), mirrorInfo)));
                if(mirrorInfo.isCompress()){
                    Compress.unzip(backupFile, tempWorldFolder);
                } else {
                    Compress.copy(backupFile, tempWorldFolder);
                }
                if(!checkWorldFolder(tempWorldFolder)){
                    cancel = false;
                    returnMirrorThread = null;
                    Runtime.getRuntime().removeShutdownHook(shutdownHookThread);
                    tempWorldFolder.delete();
                    sendToAll(source, Texts.of(failurePrefix + getMessages().unzipMirrorFailure));
                    return;
                }
                sendToAll(source, Texts.of(getMessages().serverWillRestart, createMirrorText(source.getMinecraftServer(), mirrorInfo)));

                LiteralText cancelButton = buttonText(getMessages().cancelReturnMirrorButton, "/mirror cancel", name);

                for (int i = getConfig().shutdownWaitTime; i > 0; i--) {
                    if (cancel) {
                        cancel = false;
                        returnMirrorThread = null;
                        Runtime.getRuntime().removeShutdownHook(shutdownHookThread);
                        tempWorldFolder.delete();
                        return;
                    }
                    sendToAll(source, Texts.of(getMessages().serverRestatingCountdown, i, cancelButton));
                    Thread.sleep(1000);
                }
                source.getMinecraftServer().getPlayerManager().disconnectAllPlayers();
                source.getMinecraftServer().getNetworkIo().stop();
                source.getMinecraftServer().stop(false);
            } catch (Exception e) {
                cancel = false;
                returnMirrorThread = null;
                Runtime.getRuntime().removeShutdownHook(shutdownHookThread);
                tempWorldFolder.delete();
                e.printStackTrace();
                sendToAll(source, exceptionMessage(String.format(getMessages().unzipMirrorFailure, getMirrorName(mirrorInfo)), e));
            }
        });
        returnMirrorThread.start();
        return 1;
    }

    public static int lock(ServerCommandSource source, String name){
        MirrorInfo mirrorInfo = getMirrors().get(name);
        if(mirrorInfo == null){
            source.sendError(Texts.of(getMessages().lockMirrorFailurePrefix + Formatting.RESET + getMessages().unknownMirror, name));
            return 0;
        }
        mirrorInfo.setLock(true);
        saveMirrors();
        sendToAll(source, Texts.of(getMessages().lockMirror, createMirrorText(source.getMinecraftServer(), mirrorInfo)));

        return 1;
    }
    public static int unlock(ServerCommandSource source, String name){
        MirrorInfo mirrorInfo = getMirrors().get(name);
        if(mirrorInfo == null){
            source.sendError(Texts.of(getMessages().unlockMirrorFailurePrefix + Formatting.RESET + getMessages().unknownMirror, name));
            return 0;
        }
        mirrorInfo.setLock(false);
        saveMirrors();
        sendToAll(source, Texts.of(getMessages().unlockMirror, createMirrorText(source.getMinecraftServer(), mirrorInfo)));
        return 1;
    }


    public static int deleteMirror(ServerCommandSource source, String name, boolean confirm) {
        String failurePrefix = getMessages().deleteMirrorFailurePrefix + Formatting.RESET;;

        MirrorInfo mirrorInfo = getMirrors().get(name);
        if(mirrorInfo == null){
            sendToAll(source, Texts.of(failurePrefix + getMessages().unknownMirror, name));
            return 0;
        }
        if(mirrorInfo.isLock()){
            sendToAll(source, Texts.of(failurePrefix + getMessages().mirrorIsLocked, createMirrorText(source.getMinecraftServer(), mirrorInfo)));
            return 0;
        }
        if(backUpThread != null || returnMirrorThread != null){
            sendToAll(source, Texts.of(failurePrefix + MirrorMod.getMessages().otherTaskRunning));
            return 0;
        }
        if(!confirm){
            Text text = Texts.of(getMessages().deleteMirrorConfirm, createMirrorText(source.getMinecraftServer(), mirrorInfo));
            LiteralText overrideMessage = buttonText(getMessages().deleteMirrorConfirmButton,  String.format("/mirror delete %s confirm", name), name);
            text.append(overrideMessage);
            source.sendFeedback(text, false);
            return 0;
        }
        File backupFile = getBackFile(mirrorInfo);
        if(!backupFile.exists()){
            getMirrors().remove(name);
            sendToAll(source, Texts.of(failurePrefix + getMessages().mirrorNotFound, createMirrorText(source.getMinecraftServer(), mirrorInfo)));
        }
        getMirrors().remove(name);
        backupFile.delete();
        saveMirrors();
        sendToAll(source, Texts.of(getMessages().deleteMirror,
                createMirrorText(source.getMinecraftServer(), mirrorInfo)));
        return 1;
    }

    public static int createMirror(ServerCommandSource source, String name, String description, boolean overwrite) throws CommandSyntaxException {

        String failurePrefix = getMessages().createMirrorFailurePrefix + Formatting.RESET;

        if(backUpThread != null || returnMirrorThread != null){
            sendToAll(source, Texts.of(failurePrefix + getMessages().otherTaskRunning));
            return 0;
        }
        MirrorInfo mirrorInfo = getMirrors().get(name);

        if(mirrorInfo != null){
            if(mirrorInfo.isLock()){
                sendToAll(source, Texts.of(failurePrefix + getMessages().mirrorIsLocked, createMirrorText(source.getMinecraftServer(), mirrorInfo)));
                return 0;
            }
            if(!overwrite){

                LiteralText overrideButton = buttonText(getMessages().crateMirrorOverwriteButton,
                        String.format("/mirror create %s overwrite %s", name, description), name);
                Text message = Texts.of(getMessages().crateMirrorOverwrite, createMirrorText(source.getMinecraftServer(), mirrorInfo), overrideButton);
                source.sendFeedback(message, false);
                return 0;
            }else {
                getBackFile(mirrorInfo).delete();
                getMirrors().remove(name);
            }
        } else {
            if(getMirrors().size() >= getConfig().createMirrorLimit){
                sendToAll(source, Texts.of(failurePrefix + getMessages().createMirrorLimit,
                        getMirrors().size(), getConfig().createMirrorLimit));
                return 0;
            }
        }
        File worldFolder = new File(source.getMinecraftServer().getLevelName());
        if(checkDiskIsFull(worldFolder)){
            sendToAll(source, Texts.of(failurePrefix + getMessages().diskFull));
            return 0;
        }
        long startDate = System.currentTimeMillis();
        File backupOutFile;
        if(getConfig().compressBackupFile){
            backupOutFile = new File(getConfig().backupsFolder, String.format("%s.zip", name));
            backupOutFile.getParentFile().mkdirs();
        }else {
            backupOutFile = new File(getConfig().backupsFolder, name);
            backupOutFile.mkdirs();
        }
        if(backupOutFile.exists()){
            backupOutFile.delete();
        }
        source.getMinecraftServer().save(false, true, true);
        source.getMinecraftServer().getPlayerManager().saveAllPlayerData();
        boolean autoSave = getAutoSave(source.getMinecraftServer());
        setAutoSave(source.getMinecraftServer(), false);

        URI backupsFolderUri = getConfig().backupsFolder.toURI();
        UUID playerUUID = source.getPlayer() != null ? source.getPlayer().getUuid() : null;
        mirrorInfo =  new MirrorInfo(name, playerUUID, backupsFolderUri.relativize(backupOutFile.toURI()).getPath(), startDate, description, getConfig().compressBackupFile);

        MirrorInfo finalMirrorInfo = mirrorInfo;
        backUpThread = new Thread(() -> {
            try {
                sendToAll(source, Texts.of(getMessages().startBackup, createMirrorText(source.getMinecraftServer(), finalMirrorInfo)));
                if(getConfig().compressBackupFile){
                    Compress.zip(worldFolder, backupOutFile);
                }else {
                    Compress.copy(worldFolder, backupOutFile);
                }
                getMirrors().put(name, finalMirrorInfo);
                saveMirrors();
                sendToAll(source, Texts.of(getMessages().backupFinish, System.currentTimeMillis() - startDate));
            } catch (Exception e) {
                backupOutFile.delete();
                e.printStackTrace();
                sendToAll(source, exceptionMessage(failurePrefix + getMessages().backupFailure, e));
            }
            backUpThread = null;
            if(autoSave){
                setAutoSave(source.getMinecraftServer(), true);
            }
        });
        backUpThread.start();
        return -1;
    }

    private static boolean checkWorldFolder(File file){
        if(!file.exists()){
            return false;
        }
        if(file.isFile()){
            return false;
        }
        if(!new File(file, "level.dat").exists()){
            return false;
        }
        if(new File(file, "region").listFiles().length == 0){
            return false;
        }
        return true;
    }

    private static Text exceptionMessage(String message, Exception e){
        Text text3 = Texts.of(e.getMessage() == null ? e.getClass().getName() : e.getMessage());
        StackTraceElement[] stackTraceElements = e.getStackTrace();

        for(int j = 0; j < Math.min(stackTraceElements.length, 3); ++j) {
            text3.append("\n\n").append(stackTraceElements[j].getMethodName()).append("\n ").append(stackTraceElements[j].getFileName()).append(":").append(String.valueOf(stackTraceElements[j].getLineNumber()));
        }

        return Texts.of(message).styled((style) -> {
            style.setHoverEvent(new HoverEvent(net.minecraft.text.HoverEvent.Action.SHOW_TEXT, text3));
        });
    }

    private static int cancelTask(ServerCommandSource source) {
        if(returnMirrorThread != null){
            MirrorCommand.cancel = true;
            sendToAll(source, Texts.of(getMessages().cancelReturnMirror));
        } else {
            sendToAll(source, Texts.of(MirrorMod.getMessages().noneReturnMirrorTask));
        }
        return 1;
    }

    private static LiteralText buttonText(ButtonText buttonText, String command, String name){
        LiteralText literalText = Texts.of(buttonText.text);
        Style style = new Style();
        style.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
        style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Texts.of(buttonText.description, name)));
        literalText.setStyle(style);
        return literalText;
    }

    private static void sendToAll(ServerCommandSource source, Text text) {
        Text sendToOther = new TranslatableText("chat.type.admin",source.getDisplayName(), text).formatted(Formatting.GRAY, Formatting.ITALIC);
        for (ServerPlayerEntity player : source.getMinecraftServer().getPlayerManager().getPlayerList()) {
            if(player.equals(source.getEntity())){
                player.sendMessage(text);
            } else {
                player.sendMessage(sendToOther);
            }
        }
        source.getMinecraftServer().sendMessage(text);
    }

    private static boolean getAutoSave(MinecraftServer server) {
        return  !server.getWorlds().iterator().next().savingDisabled;
    }

    private static void setAutoSave(MinecraftServer server, boolean autoSave) {
        for (ServerWorld world : server.getWorlds()) {
            world.savingDisabled = !autoSave;
        }
    }

    private static File getBackFile(MirrorInfo mirrorInfo){
        return new File(getConfig().backupsFolder, mirrorInfo.getFileName());
    }

    private static boolean checkDiskIsFull(File worldFile){
        long worldSize = FileUtils.sizeOfDirectory(worldFile);
        long serverFreeSpace = new File("/").getFreeSpace();
        return worldSize > serverFreeSpace;
    }
}
