package play451.is.larping.features.modules.movement;

import play451.is.larping.features.modules.Module;
import play451.is.larping.features.modules.ModuleCategory;

public class ExampleMovementModule extends Module {
    public ExampleMovementModule() {
        super("Example Movement", "Example movement module", ModuleCategory.MOVEMENT);
    }
    
    @Override
    public void onEnable() {
        // Called when module is enabled
    }
    
    @Override
    public void onDisable() {
        // Called when module is disabled
    }
    
    @Override
    public void onTick() {
        // Called every tick when module is enabled
    }
}