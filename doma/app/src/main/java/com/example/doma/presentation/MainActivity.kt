package com.example.doma.presentation

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.example.doma.presentation.AudioHelper
import com.example.doma.NotificationListener
import com.example.doma.R
import java.util.*

class MainActivity : AppCompatActivity(), OnInitListener {
    private lateinit var audioManager: AudioManager
    private lateinit var audioHelper: AudioHelper
    private lateinit var tts: TextToSpeech

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioHelper = AudioHelper(this)
        tts = TextToSpeech(this, this)

        registerDeviceCallbacks()
        registerNotificationListenerService()
        startVoiceRecognition()

        // Exemplo de treinamento audível
        provideTrainingFeedback()

        // Exemplo de alerta de segurança
        sendSecurityAlert("Alerta de emergência: tome medidas imediatas!")
    }

    private fun registerDeviceCallbacks() {
        audioManager.registerAudioDeviceCallback(object : AudioDeviceCallback() {
            override fun onAudioDevicesAdded(addedDevices: Array<AudioDeviceInfo>) {
                if (audioHelper.audioOutputAvailable(AudioDeviceInfo.TYPE_BLUETOOTH_A2DP)) {
                    notifyUser("Bluetooth Headset Conectado")
                }
            }

            override fun onAudioDevicesRemoved(removedDevices: Array<AudioDeviceInfo>) {
                if (!audioHelper.audioOutputAvailable(AudioDeviceInfo.TYPE_BLUETOOTH_A2DP)) {
                    notifyUser("Bluetooth Headset Desconectado")
                }
            }
        }, null)
    }

    private fun notifyUser(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, "")
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.US
        }
    }

    override fun onDestroy() {
        if (tts != null) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }

    private fun registerNotificationListenerService() {
        startService(Intent(this, NotificationListener::class.java))
    }

    private fun startVoiceRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Fale algo")
        try {
            startActivityForResult(intent, 100)
        } catch (a: Exception) {
            Toast.makeText(applicationContext, "Seu dispositivo não oferece suporte ao reconhecimento de fala", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == RESULT_OK) {
            val result = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            result?.let {
                val spokenText = it[0]
                // Aqui você pode processar o texto falado e tomar ações com base nele
                notifyUser("Você disse: $spokenText")
            }
        }
    }

    private fun provideTrainingFeedback() {
        // Exemplo de feedback de treinamento
        val trainingSteps = arrayOf(
            "Etapa 1: Faça isso primeiro",
            "Etapa 2: Agora faça isso",
            "Etapa 3: Finalmente, faça isso"
        )

        for (step in trainingSteps) {
            tts.speak(step, TextToSpeech.QUEUE_ADD, null, "")
            Thread.sleep(3000) // Adicione um atraso entre os passos
        }
    }

    private fun sendSecurityAlert(message: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Crie o canal de notificação, necessário para Android O e superior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("security_alerts", "Security Alerts", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, "security_alerts")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Security Alert")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(1, notification)
        tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, "")
    }
}
