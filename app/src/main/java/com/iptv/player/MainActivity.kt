// com/iptvplayer/app/ui/MainActivity.kt
package com.iptvplayer.app.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.iptvplayer.app.R
import com.iptvplayer.app.data.model.*
import com.iptvplayer.app.databinding.ActivityMainBinding
import com.iptvplayer.app.ui.adapter.CategoryAdapter
import com.iptvplayer.app.ui.adapter.ContentAdapter
import com.iptvplayer.app.ui.viewmodel.MainViewModel
import com.iptvplayer.app.util.AdManager

class MainActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_HOST = "extra_host"
        const val EXTRA_USERNAME = "extra_username"
        const val EXTRA_PASSWORD = "extra_password"
    }

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    private lateinit var contentAdapter: ContentAdapter
    private lateinit var categoryAdapter: CategoryAdapter

    private var currentTab = ContentType.LIVE
    private var selectedCategoryId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        // Extract and store credentials
        val host = intent.getStringExtra(EXTRA_HOST) ?: ""
        val username = intent.getStringExtra(EXTRA_USERNAME) ?: ""
        val password = intent.getStringExtra(EXTRA_PASSWORD) ?: ""
        viewModel.setCredentials(LoginCredentials(host, username, password))

        setupRecyclerViews()
        setupTabs()
        setupSearch()
        setupSwipeRefresh()
        setupObservers()

        // Load initial data
        loadDataForCurrentTab()
    }

    private fun setupRecyclerViews() {
        categoryAdapter = CategoryAdapter { category ->
            selectedCategoryId = category.categoryId
            loadStreamsForCategory(category.categoryId)
        }

        contentAdapter = ContentAdapter { item ->
            val creds = viewModel.getCredentials() ?: return@ContentAdapter
            val (title, url) = when (item) {
                is LiveStream -> Pair(
                    item.name,
                    com.iptvplayer.app.data.api.XtreamUrlBuilder.liveStreamUrl(creds, item.streamId)
                )
                is VodStream -> Pair(
                    item.name,
                    com.iptvplayer.app.data.api.XtreamUrlBuilder.vodStreamUrl(
                        creds, item.streamId, item.containerExtension
                    )
                )
                else -> return@ContentAdapter
            }
            val thumbnail = when (item) {
                is LiveStream -> item.streamIcon
                is VodStream -> item.streamIcon
                else -> null
            }
            launchPlayer(title, url, currentTab, thumbnail)
        }

        binding.rvCategories.apply {
            layoutManager = LinearLayoutManager(
                this@MainActivity, LinearLayoutManager.HORIZONTAL, false
            )
            adapter = categoryAdapter
        }

        binding.rvContent.apply {
            layoutManager = GridLayoutManager(this@MainActivity, 2)
            adapter = contentAdapter
        }
    }

    private fun setupTabs() {
        binding.tabLayout.apply {
            addTab(newTab().setText("🔴 Live TV").setTag(ContentType.LIVE))
            addTab(newTab().setText("🎬 Movies").setTag(ContentType.MOVIE))
            addTab(newTab().setText("📺 Series").setTag(ContentType.SERIES))

            addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    currentTab = tab?.tag as? ContentType ?: ContentType.LIVE
                    selectedCategoryId = null
                    categoryAdapter.clearSelection()
                    loadDataForCurrentTab()
                }
                override fun onTabUnselected(tab: TabLayout.Tab?) {}
                override fun onTabReselected(tab: TabLayout.Tab?) {}
            })
        }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                contentAdapter.filter(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeResources(R.color.accent_primary)
        binding.swipeRefresh.setOnRefreshListener {
            selectedCategoryId = null
            categoryAdapter.clearSelection()
            loadDataForCurrentTab()
        }
    }

    private fun setupObservers() {
        // Live TV
        viewModel.liveCategories.observe(this) { resource ->
            when (resource) {
                is Resource.Success -> categoryAdapter.submitList(resource.data.map {
                    GenericCategory(it.categoryId, it.categoryName)
                })
                is Resource.Error -> showError(resource.message)
                else -> {}
            }
        }

        viewModel.liveStreams.observe(this) { resource ->
            handleContentResource(resource) { data ->
                contentAdapter.submitLiveStreams(data)
            }
        }

        // VOD
        viewModel.vodCategories.observe(this) { resource ->
            when (resource) {
                is Resource.Success -> categoryAdapter.submitList(resource.data.map {
                    GenericCategory(it.categoryId, it.categoryName)
                })
                is Resource.Error -> showError(resource.message)
                else -> {}
            }
        }

        viewModel.vodStreams.observe(this) { resource ->
            handleContentResource(resource) { data ->
                contentAdapter.submitVodStreams(data)
            }
        }

        // Series
        viewModel.seriesCategories.observe(this) { resource ->
            when (resource) {
                is Resource.Success -> categoryAdapter.submitList(resource.data.map {
                    GenericCategory(it.categoryId, it.categoryName)
                })
                is Resource.Error -> showError(resource.message)
                else -> {}
            }
        }

        viewModel.seriesList.observe(this) { resource ->
            handleContentResource(resource) { data ->
                contentAdapter.submitSeriesList(data)
            }
        }
    }

    private fun <T> handleContentResource(resource: Resource<T>, onSuccess: (T) -> Unit) {
        binding.swipeRefresh.isRefreshing = false
        when (resource) {
            is Resource.Loading -> {
                binding.progressBar.visibility = View.VISIBLE
                binding.tvEmpty.visibility = View.GONE
            }
            is Resource.Success -> {
                binding.progressBar.visibility = View.GONE
                onSuccess(resource.data)
                binding.tvEmpty.visibility =
                    if (contentAdapter.itemCount == 0) View.VISIBLE else View.GONE
            }
            is Resource.Error -> {
                binding.progressBar.visibility = View.GONE
                binding.tvEmpty.visibility = View.VISIBLE
                showError(resource.message)
            }
        }
    }

    private fun loadDataForCurrentTab() {
        binding.progressBar.visibility = View.VISIBLE
        when (currentTab) {
            ContentType.LIVE -> {
                viewModel.loadLiveCategories()
                viewModel.loadLiveStreams()
            }
            ContentType.MOVIE -> {
                viewModel.loadVodCategories()
                viewModel.loadVodStreams()
            }
            ContentType.SERIES -> {
                viewModel.loadSeriesCategories()
                viewModel.loadSeries()
            }
        }
    }

    private fun loadStreamsForCategory(categoryId: String) {
        when (currentTab) {
            ContentType.LIVE -> viewModel.loadLiveStreams(categoryId)
            ContentType.MOVIE -> viewModel.loadVodStreams(categoryId)
            ContentType.SERIES -> viewModel.loadSeries(categoryId)
        }
    }

    private fun launchPlayer(
        title: String,
        url: String,
        type: ContentType,
        thumbnail: String?
    ) {
        AdManager.showInterstitialOnce(this) {
            val intent = Intent(this, PlayerActivity::class.java).apply {
                putExtra(PlayerActivity.EXTRA_STREAM_TITLE, title)
                putExtra(PlayerActivity.EXTRA_STREAM_URL, url)
                putExtra(PlayerActivity.EXTRA_CONTENT_TYPE, type.name)
                thumbnail?.let { putExtra(PlayerActivity.EXTRA_THUMBNAIL, it) }
            }
            startActivity(intent)
        }
    }

    private fun showError(message: String) {
        com.google.android.material.snackbar.Snackbar
            .make(binding.root, message, com.google.android.material.snackbar.Snackbar.LENGTH_LONG)
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_disclaimer -> {
                startActivity(Intent(this, DisclaimerActivity::class.java))
                true
            }
            R.id.action_logout -> {
                confirmLogout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun confirmLogout() {
        AlertDialog.Builder(this, R.style.DarkAlertDialog)
            .setTitle("Log Out")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Log Out") { _, _ ->
                viewModel.logout()
                AdManager.resetInterstitialFlag()
                val intent = Intent(this, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}

// Unified category wrapper used by CategoryAdapter
data class GenericCategory(val categoryId: String, val categoryName: String)
