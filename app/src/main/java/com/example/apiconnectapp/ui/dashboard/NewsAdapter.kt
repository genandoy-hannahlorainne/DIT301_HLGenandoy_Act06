package com.example.apiconnectapp.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.apiconnectapp.R
import com.example.apiconnectapp.data.news.Article
import com.example.apiconnectapp.databinding.ItemArticleBinding

class NewsAdapter(
    private val onArticleSelected: (String) -> Unit
) : ListAdapter<Article, NewsAdapter.NewsViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val binding = ItemArticleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NewsViewHolder(binding, onArticleSelected)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class NewsViewHolder(
        private val binding: ItemArticleBinding,
        private val onArticleSelected: (String) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(article: Article) {
            val context = binding.root.context

            binding.titleTextView.text = article.title?.takeUnless { it.isBlank() }
                ?: context.getString(R.string.article_untitled)

            val description = article.description?.takeUnless { it.isBlank() }
                ?: context.getString(R.string.article_no_description)
            binding.descriptionTextView.isVisible = true
            binding.descriptionTextView.text = description

            val sourceName = article.source?.name?.takeUnless { it.isBlank() }
            binding.sourceTextView.isVisible = !sourceName.isNullOrBlank()
            binding.sourceTextView.text = sourceName?.let {
                context.getString(R.string.source_format, it)
            } ?: ""

            binding.root.contentDescription =
                context.getString(R.string.news_item_content_description)

            val articleUrl = article.url
            binding.root.isEnabled = !articleUrl.isNullOrBlank()
            binding.root.setOnClickListener {
                if (!articleUrl.isNullOrBlank()) {
                    onArticleSelected(articleUrl)
                }
            }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<Article>() {
        override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem.url == newItem.url
        }

        override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem == newItem
        }
    }
}

