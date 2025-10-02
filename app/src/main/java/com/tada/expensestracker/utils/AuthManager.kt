package com.tada.expensestracker.utils

import android.content.Context
import android.provider.Settings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthManager {
    
    private val auth = FirebaseAuth.getInstance()
    
    suspend fun signInAnonymously(): Result<FirebaseUser?> {
        return try {
            val result = auth.signInAnonymously().await()
            Result.success(result.user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }
    
    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }
    
    fun isUserSignedIn(): Boolean {
        return auth.currentUser != null
    }
    
    fun signOut() {
        auth.signOut()
    }
    
    // Generate unique user identifier for development
    fun getDeviceBasedUserId(context: Context): String {
        val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        return "dev_user_$androidId"
    }
    
    // Create a unique user for each device/developer
    suspend fun signInAsDeviceUser(context: Context): Result<FirebaseUser?> {
        return try {
            // Use device ID to create consistent but unique user per device
            val deviceId = getDeviceBasedUserId(context)
            
            // Sign in anonymously first
            val result = signInAnonymously()
            if (result.isSuccess) {
                // You can store deviceId as custom claim or in user profile
                Result.success(result.getOrNull())
            } else {
                result
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Method to ensure user is authenticated with callback
    fun ensureUserAuthenticated(context: Context, callback: (String?) -> Unit) {
        if (isUserSignedIn()) {
            callback(getCurrentUserId())
        } else {
            // Use coroutine to handle async authentication
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                val result = signInAsDeviceUser(context)
                if (result.isSuccess) {
                    callback(getCurrentUserId())
                } else {
                    android.util.Log.e("AuthManager", "Authentication failed: ${result.exceptionOrNull()?.message}")
                    callback(null)
                }
            }
        }
    }
}