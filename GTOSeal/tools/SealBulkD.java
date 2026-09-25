import java.io.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import java.util.zip.*;

/**
 * SealBulkD = SealBulkC + 可选的 init_gtolib(ClassLoader) 预调用。
 * 由 -Dseal.clinit=1 打开；用于测试 DLL 的另一条入口是否能让 C 的类解密。
 */
public class SealBulkD {

    static String GTOLIB_JAR = System.getProperty("seal.jar",
        "<workspace>/_seal-close-gap/gtolib-C-unsealed.jar");
    static long SALT = Long.getLong("seal.salt", -6851021872117151357L);
    static String PREFIX = System.getProperty("seal.prefix", "com/gtolib/");
    static boolean CLINIT = "1".equals(System.getProperty("seal.clinit", "0"));

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
        Object initRet = i0.invoke(null);
        out.println("  GTOServices.initialize() = " + initRet);
        Class<?> gp = Class.forName("gto.native0.plugins.GTOProvider");

        hr("2. 注入 ModuleDataProvider + salt");
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
        out.println("  注入完成  seal.jar=" + GTOLIB_JAR + "  salt=" + SALT);

        Class<?> typeCls = Class.forName("org.objectweb.asm.Type");
        Method getObjectType = typeCls.getMethod("getObjectType", String.class);
        Method initGtolib = gp.getDeclaredMethod("init_gtolib", typeCls);
        initGtolib.setAccessible(true);

        if (CLINIT) {
            hr("2b. 预调用 init_gtolib(ClassLoader)");
            try {
                Method m2 = gp.getDeclaredMethod("init_gtolib", ClassLoader.class);
                m2.setAccessible(true);
                Object r = m2.invoke(null, SealBulkD.class.getClassLoader());
                out.println("  init_gtolib(ClassLoader) 返回: " + r);
            } catch (Throwable t) {
                out.println("  init_gtolib(ClassLoader) 失败: " + root(t));
            }
        }

        hr("3. 枚举 jar 内的类");
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
        ClassLoader cl = SealBulkD.class.getClassLoader();
        int ok = 0, fullyImpl = 0, stillNative = 0, loadFail = 0, initFail = 0;
        List<String> failed = new ArrayList<>();
        List<String> notImpl = new ArrayList<>();
        List<Class<?>> loadedClasses = new ArrayList<>();

        out.println("  [第 1 轮] 登记");
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

        out.println("  [第 2 轮] 加载");
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
        out.printf("  init_gtolib 失败: %d%n", initFail);

        hr("6. 通过 javaagent 捕获 class 字节");
        try {
            Class<?> ca = Class.forName("CaptureAgent");
            Method capAll = ca.getMethod("captureAll", List.class);
            Object n = capAll.invoke(null, loadedClasses);
            out.println("  已捕获 " + n + " 个类的字节");
        } catch (Throwable t) {
            out.println("  CaptureAgent 调用失败: " + root(t));
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
