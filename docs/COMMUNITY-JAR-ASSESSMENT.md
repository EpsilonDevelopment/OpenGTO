# Assessment: community `gtolib-26.8.3-techtree-sources.jar`

Question raised during the restoration: *could the community jar already be the complete source
tree, so that the sealed artifact would not have to be unsealed at all?*

Answer: **no.** The community jar was unpacked and built as a standalone project; the clean build
fails, and its content is measurably incomplete. It is a decompilation of the **production track**
(the jar that ships inside the client), so it inherits the production obfuscation.

## Build test

The jar contains 698 `.java` files directly (it is a source jar, no class files). Packaged as a
project and compiled with Java 21:

```text
BUILD FAILED — 179 errors
```

including four hard syntax errors in `RecipeBuilder.java` (lines 485, 554, 866, 915) where a switch
expression was mangled. For comparison, the tree in this directory went from 325 mechanical errors
to zero.

## Content comparison

| Check | Community jar | This tree (dev track) |
| --- | --- | --- |
| Source files | 698 | 656 (657 including `GTOLibMod`) |
| Clean build | FAILED, 179 errors | SUCCESS |
| Top-level class names vs client jar | aligned, 0 missing | 655/655 real names |
| Files whose path contains obfuscation characters | 109 (`com/gtolib/ㅤࣣ࣪ࣸ.java` — package lost, all dumped in `com/gtolib/`) | 0 |
| Files declaring obfuscated fields / methods / types | 341 / 698 | 0 |
| Files containing obfuscation characters | 399 / 698 | 0 |
| Records with an empty header (`record X() {`) — component names lost | 13 | 0 |
| Resources (mixins json, refmap, access transformer) | none | present |
| Tree hash | `23d10568653d5781` | `7e020070b9414f54` |

## Root cause

Same record, both tracks:

```java
// dev track (this tree) — real component names
record MinerConfig(AABB minerArea, long energyPerTick, int speed, int parallelMining,
                   int silkLevel, Filter<?, ?> itemFilter, Filter<?, ?> fluidFilter,
                   IDigitalMiner.FluidMode fluidMode)

// community jar (prod track) — component names replaced by obfuscated identifiers
record MinerConfig() { /* 8 private fields named ㅤࣣࣾࣳ … , constructor params var1..var9 */ }
```

The production obfuscation renames packages, classes, fields, methods and record components; the
client jar carries those names, and a decompilation of it cannot recover them.

## Conclusion

The community jar is useful as a **cross-check** for the 549 files whose class names survived
obfuscation, but it cannot serve as the source of truth. The primary source must be the dev-track
artifact (`gtolib-protected.jar`), which keeps names and only encrypts bodies.

