package play451.is.larping.module.setting;

public class ColorSetting extends Setting<int[]> {

    public ColorSetting(String name, int r, int g, int b, int a) {
        super(name, new int[]{
            Math.max(0, Math.min(255, r)),
            Math.max(0, Math.min(255, g)),
            Math.max(0, Math.min(255, b)),
            Math.max(0, Math.min(255, a))
        });
    }

    public int getR() { return value[0]; }
    public int getG() { return value[1]; }
    public int getB() { return value[2]; }
    public int getA() { return value[3]; }

    public void setR(int v) { value[0] = Math.max(0, Math.min(255, v)); }
    public void setG(int v) { value[1] = Math.max(0, Math.min(255, v)); }
    public void setB(int v) { value[2] = Math.max(0, Math.min(255, v)); }
    public void setA(int v) { value[3] = Math.max(0, Math.min(255, v)); }

    public int getPacked() {
        return (getA() << 24) | (getR() << 16) | (getG() << 8) | getB();
    }
}