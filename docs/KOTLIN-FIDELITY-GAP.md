# Kotlin Fidelity Gap — measured against GTOCore

This document records what happened when the restored GTOLib source tree was placed at the official
submodule path (`GTOCore/GTOLib`) so that GTOCore's own pipeline compiles it.

**Status: closed on 2026-09-23.** The Kotlin-origin files are restored as real Kotlin under
`src/main/kotlin`, and GTOCore's `:compileKotlin` compiles them with **0 errors** in forced-source
mode (`-PgtolibRebuild=true`).

## What the gap was

With GTOLib present only as decompiled Java, `:compileKotlin` failed with 141 errors in 7 GTOCore
Kotlin files. Every error was a call into GTOLib's Kotlin-origin DSL.

| GTOCore file | Errors |
| --- | --- |
| `com/gtocore/common/machine/multiblock/part/ae/widget/MEInputBufferPartMachineUI.kt` | 49 |
| `com/gtocore/common/machine/multiblock/part/ae/MEPatternPartMachineUIHelper.kt` | 29 |
| `com/gtocore/common/machine/multiblock/part/ae/MEPatternPartMachineKt.kt` | 22 |
| `com/gtocore/common/machine/multiblock/part/ae/MEPatternBufferPartMachineKt.kt` | 18 |
| `com/gtocore/common/item/OrganModifierBehaviour.kt` | 15 |
| `com/gtocore/api/gui/ktflexible/FlexibleExperimentalWidgetDSL.kt` | 6 |
| `com/gtocore/common/item/TesterBehaviour.kt` | 2 |

The cause was four Kotlin-only language features that a Java reconstruction cannot express:

1. extension functions with receivers (`fun LayoutBuilder<T>.vBox(...)`, `fun Style.spacing(...)`),
2. default arguments,
3. named arguments,
4. lambda-with-receiver parameter types.

## Resolution

13 files were restored as Kotlin under `GTOLib/src/main/kotlin/com/gtolib/`. Their Java
reconstructions were deleted, so each class has exactly one source of truth
(`src/main/java` 657 → 620 files, `src/main/kotlin` 0 → 13 files).

| File | Kotlin-origin classes it carries |
| --- | --- |
| `api/gui/ktflexible/FlexibleContainerDefenition.kt` | `Box`, `ContainerSizeProvider`, `HBox`, `VBox`, `HScrollBox`, `VScrollBox` |
| `api/gui/ktflexible/FlexibleContainerBuilder.kt` | `Style`, `LayoutBuilder`, `CustomBuilder`, `VBoxBuilder`, `HBoxBuilder`, `VScrollBuilder`, `HScrollBuilder`, `RootBuilder` |
| `api/gui/ktflexible/FlexibleContainerDsl.kt` | `root`, `rootFresh`, `vBox`, `hBox`, `vScroll`, `hScroll`, `FreshWidgetGroupAbstract` |
| `api/gui/ktflexible/FlexibleContainerExtended.kt` | `vBoxThreeColumn` |
| `api/gui/ktflexible/FlexibleStandardWidget.kt` | `button`, `iconButton`, `text`, `blank`, `field` |
| `api/lang/TooltipsSortedWrapper.kt` | `TooltipsSortedWrapper`, `Companion`, `SupplierWrapper` |
| `ae2/crafting2/logic/MainLogic.kt` | `MainLogic`, `ComputingComponentBuildFailedException`, `CycleTopologyBuildError`, `SendComponentToPlayerException` |
| `ae2/crafting2/model/ISuriedCraftingPlan.kt` | `GetSubComponent`, `ComputingComponent` (+5 nested), `SuriedCraftingPlan` (+`FinalPlan`, `MiddlePlan`) |
| `ae2/crafting2/model/PatternAnalyzer.kt` | `PatternAnalyzer`, `CraftingAnalysis`, `SelfIncreaseExtractionPlan` |
| `ae2/crafting2/utils/PerfLogger.kt` | `DisplayLevel`, `PerfLogger`, `Node` |
| `ae2/crafting2/utils/AECrafting2Utils.kt` | `AECrafting2Utils` |
| `ae2/crafting2/utils/AE2CraftingTranslation.kt` | `AE2CraftingTranslation` (+35 `TR_*` members) |
| `ae2/crafting2/utils/RequirementsManager.kt` | `RequirementsManager`, `RequirementFulfillmentResult` |

Evidence used, in priority order:

1. the client jar `gtocore-forge-1.20.1-26.8.3-techtree.jar`, which carries GTOLib's **sealed ABI**
   (932 `com/gtolib/**` classes whose bodies are `native` but whose names, signatures and
   visibility are intact, including the `internal` setter name suffix that the decrypted capture
   loses),
2. the `@Metadata` `d1`/`d2` payloads on the captured classes,
3. real bytecode from the decrypted capture (`seal-harness-2683/captured`),
4. the Vineflower Java view in `docs/kotlin-source-reference/`.

## Verification

```text
./gradlew :compileKotlin -PgtolibRebuild=true     # Java 21, network enabled
BUILD SUCCESSFUL
```

The remaining warnings are deliberate reproductions of the official sources, not defects:

| Warning | Where | Why it stays |
| --- | --- | --- |
| `Condition is always 'false'` ×5 | `FlexibleContainerBuilder.kt` | the official source uses `private lateinit var` + `if (x == null)` |
| `Unnecessary safe call on a non-null receiver` ×11 | `TooltipsSortedWrapper.kt` | the official source writes `it?.string`; the bytecode keeps the null check |
| `'val entries' is deprecated` | `MainLogic.kt` | `Object2LongMap.entries` is the official call site |

## Second consequence: the protection pipeline

GTOCore's `:jar` task embeds `libs/gtolib-release.jar` and then runs `verifyGtolibReleasePair()`,
which requires the release sidecar's `fingerprint` to equal the *current* `GTOLib/src` +
`GTOLib/protect` fingerprint.

**Re-based on 2026-09-23** onto the restored tree
(`fingerprint=84a38916af5803c0cb9ef096e48f35a55c1e34fe06008c6ff5490d21e1d60684`, `gtolibCommit=`
empty because the restored tree carries no git metadata). The pipeline reports
`jarUpToDate=true, compileSources=false`, reuses the committed sealed pair, and
`./gradlew clean build` in GTOCore succeeds — see `../../libs/GTOLIB-SIDECAR-REBASE.md`.

Build modes:

* default build — the sealed pair is reused; the restored sources are present for navigation and
  for the rebuild path;
* `-PgtolibRebuild=true` — compiles the restored sources (Java + Kotlin). It passes
  `:compileKotlin` and `:compileJava`, then stops at `:buildGtolibProtected` because re-sealing
  needs the private `GTOLib/protect/` toolchain (`proguard-release-rules.pro`,
  `gto-seal-packager.jar`);
* `-PgtolibDebug=true` — plaintext mode: sources compiled, ProGuard/Seal skipped, sealed payload
  not embedded (development only);
* obtaining `GTOLib/protect/` (private) would restore the full ProGuard + Seal rebuild path.
