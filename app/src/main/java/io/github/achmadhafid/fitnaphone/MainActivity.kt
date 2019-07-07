@file:Suppress("WildcardImport")

package io.github.achmadhafid.fitnaphone

import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.OvershootInterpolator
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
import io.github.achmadhafid.simplepref.extension.simplePref
import io.github.achmadhafid.simplepref.extension.simplePrefNullable
import io.github.achmadhafid.toolbar_badge_menu_item.createToolbarBadge
import io.github.achmadhafid.zpack.ktx.*
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    //region Resource Binding

    private val tutorialTag by stringRes(R.string.tutorial_tag)

    //endregion
    //region View Binding

    private val appBarLayout: AppBarLayout by bindView(R.id.appBarLayout)
    private val toolbar: MaterialToolbar by bindView(R.id.toolbar)
    private val progressBar: ProgressBar by bindView(R.id.progressBar)
    private val recyclerView: RecyclerView by bindView(R.id.recyclerView)

    //endregion
    //region View Model

    private val viewModel: MainActivityViewModel by bindViewModel()

    //endregion
    //region Adapter

    private val appListAdapter by createAppListAdapter { viewModel.updateAppInfo(it) }

    //endregion
    //region Preferences

    private var theme: Int? by simplePrefNullable()
    private var appInfoList by simplePref{ mutableListOf<AppInfo>()}

    //endregion

    //region Lifecycle Callback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setMaterialToolbar(R.id.toolbar)

        if (BlockerService.isForeground) {
            toastShort(R.string.message_service_already_running)
            finish()
        } else {
            //region setup recycler view
            @Suppress("MagicNumber")
            recyclerView.apply {
                adapter       = appListAdapter
                layoutManager = LinearLayoutManager(context)
                itemAnimator  = SlideInUpAnimator(OvershootInterpolator(1.0f))
                    .apply {
                        addDuration    = 250L
                        changeDuration = 100L
                    }

                setHasFixedSize(true)
                addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        appBarLayout.isSelected = recyclerView.canScrollVertically(-1)
                    }
                })
            }
            //endregion
            //region setup view model
            viewModel.initializeIfNeeded(this, appInfoList)
                .appList
                .observe(this, Observer {
                    with(appInfoList) {
                        clear()
                        addAll(viewModel.blockedAppList)
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

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        //region hide clear icon if no selection has been made

        menu.findItem(R.id.action_clear_selection)
            ?.isVisible = viewModel.blockedAppList.isNotEmpty()

        //endregion
        //region create lock icon badge

        createToolbarBadge(
            menu,
            mapOf(R.id.action_lock to R.drawable.ic_lock_black_24dp)
        ) { viewModel.blockedAppList.size }

        //endregion
        //region show first time tutorial

        if (viewModel.initialized) {
            doOnce(tutorialTag) { showFirstTimeTutorial() }
        }

        //endregion
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_clear_selection -> viewModel.clearSelection()
            R.id.action_lock -> viewModel.blockedAppList.let {
                if (it.isEmpty()) {
                    return false
                } else if (!hasAppUsagePermission) {
                    showPermissionRequestDialog()
                } else {
                    startBlockerService(it)
                    finish()
                }
            }
            R.id.action_switch_theme -> theme = toggleTheme()
        }

        return true
    }

    //endregion
    //region Private Helper

    /**
     * Show lock tutorial
     */
    private fun showFirstTimeTutorial() {
        Handler().post {
            TapTargetView.showFor(
                this,
                TapTarget.forToolbarMenuItem(
                    toolbar,
                    R.id.action_lock,
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
                    .targetRadius(resources.getInteger(R.integer.tutorial_target_radius)),
                object : TapTargetView.Listener() {
                    override fun onTargetDismissed(view: TapTargetView?, userInitiated: Boolean) {
                        toggleThemeTutorial()
                        super.onTargetDismissed(view, userInitiated)
                    }
                }
            )
        }
    }

    /**
     * Show switch theme tutorial
     */
    private fun toggleThemeTutorial() {
        TapTargetView.showFor(
            this,
            TapTarget.forToolbarMenuItem(
                toolbar,
                R.id.action_switch_theme,
                getString(R.string.tutorial_switch_theme_title),
                getString(R.string.tutorial_switch_theme_description)
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

    /**
     * Show a permission request dialog description
     */
    private fun showPermissionRequestDialog() {
        lottieDialog {
            type = LottieDialog.Type.BOTTOM_SHEET
            animation(R.raw.dialog_illustration)
            title(R.string.dialog_permission_required_title)
            content(R.string.dialog_permission_required_message)
            positiveButton {
                textRes     = android.R.string.ok
                iconRes     = R.drawable.ic_check_black_18dp
                actionDelay = resources.getInteger(R.integer.dialog_action_delay).toLong()
                onClick { openUsageAccessSettings() }
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

}
