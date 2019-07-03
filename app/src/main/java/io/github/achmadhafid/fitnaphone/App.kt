package io.github.achmadhafid.fitnaphone

import android.app.Application
import io.github.achmadhafid.simplepref.extension.simplePrefNullable
import io.github.achmadhafid.zpack.ktx.applyTheme
import jonathanfinerty.once.Once

@Suppress("unused")
class App : Application() {

    //region Properties

    private var theme: Int? by simplePrefNullable()

    //endregion
    //region Lifecycle Callback

    override fun onCreate() {
        super.onCreate()
        Once.initialise(this)
        theme?.let { applyTheme(it) }
    }

    //endregion

}

//region Helper Function : Do thing only once

fun doOnce(tag: String, block: () -> Unit) {
    if (!Once.beenDone(tag)) {
        Once.markDone(tag)
        block()
    }
}

//endregion
