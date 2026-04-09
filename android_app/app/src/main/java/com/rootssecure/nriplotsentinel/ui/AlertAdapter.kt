package com.rootssecure.nriplotsentinel.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rootssecure.nriplotsentinel.api.AlertItem
import com.rootssecure.nriplotsentinel.databinding.ItemAlertBinding

class AlertAdapter : ListAdapter<AlertItem, AlertAdapter.AlertViewHolder>(AlertDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertViewHolder {
        val binding = ItemAlertBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AlertViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlertViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class AlertViewHolder(
        private val binding: ItemAlertBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AlertItem) {
            binding.alertTitle.text = item.title
            binding.alertSeverity.text = item.severity.uppercase()
            binding.alertLocation.text = item.location
            binding.alertTime.text = item.occurredAt
            binding.alertStatus.text = item.status.replaceFirstChar { it.uppercase() }
        }
    }

    private class AlertDiffCallback : DiffUtil.ItemCallback<AlertItem>() {
        override fun areItemsTheSame(oldItem: AlertItem, newItem: AlertItem): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: AlertItem, newItem: AlertItem): Boolean = oldItem == newItem
    }
}
