package com.duvijan.pranaapp.util

import android.content.Context
import android.util.Log
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFmpegKitConfig
import com.arthenica.ffmpegkit.ReturnCode
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
                
                // Process audio files sequentially
                processNumberRecordings(sourceFile, outputDir) {
                    processPhaseAnnouncements(sourceFile, outputDir) {
                        complete()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing audio: ${e.message}")
                complete()
            }
        }
        
        private fun processNumberRecordings(sourceFile: File, outputDir: File, onComplete: () -> Unit) {
            processNextNumber(sourceFile, outputDir, 1, onComplete)
        }
        
        private fun processNextNumber(sourceFile: File, outputDir: File, currentNumber: Int, onComplete: () -> Unit) {
            if (currentNumber > 10) {
                onComplete()
                return
            }
            
            val outputFile = File(outputDir, "number_$currentNumber.mp3")
            val startTime = (currentNumber - 1).toString()
            val endTime = currentNumber.toString()
            
            val command = "-i ${sourceFile.absolutePath} -ss $startTime -to $endTime -c copy ${outputFile.absolutePath}"
            
            FFmpegKit.executeAsync(command, { session ->
                if (ReturnCode.isSuccess(session.returnCode)) {
                    Log.d(TAG, "Successfully extracted number $currentNumber audio")
                    processNextNumber(sourceFile, outputDir, currentNumber + 1, onComplete)
                } else {
                    Log.e(TAG, "Failed to extract number $currentNumber audio: ${session.allLogsAsString}")
                    processNextNumber(sourceFile, outputDir, currentNumber + 1, onComplete)
                }
            }, null, null)
        }
        
        private fun processPhaseAnnouncements(sourceFile: File, outputDir: File, onComplete: () -> Unit) {
            val phases = listOf(
                Triple("inhale", 10, 12),
                Triple("hold", 12, 14),
                Triple("exhale", 14, 16),
                Triple("silence", 16, 18)
            )
            
            processNextPhase(sourceFile, outputDir, phases, 0, onComplete)
        }
        
        private fun processNextPhase(sourceFile: File, outputDir: File, phases: List<Triple<String, Int, Int>>, index: Int, onComplete: () -> Unit) {
            if (index >= phases.size) {
                onComplete()
                return
            }
            
            val (phase, startTime, endTime) = phases[index]
            val outputFile = File(outputDir, "${phase}_voice.mp3")
            
            val command = "-i ${sourceFile.absolutePath} -ss $startTime -to $endTime -c copy ${outputFile.absolutePath}"
            
            FFmpegKit.executeAsync(command, { session ->
                if (ReturnCode.isSuccess(session.returnCode)) {
                    Log.d(TAG, "Successfully extracted $phase audio")
                    processNextPhase(sourceFile, outputDir, phases, index + 1, onComplete)
                } else {
                    Log.e(TAG, "Failed to extract $phase audio: ${session.allLogsAsString}")
                    processNextPhase(sourceFile, outputDir, phases, index + 1, onComplete)
                }
            }, null, null)
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
