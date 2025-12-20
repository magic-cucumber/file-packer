package top.kagg886.filepacker.command

import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.terminal
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.arguments.validate
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.validate
import com.github.ajalt.mordant.animation.coroutines.animateInCoroutine
import com.github.ajalt.mordant.animation.progress.MultiProgressBarAnimation
import com.github.ajalt.mordant.animation.progress.addTask
import com.github.ajalt.mordant.animation.progress.advance
import com.github.ajalt.mordant.widgets.progress.percentage
import com.github.ajalt.mordant.widgets.progress.progressBar
import com.github.ajalt.mordant.widgets.progress.progressBarLayout
import com.github.ajalt.mordant.widgets.progress.text
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray
import okio.Buffer
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.buffer
import okio.gzip
import okio.use
import top.kagg886.filepacker.data.FileDescriptor
import top.kagg886.filepacker.util.*
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * ================================================
 * Author:     886kagg
 * Created on: 2025/12/19 13:38
 * ================================================
 */

class DecryptCommand : SuspendingCliktCommand(name = "decrypt"), Loggable {
    override val debug by option(help = "enable debug log").flag()
    private val input by argument(help = "input archive folder")
        .convert { it.toPath() }
        .validate {
            require(it.exists() && it.isDirectory) { "input should be a directory" }
            require((it / "index.cbor").exists() && (it / "index.cbor").isFile) { "can't find index.cbor" }
            require((it / "payload.bin").exists() && (it / "payload.bin").isFile) { "can't find payload.bin" }
        }

    @OptIn(ExperimentalUuidApi::class)
    val output by option(help = "output folder")
        .convert { it.toPath() }
        .default(Uuid.random().toString().replace("-", "").toPath())
        .validate {
            require(!it.exists()) { "output folder already exists" }
        }

    @OptIn(ExperimentalSerializationApi::class, ExperimentalUuidApi::class)
    override suspend fun run() {
        val (blockSize, metadata) = run {
            val meta = (input / "index.cbor").source().buffer()
            val magic = meta.readLong()
            val version = meta.readLong()

            if (magic != MAGIC) {
                error("magic number is not correct")
                return
            }

            if (version != METADATA_VERSION) {
                error("metadata version is not correct,current file version is $version,but this file must be decrypt: $METADATA_VERSION")
                return
            }

            val blockSize = meta.readLong()
            blockSize to Cbor.decodeFromByteArray<List<FileDescriptor>>(meta.readByteArray())
        }


        debug("blockSize = $blockSize")
        debug("input = $input")
        debug("output = $output")

        val tmp = FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "${Uuid.random().toHexString().replace("-", "")}.bin"
        val payload = run {
            val origin = (input / "payload.bin")
            debug("create uncompressed file to $tmp")

            tmp.parent!!.mkdirs()
            tmp.create()
            origin.source().gzip().use { i ->
                tmp.sink().use { o ->
                    i.transfer(o)
                }
            }
            tmp.open(true)
        }
        val payloadProtectNotClose = payload.source()


        data class Context(
            val file: Path,
            val srcOffset: Long,
            val length: Long,
            val dstOffset: Long,
        )

        val contexts = metadata.flatMap { metadata ->
            val contexts = mutableListOf<Context>()
            metadata.blocks.mapIndexed { index, blockNumber ->
                contexts.add(
                    Context(
                        file = metadata.path,
                        srcOffset = blockNumber * blockSize,
                        length = if (index == metadata.blocks.size - 1) metadata.last.toLong() else blockSize,
                        dstOffset = index * blockSize,
                    )
                )
            }
            contexts
        }

        for (i in contexts) {
            debug("context info: ${i.file},srcOffset=${i.srcOffset},srcLength=${i.length},dstOffset=${i.dstOffset}")
        }


        val progress = MultiProgressBarAnimation(terminal).animateInCoroutine()

        val path2layout = buildMap {
            for (k in metadata) {
                val task = progressBarLayout {
                    text { k.path.toString() }
                    progressBar()
                    percentage()
                }

                put(
                    k.path,
                    progress.addTask(task, total = k.blocks.size.toLong())
                )
            }
        }

        val readLock = Semaphore(minOf(16, contexts.size))
        val writeLock = Semaphore(minOf(16, metadata.size))

        output.mkdirs()
        withContext(Dispatchers.IO) {
            metadata.map { task ->
                writeLock.withPermit {
                    output.absolute().resolve(task.path).apply {
                        parent?.mkdirs()
                        create(task.size)
                    }
                }
            }
        }

        withContext(Dispatchers.IO) {
            val path2mutex = buildMap {
                for (i in metadata.map { it.path }.toSet()) {
                    put(i, Mutex())
                }
            }

            contexts.map { task ->
                async {
                    val buffer = Buffer().apply {
                        readLock.withPermit {
                            payload.source(task.srcOffset).use { it.read(this, task.length) }
                        }
                    }

                    writeLock.withPermit {
                        path2mutex[task.file]!!.withLock {
                            output.resolve(task.file).open(true).use { handle ->
                                handle.sink(task.dstOffset).use { dst ->
                                    dst.write(buffer, buffer.size)
                                    dst.flush()
                                }
                            }
                        }
                        path2layout[task.file]!!.advance(1)
                    }
                }
            }.awaitAll()
        }

        payload.close()
        payloadProtectNotClose.close()
        tmp.delete()
    }
}
