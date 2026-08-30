package org.dizitart.example.android

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.widget.TextView
import org.dizitart.no2.Nitrite
import org.dizitart.no2.collection.Document
import org.dizitart.no2.mvstore.MVStoreModule
import java.io.File

/**
 * An ordinary Android application with a Nitrite database in it, browsable from Fanlight.
 *
 * The screen shows the pairing code, because on Android there is no console to print a banner to.
 *
 * Nothing here names a bridge class. [DebugTools] does, and it exists twice — once in `src/debug/`
 * where it starts the bridge, once in `src/release/` where it returns null. That is the release
 * guard on this platform: the release variant does not contain the artifact, so there is nothing
 * to disable, no flag to forget and nothing for R8 to be trusted with.
 */
class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = openDatabase()
        val status = DebugTools.start(this, db)
            ?: "dbinspect is not in this build"

        setContentView(TextView(this).apply {
            text = status
            textSize = 18f
            gravity = Gravity.CENTER
            setPadding(48, 48, 48, 48)
        })
    }

    private fun openDatabase(): Nitrite {
        val db = Nitrite.builder()
            .loadModule(MVStoreModule.withConfig().filePath(File(filesDir, "example.db")).build())
            .openOrCreate()

        val users = db.getCollection("users")
        if (users.size() == 0L) {
            // Small on purpose: this is the guard and the wire being demonstrated, and the
            // page-latency budget is measured against the desktop example's 50k rows.
            val batch = (0 until ROWS).map { i ->
                Document.createDocument()
                    .put("id", i)
                    .put("name", if (i == 3) "user with a ünicode name" else "user $i")
                    .put("score", i / 3.0)
                    .apply { if (i % 2 == 0) put("age", 20 + (i % 50)) }
            }
            users.insert(batch.toTypedArray())
            db.commit()
        }
        return db
    }

    private companion object {
        const val ROWS = 2_000
    }
}
