package com.example.apiconnectapp.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.apiconnectapp.databinding.ItemRecentQueryBinding

class RecentAdapter(
    private val onSelect: (String) -> Unit
) : ListAdapter<String, RecentAdapter.RecentViewHolder>(Diff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentViewHolder {
        val binding = ItemRecentQueryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecentViewHolder(binding, onSelect)
    }

    override fun onBindViewHolder(holder: RecentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class RecentViewHolder(
        private val binding: ItemRecentQueryBinding,
        private val onSelect: (String) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(query: String) {
            binding.queryTextView.text = query
            binding.root.setOnClickListener { onSelect(query) }
        }
    }

    private object Diff : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String) = oldItem == newItem
        override fun areContentsTheSame(oldItem: String, newItem: String) = oldItem == newItem
    }
}

