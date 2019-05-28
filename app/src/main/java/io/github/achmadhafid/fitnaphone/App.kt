package io.github.achmadhafid.fitnaphone

import android.app.Application
import jonathanfinerty.once.Once

@Suppress("unused")
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        Once.initialise(this)
    }

}

fun doOnce(tag: String, block: () -> Unit) {
    if (!Once.beenDone(tag)) {
        Once.markDone(tag)
        block()
    }
}
