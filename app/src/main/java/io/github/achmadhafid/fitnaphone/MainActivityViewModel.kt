package io.github.achmadhafid.fitnaphone

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import io.github.achmadhafid.simplepref.core.simplePrefSave
import io.github.achmadhafid.simplepref.lifecycle.SimplePrefLifecycleOwner
import io.github.achmadhafid.simplepref.lifecycle.SimplePrefViewModel
import io.github.achmadhafid.simplepref.simplePref
import io.github.achmadhafid.zpack.ktx.installedLauncherApp
import io.github.achmadhafid.zpack.ktx.notifyObserver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivityViewModel(application: Application) : AndroidViewModel(application),
    SimplePrefLifecycleOwner by SimplePrefViewModel(application) {

    private val _appList: MutableLiveData<List<AppInfo>> = MutableLiveData()
    val appList: LiveData<List<AppInfo>> = _appList
    val blockedApps by simplePref("blocked_apps") { mutableListOf<AppInfo>() }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            context?.let { ctx ->
                runCatching {
                    val lisOfApps = ctx.installedLauncherApp
                        .filter { it.packageName != context?.packageName }
                        .map { AppInfo(ctx, it.packageName, blockedApps.contains(it.packageName)) }
                        .sortedBy { it.name }
                    _appList.postValue(lisOfApps)
                }.onFailure {
                    _appList.postValue(listOf())
                }
            }
        }
    }

    fun update(appInfo: AppInfo) {
        blockedApps.addIfBlockedOrRemove(appInfo)
        simplePrefSave(::blockedApps)

        with(_appList) {
            value?.let {
                it.updateBlocked(appInfo)
                notifyObserver()
            }
        }
    }

    fun clearSelection() {
        blockedApps.clear()
        simplePrefSave(::blockedApps)

        with(_appList) {
            value?.let {
                it.resetBlocked()
                notifyObserver()
            }
        }
    }

    override fun onCleared() {
        onDestroySimplePref()
        super.onCleared()
    }
    
}
