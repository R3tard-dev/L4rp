package play451.is.larping.features.modules.combat;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import play451.is.larping.config.Config;
import play451.is.larping.features.modules.Module;
import play451.is.larping.features.modules.ModuleCategory;
import play451.is.larping.features.modules.ModuleSettingsRenderer;
import play451.is.larping.features.modules.SettingsHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AntiTrap extends Module implements ModuleSettingsRenderer {

    private boolean removeExisting = true;
    private boolean preventSpawn = true;
    private boolean armorStands = true;
    private boolean chestMinecarts = true;

    private final MinecraftClient mc = MinecraftClient.getInstance();

    public AntiTrap() {
        super("AntiTrap", "Allows you to escape from armor stands and chest minecarts", ModuleCategory.COMBAT);
    }

    @Override
    public void onEnable() {
        if (removeExisting) {
            removeTrapEntities();
        }
    }

    @Override
    public void onTick() {
        if (mc.world == null || mc.player == null) return;

        if (preventSpawn || removeExisting) {
            List<Entity> toRemove = new ArrayList<>();

            for (Entity entity : mc.world.getEntities()) {
                if (isTrapEntity(entity)) {
                    toRemove.add(entity);
                }
            }

            for (Entity entity : toRemove) {
                entity.discard();
            }
        }
    }

    private void removeTrapEntities() {
        if (mc.world == null) return;

        List<Entity> toRemove = new ArrayList<>();
        for (Entity entity : mc.world.getEntities()) {
            if (isTrapEntity(entity)) {
                toRemove.add(entity);
            }
        }

        for (Entity entity : toRemove) {
            entity.discard();
        }
    }

    private boolean isTrapEntity(Entity entity) {
        if (entity == null) return false;

        EntityType<?> type = entity.getType();

        if (armorStands && type == EntityType.ARMOR_STAND) return true;
        if (chestMinecarts && type == EntityType.CHEST_MINECART) return true;

        return false;
    }

    @Override
    public Map<String, Object> saveSettings() {
        Map<String, Object> settings = new HashMap<>();
        settings.put("removeExisting", removeExisting);
        settings.put("preventSpawn", preventSpawn);
        settings.put("armorStands", armorStands);
        settings.put("chestMinecarts", chestMinecarts);
        return settings;
    }

    @Override
    public void loadSettings(Map<String, Object> settings) {
        if (settings.containsKey("removeExisting")) removeExisting = (Boolean) settings.get("removeExisting");
        if (settings.containsKey("preventSpawn")) preventSpawn = (Boolean) settings.get("preventSpawn");
        if (settings.containsKey("armorStands")) armorStands = (Boolean) settings.get("armorStands");
        if (settings.containsKey("chestMinecarts")) chestMinecarts = (Boolean) settings.get("chestMinecarts");
    }

    public void setRemoveExisting(boolean removeExisting) {
        this.removeExisting = removeExisting;
        Config.getInstance().saveModules();
    }

    public void setPreventSpawn(boolean preventSpawn) {
        this.preventSpawn = preventSpawn;
        Config.getInstance().saveModules();
    }

    public void setArmorStands(boolean armorStands) {
        this.armorStands = armorStands;
        Config.getInstance().saveModules();
    }

    public void setChestMinecarts(boolean chestMinecarts) {
        this.chestMinecarts = chestMinecarts;
        Config.getInstance().saveModules();
    }

    @Override
    public int renderSettings(DrawContext context, int mouseX, int mouseY, int startX, int startY, int width, SettingsHelper helper) {
        int settingY = startY;

        helper.renderLabel(context, "Remove Existing:", null, startX, settingY);
        helper.renderToggle(context, mouseX, mouseY, startX + 150, settingY - 5, removeExisting);
        settingY += 35;

        helper.renderLabel(context, "Prevent Spawn:", null, startX, settingY);
        helper.renderToggle(context, mouseX, mouseY, startX + 150, settingY - 5, preventSpawn);
        settingY += 35;

        helper.renderLabel(context, "Armor Stands:", null, startX, settingY);
        helper.renderToggle(context, mouseX, mouseY, startX + 150, settingY - 5, armorStands);
        settingY += 35;

        helper.renderLabel(context, "Chest Minecarts:", null, startX, settingY);
        helper.renderToggle(context, mouseX, mouseY, startX + 150, settingY - 5, chestMinecarts);

        return settingY + 25;
    }

    @Override
    public boolean handleSettingsClick(double mouseX, double mouseY, int startX, int startY, int width, SettingsHelper helper) {
        int settingY = startY;

        if (helper.isToggleHovered(mouseX, mouseY, startX + 150, settingY - 5)) {
            setRemoveExisting(!removeExisting);
            return true;
        }
        settingY += 35;

        if (helper.isToggleHovered(mouseX, mouseY, startX + 150, settingY - 5)) {
            setPreventSpawn(!preventSpawn);
            return true;
        }
        settingY += 35;

        if (helper.isToggleHovered(mouseX, mouseY, startX + 150, settingY - 5)) {
            setArmorStands(!armorStands);
            return true;
        }
        settingY += 35;

        if (helper.isToggleHovered(mouseX, mouseY, startX + 150, settingY - 5)) {
            setChestMinecarts(!chestMinecarts);
            return true;
        }

        return false;
    }
}