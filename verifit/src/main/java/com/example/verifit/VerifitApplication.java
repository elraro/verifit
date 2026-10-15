package com.example.verifit;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

// Android 15+ enforces edge-to-edge, so pad each activity's content below the system bars.
public class VerifitApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        applyTheme(PreferenceManager.getDefaultSharedPreferences(this).getString("theme", "system"));
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityPostCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
                View content = activity.findViewById(android.R.id.content);
                if (content == null) return;
                ViewCompat.setOnApplyWindowInsetsListener(content, (v, windowInsets) -> {
                    Insets bars = windowInsets.getInsets(
                            WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout() | WindowInsetsCompat.Type.ime());
                    v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
                    return WindowInsetsCompat.CONSUMED;
                });
                ViewCompat.requestApplyInsets(content);
            }
            @Override public void onActivityCreated(@NonNull Activity a, @Nullable Bundle b) {}
            @Override public void onActivityStarted(@NonNull Activity a) {}
            @Override public void onActivityResumed(@NonNull Activity a) {}
            @Override public void onActivityPaused(@NonNull Activity a) {}
            @Override public void onActivityStopped(@NonNull Activity a) {}
            @Override public void onActivitySaveInstanceState(@NonNull Activity a, @NonNull Bundle b) {}
            @Override public void onActivityDestroyed(@NonNull Activity a) {}
        });
    }

    public static void applyTheme(String theme) {
        switch (theme) {
            case "light":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "dark":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }
    }
}
