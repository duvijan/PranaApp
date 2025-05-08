package com.duvijan.pranaapp.ui.peace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duvijan.pranaapp.util.AnalyticsManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class PeaceMovementViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    
    private val _peacekeeperCount = MutableStateFlow(0)
    val peacekeeperCount: StateFlow<Int> = _peacekeeperCount
    
    private val _isSignedUp = MutableStateFlow(false)
    val isSignedUp: StateFlow<Boolean> = _isSignedUp
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    init {
        loadPeacekeeperCount()
        checkUserSignupStatus()
    }
    
    private fun loadPeacekeeperCount() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val snapshot = firestore.collection("peace_movement")
                    .document("stats")
                    .get()
                    .await()
                
                val count = snapshot.getLong("count")?.toInt() ?: 0
                _peacekeeperCount.value = count
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    private fun checkUserSignupStatus() {
        val userId = auth.currentUser?.uid ?: return
        
        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("peace_movement")
                    .document("users")
                    .collection("signups")
                    .document(userId)
                    .get()
                    .await()
                
                _isSignedUp.value = snapshot.exists()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    fun signUpAsPeacekeeper() {
        val userId = auth.currentUser?.uid ?: return
        
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                // Add user to signups
                firestore.collection("peace_movement")
                    .document("users")
                    .collection("signups")
                    .document(userId)
                    .set(mapOf(
                        "timestamp" to System.currentTimeMillis(),
                        "email" to (auth.currentUser?.email ?: "")
                    ))
                    .await()
                
                // Increment counter
                firestore.collection("peace_movement")
                    .document("stats")
                    .set(mapOf(
                        "count" to (_peacekeeperCount.value + 1)
                    ))
                    .await()
                
                // Update local state
                _isSignedUp.value = true
                _peacekeeperCount.value += 1
                
                // Log analytics event
                AnalyticsManager.logPeaceMovementSignup()
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun startPeaceMeditation() {
        AnalyticsManager.logPeaceMeditationStart()
    }
    
    fun completePeaceMeditation() {
        AnalyticsManager.logPeaceMeditationComplete()
    }
}
