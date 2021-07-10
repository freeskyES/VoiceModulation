package com.muse.audiotest.ui.main

import android.os.Environment
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.io.File

class MainViewModel: ViewModel() {

    private val _isRecording = MutableLiveData<Boolean>()
    val isRecording: LiveData<Boolean> = _isRecording

    val filePath: String = makeFileName()

    fun setIsRecording(isRecording: Boolean){
        _isRecording.value = isRecording
    }

    private fun makeFileName(): String{
        Environment.getExternalStorageDirectory().run {
            File(this, "recorded.3gp")
        }.run {
            val fileName = this.absolutePath
            Log.i("Main", "저장할 파일 명 : $fileName")
            return fileName
        }
    }
}