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
import com.github.ajalt.clikt.parameters.types.long
import com.github.ajalt.mordant.animation.coroutines.animateInCoroutine
import com.github.ajalt.mordant.animation.progress.MultiProgressBarAnimation
import com.github.ajalt.mordant.animation.progress.addTask
import com.github.ajalt.mordant.animation.progress.advance
import com.github.ajalt.mordant.widgets.progress.percentage
import com.github.ajalt.mordant.widgets.progress.progressBar
import com.github.ajalt.mordant.widgets.progress.progressBarLayout
import com.github.ajalt.mordant.widgets.progress.text
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.encodeToByteArray
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


class EncryptCommand : SuspendingCliktCommand(name = "encrypt"), Loggable {

    override val debug by option(help = "enable debug log").flag()
    val helper by option(help = "create a shell script to decrypt file").flag()
    val blockSize by option(help = "max block size,unit is KB. default is 256").long().default(256)

    private val output by option(help = "the output folder")
        .convert { it.toPath() }
        .validate { require(!it.exists()) { "output folder should be not exist." } }

    private val input by argument()
        .convert { it.toPath() }
        .validate { require(it.exists() && it.isDirectory) { "input folder does not exist." } }

    @OptIn(ExperimentalSerializationApi::class, ExperimentalUuidApi::class)
    override suspend fun run() {
        val output = (output ?: (input.parent!! / "${input.name}-encrypt"))

        debug("executable path = ${current()}")
        debug("blockSize = ${blockSize.kb}")
        debug("input = $input")
        debug("output = $output")

        val blockSize = blockSize * 1024
        //创建索引
        val path2index = buildMap {
            input.walk {
                if (!it.isFile) {
                    return@walk
                }
                val fileSize = it.size
                val fileBlockSize = fileSize / blockSize
                val last = fileSize % blockSize

                put(
                    it.absolute(),
                    FileDescriptor(
                        it.relativeTo(input),
                        fileSize,
                        MutableList(fileBlockSize.toInt() + 1) { 0 },
                        last.toInt()
                    )
                )
            }
        }

        info("file info: find ${path2index.size} files.")
        for ((k, v) in path2index.entries) {
            debug("file info: $k: ${v.blocks.size - 1} * 128K + ${v.last} = ${k.size}")
        }

        output.apply {
            debug("create output folder on: $this")
            mkdirs()
        }
        val payloadLength = path2index.values.sumOf { fd -> fd.blocks.size }

        val tmp = output / "${Uuid.random().toHexString()}.temp"
        val payload = tmp.let {
            debug("create temp file on: $it, size is ${(payloadLength * blockSize).b}")
            val size = payloadLength * blockSize
            it.parent!!.mkdirs()
            it.create(size)
            it.open()
        }

        //okio的FileHandle的sink在close时，会检查FileHandle，在这里抢占一个handle以避免触发自动关闭
        val payloadProtectToNotClose = payload.sink()

        data class Context(
            val path: Path, //源路径
            val srcOffset: Long, //源偏移量
            val srcLength: Long, //源长度
            val dstOffset: Long, //目标偏移量
        )

        val srcOffsets = run {
            val dstOffsets = List(payloadLength) { it * blockSize }.shuffled().toMutableList()

            path2index.flatMap { (path, fd) ->
                val contexts = mutableListOf<Context>()

                run {
                    if (fd.blocks.size == 1) {
                        contexts.add(
                            Context(
                                path,
                                0,
                                fd.last.toLong(),
                                dstOffsets.removeFirst()
                            )
                        )
                        return@run
                    }

                    for (i in 0..<(fd.blocks.size - 1)) {
                        contexts.add(Context(path, blockSize * i, blockSize, dstOffsets.removeFirst()))
                    }

                    if (fd.last != 0) {
                        contexts.add(
                            Context(
                                path,
                                blockSize * (fd.blocks.size - 1).toLong(),
                                fd.last.toLong(),
                                dstOffsets.removeFirst()
                            )
                        )
                    }
                }

                path2index[path]!!.blocks = contexts.map { it.dstOffset / blockSize }

                contexts
            }
        }

        info("context info: split ${path2index.size} files to ${srcOffsets.size}'s blocks, each blocks is ${this.blockSize.kb}.")
        for (i in srcOffsets) {
//            [Debug]: context info: path=.DS_Store,srcOffset=0,srcLength=6148,dstOffset=655360
//            [Debug]: context info: path=libgif_rust.dylib,srcOffset=0,srcLength=131072,dstOffset=131072
//            [Debug]: context info: path=libgif_rust.dylib,srcOffset=131072,srcLength=131072,dstOffset=262144
//            [Debug]: context info: path=libgif_rust.dylib,srcOffset=262144,srcLength=131072,dstOffset=786432
//            [Debug]: context info: path=libgif_rust.dylib,srcOffset=393216,srcLength=131072,dstOffset=1048576
//            [Debug]: context info: path=libgif_rust.dylib,srcOffset=524288,srcLength=131072,dstOffset=524288
//            [Debug]: context info: path=libgif_rust.dylib,srcOffset=655360,srcLength=122128,dstOffset=0
//            [Debug]: context info: path=store/gradle.properties,srcOffset=0,srcLength=412,dstOffset=393216
//            [Debug]: context info: path=zero-length.file,srcOffset=0,srcLength=0,dstOffset=917504
            debug("context info: path=${i.path.relativeTo(input.absolute())},srcOffset=${i.srcOffset},srcLength=${i.srcLength},dstOffset=${i.dstOffset}")
        }

        for ((k, v) in path2index.entries) {
            debug("file split: $k: ${v.blocks.joinToString()}")
        }

        val read = Semaphore(minOf(16, payloadLength)) //少于payloadLength个块不需要控制信号量
        debug("maximum file will be handled by this task: ${read.availablePermits}")

        val progress = MultiProgressBarAnimation(terminal).animateInCoroutine()
        val path2layout = buildMap {
            for (k in path2index.keys) {
                val task = progressBarLayout {
                    text { k.toString() }
                    progressBar()
                    percentage()
                }

                put(
                    k,
                    progress.addTask(task, total = path2index[k]!!.blocks.size.toLong())
                )
            }
        }

        withContext(Dispatchers.IO) {
            val job = launch {
                progress.execute()
            }

            val path2mutex = buildMap {
                for (i in path2index.keys) {
                    put(i, Mutex())
                }
            }

            srcOffsets.map { task ->
                async {
                    path2mutex[task.path]!!.withLock {
                        val src = read.withPermit {
                            Buffer().apply {
                                task.path.open(true).use { handle ->
                                    handle.source(task.srcOffset).use {
                                        it.read(this, task.srcLength)
                                    }
                                }
                            }
                        }

                        payload.sink(task.dstOffset).use {
                            it.write(src, task.srcLength)
                            it.flush()
                        }
                        path2layout[task.path]!!.advance(1)
                    }

                }
            }.awaitAll()

            job.join()
        }
        payload.close()
        payloadProtectToNotClose.close()

        //gzip it
        tmp.source().use { i ->
            info("compressing files...")
            val sink = (output / "payload.bin").run {
                create()
                sink()
            }

            sink.gzip().use { o ->
                i.transfer(o)
            }
        }

        info("writing metadata...")

        val index = (output / "index.cbor").run {
            create()
            sink()
        }

        index.buffer().use {
            it.writeLong(MAGIC)
            it.writeLong(METADATA_VERSION)
            it.writeLong(blockSize) //here is bytes.
            it.write(Cbor.encodeToByteArray(path2index.map { kv -> kv.value }))
        }
        if (helper) {
            info("writing shell script...")
            output.writeShell()
        }

        info("deleting temp file...")
        tmp.delete()
    }
}

expect fun Path.writeShell()
