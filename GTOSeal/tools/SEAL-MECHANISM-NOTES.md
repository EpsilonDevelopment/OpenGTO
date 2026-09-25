# `_sealharness` — GTO Seal 运行时独立加载探针

**目的**：在不启动 Minecraft 的前提下，解密 `gtolib-protected.jar` 的全部类。

**结论：成功。805 / 805 个类全部拿到，`native` 归零。**

> 完整报告见 `../_meta/GTO-Seal-离线完全解密报告.md`。
> 本文是**实证过程记录**（各代探针的发现与踩过的坑）。

---

## 最终配方（TL;DR）

```bash
cd "<seal-harness>"
java @bulk.args          # 见「四、完整配方」
```

四条关键：
1. **顺序**：先 `init_gtolib(Type)`，再 `Class.forName` —— 反了就只拿到空壳
2. **两轮**：先把 805 个类全部登记，再统一加载（避免依赖被提前加载）
3. **去封缄**：classpath 排除 `gtocore-forge-*.jar`，并用去掉 `Sealed: true` 的 `gtolib-unsealed.jar`
4. **同进程捕获**：`-javaagent` 装 `CaptureAgent`，加载完调 `retransformClasses` 取「当前字节」

---

## 一、环境

| 组件 | 路径 |
|---|---|
| JDK | `C:\Program Files\Java\jdk-25.0.3`（与客户端一致） |
| seal runtime | `<seal-source>/gto-seal-runtime-1.0.jar` |
| 被保护 jar | `<gtocore-main>/libs/gtolib-protected.jar` |
| 运行期类快照 | `<dump-root>/gto-gtodyssey0.6dev1-<timestamp>/` |
| 客户端库（628 jar） | `<mclibs>/` ← **指向启动器 `libraries` 目录的目录联接** |

### 运行方式

```bash
cd "<seal-harness>"
java @probe5.args > out.txt 2>&1
```

`probe*.args` 是 Java 的 **argfile**（因为 classpath 有 6 万多字符，超过命令行上限）。

---

## 二、两个把人坑惨的 argfile 陷阱（务必先读）

### 1. 非 ASCII 路径会被**静默跳过**

启动器 `libraries` 路径里的中文，Java 启动器读 argfile 时按系统 ANSI 编码解释，
路径被搞乱后 **不报错、直接忽略整个 jar**。

现象：`slf4j-api-2.0.17.jar` 明明在 classpath 第 3 位，`BasicMarkerFactory` 却从 dump 目录加载，
而 dump 里恰好缺 `BasicMarker` → 一路 `NoClassDefFoundError`。

**解决**：建一个纯 ASCII 路径的目录联接。

```powershell
New-Item -ItemType Junction -Path '<mclibs>' `
         -Target '<launcher>\.minecraft\libraries'
```

### 2. argfile 里的 `\` 是**转义字符**

```text
-cp
<dir>\a.jar;<dir>\b.jar     ← 反斜杠被吃掉，路径全乱
```

**解决**：classpath 一律用正斜杠 `/`，且不要加引号（路径里没有空格）。

校验方法：写完后比对 `System.getProperty("java.class.path").length()` 与写入长度是否一致。
不一致就说明有字符被吞。

---

## 三、已实证的运行时事实

### 3.1 `native0.Loader` 能独立加载

释放 `native0/x64-windows.dll` 到临时目录并 `System.load`，成功。

### 3.2 `Hidden0` 由原生侧定义为**引导类加载器的普通类**

```text
Class.forName("native0.hidden.Hidden0") -> OK
    isHidden    = false
    classLoader = null          ← 引导类加载器
    nestHost    = native0.hidden.Hidden0
```

注意 `isHidden=false` —— 它不是 `defineHiddenClass` 生成的隐藏类，
而是原生侧用 JNI `DefineClass` 定义进**引导加载器**的普通类。所以可以按名字拿到。

其 8 个方法（与 dump 里的字节码一致）：

| 方法 | 性质 |
|---|---|
| `special_clinit_0_10(Class)` | native |
| `special_clinit_1_50(Class)` | native —— **无任何已知调用方** |
| `special_clinit_2_50(Class)` | native |
| `special_clinit_3_90(Class)` | native |
| `invokereverse0(Object, MethodHandle)` | Java |
| `invokereverse1(Object, Object, Object, MethodHandle)` | Java |
| `invokereverse2(Object, Object, MethodHandle)` | Java |
| `invokereverse3(MethodHandle)` | Java |

### 3.3 正确的加载顺序是 槽位 0 → 3 → 2

```java
Class.forName("native0.Loader");                        // 原生库
Class.forName("gto.native0.GTOServicesInit");           // 槽位 0：NAME="GTO Services"，LOGGER 已填充
Class.forName("gto.native0.plugins.GTOServices");       // 槽位 3：NAME="gto_native"
    GTOServices.initialize();                           // 静态 native，返回 long
Class.forName("gto.native0.plugins.GTOProvider");       // 槽位 2
```

三个类的 `<clinit>` 都是同一个三段式：

```java
static {
   native0.Loader.registerNativesForClass(<slot>, <ThisClass>.class);
   Hidden0.special_clinit_<slot>_<n>(<ThisClass>.class);
}
```

### 3.4 **关键：注入 `ModuleDataProvider` 是缺失的那块拼图**

`GTOProvider` 的 5 个静态字段（2×`ModuleDataProvider`、2×`int`、1×`long`）
在 `<clinit>` 跑完后**仍然是 null / 0**。

手工构造并反射写入后，行为发生质变：

```java
SecureJar sj = SecureJar.from(Paths.get(".../gtolib-protected.jar"));
Object provider = sj.moduleDataProvider();          // JarModuleDataProvider
// 写入两个 ModuleDataProvider 字段
```

| | 注入前 | 注入后 |
|---|---|---|
| `init_gtolib(Type)` | `NullPointerException: "INVOKEVIRTUAL Object npe" on 1067` | **正常返回 `null`** |

也就是说：**这就是缺的运行时状态**。之前在真实游戏里，
这个 provider 应当由 `GTOServices.completeScan(IModuleLayerManager)` 之类
能访问模块层的 ModLauncher 钩子写入。

### 3.5 Manifest 里的密钥参数

```text
GTO-Seal         = 1
GTO-Seal-Salt    = 7231466345131869023
GTO-Seal-Version = 0
NTPT-Salt        = 7231466345131869023
NTPT-Version     = 0
```

### 3.6 `decrypt(byte[], String)` 会打日志但**不改字节**

log4j 接通后可以看到它确实在跑：

```text
[main/INFO]: decrypt com/gtolib/api/recipe/RecipeBuilder hashcode -1617047309
```

但字节数组不变。试过 5 种 name 变体（类名点分/斜杠、文件名、jar 内完整路径），
手工写入 salt 后重试，**均无变化**。

### 3.7 载荷大小接近解密后大小 —— 载荷就是「真身」

| 类 | jar 内空壳 | `.prod.bin` 载荷 | dump 里解密后 |
|---|---:|---:|---:|
| `Client` | 1,369 B | 1,372 B | — |
| `DependencyGraph` | 2,680 B | 2,948 B | — |
| `RecipeBuilder` | 41,653 B | **62,251 B** | **64,206 B** |

`RecipeBuilder` 的载荷 62,251 B 与解密后 64,206 B 高度接近，
而空壳只有 41,653 B —— 载荷确实是完整的实现（压缩/加密形态）。

---

## 四、剩余缺口（精确定位）

`init_gtolib(Type)` 正常返回，但**通过我们自己注入的 provider 读类，拿到的仍是空壳**
（前后 sha256 完全一致）。

原因是 `GTOPlugin.processClassWithFlags` 的签名：

```java
public int processClassWithFlags(Phase phase, ClassNode node, Type type, String reason) {
   GTOProvider.init_gtolib(type);      // 只传了 Type，没传 node
   return 0;                           // 也不通过返回值回传
}
```

解密结果**不是**经由我们控制的 `ModuleDataProvider` 出去的，而是：

- 要么原生侧直接**原地改写 ModLauncher 传进来的 `ClassNode`**（通过 `Type` 反查内部注册表）；
- 要么经由 `Hidden0.invokereverseN(..., MethodHandle)` 那组 MethodHandle 跳板回调。

两者都需要**真实的 ModLauncher 转换管线**在场，即需要把 `TransformingClassLoader`
和模块层真正跑起来。

---

## 五、因此，可行的路线

| 路线 | 可行性 | 说明 |
|---|---|---|
| **B1 · 真实 Forge 运行时 dump** | **已跑通** | `Java-Dumper` + 客户端。本次已拿到 457/805 个 gtolib 类的真实实现 |
| **B2 · 补齐 ModLauncher 转换管线后离线解密** | 中 | 已解决依赖、状态注入、加载顺序；还差把 ModLauncher 的 `TransformingClassLoader` + 模块层跑起来 |
| **A · Ghidra 逆向原生库** | 未知 | 需突破白盒 AES；库只有 1 个导出 `JNI_OnLoad`，无 OS 加密 API 导入 |

> 本次**未启动 Minecraft 客户端**。所有探针都在独立 JVM 中运行。

---

## 六、文件清单

| 文件 | 说明 |
|---|---|
| `SealProbe.java` | 第一代：验证 classpath 补齐后 `init_gtolib` 的进展 |
| `SealProbe2.java` | 按槽位顺序 0→3→2 初始化 |
| `SealProbe3.java` | 构造 SecureJar 并注入 `ModuleDataProvider`（**质变点**） |
| `SealProbe4.java` | 验证解密结果从哪出来（provider 重读 + decrypt 探针） |
| `SealProbe5.java` | 追加 salt 注入 |
| `CpTest.java` | 诊断类由哪个 jar 提供（定位 argfile 陷阱用的） |
| `probe*.args` | Java argfile（classpath 6 万字符，必须用 argfile） |
| `probe*-out.txt` | 各代完整输出 |
| `payloads/` | 从 `gtolib-protected.jar` 抽出的 `.prod.bin` 样本 |
| `SealDump.java` | 上一代探针（缺 Forge 依赖，结论已过时） |
