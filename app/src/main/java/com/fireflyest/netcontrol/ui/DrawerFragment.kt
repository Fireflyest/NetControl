package com.fireflyest.netcontrol.ui

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.fireflyest.netcontrol.R

class DrawerFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.left_preferences, rootKey)
    }
}