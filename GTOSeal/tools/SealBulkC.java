import java.io.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import java.util.zip.*;

/**
 * SealBulk —— 离线批量解密 gtolib-protected.jar 里的全部类。
 *
 * 核心发现（SealProbe7）：
 *   **先调 GTOProvider.init_gtolib(Type)，再 Class.forName** ——
 *   类会以「完整实现」的形态被加载（native=0）。
 *   反过来（先加载）就不行，因为类一旦定义就无法替换。
 *
 * 推测机制：原生库在 JVM 内装了 JVMTI 的 ClassFileLoadHook，
 *   init_gtolib 把类名登记进「待解密集合」，类被定义时回调替换字节码。
 *
 * 因此本程序：
 *   1. 初始化 Seal 运行时（Loader → 槽位0 → 槽位3 → 槽位2 → 注入 ModuleDataProvider）
 *   2. 遍历 gtolib-protected.jar 里的全部 .class
 *   3. 逐个 init_gtolib(Type) → Class.forName(name, false, loader)
 *   4. 统计结果，然后保持存活（--wait）以便外部 JVMTI 工具附加 dump
 */
public class SealBulkC {

    static String GTOLIB_JAR = System.getProperty("seal.jar",
        "<workspace>/_seal-close-gap/gtolib-C-unsealed.jar");
    static long SALT = Long.getLong("seal.salt", -6851021872117151357L);
    static String PREFIX = System.getProperty("seal.prefix", "com/gtolib/");

    static void hr(String s) { System.out.println("\n===== " + s + " ====="); }

    public static void main(String[] args) throws Exception {
        boolean wait = args.length > 0 && args[0].equals("--wait");
        PrintStream out = System.out;

        hr("1. 初始化 Seal 运行时");
        Class.forName("native0.Loader");
        Class.forName("gto.native0.GTOServicesInit");
        Class<?> gts = Class.forName("gto.native0.plugins.GTOServices");
        Method i0 = gts.getDeclaredMethod("initialize");
        i0.setAccessible(true);
        i0.invoke(null);
        Class<?> gp = Class.forName("gto.native0.plugins.GTOProvider");

        hr("2. 注入 gtolib-protected.jar 的 ModuleDataProvider + salt");
        Class<?> sjc = Class.forName("cpw.mods.jarhandling.SecureJar");
        Object sj = sjc.getMethod("from", Path[].class)
                .invoke(null, (Object) new Path[]{ Paths.get(GTOLIB_JAR) });
        Object prov = sjc.getMethod("moduleDataProvider").invoke(sj);
        for (Field f : gp.getDeclaredFields()) {
            f.setAccessible(true);
            if (f.getType().getName().endsWith("ModuleDataProvider")) f.set(null, prov);
            else if (f.getType() == long.class) f.set(null, SALT);
            else if (f.getType() == int.class) f.set(null, 0);
        }
        out.println("  注入完成");

        Class<?> typeCls = Class.forName("org.objectweb.asm.Type");
        Method getObjectType = typeCls.getMethod("getObjectType", String.class);
        Method initGtolib = gp.getDeclaredMethod("init_gtolib", typeCls);
        initGtolib.setAccessible(true);

        hr("3. 枚举 gtolib-protected.jar 内的类");
        List<String> names = new ArrayList<>();
        try (JarFile jf = new JarFile(GTOLIB_JAR)) {
            Enumeration<JarEntry> en = jf.entries();
            while (en.hasMoreElements()) {
                JarEntry e = en.nextElement();
                String n = e.getName();
                if (n.endsWith(".class") && !n.startsWith("META-INF") && n.startsWith(PREFIX)) {
                    names.add(n.substring(0, n.length() - 6));
                }
            }
        }
        out.println("  .class 条目数: " + names.size());

        hr("4. 逐个 init_gtolib + 加载");
        ClassLoader cl = SealBulkC.class.getClassLoader();
        int ok = 0, fullyImpl = 0, stillNative = 0, loadFail = 0, initFail = 0;
        List<String> failed = new ArrayList<>();
        List<String> notImpl = new ArrayList<>();
        List<Class<?>> loadedClasses = new ArrayList<>();

        // ---- 第 1 轮：对全部类先登记（此时一个都不加载）----
        out.println("  [第 1 轮] 对全部类调 init_gtolib（登记，不加载）");
        List<String> registered = new ArrayList<>();
        for (String n : names) {
            try {
                initGtolib.invoke(null, getObjectType.invoke(null, n));
                registered.add(n);
            } catch (Throwable t) {
                initFail++;
                failed.add(n.replace('/', '.') + "  [init] " + root(t));
            }
        }
        out.println("  已登记: " + registered.size() + " / " + names.size());

        // ---- 第 2 轮：再逐个加载 ----
        out.println("  [第 2 轮] 逐个加载（依赖类此时也已被登记）");
        for (String n : registered) {
            String dot = n.replace('/', '.');
            try {
                Class<?> c = Class.forName(dot, false, cl);
                loadedClasses.add(c);
                ok++;
                int nat = 0;
                for (Method m : c.getDeclaredMethods()) if (Modifier.isNative(m.getModifiers())) nat++;
                if (nat == 0) fullyImpl++;
                else { stillNative++; notImpl.add(dot + "  native=" + nat); }
            } catch (Throwable t) {
                loadFail++;
                failed.add(dot + "  [load] " + root(t));
            }
        }

        hr("5. 结果");
        out.printf("  总类数          : %d%n", names.size());
        out.printf("  加载成功        : %d%n", ok);
        out.printf("  其中完全无 native: %d%n", fullyImpl);
        out.printf("  仍有 native     : %d%n", stillNative);
        out.printf("  加载失败        : %d%n", loadFail);
        out.printf("  合计(成功+失败)  : %d%n", ok + loadFail);
        out.printf("  init_gtolib 失败: %d%n", initFail);

        if (!notImpl.isEmpty()) {
            out.println("\n  --- 仍有 native 的类（前 20）---");
            for (int i = 0; i < Math.min(20, notImpl.size()); i++) out.println("    " + notImpl.get(i));
        }
        if (!failed.isEmpty()) {
            out.println("\n  --- 失败清单（前 30）---");
            for (int i = 0; i < Math.min(30, failed.size()); i++) out.println("    " + failed.get(i));
            // 按异常类型归类
            Map<String, Integer> byErr = new TreeMap<>();
            for (String f : failed) {
                String k = f.substring(f.lastIndexOf('['));
                byErr.merge(k, 1, Integer::sum);
            }
            out.println("\n  --- 失败原因分布 ---");
            byErr.entrySet().stream()
                .sorted((a, b) -> b.getValue() - a.getValue())
                .limit(15)
                .forEach(e -> out.println("    " + e.getValue() + "  " + e.getKey()));
        }

        // ---- 6. 让 agent 捕获字节 ----
        hr("6. 通过 javaagent 捕获 class 字节");
        try {
            Class<?> ca = Class.forName("CaptureAgent");
            Method capAll = ca.getMethod("captureAll", List.class);
            Object n = capAll.invoke(null, loadedClasses);
            out.println("  已捕获 " + n + " 个类的字节");
        } catch (Throwable t) {
            out.println("  CaptureAgent 调用失败（可能未加 -javaagent）: " + root(t));
        }

        if (wait) {
            hr("6. 保持存活，等待外部 JVMTI 工具附加");
            out.println("  PID = " + ProcessHandle.current().pid());
            out.println("  最长等待 900 秒");
            out.flush();
            Thread.sleep(900_000L);
        }
        hr("完成");
    }

    static String root(Throwable t) {
        Throwable c = t;
        if (c instanceof InvocationTargetException && c.getCause() != null) c = c.getCause();
        String m = c.toString();
        if (c.getCause() != null) m += " <- " + c.getCause();
        return m;
    }
}
