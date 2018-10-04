package com.rf.quotebot.event

import com.rf.quotebot.MessageSender
import org.slf4j.LoggerFactory
import java.util.*

class EventNotifier(private val messageSender: MessageSender,
                    private val chatId: Long,
                    private val eventRepository: EventRepository) {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val timers = HashMap<Int, Timer>()
    fun start() {
        logger.info("Starting Event notifier for chatId $chatId")
        eventRepository.purge()
        eventRepository.list().forEach {
            timers[it.id] = schedule(it)
        }
        eventRepository.registerChangeListener {
            logger.info("Event change $it found, rescheduling")
            when (it.type) {
                EventChange.Type.ADD -> timers[it.id] = schedule(eventRepository.get(it.id))
                EventChange.Type.REMOVE -> timers[it.id]?.cancel()
            }
        }
    }

    private fun schedule(event: EventBooking): Timer {
        logger.info("Scheduling $event")
        val timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                logger.info("Notifying about $event, removing from eventRepository")
                messageSender.send(chatId, "Dags for $event om en timme!")
                eventRepository.cancel(event.id)
            }
        }, event.date.minusHours(1).toDate())
        return timer
    }

    fun stop() {
        timers.forEach {
            it.value.cancel()
        }
        timers.clear()
    }
}