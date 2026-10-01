package com.example.stickmanalive

import android.app.AppOpsManager
import android.app.PictureInPictureParams
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.provider.Settings
import android.util.Rational
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var stickmanView: StickmanView
    private lateinit var editorPanel: LinearLayout
    private val currentRigData = StickmanData()
    
    private val handler = Handler(Looper.getMainLooper())
    private var wasYouTubeOpen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        stickmanView = findViewById(R.id.stickmanView)
        editorPanel = findViewById(R.id.editorPanel)

        val inputName = findViewById<EditText>(R.id.editName)
        val seekHead = findViewById<SeekBar>(R.id.seekHead)
        val seekTorso = findViewById<SeekBar>(R.id.seekTorso)
        val seekArms = findViewById<SeekBar>(R.id.seekArms)
        val btnColorRed = findViewById<Button>(R.id.btnRed)
        val btnColorBlue = findViewById<Button>(R.id.btnBlue)
        val btnSaveRig = findViewById<Button>(R.id.btnSaveRig)

        btnColorRed.setOnClickListener { currentRigData.skinColor = Color.RED }
        btnColorBlue.setOnClickListener { currentRigData.skinColor = Color.BLUE }

        seekHead.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar?, p: Int, b: Boolean) {
                currentRigData.headRadius = p.toFloat() + 20f
            }
            override fun onStartTrackingTouch(s: SeekBar?) {}
            override fun onStopTrackingTouch(s: SeekBar?) {}
        })

        seekTorso.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar?, p: Int, b: Boolean) {
                currentRigData.torsoLength = p.toFloat() + 40f
            }
            override fun onStartTrackingTouch(s: SeekBar?) {}
            override fun onStopTrackingTouch(s: SeekBar?) {}
        })

        seekArms.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar?, p: Int, b: Boolean) {
                currentRigData.armLength = p.toFloat() + 30f
                currentRigData.legLength = p.toFloat() + 35f
            }
            override fun onStartTrackingTouch(s: SeekBar?) {}
            override fun onStopTrackingTouch(s: SeekBar?) {}
        })

        btnSaveRig.setOnClickListener {
            val nameInput = inputName.text.toString()
            if (nameInput.isNotEmpty()) {
                currentRigData.name = nameInput
            }
            stickmanView.data = currentRigData.copy()
        }

        handler.post(appTrackerRunnable)
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (hasUsageStatsPermission()) {
            enterPictureInPictureModeWithRatio()
        } else {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }
    }

    private fun enterPictureInPictureModeWithRatio() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val aspectRatio = Rational(1, 1)
            val pipParams = PictureInPictureParams.Builder()
                .setAspectRatio(aspectRatio)
                .build()
            enterPictureInPictureMode(pipParams)
        }
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        if (isInPictureInPictureMode) {
            editorPanel.visibility = View.GONE
            stickmanView.setIsInPipMode(true)
        } else {
            editorPanel.visibility = View.VISIBLE
            stickmanView.setIsInPipMode(false)
        }
    }

    private val appTrackerRunnable = object : Runnable {
        override fun run() {
            if (hasUsageStatsPermission()) {
                checkForegroundApp()
            }
            handler.postDelayed(this, 1000)
        }
    }

    private fun checkForegroundApp() {
        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val time = System.currentTimeMillis()
        val stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 10000, time)
        
        if (!stats.isNullOrEmpty()) {
            val sortedStats = stats.sortedByDescending { it.lastTimeUsed }
            val currentTopApp = sortedStats.packageName
            val isYouTubeNow = currentTopApp == "com.google.android.youtube"

            if (wasYouTubeOpen && !isYouTubeNow) {
                stickmanView.triggerReaction(
                    StickmanExpression.KAGET_YOUTUBE, 
                    "lah youtubenya mana???"
                )
            }
            wasYouTubeOpen = isYouTubeNow
        }
    }

    private fun hasUsageStatsPermission(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.noteOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), packageName)
        return mode == AppOpsManager.MODE_ALLOWED
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(appTrackerRunnable)
    }
}
