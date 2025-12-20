package util

import org.gradle.api.Project
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

internal var prop: Properties? = null
internal val lock = ReentrantLock()

internal fun Project.local(key: String): String? {
    val prop = lock.withLock {
        val file = rootProject.file("local.properties")
        if (prop != null) {
            return@withLock prop!!
        }
        if (!file.exists()) {
            return@withLock Properties().apply {
                prop = this
            }
        }
        file.inputStream().use {
            return@withLock Properties().apply {
                this.load(it)
                prop = this
            }
        }
    }

    return prop.getProperty(key)
}

internal fun Project.props(vararg data: String) =
    System.getenv(data.joinToString("_") { it.uppercase() }) ?: data.joinToString(".") { it.lowercase() }.let { key->
        local(key) ?: findProperty(key)?.toString()
    }
