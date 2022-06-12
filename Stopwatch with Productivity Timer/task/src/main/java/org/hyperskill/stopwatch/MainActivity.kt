package org.hyperskill.stopwatch

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import org.hyperskill.stopwatch.R
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Exception

const val ZERO = "00:00"
const val CHANNEL_ID = "org.hyperskill"

class MainActivity : AppCompatActivity() {
    private var timeLeft = 0
    private val handler = Handler(Looper.getMainLooper())
    val textView: TextView by lazy { findViewById(R.id.textView) }
    private var timeIsStarted = false
    val progressBar: ProgressBar by lazy { findViewById(R.id.progressBar) }
    var upperLimit: Int? = null

    val notificationBuilder: NotificationCompat.Builder =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_timer_24)
            .setContentTitle("Notification")
            .setContentText("Time exceed")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
    val notificationManager: NotificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        createNotificationChannel()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textView.text = ZERO
        progressBar.visibility = View.INVISIBLE

        startButton.setOnClickListener {
            startStopwatch()
            progressBar.visibility = View.VISIBLE
        }

        resetButton.setOnClickListener {
            resetStopwatch()
            progressBar.visibility = View.INVISIBLE
        }

        settingsButton.setOnClickListener {
            val contentView = LayoutInflater.from(this).inflate(R.layout.set_upper_limit, null, false)
            AlertDialog.Builder(this)
                .setTitle("Set upper limit in seconds")
                .setView(contentView)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    val editText = contentView.findViewById<EditText>(R.id.upperLimitEditText)

                    try {
                        upperLimit = editText.text.toString().toInt() + 1
                    } catch (e: Exception) { }
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }
    }

    private val updateTime: Runnable by lazy {
        object : Runnable {
            @SuppressLint("SetTextI18n")
            override fun run() {
                val seconds = timeLeft % 60
                val minutes = timeLeft / 60
                timeLeft++
                textView.text = "${if (timeLeft / 60 < 10) "0$minutes" else minutes }:" +
                        "${if (seconds < 10) "0$seconds" else seconds}"

                if (upperLimit != null && timeLeft >= upperLimit!!) textView.setTextColor(Color.RED)
                if (timeLeft >= upperLimit ?: 99*6000) notificationManager.notify(393939, notificationBuilder.build())

                val color = Color.argb(255, (timeLeft * 10) % 200, 155, 155)
                progressBar.indeterminateTintList= getNewColor(color)

                handler.postDelayed(this, 1000)
            }
        }
    }

    fun getNewColor(newColor: Int = 0xFF8C5AFF.toInt()): ColorStateList {
        return ColorStateList(arrayOf(intArrayOf()), intArrayOf(newColor))
    }

    private fun startStopwatch() {
        if (!timeIsStarted) {
            timeIsStarted = true
            super.onStart()
            handler.postDelayed(updateTime, 100)
            settingsButton.isEnabled = false
        }
    }

    private fun resetStopwatch() {
        timeIsStarted = false
        timeLeft = 0
        super.onStop()
        handler.removeCallbacks(updateTime)
        settingsButton.isEnabled = true
        textView.text = ZERO
        textView.setTextColor(Color.BLACK)
        upperLimit = null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Stopwatch", NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }
    }
}