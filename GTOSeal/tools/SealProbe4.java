import java.io.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;

/**
 * SealProbe4 —— 状态注入成功后，验证解密是否真的生效。
 *
 * 前一代的成果：把 gtolib-protected.jar 的 ModuleDataProvider 反射注入
 * GTOProvider 的两个静态字段后，init_gtolib(Type) 从 NullPointerException
 * 变成**正常返回**。说明这就是缺失的运行时状态。
 *
 * 本代要回答的问题：解密结果从哪里出来？
 *   路线 1：init_gtolib 之后，再通过 provider.open() 读同一个类 → 字节是否变了？
 *   路线 2：直接调 decrypt(payloadBytes, name) → 载荷是否变成合法 class？
 */
public class SealProbe4 {

    static final String GTOLIB_JAR =
        "<workspace>/GTOCore-Main/libs/gtolib-protected.jar";
    static final String PAYLOAD_DIR =
        "<workspace>/_sealharness/payloads";

    static void hr(String s) {
        System.out.println();
        System.out.println("==================== " + s + " ====================");
    }

    static String uplus(String s) {
        StringBuilder sb = new StringBuilder();
        s.codePoints().forEach(c -> sb.append(String.format("U+%04X ", c)));
        return sb.toString().trim();
    }

    static String sha(byte[] b) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] d = md.digest(b);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 8; i++) sb.append(String.format("%02x", d[i]));
            return sb.toString();
        } catch (Exception e) { return "?"; }
    }

    static byte[] readOpt(Object opt) throws Exception {
        if (!(opt instanceof Optional)) return null;
        Optional<?> o = (Optional<?>) opt;
        if (o.isEmpty()) return null;
        Object in = o.get();
        if (in instanceof byte[] b) return b;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        if (in instanceof InputStream is) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = is.read(buf)) != -1) bos.write(buf, 0, n);
            is.close();
        }
        return bos.toByteArray();
    }

    static Object gProvider = null;
    static Method gOpen = null;

    static byte[] readClass(String path) throws Exception {
        if (gProvider == null) return null;
        return readOpt(gOpen.invoke(gProvider, path));
    }

    static void report(String tag, String path) throws Exception {
        byte[] b = readClass(path);
        if (b == null) {
            System.out.println("    [" + tag + "] " + path + " -> 读不到");
            return;
        }
        System.out.printf("    [%s] %-46s %6d B  sha=%s  前4=%02x%02x%02x%02x%n",
                tag, path, b.length, sha(b), b[0], b[1], b[2], b[3]);
    }

    public static void main(String[] args) throws Exception {
        String[] targets = {
            "com/gtolib/Client",
            "com/gtolib/api/recipe/RecipeBuilder",
            "com/gtolib/ae2/crafting/models/DependencyGraph",
        };

        hr("1. 初始化（Loader → 槽位0 → 槽位3 → 槽位2）");
        Class.forName("native0.Loader");
        Class.forName("gto.native0.GTOServicesInit");
        Class<?> gts = Class.forName("gto.native0.plugins.GTOServices");
        Method init0 = gts.getDeclaredMethod("initialize");
        init0.setAccessible(true);
        init0.invoke(null);
        Class<?> gp = Class.forName("gto.native0.plugins.GTOProvider");
        System.out.println("  OK");

        hr("2. 注入 gtolib-protected.jar 的 ModuleDataProvider");
        Class<?> secureJarCls = Class.forName("cpw.mods.jarhandling.SecureJar");
        Object sj = secureJarCls.getMethod("from", Path[].class)
                .invoke(null, (Object) new Path[]{ Paths.get(GTOLIB_JAR) });
        gProvider = secureJarCls.getMethod("moduleDataProvider").invoke(sj);
        gOpen = gProvider.getClass().getMethod("open", String.class);
        gOpen.setAccessible(true);
        for (Field f : gp.getDeclaredFields()) {
            if (!f.getType().getName().endsWith("ModuleDataProvider")) continue;
            f.setAccessible(true);
            f.set(null, gProvider);
            System.out.println("  写入 <" + uplus(f.getName()) + ">");
        }

        hr("3. 基线：init_gtolib 之前读类");
        for (String t : targets) report("before", t + ".class");

        hr("4. 调用 init_gtolib(Type)");
        Class<?> asmType = Class.forName("org.objectweb.asm.Type");
        Method getObjectType = asmType.getMethod("getObjectType", String.class);
        Method initGtolib = gp.getDeclaredMethod("init_gtolib", asmType);
        initGtolib.setAccessible(true);
        for (String t : targets) {
            try {
                Object r = initGtolib.invoke(null, getObjectType.invoke(null, t));
                System.out.println("  init_gtolib(" + t + ") -> " + r);
            } catch (Throwable e) {
                Throwable c = (e instanceof InvocationTargetException) ? e.getCause() : e;
                System.out.println("  init_gtolib(" + t + ") 异常: " + c);
            }
        }

        hr("5. 之后：再读同一个类");
        for (String t : targets) report("after ", t + ".class");

        hr("6. 直接调 decrypt(payload, name) —— 现在状态已初始化");
        Method decrypt = null;
        for (Method mm : gp.getDeclaredMethods()) {
            if (mm.getName().equals("decrypt")) { decrypt = mm; decrypt.setAccessible(true); break; }
        }
        if (decrypt == null) {
            System.out.println("  未找到 decrypt");
        } else {
            File dir = new File(PAYLOAD_DIR);
            File[] bins = dir.listFiles((d, n) -> n.endsWith(".bin"));
            if (bins == null || bins.length == 0) {
                System.out.println("  没有载荷文件: " + dir);
            } else {
                for (File bin : bins) {
                    byte[] data = Files.readAllBytes(bin.toPath());
                    System.out.println();
                    System.out.printf("  载荷 %s  %d B  sha=%s  前4=%02x%02x%02x%02x%n",
                            bin.getName(), data.length, sha(data), data[0], data[1], data[2], data[3]);
                    // 由文件名还原类名
                    String base = bin.getName().substring(0, bin.getName().length() - ".prod.bin".length());
                    String slash = base.replace('_', '/');
                    String[] names = {
                        slash,                                   // com/gtolib/...
                        slash.replace('/', '.'),                 // com.gtolib....
                        bin.getName(),
                        base,
                        "native0/native/" + slash + ".prod.bin",
                    };
                    for (String nm : names) {
                        byte[] copy = data.clone();
                        try {
                            decrypt.invoke(null, copy, nm);
                            boolean changed = !Arrays.equals(data, copy);
                            String magic = copy.length >= 4
                                ? String.format("%02x%02x%02x%02x", copy[0], copy[1], copy[2], copy[3]) : "????";
                            System.out.printf("      name=%-52s changed=%-5s sha=%s 前4=%s%n",
                                    nm, changed, sha(copy), magic);
                        } catch (Throwable e) {
                            Throwable c = (e instanceof InvocationTargetException) ? e.getCause() : e;
                            System.out.println("      name=" + nm + " 异常: " + c);
                        }
                    }
                }
            }
        }

        hr("完成");
    }
}
