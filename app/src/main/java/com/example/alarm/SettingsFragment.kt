package com.example.alarm

import android.content.Context
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.PopupWindow
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
    private val alarmsService: AlarmService
        get() = Repositories.alarmRepository as AlarmService
    private var globalId: Long = 0
    private lateinit var preferences: SharedPreferences

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = FragmentSettingsBinding.inflate(inflater, container, false)
        //binding.settingsLayout.background = ContextCompat.getDrawable(requireContext(), R.drawable.wallpaper1)
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
        fun readSettings(id: Long): Settings {
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
        binding.changeMelody.setOnClickListener {
            showSignalsPopupMenu(it)
        }
        binding.playMelody.setOnClickListener {
            uiScope.launch {
                val settings = async(Dispatchers.IO) { alarmsService.getSettings() }
                when (settings.await().melody) {
                    "melody1" -> {
                        //todo
                    }
                    "melody2" -> {
                        //todo
                    }
                    "melody3" -> {
                        //todo
                    }
                    "melody4" -> {
                        //todo
                    }
                    "melody5" -> {
                        //todo
                    }
                    else -> {
                        //todo
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
            //todo
        }
        binding.changeWallpaper.setOnClickListener {
            showWallpapersPopupMenu(it, container as ViewGroup)
        }
        return binding.root
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
            when (menuItem.itemId) {
                R.id.melody1 -> {
                    // Handle item1 click
                    true
                }
                R.id.melody2 -> {
                    // Handle item2 click
                    true
                }
                R.id.melody3 -> {
                    // Handle item3 click
                    true
                }
                R.id.melody4 -> {
                    // Handle item4 click
                    true
                }
                R.id.melody5 -> {
                    // Handle item5 click
                    true
                }
                else -> false
            }
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

        val adapter = PopupMenuWallpaperAdapter(menuItems) { menuItem ->
            // Обработка клика по элементу меню
            popupWindow.dismiss()
        }

        recyclerView.adapter = adapter

        popupWindow.showAsDropDown(view)
    }
}