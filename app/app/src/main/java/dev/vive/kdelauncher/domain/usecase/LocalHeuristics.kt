package dev.vive.kdelauncher.domain.usecase

import dev.vive.kdelauncher.data.model.AppCategory

object LocalHeuristics {
    private val exact = mapOf(
        // Compras
        "com.bcp.innovacxion.yapeapp"               to Pair(AppCategory.COMPRAS,      "ShoppingCart"),
        "pe.interbank.mobilebanking"                to Pair(AppCategory.COMPRAS,      "ShoppingCart"),
        "com.bbva.nxt_peru"                         to Pair(AppCategory.COMPRAS,      "ShoppingCart"),
        "ar.com.uala"                               to Pair(AppCategory.COMPRAS,      "ShoppingCart"),
        "com.nubank"                                to Pair(AppCategory.COMPRAS,      "ShoppingCart"),
        "com.mercadopago.wallet"                    to Pair(AppCategory.COMPRAS,      "ShoppingCart"),
        "com.paypal.android.p2pmobile"              to Pair(AppCategory.COMPRAS,      "ShoppingCart"),
        "com.coinbase.android"                      to Pair(AppCategory.COMPRAS,      "ShoppingCart"),
        "org.monero.wallet"                         to Pair(AppCategory.COMPRAS,      "ShoppingCart"),
        "com.nequi"                                 to Pair(AppCategory.COMPRAS,      "ShoppingCart"),

        // Multimedia
        "com.spotify.music"                         to Pair(AppCategory.MULTIMEDIA,   "Headphones"),
        "com.google.android.youtube"                to Pair(AppCategory.MULTIMEDIA,   "Headphones"),
        "com.netflix.mediaclient"                   to Pair(AppCategory.MULTIMEDIA,   "Headphones"),
        "com.amazon.avod.thirdpartyclient"          to Pair(AppCategory.MULTIMEDIA,   "Headphones"),
        "com.disney.disneyplus"                     to Pair(AppCategory.MULTIMEDIA,   "Headphones"),
        "com.hbo.hbonow"                            to Pair(AppCategory.MULTIMEDIA,   "Headphones"),
        "tv.twitch.android.app"                     to Pair(AppCategory.MULTIMEDIA,   "Headphones"),
        "com.crunchyroll.crunchyroid"               to Pair(AppCategory.MULTIMEDIA,   "Headphones"),
        "com.soundcloud.android"                    to Pair(AppCategory.MULTIMEDIA,   "Headphones"),

        // Herramientas (productivity, utilities, dev, travel, browsers)
        "com.google.android.gm"                     to Pair(AppCategory.HERRAMIENTAS, "Build"),
        "com.microsoft.office.outlook"              to Pair(AppCategory.HERRAMIENTAS, "Build"),
        "com.slack"                                 to Pair(AppCategory.HERRAMIENTAS, "Build"),
        "com.notion.android"                        to Pair(AppCategory.HERRAMIENTAS, "Build"),
        "com.todoist"                               to Pair(AppCategory.HERRAMIENTAS, "Build"),
        "com.ticktick.task"                         to Pair(AppCategory.HERRAMIENTAS, "Build"),
        "com.google.android.keep"                   to Pair(AppCategory.HERRAMIENTAS, "Build"),
        "org.mozilla.fenix"                         to Pair(AppCategory.HERRAMIENTAS, "Build"),
        "com.brave.browser"                         to Pair(AppCategory.HERRAMIENTAS, "Build"),
        "com.termux"                                to Pair(AppCategory.HERRAMIENTAS, "Build"),
        "org.connectbot"                            to Pair(AppCategory.HERRAMIENTAS, "Build"),
        "org.fdroid.fdroid"                         to Pair(AppCategory.HERRAMIENTAS, "Build"),
        "com.aurora.store"                          to Pair(AppCategory.HERRAMIENTAS, "Build"),
        "app.revanced.manager.flutter"              to Pair(AppCategory.HERRAMIENTAS, "Build"),
        "dev.imranr.obtainium.fdroid"               to Pair(AppCategory.HERRAMIENTAS, "Build"),
        "com.google.android.apps.maps"              to Pair(AppCategory.HERRAMIENTAS, "Build"),
        "com.ubercab"                               to Pair(AppCategory.HERRAMIENTAS, "Build"),
        "com.booking"                               to Pair(AppCategory.HERRAMIENTAS, "Build"),
        "com.airbnb.android"                        to Pair(AppCategory.HERRAMIENTAS, "Build"),
        "com.didi.passenger"                        to Pair(AppCategory.HERRAMIENTAS, "Build"),
        "com.cabify.rider"                          to Pair(AppCategory.HERRAMIENTAS, "Build"),
        "com.kunzisoft.keepass.libre"               to Pair(AppCategory.HERRAMIENTAS, "Build"),
        "org.kde.kdeconnect_tp"                     to Pair(AppCategory.HERRAMIENTAS, "Build"),
        "org.localsend.localsend_app"               to Pair(AppCategory.HERRAMIENTAS, "Build"),
        "com.adobe.lrmobile"                        to Pair(AppCategory.HERRAMIENTAS, "Build"),
        "com.canva.editor"                          to Pair(AppCategory.HERRAMIENTAS, "Build"),
        "com.simplemobiletools.gallery.pro"         to Pair(AppCategory.HERRAMIENTAS, "Build"),

        // Games
        "com.supercell.clashofclans"                to Pair(AppCategory.GAMES,        "Gamepad"),
        "com.miHoYo.GenshinImpact"                  to Pair(AppCategory.GAMES,        "Gamepad"),
        "com.valvesoftware.android.steam.community" to Pair(AppCategory.GAMES,        "Gamepad")
    )

    fun classify(packageName: String): Pair<String, String>? = exact[packageName]
}
