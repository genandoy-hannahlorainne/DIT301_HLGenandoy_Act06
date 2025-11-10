package com.example.apiconnectapp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.apiconnectapp.R
import com.example.apiconnectapp.data.recent.RecentSearchRepository
import com.example.apiconnectapp.databinding.FragmentHomeBinding
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: RecentAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        loadRecentQueries()
    }

    private fun setupRecyclerView() {
        adapter = RecentAdapter { query ->
            findNavController().navigate(R.id.navigation_dashboard, Bundle().apply {
                putString(KEY_SELECTED_QUERY, query)
            })
        }
        binding.recentRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recentRecyclerView.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        loadRecentQueries()
    }

    private fun loadRecentQueries() {
        lifecycleScope.launch {
            val recents = RecentSearchRepository.getRecentQueries(requireContext())
            binding.emptyRecentTextView.isVisible = recents.isEmpty()
            adapter.submitList(recents)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val KEY_SELECTED_QUERY = "selected_query"
    }
}

