# GTOSeal — reverse-engineered source of `gto-seal-runtime-1.0.jar`

GTOSeal is the encryption/decryption (seal) runtime of GTOCore. It is shipped as a separate
ModLauncher service jar next to `gtocore-forge-*.jar`, and it is what makes the `com.gtolib`
classes of a release jar unreadable by a plain decompiler.

This directory holds the reconstructed Java source of that runtime, a Gradle project that
compiles it, and the offline driver/probe tooling used to study it.

## Provenance

| Item | Value |
| --- | --- |
| Input artifact | `Required-development-files--Global/GregTech-Odyssey-file-0918/gto-seal-runtime-1.0.jar` (identical copy in the test client `mods/`) |
| Input size | 3,129,202 bytes, 23 zip entries |
| Decompiler | Vineflower 1.12.0 (CFR 0.152 cross-checked) |
| Recovered classes | 5 / 5 |
| Build toolchain | Java 21 (`options.release = 17`, matching the original `Created-By: 17.0.19`) |

The jar contains no obfuscated class names, so no deobfuscation step was required. Only
`gto/native0/plugins/GTOProvider.java` still carries ProGuard-style field names
(`ㅤࣦ࣮ࣶ` …); they are kept verbatim because they are the real field names of the shipped class.

## Layout

```
GTOSeal/
├── build.gradle / settings.gradle / gradle.properties / gradlew(.bat) / gradle/wrapper
├── src/main/java/gto/native0/GTOServicesInit.java          # slot 0 entry point
├── src/main/java/gto/native0/plugins/GTOServices.java      # ModLauncher ITransformationService
├── src/main/java/gto/native0/plugins/GTOPlugin.java        # ModLauncher ILaunchPluginService
├── src/main/java/gto/native0/plugins/GTOProvider.java      # native method host (decrypt / init_gtolib)
├── src/main/java/native0/Loader.java                       # unpacks and System.load()s the platform library
├── src/stub/java/native0/hidden/Hidden0.java               # compile-time stub, never packaged
├── src/main/resources/META-INF/services/...                # SPI registration
├── src/main/resources/native0/{x64-windows.dll,x64-linux.so,x64-macos.dylib,arm64-macos.dylib}
├── src/main/resources/pack.mcmeta
├── license/LICENSE1.txt, license/LICENSE2.txt              # third-party native library notices
└── tools/                                                  # offline driver / probe reference code
```

## Build

```
cd GTOSeal
./gradlew clean build
```

Verified on 2026-09-23 with Java 21.0.5 and network access enabled. The build succeeds and
produces `build/libs/gto-seal-runtime-1.0.jar` with the same entry set as the original
artifact (5 classes + 4 native libraries + `pack.mcmeta` + the ModLauncher SPI file).

## Verified behaviour (mechanism summary)

These facts were established empirically; see `tools/README.md` for the probe programs.

1. **Not a `-javaagent`.** `x64-windows.dll` exports exactly one symbol (`JNI_OnLoad`); there is
   no JVMTI entry point. The library obtains the JVMTI environment through HotSpot internal
   structures and registers its own `ClassFileLoadHook`.
2. **Protected classes are hollow shells.** Their methods are rewritten to `native`, and their
   `<clinit>` / constructors are replaced by a hard-coded
   `throw new UnsatisfiedLinkError("Not Impl")`. The real bytecode lives in
   `native0/native/<name>.prod.bin` inside the release jar and is swapped in at class-definition
   time.
3. **Load order is slot 0 → 3 → 2**: `native0.Loader`, then `gto.native0.GTOServicesInit` (slot 0),
   then `gto.native0.plugins.GTOServices` (slot 3), then `gto.native0.plugins.GTOProvider` (slot 2).
   Every one of them follows the same three-step `<clinit>`:
   `Loader.registerNativesForClass(slot, ThisClass.class)` followed by
   `Hidden0.special_clinit_<slot>_<n>(ThisClass.class)`.
4. **`native0.hidden.Hidden0` is not a class file.** The native side defines it into the bootstrap
   class loader with JNI `DefineClass`. It exposes four `special_clinit_*` natives and four
   `invokereverse*` bridges. That is why this project needs a compile-time stub.
5. **Key material comes from the jar signature.** The native side reads
   `SecureJar.ModuleDataProvider.verifyAndGetSigners(...)`, takes
   `signers[0].getSignerCertPath().getCertificates().get(0).getPublicKey().getEncoded()` and
   digests it with `MessageDigest("SHA-256")`. The DLL contains no AES S-box, no ChaCha/Salsa
   constants and no AES-NI instructions, so the payload cipher is a bespoke ARX stream cipher.
6. **The payload cipher is length-preserving.** `native0/native/**.prod.bin` has the same length
   distribution as the decrypted classes, so it is a stream cipher, not a block cipher.
7. **The manifest carries the seal parameters**:

   ```
   GTO-Seal         = 1
   GTO-Seal-Salt    = -6851021872117151357     # gtocore 26.8.3-techtree
   GTO-Seal-Version = 0
   NTPT-Salt        = -6851021872117151357
   NTPT-Version     = 0
   ```

   The values differ per build; read them from the jar being analysed.

## Known limitations

- **No native source.** The four platform libraries are compiled artifacts; only the Java side can
  be reconstructed. `src/main/resources/native0/*` are the original binaries, byte for byte.
- **The rebuilt jar is not signature-identical to the original.** Recompiling changes class sizes
  and the manifest, so the `GTOKEY.SF` / `GTOKEY.EC` signature pair of the original jar cannot be
  reproduced. The seal runtime verifies the signature of the *protected* jar it is applied to, so
  this only matters if a rebuilt seal runtime is expected to accept a signed payload.
- **`GTOServicesInit.LOGGER` is written by the native `<clinit>`.** The decompiled class declares
  it as a blank `static final` field, which javac rejects. It is declared
  `public static final Logger LOGGER = null;` here: the field initialiser runs before the native
  `special_clinit_0_10(...)` call in the same static initialiser, so the native value still wins.
- `GTOPlugin.handlesClass(Type, boolean)` intentionally throws
  `IllegalStateException("Outdated ModLauncher")`; the two-argument overload must never be called.

## Third-party notices

`license/LICENSE1.txt` and `license/LICENSE2.txt` are the notices shipped inside the original jar
and are reproduced unchanged.
