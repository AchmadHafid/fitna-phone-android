package io.github.achmadhafid.fitnaphone

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.MainThread
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.github.achmadhafid.zpack.extension.getAppIcon
import io.github.achmadhafid.zpack.extension.view.f
import io.github.achmadhafid.zpack.extension.view.visibleOrInvisible

class AppListAdapter(
    private val context: Context,
    private val onClickListener: (AppInfo) -> Unit
) : ListAdapter<AppInfo, AppListAdapter.ViewHolder>(DiffUtilCallback) {

    private val iconStore: HashMap<String, Drawable?> = HashMap()

    fun setIcons(icons: List<Pair<String, Drawable?>>) {
        iconStore.putAll(icons)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_view_app_list, parent, false) as ConstraintLayout
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(getItem(position)) {
            holder.bindAppInfo(this, getIconDrawable(packageName), onClickListener)
        }
    }

    private fun getIconDrawable(packageName: String) =
        iconStore[packageName] ?: context.getAppIcon(packageName)?.let {
            iconStore[packageName] = it
            it
        }

    //region View Holder

    class ViewHolder(private val container: ConstraintLayout) : RecyclerView.ViewHolder(container) {

        private val tvName: TextView     = container f R.id.tvName
        private val ivIcon: ImageView    = container f R.id.ivIcon
        private val ivLock: ImageView    = container f R.id.ivLock
        private val ivOverlay: ImageView = container f R.id.ivOverlay

        fun bindAppInfo(
            appInfo: AppInfo,
            iconDrawable: Drawable?,
            onClickListener: (AppInfo) -> Unit
        ) {
            fun showLockIcon(show: Boolean) {
                ivLock.visibleOrInvisible { show }
                ivOverlay.alpha = if (show) 1f else 0f
            }

            tvName.text = appInfo.name
            iconDrawable?.let { ivIcon.setImageDrawable(it) }
            showLockIcon(appInfo.blocked)
            container.setOnClickListener {
                appInfo.blocked = !appInfo.blocked
                showLockIcon(appInfo.blocked)
                onClickListener(appInfo)
            }
        }

    }

    //endregion

}

//region Diff Util Helper

object DiffUtilCallback : DiffUtil.ItemCallback<AppInfo>() {

    override fun areItemsTheSame(oldItem: AppInfo, newItem: AppInfo) =
        newItem.packageName == oldItem.packageName

    override fun areContentsTheSame(oldItem: AppInfo, newItem: AppInfo) =
        newItem.packageName == oldItem.packageName && newItem.blocked == oldItem.blocked

}

//endregion
//region Extension Helper

@MainThread
fun AppCompatActivity.createAppListAdapter(onClickListener: (AppInfo) -> Unit) =
    lazy(LazyThreadSafetyMode.NONE) { AppListAdapter(this, onClickListener) }

//endregion
