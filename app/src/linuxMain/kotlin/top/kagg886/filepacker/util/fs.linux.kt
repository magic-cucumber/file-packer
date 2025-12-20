package top.kagg886.filepacker.util

import kotlinx.cinterop.ExperimentalForeignApi
import okio.FileSystem
import okio.Path.Companion.toPath

@OptIn(ExperimentalForeignApi::class)
actual fun current(): okio.Path = FileSystem.SYSTEM.canonicalize("/proc/self/exe".toPath())
