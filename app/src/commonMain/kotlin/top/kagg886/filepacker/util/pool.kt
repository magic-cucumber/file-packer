package top.kagg886.filepacker.util

/**
 * ================================================
 * Author:     886kagg
 * Created on: 2025/12/19 19:03
 * ================================================
 */


import kotlinx.coroutines.channels.Channel
import okio.Closeable
import kotlin.reflect.KProperty

class AccessPool<T : AutoCloseable>(size: Int, factory: () -> T) : AutoCloseable {
    private val channel = Channel<T>(capacity = size)
    private val list = List(size) { factory() }

    init {
        list.forEach { channel.trySend(it) }
    }

    suspend fun acquire(): T = channel.receive()
    suspend fun release(resource: T) {
        if (resource !in list) {
            error("Resource not in pool")
        }
        channel.send(resource)
    }

    /**
     * 优雅使用：确保资源一定归还
     */
    suspend fun <R> peek(block: suspend (T) -> R): R {
        val resource = acquire()
        try {
            return block(resource)
        } finally {
            release(resource)
        }
    }

    override fun close() {
        for (res in list) {
            res.close()
        }
    }
}

class OkioAutoClose<T: Closeable>(private val value: T) : AutoCloseable {
    override fun close() = value.close()
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T = value
}


fun <T : Closeable> T.toAutoClose() = OkioAutoClose(this)
