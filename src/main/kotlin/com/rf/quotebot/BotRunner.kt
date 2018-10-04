package com.rf.quotebot

import com.rf.quotebot.conversation.H2ConversationRepository
import com.rf.quotebot.event.H2EventRepository
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
        logger.info("Connectedto H2 server...")
        val conversationRepository = H2ConversationRepository(database)
        val eventRepository = H2EventRepository(database)
        val bot = Beatrice(config.telegramToken, config.chatId, conversationRepository, eventRepository)
        bot.runBlocking()
    }

}

class Config(val telegramToken: String, val chatId: Long, val dataSource: String)