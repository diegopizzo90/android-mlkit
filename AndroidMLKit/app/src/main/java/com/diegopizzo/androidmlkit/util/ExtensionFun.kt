package com.diegopizzo.androidmlkit.util

/**
 * Validates nullable Boolean
 */
fun Boolean?.isTrue(): Boolean {
    return this == true
}