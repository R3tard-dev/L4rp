package play451.is.larping.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;
import play451.is.larping.Larp;
import play451.is.larping.features.modules.Module;
import play451.is.larping.features.modules.ModuleManager;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class Config {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_DIR = FabricLoader.getInstance()
            .getGameDir()
            .resolve("l4rp")
            .resolve("config");
    private static final String GUI_CONFIG_FILE = "gui.json";
    private static final String MODULES_CONFIG_FILE = "modules.json";
    
    // GUI Settings
    public int guiX = 100;
    public int guiY = 100;
    public int guiWidth = 600;
    public int guiHeight = 420;
    
    private static Config INSTANCE;
    
    public static Config getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Config();
            INSTANCE.loadGui();
            INSTANCE.loadModules();
        }
        return INSTANCE;
    }
    
    private void loadGui() {
        try {
            // Create directories if they don't exist
            if (!Files.exists(CONFIG_DIR)) {
                Files.createDirectories(CONFIG_DIR);
                Larp.LOGGER.info("Created config directory at " + CONFIG_DIR);
            }
            
            Path configPath = CONFIG_DIR.resolve(GUI_CONFIG_FILE);
            
            if (Files.exists(configPath)) {
                try (FileReader reader = new FileReader(configPath.toFile())) {
                    Config loaded = GSON.fromJson(reader, Config.class);
                    if (loaded != null) {
                        this.guiX = loaded.guiX;
                        this.guiY = loaded.guiY;
                        this.guiWidth = loaded.guiWidth;
                        this.guiHeight = loaded.guiHeight;
                    }
                    Larp.LOGGER.info("Loaded GUI config from " + configPath);
                }
            }
        } catch (IOException e) {
            Larp.LOGGER.error("Failed to load GUI config", e);
        }
    }
    
    private void loadModules() {
        try {
            Path configPath = CONFIG_DIR.resolve(MODULES_CONFIG_FILE);
            
            if (Files.exists(configPath)) {
                try (FileReader reader = new FileReader(configPath.toFile())) {
                    Type type = new TypeToken<Map<String, ModuleConfig>>(){}.getType();
                    Map<String, ModuleConfig> configs = GSON.fromJson(reader, type);
                    
                    if (configs != null) {
                        for (Module module : ModuleManager.getInstance().getModules()) {
                            ModuleConfig config = configs.get(module.getName());
                            if (config != null) {
                                module.setEnabled(config.enabled);
                                module.loadSettings(config.settings);
                            }
                        }
                    }
                    Larp.LOGGER.info("Loaded module configs from " + configPath);
                }
            }
        } catch (IOException e) {
            Larp.LOGGER.error("Failed to load module configs", e);
        }
    }
    
    public void saveGui() {
        try {
            // Ensure directories exist
            if (!Files.exists(CONFIG_DIR)) {
                Files.createDirectories(CONFIG_DIR);
            }
            
            Path configPath = CONFIG_DIR.resolve(GUI_CONFIG_FILE);
            try (FileWriter writer = new FileWriter(configPath.toFile())) {
                GSON.toJson(this, writer);
                Larp.LOGGER.info("Saved GUI config to " + configPath);
            }
        } catch (IOException e) {
            Larp.LOGGER.error("Failed to save GUI config", e);
        }
    }
    
    public void saveModules() {
        try {
            // Ensure directories exist
            if (!Files.exists(CONFIG_DIR)) {
                Files.createDirectories(CONFIG_DIR);
            }
            
            Map<String, ModuleConfig> configs = new HashMap<>();
            for (Module module : ModuleManager.getInstance().getModules()) {
                ModuleConfig config = new ModuleConfig();
                config.enabled = module.isEnabled();
                config.settings = module.saveSettings();
                configs.put(module.getName(), config);
            }
            
            Path configPath = CONFIG_DIR.resolve(MODULES_CONFIG_FILE);
            try (FileWriter writer = new FileWriter(configPath.toFile())) {
                GSON.toJson(configs, writer);
                Larp.LOGGER.info("Saved module configs to " + configPath);
            }
        } catch (IOException e) {
            Larp.LOGGER.error("Failed to save module configs", e);
        }
    }
    
    // Getters and setters
    public int getGuiX() {
        return guiX;
    }
    
    public void setGuiX(int guiX) {
        this.guiX = guiX;
        saveGui();
    }
    
    public int getGuiY() {
        return guiY;
    }
    
    public void setGuiY(int guiY) {
        this.guiY = guiY;
        saveGui();
    }
    
    public int getGuiWidth() {
        return guiWidth;
    }
    
    public void setGuiWidth(int guiWidth) {
        this.guiWidth = guiWidth;
        saveGui();
    }
    
    public int getGuiHeight() {
        return guiHeight;
    }
    
    public void setGuiHeight(int guiHeight) {
        this.guiHeight = guiHeight;
        saveGui();
    }
    
    public void setGuiPosition(int x, int y) {
        this.guiX = x;
        this.guiY = y;
        saveGui();
    }
    
    // Module config class
    private static class ModuleConfig {
        boolean enabled;
        Map<String, Object> settings;
    }
}