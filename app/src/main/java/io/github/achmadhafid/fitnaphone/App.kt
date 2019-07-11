package io.github.achmadhafid.fitnaphone

import android.app.Application
import io.github.achmadhafid.simplepref.extension.simplePrefNullable
import io.github.achmadhafid.zpack.ktx.applyTheme
import jonathanfinerty.once.Once
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

@Suppress("unused")
class App : Application() {

    //region Preference

    private var theme: Int? by simplePrefNullable()

    //endregion

    //region Lifecycle Callback

    override fun onCreate() {
        super.onCreate()

        Once.initialise(this)
        startKoin {
            androidLogger()
            androidContext(this@App)
            modules(appModule)
        }
        theme?.let { applyTheme(it) }
    }

    //endregion

}
