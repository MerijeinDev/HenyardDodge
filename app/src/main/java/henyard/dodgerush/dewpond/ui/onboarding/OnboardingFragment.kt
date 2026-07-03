package henyard.dodgerush.dewpond.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import henyard.dodgerush.dewpond.R
import henyard.dodgerush.dewpond.databinding.FragmentOnboardingBinding
import henyard.dodgerush.dewpond.databinding.ItemOnboardingBinding
import henyard.dodgerush.dewpond.ui.base.BaseFragment
import henyard.dodgerush.dewpond.util.AppPrefs

/**
 * First-run "how to play" tutorial (portrait). Three swipeable pages; the button
 * advances to the next page and, on the last page, finishes onboarding and
 * continues to the main menu. Marked as seen so it never shows again.
 */
class OnboardingFragment : BaseFragment(R.layout.fragment_onboarding) {

    private data class Page(val imageRes: Int, val textRes: Int, val labelRes: Int)

    private val pages = listOf(
        Page(R.drawable.onboarding_1, R.string.onboarding_1, R.string.next),
        Page(R.drawable.onboarding_2, R.string.onboarding_2, R.string.next),
        Page(R.drawable.onboarding_3, R.string.onboarding_3, R.string.start),
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentOnboardingBinding.bind(view)

        binding.onboardingPager.adapter = OnboardingAdapter(pages) { position ->
            if (position < pages.lastIndex) {
                binding.onboardingPager.currentItem = position + 1
            } else {
                finishOnboarding()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val pager = binding.onboardingPager
                    if (pager.currentItem > 0) {
                        pager.currentItem = pager.currentItem - 1
                    } else {
                        finishOnboarding()
                    }
                }
            })
    }

    private fun finishOnboarding() {
        if (!isAdded) return
        AppPrefs(requireContext()).onboardingSeen = true
        val nav = findNavController()
        if (nav.currentDestination?.id == R.id.onboardingFragment) {
            nav.navigate(R.id.action_onboarding_to_choose_hen)
        }
    }

    private class OnboardingAdapter(
        private val pages: List<Page>,
        private val onButtonClick: (position: Int) -> Unit,
    ) : RecyclerView.Adapter<OnboardingAdapter.PageHolder>() {

        class PageHolder(val binding: ItemOnboardingBinding) :
            RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageHolder {
            val binding = ItemOnboardingBinding.inflate(
                LayoutInflater.from(parent.context), parent, false,
            )
            return PageHolder(binding)
        }

        override fun getItemCount(): Int = pages.size

        override fun onBindViewHolder(holder: PageHolder, position: Int) {
            val page = pages[position]
            holder.binding.imageOb.setImageResource(page.imageRes)
            holder.binding.textOb.setText(page.textRes)
            holder.binding.btnNext.label.setText(page.labelRes)
            holder.binding.btnNext.root.setOnClickListener {
                onButtonClick(holder.bindingAdapterPosition)
            }
        }
    }
}
