package com.fireflyest.netcontrol.ui

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.fireflyest.netcontrol.R

class SettingFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }
}