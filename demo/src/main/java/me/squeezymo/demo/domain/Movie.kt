package me.squeezymo.demo.domain

import androidx.annotation.DrawableRes
import androidx.annotation.IntRange
import me.squeezymo.demo.R

data class Movie(
    val title: String,
    val releaseYear: Int,
    val director: String,
    val synopsis: String,
    @IntRange(from = 0L, to = 100L) val rating: Int,
    @DrawableRes val posterResId: Int
) {

    companion object {

        fun createDefaultList(): List<Movie> {
            return listOf(
                Movie(
                    title = "Forrest Gump",
                    releaseYear = 1994,
                    director = "Robert Zemeckis",
                    synopsis =
                    """
                        Slow-witted Forrest Gump has never thought of himself as disadvantaged, and thanks to his supportive mother, he leads anything but a restricted life. 
                        
                        Whether dominating on the gridiron as a college football star, fighting in Vietnam or captaining a shrimp boat, Forrest inspires people with his childlike optimism. But one person Forrest cares about most may be the most difficult to save -- his childhood love, the sweet but troubled Jenny.        
                        """.trimIndent(),
                    rating = 88,
                    posterResId = R.drawable.poster_forrest_gump
                ),
                Movie(
                    title = "Pulp Fiction",
                    releaseYear = 1994,
                    director = "Quentin Tarantino",
                    synopsis =
                    """
                        The lives of two mob hitmen, a boxer, a gangster and his wife, and a pair of diner bandits intertwine in four tales of violence and redemption.
                        """.trimIndent(),
                    rating = 89,
                    posterResId = R.drawable.poster_pulp_fiction
                ),
                Movie(
                    title = "Jaws",
                    releaseYear = 1974,
                    director = "Steven Spielberg",
                    synopsis =
                    """
                        When a killer shark unleashes chaos on beach community off Long Island, it's up to a local sheriff, a marine biologist, and an old seafarer to hunt the beast down. It's a hot summer on Amity Island, a small community whose main business is its beaches.
                        """.trimIndent(),
                    rating = 80,
                    posterResId = R.drawable.poster_jaws
                ),
                Movie(
                    title = "Back to the Future",
                    releaseYear = 1985,
                    director = "Robert Zemeckis",
                    synopsis =
                    """
                        Marty McFly, a typical American teenager of the Eighties, is accidentally sent back to 1955 in a plutonium-powered DeLorean "time machine" invented by a slightly mad scientist. 
                        
                        During his often hysterical, always amazing trip back in time, Marty must make certain his teenage parents-to-be meet and fall in love - so he can get back to the future.    
                        """.trimIndent(),
                    rating = 85,
                    posterResId = R.drawable.poster_back_to_the_future
                )
            )
        }

    }

}
