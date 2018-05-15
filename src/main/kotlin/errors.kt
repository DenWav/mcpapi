package de.oceanlabs.mcp

import io.javalin.Javalin
import org.eclipse.jetty.http.HttpStatus

fun mapErrors(app: Javalin) {
    app.exception(MissingParam::class.java) { e, ctx ->
        ctx.status(HttpStatus.BAD_REQUEST_400)
        ctx.result(e.message)
    }

    app.exception(BadRequest::class.java) { e, ctx ->
        ctx.status(HttpStatus.BAD_REQUEST_400)
        ctx.result(e.message)
    }
}

class MissingParam(value: String) : Exception("$value parameter not provided") {
    override val message: String
        get() = super.message!!
}

class BadRequest(value: String) : Exception(value) {
    override val message: String
        get() = super.message!!
}
