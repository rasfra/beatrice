package com.rf.beatrice

import com.pengrad.telegrambot.TelegramBot
import com.rf.beatrice.conversation.H2ConversationRepository
import com.rf.beatrice.event.H2EventRepository
import org.h2.tools.Server
import org.jetbrains.exposed.sql.Database
import org.slf4j.LoggerFactory

class BotRunner(private val config: Config) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    fun run() {
        logger.info("Starting H2 server ${config.dataSource}")
        val server = Server.createTcpServer("-tcpAllowOthers")
        server.start()
        migrateDB(config.dataSource)

        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() {
                try {
                    Thread.sleep(200)
                    println("Shouting down H2 server ...")
                    server.stop()
                } catch (e: InterruptedException) {
                    // TODO Auto-generated catch block
                    e.printStackTrace()
                }

            }
        })
        val database = Database.connect(config.dataSource, driver = "org.h2.Driver")
        logger.info("Connected to H2 server...")

        beatrice(database)
        botify()
    }

    fun beatrice(database: Database) {
        val conversationRepository = H2ConversationRepository(database)
        val eventRepository = H2EventRepository(database)
        val bot = TelegramBot(config.beatriceToken)
        val beatrice = Beatrice(TelegramMessageSender(bot, config.chatId), conversationRepository, eventRepository)
        logger.info("Starting Beatrice...")
        bot.setUpdatesListener(beatrice::process)
    }

    fun botify() {
        val botify = Botify(Spotify(config.spotifyClientId, config.spotifyClientSecret, config.spotifyRefreshToken, config.playlistId))
        val bot = TelegramBot(config.botifyToken)
        logger.info("Starting Botify...")
        bot.setUpdatesListener(botify::process)
    }

}

class Config(val beatriceToken: String,
             val chatId: Long,
             val dataSource: String,
             val botifyToken: String,
             val spotifyClientId: String,
             val spotifyClientSecret: String,
             val spotifyRefreshToken: String,
             val playlistId: String)