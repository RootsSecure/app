package com.rootssecure.nriplotsentinel.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.rootssecure.nriplotsentinel.api.EventHistoryItem
import com.rootssecure.nriplotsentinel.databinding.ItemEventHistoryBinding

class EventHistoryAdapter : ListAdapter<EventHistoryItem, EventHistoryAdapter.EventHistoryViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventHistoryViewHolder {
        val binding = ItemEventHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventHistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EventHistoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class EventHistoryViewHolder(
        private val binding: ItemEventHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: EventHistoryItem) {
            binding.alertTypeText.text = item.alertType.replace('_', ' ').replaceFirstChar { it.uppercase() }
            binding.occurredAtText.text = item.occurredAt.ifBlank { "Unknown time" }

            val imageUrl = item.mediaRefs.firstOrNull()
            binding.eventImage.load(imageUrl) {
                crossfade(true)
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<EventHistoryItem>() {
        override fun areItemsTheSame(oldItem: EventHistoryItem, newItem: EventHistoryItem): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: EventHistoryItem, newItem: EventHistoryItem): Boolean = oldItem == newItem
    }
}
