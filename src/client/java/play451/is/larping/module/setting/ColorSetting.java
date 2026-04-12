package play451.is.larping.module.setting;

public class ColorSetting extends Setting<int[]> {

    public ColorSetting(String name, String description, int r, int g, int b, int a) {
        super(name, name, description, new int[]{ clamp(r), clamp(g), clamp(b), clamp(a) });
    }

    private static int clamp(int v) { return Math.max(0, Math.min(255, v)); }

    public int getR() { return value[0]; }
    public int getG() { return value[1]; }
    public int getB() { return value[2]; }
    public int getA() { return value[3]; }

    public void setR(int v) { value[0] = clamp(v); }
    public void setG(int v) { value[1] = clamp(v); }
    public void setB(int v) { value[2] = clamp(v); }
    public void setA(int v) { value[3] = clamp(v); }

    public int getPacked() {
        return (getA() << 24) | (getR() << 16) | (getG() << 8) | getB();
    }
}