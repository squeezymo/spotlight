package me.squeezymo.demo.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import me.squeezymo.demo.R
import me.squeezymo.demo.domain.Movie
import me.squeezymo.demo.domain.MovieTooltip
import me.squeezymo.demo.ui.contract.FabMenuOwner
import me.squeezymo.demo.ui.itemdecoration.SimpleSpacingItemDecoration
import me.squeezymo.demo.ui.outlineprovider.RoundedCornersOutlineProvider
import me.squeezymo.demo.ui.util.createBeatAnimation
import me.squeezymo.demo.ui.util.dp
import me.squeezymo.demo.ui.util.showTooltip
import me.squeezymo.demo.ui.widget.MovieWidget
import me.squeezymo.spotlight.overlay.AccentParams
import me.squeezymo.spotlight.overlay.ext.spotlightController

class MoviesFragment : Fragment() {

    private lateinit var moviesRcv: RecyclerView
    private lateinit var layoutManager: LinearLayoutManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.f_movies, container, false)

        setupViews(view)

        return view
    }

    private fun setupViews(view: View) {
        layoutManager = object : LinearLayoutManager(requireContext()) {
            private var mustShowTour = true

            override fun onLayoutCompleted(state: RecyclerView.State?) {
                super.onLayoutCompleted(state)

                if (mustShowTour && moviesRcv.adapter?.itemCount.let { it != null && it > 0 }) {
                    mustShowTour = false
                    view.postDelayed(
                        {
                            tryToStartTour()
                        },
                        500L
                    )
                }
            }
        }

        moviesRcv = view.findViewById<RecyclerView>(R.id.rcv).apply {
            layoutManager = this@MoviesFragment.layoutManager
            adapter = MoviesAdapter(
                ::showRatingTooltip,
                ::showFavouriteTooltip,
                ::showShareTooltip,
                ::showPosterTooltip
            )
            addItemDecoration(
                SimpleSpacingItemDecoration(
                    orientation = RecyclerView.VERTICAL,
                    spacePx = dp(8),
                    spaceBeforeFirstPx = dp(16),
                    spaceAfterLastPx = dp(16)
                )
            )
            addItemDecoration(
                SimpleSpacingItemDecoration(
                    orientation = RecyclerView.HORIZONTAL,
                    spacePx = dp(16)
                )
            )
        }
    }

    private fun tryToStartTour() {
        val view = layoutManager.getChildAt(layoutManager.findFirstCompletelyVisibleItemPosition())

        if (view is MovieWidget) {
            showRatingTooltip(view, isTourMode = true)
        }
    }

    private fun showRatingTooltip(widget: MovieWidget, isTourMode: Boolean = false) {
        val anchor = widget.getAnchorForTooltip(MovieTooltip.RATING)

        spotlightController.showTooltip(
            anchor = anchor,
            accentShape = AccentParams.Shape.Oval(),
            tooltipText = "A movie rating as per IMDb",
            distancePx = dp(16),
            onAcknowledged = {
                if (isTourMode) {
                    showFavouriteTooltip(widget, isTourMode)
                } else {
                    spotlightController.hide()
                }
            },
            onTooltipShown = {
                createBeatAnimation(anchor).start()
            }
        )
    }

    private fun showFavouriteTooltip(widget: MovieWidget, isTourMode: Boolean = false) {
        val anchor = widget.getAnchorForTooltip(MovieTooltip.FAVOURITE)

        spotlightController.showTooltip(
            anchor = anchor,
            accentShape = AccentParams.Shape.Oval(),
            tooltipText = "Only the movies you explicitly like will affect your recommendations",
            distancePx = dp(16),
            onAcknowledged = { tooltipWidget ->
                tooltipWidget.setText("You can find them in your profile")
                tooltipWidget.setOnAcknowledgedListener {
                    if (isTourMode) {
                        showShareTooltip(widget, isTourMode)
                    } else {
                        spotlightController.hide()
                    }
                }
            },
            onTooltipShown = {
                createBeatAnimation(anchor).start()
            }
        )
    }

    private fun showShareTooltip(widget: MovieWidget, isTourMode: Boolean = false) {
        val anchor = widget.getAnchorForTooltip(MovieTooltip.SHARE)

        spotlightController.showTooltip(
            anchor = anchor,
            accentShape = AccentParams.Shape.ExactViewShape(),
            tooltipText = "You can share a link to this movie",
            distancePx = dp(4),
            onAcknowledged = {
                if (isTourMode) {
                    showPosterTooltip(widget, isTourMode)
                } else {
                    spotlightController.hide()
                }
            },
            onTooltipShown = {
                createBeatAnimation(anchor).start()
            }
        )
    }

    private fun showPosterTooltip(widget: MovieWidget, isTourMode: Boolean = false) {
        val anchor = widget.getAnchorForTooltip(MovieTooltip.POSTER)

        spotlightController.showTooltip(
            anchor = anchor,
            accentShape = AccentParams.Shape.ExactViewShape(),
            tooltipText = "This is an original movie poster",
            distancePx = dp(8),
            onAcknowledged = {
                val fabMenuOwner = requireActivity() as? FabMenuOwner

                if (isTourMode && fabMenuOwner != null) {
                    showFabMenuTooltip(fabMenuOwner, isTourMode)
                } else {
                    spotlightController.hide()
                }
            }
        )
    }

    private fun showFabMenuTooltip(fabMenuOwner: FabMenuOwner, isTourMode: Boolean = false) {
        spotlightController.showTooltip(
            anchor = fabMenuOwner.getFloatingActionMenu(),
            accentShape = AccentParams.Shape.ExactViewShape(),
            tooltipText = "Use this menu for additional options",
            distancePx = dp(4),
            onAcknowledged = {
                if (isTourMode) {
                    fabMenuOwner.expandMenu(shouldExpand = true, isTourMode = isTourMode)
                } else {
                    spotlightController.hide()
                }
            }
        )
    }

    private class MoviesAdapter(
        private val onRatingClickListener: (MovieWidget) -> Unit,
        private val onFavouriteToggledListener: (MovieWidget) -> Unit,
        private val onShareClickListener: (MovieWidget) -> Unit,
        private val onPosterClickListener: (MovieWidget) -> Unit
    ) : RecyclerView.Adapter<MovieHolder>() {

        private val movies = Movie.createDefaultList()

        override fun getItemCount() = movies.size

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): MovieHolder {
            return MovieHolder.create(
                parent,
                onRatingClickListener,
                onFavouriteToggledListener,
                onShareClickListener,
                onPosterClickListener
            )
        }

        override fun onBindViewHolder(
            holder: MovieHolder,
            position: Int
        ) {
            holder.bind(movies[position])
        }

    }

    private class MovieHolder private constructor(
        itemView: MovieWidget
    ) : RecyclerView.ViewHolder(itemView) {

        private val widget = itemView

        fun bind(movie: Movie) {
            widget.setState(movie)
        }

        companion object {

            fun create(
                parent: ViewGroup,
                onRatingClickListener: (MovieWidget) -> Unit,
                onFavouriteToggledListener: (MovieWidget) -> Unit,
                onShareClickListener: (MovieWidget) -> Unit,
                onPosterClickListener: (MovieWidget) -> Unit
            ): MovieHolder {
                // For simplicity's sake, MovieWidget extends ConstraintLayout.
                // Normally it is not a good idea to use ConstraintLayout in RecyclerView
                // due to performance implications.

                val widget = MovieWidget(parent.context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )

                    setPaddingRelative(dp(8), dp(12), dp(8), dp(12))

                    clipToOutline = true
                    outlineProvider = RoundedCornersOutlineProvider(dp(8).toFloat())

                    setOnRatingClickListener(onRatingClickListener)
                    setOnFavouriteToggledListener(onFavouriteToggledListener)
                    setOnShareClickListener(onShareClickListener)
                    setOnPosterClickListener(onPosterClickListener)
                }

                return MovieHolder(widget)
            }

        }

    }

}
