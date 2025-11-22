package com.yenaing.apps.tasks.util.biometric

interface BiometricAuthListener {
    fun onBiometricAuthSuccess()
    fun onUserCancelled()
    fun onErrorOccurred()
}