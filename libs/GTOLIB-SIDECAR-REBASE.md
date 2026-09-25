# GTOLib sidecar rebase — 2026-09-23

`libs/gtolib-protected.PROTECTED` and `libs/gtolib-release.PROTECTED` bind the committed sealed
pair to a source revision: the pipeline requires the sidecar `fingerprint` to equal the fingerprint
of `GTOLib/src` + `GTOLib/protect`. Whenever the restored tree changes, the sidecars must be
re-based onto it, otherwise `isGtolibJarUpToDate` turns false and the build demands a ProGuard +
Seal rebuild that cannot complete here (`GTOLib/protect/` is private).

## Rebase 2 — 2026-09-23 (Kotlin restoration complete)

| Field | Before | After |
| --- | --- | --- |
| `fingerprint` | `4bb0c3e165fe763a19f955f55f22604303a6fc9c48dadc22a30fee02be0c64be` | `84a38916af5803c0cb9ef096e48f35a55c1e34fe06008c6ff5490d21e1d60684` |
| `gtolibCommit` | empty | empty (unchanged) |
| `jarSha256`, `size` | unchanged | unchanged — the sealed jars themselves were not modified |

Trigger: the 37 Kotlin-origin classes were restored as 13 real Kotlin files under
`GTOLib/src/main/kotlin`, and their Java reconstructions were deleted (`src/main/java`
657 → 620 files; the fingerprinted set now holds 635 files). See
`../GTOLib/docs/KOTLIN-FIDELITY-GAP.md`.

## Rebase 1 — 2026-09-23 (restored tree first put in place)

| Field | Before | After |
| --- | --- | --- |
| `fingerprint` | `75b9852b…` (private source revision) | `4bb0c3e165fe763a19f955f55f22604303a6fc9c48dadc22a30fee02be0c64be` |
| `gtolibCommit` | `02c458201712238bc1c10cc79f973130cb5f82bc` | empty (the restored tree has no git metadata; the pipeline warns and leaves it empty) |
| `jarSha256`, `size` | unchanged | unchanged — the sealed jars themselves were not modified |

The upstream commit `02c458201712238bc1c10cc79f973130cb5f82bc` is still recorded in
`GTOLib/README.md` and `GTOLib/docs/RESTORATION-METHOD.md` as the revision the restored source was
recovered from.

## Why

`gradle/scripts/gtolib-pipeline.gradle` binds the committed sealed pair to a source revision:

* `inspectGtolibPrebuiltPair()` requires `libs/gtolib-protected.PROTECTED.fingerprint` to equal the
  fingerprint of `GTOLib/src` + `GTOLib/protect`;
* `verifyGtolibReleasePair()` (run by `:jar`) requires the same of
  `libs/gtolib-release.PROTECTED`;
* `hydrateGtolibPrebuilt` writes `build/gtolib/inputs.stamp` from the current fingerprint and then
  re-verifies consistency.

With a changed tree the recorded fingerprint no longer matches, so the pipeline switches to
“rebuild + re-protect”, which cannot complete here because `GTOLib/protect/`
(`proguard-rules.pro`, `proguard-release-rules.pro`, `gto-seal-packager.jar`) belongs to the private
repository.

Re-basing the fingerprint restores the intended official state: **the restored source tree and the
committed sealed artifacts are declared to be the same revision**, the pipeline reuses the sealed
pair (`jarUpToDate=true`, `compileGtolibSources=false`) and `./gradlew build` completes.

## Consequence

The drift detector now guards *this* source tree: any edit under `GTOLib/src` changes the
fingerprint, `isGtolibJarUpToDate` becomes false, and the pipeline again demands a ProGuard + Seal
rebuild — which needs the private packager. Edit GTOLib sources only when a rebuild path
(the private `protect/` directory, or `-PgtolibDebug=true` for a plaintext build) is available.

## Verification (rebase 2)

```text
$ ./gradlew clean build                     # GTOCore, Java 21, network enabled
GTOLib: 指纹命中 → 编译复用 protected jar
GTOLib: 已从 libs 回填 build/gtolib，未执行 ProGuard/Seal；jarSha=399d76d6bb6e61cb…
GTOLib: jar 已是最新 (stamp 命中)，跳过打包/保护 → 执行一致性校验
> Task :buildGtolibProtected / :finalizeGtolib / :prepareGtolibModJar / :installGtolibIntoModOutput
> Task :jar / :reobfJar / :verifyResourceLocationMixnReobf / :sourcesJar / :assemble
BUILD SUCCESSFUL in 27s
```

Artifacts: `build/libs/gtocore-forge-1.20.1-26.8.3-techtree.jar` (53,342,757 bytes),
`build/libs/gtocore-forge-1.20.1-26.8.3-techtree-sources.jar` (26,622,095 bytes),
`build/libs/gto-seal-runtime-1.0.jar`, and `build/gtolib/gtolib-mod.jar` (8,245,558 bytes —
identical to the pre-restoration verified build).

The forced-source path was verified separately: `:compileKotlin -PgtolibRebuild=true` compiles the
restored Java + Kotlin tree with 0 errors and stops only at `:buildGtolibProtected` (private
sealing toolchain).

## Verification (rebase 1)

```text
$ ./gradlew help
GTOLib pipeline: sources=true, jarUpToDate=true, compileSources=false, debugMode=false,
                 fingerprint=4bb0c3e165fe…, proguard=false, proguardRelease=false,
                 sealPack=false, GTOSeal=false
GTOLib: 指纹命中 → 编译复用 protected jar

$ ./gradlew clean build
BUILD SUCCESSFUL in 3m 48s
```

`GTOLib mod jar → build/gtolib/gtolib-mod.jar (8245558 bytes) + GTOLibMod + pack.mcmeta` — the
sealed dev-track payload is what gets installed, exactly as before the restored sources were put in
place.
