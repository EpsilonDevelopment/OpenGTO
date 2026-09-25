import java.io.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;

/**
 * SealProbe3 —— 手工注入 GTOProvider 的运行时状态。
 *
 * 前两代的结论：
 *   - 依赖问题已解决（Forge + slf4j 都在 classpath 上）
 *   - decrypt 已能运行；init_gtolib 卡在 NullPointerException
 *   - 两个 ModuleDataProvider 字段始终为 null
 *
 * 本代假设：这两个字段由 GTOProvider.<clinit> 之外的路径填充
 * （很可能是 GTOServices.completeScan(IModuleLayerManager) 拿到模块层里的 SecureJar）。
 * 因此这里**手工构造 SecureJar 并反射写入**，看 init_gtolib 能否继续。
 */
public class SealProbe3 {

    static final String GTOLIB_JAR =
        "<workspace>/GTOCore-Main/libs/gtolib-protected.jar";

    static void hr(String s) {
        System.out.println();
        System.out.println("==================== " + s + " ====================");
    }

    static String uplus(String s) {
        StringBuilder sb = new StringBuilder();
        s.codePoints().forEach(c -> sb.append(String.format("U+%04X ", c)));
        return sb.toString().trim();
    }

    static void showFields(Class<?> gp, String tag) {
        System.out.println("  --- GTOProvider 静态字段 [" + tag + "] ---");
        for (Field f : gp.getDeclaredFields()) {
            try {
                f.setAccessible(true);
                System.out.printf("    %-56s %s%n",
                        f.getType().getSimpleName() + " <" + uplus(f.getName()) + ">", f.get(null));
            } catch (Throwable t) {
                System.out.println("    读取失败: " + t);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        hr("1. native0.Loader");
        Class.forName("native0.Loader");
        System.out.println("  OK");

        hr("2. 槽位 0 / 3");
        Class.forName("gto.native0.GTOServicesInit");
        Class<?> gts = Class.forName("gto.native0.plugins.GTOServices");
        System.out.println("  GTOServicesInit OK, GTOServices OK");
        Method init0 = gts.getDeclaredMethod("initialize");
        init0.setAccessible(true);
        System.out.println("  GTOServices.initialize() -> " + init0.invoke(null));

        hr("3. 槽位 2: GTOProvider");
        Class<?> gp = Class.forName("gto.native0.plugins.GTOProvider");
        showFields(gp, "加载后");

        hr("4. gtolib-protected.jar 的 Manifest");
        try (java.util.jar.JarFile jf = new java.util.jar.JarFile(GTOLIB_JAR)) {
            java.util.jar.Manifest mf = jf.getManifest();
            if (mf != null) {
                for (Map.Entry<Object, Object> e : mf.getMainAttributes().entrySet()) {
                    System.out.println("    " + e.getKey() + " = " + e.getValue());
                }
            } else {
                System.out.println("    (无 Manifest)");
            }
        }

        hr("5. 构造 SecureJar 并注入 ModuleDataProvider 字段");
        Class<?> secureJarCls = Class.forName("cpw.mods.jarhandling.SecureJar");
        Object sj = null;
        try {
            Method from = secureJarCls.getMethod("from", Path[].class);
            sj = from.invoke(null, (Object) new Path[]{ Paths.get(GTOLIB_JAR) });
            System.out.println("    SecureJar.from -> " + sj);
            Method mdp = secureJarCls.getMethod("moduleDataProvider");
            Object provider = mdp.invoke(sj);
            System.out.println("    moduleDataProvider() -> " + provider);
            System.out.println("    实现类: " + (provider == null ? "null" : provider.getClass().getName()));

            // 打印 provider 的可用方法
            if (provider != null) {
                for (Method mm : provider.getClass().getDeclaredMethods()) {
                    if (!mm.isSynthetic()) System.out.println("        " + mm);
                }
                // 试读一个类，看是明文还是密文
                Method open = provider.getClass().getMethod("open", String.class);
                open.setAccessible(true);
                Object opt = open.invoke(provider, "com/gtolib/Client.class");
                System.out.println("        open(com/gtolib/Client.class) -> " + describe(opt));
            }

            // 反射写入两个 ModuleDataProvider 字段
            for (Field f : gp.getDeclaredFields()) {
                if (!f.getType().getName().endsWith("ModuleDataProvider")) continue;
                f.setAccessible(true);
                f.set(null, provider);
                System.out.println("    已写入 <" + uplus(f.getName()) + ">");
            }
        } catch (Throwable t) {
            Throwable c = (t instanceof InvocationTargetException) ? t.getCause() : t;
            System.out.println("    失败: " + c);
            c.printStackTrace(System.out);
        }

        showFields(gp, "注入 ModuleDataProvider 后");

        hr("6. 再调 init_gtolib(Type)");
        Class<?> asmType = Class.forName("org.objectweb.asm.Type");
        Method getObjectType = asmType.getMethod("getObjectType", String.class);
        Method initGtolib = gp.getDeclaredMethod("init_gtolib", asmType);
        initGtolib.setAccessible(true);
        for (String cls : new String[]{
                "com/gtolib/Client",
                "com/gtolib/api/recipe/RecipeBuilder",
                "com/gtolib/ae2/crafting/models/DependencyGraph"}) {
            System.out.println("  init_gtolib(" + cls + ")");
            try {
                System.out.println("      返回: " + initGtolib.invoke(null, getObjectType.invoke(null, cls)));
            } catch (Throwable t) {
                Throwable c = (t instanceof InvocationTargetException) ? t.getCause() : t;
                System.out.println("      异常: " + c);
                for (StackTraceElement st : c.getStackTrace()) System.out.println("         at " + st);
            }
        }
        showFields(gp, "init_gtolib 之后");

        hr("完成");
    }

    static String describe(Object opt) throws Exception {
        if (!(opt instanceof Optional)) return String.valueOf(opt);
        Optional<?> o = (Optional<?>) opt;
        if (o.isEmpty()) return "空";
        Object in = o.get();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        if (in instanceof InputStream is) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = is.read(buf)) != -1) bos.write(buf, 0, n);
            is.close();
        }
        byte[] b = bos.toByteArray();
        return b.length + " B  前8=" + hex(b, 8);
    }

    static String hex(byte[] b, int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(n, b.length); i++) sb.append(String.format("%02x", b[i]));
        return sb.toString();
    }
}
