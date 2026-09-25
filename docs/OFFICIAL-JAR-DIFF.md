# Official JAR Diff

Compares the artifact produced by this tree with the official release artifact, to separate
restoration defects from source-branch differences.

## Artifacts

| Role | Path | Size | SHA-256 |
| --- | --- | --- | --- |
| Ours (clean build 2026-09-23) | `GTOCore/build/libs/gtocore-forge-1.20.1-26.8.3-techtree.jar` | 53,342,757 | `d168cdb72e0ec0fbb893e824f4aebadfc047db101197158271716d40c246a3c4` |
| Official | `GTOCore/GTOLib/libs/gtocore-forge-1.20.1-26.8.3-techtree.jar` | 54,752,268 | `3dc9958399ea58ba2d59f0a4f144ad69a448b53a5dbd8b833b8fc59d4e7ca1e9` |

The official jar is byte-identical to the client copy at
`<client>/mods/gtocore-forge-1.20.1-26.8.3-techtree.jar`,
so the comparison does not require touching the client. The official jar also enters this tree as a
`compileOnly` dependency (`GTOLib/dependencies.gradle`), which is what resolves the reverse
`com.gtocore.*` references.

The local jar is not a reproducible build: entry timestamps and archive ordering change on every
build, so the "Ours" hash above identifies one specific clean build rather than a canonical output.
Compare sizes and entry-level content, not whole-archive hashes.

## Entry level

| Metric | Result |
| --- | --- |
| Entries | ours 10,906 / official 10,908 |
| Only in ours | none |
| Only in official | `META-INF/GTOKEY.EC`, `META-INF/GTOKEY.SF` (GTO-Seal signature) |
| Same name, different content | 7 |

## The seven differences

| Entry | Verdict |
| --- | --- |
| `META-INF/MANIFEST.MF` (396 → 1,589,800) | Expected. The official manifest carries `GTO-Seal-Salt` and a per-entry `SHA-384-Digest` block; our build skips `signJar`. |
| `META-INF/jarjar/metadata.json` (1,332 → 1,280) | Line endings only (CRLF vs LF). The four embedded jars are the same versions (AE2 15.268.1, GTCEu 26.8.11, FastCollection 26.8.7, commons-math3 3.6.1). |
| `com/gtocore/common/data/GTOItems$1.class` (2,331 → 2,322) | Debug info only. Constant pool indices and both method bodies are identical; the single difference is a `LocalVariableTable` parameter name, `properties` (ours) vs `o` (official). No semantic difference. |
| `com/gtocore/common/data/machines/GeneratorMultiblock.class` (72,343 → 72,400) | Source-branch difference. Official has 177 pattern-row string literals, ours 174; `main`/`origin/26.8` have 177 and `origin/techtree2`/`origin/0.6.0` have 174. |
| `com/gtocore/common/data/machines/MultiBlockG.class` (55,784 → 55,812) | Source-branch difference. String sets are identical (225 each); only lambda-vs-named-method shape differs. |
| `pattern/wireless_dimension_repeater.mbs` (239 → 257) | Source-branch difference, byte-exact proof: the official entry equals `main`/`origin/26.8` byte for byte, ours equals `origin/techtree2`/`origin/0.6.0` byte for byte. |
| `com/gtocore/common/entity/NukeBombEntity.class` (2,141 → 2,379) | Unresolved build-toolchain artifact. See below. |

## Source branch

The official jar was built from `main` / `origin/26.8`. This tree is `origin/techtree2`:
`NukeBombEntity.java` and `GTOItems.java` are the same blob in every ref, `GeneratorMultiblock.java`,
`MultiBlockG.java` and `wireless_dimension_repeater.mbs` match `techtree2` exactly, and the official
jar matches `main`/`26.8` for the pattern file. No difference in `com/gtocore/**` points at a GTOLib
restoration defect.

## `NukeBombEntity` bridge

The official class declares one extra method that ours does not:

```
public net.minecraft.world.entity.Entity m_19749_();
  flags: ACC_PUBLIC | ACC_BRIDGE | ACC_SYNTHETIC
  0: aload_0
  1: invokespecial  // GTExplosiveEntity.m_19749_:()Lnet/minecraft/world/entity/LivingEntity;
  4: areturn
  LineNumberTable: line 17: 0
  RuntimeVisibleAnnotations: javax.annotation.Nullable
```

`m_19749_` is `getOwner` (`srg_to_official_1.20.1.tsrg`). `NukeBombEntity` implements no interface
(`interfaces: 0`) and `NukeBombEntity.java` is the same blob in all twelve refs and in this tree;
the file never contained a `getOwner` override in any ref.

Reproduction attempts, all negative:

| Compiler | Input | Result |
| --- | --- | --- |
| javac 17 / 21 / 25 | leaf `final` class, no override | no bridge (matches ours) |
| javac 21 | leaf class with a narrow covariant override | two methods (override + `invokevirtual` bridge) |
| ECJ 3.37.0 | leaf `final` class, no override | no bridge |
| ECJ 3.37.0 | leaf class with a narrow covariant override | two methods (override + `invokevirtual` bridge) |

So the official class is neither plain javac nor plain ECJ output for any available source; the
official pipeline must add or rewrite the bridge. Impact is nil: no entry in either jar references
`NukeBombEntity.getOwner()`, and JVM resolution of `NukeBombEntity.m_19749_:()LEntity;` falls through
to the `PrimedTnt` bridge. The harness is eight throwaway classes (a four-level covariant-return
hierarchy, an override variant, a wide-return variant and an anonymous-class case) and is
reproducible from the table above with JDK 17/21/25 and `org.eclipse.jdt:ecj:3.37.0`.

## What this comparison does not prove

`com/gtolib/**` (932 entries) and `native0/**` (750 entries) are identical between the two jars, but
that is by construction, not evidence about the restored Kotlin/Java sources. The build log records:

```
GTOLib pipeline: sources=true, jarUpToDate=true, compileSources=false, ..., sealPack=false, GTOSeal=false
GTOLib: 指纹命中 → 编译复用 protected jar
GTOLib: 已从 libs 回填 build/gtolib，未执行 ProGuard/Seal；jarSha=399d76d6bb6e61cb…
GTO jar: 嵌入 gtolib 包体 ← gtolib-release.jar
```

The build reused the prebuilt `libs/gtolib-protected.jar` / `libs/gtolib-release.jar` and embedded the
official sealed payload. Source-level fidelity of the restored GTOLib sources is therefore validated
only by the forced source compile (`:compileKotlin -PgtolibRebuild=true`, zero errors), not by this
byte comparison. A future check should compare classes compiled from source against
`libs/gtolib-release.jar`.
