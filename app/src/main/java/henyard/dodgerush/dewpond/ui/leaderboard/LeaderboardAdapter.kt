package henyard.dodgerush.dewpond.ui.leaderboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import henyard.dodgerush.dewpond.R
import henyard.dodgerush.dewpond.databinding.ItemLeaderboardBinding
import henyard.dodgerush.dewpond.game.LbEntry

/** Renders leaderboard rows: rank (medal for the top three), avatar, name and
 * score. The player's ("You") row is tinted green and sits a touch taller. */
class LeaderboardAdapter(
    private val items: List<LbEntry>,
) : RecyclerView.Adapter<LeaderboardAdapter.Holder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemLeaderboardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) =
        holder.bind(position + 1, items[position])

    override fun getItemCount(): Int = items.size

    class Holder(private val binding: ItemLeaderboardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(rank: Int, entry: LbEntry) {
            val medal = MEDALS.getOrNull(rank - 1)
            binding.lbMedal.visibility = if (medal != null) View.VISIBLE else View.GONE
            binding.lbRank.visibility = if (medal != null) View.GONE else View.VISIBLE
            medal?.let(binding.lbMedal::setImageResource)
            binding.lbRank.text = rank.toString()

            binding.lbAvatar.setImageResource(entry.avatarRes)
            binding.lbName.text = entry.name
            binding.lbScore.text = "%,d".format(entry.score)

            binding.lbRoot.setBackgroundResource(
                if (entry.isPlayer) R.drawable.lb_row_you else R.drawable.profile_stat_row
            )

            val density = binding.root.resources.displayMetrics.density
            val heightDp = if (medal != null) TOP_ROW_HEIGHT_DP else ROW_HEIGHT_DP
            binding.lbRoot.layoutParams = binding.lbRoot.layoutParams.apply {
                height = (heightDp * density).toInt()
            }
        }

        private companion object {
            val MEDALS = intArrayOf(
                R.drawable.lb_medal_1, R.drawable.lb_medal_2, R.drawable.lb_medal_3
            )
            const val ROW_HEIGHT_DP = 44
            const val TOP_ROW_HEIGHT_DP = 50
        }
    }
}
