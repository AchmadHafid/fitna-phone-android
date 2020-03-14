package io.github.achmadhafid.fitnaphone

import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetView
import io.github.achmadhafid.fitnaphone.databinding.ActivityMainBinding
import io.github.achmadhafid.lottie_dialog.lottieConfirmationDialog
import io.github.achmadhafid.lottie_dialog.model.LottieDialogType
import io.github.achmadhafid.lottie_dialog.model.onClick
import io.github.achmadhafid.lottie_dialog.withAnimation
import io.github.achmadhafid.lottie_dialog.withCancelOption
import io.github.achmadhafid.lottie_dialog.withContent
import io.github.achmadhafid.lottie_dialog.withNegativeButton
import io.github.achmadhafid.lottie_dialog.withPositiveButton
import io.github.achmadhafid.lottie_dialog.withTitle
import io.github.achmadhafid.simplepref.SimplePref
import io.github.achmadhafid.simplepref.simplePref
import io.github.achmadhafid.toolbar_badge_menu_item.createToolbarBadge
import io.github.achmadhafid.toolbar_badge_menu_item.withCount
import io.github.achmadhafid.zpack.ktx.getAppIcon
import io.github.achmadhafid.zpack.ktx.hasAppUsagePermission
import io.github.achmadhafid.zpack.ktx.openUsageAccessSettings
import io.github.achmadhafid.zpack.ktx.resolveColor
import io.github.achmadhafid.zpack.ktx.setMaterialToolbar
import io.github.achmadhafid.zpack.ktx.setSelectedOnScrollDown
import io.github.achmadhafid.zpack.ktx.startForegroundServiceCompat
import io.github.achmadhafid.zpack.ktx.stringRes
import io.github.achmadhafid.zpack.ktx.toastShort
import io.github.achmadhafid.zpack.ktx.toggleTheme
import jonathanfinerty.once.Once
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity(), SimplePref {

    //region Preference

    private var theme: Int? by simplePref("app_theme")

    //endregion
    //region Resource Binding

    private val tutorialTag by stringRes(R.string.tutorial_tag)

    //endregion
    //region View Binding

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    //endregion
    //region View Model

    private val mainViewModel: MainActivityViewModel by viewModel()

    //endregion
    //region Adapter

    private val appListAdapter by createAppListAdapter {
        mainViewModel.update(it)
    }

    //endregion

    //region Lifecycle Callback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setMaterialToolbar(R.id.toolbar)

        if (BlockerService.isForeground) {
            toastShort(R.string.message_service_already_running)
            finish()
        } else {
            //region setup recycler view

            @Suppress("MagicNumber")
            binding.recyclerView.apply {
                setHasFixedSize(true)
                adapter = appListAdapter
                layoutManager = LinearLayoutManager(context)
                itemAnimator = SlideInUpAnimator(OvershootInterpolator(1.0f)).apply {
                    addDuration = 250L
                    changeDuration = 100L
                }
                binding.appBarLayout.setSelectedOnScrollDown(binding.recyclerView)
            }

            //endregion
            //region setup view model

            mainViewModel.appList.observe(this, Observer { apps ->
                lifecycleScope.launch {
                    val icons = withContext(Dispatchers.IO) {
                        apps.map {
                            it.packageName to getAppIcon(it.packageName)
                        }
                    }
                    appListAdapter.setIcons(icons)
                    appListAdapter.submitList(apps.map { item -> item.copy() })
                    binding.progressBar.visibility = View.GONE
                    invalidateOptionsMenu()
                    if (!Once.beenDone(tutorialTag)) {
                        Once.markDone(tutorialTag)
                        ({ showFirstTimeTutorial() })()
                    }
                }
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
            ?.isVisible = mainViewModel.blockedApps.isNotEmpty()

        //endregion
        //region create lock icon badge

        createToolbarBadge {
            toolbarMenu = menu
            icons = mapOf(R.id.action_lock to R.drawable.ic_lock_black_24dp)
            withCount { itemId ->
                when (itemId) {
                    R.id.action_lock -> mainViewModel.blockedApps.size
                    else -> 0 // no badge
                }
            }
        }

        //endregion
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_clear_selection -> mainViewModel.clearSelection()
            R.id.action_lock -> mainViewModel.blockedApps.let {
                if (it.isEmpty()) {
                    return false
                } else if (!hasAppUsagePermission) {
                    showPermissionRequestDialog()
                } else {
                    startForegroundServiceCompat<BlockerService>()
                    finish()
                }
            }
            R.id.action_switch_theme -> theme = toggleTheme()
        }

        return true
    }

    //endregion
    //region Private Helper

    private fun showFirstTimeTutorial() {
        Handler().post {
            TapTargetView.showFor(
                this,
                TapTarget.forToolbarMenuItem(
                        binding.toolbar,
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

    private fun toggleThemeTutorial() {
        TapTargetView.showFor(
            this,
            TapTarget.forToolbarMenuItem(
                    binding.toolbar,
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

    private fun showPermissionRequestDialog() {
        lottieConfirmationDialog {
            type = LottieDialogType.BOTTOM_SHEET
            withAnimation(R.raw.dialog_illustration)
            withTitle(R.string.dialog_permission_required_title)
            withContent(R.string.dialog_permission_required_message)
            withPositiveButton {
                textRes = android.R.string.ok
                iconRes = R.drawable.ic_check_black_18dp
                actionDelay = resources.getInteger(R.integer.dialog_action_delay).toLong()
                onClick { openUsageAccessSettings() }
            }
            withNegativeButton {
                textRes = android.R.string.cancel
                iconRes = R.drawable.ic_close_black_18dp
            }
            withCancelOption { onTouchOutside = false }
        }
    }

    //endregion

}
