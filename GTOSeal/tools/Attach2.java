import com.sun.tools.attach.VirtualMachine;

/** 极简附加器：Attach2 <pid> <agentJar> [args] —— 允许显式指定 agent jar 路径。 */
public class Attach2 {
    public static void main(String[] a) throws Exception {
        VirtualMachine vm = VirtualMachine.attach(a[0]);
        try {
            vm.loadAgent(a[1], a.length > 2 ? a[2] : null);
            System.out.println("[+] loaded " + a[1] + " -> pid " + a[0]);
        } finally {
            vm.detach();
        }
    }
}
