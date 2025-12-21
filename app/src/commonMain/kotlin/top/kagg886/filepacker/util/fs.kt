package top.kagg886.filepacker.util

import okio.Buffer
import okio.FileHandle
import okio.FileSystem
import okio.IOException
import okio.Path
import okio.SYSTEM
import okio.Sink
import okio.Source
import okio.buffer
import okio.use


/**
 * ================================================
 * Author:     886kagg
 * Created on: 2025/12/19 13:52
 * ================================================
 */

fun Path.exists(): Boolean = FileSystem.SYSTEM.exists(this)
fun Path.metadata() = FileSystem.SYSTEM.metadata(this)
fun Path.list() = FileSystem.SYSTEM.list(this)
fun Path.delete() = FileSystem.SYSTEM.deleteRecursively(this, true)
fun Path.mkdirs() = FileSystem.SYSTEM.createDirectories(this)
fun Path.source() = FileSystem.SYSTEM.source(this)
fun Path.sink() = AutoClosableSink(FileSystem.SYSTEM.openReadWrite(this))
fun Path.absolute(): Path = FileSystem.SYSTEM.canonicalize(this)


private const val BUFFER_SIZE = 8192L * 1024L
fun Path.create(size: Long = 0L) =
    if (exists()) throw IOException("file already exists") else FileSystem.SYSTEM.sink(this).use { sink ->
        val block = size / BUFFER_SIZE
        val until = size % BUFFER_SIZE

        val byte = ByteArray(BUFFER_SIZE.toInt())

        sink.buffer().use {
            repeat(block.toInt()) { _ ->
                it.write(byte)
            }
            it.write(ByteArray(until.toInt()))
            it.flush()
            it.close()
        }
    }

fun Path.open(readonly: Boolean = false) =
    if (readonly) FileSystem.SYSTEM.openReadOnly(this) else FileSystem.SYSTEM.openReadWrite(this, mustExist = true)

fun Source.transfer(sink: Sink) {
    val buf = Buffer()
    while (read(buf, BUFFER_SIZE) != -1L) {
        sink.write(buf, buf.size)
        sink.flush()
        buf.clear()
    }
}

fun Path.copyTo(path: Path) = FileSystem.SYSTEM.copy(this,path)

fun Path.moveTo(path: Path) {
    if (exists() && isDirectory) error("the input or output must be file.")

    if (path.exists()) {
        path.delete()
    }
    // IOException -
    // if the move cannot be performed, or cannot be performed atomically.
    // Moves fail if the source doesn't exist,
    // if the target is not writable,
    // if the target already exists and cannot be replaced,
    // or if the move would cause physical or quota limits to be exceeded.
    // This list of potential problems is not exhaustive.
    try {
        FileSystem.SYSTEM.atomicMove(this, path)
    } catch (_: IOException) {
        path.parent!!.mkdirs()
        path.create()
        path.sink().use { o ->
            this.source().use { i ->
                i.transfer(o)
            }
        }
        delete()
    }
}


val Path.isFile
    get() = metadata().isRegularFile
val Path.isDirectory
    get() = metadata().isDirectory
val Path.size
    get() = metadata().size!!

fun Path.walk(consumer: (Path) -> Unit) {
    if (isFile) {
        consumer(this)
        return
    }
    for (child in list()) {
        child.walk(consumer)
    }
}

expect fun current(): Path


class AutoClosableSink(private val handle: FileHandle, private val sink: Sink = handle.sink()) : Sink by sink {
    override fun close() {
        sink.close()
        handle.close()
    }
}
