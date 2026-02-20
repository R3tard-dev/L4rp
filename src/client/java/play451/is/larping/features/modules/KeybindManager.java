package play451.is.larping.features.modules;

import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;
import play451.is.larping.features.gui.ClickGui;

import java.util.HashMap;
import java.util.Map;

 
public class KeybindManager {
    
    private static final KeybindManager INSTANCE = new KeybindManager();
    
    
    private final Map<Integer, Boolean> prevKeyState = new HashMap<>();
    
    private final MinecraftClient mc = MinecraftClient.getInstance();
    
    private KeybindManager() {}
    
    public static KeybindManager getInstance() {
        return INSTANCE;
    }
    
     
    public void tick() {
        if (mc.getWindow() == null) return;
        
        
        if (mc.currentScreen != null && !(mc.currentScreen instanceof ClickGui)) {
            prevKeyState.clear();
            return;
        }
        
        for (Module module : ModuleManager.getInstance().getModules()) {
            int key = module.getKeyBind();
            if (key == -1) continue;
            
            boolean currentlyPressed = GLFW.glfwGetKey(mc.getWindow().getHandle(), key) == GLFW.GLFW_PRESS;
            boolean wasPressed = prevKeyState.getOrDefault(key, false);
            
            
            if (currentlyPressed && !wasPressed) {
                module.toggle();
            }
            
            prevKeyState.put(key, currentlyPressed);
        }
    }
}