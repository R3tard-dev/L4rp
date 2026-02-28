package play451.is.larping.features.modules.render;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.fluid.FluidState;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;
import org.joml.Matrix4f;
import play451.is.larping.config.Config;
import play451.is.larping.features.modules.Module;
import play451.is.larping.features.modules.ModuleCategory;
import play451.is.larping.features.modules.ModuleSettingsRenderer;
import play451.is.larping.features.modules.SettingsHelper;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

public class ChunkFinder extends Module implements ModuleSettingsRenderer {

    private boolean detectCobbledDeepslate = true;
    private boolean detectRotatedDeepslate = true;
    private boolean detectEndStone         = true;
    private boolean ignoreExposed          = true;

    private int cobbledThreshold           = 4;
    private int rotatedThreshold           = 3;
    private int endStoneThreshold          = 2;

    private double  renderY                = 64.0;
    private int     chunkColour            = 0x78FFD700;
    private boolean highlightBlocks        = true;
    private int     blockColour            = 0xC878C8FF;

    private final Set<ChunkPos>                      flaggedChunks    = ConcurrentHashMap.newKeySet();
    private final Set<ChunkPos>                      scannedChunks    = ConcurrentHashMap.newKeySet();
    private final Map<BlockPos, SuspiciousBlockType> suspiciousBlocks = new ConcurrentHashMap<>();
    private final AtomicLong                         activeScanCount  = new AtomicLong(0);

    private ExecutorService  pool;
    private volatile boolean active = false;

    private final MinecraftClient mc = MinecraftClient.getInstance();

    public ChunkFinder() {
        super("ChunkFinder", "Highlights chunks with suspicious blocks", ModuleCategory.RENDER);
    }

    @Override
    public void onEnable() {
        active = true;
        flaggedChunks.clear();
        scannedChunks.clear();
        suspiciousBlocks.clear();
        pool = Executors.newFixedThreadPool(2, r -> {
            Thread t = new Thread(r, "ChunkFinder-Worker");
            t.setDaemon(true);
            t.setPriority(Thread.NORM_PRIORITY - 1);
            return t;
        });
        scheduleInitialScan();
    }

    @Override
    public void onDisable() {
        active = false;
        if (pool != null) { pool.shutdownNow(); pool = null; }
        flaggedChunks.clear();
        scannedChunks.clear();
        suspiciousBlocks.clear();
    }

    public static void renderWorld(MatrixStack matrices, VertexConsumerProvider consumers, Camera camera) {
        ChunkFinder inst = getInstance();
        if (inst == null || !inst.isEnabled()) return;
        inst.doRender(matrices, consumers, camera);
    }

    private static ChunkFinder getInstance() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null) return null;
        for (var m : play451.is.larping.features.modules.ModuleManager.getInstance().getModules()) {
            if (m instanceof ChunkFinder cf) return cf;
        }
        return null;
    }

    public void onChunkLoad(WorldChunk chunk) {
        if (!active || pool == null) return;
        ChunkPos pos = chunk.getPos();
        if (scannedChunks.contains(pos)) return;
        pool.submit(() -> analyzeChunk(chunk));
    }

    private void scheduleInitialScan() {
        if (mc.world == null || mc.player == null) return;
        pool.submit(() -> {
            int viewDist = mc.options.getViewDistance().getValue();
            int pcx = mc.player.getChunkPos().x;
            int pcz = mc.player.getChunkPos().z;
            for (int cx = pcx - viewDist; cx <= pcx + viewDist; cx++) {
                for (int cz = pcz - viewDist; cz <= pcz + viewDist; cz++) {
                    if (!active) return;
                    WorldChunk c = mc.world.getChunkManager().getWorldChunk(cx, cz);
                    if (c != null) analyzeChunk(c);
                    try { Thread.sleep(50); } catch (InterruptedException e) { return; }
                }
            }
        });
    }

    private void analyzeChunk(WorldChunk chunk) {
        if (chunk == null || !active) return;
        ChunkPos pos = chunk.getPos();
        if (!scannedChunks.add(pos)) return;

        activeScanCount.incrementAndGet();
        try {
            int maxY = Math.min(chunk.getBottomY() + chunk.getHeight(), 128);
            ChunkSection[] sections = chunk.getSectionArray();

            int cobbled = 0, rotated = 0, endStone = 0;

            for (int si = 0; si < sections.length; si++) {
                ChunkSection section = sections[si];
                if (section == null || section.isEmpty()) continue;

                int sectionBaseY = chunk.getBottomY() + si * 16;
                int startY = Math.max(0,  0        - sectionBaseY);
                int endY   = Math.min(15, maxY - 1 - sectionBaseY);
                if (startY > 15 || endY < 0) continue;

                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        for (int y = startY; y <= endY; y++) {
                            if (!active) return;

                            BlockState state  = section.getBlockState(x, y, z);
                            int        worldY = sectionBaseY + y;
                            BlockPos   bPos   = new BlockPos(pos.getStartX() + x, worldY, pos.getStartZ() + z);

                            if (ignoreExposed && isExposed(bPos)) continue;

                            if (detectCobbledDeepslate && isCobbledDeepslate(state)) {
                                cobbled++;
                                if (highlightBlocks) suspiciousBlocks.put(bPos, SuspiciousBlockType.COBBLED_DEEPSLATE);
                            } else if (detectRotatedDeepslate && isRotatedDeepslate(state)) {
                                rotated++;
                                if (highlightBlocks) suspiciousBlocks.put(bPos, SuspiciousBlockType.ROTATED_DEEPSLATE);
                            } else if (detectEndStone && isEndStone(state)
                                       && mc.world != null
                                       && mc.world.getRegistryKey() != World.END) {
                                endStone++;
                                if (highlightBlocks) suspiciousBlocks.put(bPos, SuspiciousBlockType.END_STONE);
                            }
                        }
                    }
                }
            }

            boolean suspicious =
                (detectCobbledDeepslate && cobbled  >= cobbledThreshold) ||
                (detectRotatedDeepslate && rotated  >= rotatedThreshold) ||
                (detectEndStone         && endStone >= endStoneThreshold);

            if (suspicious) flaggedChunks.add(pos);
            else            flaggedChunks.remove(pos);

        } finally {
            activeScanCount.decrementAndGet();
        }
    }

    private boolean isCobbledDeepslate(BlockState s) {
        return s.getBlock() == Blocks.COBBLED_DEEPSLATE;
    }

    private boolean isRotatedDeepslate(BlockState s) {
        if (s.getBlock() != Blocks.DEEPSLATE) return false;
        if (!s.contains(Properties.AXIS))     return false;
        return s.get(Properties.AXIS) != Direction.Axis.Y;
    }

    private boolean isEndStone(BlockState s) {
        return s.getBlock() == Blocks.END_STONE;
    }

    private boolean isExposed(BlockPos pos) {
        if (mc.world == null) return false;
        for (Direction dir : Direction.values()) {
            BlockPos nb = pos.offset(dir);
            if (nb.getY() < mc.world.getBottomY() || nb.getY() >= mc.world.getHeight()) continue;
            BlockState ns = mc.world.getBlockState(nb);
            if (ns.isAir()) return true;
            FluidState fs = ns.getFluidState();
            if (fs != null && !fs.isEmpty()) return true;
        }
        return false;
    }

    private void doRender(MatrixStack matrices, VertexConsumerProvider consumers, Camera camera) {
        if (mc.world == null) return;

        VertexConsumer lines = consumers.getBuffer(RenderLayer.getLines());
        Matrix4f mat = matrices.peek().getPositionMatrix();

        for (ChunkPos cp : flaggedChunks) {
            renderChunkBox(lines, mat, camera, cp);
        }

        if (highlightBlocks) {
            for (Map.Entry<BlockPos, SuspiciousBlockType> e : suspiciousBlocks.entrySet()) {
                renderBlockBox(lines, mat, camera, e.getKey());
            }
        }
    }

    private void renderChunkBox(VertexConsumer buf, Matrix4f mat, Camera cam, ChunkPos cp) {
        double cx = cam.getPos().x;
        double cy = cam.getPos().y;
        double cz = cam.getPos().z;

        float x1 = (float) (cp.getStartX()     - cx);
        float z1 = (float) (cp.getStartZ()     - cz);
        float x2 = (float) (cp.getEndX() + 1   - cx);
        float z2 = (float) (cp.getEndZ() + 1   - cz);
        float y1 = (float) (renderY             - cy);
        float y2 = (float) (renderY + 0.3       - cy);

        int r = (chunkColour >> 16) & 0xFF;
        int g = (chunkColour >>  8) & 0xFF;
        int b = (chunkColour      ) & 0xFF;
        int a = (chunkColour >> 24) & 0xFF;

        addLine(buf, mat, x1, y1, z1, x2, y1, z1, r, g, b, a);
        addLine(buf, mat, x2, y1, z1, x2, y1, z2, r, g, b, a);
        addLine(buf, mat, x2, y1, z2, x1, y1, z2, r, g, b, a);
        addLine(buf, mat, x1, y1, z2, x1, y1, z1, r, g, b, a);
        addLine(buf, mat, x1, y2, z1, x2, y2, z1, r, g, b, a);
        addLine(buf, mat, x2, y2, z1, x2, y2, z2, r, g, b, a);
        addLine(buf, mat, x2, y2, z2, x1, y2, z2, r, g, b, a);
        addLine(buf, mat, x1, y2, z2, x1, y2, z1, r, g, b, a);
        addLine(buf, mat, x1, y1, z1, x1, y2, z1, r, g, b, a);
        addLine(buf, mat, x2, y1, z1, x2, y2, z1, r, g, b, a);
        addLine(buf, mat, x2, y1, z2, x2, y2, z2, r, g, b, a);
        addLine(buf, mat, x1, y1, z2, x1, y2, z2, r, g, b, a);
    }

    private void renderBlockBox(VertexConsumer buf, Matrix4f mat, Camera cam, BlockPos bp) {
        double cx = cam.getPos().x;
        double cy = cam.getPos().y;
        double cz = cam.getPos().z;

        float x1 = (float) (bp.getX() - cx);
        float y1 = (float) (bp.getY() - cy);
        float z1 = (float) (bp.getZ() - cz);
        float x2 = x1 + 1f;
        float y2 = y1 + 1f;
        float z2 = z1 + 1f;

        int r = (blockColour >> 16) & 0xFF;
        int g = (blockColour >>  8) & 0xFF;
        int b = (blockColour      ) & 0xFF;
        int a = (blockColour >> 24) & 0xFF;

        addLine(buf, mat, x1, y1, z1, x2, y1, z1, r, g, b, a);
        addLine(buf, mat, x2, y1, z1, x2, y1, z2, r, g, b, a);
        addLine(buf, mat, x2, y1, z2, x1, y1, z2, r, g, b, a);
        addLine(buf, mat, x1, y1, z2, x1, y1, z1, r, g, b, a);
        addLine(buf, mat, x1, y2, z1, x2, y2, z1, r, g, b, a);
        addLine(buf, mat, x2, y2, z1, x2, y2, z2, r, g, b, a);
        addLine(buf, mat, x2, y2, z2, x1, y2, z2, r, g, b, a);
        addLine(buf, mat, x1, y2, z2, x1, y2, z1, r, g, b, a);
        addLine(buf, mat, x1, y1, z1, x1, y2, z1, r, g, b, a);
        addLine(buf, mat, x2, y1, z1, x2, y2, z1, r, g, b, a);
        addLine(buf, mat, x2, y1, z2, x2, y2, z2, r, g, b, a);
        addLine(buf, mat, x1, y1, z2, x1, y2, z2, r, g, b, a);
    }

    private void addLine(VertexConsumer buf, Matrix4f mat,
                         float x1, float y1, float z1,
                         float x2, float y2, float z2,
                         int r, int g, int b, int a) {
        float nx = x2 - x1, ny = y2 - y1, nz = z2 - z1;
        float len = (float) Math.sqrt(nx*nx + ny*ny + nz*nz);
        if (len == 0) return;
        nx /= len; ny /= len; nz /= len;
        buf.vertex(mat, x1, y1, z1).color(r, g, b, a).normal(nx, ny, nz);
        buf.vertex(mat, x2, y2, z2).color(r, g, b, a).normal(nx, ny, nz);
    }

    @Override
    public int renderSettings(DrawContext context, int mouseX, int mouseY,
                              int startX, int startY, int width, SettingsHelper helper) {
        int y    = startY;
        int col1 = startX;
        int col2 = startX + 170;

        helper.renderInfo(context, "── Detection ──", col1, y); y += 14;

        helper.renderLabel(context, "Cobbled DS:", String.valueOf(cobbledThreshold), col1, y);
        helper.renderSlider(context, col1 + 110, y + 4, 100, cobbledThreshold, 1, 15);
        helper.renderLabel(context, "Enable:", null, col2, y);
        helper.renderToggle(context, mouseX, mouseY, col2 + 70, y - 4, detectCobbledDeepslate);
        y += 24;

        helper.renderLabel(context, "Rotated DS:", String.valueOf(rotatedThreshold), col1, y);
        helper.renderSlider(context, col1 + 110, y + 4, 100, rotatedThreshold, 1, 20);
        helper.renderLabel(context, "Enable:", null, col2, y);
        helper.renderToggle(context, mouseX, mouseY, col2 + 70, y - 4, detectRotatedDeepslate);
        y += 24;

        helper.renderLabel(context, "End Stone:", String.valueOf(endStoneThreshold), col1, y);
        helper.renderSlider(context, col1 + 110, y + 4, 100, endStoneThreshold, 1, 15);
        helper.renderLabel(context, "Enable:", null, col2, y);
        helper.renderToggle(context, mouseX, mouseY, col2 + 70, y - 4, detectEndStone);
        y += 24;

        helper.renderLabel(context, "Ignore Exposed:", null, col1, y);
        helper.renderToggle(context, mouseX, mouseY, col1 + 120, y - 4, ignoreExposed);
        y += 28;

        helper.renderInfo(context, "── Render ──", col1, y); y += 14;

        helper.renderLabel(context, "Render Y:", String.format("%.0f", renderY), col1, y);
        helper.renderSlider(context, col1 + 100, y + 4, 120, renderY, -64, 320);
        y += 24;

        helper.renderLabel(context, "Block ESP:", null, col1, y);
        helper.renderToggle(context, mouseX, mouseY, col1 + 80, y - 4, highlightBlocks);
        y += 24;

        helper.renderInfo(context, "Chunks: " + flaggedChunks.size() + "  Blocks: " + suspiciousBlocks.size(), col1, y);
        y += 14;

        return y;
    }

    @Override
    public boolean handleSettingsClick(double mx, double my, int startX, int startY,
                                       int width, SettingsHelper helper) {
        int y    = startY;
        int col1 = startX;
        int col2 = startX + 170;

        y += 14;

        if (helper.isSliderHovered(mx, my, col1 + 110, y + 4, 100)) {
            cobbledThreshold = (int) Math.round(helper.calculateSliderValue(mx, col1 + 110, 100, 1, 15));
            rescan(); save(); return true;
        }
        if (helper.isToggleHovered(mx, my, col2 + 70, y - 4)) {
            detectCobbledDeepslate = !detectCobbledDeepslate; rescan(); save(); return true;
        }
        y += 24;

        if (helper.isSliderHovered(mx, my, col1 + 110, y + 4, 100)) {
            rotatedThreshold = (int) Math.round(helper.calculateSliderValue(mx, col1 + 110, 100, 1, 20));
            rescan(); save(); return true;
        }
        if (helper.isToggleHovered(mx, my, col2 + 70, y - 4)) {
            detectRotatedDeepslate = !detectRotatedDeepslate; rescan(); save(); return true;
        }
        y += 24;

        if (helper.isSliderHovered(mx, my, col1 + 110, y + 4, 100)) {
            endStoneThreshold = (int) Math.round(helper.calculateSliderValue(mx, col1 + 110, 100, 1, 15));
            rescan(); save(); return true;
        }
        if (helper.isToggleHovered(mx, my, col2 + 70, y - 4)) {
            detectEndStone = !detectEndStone; rescan(); save(); return true;
        }
        y += 24;

        if (helper.isToggleHovered(mx, my, col1 + 120, y - 4)) {
            ignoreExposed = !ignoreExposed; rescan(); save(); return true;
        }
        y += 28;

        y += 14;

        if (helper.isSliderHovered(mx, my, col1 + 100, y + 4, 120)) {
            renderY = helper.calculateSliderValue(mx, col1 + 100, 120, -64, 320);
            save(); return true;
        }
        y += 24;

        if (helper.isToggleHovered(mx, my, col1 + 80, y - 4)) {
            highlightBlocks = !highlightBlocks; save(); return true;
        }

        return false;
    }

    private void rescan() {
        scannedChunks.clear();
        flaggedChunks.clear();
        suspiciousBlocks.clear();
        scheduleInitialScan();
    }

    @Override
    public Map<String, Object> saveSettings() {
        Map<String, Object> s = super.saveSettings();
        s.put("detectCobbledDeepslate", detectCobbledDeepslate);
        s.put("detectRotatedDeepslate", detectRotatedDeepslate);
        s.put("detectEndStone",         detectEndStone);
        s.put("ignoreExposed",          ignoreExposed);
        s.put("cobbledThreshold",       cobbledThreshold);
        s.put("rotatedThreshold",       rotatedThreshold);
        s.put("endStoneThreshold",      endStoneThreshold);
        s.put("renderY",                renderY);
        s.put("highlightBlocks",        highlightBlocks);
        s.put("chunkColour",            chunkColour);
        s.put("blockColour",            blockColour);
        return s;
    }

    @Override
    public void loadSettings(Map<String, Object> s) {
        super.loadSettings(s);
        if (s.containsKey("detectCobbledDeepslate")) detectCobbledDeepslate = (Boolean) s.get("detectCobbledDeepslate");
        if (s.containsKey("detectRotatedDeepslate")) detectRotatedDeepslate = (Boolean) s.get("detectRotatedDeepslate");
        if (s.containsKey("detectEndStone"))         detectEndStone         = (Boolean) s.get("detectEndStone");
        if (s.containsKey("ignoreExposed"))          ignoreExposed          = (Boolean) s.get("ignoreExposed");
        if (s.containsKey("cobbledThreshold"))       cobbledThreshold       = ((Number) s.get("cobbledThreshold")).intValue();
        if (s.containsKey("rotatedThreshold"))       rotatedThreshold       = ((Number) s.get("rotatedThreshold")).intValue();
        if (s.containsKey("endStoneThreshold"))      endStoneThreshold      = ((Number) s.get("endStoneThreshold")).intValue();
        if (s.containsKey("renderY"))                renderY                = ((Number) s.get("renderY")).doubleValue();
        if (s.containsKey("highlightBlocks"))        highlightBlocks        = (Boolean) s.get("highlightBlocks");
        if (s.containsKey("chunkColour"))            chunkColour            = ((Number) s.get("chunkColour")).intValue();
        if (s.containsKey("blockColour"))            blockColour            = ((Number) s.get("blockColour")).intValue();
    }

    private void save() { Config.getInstance().saveModules(); }

    private enum SuspiciousBlockType {
        COBBLED_DEEPSLATE,
        ROTATED_DEEPSLATE,
        END_STONE
    }
}