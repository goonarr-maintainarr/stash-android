package goonarr.stash.core.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
data class Log(
    val time: String,
    val level: LogLevel,
    val message: String
) {
    val id: String get() = time + message
}

@Serializable(with = LogLevelSerializer::class)
enum class LogLevel(val value: String) {
    TRACE("trace"),
    DEBUG("debug"),
    INFO("info"),
    WARNING("warning"),
    ERROR("error"),
    PROGRESS("progress");

    val displayName: String
        get() = when (this) {
            TRACE -> "Trace"
            DEBUG -> "Debug"
            INFO -> "Info"
            WARNING -> "Warning"
            ERROR -> "Error"
            PROGRESS -> "Progress"
        }

    companion object {
        fun fromString(value: String): LogLevel {
            return entries.find { it.value.equals(value, ignoreCase = true) }
                ?: if (value.equals("warn", ignoreCase = true)) WARNING else INFO
        }
    }
}

object LogLevelSerializer : KSerializer<LogLevel> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LogLevel", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LogLevel) {
        encoder.encodeString(value.displayName.uppercase())
    }

    override fun deserialize(decoder: Decoder): LogLevel {
        return LogLevel.fromString(decoder.decodeString())
    }
}

@Serializable
data class LogsResult(
    val logs: List<Log>? = null
)
