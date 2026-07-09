---
description: Bump version, update CHANGELOG, and publish a new nitrite-java release to Maven Central
argument-hint: "[major|minor|patch] (optional - overrides the auto-detected bump type)"
---

You are cutting a release of nitrite-java. This publishes real, permanent artifacts to Maven
Central (org.dizitart:nitrite and sibling modules) — a release **cannot be deleted or
overwritten** once it lands, only superseded by a new version. Follow every step; do not skip
the confirmation gates.

## How this repo's release pipeline actually works (verified, do not re-derive from scratch)

- **One shared version** across all 10 `pom.xml` files (root `pom.xml`, `nitrite`, `nitrite-bom`,
  `nitrite-jackson-mapper`, `nitrite-mvstore-adapter`, `nitrite-native-tests`,
  `nitrite-rocksdb-adapter`, `nitrite-spatial`, `nitrite-support`, `potassium-nitrite`). Every one
  of them has the version on line 7 (line 10 for `nitrite-native-tests`), both as the module's own
  `<version>` and as its `<parent><version>`.
- Tag format is a single **`vX.Y.Z`** tag (not per-module).
- The publish pipeline is a two-workflow chain, **not** a single workflow:
  1. `.github/workflows/build.yml` ("Build") triggers on push to a tag matching `v*` (also on
     push to `main`/`develop`/`release` branches — ignore those runs, you only care about the run
     whose `headBranch` equals your new tag). It runs Linux/macOS/Windows builds plus a GraalVM
     native-image compatibility matrix (3 OSes × 2 JDKs). **This takes 40-60 minutes.**
  2. `.github/workflows/release.yml` ("Release") triggers via `workflow_run` **only after** the
     Build run on the `v*` tag completes with `conclusion == success`. It runs `mvn deploy` to
     Maven Central using the `central` server id (Sonatype Central Publishing Portal, PGP-signed).
     This takes ~10 minutes.
  - So: push the tag → wait for Build (~45 min) → Release auto-fires → wait for Release (~10
    min) → artifacts are on Maven Central. There is no manual `mvn deploy` step and no
    `workflow_dispatch` fallback — the only trigger is the tag push.
- **Tag type matters.** Empirically, both annotated and lightweight tags have triggered this
  pipeline successfully in this repo, but use a **lightweight tag** (`git tag vX.Y.Z`, no `-a`,
  no `-m`) anyway — nitrite-flutter's identical-looking tag pattern silently failed to trigger
  its release workflow when pushed as an annotated tag, so lightweight is the proven-safe choice
  and there is no reason to risk the other form here.
- GitHub Releases are **not** created automatically by any workflow — create one yourself with
  `gh release create` after the tag exists, purely for changelog visibility. It is not required
  for the Maven Central publish, which fires from the raw tag push alone.

## Step 1 — Preflight

1. `git status --short` — must be clean. If not, stop and tell the user what's uncommitted.
2. `git fetch origin --quiet && git log --oneline main..origin/main` — must be empty (local main
   up to date). If not, stop; do not silently rebase over unknown remote commits.
3. Confirm you are on `main`.

## Step 2 — Determine the version bump

1. Find the last release tag: `git tag -l 'v*' | sort -V | tail -1`.
2. `git log <lastTag>..HEAD --oneline` to see everything since the last release.
3. Classify using Conventional Commits semantics:
   - **major**: any commit/PR indicates a breaking change — an explicit `BREAKING CHANGE:`
     footer, a `!` after the type (`fix!:`, `feat!:`), or a changelog-worthy note about an
     incompatible on-disk format or public API change (this repo has precedent: the 4.4.0
     composite-index layout change was forward-only-incompatible and got an "Upgrade Notes"
     section — that class of change is major-worthy even without a `!` marker).
   - **minor**: any commit adds new capability without breaking compatibility (`feat:`, new
     public API, new module/index type, meaningful performance work presented as a feature).
   - **patch**: everything else is bug fixes / maintenance only (`fix:`, `chore:`, `docs:`,
     `refactor:`, `test:`, `ci:`, dependency bumps).
   - Take the highest applicable level across all commits since the last tag.
4. If `$ARGUMENTS` explicitly names `major`, `minor`, or `patch`, that overrides your
   classification — but still show your own analysis first so the user can see the discrepancy.
5. Compute the new version from the last tag + bump type.
6. **Use AskUserQuestion to confirm the proposed version and bump type before touching any
   file.** Show the commit list and your reasoning. Let the user override.

## Step 3 — Bump versions

1. In all 10 `pom.xml` files, replace the old `<version>OLD</version>` with
   `<version>NEW</version>` — this single string appears exactly once per file at/near the top
   and covers both the module's own version and (for the 9 non-root poms) the `<parent>` version,
   since they're identical strings. Verify afterward with:
   `grep -rn "<version>NEW</version>" --include="pom.xml" . | grep -v target` — expect exactly 10
   hits (one per file, at the version-declaration line, not inside `<dependencyManagement>` blocks
   for unrelated third-party deps — check each hit is on the module's own `<version>` tag near the
   top of the file, not a coincidental unrelated match).
2. Update `CHANGELOG.md`: add a new `## Release NEW - <Month Day, Year>` section at the top,
   above the previous release's section, with `### Issue Fixes` / `### New Changes` /
   `### Performance Improvements` / `### Security Fixes` / `### Upgrade Notes` subsections as
   applicable (mirror the existing style further down the file). Draft entries from the commit
   log and any GitHub issue numbers referenced in commit messages. Show the drafted section to
   the user before proceeding — this is the one part of the release that benefits most from a
   human read-through.

## Step 4 — Verify before committing

Run `mvn -q -pl nitrite -am test` at minimum (full reactor test if time allows:
`mvn -q test`). Do not proceed past a red build. If tests fail, stop and report — do not
weaken or skip tests to force a release through.

## Step 5 — Commit and push

1. Commit the version bump as its own `chore: update version to NEW in POM files and changelog`
   commit (matches repo convention — see `git log --oneline -- pom.xml`). Do not bundle it with
   unrelated changes.
2. `git fetch origin --quiet` once more and confirm no new divergence, then `git push origin
   main`.

## Step 6 — Tag and trigger the release

**This is the point of no return** — once Maven Central accepts the deploy, it cannot be undone.
Use AskUserQuestion one more time to get explicit go-ahead before this step, showing the exact
tag and version about to be published.

1. `git tag vNEW` (lightweight — no `-a`/`-m`).
2. `git push origin vNEW`.
3. Within ~30s, confirm the Build workflow picked it up:
   `gh run list --workflow=build.yml --limit 3` — look for a run with `headBranch == vNEW`. If
   none appears within a couple of minutes, something is wrong (check tag type with
   `git cat-file -t vNEW` — must print `commit`); do not just wait indefinitely, investigate.

## Step 7 — Wait and verify

1. Poll (don't busy-loop; use ScheduleWakeup with ~5-10 minute intervals, or Monitor for a
   polling loop) `gh run list --workflow=build.yml --limit 3` until the `vNEW` run completes.
   Expect ~45-60 minutes. If it fails, inspect with `gh run view <id> --log-failed`, report to
   the user, and stop — do not push a new tag over the same version to retry; fix the underlying
   issue, bump to the next patch version, and start over from Step 2.
2. Once Build succeeds, poll `gh run list --workflow=release.yml --limit 3` for the
   `workflow_run`-triggered run (head branch shows as `main` for this trigger type — identify it
   by recency/timing right after Build completed, not by tag name). Expect ~10 minutes.
3. If Release fails, inspect with `gh run view <id> --log-failed`. A failure here after Build
   succeeded is usually a signing/credentials issue, not a code issue — do not attempt to
   re-trigger by pushing another tag; report exactly what failed and let the user decide.
4. Once Release succeeds, verify on Maven Central (allow a few minutes for CDN sync):
   `curl -s https://repo1.maven.org/maven2/org/dizitart/nitrite/maven-metadata.xml | grep NEW`.

## Step 8 — Create the GitHub Release

**Always use the CHANGELOG.md content as the release notes — never `--generate-notes`.** This
repo's past releases (e.g. v4.4.1, v4.4.2) all use the curated CHANGELOG section verbatim as the
release body; `--generate-notes` instead produces a bare auto-generated PR list, which is
inconsistent with every existing release here and must not be used.

1. Extract exactly the new section you added to `CHANGELOG.md` in Step 3 — everything between
   the `## Release NEW - ...` heading and the next `## Release` heading, excluding both heading
   lines — into a temp file, e.g.:
   `awk '/^## / && ++c==1 {next} /^## / && c==1 {exit} c==1 {print}' CHANGELOG.md > /tmp/notes.md`
2. `gh release create vNEW --repo nitrite/nitrite-java --title vNEW --notes-file /tmp/notes.md`
3. Verify: `gh release view vNEW --repo nitrite/nitrite-java --json body -q '.body'` should print
   the same prose you drafted in Step 3, not a "What's Changed" / "Full Changelog" auto-generated
   block.

## Step 9 — Report

Summarize: old → new version, bump type and why, Build/Release run outcomes and durations,
Maven Central confirmation, GitHub Release URL.
