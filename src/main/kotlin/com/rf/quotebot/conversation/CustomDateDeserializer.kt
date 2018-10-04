package com.rf.quotebot.conversation

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


class CustomDateDeserializer @JvmOverloads constructor(c: Class<*>? = null) : StdDeserializer<Date>(c) {
    @Throws(IOException::class, JsonProcessingException::class)
    override fun deserialize(jsonParser: JsonParser, deserializationContext: DeserializationContext): Date {
        val date = jsonParser.getText()
        try {
            return simpleDateFormat.parse(date)
        } catch (e: ParseException) {
            throw RuntimeException(e)
        }

    }

    companion object {
        private val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss 'GMT'")
    }
}