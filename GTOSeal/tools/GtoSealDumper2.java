import java.lang.instrument.*;
import java.lang.reflect.*;
import java.security.ProtectionDomain;
import java.io.*;
import java.util.*;
import java.util.jar.JarFile;

/**
 * GTO Seal 类字节转储 agent。
 *
 * 原理：受保护类的明文是在 JVM **定义类的那一刻**由 x64-windows.dll 自注册的
 * JVMTI ClassFileLoadHook 注入的，不经过 ModLauncher 的 ClassNode。
 * 因此本 agent 必须注册在 DLL 的 hook **之后**才能看到明文：
 *   - 若作为 -javaagent 在启动时加载：premain 只起一个看门线程，
 *     等 native0.hidden.Hidden0 出现（= DLL 已加载）后自附加一次，
 *     由 agentmain 注册 transformer；
 *   - 若在游戏运行中附加：agentmain 直接注册（此时 DLL 一定已加载）。
 *
 * 注册后会做三件事：
 *   1) 转储后续所有 com/gtolib/** 的类字节；
 *   2) 对已加载的 com/gtolib 类逐个 retransform 补抓（单个失败不影响其它）；
 *   3) 顺着 mod 的 ClassLoader 枚举 jar 内全部 com/gtolib 类名，
 *      用 Class.forName(name,false,cl) **强制定义**（不初始化），
 *      让 DLL 为每个类提供明文并落盘 —— 从而一次性抓全。
 */
public class GtoSealDumper2 {
    static File dir;
    static PrintStream log;
    static final Set<String> seen = Collections.synchronizedSet(new HashSet<String>());
    static volatile boolean dumping = false;

    static void logf(String s) {
        System.err.println("[DUMP] " + s);
        if (log != null) { log.println(s); log.flush(); }
    }

    public static void premain(String a, Instrumentation inst) {
        if (dllLoaded()) { setup(a, inst); return; }
        final String out = a;
        Thread t = new Thread(() -> {
            logf("premain: 等待 GTO Seal 原生库加载 ...");
            long deadline = System.currentTimeMillis() + 15 * 60 * 1000L;
            while (System.currentTimeMillis() < deadline) {
                if (dllLoaded()) {
                    logf("检测到原生库已加载，准备自附加 ...");
                    try { Thread.sleep(150); } catch (InterruptedException ignored) {}
                    selfAttach(out);
                    return;
                }
                try { Thread.sleep(100); } catch (InterruptedException ignored) {}
            }
            logf("超时：始终未检测到 GTO Seal 原生库（mod 是否已加载？）");
        }, "gtoseal-dumper-watch");
        t.setDaemon(true);
        t.start();
    }

    public static void agentmain(String a, Instrumentation inst) {
        try { setup(a, inst); }
        catch (Throwable t) {
            try (PrintStream p = new PrintStream(new FileOutputStream(
                    "<workspace>/Tools/gtolib-dump/_agentmain_error.txt", true), true, "UTF-8")) {
                p.println("agentmain failed: " + t);
                t.printStackTrace(p);
            } catch (Throwable ignored) {}
            throw new RuntimeException(t);
        }
    }

    static boolean dllLoaded() {
        for (ClassLoader cl : new ClassLoader[]{null, ClassLoader.getSystemClassLoader()}) {
            try { Class.forName("native0.hidden.Hidden0", false, cl); return true; } catch (Throwable ignored) {}
        }
        return false;
    }

    static void selfAttach(String out) {
        try {
            com.sun.tools.attach.VirtualMachine vm = com.sun.tools.attach.VirtualMachine.attach(
                    String.valueOf(ProcessHandle.current().pid()));
            try {
                vm.loadAgent(agentPath(), out);
                logf("自附加成功（transformer 已排在 DLL 之后）");
            } finally { vm.detach(); }
        } catch (Throwable t) {
            logf("自附加失败: " + t + "  (启动参数需加 -Djdk.attach.allowAttachSelf=true)");
        }
    }

    static String agentPath() {
        try {
            File loc = new File(GtoSealDumper2.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            File base = loc.isDirectory() ? loc : loc.getParentFile();
            return new File(base, "gtoseal-dumper.jar").getAbsolutePath();
        } catch (Throwable t) { return "gtoseal-dumper.jar"; }
    }

    static synchronized void setup(String a, Instrumentation inst) {
        if (dumping) { logf("已在转储中，忽略重复附加"); return; }
        dumping = true;
        dir = new File(a == null || a.isEmpty() ? "gtolib-dump" : a);
        dir.mkdirs();
        try { log = new PrintStream(new FileOutputStream(new File(dir, "_dumper.log"), true), true, "UTF-8"); }
        catch (Throwable ignored) {}
        logf("输出目录: " + dir.getAbsolutePath());
        logf("retransformSupported=" + inst.isRetransformClassesSupported());

        inst.addTransformer(new ClassFileTransformer() {
            public byte[] transform(ClassLoader l, String name, Class<?> c, ProtectionDomain pd, byte[] buf) {
                if (name != null && name.startsWith("com/gtolib/") && buf != null && buf.length > 4) {
                    write(name, buf);
                }
                return null;
            }
        }, true);

        // 阶段 2：补抓已加载类（逐个进行，单个失败不影响其它）
        Thread t2 = new Thread(() -> retransformLoaded(inst), "gtoseal-retransform");
        t2.setDaemon(true);
        t2.start();
    }

    static void write(String name, byte[] buf) {
        try {
            File f = new File(dir, name.replace('/', '.') + ".class");
            f.getParentFile().mkdirs();
            try (FileOutputStream o = new FileOutputStream(f)) { o.write(buf); }
            if (seen.add(name))
                logf("captured " + name + " len=" + buf.length
                        + " magic=" + String.format("%02x%02x%02x%02x", buf[0], buf[1], buf[2], buf[3]));
        } catch (Throwable t) { logf("write failed " + name + ": " + t); }
    }

    static void retransformLoaded(Instrumentation inst) {
        try {
            List<Class<?>> todo = new ArrayList<>();
            for (Class<?> c : inst.getAllLoadedClasses())
                if (c.getName().startsWith("com.gtolib") && inst.isModifiableClass(c)) todo.add(c);
            logf("待 retransform 的已加载类: " + todo.size());
            int ok = 0, fail = 0;
            for (Class<?> c : todo) {
                try { inst.retransformClasses(c); ok++; }
                catch (Throwable t) { fail++; }
            }
            logf("retransform 完成 ok=" + ok + " fail=" + fail);
            forceLoadAll(inst);
        } catch (Throwable t) { logf("retransform 阶段异常: " + t); }
    }

    /** 顺着 mod 的 ClassLoader 枚举 jar 内所有 com/gtolib 类并强制定义（不初始化）。 */
    static void forceLoadAll(Instrumentation inst) {
        try {
            ClassLoader cl = null;
            for (Class<?> c : inst.getAllLoadedClasses())
                if (c.getName().startsWith("com.gtolib")) { cl = c.getClassLoader(); if (cl != null) break; }
            if (cl == null) { logf("找不到 mod 的 ClassLoader，跳过强制加载"); return; }
            logf("mod ClassLoader = " + cl.getClass().getName());

            File jar = null;
            for (Class<?> c : inst.getAllLoadedClasses()) {
                if (!c.getName().startsWith("com.gtolib")) continue;
                ProtectionDomain pd = c.getProtectionDomain();
                if (pd == null || pd.getCodeSource() == null || pd.getCodeSource().getLocation() == null) continue;
                try {
                    File f = new File(pd.getCodeSource().getLocation().toURI());
                    if (f.isFile()) { jar = f; break; }
                } catch (Throwable ignored) {}
            }
            if (jar == null) { logf("找不到 mod jar，跳过强制加载"); return; }
            logf("mod jar = " + jar);

            List<String> names = new ArrayList<>();
            try (JarFile jf = new JarFile(jar)) {
                Enumeration<java.util.jar.JarEntry> en = jf.entries();
                while (en.hasMoreElements()) {
                    String n = en.nextElement().getName();
                    if (n.startsWith("com/gtolib/") && n.endsWith(".class"))
                        names.add(n.substring(0, n.length() - 6).replace('/', '.'));
                }
            }
            logf("jar 内 com/gtolib 类总数: " + names.size());

            int ok = 0, fail = 0;
            for (String n : names) {
                try { Class.forName(n, false, cl); ok++; }
                catch (Throwable t) { fail++; }
            }
            logf("强制定义完成 ok=" + ok + " fail=" + fail + " 已落盘=" + seen.size());
        } catch (Throwable t) { logf("强制加载阶段异常: " + t); }
    }
}
