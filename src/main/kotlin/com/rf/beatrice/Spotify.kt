package com.rf.beatrice

import org.json.JSONArray
import org.json.JSONObject
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.*

class Spotify(private val clientId: String,
              private val secret: String,
              private val refreshToken: String,
              private val playlistId: String) {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private var accessToken: String = ""
    private var expiresAt = Instant.now()


    fun addToPlaylist(trackId: String) {
        logger.info("Adding $trackId to playlist $playlistId")
        authedRequest { authedHeader ->
            if (!trackAlreadyExists(trackId)) {
                val response = khttp.post(
                        url = "https://api.spotify.com/v1/playlists/$playlistId/tracks",
                        headers = authedHeader,
                        json = mapOf("uris" to listOf("spotify:track:$trackId"), "position" to "0")
                )
                if (response.statusCode != 201) {
                    throw RuntimeException("Unable to add track to playlist. Error ${response.statusCode}: ${response.text}")
                }
            } else {
                logger.info("Duplicate track $trackId, won't add")
            }
        }

    }

    private fun trackAlreadyExists(trackId: String) = getPlaylistTracks().contains(trackId)

    private fun getPlaylistTracks(): List<String> {
        return authedRequest {
            val response = khttp.get("https://api.spotify.com/v1/playlists/$playlistId",
                    headers = it,
                    params = mapOf("fields" to "tracks.items(track.id)")
            )
            if (response.statusCode in 200..299) {
                response.jsonObject.getJSONObject("tracks").getJSONArray("items")
                        .map { it as JSONObject }
                        .map { it.getJSONObject("track").getString("id") }

            } else {
                throw RuntimeException("Unable to check playlist for duplicates. ${response.statusCode}: ${response.text}")
            }
        }
    }

    private fun <T> authedRequest(call: (Map<String, String>) -> T): T {
        if (!tokenValid()) refreshToken()
        return call.invoke(mapOf("Authorization" to "Bearer $accessToken"))
    }

    private fun tokenValid() = expiresAt.isAfter(Instant.now())

    private fun refreshToken() {
        logger.info("Refreshing spotify token....")
        val responseObject = khttp.post(url = "https://accounts.spotify.com/api/token",
                headers = mapOf("Authorization" to "Basic ${refreshAuth()}"),
                data = mapOf("grant_type" to "refresh_token", "refresh_token" to refreshToken)
        )
        if (responseObject.statusCode != 200) {
            throw RuntimeException("Spotify error: ${responseObject.statusCode}, ${responseObject.text}")
        }
        val json = responseObject.jsonObject
        accessToken = json.getString("access_token")
        expiresAt = Instant.now().plusSeconds(json.getLong("expires_in") - 3)
        logger.info("Spotify auth refresh success!")
    }

    private fun refreshAuth() = Base64.getEncoder().encodeToString("$clientId:$secret".toByteArray())

    operator fun JSONArray.iterator(): Iterator<JSONObject> = (0 until length()).asSequence().map { get(it) as JSONObject }.iterator()
}