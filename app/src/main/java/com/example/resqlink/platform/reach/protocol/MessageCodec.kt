package com.example.resqlink.platform.reach.protocol

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

class MessageCodec {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true

        // sealed Payload 인식
        serializersModule = SerializersModule {
            polymorphic(Payload::class) {
                subclass(SosPayload::class)
            }
        }

        // "payload": { "type": "SOS", ... } 형태로 넣고 싶으면 classDiscriminator 바꾸면 됨
        classDiscriminator = "kind"
    }

    fun encode(envelope: MessageEnvelope): ByteArray {
        val s = json.encodeToString(envelope)
        return s.toByteArray(Charsets.UTF_8)
    }

    fun decode(bytes: ByteArray): MessageEnvelope? {
        return runCatching {
            val s = bytes.toString(Charsets.UTF_8)
            json.decodeFromString(MessageEnvelope.serializer(), s)
        }.getOrNull()
    }
}
