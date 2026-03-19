package dev.nixgeek.bliki.lib.log

fun buildLogMessage(message: String?, vararg values: Pair<String, Any?>): String =
    buildString {
        val hasList = values.isNotEmpty()
        if (!message.isNullOrBlank()) {
            append(message)
            if (hasList) {
                append(" ")
            }
        }
        if (hasList) {
            append("[")
            append(values.joinToString { (name, value) -> "$name: $value" })
            append("]")
        }
    }
