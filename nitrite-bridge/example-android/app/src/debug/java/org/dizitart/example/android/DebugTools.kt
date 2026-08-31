package org.dizitart.example.android

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.net.wifi.WifiManager
import android.util.Log
import org.dizitart.dbinspect.BridgeAdapter
import org.dizitart.dbinspect.BridgeServer
import org.dizitart.dbinspect.DbInspect
import org.dizitart.no2.Nitrite
import org.dizitart.no2.bridge.NitriteAdapter
import java.security.SecureRandom

/**
 * Everything this application knows about dbinspect, in the debug variant only.
 *
 * `src/release/` has a file with this name that returns null and names nothing, so [MainActivity]
 * calls one signature and the packaging decides which one exists. See app/build.gradle.kts.
 *
 * **The primary path on Android is `adb forward`, not mDNS**, and that needs nothing from this
 * file — the bridge binds loopback and `adb forward tcp:<local> tcp:<bridge>` reaches it. mDNS is
 * opt-in and secondary, and it is here because PLAN.md M3 asks for `NsdManager` rather than JmDNS
 * on this platform, with a multicast lock held.
 */
object DebugTools {

    private const val TAG = "dbinspect"

    /** Set to true to also advertise on the local network. Off is the default for a reason. */
    private const val ADVERTISE_OVER_MDNS = false

    fun start(context: Context, db: Nitrite): String? {
        val adapters: List<BridgeAdapter> =
            listOf(NitriteAdapter.builder(db, "nitrite-main", "example.db").build())

        // bridgeEnabled() is true on Dalvik/ART by construction: an application has no command
        // line, so the desktop system property could never be set and a guard requiring it could
        // never be satisfied. On this platform the packaging above is the guard.
        val bridge = DbInspect.start(DbInspect.options("example_app", adapters, android.os.Build.MODEL))
            ?: return null

        if (ADVERTISE_OVER_MDNS) {
            advertise(context, bridge)
        }

        Log.i(TAG, bridge.banner())
        return buildString {
            append("dbinspect is listening\n\n")
            append("pairing code   ").append(bridge.pairingCode().value()).append('\n')
            append("port           ").append(bridge.port()).append("\n\n")
            append("adb forward tcp:").append(bridge.port())
                .append(" tcp:").append(bridge.port())
        }
    }

    /**
     * NsdManager, with a multicast lock held for as long as the advertisement is up.
     *
     * Without the lock most handsets drop multicast frames while the screen is off and some drop
     * them always, which presents as "the device never appears" with nothing in any log.
     *
     * The TXT record carries a random per-session instance name and `proto`, and nothing else —
     * PROTOCOL.md §1: appName, deviceModel, bridgeVersion and the adapter list are answered after
     * pairing, because advertising them unauthenticated hands every peer on the subnet a target
     * list and a version number to look up.
     */
    private fun advertise(context: Context, bridge: BridgeServer) {
        val wifi = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val lock = wifi.createMulticastLock("dbinspect").apply {
            setReferenceCounted(false)
            acquire()
        }

        val random = SecureRandom()
        val instance = (0 until 8)
            .map { "abcdefghijklmnopqrstuvwxyz0123456789"[random.nextInt(36)] }
            .joinToString("")

        val service = NsdServiceInfo().apply {
            serviceName = instance
            serviceType = "_dbinspect._tcp"
            port = bridge.port()
            setAttribute("proto", "1")
        }

        val manager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
        manager.registerService(service, NsdManager.PROTOCOL_DNS_SD, object : NsdManager.RegistrationListener {
            override fun onServiceRegistered(info: NsdServiceInfo) {
                Log.i(TAG, "advertising as ${info.serviceName}")
            }

            override fun onRegistrationFailed(info: NsdServiceInfo, errorCode: Int) {
                Log.w(TAG, "mDNS registration failed: $errorCode")
                lock.release()
            }

            override fun onServiceUnregistered(info: NsdServiceInfo) {
                lock.release()
            }

            override fun onUnregistrationFailed(info: NsdServiceInfo, errorCode: Int) {
                lock.release()
            }
        })
    }
}
