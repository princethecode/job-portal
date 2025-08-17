package com.example.jobportal.ui.settings;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.example.jobportal.R;

import java.util.Locale;

public class SettingsFragment extends Fragment {
    
    private static final String PREFS_NAME = "JobPortalPrefs";
    private static final String PREF_THEME = "theme_mode";
    private static final String PREF_LANGUAGE = "language";
    
    private TextView currentTheme;
    private TextView currentLanguage;
    private TextView versionText;
    private SharedPreferences prefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        
        initViews(view);
        setupClickListeners();
        updateCurrentSettings();
    }

    private void initViews(View view) {
        currentTheme = view.findViewById(R.id.currentTheme);
        currentLanguage = view.findViewById(R.id.currentLanguage);
        versionText = view.findViewById(R.id.versionText);
        
        // Set app version
        try {
            PackageInfo pInfo = requireContext().getPackageManager().getPackageInfo(requireContext().getPackageName(), 0);
            versionText.setText(pInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            versionText.setText("1.0.0");
        }
    }

    private void setupClickListeners() {
        // Theme selector
        View themeSelector = requireView().findViewById(R.id.themeSelector);
        themeSelector.setOnClickListener(v -> showThemeDialog());
        
        // Language selector
        View languageSelector = requireView().findViewById(R.id.languageSelector);
        languageSelector.setOnClickListener(v -> showLanguageDialog());
        
        // Privacy Policy
        View privacyPolicyItem = requireView().findViewById(R.id.privacyPolicyItem);
        privacyPolicyItem.setOnClickListener(v -> openPrivacyPolicy());
        
        // Terms & Conditions
        View termsConditionsItem = requireView().findViewById(R.id.termsConditionsItem);
        termsConditionsItem.setOnClickListener(v -> openTermsConditions());
        
        // Account Deletion
        View accountDeletionItem = requireView().findViewById(R.id.accountDeletionItem);
        accountDeletionItem.setOnClickListener(v -> showAccountDeletionDialog());
        
        // App Version (show additional info)
        View appVersionItem = requireView().findViewById(R.id.appVersionItem);
        appVersionItem.setOnClickListener(v -> showAppInfo());
    }

    private void updateCurrentSettings() {
        // Update theme display
        int currentMode = prefs.getInt(PREF_THEME, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        String themeText;
        switch (currentMode) {
            case AppCompatDelegate.MODE_NIGHT_YES:
                themeText = getString(R.string.dark_mode);
                break;
            case AppCompatDelegate.MODE_NIGHT_NO:
                themeText = getString(R.string.light_mode);
                break;
            default:
                themeText = getString(R.string.system_default);
                break;
        }
        currentTheme.setText(themeText);
        
        // Update language display
        String currentLang = prefs.getString(PREF_LANGUAGE, "en");
        String langText = currentLang.equals("hi") ? getString(R.string.hindi) : getString(R.string.english);
        currentLanguage.setText(langText);
    }

    private void showThemeDialog() {
        String[] themes = {getString(R.string.light_mode), getString(R.string.dark_mode), getString(R.string.system_default)};
        int currentMode = prefs.getInt(PREF_THEME, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        int selectedIndex = 0;
        
        switch (currentMode) {
            case AppCompatDelegate.MODE_NIGHT_NO:
                selectedIndex = 0;
                break;
            case AppCompatDelegate.MODE_NIGHT_YES:
                selectedIndex = 1;
                break;
            case AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM:
                selectedIndex = 2;
                break;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.choose_theme))
                .setSingleChoiceItems(themes, selectedIndex, (dialog, which) -> {
                    int newMode;
                    switch (which) {
                        case 0:
                            newMode = AppCompatDelegate.MODE_NIGHT_NO;
                            break;
                        case 1:
                            newMode = AppCompatDelegate.MODE_NIGHT_YES;
                            break;
                        default:
                            newMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                            break;
                    }
                    
                    prefs.edit().putInt(PREF_THEME, newMode).apply();
                    AppCompatDelegate.setDefaultNightMode(newMode);
                    updateCurrentSettings();
                    dialog.dismiss();
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    private void showLanguageDialog() {
        String[] languages = {getString(R.string.english), getString(R.string.hindi)};
        String currentLang = prefs.getString(PREF_LANGUAGE, "en");
        int selectedIndex = currentLang.equals("hi") ? 1 : 0;

        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.choose_language))
                .setSingleChoiceItems(languages, selectedIndex, (dialog, which) -> {
                    String newLang = which == 1 ? "hi" : "en";
                    prefs.edit().putString(PREF_LANGUAGE, newLang).apply();
                    setLocale(newLang);
                    updateCurrentSettings();
                    dialog.dismiss();
                    
                    // Restart activity to apply language change
                    requireActivity().recreate();
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    private void setLocale(String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        requireContext().getResources().updateConfiguration(config, requireContext().getResources().getDisplayMetrics());
    }

    private void openPrivacyPolicy() {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://emps.co.in/privacy-policy"));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(requireContext(), getString(R.string.error_opening_link), Toast.LENGTH_SHORT).show();
        }
    }

    private void openTermsConditions() {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://emps.co.in/terms-of-service"));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(requireContext(), getString(R.string.error_opening_link), Toast.LENGTH_SHORT).show();
        }
    }

    private void showAccountDeletionDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.delete_account))
                .setMessage(getString(R.string.delete_account_warning))
                .setIcon(R.drawable.ic_warning)
                .setPositiveButton(getString(R.string.proceed), (dialog, which) -> openAccountDeletionPage())
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    private void openAccountDeletionPage() {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://emps.co.in/account-remove"));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(requireContext(), getString(R.string.error_opening_link), Toast.LENGTH_SHORT).show();
        }
    }

    private void showAppInfo() {
        try {
            PackageInfo pInfo = requireContext().getPackageManager().getPackageInfo(requireContext().getPackageName(), 0);
            String message = getString(R.string.app_info_details, 
                    pInfo.versionName, 
                    String.valueOf(pInfo.versionCode),
                    android.os.Build.VERSION.RELEASE,
                    String.valueOf(android.os.Build.VERSION.SDK_INT));
            
            new AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.app_information))
                    .setMessage(message)
                    .setPositiveButton(getString(R.string.ok), null)
                    .show();
        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(requireContext(), getString(R.string.error_getting_app_info), Toast.LENGTH_SHORT).show();
        }
    }
}