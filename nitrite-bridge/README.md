# nitrite-bridge

Inspect a running Nitrite database from a desktop client, over a paired, loopback-by-default
connection.

This artifact is the **Nitrite adapter only**. The wire protocol, pairing, transport and the
release guard are in `org.dizitart:dbinspect-bridge`, which knows about no database at all — so a
JVM developer inspecting an H2, MapDB or JDBC database takes that artifact and writes their own
adapter, and nothing about that path routes through a Nitrite repository.

```java
BridgeServer bridge = DbInspect.start(
    DbInspect.options("my_app", List.of(
        NitriteAdapter.builder(db, "main", "app data")
            .repositories(orders, customers)
            .build())));
```

## Two things to know before you ship it

**Declare `INTERNET`.** A bridge binds a socket; on Android, without the permission it fails with
a `SecurityException` that reads like a bug in us.

**Keep it out of your release build.** Everything below is off unless you turn it on, but the
strongest guard is the artifact not being there at all:

- **Desktop** — `provided` scope, *and* `-Ddbinspect.bridge.enabled=true` at launch. Two signals,
  neither of which survives into a production launch script by accident.
- **Android** — `debugImplementation`. An application has no command line, so the system property
  can never be set and a guard requiring it could never be satisfied; the packaging is the guard,
  which is a stronger property rather than a weaker one. R8 shrinking is **not** the guard.

Both example applications carry a script that greps the built artifact for the protocol strings,
and both build twice so the check has a negative control.

## Capabilities, and what is off by default

`edit`, `snapshot` and `regex` are off unless you asked for them on that adapter — `allowWrite`,
`allowSnapshot`, `allowRegex` on the builder. What is off is *absent from the reported
capabilities*, not merely refused at call time, so the client greys it out rather than offering
something that will fail.

**A row is addressed by `_id`.** With `allowWrite(true)`, `updateRow` and `deleteRow` take the
value the grid showed in that column — a `long` here, and the bracketed `[1755…]NO₂` rendering is
accepted too, so an id pasted from another runtime's grid works. `_id` inside an update's `values`
is refused: Nitrite merges an update document, so it would rewrite the identity of the row it just
matched. An update is partial, and `changes: 0` means the row was not there, which is an answer
rather than an error.

`capabilities.filterOps` reports what this implementation actually has. Two notes on it:

- **`exists` needs nitrite 5.0.0**, which is the floor this artifact sets. It tests presence only:
  a field explicitly set to null is present and matches, and "does not have the field" is `not`
  around it, never `exists` with `value: false`.
- **`text` needs a full-text index on the field.** Nitrite's own query planner refuses a text
  filter it cannot serve from an index, and `filterOps` is a flat operator list that cannot say
  "text, but only on `bio`". The adapter refuses per field, with a message that names the fix.

## Repositories are handed in; collections are discovered

`listRepositories()` answers with entity names, and an entity name is not a `Class`. Turning one
back into a type means loading a class named by a paired client, and the underlying document
collection cannot be reached by name either — `getCollection` refuses a name a repository owns,
and re-opening the map behind one would give you a second collection object with its own event
bus, so `watch` would silently never fire.

So pass the repositories you want inspected. One that was not passed in is not listed, rather than
listed and unopenable.

## Building it

`dbinspect-bridge` is not on Maven Central yet, so this module is deliberately **not** in
`nitrite-java`'s `<modules>` — a clean checkout could not resolve it and the whole reactor would
fail. Install the core, then build this on its own:

```bash
mvn -f <dbinspect>/jvm/dbinspect-bridge/pom.xml install
mvn -f nitrite-bridge/pom.xml test
```

## Examples

| Path | What it is |
|---|---|
| `example/` | a plain `java -jar` desktop application, and `tool/verify_release_jar.sh` |
| `example-android/` | a native Android (Kotlin) application, and `tool/verify_release_apk.sh` |

Both are browsable from the desktop client, and the dbinspect conformance suite passes unmodified
against either. `tool/run_reference_bridge.sh` starts a bridge over a memory, MVStore or RocksDB
database for the suite to be pointed at.
