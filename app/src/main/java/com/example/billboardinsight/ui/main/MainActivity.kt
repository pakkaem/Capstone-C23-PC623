package com.example.billboardinsight.ui.main

import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.example.billboardinsight.R
import com.example.billboardinsight.ViewModelFactory
import com.example.billboardinsight.databinding.ActivityMainBinding
import com.example.billboardinsight.model.UserPreference
import com.example.billboardinsight.ui.login.LoginActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user")

class MainActivity : AppCompatActivity() {
    companion object {
        const val URL_VIDEO_RECOGNITION = "https://github.com/pakkaem/Capstone-C23-PC623/blob/master/vehiclerecognition.mp4"
    }

    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private lateinit var mainViewModel: MainViewModel
    private lateinit var auth: FirebaseAuth

    private lateinit var barChart: BarChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        val player = ExoPlayer.Builder(this).build()
        viewBinding.videoView.player = player

        val mediaItem = MediaItem.fromUri(URL_VIDEO_RECOGNITION)
        player.setMediaItem(mediaItem)
        player.prepare()


        mainViewModel = ViewModelProvider(
            this@MainActivity,
            ViewModelFactory.getInstance(UserPreference.getInstance(dataStore))
        )[MainViewModel::class.java]

        val items = listOf("5 menit", "10 menit", "15 menit")
        val adapter = ArrayAdapter(this, R.layout.list_interval, items)
        viewBinding.dropdownInterval.setAdapter(adapter)

        auth = Firebase.auth
        val firebaseUser = auth.currentUser
        if (firebaseUser == null) {
            // Not signed in, launch the Login activity
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        barChart = findViewById(R.id.barChart)

        // Create sample data entries
        val entries = listOf(
            BarEntry(1f, 10f),
            BarEntry(2f, 20f),
            BarEntry(3f, 30f),
            BarEntry(4f, 40f),
            BarEntry(5f, 50f)
        )

        // Create a dataset with the entries
        val dataSet = BarDataSet(entries, "Sample Dataset")
        dataSet.color = Color.BLUE

        // Create a BarData object with the dataset
        val barData = BarData(dataSet)

        // Customize the chart appearance
        barChart.apply {
            data = barData
            description.isEnabled = false
            setDrawValueAboveBar(true)
            axisRight.isEnabled = false
            xAxis.setDrawGridLines(false)
            axisLeft.setDrawGridLines(false)
            animateY(1000)
            invalidate()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.option_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

            R.id.logout_menu -> {
                AlertDialog.Builder(this).apply {
                    setTitle(R.string.logout)
                    setMessage(R.string.logout_confirmation)
                    setPositiveButton(R.string.yes) { _, _ ->
                        mainViewModel.logout()
                        signOut()

                        val intent = Intent(this@MainActivity, LoginActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        startActivity(intent)
                        finish()
                    }
                    setNegativeButton(R.string.cancel) { _, _ ->

                    }
                    create()
                    show()
                }
            }
        }

        return true
    }

    private fun signOut() {
        auth.signOut()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}