package dev.vive.kdelauncher.domain.usecase

object LocalHeuristics {
    private val exact = mapOf(
        // Social
        "com.whatsapp"                              to Pair("social",       "message-circle"),
        "org.telegram.messenger"                    to Pair("social",       "message-circle"),
        "com.instagram.android"                     to Pair("social",       "heart"),
        "com.discord"                               to Pair("social",       "users"),
        "com.twitter.android"                       to Pair("social",       "at-sign"),
        "com.linkedin.android"                      to Pair("social",       "briefcase"),
        "com.reddit.frontpage"                      to Pair("social",       "message-square"),
        "org.joinmastodon.android"                  to Pair("social",       "globe"),
        "xyz.blueskyweb.app"                        to Pair("social",       "cloud"),
        "com.instagram.barcelona"                   to Pair("social",       "at-sign"),

        // Media / Streaming
        "com.spotify.music"                         to Pair("media",        "music"),
        "com.google.android.youtube"                to Pair("media",        "play-circle"),
        "com.netflix.mediaclient"                   to Pair("media",        "video"),
        "com.amazon.avod.thirdpartyclient"          to Pair("media",        "video"),
        "com.disney.disneyplus"                     to Pair("media",        "video"),
        "com.hbo.hbonow"                            to Pair("media",        "video"),
        "tv.twitch.android.app"                     to Pair("media",        "video"),
        "com.crunchyroll.crunchyroid"               to Pair("media",        "video"),
        "com.soundcloud.android"                    to Pair("media",        "music"),

        // Productivity
        "com.google.android.gm"                     to Pair("productivity", "mail"),
        "com.microsoft.office.outlook"              to Pair("productivity", "mail"),
        "com.slack"                                 to Pair("productivity", "hash"),
        "com.notion.android"                        to Pair("productivity", "file-text"),
        "com.todoist"                               to Pair("productivity", "check-square"),
        "com.ticktick.task"                         to Pair("productivity", "check-square"),
        "com.google.android.keep"                   to Pair("productivity", "sticky-note"),

        // Browsers
        "org.mozilla.fenix"                         to Pair("browsers",     "globe"),
        "com.brave.browser"                         to Pair("browsers",     "shield"),

        // Development
        "com.termux"                                to Pair("development",  "terminal"),
        "org.connectbot"                            to Pair("development",  "terminal"),
        "org.fdroid.fdroid"                         to Pair("development",  "package-2"),
        "com.aurora.store"                          to Pair("development",  "package-2"),
        "app.revanced.manager.flutter"              to Pair("development",  "code-2"),
        "dev.imranr.obtainium.fdroid"               to Pair("development",  "git-branch"),

        // Games
        "com.supercell.clashofclans"                to Pair("games",        "swords"),
        "com.miHoYo.GenshinImpact"                  to Pair("games",        "gamepad-2"),
        "com.valvesoftware.android.steam.community" to Pair("games",        "gamepad-2"),

        // Travel
        "com.google.android.apps.maps"              to Pair("travel",       "map-pin"),
        "com.ubercab"                               to Pair("travel",       "car"),
        "com.booking"                               to Pair("travel",       "map-pin"),
        "com.airbnb.android"                        to Pair("travel",       "home"),
        "com.didi.passenger"                        to Pair("travel",       "car"),
        "com.cabify.rider"                          to Pair("travel",       "car"),

        // Finance / Wallets (LATAM focus)
        "com.bcp.innovacxion.yapeapp"               to Pair("finance",      "wallet"),
        "com.paypal.android.p2pmobile"              to Pair("finance",      "banknote"),
        "com.coinbase.android"                      to Pair("finance",      "coins"),
        "org.monero.wallet"                         to Pair("finance",      "shield-dollar"),
        "ar.com.uala"                               to Pair("finance",      "wallet"),
        "com.nubank"                                to Pair("finance",      "wallet"),
        "com.mercadopago.wallet"                    to Pair("finance",      "wallet"),
        "com.nequi"                                 to Pair("finance",      "wallet"),

        // Utilities
        "com.kunzisoft.keepass.libre"               to Pair("utilities",    "shield-check"),
        "org.kde.kdeconnect_tp"                     to Pair("utilities",    "layers"),
        "org.localsend.localsend_app"               to Pair("utilities",    "hard-drive"),

        // Creativity
        "com.adobe.lrmobile"                        to Pair("creativity",   "aperture"),
        "com.canva.editor"                          to Pair("creativity",   "palette"),
        "com.simplemobiletools.gallery.pro"         to Pair("creativity",   "image"),

        // Health
        "com.google.android.apps.fitness"           to Pair("health",       "heart-pulse"),
        "com.strava"                                to Pair("health",       "activity"),
    )

    fun classify(packageName: String): Pair<String, String>? = exact[packageName]
}
