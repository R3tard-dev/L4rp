package play451.is.larping.features.modules.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import play451.is.larping.config.Config;
import play451.is.larping.features.modules.Module;
import play451.is.larping.features.modules.ModuleCategory;
import play451.is.larping.features.modules.ModuleSettingsRenderer;
import play451.is.larping.features.modules.SettingsHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ESP extends Module implements ModuleSettingsRenderer {

    
    private boolean showBox       = true;   
    private boolean showName      = true;   
    private boolean showHealth    = true;   
    private boolean showDistance  = false;  
    private boolean showArmour    = false;  
    private boolean showTracers   = false;  
    private boolean showSkeleton  = false;  

    
    private String colourMode     = "Static";   
    private boolean rainbow       = false;       

    
    private int boxColour         = 0xFF1E90FF;  
    private int nameColour        = 0xFFF0F0F0;  
    private int tracerColour      = 0xFF1E90FF;

    
    private String boxStyle       = "Full";      
    private int boxLineWidth      = 1;           
    private boolean boxFill       = false;       
    private int boxFillOpacity    = 25;          

    
    private String healthBarSide  = "Left";      
    private boolean healthNumbers = false;       

    
    private double maxRange       = 64.0;
    private boolean friendsOnly   = false;
    private boolean includeTeam   = true;

    private final MinecraftClient mc = MinecraftClient.getInstance();

    public ESP() {
        super("ESP", "Draws player outlines and info through walls", ModuleCategory.RENDER);
    }

    
    
    
    public static void renderAll(DrawContext context, float tickDelta) {
        ESP instance = getInstance();
        if (instance == null || !instance.isEnabled()) return;
        instance.render(context, tickDelta);
    }

    private static ESP getInstance() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.world == null) return null;
        
        for (var m : play451.is.larping.features.modules.ModuleManager.getInstance().getModules()) {
            if (m instanceof ESP esp) return esp;
        }
        return null;
    }

    private void render(DrawContext context, float tickDelta) {
        if (mc.player == null || mc.world == null || mc.getWindow() == null) return;

        List<? extends PlayerEntity> players = mc.world.getPlayers();
        int screenW = mc.getWindow().getScaledWidth();
        int screenH = mc.getWindow().getScaledHeight();

        for (PlayerEntity target : players) {
            if (target == mc.player) continue;
            if (!target.isAlive() || target.isSpectator()) continue;

            double dist = mc.player.distanceTo(target);
            if (dist > maxRange) continue;

            
            Box bb = target.getBoundingBox();
            int[] screen = projectBox(bb, tickDelta, screenW, screenH);
            if (screen == null) continue;

            int sx = screen[0]; 
            int sy = screen[1]; 
            int sw = screen[2]; 
            int sh = screen[3]; 

            
            if (sx + sw < 0 || sy + sh < 0 || sx > screenW || sy > screenH) continue;

            
            int colour = resolveColour(target, dist);

            
            if (showBox) {
                if (boxFill) {
                    int alpha = (int) (boxFillOpacity / 100.0 * 255);
                    int fill  = (alpha << 24) | (colour & 0x00FFFFFF);
                    context.fill(sx, sy, sx + sw, sy + sh, fill);
                }
                int lw = Math.max(1, Math.min(3, boxLineWidth));
                if (boxStyle.equals("Corner")) {
                    int cLen = Math.max(4, sw / 4);
                    drawCornerBox(context, sx, sy, sw, sh, lw, colour);
                } else {
                    drawOutline(context, sx, sy, sw, sh, lw, colour);
                }
            }

            
            if (showHealth) {
                float hp    = target.getHealth();
                float maxHp = target.getMaxHealth();
                drawHealthBar(context, sx, sy, sw, sh, hp, maxHp);
            }

            
            if (showName) {
                String name = target.getName().getString();
                int tw = mc.textRenderer.getWidth(name);
                int tx = sx + (sw - tw) / 2;
                int ty = sy - 10;
                
                context.fill(tx - 1, ty - 1, tx + tw + 1, ty + mc.textRenderer.fontHeight + 1, 0x55000000);
                context.drawText(mc.textRenderer, name, tx, ty, nameColour, false);
            }

            
            if (showDistance) {
                String distText = String.format("%.0fm", dist);
                int tw = mc.textRenderer.getWidth(distText);
                context.drawText(mc.textRenderer, distText,
                    sx + (sw - tw) / 2, sy + sh + 2, 0xFFAAAAAA, false);
            }

            
            if (showTracers) {
                drawTracer(context, sx + sw / 2, sy + sh, screenW / 2, screenH, tracerColour);
            }

            
            if (showSkeleton) {
                drawSkeleton(context, target, tickDelta, screenW, screenH);
            }
        }
    }

    

    private void drawOutline(DrawContext ctx, int x, int y, int w, int h, int lw, int col) {
        ctx.fill(x,        y,        x + w,      y + lw,    col); 
        ctx.fill(x,        y + h - lw, x + w,    y + h,     col); 
        ctx.fill(x,        y,        x + lw,     y + h,     col); 
        ctx.fill(x + w - lw, y,      x + w,      y + h,     col); 
    }

    private void drawCornerBox(DrawContext ctx, int x, int y, int w, int h, int lw, int col) {
        int cl = Math.max(4, w / 4);
        int ch = Math.max(4, h / 4);
        
        ctx.fill(x, y, x + cl, y + lw, col);
        ctx.fill(x, y, x + lw, y + ch, col);
        
        ctx.fill(x + w - cl, y, x + w, y + lw, col);
        ctx.fill(x + w - lw, y, x + w, y + ch, col);
        
        ctx.fill(x, y + h - lw, x + cl, y + h, col);
        ctx.fill(x, y + h - ch, x + lw, y + h, col);
        
        ctx.fill(x + w - cl, y + h - lw, x + w, y + h, col);
        ctx.fill(x + w - lw, y + h - ch, x + w, y + h, col);
    }

    private void drawHealthBar(DrawContext ctx, int bx, int by, int bw, int bh,
                               float hp, float maxHp) {
        float ratio = MathHelper.clamp(hp / maxHp, 0f, 1f);
        int barThick = 3;
        int padding  = 2;

        
        int r = (int) (ratio < 0.5f ? 255 : (1f - ratio) * 2 * 255);
        int g = (int) (ratio < 0.5f ? ratio * 2 * 255 : 255);
        int barCol = 0xFF000000 | (r << 16) | (g << 8);

        int bx2, by2, barLen;
        switch (healthBarSide) {
            case "Right" -> {
                bx2 = bx + bw + padding;
                by2 = by;
                barLen = (int) (bh * ratio);
                ctx.fill(bx2, by2, bx2 + barThick, by2 + bh, 0xFF1A1A1A);       
                ctx.fill(bx2, by2 + bh - barLen, bx2 + barThick, by2 + bh, barCol); 
            }
            case "Top" -> {
                bx2 = bx;
                by2 = by - barThick - padding;
                barLen = (int) (bw * ratio);
                ctx.fill(bx2, by2, bx2 + bw, by2 + barThick, 0xFF1A1A1A);
                ctx.fill(bx2, by2, bx2 + barLen, by2 + barThick, barCol);
            }
            case "Bottom" -> {
                bx2 = bx;
                by2 = by + bh + padding;
                barLen = (int) (bw * ratio);
                ctx.fill(bx2, by2, bx2 + bw, by2 + barThick, 0xFF1A1A1A);
                ctx.fill(bx2, by2, bx2 + barLen, by2 + barThick, barCol);
            }
            default -> { 
                bx2 = bx - barThick - padding;
                by2 = by;
                barLen = (int) (bh * ratio);
                ctx.fill(bx2, by2, bx2 + barThick, by2 + bh, 0xFF1A1A1A);
                ctx.fill(bx2, by2 + bh - barLen, bx2 + barThick, by2 + bh, barCol);
            }
        }

        if (healthNumbers) {
            String hpText = String.format("%.0f", hp);
            ctx.drawText(mc.textRenderer, hpText,
                bx2 + barThick + 2, by2 + bh / 2 - 4, barCol, false);
        }
    }

    private void drawTracer(DrawContext ctx, int tx, int ty, int cx, int cy, int col) {
        
        int dx = Math.abs(tx - cx), sx = cx < tx ? 1 : -1;
        int dy = -Math.abs(ty - cy), sy = cy < ty ? 1 : -1;
        int err = dx + dy;
        int x = cx, y = cy;
        int steps = 0;
        while (steps++ < 2000) {
            ctx.fill(x, y, x + 1, y + 1, col);
            if (x == tx && y == ty) break;
            int e2 = 2 * err;
            if (e2 >= dy) { err += dy; x += sx; }
            if (e2 <= dx) { err += dx; y += sy; }
        }
    }

    private void drawSkeleton(DrawContext ctx, PlayerEntity target, float tickDelta,
                              int screenW, int screenH) {
        
        Vec3d pos = target.getLerpedPos(tickDelta);
        double h   = target.getHeight();
        double w2  = target.getWidth() / 2.0;

        
        Vec3d head  = pos.add(0, h * 0.9,  0);
        Vec3d neck  = pos.add(0, h * 0.78, 0);
        Vec3d chest = pos.add(0, h * 0.6,  0);
        Vec3d waist = pos.add(0, h * 0.42, 0);
        Vec3d lSh   = pos.add(-w2 * 1.2, h * 0.72, 0);
        Vec3d rSh   = pos.add( w2 * 1.2, h * 0.72, 0);
        Vec3d lEl   = pos.add(-w2 * 1.6, h * 0.48, 0);
        Vec3d rEl   = pos.add( w2 * 1.6, h * 0.48, 0);
        Vec3d lHp   = pos.add(-w2 * 0.6, h * 0.36, 0);
        Vec3d rHp   = pos.add( w2 * 0.6, h * 0.36, 0);
        Vec3d lKn   = pos.add(-w2 * 0.7, h * 0.2,  0);
        Vec3d rKn   = pos.add( w2 * 0.7, h * 0.2,  0);
        Vec3d lFt   = pos.add(-w2 * 0.5, 0,        0);
        Vec3d rFt   = pos.add( w2 * 0.5, 0,        0);

        int col = boxColour;
        drawBone(ctx, head,  neck,  tickDelta, screenW, screenH, col);
        drawBone(ctx, neck,  chest, tickDelta, screenW, screenH, col);
        drawBone(ctx, chest, waist, tickDelta, screenW, screenH, col);
        drawBone(ctx, chest, lSh,   tickDelta, screenW, screenH, col);
        drawBone(ctx, chest, rSh,   tickDelta, screenW, screenH, col);
        drawBone(ctx, lSh,   lEl,   tickDelta, screenW, screenH, col);
        drawBone(ctx, rSh,   rEl,   tickDelta, screenW, screenH, col);
        drawBone(ctx, waist, lHp,   tickDelta, screenW, screenH, col);
        drawBone(ctx, waist, rHp,   tickDelta, screenW, screenH, col);
        drawBone(ctx, lHp,   lKn,   tickDelta, screenW, screenH, col);
        drawBone(ctx, rHp,   rKn,   tickDelta, screenW, screenH, col);
        drawBone(ctx, lKn,   lFt,   tickDelta, screenW, screenH, col);
        drawBone(ctx, rKn,   rFt,   tickDelta, screenW, screenH, col);
    }

    private void drawBone(DrawContext ctx, Vec3d a, Vec3d b, float tickDelta,
                          int screenW, int screenH, int col) {
        int[] pa = worldToScreen(a, screenW, screenH);
        int[] pb = worldToScreen(b, screenW, screenH);
        if (pa == null || pb == null) return;
        drawTracer(ctx, pa[0], pa[1], pb[0], pb[1], col);
    }

    

    private int[] projectBox(Box bb, float tickDelta, int screenW, int screenH) {
        Vec3d[] corners = {
            new Vec3d(bb.minX, bb.minY, bb.minZ),
            new Vec3d(bb.maxX, bb.minY, bb.minZ),
            new Vec3d(bb.minX, bb.maxY, bb.minZ),
            new Vec3d(bb.maxX, bb.maxY, bb.minZ),
            new Vec3d(bb.minX, bb.minY, bb.maxZ),
            new Vec3d(bb.maxX, bb.minY, bb.maxZ),
            new Vec3d(bb.minX, bb.maxY, bb.maxZ),
            new Vec3d(bb.maxX, bb.maxY, bb.maxZ),
        };

        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;
        int visible = 0;

        for (Vec3d corner : corners) {
            int[] sc = worldToScreen(corner, screenW, screenH);
            if (sc == null) continue;
            visible++;
            minX = Math.min(minX, sc[0]);
            minY = Math.min(minY, sc[1]);
            maxX = Math.max(maxX, sc[0]);
            maxY = Math.max(maxY, sc[1]);
        }

        if (visible == 0) return null;
        return new int[]{minX, minY, maxX - minX, maxY - minY};
    }

    private int[] worldToScreen(Vec3d worldPos, int screenW, int screenH) {
        if (mc.gameRenderer == null || mc.player == null) return null;
        try {
            net.minecraft.client.render.Camera cam = mc.gameRenderer.getCamera();
            Vec3d camPos = cam.getPos();
            Vec3d delta  = worldPos.subtract(camPos);

            float pitch = (float) Math.toRadians(cam.getPitch());
            float yaw   = (float) Math.toRadians(cam.getYaw());

            
            double cosYaw   = Math.cos(yaw);
            double sinYaw   = Math.sin(yaw);
            double cosPitch = Math.cos(pitch);
            double sinPitch = Math.sin(pitch);

            double cx = delta.x * cosYaw  - delta.z * sinYaw;
            double cy = delta.x * sinYaw * sinPitch + delta.y * cosPitch - delta.z * cosYaw * sinPitch;
            double cz = delta.x * sinYaw * cosPitch - delta.y * sinPitch + delta.z * cosYaw * cosPitch;

            if (cz <= 0.1) return null; 

            
            double fovRad = Math.toRadians(mc.options.getFov().getValue());
            double f      = (screenH / 2.0) / Math.tan(fovRad / 2.0);

            int sx = (int) (screenW / 2.0 + cx / cz * f);
            int sy = (int) (screenH / 2.0 - cy / cz * f);

            return new int[]{sx, sy};
        } catch (Exception e) {
            return null;
        }
    }

    

    private int resolveColour(PlayerEntity target, double dist) {
        return switch (colourMode) {
            case "Health" -> healthColour(target.getHealth(), target.getMaxHealth());
            case "Rainbow" -> rainbowColour();
            case "Distance" -> distanceColour(dist, maxRange);
            default -> boxColour; 
        };
    }

    private int healthColour(float hp, float maxHp) {
        float r = MathHelper.clamp(hp / maxHp, 0f, 1f);
        int red   = (int) (r < 0.5f ? 255 : (1f - r) * 2 * 255);
        int green = (int) (r < 0.5f ? r * 2 * 255 : 255);
        return 0xFF000000 | (red << 16) | (green << 8);
    }

    private int rainbowColour() {
        long t   = System.currentTimeMillis();
        float hue = ((t % 3000) / 3000.0f);
        return 0xFF000000 | net.minecraft.util.math.ColorHelper.fromFloats(1f,
            (float) hsvToRgb(hue, 1f, 1f)[0],
            (float) hsvToRgb(hue, 1f, 1f)[1],
            (float) hsvToRgb(hue, 1f, 1f)[2]);
    }

    private int distanceColour(double dist, double maxDist) {
        float r  = (float) MathHelper.clamp(dist / maxDist, 0.0, 1.0);
        int red  = (int) (r * 255);
        int blue = (int) ((1f - r) * 255);
        return 0xFF000000 | (red << 16) | blue;
    }

    private double[] hsvToRgb(float h, float s, float v) {
        int i   = (int) (h * 6);
        float f = h * 6 - i;
        float p = v * (1 - s);
        float q = v * (1 - f * s);
        float t = v * (1 - (1 - f) * s);
        return switch (i % 6) {
            case 0 -> new double[]{v, t, p};
            case 1 -> new double[]{q, v, p};
            case 2 -> new double[]{p, v, t};
            case 3 -> new double[]{p, q, v};
            case 4 -> new double[]{t, p, v};
            default -> new double[]{v, p, q};
        };
    }

    

    @Override
    public Map<String, Object> saveSettings() {
        Map<String, Object> s = super.saveSettings();
        s.put("showBox",       showBox);
        s.put("showName",      showName);
        s.put("showHealth",    showHealth);
        s.put("showDistance",  showDistance);
        s.put("showArmour",    showArmour);
        s.put("showTracers",   showTracers);
        s.put("showSkeleton",  showSkeleton);
        s.put("colourMode",    colourMode);
        s.put("boxColour",     boxColour);
        s.put("nameColour",    nameColour);
        s.put("tracerColour",  tracerColour);
        s.put("boxStyle",      boxStyle);
        s.put("boxLineWidth",  boxLineWidth);
        s.put("boxFill",       boxFill);
        s.put("boxFillOpacity",boxFillOpacity);
        s.put("healthBarSide", healthBarSide);
        s.put("healthNumbers", healthNumbers);
        s.put("maxRange",      maxRange);
        return s;
    }

    @Override
    public void loadSettings(Map<String, Object> s) {
        super.loadSettings(s);
        if (s.containsKey("showBox"))       showBox       = (Boolean) s.get("showBox");
        if (s.containsKey("showName"))      showName      = (Boolean) s.get("showName");
        if (s.containsKey("showHealth"))    showHealth    = (Boolean) s.get("showHealth");
        if (s.containsKey("showDistance"))  showDistance  = (Boolean) s.get("showDistance");
        if (s.containsKey("showArmour"))    showArmour    = (Boolean) s.get("showArmour");
        if (s.containsKey("showTracers"))   showTracers   = (Boolean) s.get("showTracers");
        if (s.containsKey("showSkeleton"))  showSkeleton  = (Boolean) s.get("showSkeleton");
        if (s.containsKey("colourMode"))    colourMode    = (String)  s.get("colourMode");
        if (s.containsKey("boxColour"))     boxColour     = ((Number) s.get("boxColour")).intValue();
        if (s.containsKey("nameColour"))    nameColour    = ((Number) s.get("nameColour")).intValue();
        if (s.containsKey("tracerColour"))  tracerColour  = ((Number) s.get("tracerColour")).intValue();
        if (s.containsKey("boxStyle"))      boxStyle      = (String)  s.get("boxStyle");
        if (s.containsKey("boxLineWidth"))  boxLineWidth  = ((Number) s.get("boxLineWidth")).intValue();
        if (s.containsKey("boxFill"))       boxFill       = (Boolean) s.get("boxFill");
        if (s.containsKey("boxFillOpacity"))boxFillOpacity= ((Number) s.get("boxFillOpacity")).intValue();
        if (s.containsKey("healthBarSide")) healthBarSide = (String)  s.get("healthBarSide");
        if (s.containsKey("healthNumbers")) healthNumbers = (Boolean) s.get("healthNumbers");
        if (s.containsKey("maxRange"))      maxRange      = ((Number) s.get("maxRange")).doubleValue();
    }

    

    @Override
    public int renderSettings(DrawContext context, int mouseX, int mouseY,
                              int startX, int startY, int width, SettingsHelper helper) {
        int y = startY;
        int col1 = startX;
        int col2 = startX + 170;

        
        helper.renderInfo(context, "── Features ──", col1, y); y += 14;

        helper.renderLabel(context, "Box:",       null, col1, y);
        helper.renderToggle(context, mouseX, mouseY, col1 + 80, y - 4, showBox);
        helper.renderLabel(context, "Name:",      null, col2, y);
        helper.renderToggle(context, mouseX, mouseY, col2 + 80, y - 4, showName);
        y += 24;

        helper.renderLabel(context, "Health:",    null, col1, y);
        helper.renderToggle(context, mouseX, mouseY, col1 + 80, y - 4, showHealth);
        helper.renderLabel(context, "Distance:",  null, col2, y);
        helper.renderToggle(context, mouseX, mouseY, col2 + 80, y - 4, showDistance);
        y += 24;

        helper.renderLabel(context, "Tracers:",   null, col1, y);
        helper.renderToggle(context, mouseX, mouseY, col1 + 80, y - 4, showTracers);
        helper.renderLabel(context, "Skeleton:",  null, col2, y);
        helper.renderToggle(context, mouseX, mouseY, col2 + 80, y - 4, showSkeleton);
        y += 28;

        
        helper.renderInfo(context, "── Box ──", col1, y); y += 14;

        helper.renderLabel(context, "Style:", null, col1, y);
        helper.renderModeSelector(context, mouseX, mouseY, col1 + 60, y,
            new String[]{"Full", "Corner"}, boxStyle, 45);
        y += 24;

        helper.renderLabel(context, "Line Width:", String.valueOf(boxLineWidth), col1, y);
        helper.renderSlider(context, col1 + 100, y + 4, 120, boxLineWidth, 1, 3);
        y += 24;

        helper.renderLabel(context, "Fill:",      null, col1, y);
        helper.renderToggle(context, mouseX, mouseY, col1 + 80, y - 4, boxFill);
        if (boxFill) {
            helper.renderLabel(context, "Opacity:", String.valueOf(boxFillOpacity) + "%", col2, y);
            helper.renderSlider(context, col2 + 80, y + 4, 80, boxFillOpacity, 0, 100);
        }
        y += 28;

        
        helper.renderInfo(context, "── Colour Mode ──", col1, y); y += 14;

        helper.renderModeSelector(context, mouseX, mouseY, col1, y,
            new String[]{"Static", "Health", "Rainbow", "Distance"}, colourMode, 50);
        y += 28;

        
        if (showHealth) {
            helper.renderInfo(context, "── Health Bar ──", col1, y); y += 14;

            helper.renderLabel(context, "Side:", null, col1, y);
            helper.renderModeSelector(context, mouseX, mouseY, col1 + 50, y,
                new String[]{"Left", "Right", "Top", "Bottom"}, healthBarSide, 38);
            y += 24;

            helper.renderLabel(context, "Show HP#:", null, col1, y);
            helper.renderToggle(context, mouseX, mouseY, col1 + 80, y - 4, healthNumbers);
            y += 28;
        }

        
        helper.renderInfo(context, "── Misc ──", col1, y); y += 14;

        helper.renderLabel(context, "Max Range:", String.format("%.0fm", maxRange), col1, y);
        helper.renderSlider(context, col1 + 110, y + 4, 120, maxRange, 8, 128);
        y += 24;

        return y;
    }

    @Override
    public boolean handleSettingsClick(double mx, double my, int startX, int startY,
                                       int width, SettingsHelper helper) {
        int y = startY;
        int col1 = startX;
        int col2 = startX + 170;

        
        y += 14;
        if (helper.isToggleHovered(mx, my, col1 + 80, y - 4)) { showBox      = !showBox;      save(); return true; }
        if (helper.isToggleHovered(mx, my, col2 + 80, y - 4)) { showName     = !showName;     save(); return true; }
        y += 24;
        if (helper.isToggleHovered(mx, my, col1 + 80, y - 4)) { showHealth   = !showHealth;   save(); return true; }
        if (helper.isToggleHovered(mx, my, col2 + 80, y - 4)) { showDistance = !showDistance; save(); return true; }
        y += 24;
        if (helper.isToggleHovered(mx, my, col1 + 80, y - 4)) { showTracers  = !showTracers;  save(); return true; }
        if (helper.isToggleHovered(mx, my, col2 + 80, y - 4)) { showSkeleton = !showSkeleton; save(); return true; }
        y += 28;

        
        y += 14;
        int modeX = col1 + 60;
        for (String opt : new String[]{"Full", "Corner"}) {
            if (helper.isModeButtonHovered(mx, my, modeX, y, 45)) { boxStyle = opt; save(); return true; }
            modeX += 48;
        }
        y += 24;
        if (helper.isSliderHovered(mx, my, col1 + 100, y + 4, 120)) {
            boxLineWidth = (int) Math.round(helper.calculateSliderValue(mx, col1 + 100, 120, 1, 3));
            save(); return true;
        }
        y += 24;
        if (helper.isToggleHovered(mx, my, col1 + 80, y - 4)) { boxFill = !boxFill; save(); return true; }
        if (boxFill && helper.isSliderHovered(mx, my, col2 + 80, y + 4, 80)) {
            boxFillOpacity = (int) helper.calculateSliderValue(mx, col2 + 80, 80, 0, 100);
            save(); return true;
        }
        y += 28;

        
        y += 14;
        int cmX = col1;
        for (String opt : new String[]{"Static", "Health", "Rainbow", "Distance"}) {
            if (helper.isModeButtonHovered(mx, my, cmX, y, 50)) { colourMode = opt; save(); return true; }
            cmX += 53;
        }
        y += 28;

        
        if (showHealth) {
            y += 14;
            int hbX = col1 + 50;
            for (String opt : new String[]{"Left", "Right", "Top", "Bottom"}) {
                if (helper.isModeButtonHovered(mx, my, hbX, y, 38)) { healthBarSide = opt; save(); return true; }
                hbX += 41;
            }
            y += 24;
            if (helper.isToggleHovered(mx, my, col1 + 80, y - 4)) { healthNumbers = !healthNumbers; save(); return true; }
            y += 28;
        }

        
        y += 14;
        if (helper.isSliderHovered(mx, my, col1 + 110, y + 4, 120)) {
            maxRange = helper.calculateSliderValue(mx, col1 + 110, 120, 8, 128);
            save(); return true;
        }

        return false;
    }

    private void save() {
        Config.getInstance().saveModules();
    }
}