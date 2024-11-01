package com.muneeb.sonara

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.muneeb.sonara.animations.SongDiffCallback
import java.util.Locale

class SongAdapter(
    private var songs: MutableList<Song>,
    private val onItemClick: (Song, Int) -> Unit
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    private var selectedIndex = -1
    private var filteredSongs: MutableList<Song> = songs.toMutableList()
    private var currentPlayingSongId: Long? = null

    inner class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val songTitle: TextView = itemView.findViewById(R.id.songTITLE)
        private val artistName: TextView = itemView.findViewById(R.id.songARTIST)
        private val albumCover: ImageView = itemView.findViewById(R.id.albumART)
        private val animationView: LottieAnimationView = itemView.findViewById(R.id.animation_view)
        private val songTimeTV: TextView = itemView.findViewById(R.id.songTimeTV)

        fun bind(song: Song, isSelected: Boolean) {
            songTitle.text = song.title
            artistName.text = song.artist
            songTimeTV.text = formatDuration(song.duration)

            Glide.with(itemView.context).load(song.albumCover)
//                .skipMemoryCache(false)
                .placeholder(R.drawable.black)
                .error(R.drawable.logo)
                .override(150,150)
                .into(albumCover)

            if (isSelected) {
                itemView.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.highlight_blue))
                songTitle.setTextColor(ContextCompat.getColor(itemView.context, R.color.text_color))
                artistName.setTextColor(ContextCompat.getColor(itemView.context, R.color.text_color))
                animationView.visibility = View.VISIBLE
                if (!animationView.isAnimating) {
                    animationView.playAnimation()
                }
            } else {
                itemView.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.transparent))
                songTitle.setTextColor(ContextCompat.getColor(itemView.context, R.color.text_color))
                artistName.setTextColor(ContextCompat.getColor(itemView.context, R.color.text_color))
                if (animationView.isAnimating) {
                    animationView.pauseAnimation()
                }
                animationView.visibility = View.GONE
            }

            itemView.setOnClickListener {
                onItemClick(song, layoutPosition)
                currentPlayingSongId = song.id
                clearSearch()
            }
        }

        private fun formatDuration(durationInMillis: Long): String {
            val minutes = (durationInMillis / 1000) / 60
            val seconds = (durationInMillis / 1000) % 60
            return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.song_card, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        holder.bind(filteredSongs[position], position == selectedIndex)
    }

    override fun getItemCount(): Int = filteredSongs.size

    fun updateSelectedIndex(newIndex: Int) {
        val previousIndex = selectedIndex
        selectedIndex = newIndex
        if (previousIndex != -1) notifyItemChanged(previousIndex)
        if (newIndex != -1) notifyItemChanged(newIndex)
    }

    fun filterSongs(query: String) {
        val newFilteredSongs = if (query.isEmpty()) {
            songs
        } else {
            songs.filter {
                it.title.contains(query, ignoreCase = true) || it.artist.contains(query, ignoreCase = true)
            }
        }

        updateFilteredList(newFilteredSongs)
    }

    private fun updateFilteredList(newFilteredSongs: List<Song>) {
        val diffCallback = SongDiffCallback(filteredSongs, newFilteredSongs)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        filteredSongs.clear()
        filteredSongs.addAll(newFilteredSongs)
        diffResult.dispatchUpdatesTo(this)

        updateSelectedIndexForCurrentPlayingSong()
    }

    fun clearSearch() {
        updateFilteredList(songs)
    }

    private fun updateSelectedIndexForCurrentPlayingSong() {
        currentPlayingSongId?.let { songId ->
            val indexInFiltered = filteredSongs.indexOfFirst { it.id == songId }
            selectedIndex = if (indexInFiltered != -1) indexInFiltered else -1
        } ?: run {
            selectedIndex = -1
        }
    }
}
