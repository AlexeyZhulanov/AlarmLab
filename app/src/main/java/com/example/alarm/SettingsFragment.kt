package com.example.alarm

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.PopupWindow
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.alarm.databinding.FragmentSettingsBinding
import com.example.alarm.model.AlarmService
import com.example.alarm.model.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {

    private val job = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + job)
    private lateinit var binding: FragmentSettingsBinding
    private val alarmsService: AlarmService
        get() = Repositories.alarmRepository as AlarmService
    private var globalId: Long = 0
    private lateinit var preferences: SharedPreferences

    @SuppressLint("DiscouragedApi")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        preferences = requireActivity().getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE)
        preferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
        val wallpaper = preferences.getString(PREF_WALLPAPER, "")
        if(wallpaper != "") {
            binding.wallpaperName.text = wallpaper
            val resId = resources.getIdentifier(wallpaper, "drawable", requireContext().packageName)
            if(resId != 0) binding.settingsLayout.background = ContextCompat.getDrawable(requireContext(), resId)
        }
        else {
            binding.wallpaperName.text = "Classic"
        }
        val themeNumber = preferences.getInt(PREF_THEME, 0)
        if(themeNumber != 0) binding.colorThemeName.text = "Theme ${themeNumber+1}" else binding.colorThemeName.text = "Classic"
        uiScope.launch {
            val settings = alarmsService.getSettings()
            binding.melodyName.text = settings.melody
            binding.repeatRadioGroup.isEnabled = settings.repetitions == 1
            binding.switchVibration.isChecked = settings.vibration == 1
            when(settings.repetitions) {
                3 -> binding.repeats3.isChecked = true
                5 -> binding.repeats5.isChecked = true
                else -> binding.repeatsInfinite.isChecked = true
            }
            when(settings.interval) {
                3 -> binding.interval3.isChecked = true
                5 -> binding.interval5.isChecked = true
                else -> binding.interval10.isChecked = true
            }
            globalId = settings.id
        }
        binding.changeMelody.setOnClickListener {
            showSignalsPopupMenu(it)
        }
        binding.playMelody.setOnClickListener {
            uiScope.launch {
                val settings = async(Dispatchers.IO) { alarmsService.getSettings() }
                when (settings.await().melody) {
                    getString(R.string.melody1) -> {
                        //todo
                    }
                    getString(R.string.melody2) -> {
                        //todo
                    }
                    getString(R.string.melody3) -> {
                        //todo
                    }
                    getString(R.string.melody4) -> {
                        //todo
                    }
                    getString(R.string.melody5) -> {
                        //todo
                    }
                    getString(R.string.melody6) -> {
                        //todo
                    }
                    getString(R.string.melody7) -> {
                        //todo
                    }
                    getString(R.string.melody8) -> {
                        //todo
                    }
                    else -> {
                        Toast.makeText(requireContext(), "Melody not found", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        binding.switchVibration.setOnClickListener {
            val s = readSettings(globalId)
            uiScope.launch { alarmsService.updateSettings(s) }
        }
        binding.repeats3.setOnClickListener {
            val s = readSettings(globalId)
            uiScope.launch { alarmsService.updateSettings(s) }
        }
        binding.repeats5.setOnClickListener {
            val s = readSettings(globalId)
            uiScope.launch { alarmsService.updateSettings(s) }
        }
        binding.repeatsInfinite.setOnClickListener {
            val s = readSettings(globalId)
            uiScope.launch { alarmsService.updateSettings(s) }
        }
        binding.interval3.setOnClickListener {
            val s = readSettings(globalId)
            uiScope.launch { alarmsService.updateSettings(s) }
            updatePrefs(3)
        }
        binding.interval5.setOnClickListener {
            val s = readSettings(globalId)
            uiScope.launch { alarmsService.updateSettings(s) }
            updatePrefs(5)
        }
        binding.interval10.setOnClickListener {
            val s = readSettings(globalId)
            uiScope.launch { alarmsService.updateSettings(s) }
            updatePrefs(10)
        }
        binding.changeColorTheme.setOnClickListener {
            showColorThemePopupMenu(it, container as ViewGroup)
        }
        binding.changeWallpaper.setOnClickListener {
            showWallpapersPopupMenu(it, container as ViewGroup)
        }
        return binding.root
    }

    private fun readSettings(id: Long): Settings {
        val settings = Settings(
            id = id,
            melody = binding.melodyName.text.toString(),
            vibration = if(binding.switchVibration.isChecked) 1 else 0,
            interval = if(binding.interval3.isChecked) 3
            else if(binding.interval5.isChecked) 5
            else 10,
            repetitions = if(binding.repeats3.isChecked) 3
            else if(binding.repeats5.isChecked) 5
            else 100,
            disableType = 0 //todo
        )
        return settings
    }

    private fun updatePrefs(interval: Int) {
        uiScope.launch {
            preferences = requireActivity().getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE)
            preferences.edit()
                .putInt(PREF_INTERVAL, interval)
                .apply()
        }
    }
    private fun showSignalsPopupMenu(view: View) {
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.menuInflater.inflate(R.menu.melody_menu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { menuItem: MenuItem ->
            val newMelodyName = when (menuItem.itemId) {
                R.id.melody1 -> getString(R.string.melody1)
                R.id.melody2 -> getString(R.string.melody2)
                R.id.melody3 -> getString(R.string.melody3)
                R.id.melody4 -> getString(R.string.melody4)
                R.id.melody5 -> getString(R.string.melody5)
                R.id.melody6 -> getString(R.string.melody6)
                R.id.melody7 -> getString(R.string.melody7)
                R.id.melody8 -> getString(R.string.melody8)
                else -> null
            }

            newMelodyName?.let { name ->
                binding.melodyName.text = name
                val s = readSettings(globalId)
                uiScope.launch { alarmsService.updateSettings(s) }
                true
            } ?: false
        }
        popupMenu.show()
    }
    private fun showWallpapersPopupMenu(view: View, container: ViewGroup) {
        val inflater = layoutInflater
        val popupView = inflater.inflate(R.layout.popup_menu_wallpaper_layout, container, false)

        val popupWindow = PopupWindow(
            popupView,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        )

        popupWindow.showAtLocation(requireView(), Gravity.CENTER, 0, 0)
        val recyclerView = popupView.findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val menuItems = listOf(
            MenuItemData("Classic", R.drawable.whitequad),
            MenuItemData("1.", R.drawable.wallpaper1),
            MenuItemData("2.", R.drawable.wallpaper2),
            MenuItemData("3.", R.drawable.wallpaper3),
            MenuItemData("4.", R.drawable.wallpaper4),
            MenuItemData("5.", R.drawable.wallpaper5),
            MenuItemData("6.", R.drawable.wallpaper6),
            MenuItemData("7.", R.drawable.wallpaper7),
            MenuItemData("8.", R.drawable.wallpaper8),
            MenuItemData("9.", R.drawable.wallpaper9),
            MenuItemData("10.", R.drawable.wallpaper10)
        )
        var temp = ""
        val adapter = PopupMenuWallpaperAdapter(menuItems) { menuItem ->
            temp = when(menuItem.title) {
                "Classic" -> ""
                "1." -> "wallpaper1"
                "2." -> "wallpaper2"
                "3." -> "wallpaper3"
                "4." -> "wallpaper4"
                "5." -> "wallpaper5"
                "6." -> "wallpaper6"
                "7." -> "wallpaper7"
                "8." -> "wallpaper8"
                "9." -> "wallpaper9"
                "10." -> "wallpaper10"
                else -> ""
            }
            preferences = requireActivity().getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE)
            preferences.edit()
                .putString(PREF_WALLPAPER, temp)
                .apply()
            popupWindow.dismiss()
        }

        recyclerView.adapter = adapter

        popupWindow.showAsDropDown(view)
    }
    private fun showColorThemePopupMenu(view: View, container: ViewGroup) {
        val inflater = layoutInflater
        val popupView = inflater.inflate(R.layout.popup_menu_wallpaper_layout, container, false)

        val popupWindow = PopupWindow(
            popupView,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        )

        popupWindow.showAtLocation(requireView(), Gravity.CENTER, 0, 0)
        val recyclerView = popupView.findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val menuItems = listOf(
            ColorThemeMenuItem(R.color.colorPrimary, R.color.colorAccent, 0),
            ColorThemeMenuItem(R.color.color1_main, R.color.color1_secondary, 1),
            ColorThemeMenuItem(R.color.color2_main, R.color.color2_secondary, 2),
            ColorThemeMenuItem(R.color.color3_main, R.color.color3_secondary, 3),
            ColorThemeMenuItem(R.color.color4_main, R.color.color4_secondary, 4),
            ColorThemeMenuItem(R.color.color5_main, R.color.color5_secondary, 5),
            ColorThemeMenuItem(R.color.color6_main, R.color.color6_secondary, 6),
            ColorThemeMenuItem(R.color.color7_main, R.color.color7_secondary, 7),
            ColorThemeMenuItem(R.color.color8_main, R.color.color8_secondary, 8)
        )
        val adapter = ColorThemeMenuAdapter(menuItems) { menuItem ->
            preferences = requireActivity().getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE)
            preferences.edit()
                .putInt(PREF_THEME, menuItem.themeNumber)
                .apply()
            requireActivity().recreate()
            popupWindow.dismiss()
        }

        recyclerView.adapter = adapter

        popupWindow.showAsDropDown(view)
    }
    @SuppressLint("DiscouragedApi")
    private val preferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
        if (key == PREF_WALLPAPER) {
            val tmp = sharedPreferences.getString(PREF_WALLPAPER, "")
            binding.wallpaperName.text = tmp
            if(tmp != "") {
                val resId = resources.getIdentifier(tmp, "drawable", requireContext().packageName)
                if(resId != 0)
                    binding.settingsLayout.background = ContextCompat.getDrawable(requireContext(), resId)
            }
            else {
                binding.settingsLayout.background = null
                binding.wallpaperName.text = "Classic"
            }
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        val sharedPreferences = requireActivity().getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE)
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
    }
}
const val PREF_WALLPAPER = "PREF_WALLPAPER"
const val PREF_THEME = "PREF_THEME"