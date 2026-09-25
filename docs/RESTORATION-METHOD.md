# Restoration Method — GTOLib 26.8.3-techtree

This document records exactly how the source tree in `src/main/java/com/gtolib` was recovered from
the sealed artifact. No official source repository access was available for GTOLib.

## 1. Inputs

| Input | Role |
| --- | --- |
| `libs/gtolib-protected.jar` (from the official `26.8.3-techtree` GTOCore snapshot) | Primary input. Classes keep their real names but their bodies are hollowed out; the real bytecode lives in `native0/native/com/gtolib/**.prod.bin` (+ `.dev.bin`) |
| `gtolib-26.8.3-techtree-sources.jar` (community artifact) | Cross-check only, see `COMMUNITY-JAR-ASSESSMENT.md` |
| Client mods folder (`GregTech-Odyssey-0.6.0-dev1`) | Cross-check of class names / nesting |
| `gtocore-forge-1.20.1-26.8.3-techtree.jar` | Supplies the reverse-referenced `com.gtocore.*` types |

## 2. Unsealing (GTO-Seal / NTPT)

`META-INF/MANIFEST.MF` of the protected jar declares:

```text
GTO-Seal: 1
GTO-Seal-Salt: -1426222262076855031
GTO-Seal-Version: 0
NTPT-Salt: -1426222262076855031
NTPT-Version: 0
```

The runtime decrypts class bodies on demand: the native library registers a class-file load hook,
and a class is only replaced with its real implementation **after** its name has been announced to
`gto.native0.plugins.GTOProvider.init_gtolib(org.objectweb.asm.Type)`. Once a class has been defined
it can no longer be replaced, so the order matters.

The harness (`SealBulk2683`) therefore:

1. boots the seal runtime — `native0.Loader`, `gto.native0.GTOServicesInit`,
   `gto.native0.plugins.GTOServices.initialize()`;
2. builds a `SecureJar` over the protected jar, injects its `ModuleDataProvider` and the salt
   `-1426222262076855031` into the static fields of `GTOProvider`;
3. enumerates every `.class` entry of the protected jar;
4. round 1 — calls `init_gtolib(Type)` for every class without loading any of them;
5. round 2 — forces definition with `Class.forName(name, false, loader)`, at which point the native
   hook hands the decrypted bytecode to the JVM;
6. a capture agent (a `ClassFileTransformer`) writes each final class file to disk.

Result: **836 fully implemented class files** captured (no `native` methods left, no hollow bodies).

## 3. Decompilation

```text
Vineflower 1.12.0 --remove-synthetic=false --classpath <193 entries>
```

* `--remove-synthetic=false` is mandatory: without it the Kotlin `$default` bridges are dropped and
  the tree explodes from ~40 to 325 compile errors.
* The full 193-entry classpath (Minecraft, Forge, GTCEu, AE2, LDLib, Kotlin stdlib, …) is mandatory:
  with a partial classpath Vineflower emits raw `Object`/`Entry` everywhere.
* Output: 656 `.java` files, one per top-level class, preserving the original package layout.

Alternative decompilers were evaluated and rejected: CFR 0.152 (invalid syntax around Kotlin facade
packages), Procyon 0.6.0 (loses `invokedynamic` lambda captures), Vineflower
`--lambda-to-anonymous-class=true` (emits illegal syntax), JADX (hangs on this input).

## 4. Mechanical repair

The decompiler output needed mechanical fixes, driven down in stages
(compile-error count: 325 → 215 → 186 → 164 → 130 → 117 → 102 → 12 → 0). Categories:

* erased generics restored from the original bytecode signatures (`javap`),
* lost Kotlin lambda captures (`invokedynamic`) reconstructed from `javap -c`,
* anonymous classes that Vineflower gave constructor arguments (illegal in Java) rewritten,
* synthetic `$default` bridges retyped,
* `$assertionsDisabled` synthetic fields removed (they collide with the compiler-synthesised symbol
  once `assert` statements are present),
* a handful of dropped local-variable initialisers recovered from the bytecode.

## 5. Kotlin-origin sources

37 of the recovered classes come from Kotlin source files. They were reconstructed as Java, using
the bytecode as the source of truth for lambda captures and bridge signatures. The Kotlin-shaped
view of those same files is kept in `docs/kotlin-source-reference/` for reading only — it is
decompiler output and does not compile.

## 6. Verification

See `BUILD-AND-VERIFY.md`. In short: `./gradlew clean build` succeeds and the produced jar contains
839 classes against the official artifact's 836, the difference being anonymous-class naming plus
the `GTOLibMod` entry class.

## 7. Tool inventory

| Tool | Version / location |
| --- | --- |
| JDK (build) | Java 21 |
| JDK (inspection) | Java 25 `javap` |
| Vineflower | 1.12.0 |
| `indy.py` | extracts method signatures + `invokedynamic` capture types from `javap -c` output |

