package top.kagg886.filepacker.util

import com.github.ajalt.clikt.core.BaseCliktCommand

/**
 * ================================================
 * Author:     886kagg
 * Created on: 2025/12/19 16:00
 * ================================================
 */

interface Loggable {
    val debug: Boolean
}

context(scope: BaseCliktCommand<*>)
fun Loggable.info(message: Any?) = scope.echo("[Info]: $message")

context(scope: BaseCliktCommand<*>)
fun Loggable.debug(message: Any?) = if (debug) scope.echo("[Debug]: $message") else Unit

context(scope: BaseCliktCommand<*>)
fun Loggable.error(message: Any?) = scope.echo("[Error]: $message", err = true)
