package com.muscleplay.app.core.network.auth

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.CoreFoundation.CFDictionaryRef
import platform.CoreFoundation.CFTypeRefVar
import platform.Foundation.NSData
import platform.Foundation.NSDictionary
import platform.Foundation.NSString
import platform.Foundation.NSUserDefaults
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.SecItemUpdate
import platform.Security.errSecItemNotFound
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
class IosSessionStorage : SessionStorage {
    private val serviceName = "com.muscleplay.app.session"
    private val defaults = NSUserDefaults.standardUserDefaults

    @Suppress("UNCHECKED_CAST", "CAST_NEVER_SUCCEEDS")
    override fun getString(key: String): String? {
        val query = mapOf<Any?, Any?>(
            kSecClass to kSecClassGenericPassword,
            kSecAttrService to serviceName,
            kSecAttrAccount to key,
            kSecReturnData to true,
            kSecMatchLimit to kSecMatchLimitOne
        )

        val valueFromKeychain = memScoped {
            val dataTypeRef = alloc<CFTypeRefVar>()
            val status = SecItemCopyMatching((query as NSDictionary) as CFDictionaryRef, dataTypeRef.ptr)
            if (status == errSecSuccess) {
                val data = dataTypeRef.value as? NSData
                data?.let {
                    NSString.create(data = it, encoding = NSUTF8StringEncoding)?.toString()
                }
            } else {
                null
            }
        }

        return valueFromKeychain ?: defaults.stringForKey(key)
    }

    @Suppress("UNCHECKED_CAST", "CAST_NEVER_SUCCEEDS")
    override fun putString(key: String, value: String?) {
        if (value == null) {
            deleteKeychainItem(key)
            defaults.removeObjectForKey(key)
            return
        }

        defaults.setObject(value, forKey = key)

        val data = (value as NSString).dataUsingEncoding(NSUTF8StringEncoding) ?: return

        val query = mapOf<Any?, Any?>(
            kSecClass to kSecClassGenericPassword,
            kSecAttrService to serviceName,
            kSecAttrAccount to key
        )

        val attributesToUpdate = mapOf<Any?, Any?>(
            kSecValueData to data
        )

        val status = SecItemUpdate((query as NSDictionary) as CFDictionaryRef, (attributesToUpdate as NSDictionary) as CFDictionaryRef)
        if (status == errSecItemNotFound) {
            val newItem = mapOf<Any?, Any?>(
                kSecClass to kSecClassGenericPassword,
                kSecAttrService to serviceName,
                kSecAttrAccount to key,
                kSecValueData to data
            )
            SecItemAdd((newItem as NSDictionary) as CFDictionaryRef, null)
        }
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        val str = getString(key) ?: return defaults.boolForKey(key)
        return str.toBooleanStrictOrNull() ?: defaultValue
    }

    override fun putBoolean(key: String, value: Boolean) {
        putString(key, value.toString())
        defaults.setBool(value, forKey = key)
    }

    override fun clear() {
        deleteKeychainItem("access_token")
        deleteKeychainItem("refresh_token")
        deleteKeychainItem("is_profile_complete")
        deleteKeychainItem("saved_email")
        deleteKeychainItem("saved_user_name")
        defaults.removeObjectForKey("access_token")
        defaults.removeObjectForKey("refresh_token")
        defaults.removeObjectForKey("is_profile_complete")
        defaults.removeObjectForKey("saved_email")
        defaults.removeObjectForKey("saved_user_name")
    }

    @Suppress("UNCHECKED_CAST", "CAST_NEVER_SUCCEEDS")
    private fun deleteKeychainItem(key: String) {
        val query = mapOf<Any?, Any?>(
            kSecClass to kSecClassGenericPassword,
            kSecAttrService to serviceName,
            kSecAttrAccount to key
        )
        SecItemDelete((query as NSDictionary) as CFDictionaryRef)
    }
}

actual fun createPlatformSessionStorage(): SessionStorage {
    return IosSessionStorage()
}
