package me.squeezymo.demo.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import me.squeezymo.demo.R
import me.squeezymo.demo.domain.Movie
import me.squeezymo.demo.domain.MovieTooltip
import me.squeezymo.demo.ui.outlineprovider.RoundedCornersOutlineProvider
import me.squeezymo.demo.ui.util.dp

class MovieWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    init {
        LayoutInflater
            .from(context)
            .inflate(R.layout.v_movie, this, true)

        setBackgroundColor(ContextCompat.getColor(context, R.color.bg_secondary))
    }

    lateinit var movie: Movie
        private set

    private val posterIv: ImageView = findViewById(R.id.poster_iv)
    private val titleTv: TextView = findViewById(R.id.title_tv)
    private val directorAndReleaseYearTv: TextView = findViewById(R.id.director_and_release_year_tv)
    private val synopsisTv: TextView = findViewById(R.id.synopsis_tv)
    private val ratingTv: TextView = findViewById(R.id.rating_tv)
    private val shareIv: ImageView = findViewById(R.id.share_iv)
    private val toggleFavIv: ImageView = findViewById(R.id.toggle_fav_iv)

    init {
        posterIv.clipToOutline = true
        posterIv.outlineProvider = RoundedCornersOutlineProvider(dp(6).toFloat())

        if (isInEditMode) {
            setState(Movie.createDefaultList()[0])
        }
    }

    fun setState(movie: Movie) {
        this.movie = movie

        posterIv.setImageResource(movie.posterResId)
        titleTv.text = movie.title
        directorAndReleaseYearTv.text = resources.getString(
            R.string.movie_director_and_release_year,
            movie.director,
            movie.releaseYear
        )
        synopsisTv.text = movie.synopsis
        ratingTv.text = resources.getString(R.string.movie_rating, movie.rating / 10F)
    }

    fun setOnRatingClickListener(listener: (widget: MovieWidget) -> Unit) {
        ratingTv.setOnClickListener {
            listener(this)
        }
    }

    fun setOnFavouriteToggledListener(listener: (widget: MovieWidget) -> Unit) {
        toggleFavIv.setOnClickListener {
            listener(this)
        }
    }

    fun setOnShareClickListener(listener: (widget: MovieWidget) -> Unit) {
        shareIv.setOnClickListener {
            listener(this)
        }
    }

    fun setOnPosterClickListener(listener: (widget: MovieWidget) -> Unit) {
        posterIv.setOnClickListener {
            listener(this)
        }
    }

    fun getAnchorForTooltip(tooltip: MovieTooltip): View {
        return when (tooltip) {
            MovieTooltip.POSTER -> posterIv
            MovieTooltip.RATING -> ratingTv
            MovieTooltip.FAVOURITE -> toggleFavIv
            MovieTooltip.SHARE -> shareIv
        }
    }

}
