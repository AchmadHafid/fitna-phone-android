package io.github.achmadhafid.fitnaphone

import android.app.Application
import jonathanfinerty.once.Once

@Suppress("unused")
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        loadTheme()
        Once.initialise(this)
    }

}

//region Helper Function : Do thing only once

fun doOnce(tag: String, block: () -> Unit) {
    if (!Once.beenDone(tag)) {
        Once.markDone(tag)
        block()
    }
}

//endregion
