package io.github.achmadhafid.fitnaphone

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.MainThread
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import io.github.achmadhafid.zpack.ktx.getAppIcon
import io.github.achmadhafid.zpack.ktx.visibleOrInvisible

class AppListAdapter(
    private val context: Context,
    private val onClickListener: (AppInfo) -> Unit
) : RecyclerView.Adapter<AppListAdapter.ViewHolder>() {

    private val iconStore: HashMap<String, Drawable> = HashMap()
    var items: MutableList<AppInfo> = mutableListOf()
        set(newItems) {
            DiffUtil.calculateDiff(DiffUtilCallback(newItems, field))
                .also { field = newItems.map { it.copy() }.toMutableList() }
                .dispatchUpdatesTo(this)
        }

    override fun getItemCount() = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(
                R.layout.recycler_view_app_list,
                parent,
                false
            ) as FrameLayout
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(items[position]) {
            holder.bindAppInfo(
                this,
                getIconDrawable(packageName),
                onClickListener
            )
        }
    }

    private fun getIconDrawable(packageName: String) =
        iconStore[packageName] ?: context.getAppIcon(packageName)
            ?.let {
                iconStore[packageName] = it
                it
            }

    //region View Holder

    class ViewHolder(private val container: FrameLayout) : RecyclerView.ViewHolder(container) {

        private val tvName: TextView = container.findViewById(R.id.tvName)
        private val ivIcon: ImageView = container.findViewById(R.id.ivIcon)
        private val ivLock: ImageView = container.findViewById(R.id.ivLock)
        private val ivOverlay: ImageView = container.findViewById(R.id.ivOverlay)

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

class DiffUtilCallback(
    private val newItems: List<AppInfo>,
    private val oldItems: List<AppInfo>
) : DiffUtil.Callback() {

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        newItems[newItemPosition].packageName == oldItems[oldItemPosition].packageName

    override fun getOldListSize() = oldItems.size

    override fun getNewListSize() = newItems.size

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        newItems[newItemPosition].name == oldItems[oldItemPosition].name &&
                newItems[newItemPosition].blocked == oldItems[oldItemPosition].blocked

}

//endregion
//region Extension Helper

@MainThread
fun MainActivity.createAppListAdapter(onClickListener: (AppInfo) -> Unit) =
    lazy(LazyThreadSafetyMode.NONE) { AppListAdapter(this, onClickListener) }

//endregion
