package io.github.achmadhafid.fitnaphone

import android.app.Application
import io.github.achmadhafid.simplepref.converter.onDeserialize
import io.github.achmadhafid.simplepref.converter.onSerialize
import io.github.achmadhafid.simplepref.converter.simplePrefAddConverter
import io.github.achmadhafid.simplepref.lifecycle.SimplePrefApplication
import io.github.achmadhafid.simplepref.lifecycle.SimplePrefLifecycleOwner
import io.github.achmadhafid.simplepref.simplePref
import io.github.achmadhafid.zpack.ktx.applyTheme
import jonathanfinerty.once.Once
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class App : Application(), SimplePrefLifecycleOwner by SimplePrefApplication() {

    //region Preference

    private var theme: Int? by simplePref("app_theme")

    //endregion

    //region Lifecycle Callback

    override fun onCreate() {
        super.onCreate()

        attachSimplePrefContext(this)
        // Add converter for data type `MutableList<String>`
        simplePrefAddConverter<MutableList<AppInfo>> {
            onSerialize {
                it.joinToString("::") { appInfo ->
                    appInfo.serialize()
                }
            }
            onDeserialize {
                if (it.isEmpty()) mutableListOf()
                else it.split("::").map { item ->
                    AppInfo.deserialize(item)
                }.toMutableList()
            }
        }
        startKoin {
            androidLogger()
            androidContext(this@App)
            modules(appModule)
        }
        Once.initialise(this)
        theme?.let { applyTheme(it) }
    }

    //endregion

}
