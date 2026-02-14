package play451.is.larping.features.modules.combat;

import play451.is.larping.features.modules.Module;
import play451.is.larping.features.modules.ModuleCategory;

public class ExampleCombatModule extends Module {
    public ExampleCombatModule() {
        super("Example Combat", "Example combat module", ModuleCategory.COMBAT);
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