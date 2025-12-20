package top.kagg886.filepacker.util

import Main
import okio.Path.Companion.toPath
import java.net.URLDecoder

actual fun current(): okio.Path =
    URLDecoder.decode(Main::class.java.protectionDomain.codeSource.location.path, "UTF-8").toPath()
