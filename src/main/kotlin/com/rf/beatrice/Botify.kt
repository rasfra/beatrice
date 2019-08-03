package com.rf.beatrice

import com.pengrad.telegrambot.model.Message
import com.pengrad.telegrambot.model.Update
import org.slf4j.LoggerFactory

class Botify(private val spotify: Spotify) {
    private val logger = LoggerFactory.getLogger(this::class.java)
    // spotify:track:2tt4dhpmGVstym8Hijj8gh
    // https://open.spotify.com/track/2tt4dhpmGVstym8Hijj8gh?si=mxNzAy-MQKemnLi1DXUNmA
    private val trackUrlFormat = Regex("spotify:track:([A-Za-z0-9]*)|https://open.spotify.com/track/([A-Za-z0-9]*)\\?.*")

    fun process(updates: List<Update>): Int {
        val message = updates.first().message()
        val trackUrl = getTrackUrl(message)
        if (trackUrl != null) {
            logger.info("Found spotify track: $trackUrl")
            try {
                spotify.addToPlaylist(trackUrl)
            } catch (e: Exception) {
                logger.error("Unable to add spotify track to playlist", e)
            }
        }
        return updates.first().updateId()
    }

    fun getTrackUrl(message: Message?): String? {
        return if (message != null && trackUrlFormat.containsMatchIn(message.text())) {
            val values = trackUrlFormat.find(message.text())?.groupValues!!
            // id will be in index 1 or 2 depending on url style
            if (!values[1].isNullOrBlank()) values[1] else if (!values[2].isNullOrBlank()) values[2] else null
        } else null
    }
}