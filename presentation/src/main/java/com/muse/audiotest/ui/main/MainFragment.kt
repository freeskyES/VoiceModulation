package com.muse.audiotest.ui.main

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Context
import android.content.pm.PackageManager
import android.media.*
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.muse.audiotest.databinding.FragmentMainBinding
import me.bogerchan.niervisualizer.NierVisualizerManager
import me.bogerchan.niervisualizer.renderer.circle.CircleBarRenderer
import me.bogerchan.niervisualizer.renderer.circle.CircleRenderer
import me.bogerchan.niervisualizer.renderer.columnar.ColumnarType4Renderer
import java.lang.Exception

class MainFragment : Fragment() {

    private lateinit var viewDataBinding: FragmentMainBinding
    lateinit var soundpool: SoundPool

    val viewModel = MainViewModel() // 주입으로 대체

    private val requestPermissions = arrayOf(
        android.Manifest.permission.RECORD_AUDIO,
        WRITE_EXTERNAL_STORAGE
    )

    private lateinit var recorder: MediaRecorder
    private var visualizerManager: NierVisualizerManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        viewDataBinding = FragmentMainBinding.inflate(inflater, container, false).apply {
            viewmodel = viewModel
        }
        return viewDataBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupLifecycleOwner()
        setupSoundPool()
        requestAudioPermission()
        setupEvents()
        setupSurfaceView()
    }

    private fun setupSurfaceView() {
        viewDataBinding.wave.apply {
            setZOrderOnTop(true)
//            holder.setFormat(PixelFormat.TRANSLUCENT)
        }
    }

    private fun setupLifecycleOwner() {
        viewDataBinding.lifecycleOwner = this.viewLifecycleOwner
    }

    private fun setupEvents() {
        viewDataBinding.recordButton.setOnClickListener {
            viewModel.setIsRecording(viewModel.isRecording.value != true)
        }

        viewModel.isRecording.observe(this.viewLifecycleOwner, Observer {
            if (it)
                startRecording()
            else
                stopAudio()
        })
    }


    private fun setupRecord() {
        recorder = MediaRecorder()
            .apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
//                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
//                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(viewModel.filePath)
            }
        recorder.prepare()
    }


    private fun startRecording() {
        setupRecord()
        recorder.start()
        stopVisualizer()
    }

    private fun stopAudio() {
        recorder.stop()
        recorder.release()
        playAudio(1.0f)
    }

    private fun playAudioForUrl(speed: Float) {
        val mediaPlayer = MediaPlayer()
        val audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
        try {
            mediaPlayer.setDataSource(audioUrl)
            mediaPlayer.playbackParams = mediaPlayer.playbackParams.apply {
                setSpeed(speed)
                pitch = 2.5f
            }
            mediaPlayer.playbackParams.pitch
            mediaPlayer.prepare()
            mediaPlayer.start()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun playAudio(speed: Float) {
        MediaPlayer().apply {
            Log.i("TEST","${viewModel.filePath}")
            setDataSource(viewModel.filePath)
            playbackParams = this.playbackParams.apply {
                setSpeed(speed)
                pitch = 2.5f
            }
            this.playbackParams.pitch
            prepare()
            start()
        }.run {
            this.audioSessionId
        }.run {
            if (this != -1) {
                initVisualizer(this)
                startVisualizer()
            }
        }
    }

    private fun initVisualizer(audioSessionId: Int) {
        Log.i("initVisualizer", "$audioSessionId")
        // need a param of audioSession, 0 is output mix, AudioRecord user please see 3.3.7

        visualizerManager?.release()
        visualizerManager = NierVisualizerManager().apply {
            init(audioSessionId)
        }
    }

    private fun startVisualizer(){
        visualizerManager?.start(viewDataBinding.wave, arrayOf(CircleRenderer(true), CircleBarRenderer(), ColumnarType4Renderer()))
    }
    private fun stopVisualizer() {
        visualizerManager?.stop()
    }

    private fun requestAudioPermission() {
        requestPermissions(requestPermissions, REQUEST_RECORD_AUDIO_PERMISSION)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        val audioRequestPermissionGranted  =
            requestCode == REQUEST_RECORD_AUDIO_PERMISSION &&
                grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED

        if (!audioRequestPermissionGranted) {
            Log.i("TAG","NOT GRANTED")
        }
    }

    private fun setupSoundPool() {
        context?.run {
            val attributes = AudioAttributes.Builder().apply {
                setUsage(AudioAttributes.USAGE_GAME)
                setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            }.build()

            soundpool = SoundPool.Builder()
                .setAudioAttributes(attributes)
                .build()
//            val sound = soundpool.load(this, R.raw.voice_test, 1)
            val sound = soundpool.load(viewModel.filePath, 1)
            viewDataBinding.button.setOnClickListener {
                playSound(sound, 2.0f)
            }
        }
    }

    fun playSound(sound: Int, fSpeed: Float) {
        activity?.run {
            val mgr = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val streamVolumeCurrent = mgr.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat()
            val streamVolumeMax = mgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC).toFloat()
            val volume = streamVolumeCurrent / streamVolumeMax
            soundpool.play(sound, volume, volume, 1, 0, fSpeed)
        }

    }

    companion object {

        private const val REQUEST_RECORD_AUDIO_PERMISSION = 100

        @JvmStatic
        fun newInstance() =
            MainFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}