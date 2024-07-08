package com.asad.mediapicker

import android.util.Log

/*
* Property : GSoft
* Created by : GSoft
* Updated by : GSoft
*/

/**
 * The Class DQDebugHelper.
 */
object DebugHelper {
    /** The mode debug.  */
    private const val MODE_DEBUG = true

    /** The mode debug.  */
    private const val MODE_TRACK = true

    /** The Constant TAG.  */
    private const val TAG = "DebugHelper"


    /**
     * Prints the and track error.
     *
     * @param error
     * the exception
     */
    fun trackError(error: Error) {
        printError(TAG, error)
    }


    /**
     * Prints the and track exception.
     *
     * @param TAG
     * the tag
     * @param error
     * the exception
     */
    fun trackError(TAG: String, error: Error) {
        printError(TAG, error)
    }


    /**
     * Prints the and track exception.
     *
     * @param TAG
     * the tag
     * @param error
     * the exception
     */
    fun printError(TAG: String, error: Error) {
        if (MODE_DEBUG) {
            printData(TAG, "Error = $error")
            error.printStackTrace()
            if (MODE_TRACK) {
                //BugSenseHandler.sendEvent("Error = " + error.toString());
            }
        }
    }

    fun printError(error: Error) {
        if (MODE_DEBUG) {
            printData(TAG, "Error = $error")
            error.printStackTrace()
            if (MODE_TRACK) {
                //BugSenseHandler.sendEvent("Error = " + error.toString());
            }
        }
    }


    /**
     * Prints the and track exception.
     *
     * @param exception
     * the exception
     */
    fun trackException(exception: Exception) {
        print(TAG, exception, true)
    }


    /**
     * Prints the and track exception.
     *
     * @param TAG
     * the tag
     * @param exception
     * the exception
     */
    fun trackException(TAG: String, exception: Exception) {
        print(TAG, exception, true)
    }


    /**
     * Prints the and track exception.
     *
     * @param TAG
     * the tag
     * @param exception
     * the exception
     */
    fun print(TAG: String, exception: Exception, track: Boolean) {
        if (MODE_DEBUG) {
            printData(TAG, "Exception = $exception")
            exception.printStackTrace()
            //			if (track) {
            //BugSenseHandler.sendException(exception);
//			}
        }
    }


    /**
     * Prints the exception.
     *
     * @param exception
     * the exception
     */
    fun printException(exception: Exception) {
        print(TAG, exception, false)
    }


    /**
     * Prints the exception.
     *
     * @param TAG
     * the tag
     * @param exception
     * the exception
     */
    fun printException(TAG: String, exception: Exception) {
        print(TAG, exception, true)
    }


    /**
     * Prints the data.
     *
     * @param data
     * the data
     */
    fun printData(data: String) {
        printData(TAG, data)
    }


    /**
     * Prints the data.
     *
     * @param TAG
     * the tag
     * @param data
     * the data
     */
    fun printData(TAG: String, data: String) {
        if (MODE_DEBUG) {
            Log.e("" + TAG, "Data = $data")
        }
    }
}