# minSdk 26, and what pinned it

`PLAN.md` §6 M3 asks for a floor set by measurement rather than assumption, and for the finding
that set it. Here is both. Measured 2026-08-06 with AGP 8.9.1, Gradle 9.2, `desugar_jdk_libs`
2.1.5 and core library desugaring on.

The measurement is `assembleRelease` at each candidate level: D8 refuses to dex what it cannot
desugar, so the floor is a build failure rather than a runtime surprise.

| minSdk | this app (Nitrite + MVStore + bridge) | bridge without MVStore |
|---|---|---|
| 21 | **fails to dex** | dexes |
| 24 | **fails to dex** | dexes |
| 26 | dexes | dexes |

## What sets it, and it is not what the plan expected

The plan predicted the gap would be in `java.nio` / `java.time`, since that is where desugaring
usually falls short of Java 11. It is not. Every one of the four errors is the same shape:

```
D8: MethodHandle.invoke and MethodHandle.invokeExact are only supported starting with
    Android O (--min-api 26): Lorg/h2/util/Bits;uuidToBytes(JJ)[B
```

…and all four are in **`h2-mvstore`**, the storage engine behind `nitrite-mvstore-adapter`:
`Bits.uuidToBytes`, `AES.encryptBlock`, `AES.decryptBlock`, `SHA256.getPBKDF2`. H2 reaches for
`VarHandle`/`MethodHandle` polymorphic signatures for byte access, and a polymorphic signature is
not something desugaring can rewrite — the call is resolved by the VM, so Android O is the floor
and no library version of ours moves it.

**So the floor is the store, not the bridge.** `dbinspect-bridge` (Java-WebSocket, Gson, slf4j),
`nitrite-bridge` and Nitrite's own core all dex cleanly at API 21 with desugaring on. If a future
Android target needs to go below 26, the question to ask is which store it uses, not what the
bridge does — and the answer is likely the in-memory store or a different adapter, since
`nitrite-rocksdb-adapter` carries a native library that has no Android build at all.

## What this does not prove

Dexing is not running. The app was run on an API 37 emulator (Pixel 10 Pro XL) and the M1
conformance suite passes against it unmodified over `adb forward` — 111 checks, 108 passed, 0
failed, 3 skipped. **API 26 itself has not been run**: no API 26 system image is installed on the
build machine. Dexing at 26 is a hard floor for the artifact and running at 37 shows the bridge
works on the platform, but a run on an API 26 image is what would close this properly, and it is
one `sdkmanager` download away.

`animal-sniffer` in `nitrite-bridge`'s own build checks its sources against the API 26 signature
on every `mvn test`, so a regression in the adapter itself fails there rather than here.
