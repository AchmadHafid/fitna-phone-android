@file:Suppress("WildcardImport")

package io.github.achmadhafid.fitnaphone

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.achmadhafid.simplepref.extension.savePref
import io.github.achmadhafid.simplepref.extension.saveThanDisposePref
import io.github.achmadhafid.simplepref.extension.simplePref
import io.github.achmadhafid.zpack.ktx.installedAppsWithLaunchIntent
import io.github.achmadhafid.zpack.ktx.notifyObserver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivityViewModel(private val context: Context) : ViewModel() {

    private val _appList: MutableLiveData<List<AppInfo>> = MutableLiveData()
    val appList: LiveData<List<AppInfo>> = _appList
    val blockedApps by simplePref(context = context){ mutableListOf<AppInfo>() }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val lisOfApps = context.installedAppsWithLaunchIntent
                .filter { it.packageName != context.packageName }
                .map { AppInfo(context, it.packageName, blockedApps.contains(it.packageName)) }
                .sortedBy { it.name }
            _appList.postValue(lisOfApps)
        }
    }

    fun update(appInfo: AppInfo) {
        savePref { blockedApps.addIfBlockedOrRemove(appInfo) }

        with(_appList) {
            value?.let {
                it.updateBlocked(appInfo)
                notifyObserver()
            }
        }
    }

    fun clearSelection() {
        savePref { blockedApps.clear() }

        with(_appList) {
            value?.let {
                it.resetBlocked()
                notifyObserver()
            }
        }
    }

    override fun onCleared() {
        saveThanDisposePref()
        super.onCleared()
    }
}
