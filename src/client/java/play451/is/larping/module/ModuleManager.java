package play451.is.larping.module;

import java.util.ArrayList;
import java.util.List;

public class ModuleManager {
    private static final List<Module> modules = new ArrayList<>();

    public static void init() {
    }

    public static List<Module> getModulesForCategory(Category category) {
        List<Module> result = new ArrayList<>();
        for (Module m : modules) {
            if (m.getCategory() == category) result.add(m);
        }
        return result;
    }

    public static List<Module> getModules() {
        return modules;
    }
}