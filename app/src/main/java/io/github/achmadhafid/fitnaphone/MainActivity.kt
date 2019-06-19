@file:Suppress("WildcardImport")

package io.github.achmadhafid.fitnaphone

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.MaterialToolbar
import io.github.achmadhafid.lottie_dialog.*
import io.github.achmadhafid.toolbar_badge_menu_item.createToolbarBadge

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    //region Resource Binding

    private val tutorialTag by stringRes(R.string.tutorial_lock_tag)

    //endregion
    //region View Binding

    private val appBarLayout: AppBarLayout by bindView(R.id.appBarLayout)
    private val toolbar: MaterialToolbar by bindView(R.id.toolbar)
    private val recyclerView: RecyclerView by bindView(R.id.recyclerView)
    private val progressBar: ProgressBar by bindView(R.id.progressBar)

    //endregion
    //region View Model

    private val viewModel: MainActivityViewModel by bindViewModel()

    //endregion
    //region Adapter

    private val appListAdapter by lazy {
        AppListAdapter(this, loadAppList()) { viewModel.updateAppInfo(it) }
    }

    //endregion

    //region Lifecycle Callback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(toolbar)

        if (isBlockerServiceRunning) {
            toastShort(R.string.message_service_already_running)
            finish()
        } else {
            //region setup recycler view
            recyclerView.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = appListAdapter
                setHasFixedSize(true)
                addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        appBarLayout.isSelected = recyclerView.canScrollVertically(-1)
                    }
                })
            }
            //endregion
            //region setup view model
            viewModel.initializeIfNeeded(this)
                .appList
                .observe(this, Observer {
                    if (viewModel.blockedAppList.isEmpty()) {
                        deleteAppList()
                    }
                    appListAdapter.items = it.toMutableList()
                    progressBar.visibility = View.GONE
                    invalidateOptionsMenu()
                })
            //endregion
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.toolbar, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        //region hide clear icon if no selection has been made

        menu?.findItem(R.id.action_clear_selection)
            ?.isVisible = viewModel.blockedAppList.isNotEmpty()

        //endregion
        //region create lock icon badge

        createToolbarBadge(
            menu,
            mapOf(R.id.action_lock to R.drawable.ic_lock_black_24dp),
            R.color.color_selection_overlay,
            R.color.color_selection_icon,
            R.color.color_selection_icon
        ) { viewModel.blockedAppList.size }

        //endregion
        //region show first time tutorial on how to lock apps

        menu?.findItem(R.id.action_lock)
            ?.actionView
            ?.let {
                if (viewModel.initialized) {
                    doOnce(tutorialTag) { showFirstTimeTutorial(it) }
                }
            }

        //endregion
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_clear_selection -> viewModel.clearSelection()
            R.id.action_lock -> {
                if (viewModel.blockedAppList.isEmpty()) {
                    return false
                } else if (!hasAppUsagePermission()) {
                    showPermissionRequestDialog()
                } else {
                    with(viewModel.blockedAppList) {
                        saveAppList(this)
                        startBlockerService(this)
                    }
                    finish()
                }
            }
            R.id.action_switch_theme -> saveTheme(switchTheme())
        }

        return true
    }

    //endregion

}

//region Helper Function : Manage blocked app list on shared preference

/**
 * Save blocked app list into shared preference
 * @param appList blocked app list
 */
private fun MainActivity.saveAppList(appList: List<AppInfo>) {
    with(getPreferences(Context.MODE_PRIVATE).edit()) {
        putStringSet(SP_KEY, appList.map { "${it.packageName}$DELIMITER${it.name}" }.toMutableSet())
        apply()
    }
}

/**
 * Load blocked app list from shared preference
 */
private fun MainActivity.loadAppList(): MutableList<AppInfo> = getPreferences(Context.MODE_PRIVATE)
    .getStringSet(SP_KEY, emptySet())
    ?.map { AppInfo(it.split(DELIMITER)[0], it.split(DELIMITER)[1], true) }
    ?.toMutableList()
    ?: mutableListOf()

/**
 * Delete blocked app list shared preference
 */
private fun MainActivity.deleteAppList() {
    getPreferences(Context.MODE_PRIVATE).edit()
        .clear()
        .apply()
}

private const val SP_KEY = "app_list"
private const val DELIMITER = "::"

//endregion
//region Helper Function : Show Tutorial

/**
 * Show tutorial first time tutorial
 * @param view target
 */
private fun MainActivity.showFirstTimeTutorial(view: View) {
    Handler().post {
        TapTargetView.showFor(
            this,
            TapTarget.forView(
                view,
                getString(R.string.tutorial_lock_title),
                getString(R.string.tutorial_lock_description)
            ).outerCircleColor(R.color.color_selection_overlay)
                .outerCircleAlpha(1f)
                .titleTextColorInt(resolveColor(R.attr.colorOnSurface))
                .descriptionTextColorInt(resolveColor(R.attr.colorOnSurface))
                .drawShadow(true)
                .cancelable(true)
                .tintTarget(true)
                .transparentTarget(false)
                .targetRadius(resources.getInteger(R.integer.tutorial_target_radius))
        )
    }
}

//endregion
//region Helper Function : Show Required Permission Request Dialog

fun MainActivity.showPermissionRequestDialog() {
    lottieDialog {
        type = LottieDialog.Type.BottomSheet
        animation {
            lottieFileRes = R.raw.dialog_illustration
        }
        title {
            textRes = R.string.dialog_permission_required_title
        }
        content {
            textRes = R.string.dialog_permission_required_message
        }
        positiveButton {
            textRes = android.R.string.ok
            iconRes = R.drawable.ic_check_black_18dp
            onClick {
                @Suppress("MagicNumber")
                Handler().postDelayed({
                    startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                }, 250)
            }
        }
        negativeButton {
            textRes = android.R.string.cancel
            iconRes = R.drawable.ic_close_black_18dp
        }
        cancel {
            onBackPressed  = true
            onTouchOutside = false
        }
    }
}

//endregion
