package io.github.achmadhafid.fitnaphone

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.github.florent37.viewanimator.AnimationBuilder
import com.github.florent37.viewanimator.ViewAnimator

class AppListAdapter(
    private val context: Context,
    private val onBlockListener: (AppInfo) -> Unit
) : RecyclerView.Adapter<AppListAdapter.ViewHolder>() {

    private var items: List<AppInfo> = emptyList()
    private val iconStore: HashMap<String, Drawable> = HashMap()
    private val animationDuration by longResC(context, R.integer.animation_duration)

    fun setItems(appInfoList: List<AppInfo>) {
        DiffUtil.calculateDiff(DiffUtilCallback(appInfoList, items))
            .also {
                items = appInfoList
            }
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
                animationDuration,
                onBlockListener
            )
        }
    }

    private fun getIconDrawable(packageName: String) =
        iconStore[packageName] ?: context.getAppIcon(packageName)
            ?.let {
                iconStore[packageName] = it
                it
            }

    class ViewHolder(private val container: FrameLayout) : RecyclerView.ViewHolder(container) {

        private val tvName: TextView     = container.findViewById(R.id.tvName)
        private val ivIcon: ImageView    = container.findViewById(R.id.ivIcon)
        private val ivLock: ImageView    = container.findViewById(R.id.ivLock)
        private val ivOverlay: ImageView = container.findViewById(R.id.ivOverlay)

        fun bindAppInfo(
            appInfo: AppInfo,
            iconDrawable: Drawable?,
            animationDuration: Long,
            onBlockListener: (AppInfo) -> Unit
        ) {
            tvName.text = appInfo.name
            iconDrawable?.let { ivIcon.setImageDrawable(it) }
            showLockIcon(appInfo.blocked, false, animationDuration)
            container.setOnClickListener {
                appInfo.blocked = !appInfo.blocked
                showLockIcon(appInfo.blocked, true, animationDuration)
                onBlockListener(appInfo)
            }
        }

        private fun showLockIcon(
            show: Boolean,
            animate: Boolean,
            animationDuration: Long
        ) {
            ivLock.visibleOrInvisible(show)

            if (animate) {
                ViewAnimator.animate(ivOverlay)
                    .duration(animationDuration)
                    .fade(show)
                    .start()
            } else {
                ivOverlay.visibleOrInvisible(show)
                ivOverlay.alpha = if (show) 1f else 0f
            }
        }

        private fun AnimationBuilder.fade(show: Boolean): AnimationBuilder {
            if (show) {
                alpha(0f, 1f)
                onStart { view.visibility = View.VISIBLE }
            } else {
                alpha(1f, 0f)
                onStop { view.visibility = View.INVISIBLE }
            }
            return this
        }

    }

}

class DiffUtilCallback(
    private val newItems: List<AppInfo>,
    private val oldItems: List<AppInfo>
) : DiffUtil.Callback() {

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        areContentsTheSame(oldItemPosition, newItemPosition)

    override fun getOldListSize() = oldItems.size

    override fun getNewListSize() = newItems.size

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        newItems[newItemPosition] == oldItems[oldItemPosition]

}

data class AppInfo(
    val packageName: String,
    val name: String,
    var blocked: Boolean
)
