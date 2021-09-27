package com.termux.shared.settings.preferences;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.termux.shared.logger.Logger;
import com.termux.shared.packages.PackageUtils;
import com.termux.shared.settings.preferences.TermuxPreferenceConstants.TERMUX_WIDGET_APP;
import com.termux.shared.termux.TermuxConstants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TermuxWidgetAppSharedPreferences {

    private final Context mContext;
    private final SharedPreferences mSharedPreferences;


    private static final String LOG_TAG = "TermuxWidgetAppSharedPreferences";

    private TermuxWidgetAppSharedPreferences(@Nonnull Context context) {
        mContext = context;
        mSharedPreferences = getPrivateSharedPreferences(mContext);
    }

    /**
     * Get the {@link Context} for a package name.
     *
     * @param context The {@link Context} to use to get the {@link Context} of the
     *                {@link TermuxConstants#TERMUX_WIDGET_PACKAGE_NAME}.
     * @return Returns the {@link TermuxWidgetAppSharedPreferences}. This will {@code null} if an exception is raised.
     */
    @Nullable
    public static TermuxWidgetAppSharedPreferences build(@NonNull final Context context) {
        Context termuxTaskerPackageContext = PackageUtils.getContextForPackage(context, TermuxConstants.TERMUX_WIDGET_PACKAGE_NAME);
        if (termuxTaskerPackageContext == null)
            return null;
        else
            return new TermuxWidgetAppSharedPreferences(termuxTaskerPackageContext);
    }

    /**
     * Get the {@link Context} for a package name.
     *
     * @param context The {@link Activity} to use to get the {@link Context} of the
     *                {@link TermuxConstants#TERMUX_WIDGET_PACKAGE_NAME}.
     * @param exitAppOnError If {@code true} and failed to get package context, then a dialog will
     *                       be shown which when dismissed will exit the app.
     * @return Returns the {@link TermuxWidgetAppSharedPreferences}. This will {@code null} if an exception is raised.
     */
    public static TermuxWidgetAppSharedPreferences build(@NonNull final Context context, final boolean exitAppOnError) {
        Context termuxTaskerPackageContext = PackageUtils.getContextForPackageOrExitApp(context, TermuxConstants.TERMUX_WIDGET_PACKAGE_NAME, exitAppOnError);
        if (termuxTaskerPackageContext == null)
            return null;
        else
            return new TermuxWidgetAppSharedPreferences(termuxTaskerPackageContext);
    }

    private static SharedPreferences getPrivateSharedPreferences(Context context) {
        if (context == null) return null;
        return SharedPreferenceUtils.getPrivateSharedPreferences(context, TermuxConstants.TERMUX_WIDGET_DEFAULT_PREFERENCES_FILE_BASENAME_WITHOUT_EXTENSION);
    }



    public int getLogLevel() {
        return SharedPreferenceUtils.getInt(mSharedPreferences, TERMUX_WIDGET_APP.KEY_LOG_LEVEL, Logger.DEFAULT_LOG_LEVEL);
    }

    public void setLogLevel(Context context, int logLevel) {
        logLevel = Logger.setLogLevel(context, logLevel);
        SharedPreferenceUtils.setInt(mSharedPreferences, TERMUX_WIDGET_APP.KEY_LOG_LEVEL, logLevel, false);
    }

}
