package com.example.miniyam.Utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.io.IOException
import java.security.GeneralSecurityException

object SecurePreferencesHelper {
    
    private const val PREFS_NAME = "encrypted_prefs"
    private const val OLD_PREFS_NAME = "my_prefs"
    private const val MIGRATION_DONE_KEY = "migration_done"
    private const val MASTER_KEY_ALIAS = "_androidx_security_crypto_encrypted_prefs_key_"
    
    fun getEncryptedSharedPreferences(context: Context): SharedPreferences {
        return try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            
            val encryptedPrefs = EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            
            migrateFromOldPreferences(context, encryptedPrefs)
            
            encryptedPrefs
        } catch (e: GeneralSecurityException) {
            context.getSharedPreferences("fallback_prefs", Context.MODE_PRIVATE)
        } catch (e: IOException) {
            context.getSharedPreferences("fallback_prefs", Context.MODE_PRIVATE)
        }
    }
    
    private fun migrateFromOldPreferences(
        context: Context,
        encryptedPrefs: SharedPreferences
    ) {
        val migrationDone = encryptedPrefs.getBoolean(MIGRATION_DONE_KEY, false)
        if (migrationDone) {
            return
        }
        
        try {
            val oldPrefs = context.getSharedPreferences(OLD_PREFS_NAME, Context.MODE_PRIVATE)
            val oldToken = oldPrefs.getString("token", null)
            
            if (!oldToken.isNullOrBlank()) {
                encryptedPrefs.edit()
                    .putString("token", oldToken)
                    .putBoolean(MIGRATION_DONE_KEY, true)
                    .apply()
                
                oldPrefs.edit()
                    .remove("token")
                    .apply()
            } else {
                encryptedPrefs.edit()
                    .putBoolean(MIGRATION_DONE_KEY, true)
                    .apply()
            }
        } catch (e: Exception) {
            encryptedPrefs.edit()
                .putBoolean(MIGRATION_DONE_KEY, true)
                .apply()
        }
    }
    
    fun saveToken(context: Context, token: String) {
        val prefs = getEncryptedSharedPreferences(context)
        prefs.edit()
            .putString("token", token)
            .apply()
    }
    
    fun getToken(context: Context): String {
        val prefs = getEncryptedSharedPreferences(context)
        return prefs.getString("token", "") ?: ""
    }
    
    fun clearToken(context: Context) {
        val prefs = getEncryptedSharedPreferences(context)
        prefs.edit()
            .remove("token")
            .apply()
    }
}
