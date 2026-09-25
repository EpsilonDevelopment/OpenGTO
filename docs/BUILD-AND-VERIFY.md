# Build And Verification Record

## Prerequisites

* JDK 21 (`java.toolchain.languageVersion = 21`; GTCEu and the client both require JVM 21 bytecode).
* Network access — Minecraft 1.20.1, Forge 47.4.20 and the mod dependencies are resolved from Maven
  (`maven.gtodyssey.com`, NeoForged, MinecraftForge, Maven Central, CurseForge).
* Local jars in `libs/` (they are resolved through the `flatDir { dir "libs" }` repository):

| Jar | Used for |
| --- | --- |
| `gtocore-forge-1.20.1-26.8.3-techtree.jar` | `com.gtocore.*` reverse references (`compileOnly`) |
| `gto-seal-runtime-1.0.jar` | `dev:gto-seal-runtime:1.0` |
| `gtceu-1.20.1-forge-1.20.1-26.8.11.jar` | GTCEu |
| `appliedenergistics2-forge-1.20.1-15.268.1.jar` | AE2 |
| `FastCollection-26.8.7.jar`, `fastcollection-1.2.jar` | `com.gto.fastcollection.fastutil.*` |
| `commons-math3-3.6.1.jar` | math helpers |
| `Jade-*.jar`, `jecharacters-*.jar`, `gtoepp-1.0.1.jar`, `calculatoroverlay-1.0.2.jar`, `RecipeSearch-1.3.jar` | optional mixin targets / compat |

## Command

```bash
./gradlew clean build
```

Executed with `JAVA_HOME` pointing at JDK 21, network enabled, no `--offline`, no extra tasks.

## Result

```text
> Task :compileKotlin NO-SOURCE
> Task :compileJava
> Task :processResources
> Task :jar
> Task :reobfJar
> Task :build
BUILD SUCCESSFUL
```

Artifacts:

| Artifact | Size | Content |
| --- | --- | --- |
| `build/libs/gtolib-26.8.3-techtree.jar` | 1.8 MB | 839 classes, `gtolib.mixins.json`, generated `gtolib.refmap.json`, `META-INF/accesstransformer.cfg` |
| `build/libs/gtolib-26.8.3-techtree-sources.jar` | 787 KB | the restored sources |

## Fidelity check against the official artifact

| Metric | Official `gtolib-protected.jar` | This build |
| --- | --- | --- |
| Class files | 836 | 839 |
| Only here | 7 Kotlin-generated anonymous classes (`X$buildAndInit$1`, `X$button$button$1`, …) | 7 equivalent Java anonymous classes (`X$1`, `X$2`, `X$3`, …) |
| Only in official | — | `GTOLibMod` (the official stub, carried by the client jar), `MainLogic$1`, `RecipeScreenMixin$1` |
| Resources | `gtolib.mixins.json`, `gtolib.refmap.json`, `gtocore.refmap.json`, `META-INF/gtocore-forge-1.20.1.kotlin_module`, `native0/**` payloads | `gtolib.mixins.json`, generated `gtolib.refmap.json` |
| Sealed payloads | 675 `.prod.bin` + `.dev.bin` | none (source form) |

The missing `gtocore.refmap.json`, the `kotlin_module` marker and the `native0/` payloads are
artifacts of the official *GTOCore* build pipeline, not of GTOLib source; they cannot exist in a
standalone GTOLib build. The refmap is produced by the Mixin annotation processor in both cases.

### Build-configuration correction

`dependencies.gradle` is derived from the GTOCore repository's own dependency file. GTOCore wraps
FastCollection / commons-math3 / AE2 / GTCEu in `jarJar(...)`, which is correct for the **GTOCore**
jar but wrong for GTOLib: the official `gtolib-protected.jar` contains no `META-INF/jarjar` section.
Those wrappers were removed here, which took the produced jar from 20.7 MB to 1.8 MB and aligned it
with the official structure.

