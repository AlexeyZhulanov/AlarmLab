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

import androidx.annotation.NonNull;
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
    private SettingsViewModel settingsViewModel;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @SuppressLint("DiscouragedApi")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        settingsViewModel = new ViewModelProvider(this).get(SettingsViewModel.class);
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        settingsViewModel.registerPreferences();
        settingsViewModel.wallpaper.observe(getViewLifecycleOwner(), this::updateWallpapers);
        settingsViewModel.getPreferencesWallpaper(result -> updateWallpapers(result.first));
        int themeNumber = settingsViewModel.getPreferencesTheme() + 1;
        String themeText = getString(R.string.theme) + themeNumber;
        if(themeNumber != 1) binding.colorThemeName.setText(themeText); else binding.colorThemeName.setText(R.string.classic);
        // Update wallpapers and preferences
        executorService.submit(() -> {
            Settings settings = settingsViewModel.getSettings();
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
            String melody = settingsViewModel.getSettings().getMelody();
            int signalId = getMelodyResource(melody);
            requireActivity().runOnUiThread(() -> playMelody(signalId));
        }));

        binding.switchVibration.setOnClickListener(v -> {
            Settings s = readSettings(globalId);
            executorService.submit(() -> settingsViewModel.updateSettings(s));
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
        executorService.submit(() -> settingsViewModel.updateSettings(s));
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
        settingsViewModel.editPreferencesInterval(interval);
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
        return switch (melody) {
            case "Melody 1" -> R.raw.default_signal1;
            case "Melody 2" -> R.raw.default_signal2;
            case "Melody 3" -> R.raw.default_signal3;
            case "Melody 4" -> R.raw.default_signal4;
            case "Melody 5" -> R.raw.default_signal5;
            case "Ship alarm" -> R.raw.signal;
            case "Banjo" -> R.raw.banjo_signal;
            case "Morning" -> R.raw.morning_signal;
            case "Simple" -> R.raw.simple_signal;
            case "Fitness" -> R.raw.fitness_signal;
            case "Medieval" -> R.raw.medieval_signal;
            case "Introduction" -> R.raw.introduction_signal;
            default -> -1; // Melody not selected
        };
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
            String selectedWallpaper = switch (menuItem.title) {
                case "1." -> getString(R.string.wallpaper1);
                case "2." -> getString(R.string.wallpaper2);
                case "3." -> getString(R.string.wallpaper3);
                case "4." -> getString(R.string.wallpaper4);
                case "5." -> getString(R.string.wallpaper5);
                case "6." -> getString(R.string.wallpaper6);
                case "7." -> getString(R.string.wallpaper7);
                case "8." -> getString(R.string.wallpaper8);
                case "9." -> getString(R.string.wallpaper9);
                case "10." -> getString(R.string.wallpaper10);
                case "Classic" -> "";
                default -> ""; // Default case
            };
            // Save the selected wallpaper
            settingsViewModel.editPreferencesWallpaper(selectedWallpaper);
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
            settingsViewModel.editPreferencesTheme(menuItem.getThemeNumber());
            requireActivity().recreate();
            popupWindow.dismiss();
        });

        recyclerView.setAdapter(adapter);

        popupWindow.showAsDropDown(view);
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
            binding.wallpaperName.setText(R.string.classic);
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
        binding.floatingActionButtonVolumeOff.setOnClickListener(v -> {
            stopAndReleaseMediaPlayer();
            binding.floatingActionButtonVolumeOff.setVisibility(View.GONE);
        });
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
                executorService.submit(() -> settingsViewModel.updateSettings(settings));
                return true;
            }
            return false;
        });
        popupMenu.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        settingsViewModel.unregisterPreferences();
        executorService.shutdown();
    }

}

