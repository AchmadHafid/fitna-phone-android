package io.github.achmadhafid.fitnaphone

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.achmadhafid.zpack.ktx.getAppName
import io.github.achmadhafid.zpack.ktx.installedAppsWithLaunchIntent
import io.github.achmadhafid.zpack.ktx.notifyObserver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivityViewModel : ViewModel() {

    private val _appList: MutableLiveData<List<AppInfo>> = MutableLiveData()
    val appList: LiveData<List<AppInfo>> = _appList
    val blockedAppList: List<AppInfo>
        get() = _appList.value
            ?.filter { it.blocked }
            ?: emptyList()
    var initialized = false
        private set

    fun initializeIfNeeded(
        context: Context,
        lastBlockedItems: MutableList<AppInfo>
    ): MainActivityViewModel {
        if (_appList.value == null) {
            viewModelScope.launch {
                loadAppList(context, lastBlockedItems)
                initialized = true
            }
        }
        return this
    }

    fun updateAppInfo(appInfo: AppInfo) {
        _appList.value?.forEach {
            if (it.packageName == appInfo.packageName)
                it.blocked = appInfo.blocked
        }
        _appList.notifyObserver()
    }

    fun clearSelection() {
        _appList.value?.forEach { it.blocked = false } ?: return
        _appList.notifyObserver()
    }

    private suspend fun loadAppList(
        context: Context,
        lastBlockedItems: MutableList<AppInfo>
    ) = withContext(Dispatchers.IO) {
        val lisOfApps = context.installedAppsWithLaunchIntent
            .filter { it.packageName != context.packageName }
            .map {
                val blocked = lastBlockedItems.find { item ->
                    it.packageName == item.packageName
                } != null
                AppInfo(
                    it.packageName,
                    context.getAppName(it.packageName) ?: "",
                    blocked
                )
            }
            .sortedBy { it.name }
        _appList.postValue(lisOfApps)
    }

}
