package play451.is.larping.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import play451.is.larping.Larp;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Config {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_DIR = FabricLoader.getInstance()
            .getGameDir()
            .resolve("l4rp")
            .resolve("config");
    private static final String CONFIG_FILE = "settings.json";
    
    // GUI Settings
    public int guiX = 100;
    public int guiY = 100;
    public int guiWidth = 600;
    public int guiHeight = 420;
    
    private static Config INSTANCE;
    
    public static Config getInstance() {
        if (INSTANCE == null) {
            INSTANCE = load();
        }
        return INSTANCE;
    }
    
    public static Config load() {
        try {
            // Create directories if they don't exist
            if (!Files.exists(CONFIG_DIR)) {
                Files.createDirectories(CONFIG_DIR);
                Larp.LOGGER.info("Created config directory at " + CONFIG_DIR);
            }
            
            Path configPath = CONFIG_DIR.resolve(CONFIG_FILE);
            
            if (Files.exists(configPath)) {
                try (FileReader reader = new FileReader(configPath.toFile())) {
                    Config config = GSON.fromJson(reader, Config.class);
                    Larp.LOGGER.info("Loaded config from " + configPath);
                    return config != null ? config : new Config();
                }
            }
        } catch (IOException e) {
            Larp.LOGGER.error("Failed to load config", e);
        }
        
        Config config = new Config();
        config.save(); // Save default config
        return config;
    }
    
    public void save() {
        try {
            // Ensure directories exist
            if (!Files.exists(CONFIG_DIR)) {
                Files.createDirectories(CONFIG_DIR);
            }
            
            Path configPath = CONFIG_DIR.resolve(CONFIG_FILE);
            try (FileWriter writer = new FileWriter(configPath.toFile())) {
                GSON.toJson(this, writer);
                Larp.LOGGER.info("Saved config to " + configPath);
            }
        } catch (IOException e) {
            Larp.LOGGER.error("Failed to save config", e);
        }
    }
    
    // Getters and setters
    public int getGuiX() {
        return guiX;
    }
    
    public void setGuiX(int guiX) {
        this.guiX = guiX;
        save();
    }
    
    public int getGuiY() {
        return guiY;
    }
    
    public void setGuiY(int guiY) {
        this.guiY = guiY;
        save();
    }
    
    public int getGuiWidth() {
        return guiWidth;
    }
    
    public void setGuiWidth(int guiWidth) {
        this.guiWidth = guiWidth;
        save();
    }
    
    public int getGuiHeight() {
        return guiHeight;
    }
    
    public void setGuiHeight(int guiHeight) {
        this.guiHeight = guiHeight;
        save();
    }
    
    public void setGuiPosition(int x, int y) {
        this.guiX = x;
        this.guiY = y;
        save();
    }
}