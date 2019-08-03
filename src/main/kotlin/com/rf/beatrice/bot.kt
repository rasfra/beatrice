package com.rf.beatrice

import java.io.FileInputStream
import java.util.*

fun main(args: Array<String>) {
    val p = Properties()
    p.load(FileInputStream(System.getProperty("user.home") + "/.beatrice.properties"))
    val config = Config(
            p.getProperty("beatrice.token.telegram"),
            p.getProperty("beatrice.chatid").toLong(),
            "jdbc:h2:file:${p.getProperty("database.path")}",
            p.getProperty("botify.token.telegram"),
            p.getProperty("botify.spotify.clientid"),
            p.getProperty("botify.spotify.secret"),
            p.getProperty("botify.spotify.refreshtoken"),
            p.getProperty("botify.playlistId")

    )
    BotRunner(config).run()

}
