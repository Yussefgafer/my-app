package com.chatex.app.util

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

/**
 * Interface for objects that can be serialized to and from JSON
 */
interface JsonSerializable {
    /**
     * Convert the object to a JSON string
     */
    fun toJson(): String {
        return Gson().toJson(this)
    }

    /**
     * Convert the object to a formatted JSON string
     */
    fun toPrettyJson(): String {
        return GsonBuilder().setPrettyPrinting().create().toJson(this)
    }
}

/**
 * Extension function to convert any object to JSON string
 */
fun Any.toJsonString(): String = Gson().toJson(this)

/**
 * Extension function to convert JSON string to object
 */
inline fun <reified T> String.fromJson(): T? {
    return try {
        Gson().fromJson(this, object : TypeToken<T>() {}.type)
    } catch (e: Exception) {
        null
    }
}

/**
 * Extension function to convert JSON string to object with type
 */
inline fun <reified T> String.fromJson(type: Type): T? {
    return try {
        Gson().fromJson(this, type)
    } catch (e: Exception) {
        null
    }
}

/**
 * Extension function to convert JSON string to list of objects
 */
inline fun <reified T> String.fromJsonList(): List<T>? {
    return try {
        val type = object : TypeToken<List<T>>() {}.type
        Gson().fromJson(this, type)
    } catch (e: Exception) {
        null
    }
}

/**
 * Extension function to convert Map to JSON string
 */
fun Map<*, *>.toJsonString(): String = Gson().toJson(this)

/**
 * Extension function to convert JSON string to Map
 */
fun String.toMap(): Map<String, Any>? {
    return try {
        val type = object : TypeToken<Map<String, Any>>() {}.type
        Gson().fromJson(this, type)
    } catch (e: Exception) {
        null
    }
}
