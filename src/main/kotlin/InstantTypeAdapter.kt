package de.oceanlabs.mcp

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.time.Instant

object InstantTypeAdapter : TypeAdapter<Instant?>() {
    override fun write(out: JsonWriter, value: Instant?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(value.toEpochMilli())
        }
    }

    override fun read(`in`: JsonReader?) =
        if (`in` == null) {
            null
        } else {
            Instant.ofEpochMilli(`in`.nextLong())
        }
}
