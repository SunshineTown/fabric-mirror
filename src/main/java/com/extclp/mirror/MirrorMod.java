package com.extclp.mirror;

import com.extclp.mirror.command.MirrorCommand;
import com.extclp.mirror.config.MirrorConfig;
import com.extclp.mirror.config.MirrorInfo;
import com.extclp.mirror.config.MirrorMessage;
import com.extclp.mirror.config.MirrorsData;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.registry.CommandRegistry;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Map;

public class MirrorMod implements ModInitializer {

    private static final Gson GSON = new GsonBuilder().
            registerTypeAdapter(SimpleDateFormat.class, new TypeAdapter<SimpleDateFormat>() {
                @Override
                public void write(JsonWriter out, SimpleDateFormat value) throws IOException {
                    if (value == null) {
                        out.nullValue();
                    } else {
                        out.value(value.toPattern());
                    }
                }

                @Override
                public SimpleDateFormat read(JsonReader in) throws IOException {
                    if(in.peek() == JsonToken.NULL){
                        in.nextNull();
                        return null;
                    }else {
                        return new SimpleDateFormat(in.nextString());
                    }
                }
            }).
            registerTypeAdapter(File.class, new TypeAdapter<File>() {
                @Override
                public void write(JsonWriter out, File value) throws IOException {
                    if (value == null) {
                        out.nullValue();
                    } else {
                        out.value(value.getPath());
                    }
                }

                @Override
                public File read(JsonReader in) throws IOException {
                    if(in.peek() == JsonToken.NULL){
                        in.nextNull();
                        return null;
                    }else {
                        return new File(in.nextString());
                    }
                }
            }).
            setPrettyPrinting().create();

    private static MirrorConfig config;

    private static MirrorMessage messages;

    private static MirrorsData mirrorsData;

    public static MirrorsData getMirrorsData() {
        return mirrorsData;
    }

    public static MirrorMessage getMessages() {
        return messages;
    }

    public static MirrorConfig getConfig() {
        return config;
    }

    public static Map<String, MirrorInfo> getMirrors(){
        return mirrorsData.mirrors;
    }

    @Override
    public void onInitialize() {
        try {
            if(setupMessage() && setupConfig() && loadMirrors()){
                CommandRegistry.INSTANCE.register(true, MirrorCommand::register);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static boolean setupConfig() throws IOException {
        File configFile = new File("config/mirror/config.json");
        if(configFile.exists()){
            try (FileReader reader = new FileReader(configFile)){
                config = GSON.fromJson(reader, MirrorConfig.class);
            }
        } else {
            configFile.getParentFile().mkdirs();
            config = new MirrorConfig();
        }
        try (FileWriter writer = new FileWriter(configFile)){
            GSON.toJson(config, config.getClass(), writer);
        }
        return true;
    }

    public static boolean setupMessage() throws IOException {
        File messageConfigFile = new File("config/mirror/message.json");
        if(messageConfigFile.exists()){
            try (FileReader reader = new FileReader(messageConfigFile)){
                messages = GSON.fromJson(reader, MirrorMessage.class);
            }
        } else {
            messageConfigFile.getParentFile().mkdirs();
            messages = new MirrorMessage();
        }
        try (FileWriter writer = new FileWriter(messageConfigFile)){
            GSON.toJson(messages, messages.getClass(), writer);
        }
        return true;
    }

    private static boolean loadMirrors() throws IOException {
        if(getConfig().mirrorsDataFile.exists()){
            try (FileReader reader = new FileReader(getConfig().mirrorsDataFile)){
                mirrorsData = GSON.fromJson(reader, MirrorsData.class);
            }
        } else {
            mirrorsData = new MirrorsData();
        }
        return saveMirrors();
    }

    public static boolean saveMirrors() {
        try {
            config.mirrorsDataFile.getParentFile().mkdirs();
            try (FileWriter writer = new FileWriter(config.mirrorsDataFile)){
                GSON.toJson(mirrorsData, MirrorsData.class, writer);
            }
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

}
