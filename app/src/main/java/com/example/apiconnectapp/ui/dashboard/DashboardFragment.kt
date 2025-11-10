package com.example.apiconnectapp.ui.dashboard

import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.getSystemService
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.apiconnectapp.R
import com.example.apiconnectapp.ui.home.HomeFragment
import com.example.apiconnectapp.data.recent.RecentSearchRepository
import com.example.apiconnectapp.databinding.FragmentDashboardBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DashboardViewModel by viewModels()

    private lateinit var newsAdapter: NewsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearch()
        observeUiState()
        handleIncomingQuery()
    }

    private fun setupRecyclerView() {
        newsAdapter = NewsAdapter { url -> openArticle(url) }
        binding.articlesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = newsAdapter
        }
    }

    private fun setupSearch() {
        binding.searchButton.setOnClickListener { performSearch() }
        binding.searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch()
                true
            } else {
                false
            }
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.loadingProgressBar.isVisible = state.isLoading
                    binding.statusTextView.isVisible = state.showStatus
                    binding.statusTextView.text = state.statusMessageText
                        ?: state.statusMessageRes?.let { getString(it) }
                        ?: ""
                    newsAdapter.submitList(state.articles)

                    val transientMessage = state.transientMessage
                    if (!transientMessage.isNullOrBlank()) {
                        Snackbar.make(binding.root, transientMessage, Snackbar.LENGTH_LONG).show()
                        viewModel.onTransientMessageShown()
                    }

                    state.recentAddedQuery?.let { query ->
                        RecentSearchRepository.addRecentQuery(requireContext(), query)
                        viewModel.onRecentQueryHandled()
                    }
                }
            }
        }
    }

    private fun performSearch() {
        val query = binding.searchEditText.text?.toString().orEmpty()

        if (query.isBlank()) {
            Toast.makeText(
                requireContext(),
                getString(R.string.toast_enter_query),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (!hasNetworkConnection()) {
            Snackbar.make(
                binding.root,
                getString(R.string.status_error_network),
                Snackbar.LENGTH_LONG
            ).setAction(R.string.retry) {
                performSearch()
            }.show()
            return
        }

        hideKeyboard()
        viewModel.search(query)
    }

    private fun handleIncomingQuery() {
        val incomingQuery = arguments?.getString(HomeFragment.KEY_SELECTED_QUERY)
        if (!incomingQuery.isNullOrBlank()) {
            binding.searchEditText.setText(incomingQuery)
            performSearch()
            arguments?.remove(HomeFragment.KEY_SELECTED_QUERY)
        }
    }

    private fun hasNetworkConnection(): Boolean {
        val connectivityManager = requireContext().getSystemService<ConnectivityManager>()
            ?: return false
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
    }

    private fun openArticle(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        runCatching {
            startActivity(intent)
        }.onFailure {
            Snackbar.make(
                binding.root,
                getString(R.string.status_error_generic),
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService<InputMethodManager>()
        imm?.hideSoftInputFromWindow(binding.searchEditText.windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

