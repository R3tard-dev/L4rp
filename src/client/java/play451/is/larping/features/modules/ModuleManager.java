package play451.is.larping.features.modules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModuleManager {
    private static ModuleManager INSTANCE;
    private final List<Module> modules = new ArrayList<>();
    private final Map<ModuleCategory, List<Module>> modulesByCategory = new HashMap<>();
    
    private ModuleManager() {
        // Initialize category lists
        for (ModuleCategory category : ModuleCategory.values()) {
            modulesByCategory.put(category, new ArrayList<>());
        }
    }
    
    public static ModuleManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ModuleManager();
        }
        return INSTANCE;
    }
    
    public void registerModule(Module module) {
        modules.add(module);
        modulesByCategory.get(module.getCategory()).add(module);
    }
    
    public List<Module> getModules() {
        return new ArrayList<>(modules);
    }
    
    public List<Module> getModulesByCategory(ModuleCategory category) {
        return new ArrayList<>(modulesByCategory.getOrDefault(category, new ArrayList<>()));
    }
    
    public Module getModuleByName(String name) {
        return modules.stream()
                .filter(m -> m.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }
    
    public void onTick() {
        for (Module module : modules) {
            if (module.isEnabled()) {
                module.onTick();
            }
        }
    }
}