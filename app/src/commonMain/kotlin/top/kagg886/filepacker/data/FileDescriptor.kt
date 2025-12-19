package top.kagg886.filepacker.data

import kotlinx.serialization.Serializable
import okio.Path
import top.kagg886.filepacker.util.PathSerializer

/**
 * ================================================
 *
 * Author:     886kagg
 * Created on: 2025/12/19 15:09
 *
 * ================================================
 *
 * 单个文件描述符
 * @param path 文件相对根目录的路径
 * @param blocks 文件块的索引
 * @param last 最后一个块的大小
 */
@Serializable
data class FileDescriptor(
    @Serializable(PathSerializer::class)
    var path: Path,
    var size: Long,
    var blocks: List<Long>,
    var last: Int,
)
