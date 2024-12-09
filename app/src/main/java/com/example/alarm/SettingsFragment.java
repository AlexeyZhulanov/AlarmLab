package com.example.alarm;

import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupWindow;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.alarm.databinding.FragmentSettingsBinding;
import com.example.alarm.model.Settings;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dagger.hilt.android.AndroidEntryPoint;


@AndroidEntryPoint
public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private long globalId;
    private MediaPlayer mediaPlayer;
    private AlarmViewModel alarmViewModel;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @SuppressLint("DiscouragedApi")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        alarmViewModel = new ViewModelProvider(this).get(AlarmViewModel.class);
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        alarmViewModel.registerPreferences(requireContext());

        // Update wallpapers and preferences
        executorService.submit(() -> {
            Settings settings = alarmViewModel.getSettings();
            requireActivity().runOnUiThread(() -> {
                binding.melodyName.setText(settings.getMelody());
                binding.repeatRadioGroup.setEnabled(settings.getRepetitions() == 1);
                binding.switchVibration.setChecked(settings.getVibration());
                switch (settings.getRepetitions()) {
                    case 3:
                        binding.repeats3.setChecked(true);
                        break;
                    case 5:
                        binding.repeats5.setChecked(true);
                        break;
                    default:
                        binding.repeatsInfinite.setChecked(true);
                        break;
                }
                switch (settings.getInterval()) {
                    case 3:
                        binding.interval3.setChecked(true);
                        break;
                    case 5:
                        binding.interval5.setChecked(true);
                        break;
                    default:
                        binding.interval10.setChecked(true);
                        break;
                }
                globalId = settings.getId();
            });
        });

        binding.changeMelody.setOnClickListener(this::showSignalsPopupMenu);
        binding.playMelody.setOnClickListener(v -> executorService.submit(() -> {
            String melody = alarmViewModel.getSettings().getMelody();
            playMelody(getMelodyResource(melody));
        }));

        binding.switchVibration.setOnClickListener(v -> {
            Settings s = readSettings(globalId);
            executorService.submit(() -> alarmViewModel.updateSettings(s));
        });

        setUpRepeatSettings();
        setUpIntervalSettings();

        binding.changeColorTheme.setOnClickListener(v -> showColorThemePopupMenu(v, container));
        binding.changeWallpaper.setOnClickListener(v -> showWallpapersPopupMenu(v, container));

        return binding.getRoot();
    }

    private void setUpRepeatSettings() {
        binding.repeats3.setOnClickListener(v -> updateSettings());
        binding.repeats5.setOnClickListener(v -> updateSettings());
        binding.repeatsInfinite.setOnClickListener(v -> updateSettings());
    }

    private void setUpIntervalSettings() {
        binding.interval3.setOnClickListener(v -> {
            updateSettings();
            updatePrefs(3);
        });
        binding.interval5.setOnClickListener(v -> {
            updateSettings();
            updatePrefs(5);
        });
        binding.interval10.setOnClickListener(v -> {
            updateSettings();
            updatePrefs(10);
        });
    }

    private void updateSettings() {
        Settings s = readSettings(globalId);
        executorService.submit(() -> alarmViewModel.updateSettings(s));
    }

    private Settings readSettings(long id) {
        Settings settings = new Settings(id);
        settings.setMelody(binding.melodyName.getText().toString());
        settings.setVibration(binding.switchVibration.isChecked());
        settings.setInterval(binding.interval3.isChecked() ? 3 :
                binding.interval5.isChecked() ? 5 : 10);
        settings.setRepetitions(binding.repeats3.isChecked() ? 3 :
                binding.repeats5.isChecked() ? 5 : 100);
        return settings;
    }

    private void updatePrefs(int interval) {
        alarmViewModel.editPreferencesInterval(requireContext(), interval);
    }

    private String getMelodyName(int itemId) {
        if (itemId == R.id.melody1) return getString(R.string.melody1);
        else if (itemId == R.id.melody2) return getString(R.string.melody2);
        else if (itemId == R.id.melody3) return getString(R.string.melody3);
        else if (itemId == R.id.melody4) return getString(R.string.melody4);
        else if (itemId == R.id.melody5) return getString(R.string.melody5);
        else if (itemId == R.id.melody6) return getString(R.string.melody6);
        else if (itemId == R.id.melody7) return getString(R.string.melody7);
        else if (itemId == R.id.melody8) return getString(R.string.melody8);
        else if (itemId == R.id.melody9) return getString(R.string.melody9);
        else if (itemId == R.id.melody10) return getString(R.string.melody10);
        else if (itemId == R.id.melody11) return getString(R.string.melody11);
        else if (itemId == R.id.melody12) return getString(R.string.melody12);
        else return null;
    }

    private int getMelodyResource(String melody) {
        switch (melody) {
            case "melody1": return R.raw.default_signal1;
            case "melody2": return R.raw.default_signal2;
            case "melody3": return R.raw.default_signal3;
            case "melody4": return R.raw.default_signal4;
            case "melody5": return R.raw.default_signal5;
            case "melody6": return R.raw.signal;
            case "melody7": return R.raw.banjo_signal;
            case "melody8": return R.raw.morning_signal;
            case "melody9": return R.raw.simple_signal;
            case "melody10": return R.raw.fitness_signal;
            case "melody11": return R.raw.medieval_signal;
            case "melody12": return R.raw.introduction_signal;
            default: return -1; // Melody not selected
        }
    }

    private void showWallpapersPopupMenu(View view, ViewGroup container) {
        // Inflate the custom layout for the popup
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View popupView = inflater.inflate(R.layout.popup_menu_wallpaper_layout, container, false);

        // Create the PopupWindow
        PopupWindow popupWindow = new PopupWindow(
                popupView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                true // Focusable
        );

        // Show the popup window at a specific location
        popupWindow.showAtLocation(requireView(), Gravity.CENTER, 0, 0);

        // Set up the RecyclerView
        RecyclerView recyclerView = popupView.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Define the menu items
        List<MenuItemData> menuItems = Arrays.asList(
                new MenuItemData(getString(R.string.classic), R.drawable.whitequad),
                new MenuItemData("1.", R.drawable.wallpaper1),
                new MenuItemData("2.", R.drawable.wallpaper2),
                new MenuItemData("3.", R.drawable.wallpaper3),
                new MenuItemData("4.", R.drawable.wallpaper4),
                new MenuItemData("5.", R.drawable.wallpaper5),
                new MenuItemData("6.", R.drawable.wallpaper6),
                new MenuItemData("7.", R.drawable.wallpaper7),
                new MenuItemData("8.", R.drawable.wallpaper8),
                new MenuItemData("9.", R.drawable.wallpaper9),
                new MenuItemData("10.", R.drawable.wallpaper10)
        );

        // Create and set the adapter
        PopupMenuWallpaperAdapter adapter = new PopupMenuWallpaperAdapter(menuItems, menuItem -> {
            String selectedWallpaper = "";
            switch (menuItem.title) {
                case "1.":
                    selectedWallpaper = getString(R.string.wallpaper1);
                    break;
                case "2.":
                    selectedWallpaper = getString(R.string.wallpaper2);
                    break;
                case "3.":
                    selectedWallpaper = getString(R.string.wallpaper3);
                    break;
                case "4.":
                    selectedWallpaper = getString(R.string.wallpaper4);
                    break;
                case "5.":
                    selectedWallpaper = getString(R.string.wallpaper5);
                    break;
                case "6.":
                    selectedWallpaper = getString(R.string.wallpaper6);
                    break;
                case "7.":
                    selectedWallpaper = getString(R.string.wallpaper7);
                    break;
                case "8.":
                    selectedWallpaper = getString(R.string.wallpaper8);
                    break;
                case "9.":
                    selectedWallpaper = getString(R.string.wallpaper9);
                    break;
                case "10.":
                    selectedWallpaper = getString(R.string.wallpaper10);
                    break;
                case "Classic":
                    selectedWallpaper = ""; // Default case
                    break;
            }
            // Save the selected wallpaper
            alarmViewModel.editPreferencesWallpaper(requireContext(), selectedWallpaper);
            // Dismiss the popup
            popupWindow.dismiss();
        });

        recyclerView.setAdapter(adapter);

        // Show the popup below the clicked view
        popupWindow.showAsDropDown(view);
    }


    private void showColorThemePopupMenu(View view, ViewGroup container) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View popupView = inflater.inflate(R.layout.popup_menu_wallpaper_layout, container, false);

        PopupWindow popupWindow = new PopupWindow(
                popupView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                true
        );

        popupWindow.showAtLocation(requireView(), Gravity.CENTER, 0, 0);

        RecyclerView recyclerView = popupView.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        List<ColorThemeMenuItem> menuItems = Arrays.asList(
                new ColorThemeMenuItem(R.color.colorPrimary, R.color.colorAccent, 0),
                new ColorThemeMenuItem(R.color.color1_main, R.color.color1_secondary, 1),
                new ColorThemeMenuItem(R.color.color2_main, R.color.color2_secondary, 2),
                new ColorThemeMenuItem(R.color.color3_main, R.color.color3_secondary, 3),
                new ColorThemeMenuItem(R.color.color4_main, R.color.color4_secondary, 4),
                new ColorThemeMenuItem(R.color.color5_main, R.color.color5_secondary, 5),
                new ColorThemeMenuItem(R.color.color6_main, R.color.color6_secondary, 6),
                new ColorThemeMenuItem(R.color.color7_main, R.color.color7_secondary, 7),
                new ColorThemeMenuItem(R.color.color8_main, R.color.color8_secondary, 8)
        );

        ColorThemeMenuAdapter adapter = new ColorThemeMenuAdapter(menuItems, menuItem -> {
            alarmViewModel.editPreferencesTheme(requireContext(), menuItem.getThemeNumber());
            requireActivity().recreate();
            popupWindow.dismiss();
        });

        recyclerView.setAdapter(adapter);

        popupWindow.showAsDropDown(view);
    }


    private void onWallpaperItemSelected(MenuItemData menuItem) {
        String temp = menuItem.title.equals("Classic") ? "" : "wallpaper" + menuItem.title.charAt(0);
        alarmViewModel.editPreferencesWallpaper(requireContext(), temp);
    }

    @SuppressLint("DiscouragedApi")
    private void updateWallpapers(String wallpaper) {
        binding.wallpaperName.setText(wallpaper);
        if (!wallpaper.isEmpty()) {
            int resId = requireContext().getResources().getIdentifier(wallpaper, "drawable", requireContext().getPackageName());
            if (resId != 0) {
                binding.settingsLayout.setBackground(ContextCompat.getDrawable(requireContext(), resId));
            }
        } else {
            binding.settingsLayout.setBackground(null);
            binding.wallpaperName.setText("Classic");
        }
    }

    private void playMelody(int signalId) {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            stopAndReleaseMediaPlayer();
        }

        mediaPlayer = MediaPlayer.create(getContext(), signalId);
        mediaPlayer.start();
        mediaPlayer.setOnCompletionListener(mp -> {
            stopAndReleaseMediaPlayer();
            binding.floatingActionButtonVolumeOff.setVisibility(View.GONE);
        });
        binding.floatingActionButtonVolumeOff.setVisibility(View.VISIBLE);
        binding.floatingActionButtonVolumeOff.setOnClickListener(v -> stopAndReleaseMediaPlayer());
    }

    private void stopAndReleaseMediaPlayer() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
    private void showSignalsPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(requireContext(), view);
        popupMenu.getMenuInflater().inflate(R.menu.melody_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(menuItem -> {
            String newMelodyName = getMelodyName(menuItem.getItemId());
            if (newMelodyName != null) {
                binding.melodyName.setText(newMelodyName);
                Settings settings = readSettings(globalId);
                executorService.submit(() -> alarmViewModel.updateSettings(settings));
                return true;
            }
            return false;
        });
        popupMenu.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        alarmViewModel.unregisterPreferences(requireContext());
        executorService.shutdown();
    }

}

