import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import java.io.File;
import java.util.*;

/**
 * 把 gtoseal-dumper.jar 附加到正在运行的 Minecraft/Forge 进程。
 * 用法: java Attacher [PID] [输出目录]
 *   不带 PID 时自动挑选第一个看起来是 Minecraft/Forge 的 java 进程。
 */
public class Attacher {
    public static void main(String[] args) throws Exception {
        File loc = new File(Attacher.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        File base = loc.isDirectory() ? loc : loc.getParentFile();
        String agent = System.getProperty("gto.agent");
        if (agent == null || !new File(agent).isFile()) agent = new File(base, "gtoseal-dumper.jar").getAbsolutePath();
        if (!new File(agent).isFile()) {
            System.out.println("[!] 找不到 " + agent);
            return;
        }
        String out = args.length > 1 ? args[1] : new File(".").getAbsolutePath() + File.separator + "gtolib-dump";
        String pid = args.length > 0 ? args[0] : null;

        List<VirtualMachineDescriptor> vms = VirtualMachine.list();
        System.out.println("== 当前可附加的 JVM ==");
        long self = ProcessHandle.current().pid();
        for (VirtualMachineDescriptor d : vms) {
            String n = d.displayName();
            boolean selfFlag = n != null && n.contains(String.valueOf(self));
            System.out.printf("  pid=%-8s %s%s%n", d.id(),
                    (n == null ? "(no name)" : (n.length() > 110 ? n.substring(0, 110) + "..." : n)),
                    selfFlag ? "   <- 本程序" : "");
        }
        if (pid == null) {
            for (VirtualMachineDescriptor d : vms) {
                String n = d.displayName() == null ? "" : d.displayName().toLowerCase();
                if (n.contains(String.valueOf(self))) continue;
                if (n.contains("forge") || n.contains("minecraft") || n.contains("fml") || n.contains("bootstraplauncher")) {
                    pid = d.id(); break;
                }
            }
            if (pid == null) {
                System.out.println("\n[!] 未自动识别到 Minecraft/Forge 进程，请把 PID 作为第一个参数传入，例如：");
                System.out.println("    java -cp . Attacher 12345 D://gtolib-dump");
                return;
            }
            System.out.println("\n[+] 自动选择 pid=" + pid);
        }
        System.out.println("[+] agent   = " + agent);
        System.out.println("[+] 输出目录 = " + out);
        VirtualMachine vm = VirtualMachine.attach(pid);
        try {
            vm.loadAgent(agent, out);
            System.out.println("[+] 附加成功。类字节会实时写入输出目录；已加载的 com.gtolib 类会被 retransform 补抓。");
        } finally {
            vm.detach();
        }
    }
}
