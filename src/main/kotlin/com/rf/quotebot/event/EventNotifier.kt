package com.rf.quotebot.event

import com.rf.quotebot.MessageSender
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.TimeUnit

class EventNotifier(private val messageSender: MessageSender,
                    private val chatId: Long,
                    private val eventRepository: EventRepository) {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val timers = HashMap<Int, Timer>()
    private val dailyReminder = Timer()
    fun start() {
        logger.info("Starting Event notifier for chatId $chatId")
        eventRepository.purge()
        eventRepository.list()
                .filter { notificationInTime(it) }
                .forEach {
            timers[it.id] = schedule(it)
        }
        eventRepository.registerChangeListener {
            logger.info("Event change $it found, rescheduling")
            when (it.type) {
                EventChange.Type.ADD -> {
                    val event = eventRepository.get(it.id)
                    if (notificationInTime(event)) {
                        timers[it.id] = schedule(event)
                    }
                }
                EventChange.Type.REMOVE -> timers[it.id]?.cancel()
            }
        }
        val dailyReminderAt = DateTime.now().withTime(10, 0, 0, 0)
        val nextDailyReminder = if (dailyReminderAt.isAfterNow) dailyReminderAt else dailyReminderAt.plusDays(1)
        dailyReminder.schedule(object : TimerTask() {
            override fun run() {
                val todaysEvents = eventRepository.list().filter { it.date.toLocalDate() == LocalDate.now() }
                val markdown = "**Event idag**\n${todaysEvents.joinToString("\n")}"
                messageSender.send(chatId, markdown)
            }
        }, nextDailyReminder.toDate(), TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS))
    }

    private fun notificationInTime(it: EventBooking) = it.date.minusHours(1).isAfterNow

    private fun schedule(event: EventBooking): Timer {
        logger.info("Scheduling $event")
        assert(event.date.minusHours(1).isAfterNow)
        val timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                sendNotification(event)
            }
        }, event.date.minusHours(1).toDate())
        return timer
    }

    fun sendNotification(event: EventBooking) {
        logger.info("Notifying about $event, removing from eventRepository")
        messageSender.send(chatId, "**${event.description()} börjar kl ${event.formatTime()}!**")
        eventRepository.cancel(event.id)
    }

    fun stop() {
        timers.forEach {
            it.value.cancel()
        }
        timers.clear()
    }
}