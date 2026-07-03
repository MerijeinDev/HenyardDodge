package henyard.dodgerush.dewpond.ui.achievement

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import henyard.dodgerush.dewpond.R
import henyard.dodgerush.dewpond.databinding.ItemAchievementBinding
import henyard.dodgerush.dewpond.game.Achievement
import henyard.dodgerush.dewpond.util.AppPrefs

/** Renders the achievement list: numbered title, description, progress bar and
 * a gold/gray star depending on completion. */
class AchievementAdapter(
    private val items: List<Achievement>,
    private val prefs: AppPrefs,
) : RecyclerView.Adapter<AchievementAdapter.Holder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemAchievementBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) =
        holder.bind(position + 1, items[position], prefs)

    override fun getItemCount(): Int = items.size

    class Holder(private val binding: ItemAchievementBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(number: Int, item: Achievement, prefs: AppPrefs) {
            val ctx = binding.root.context
            val done = item.isComplete(prefs)

            binding.achTitle.text = ctx.getString(
                R.string.ach_numbered_title, number, ctx.getString(item.titleRes)
            )
            binding.achDesc.setText(item.descRes)
            binding.achProgress.progress = item.percent(prefs)

            binding.achRoot.setBackgroundResource(
                if (done) R.drawable.ach_plate_done else R.drawable.ach_plate
            )
            binding.achStar.setImageResource(
                if (done) R.drawable.star_active else R.drawable.star_inactive
            )
        }
    }
}
