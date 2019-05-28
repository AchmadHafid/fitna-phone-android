package io.github.achmadhafid.fitnaphone

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetView
import io.github.achmadhafid.toolbar_badge_menu_item.createToolbarBadge

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    //region Resource Binding

    private val messageServiceAlreadyRunning by stringRes(R.string.message_service_already_running)
    private val tutorialTitle by stringRes(R.string.tutorial_lock_title)
    private val tutorialDescription by stringRes(R.string.tutorial_lock_description)
    private val tutorialTag by stringRes(R.string.tutorial_lock_tag)
    private val tutorialTargetRadius by intRes(R.integer.tutorial_target_radius)

    //endregion
    //region View Binding

    private val toolbar: Toolbar by bindView(R.id.toolbar)
    private val recyclerView: RecyclerView by bindView(R.id.recyclerView)
    private val progressBar: ProgressBar by bindView(R.id.progressBar)

    //endregion
    //region View Model

    private val viewModel: MainActivityViewModel by bindViewModel()

    //endregion

    //region Lifecycle Callback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(toolbar)

        if (isBlockerServiceRunning) {
            toastShort(messageServiceAlreadyRunning)
            finish()
        } else {
            val appListAdapter = AppListAdapter(this) { viewModel.updateAppInfo(it) }
            recyclerView.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(context)
                adapter       = appListAdapter
            }
            viewModel.initializeIfNeeded(this)
                .appList
                .observe(this, Observer {
                    appListAdapter.setItems(it)
                    progressBar.visibility = View.GONE
                    invalidateOptionsMenu()
                })
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.toolbar, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        //region create lock icon badge

        createToolbarBadge(
            menu,
            mapOf(R.id.action_lock to R.drawable.ic_lock_black_24dp),
            iconTintRes = R.attr.colorOnPrimary,
            count = { viewModel.blockedAppList.size }
        )

        //endregion
        //region show first time tutorial on how to lock apps

        menu?.findItem(R.id.action_lock)
            ?.actionView
            ?.let {
                if (viewModel.initialized) {
                    doOnce(tutorialTag) {
                        showFirstTimeTutorial(it)
                    }
                }
            }

        //endregion
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (viewModel.blockedAppList.isEmpty()) {
            return false
        } else if (!hasAppUsagePermission()) {
            //region show dialog asking user to enable usage access on the settings screen

            AlertDialog.Builder(this)
                .setTitle(R.string.dialog_permission_required_title)
                .setMessage(R.string.dialog_permission_required_message)
                .setPositiveButton(android.R.string.ok) { dialog, _ ->
                    dialog.dismiss()
                    startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                }
                .create()
                .apply { setCanceledOnTouchOutside(false) }
                .show()

            //endregion
        } else {
            startBlockerService(viewModel.blockedAppList)
            finish()
        }

        return true
    }

    //endregion
    //region Helper Function

    /**
     * Show tutorial first time tutorial
     * @param view target
     */
    private fun showFirstTimeTutorial(view: View) {
        Handler().post {
            TapTargetView.showFor(
                this,
                TapTarget.forView(view, tutorialTitle, tutorialDescription)
                    .outerCircleColor(R.color.color_overlay_light)
                    .outerCircleAlpha(1f)
                    .titleTextColorInt(resolveColor(R.attr.colorOnSurface))
                    .descriptionTextColorInt(resolveColor(R.attr.colorOnSurface))
                    .drawShadow(true)
                    .cancelable(true)
                    .tintTarget(true)
                    .transparentTarget(false)
                    .targetRadius(tutorialTargetRadius)
            )
        }
    }

    //endregion

}
