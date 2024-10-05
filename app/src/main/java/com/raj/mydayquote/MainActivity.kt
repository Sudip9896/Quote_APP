package com.raj.mydayquote

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.raj.mydayquote.databinding.ActivityMainBinding
import com.raj.mydayquote.item.Quote
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d("MainActivity", "onCreate: Activity Created")
        fetchQuoteOfTheDay()
        binding.newQuoteButton.setOnClickListener {
            checkInternetAndFetchQuote()
        }
        binding.shareImageView.setOnClickListener {
                shareQuotes()
        }
    }
    private fun checkInternetAndFetchQuote() {
        if (isInternetAvailable()) {
            fetchQuoteOfTheDay()
        } else {
            showNoInternetDialog()
        }
    }

    private fun shareQuotes() {

        if (binding.quoteTextView.text.isNotEmpty()) {
            val shareText = binding.quoteTextView.text.toString()

            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, shareText)
                type = "text/plain"
            }


            startActivity(Intent.createChooser(shareIntent, "Share quote via"))
        } else {

            Toast.makeText(this, "No quote to share", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchQuoteOfTheDay() {
        Log.d("MainActivity", "fetchQuoteOfTheDay: Starting API call")
        binding.progressBar.visibility = View.VISIBLE
        binding.quoteTextView.visibility = View.GONE
        binding.authorTextView.visibility = View.GONE
        setInProgress(true)


        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.quoteApi.getQuoteOfTheDay()
                if (response.isSuccessful) {
                    response.body()?.firstOrNull()?.let {
                        setUi(it)
                        binding.progressBar.visibility = View.GONE
                        binding.quoteTextView.visibility = View.VISIBLE
                        binding.authorTextView.visibility = View.VISIBLE

                    } ?: run {
                        Log.e("MainActivity", "No quotes found")
                    }
                } else {
                    Log.e("MainActivity", "Error: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "API call failed: ${e.message}")
            } finally {
                setInProgress(false)
            }
        }
    }

    private fun setUi(quote: Quote) {
        binding.quoteTextView.text = quote.q
        binding.authorTextView.text = quote.a
    }

    private fun setInProgress(inProgress: Boolean) {
        binding.quoteTextView.visibility = if (inProgress) View.GONE else View.VISIBLE
        binding.authorTextView.visibility = if (inProgress) View.GONE else View.VISIBLE
    }

    // to check internet || add  permission in network state
    private fun isInternetAvailable(): Boolean {
        // manager to call
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                    // manager check if any network is active
        val activeNetwork = connectivityManager.activeNetwork ?: return false
                            // manager also check  network capabilities || dont specify  all
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        // return the Internet capability
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
    private fun showNoInternetDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("No Internet Connection")
        builder.setMessage("Please check your internet connection and try again.")
        builder.setPositiveButton("Open Settings") { dialog, _ ->
            startActivity(Intent(Settings.ACTION_SETTINGS))
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()
    }
}
