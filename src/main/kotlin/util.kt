package de.oceanlabs.mcp

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.javalin.ApiBuilder
import io.javalin.Context
import org.jdbi.v3.core.Jdbi
import java.lang.reflect.Method
import kotlin.system.measureTimeMillis

const val EMPTY = "{}"

val gson: Gson = GsonBuilder().setPrettyPrinting().create()

val Any?.json: String
    get() {
        return if (this == null) {
            EMPTY
        } else {
            gson.toJson(this)
        }
    }

lateinit var config: DatabaseConfig

val jdbi: Jdbi by lazy { Jdbi.create(config.dataSource) }

operator fun StringBuilder.plusAssign(text: String) {
    this.append(text)
}

inline fun Context.jsonResult(func: () -> Any?) {
    var obj: Any? = null
    val time = measureTimeMillis {
        obj = func()
    }
    this.result(CompleteRequest(time, (obj as? Collection<*>)?.size, obj).json)
    this.contentType("application/json")
}

private val paramRegex = Regex(":[^/]+")

val prefixPath: Method =
    ApiBuilder::class.java.getDeclaredMethod("prefixPath", String::class.java).apply { isAccessible = true }

private fun getFullPath(path: String) = prefixPath(null, path) as String

fun get(path: String = "", func: (String, Context) -> Unit) {
    val fullPath = getFullPath(path)

    val paramList = paramRegex.findAll(fullPath).map { it.value }.toList()
    val param1 = paramList[0]

    ApiBuilder.get(path) { ctx ->
        val p1 = ctx.param(param1) ?: throw MissingParam(param1)
        func(p1, ctx)
    }
}

fun get(path: String = "", func: (String, String, Context) -> Unit) {
    val fullPath = getFullPath(path)

    val paramList = paramRegex.findAll(fullPath).map { it.value }.toList()
    val param1 = paramList[0]
    val param2 = paramList[1]

    ApiBuilder.get(path) { ctx ->
        val p1 = ctx.param(param1) ?: throw MissingParam(param1)
        val p2 = ctx.param(param2) ?: throw MissingParam(param2)
        func(p1, p2, ctx)
    }
}

data class CompleteRequest(
    val requestTime: Long,
    val resultCount: Int?,
    val data: Any?
)

fun splitName(name: String): Pair<String?, String> {
    val parts = name.split('.')
    return when {
        parts.size == 1 -> null to parts[0]
        parts.size == 2 -> parts[0] to parts[1]
        else -> throw BadRequest("Name not valid: $name")
    }
}

fun versionType(version: String) =
    if (version.count { it == '.' } == 1) {
        "both"
    } else {
        "mcOnly"
    }
