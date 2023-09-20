package com.legend.common.utils

import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.legend.base.Applications
import com.legend.common.ApplicationConst
import com.legend.commonres.R

/**
 *
 * @Date: 2023/7/19 21:54
 */
object NotificationManager {
    private val audioManager: AudioManager? by lazy {
        Applications.getCurrent().getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }
    
    fun playSound() {
        if (ApplicationConst.VOICE_ALL_MUTE || audioManager?.ringerMode == AudioManager.RINGER_MODE_SILENT) return
//        playRing()
        playRingDoda()
    }
    
    // 震动
    private fun vibrate() {
        val vibratePattern = longArrayOf(0, 200, 250, 200)
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = Applications.getCurrent().getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            Applications.getCurrent().getSystemService(VIBRATOR_SERVICE) as Vibrator
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            vibrator.vibrate(VibrationEffect.createWaveform(vibratePattern, 0))
        } else {
            vibrator.vibrate(vibratePattern, 0)
        }
    }
    
    private fun playRing() {
        val ringUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val mediaPlayer = MediaPlayer()
        mediaPlayer.setDataSource(Applications.getCurrent(), ringUri)
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_NOTIFICATION)
        mediaPlayer.isLooping = false
        mediaPlayer.prepare()
        mediaPlayer.start()
    }

    private fun playRingDoda() {val player = MediaPlayer.create(Applications.getCurrent(), R.raw.doda)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            player?.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                    .build()
            )
        } else {
            player?.setAudioStreamType(AudioManager.STREAM_MUSIC)
        }
        try {
            player?.setOnPreparedListener(MediaPlayer.OnPreparedListener { mp -> mp.start() })
            player?.prepareAsync()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}