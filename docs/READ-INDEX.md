# Read Index

Files inspected as restoration evidence. Scoped discovery searches are recorded by pattern; every
file whose content was used as evidence is listed individually.

## Official reference material

| Path | Used for |
| --- | --- |
| `GregTech-Odyssey-file-0918/GTOCore-Main/gradle/scripts/gtolib-pipeline.gradle` | Official GTOLib build pipeline: source location, dev/release tracks, seal metadata |
| `GregTech-Odyssey-file-0918/GTOCore-Main/gradle/scripts/gtolib-mod-stub/src/com/gtolib/GTOLibMod.java` | Official `GTOLibMod` source added to this tree |
| `GregTech-Odyssey-file-0918/GTOCore-Main/dependencies.gradle` | Origin of `dependencies.gradle`; showed the `jarJar(...)` wrappers belong to GTOCore |
| `GregTech-Odyssey-file-0918/GTOCore-Main/libs/gtolib-protected.jar` | MANIFEST (seal metadata) and entry list |
| `GregTech-Odyssey-file-0918/GTOCore-Main/libs/gtolib-release.jar` | Entry list comparison (release track) |
| `GregTech-Odyssey-file-0918-from-public/gtolib-26.8.3-techtree-sources.jar` | Community artifact: unpacked tree, entry list, build test |

## Restored tree files edited or read as evidence

| File | Reason |
| --- | --- |
| `src/main/java/com/gtolib/ae2/crafting2/model/ComputingComponent.java` | Generic signature restored from bytecode |
| `src/main/java/com/gtolib/ae2/crafting2/logic/MainLogic.java` | Dropped initialiser and raw-collection fixes (`javap -c` of `executeV2`) |
| `src/main/java/com/gtolib/ae2/crafting2/utils/AE2CraftingTranslation.java` | Varargs/`withStyle` overload repair |
| `src/main/java/com/gtolib/api/gui/ktflexible/FlexibleContainerExtendedKt.java` | Kotlin lambda captures (`$spacing`, `$init`, `$between`, `$width`) |
| `src/main/java/com/gtolib/api/gui/ktflexible/FlexibleStandardWidgetKt.java` | `$default` bridge lambda typing |
| `src/main/java/com/gtolib/api/annotation/dynamic/DynamicInitialData.java` | Synthetic `$assertionsDisabled` removal |
| `src/main/java/com/gtolib/utils/SortUtils.java` | Same synthetic field |
| `src/main/java/com/gtolib/api/registries/ScanningClass.java` | Same synthetic field |
| `src/main/java/com/gtolib/GTOLibMod.java` | Added official stub |

## Build configuration read

`build.gradle`, `settings.gradle`, `dependencies.gradle`, `gradle.properties`,
`gradle/scripts/moddevgradle.gradle`, `gradle/scripts/repositories.gradle`, generated `cp.txt`.

## Bytecode evidence

`javap -c -p` over the captured dev-track classes (836 classes), in particular
`com.gtolib.ae2.crafting2.logic.MainLogic#executeV2` and the Kotlin facade classes listed above.

## Kotlin restoration evidence (2026-09-23)

| Path | Used for |
| --- | --- |
| `<client>/mods/gtocore-forge-1.20.1-26.8.3-techtree.jar` | Sealed ABI of the 932 `com/gtolib/**` classes: names, descriptors, visibility and the `internal` setter name suffix the decrypted capture loses |
| `seal-harness-2683/captured/com/gtolib/**` | Real bodies for the Kotlin facades; `FlexibleStandardWidgetKt$iconButton$button$1` confirmed `Intrinsics.checkNotNull` before both `ImmutableList.of` and `writeComponent` |
| `GTOCore-Main` (git, 12 refs / 536 commits) | History scan: `GTOLib` and `GTOSeal` are gitlinks in every ref, and no commit ever carried GTOLib Kotlin source (`com/gtolib` appears only as the `GTOLibMod` stub) |
| `GTOLib/docs/kotlin-source-reference/**` (37 files) | Vineflower Java view used as the base for the Kotlin transcriptions |
| `GTOCore/src/main/java/com/gtocore/**/*.kt` (81 paths in git history) | GTOCore's own Kotlin call sites — the acceptance surface for the restored DSL |

## Scoped searches

| Pattern | Scope |
| --- | --- |
| `rg -l 'assertionsDisabled' <project>/src/main/java` | locate synthetic-field collisions |
| `rg -n 'jarJar' <GTOCore-Main>/dependencies.gradle` | confirm GTOCore ownership of jarJar |
| `find <GTOCore-Main> -iname '*gtolib*'` | locate the official GTOLib source directory and pipeline scripts |

## Official release comparison (2026-09-23)

Full report: `GTOLib/docs/OFFICIAL-JAR-DIFF.md`.

| Path | Used for |
| --- | --- |
| `GTOCore/GTOLib/libs/gtocore-forge-1.20.1-26.8.3-techtree.jar` | Official release artifact, byte-identical to the client copy; baseline for the entry-level and class-level diff |
| `GTOCore/build/libs/gtocore-forge-1.20.1-26.8.3-techtree.jar` | Our clean-build artifact under comparison |
| `GTOCore-Main` refs `main`, `origin/26.8`, `origin/techtree2`, `origin/0.6.0` | Blob comparison for the five differing `com/gtocore` entries; `wireless_dimension_repeater.mbs` is byte-exact evidence that the official jar is `main`/`26.8` and this tree is `techtree2` |
| `.gradle/caches/forge_gradle/minecraft_user_repo/.../srg_to_official_1.20.1.tsrg` | Resolved `m_19749_` to `getOwner` |
| `GTOCore/Agent cache/Chatgpt/bridge-test` | javac 17/21/25 and ECJ 3.37.0 reproduction harness for the extra bridge |
| Clean-build log (scratch, not kept in the tree) | Proves the build reused the prebuilt GTOLib artifacts (`compileSources=false`, `sealPack=false`), so `com/gtolib` identity is not source-level evidence |
