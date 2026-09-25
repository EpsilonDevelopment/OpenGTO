import java.io.*;
import java.lang.instrument.*;
import java.security.ProtectionDomain;
import java.util.*;

/**
 * CaptureAgent —— 在同一个 JVM 内捕获 class 字节。
 *
 * 用法：java -javaagent:capture-agent.jar -Dcapture.out=<目录> ...
 *
 * 两条捕获路径：
 *   1) transform()：每次 defineClass 时被调用
 *   2) retransform()：对已加载的类重新走一遍 hook 链，拿到「当前字节」
 *       —— 对 Seal 解密后的类，这就是解密结果
 */
public class CaptureAgent {

    static Instrumentation inst;
    static String outDir;
    static final Map<String, Integer> captured = new LinkedHashMap<>();

    public static void premain(String args, Instrumentation instrumentation) {
        inst = instrumentation;
        outDir = System.getProperty("capture.out", "captured");
        new File(outDir).mkdirs();
        instrumentation.addTransformer(new ClassFileTransformer() {
            @Override
            public byte[] transform(ClassLoader loader, String name, Class<?> cls,
                                    ProtectionDomain pd, byte[] bytes) {
                if (name != null && name.startsWith("com/gtolib/")) {
                    record(name, bytes);
                }
                return null;   // 不改字节，只观察
            }
        }, true);
        System.out.println("[CaptureAgent] 已装载，输出目录 = " + new File(outDir).getAbsolutePath());
    }

    static synchronized void record(String name, byte[] bytes) {
        try {
            File f = new File(outDir, name + ".class");
            File p = f.getParentFile();
            if (p != null) p.mkdirs();
            Integer prev = captured.get(name);
            // 保留体积更大的那份（空壳 21K vs 解密后 64K）
            if (prev == null || bytes.length > prev) {
                try (FileOutputStream fos = new FileOutputStream(f)) { fos.write(bytes); }
                captured.put(name, bytes.length);
            }
        } catch (IOException e) { /* ignore */ }
    }

    /** 由主程序反射调用：对已加载的类做 retransform，触发 transform() 拿当前字节 */
    public static int captureAll(List<Class<?>> classes) {
        List<Class<?>> todo = new ArrayList<>();
        for (Class<?> c : classes) {
            String n = c.getName().replace('.', '/');
            if (n.startsWith("com/gtolib/")) todo.add(c);
        }
        System.out.println("[CaptureAgent] 准备 retransform " + todo.size() + " 个类");
        int batch = 200, ok = 0;
        for (int i = 0; i < todo.size(); i += batch) {
            List<Class<?>> sub = todo.subList(i, Math.min(i + batch, todo.size()));
            try {
                inst.retransformClasses(sub.toArray(new Class<?>[0]));
                ok += sub.size();
            } catch (Throwable t) {
                // 退化为逐个
                for (Class<?> c : sub) {
                    try { inst.retransformClasses(c); ok++; } catch (Throwable t2) { }
                }
            }
        }
        System.out.println("[CaptureAgent] retransform 完成，成功 " + ok);
        return captured.size();
    }

    public static int count() { return captured.size(); }
}
