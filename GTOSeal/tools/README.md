# Reference tooling — provenance

Everything in this directory was written by a previous AI collaborator in a
separate scratch workspace while investigating the GTO Seal mechanism, and is reproduced here
as reference material. It has **not** been re-verified by the current reconstruction pass; the
verified mechanism description is in the project `README.md`.

| File | Origin | Purpose |
| --- | --- | --- |
| `SEAL-MECHANISM-NOTES.md` | `<scratch>/_seal-close-gap/README.md` | Empirical notes: load order, `ModuleDataProvider` injection, classpath pitfalls |
| `SealProbe3.java` | `<scratch>/_seal-close-gap/` | Drives `init_gtolib` offline; injects a `SecureJar` `ModuleDataProvider` into `GTOProvider` |
| `SealProbe4.java` | `<scratch>/_seal-close-gap/` | Determines where decrypted bytes actually leave the native side |
| `SealProbe5.java` | `<scratch>/_seal-close-gap/` | Adds manifest salt injection |
| `SealBulk.java`, `SealBulkC.java`, `SealBulkD.java` | `<scratch>/_seal-close-gap/` | Batch drivers that register then load every protected class of a jar |
| `CaptureAgent.java` | `<scratch>/_seal-close-gap/` | `-javaagent` that captures class bytes via `retransformClasses` |
| `GtoSealDumper.java`, `GtoSealDumper2.java` | `<scratch>/Source-code/Tools/` | Attach-time agent that dumps `com/gtolib/**` bytes after the native hook is installed |
| `Attacher.java`, `Attach2.java` | `<scratch>/Source-code/Tools/` | Small attach-API helpers used by the dumper |

## Notes on the classpath

The seal runtime is loaded through ModLauncher. Driving it offline requires the Minecraft
libraries on the classpath, and two traps were documented by the previous pass:

1. Non-ASCII paths inside a Java argfile are silently skipped by the launcher.
2. Backslashes inside an argfile are treated as escapes; use forward slashes and no quotes.
