package org.dizitart.example.android

import android.content.Context
import org.dizitart.no2.Nitrite

/**
 * The release variant's half of the guard: it does nothing, and it names nothing.
 *
 * `debugImplementation` keeps `nitrite-bridge` out of this variant entirely, so the real
 * [DebugTools] in `src/debug/` could not compile here even if someone wanted it to. Keeping the
 * two behind one identical signature is what lets [MainActivity] call it unconditionally with no
 * reflection, no `if (BuildConfig.DEBUG)` that still has to link, and no R8 rule to trust.
 *
 * `tool/verify_release_apk.sh` greps the built APK for the protocol strings, which is the check
 * PLAN.md §6 M3 names as the one that would have caught the guard gap.
 */
object DebugTools {
    @Suppress("UNUSED_PARAMETER")
    fun start(context: Context, db: Nitrite): String? = null
}
