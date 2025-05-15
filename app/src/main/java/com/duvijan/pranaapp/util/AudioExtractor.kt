package com.duvijan.pranaapp.util

import android.content.Context
import android.util.Log
import com.arthenica.mobileffmpeg.FFmpeg
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
                
                // Extract individual number recordings (1-10)
                for (i in 1..10) {
                    val outputFile = File(outputDir, "number_$i.mp3")
                    val startTime = (i - 1).toString()
                    val endTime = i.toString()
                    
                    val command = "-i ${sourceFile.absolutePath} -ss $startTime -to $endTime -c copy ${outputFile.absolutePath}"
                    val result = FFmpeg.execute(command)
                    
                    if (result != 0) {
                        Log.e(TAG, "Failed to extract number $i audio")
                    }
                }
                
                // Extract phase announcements
                val phases = mapOf(
                    "inhale" to Pair(10, 12),
                    "hold" to Pair(12, 14),
                    "exhale" to Pair(14, 16),
                    "silence" to Pair(16, 18)
                )
                
                for ((phase, times) in phases) {
                    val outputFile = File(outputDir, "${phase}_voice.mp3")
                    val startTime = times.first.toString()
                    val endTime = times.second.toString()
                    
                    val command = "-i ${sourceFile.absolutePath} -ss $startTime -to $endTime -c copy ${outputFile.absolutePath}"
                    val result = FFmpeg.execute(command)
                    
                    if (result != 0) {
                        Log.e(TAG, "Failed to extract $phase audio")
                    }
                }
                
                complete()
            } catch (e: Exception) {
                Log.e(TAG, "Error processing audio: ${e.message}")
            }
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
                // Get the cache directory for temporary files
                val cacheDir = context.cacheDir
                val outputDir = File(context.filesDir, "audio")
                
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
