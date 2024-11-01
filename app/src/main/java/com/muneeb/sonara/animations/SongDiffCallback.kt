package com.muneeb.sonara.animations

import androidx.recyclerview.widget.DiffUtil
import com.muneeb.sonara.Song

class SongDiffCallback(
    private val oldList: List<Song>,
    private val newList: List<Song>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id // Compare unique IDs
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        // Compare the contents of the items, ensuring Song class implements equals() and hashCode()
        return oldList[oldItemPosition].title == newList[newItemPosition].title &&
                oldList[oldItemPosition].artist == newList[newItemPosition].artist &&
                oldList[oldItemPosition].albumCover == newList[newItemPosition].albumCover &&
                oldList[oldItemPosition].duration == newList[newItemPosition].duration
    }
}
