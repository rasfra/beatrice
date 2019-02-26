package com.rf.beatrice

import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException

fun main(args: Array<String>) {
    val opts = Options()
            .addRequiredOption("t", "token", true, "Telegram API token")
            .addRequiredOption("cid", "chatId", true, "Telegram group chat id")
            .addRequiredOption("db", "database", true, "path to H2 database file, e.g. ~/quotebot for home folder")

    val parser = DefaultParser()
    try {
        val cmd = parser.parse(opts, args)
        val db = cmd.getOptionValue("db")
        val config = Config(
                cmd.getOptionValue("t"),
                cmd.getOptionValue("cid").toLong(),
                "jdbc:h2:file:$db"
        )
        BotRunner(config).run()
    } catch (e: ParseException) {
        e.printStackTrace()
    }
}
