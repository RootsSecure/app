package com.rootssecure.nriplotsentinel.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rootssecure.nriplotsentinel.api.DeviceItem
import com.rootssecure.nriplotsentinel.databinding.ItemDeviceBinding

class DeviceAdapter : ListAdapter<DeviceItem, DeviceAdapter.DeviceViewHolder>(DeviceDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val binding = ItemDeviceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DeviceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DeviceViewHolder(
        private val binding: ItemDeviceBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DeviceItem) {
            binding.deviceName.text = item.name
            binding.deviceStatus.text = item.status.replaceFirstChar { it.uppercase() }
            binding.deviceBattery.text = item.batteryLevel?.let { "$it%" } ?: "N/A"
            binding.deviceLastSeen.text = item.lastSeen.ifBlank { "Unknown" }
        }
    }

    private class DeviceDiffCallback : DiffUtil.ItemCallback<DeviceItem>() {
        override fun areItemsTheSame(oldItem: DeviceItem, newItem: DeviceItem): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: DeviceItem, newItem: DeviceItem): Boolean = oldItem == newItem
    }
}
