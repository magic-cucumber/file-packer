package top.kagg886.filepacker.util

import okio.FileHandle
import okio.FileSystem
import okio.IOException
import okio.Path
import okio.SYSTEM
import okio.Sink
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
fun Path.sink() = FileSystem.SYSTEM.openReadWrite(this).sink()
fun Path.absolute(): Path = FileSystem.SYSTEM.canonicalize(this)


private const val BUFFER_SIZE = 8192L * 1024L
fun Path.create(size: Long = 0L) =
    if (exists()) throw IOException("file already exists") else FileSystem.SYSTEM.sink(this).buffer().use {
        val block = size / BUFFER_SIZE
        val until = size % BUFFER_SIZE

        val byte = ByteArray(BUFFER_SIZE.toInt())

        repeat(block.toInt()) { _ ->
            it.write(byte)
        }
        it.write(ByteArray(until.toInt()))
    }

fun Path.open() = FileSystem.SYSTEM.openReadWrite(this, mustExist = true)


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
