package play451.is.larping.features.modules.render;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import play451.is.larping.config.Config;
import play451.is.larping.features.modules.Module;
import play451.is.larping.features.modules.ModuleCategory;
import play451.is.larping.features.modules.ModuleSettingsRenderer;
import play451.is.larping.features.modules.SettingsHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Search extends Module implements ModuleSettingsRenderer {

    private final Set<String> targetBlocks = new HashSet<>();

    private String renderMode = "Box";
    private String colourMode = "Block";
    private double maxRange   = 50.0;
    private int    fillColour = 0x32FFFF00;
    private int    lineColour = 0xFFFFFFFF;

    private final List<BlockPos>  foundBlocks = new ArrayList<>();
    private final Object          lock        = new Object();
    private final ExecutorService executor    = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "search-scanner");
        t.setDaemon(true);
        return t;
    });
    private volatile boolean searching = false;
    private int              tickTimer  = 0;

    private static final int SCAN_INTERVAL = 10;

    private final MinecraftClient mc = MinecraftClient.getInstance();

    public Search() {
        super("Search", "Highlights target blocks in the world", ModuleCategory.RENDER);
    }

    public static void tickAll() {
        Search inst = getInstance();
        if (inst == null || !inst.isEnabled()) return;
        inst.tick();
    }

    public static void renderAll(Object renderCtx) {
        Search inst = getInstance();
        if (inst == null || !inst.isEnabled()) return;
        inst.renderWorld(renderCtx);
    }

    private static Search getInstance() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.world == null) return null;
        for (var m : play451.is.larping.features.modules.ModuleManager.getInstance().getModules()) {
            if (m instanceof Search s) return s;
        }
        return null;
    }

    private void tick() {
        if (mc.player == null || mc.world == null) return;
        if (searching) return;
        if (++tickTimer < SCAN_INTERVAL) return;
        tickTimer = 0;

        searching = true;
        final double rangeSq = maxRange * maxRange;
        final int    r       = (int) maxRange;
        final BlockPos origin  = mc.player.getBlockPos();
        final Set<String> targets = new HashSet<>(targetBlocks);

        executor.execute(() -> {
            try {
                List<BlockPos> found = new ArrayList<>();
                for (int x = -r; x <= r; x++) {
                    for (int y = -r; y <= r; y++) {
                        for (int z = -r; z <= r; z++) {
                            BlockPos pos = origin.add(x, y, z);
                            double dx = pos.getX() + 0.5 - mc.player.getX();
                            double dy = pos.getY() + 0.5 - mc.player.getY();
                            double dz = pos.getZ() + 0.5 - mc.player.getZ();
                            if (dx * dx + dy * dy + dz * dz > rangeSq) continue;

                            BlockState state = mc.world.getBlockState(pos);
                            String id = net.minecraft.registry.Registries.BLOCK
                                    .getId(state.getBlock()).toString();
                            if (targets.contains(id)) {
                                found.add(pos.toImmutable());
                            }
                        }
                    }
                }
                synchronized (lock) {
                    foundBlocks.clear();
                    foundBlocks.addAll(found);
                }
            } finally {
                searching = false;
            }
        });
    }

    @Override
    public void onDisable() {
        synchronized (lock) {
            foundBlocks.clear();
        }
    }

    private void renderWorld(Object renderCtx) {
        if (mc.player == null || mc.world == null) return;

        List<BlockPos> toRender;
        synchronized (lock) {
            if (foundBlocks.isEmpty()) return;
            toRender = new ArrayList<>(foundBlocks);
        }

        boolean drawBox    = renderMode.equals("Box")    || renderMode.equals("Both");
        boolean drawTracer = renderMode.equals("Tracer") || renderMode.equals("Both");
        boolean useCustom  = colourMode.equals("Custom");

        Vec3d cameraPos = mc.gameRenderer.getCamera().getPos();

        for (BlockPos pos : toRender) {
            int fill, line;

            if (useCustom) {
                fill = fillColour;
                line = lineColour;
            } else {
                BlockState state = mc.world.getBlockState(pos);
                int c = mc.getBlockColors().getColor(state, mc.world, pos, 0);
                if (c <= 0) c = state.getMapColor(mc.world, pos).color;
                int rr = (c >> 16) & 0xFF, gg = (c >> 8) & 0xFF, bb = c & 0xFF;
                fill = (0x50 << 24) | (rr << 16) | (gg << 8) | bb;
                line = (0xFF << 24) | (rr << 16) | (gg << 8) | bb;
            }

            renderBlock(renderCtx, pos, cameraPos, fill, line, drawBox, drawTracer);
        }
    }

    private void renderBlock(Object ctx, BlockPos pos, Vec3d cameraPos,
                             int fill, int line, boolean drawBox, boolean drawTracer) {
    }

    public void addBlock(String registryName)    { targetBlocks.add(registryName);    save(); }
    public void removeBlock(String registryName) { targetBlocks.remove(registryName); save(); }
    public Set<String> getTargetBlocks()         { return Collections.unmodifiableSet(targetBlocks); }

    @Override
    public Map<String, Object> saveSettings() {
        Map<String, Object> s = super.saveSettings();
        s.put("targetBlocks", String.join(",", targetBlocks));
        s.put("renderMode",   renderMode);
        s.put("colourMode",   colourMode);
        s.put("maxRange",     maxRange);
        s.put("fillColour",   fillColour);
        s.put("lineColour",   lineColour);
        return s;
    }

    @Override
    public void loadSettings(Map<String, Object> s) {
        super.loadSettings(s);
        if (s.containsKey("targetBlocks")) {
            targetBlocks.clear();
            String raw = (String) s.get("targetBlocks");
            if (!raw.isBlank()) for (String b : raw.split(",")) targetBlocks.add(b.trim());
        }
        if (s.containsKey("renderMode")) renderMode = (String)  s.get("renderMode");
        if (s.containsKey("colourMode")) colourMode = (String)  s.get("colourMode");
        if (s.containsKey("maxRange"))   maxRange   = ((Number) s.get("maxRange")).doubleValue();
        if (s.containsKey("fillColour")) fillColour = ((Number) s.get("fillColour")).intValue();
        if (s.containsKey("lineColour")) lineColour = ((Number) s.get("lineColour")).intValue();
    }

    @Override
    public int renderSettings(DrawContext context, int mouseX, int mouseY,
                              int startX, int startY, int width, SettingsHelper helper) {
        int y    = startY;
        int col1 = startX;

        helper.renderInfo(context, "── Render ──", col1, y); y += 14;

        helper.renderLabel(context, "Mode:", null, col1, y);
        helper.renderModeSelector(context, mouseX, mouseY, col1 + 55, y,
                new String[]{"Box", "Tracer", "Both"}, renderMode, 43);
        y += 28;

        helper.renderInfo(context, "── Colour ──", col1, y); y += 14;

        helper.renderLabel(context, "Source:", null, col1, y);
        helper.renderModeSelector(context, mouseX, mouseY, col1 + 65, y,
                new String[]{"Block", "Custom"}, colourMode, 47);
        y += 28;

        helper.renderInfo(context, "── Misc ──", col1, y); y += 14;

        helper.renderLabel(context, "Range:", String.format("%.0fm", maxRange), col1, y);
        helper.renderSlider(context, col1 + 90, y + 4, 120, maxRange, 5, 128);
        y += 24;

        helper.renderInfo(context, "── Target Blocks ──", col1, y); y += 14;
        if (targetBlocks.isEmpty()) {
            helper.renderLabel(context, "(none)", null, col1, y); y += 14;
        } else {
            for (String b : targetBlocks) {
                String display = b.contains(":") ? b.substring(b.indexOf(':') + 1) : b;
                helper.renderLabel(context, "• " + display, null, col1, y); y += 12;
            }
            y += 4;
        }

        return y;
    }

    @Override
    public boolean handleSettingsClick(double mx, double my, int startX, int startY,
                                        int width, SettingsHelper helper) {
        int y    = startY;
        int col1 = startX;

        y += 14;
        int modeX = col1 + 55;
        for (String opt : new String[]{"Box", "Tracer", "Both"}) {
            if (helper.isModeButtonHovered(mx, my, modeX, y, 43)) { renderMode = opt; save(); return true; }
            modeX += 46;
        }
        y += 28;

        y += 14;
        int cmX = col1 + 65;
        for (String opt : new String[]{"Block", "Custom"}) {
            if (helper.isModeButtonHovered(mx, my, cmX, y, 47)) { colourMode = opt; save(); return true; }
            cmX += 50;
        }
        y += 28;

        y += 14;
        if (helper.isSliderHovered(mx, my, col1 + 90, y + 4, 120)) {
            maxRange = helper.calculateSliderValue(mx, col1 + 90, 120, 5, 128);
            save(); return true;
        }

        return false;
    }

    private void save() { Config.getInstance().saveModules(); }
}