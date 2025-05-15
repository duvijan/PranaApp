package com.duvijan.pranaapp.util

import android.content.Context
import android.util.Log
import nl.bravobit.ffmpeg.ExecuteBinaryResponseHandler
import nl.bravobit.ffmpeg.FFmpeg
import nl.bravobit.ffmpeg.FFtask
import java.io.File
import java.io.FileOutputStream

class AudioExtractor {
    companion object {
        private const val TAG = "AudioExtractor"
        
        fun extractAudioFromRawResource(context: Context, sourceFile: File, outputDir: File, complete: () -> Unit) {
            try {
                // Create output directory if it doesn't exist
                if (!outputDir.exists()) {
                    outputDir.mkdirs()
                }
                
                // Initialize FFmpeg
                val ffmpeg = FFmpeg.getInstance(context)
                
                // Check if FFmpeg is supported
                if (!ffmpeg.isSupported) {
                    Log.e(TAG, "FFmpeg is not supported on this device")
                    complete()
                    return
                }
                
                // Process audio files sequentially
                processNumberRecordings(context, sourceFile, outputDir) {
                    processPhaseAnnouncements(context, sourceFile, outputDir) {
                        complete()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing audio: ${e.message}")
                complete()
            }
        }
        
        private fun processNumberRecordings(context: Context, sourceFile: File, outputDir: File, onComplete: () -> Unit) {
            processNextNumber(context, sourceFile, outputDir, 1, onComplete)
        }
        
        private fun processNextNumber(context: Context, sourceFile: File, outputDir: File, currentNumber: Int, onComplete: () -> Unit) {
            if (currentNumber > 10) {
                onComplete()
                return
            }
            
            val outputFile = File(outputDir, "number_$currentNumber.mp3")
            val startTime = (currentNumber - 1).toString()
            val endTime = currentNumber.toString()
            
            val cmd = arrayOf("-i", sourceFile.absolutePath, "-ss", startTime, "-to", endTime, "-c", "copy", outputFile.absolutePath)
            
            val ffmpeg = FFmpeg.getInstance(context)
            ffmpeg.execute(cmd, object : ExecuteBinaryResponseHandler() {
                override fun onSuccess(message: String?) {
                    Log.d(TAG, "Successfully extracted number $currentNumber audio")
                    processNextNumber(context, sourceFile, outputDir, currentNumber + 1, onComplete)
                }
                
                override fun onFailure(message: String?) {
                    Log.e(TAG, "Failed to extract number $currentNumber audio: $message")
                    processNextNumber(context, sourceFile, outputDir, currentNumber + 1, onComplete)
                }
                
                override fun onFinish() {
                    Log.d(TAG, "Finished processing number $currentNumber")
                }
            })
        }
        
        private fun processPhaseAnnouncements(context: Context, sourceFile: File, outputDir: File, onComplete: () -> Unit) {
            val phases = listOf(
                Triple("inhale", 10, 12),
                Triple("hold", 12, 14),
                Triple("exhale", 14, 16),
                Triple("silence", 16, 18)
            )
            
            processNextPhase(context, sourceFile, outputDir, phases, 0, onComplete)
        }
        
        private fun processNextPhase(context: Context, sourceFile: File, outputDir: File, phases: List<Triple<String, Int, Int>>, index: Int, onComplete: () -> Unit) {
            if (index >= phases.size) {
                onComplete()
                return
            }
            
            val (phase, startTime, endTime) = phases[index]
            val outputFile = File(outputDir, "${phase}_voice.mp3")
            
            val cmd = arrayOf("-i", sourceFile.absolutePath, "-ss", startTime.toString(), "-to", endTime.toString(), "-c", "copy", outputFile.absolutePath)
            
            val ffmpeg = FFmpeg.getInstance(context)
            ffmpeg.execute(cmd, object : ExecuteBinaryResponseHandler() {
                override fun onSuccess(message: String?) {
                    Log.d(TAG, "Successfully extracted $phase audio")
                    processNextPhase(context, sourceFile, outputDir, phases, index + 1, onComplete)
                }
                
                override fun onFailure(message: String?) {
                    Log.e(TAG, "Failed to extract $phase audio: $message")
                    processNextPhase(context, sourceFile, outputDir, phases, index + 1, onComplete)
                }
                
                override fun onFinish() {
                    Log.d(TAG, "Finished processing $phase")
                }
            })
        }
        
        fun copyRawResourceToFile(context: Context, resourceId: Int, outputFile: File): Boolean {
            try {
                val inputStream = context.resources.openRawResource(resourceId)
                val outputStream = FileOutputStream(outputFile)
                
                val buffer = ByteArray(4096)
                var bytesRead: Int
                
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                }
                
                outputStream.close()
                inputStream.close()
                
                return true
            } catch (e: Exception) {
                Log.e(TAG, "Error copying raw resource: ${e.message}")
                return false
            }
        }
        
        fun processAudioInBackground(context: Context, complete: () -> Unit) {
            try {
                // Check if FFmpeg is supported
                val ffmpeg = FFmpeg.getInstance(context)
                if (!ffmpeg.isSupported) {
                    Log.e(TAG, "FFmpeg is not supported on this device")
                    complete()
                    return
                }
                
                // Get the cache directory for temporary files
                val cacheDir = context.cacheDir
                val outputDir = File(context.filesDir, "audio")
                
                // Create output directory if it doesn't exist
                if (!outputDir.exists()) {
                    outputDir.mkdirs()
                }
                
                // Create a temporary file from the raw resource
                val tempFile = File(cacheDir, "temp_mahan_voice.m4a")
                val resourceId = context.resources.getIdentifier("temp_mahan_voice", "raw", context.packageName)
                
                if (resourceId != 0) {
                    if (copyRawResourceToFile(context, resourceId, tempFile)) {
                        // Extract audio segments
                        extractAudioFromRawResource(context, tempFile, outputDir) {
                            // Delete temporary file after processing
                            tempFile.delete()
                            complete()
                        }
                    } else {
                        Log.e(TAG, "Failed to copy raw resource to temporary file")
                        complete()
                    }
                } else {
                    Log.e(TAG, "Raw resource not found")
                    complete()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in processAudioInBackground: ${e.message}")
                complete()
            }
        }
    }
}
