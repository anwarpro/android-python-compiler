package com.termux.shared.packages;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.UserManager;

import androidx.annotation.NonNull;

import com.termux.shared.data.DataUtils;
import com.termux.shared.logger.Logger;

import java.security.MessageDigest;
import java.util.List;

import javax.annotation.Nullable;

public class PackageUtils {

    private static final String LOG_TAG = "PackageUtils";

    /**
     * Get the {@link Context} for the package name.
     *
     * @param context The {@link Context} to use to get the {@link Context} of the {@code packageName}.
     * @param packageName The package name whose {@link Context} to get.
     * @return Returns the {@link Context}. This will {@code null} if an exception is raised.
     */
    @Nullable
    public static Context getContextForPackage(@NonNull final Context context, String packageName) {
        try {
            return context.createPackageContext(packageName, Context.CONTEXT_RESTRICTED);
        } catch (Exception e) {
            Logger.logVerbose(LOG_TAG, "Failed to get \"" + packageName + "\" package context: " + e.getMessage());
            return null;
        }
    }

    /**
     * Get the {@link Context} for a package name.
     *
     * @param context The {@link Context} to use to get the {@link Context} of the {@code packageName}.
     * @param packageName The package name whose {@link Context} to get.
     * @param exitAppOnError If {@code true} and failed to get package context, then a dialog will
     *                       be shown which when dismissed will exit the app.
     * @return Returns the {@link Context}. This will {@code null} if an exception is raised.
     */
    @Nullable
    public static Context getContextForPackageOrExitApp(@NonNull Context context, String packageName, final boolean exitAppOnError) {
        Context packageContext = getContextForPackage(context, packageName);

        return context;
    }

    /**
     * Get the {@link PackageInfo} for the package associated with the {@code context}.
     *
     * @param context The {@link Context} for the package.
     * @return Returns the {@link PackageInfo}. This will be {@code null} if an exception is raised.
     */
    public static PackageInfo getPackageInfoForPackage(@NonNull final Context context) {
            return getPackageInfoForPackage(context, 0);
    }

    /**
     * Get the {@link PackageInfo} for the package associated with the {@code context}.
     *
     * @param context The {@link Context} for the package.
     * @param flags The flags to pass to {@link PackageManager#getPackageInfo(String, int)}.
     * @return Returns the {@link PackageInfo}. This will be {@code null} if an exception is raised.
     */
    @Nullable
    public static PackageInfo getPackageInfoForPackage(@NonNull final Context context, final int flags) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), flags);
        } catch (final Exception e) {
            return null;
        }
    }

    /**
     * Get the app name for the package associated with the {@code context}.
     *
     * @param context The {@link Context} for the package.
     * @return Returns the {@code android:name} attribute.
     */
    public static String getAppNameForPackage(@NonNull final Context context) {
        return context.getApplicationInfo().loadLabel(context.getPackageManager()).toString();
    }

    /**
     * Get the package name for the package associated with the {@code context}.
     *
     * @param context The {@link Context} for the package.
     * @return Returns the package name.
     */
    public static String getPackageNameForPackage(@NonNull final Context context) {
        return context.getApplicationInfo().packageName;
    }

    /**
     * Get the {@code targetSdkVersion} for the package associated with the {@code context}.
     *
     * @param context The {@link Context} for the package.
     * @return Returns the {@code targetSdkVersion}.
     */
    public static int getTargetSDKForPackage(@NonNull final Context context) {
        return context.getApplicationInfo().targetSdkVersion;
    }

    /**
     * Check if the app associated with the {@code context} has {@link ApplicationInfo#FLAG_DEBUGGABLE}
     * set.
     *
     * @param context The {@link Context} for the package.
     * @return Returns the {@code versionName}. This will be {@code null} if an exception is raised.
     */
    public static Boolean isAppForPackageADebugBuild(@NonNull final Context context) {
        return ( 0 != ( context.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE ) );
    }

    /**
     * Check if the app associated with the {@code context} has {@link ApplicationInfo#FLAG_EXTERNAL_STORAGE}
     * set.
     *
     * @param context The {@link Context} for the package.
     * @return Returns the {@code versionName}. This will be {@code null} if an exception is raised.
     */
    public static Boolean isAppInstalledOnExternalStorage(@NonNull final Context context) {
        return ( 0 != ( context.getApplicationInfo().flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE ) );
    }

    /**
     * Get the {@code versionCode} for the package associated with the {@code context}.
     *
     * @param context The {@link Context} for the package.
     * @return Returns the {@code versionCode}. This will be {@code null} if an exception is raised.
     */
    @Nullable
    public static Integer getVersionCodeForPackage(@NonNull final Context context) {
        try {
            return getPackageInfoForPackage(context).versionCode;
        } catch (final Exception e) {
            return null;
        }
    }

    /**
     * Get the {@code versionName} for the package associated with the {@code context}.
     *
     * @param context The {@link Context} for the package.
     * @return Returns the {@code versionName}. This will be {@code null} if an exception is raised.
     */
    @Nullable
    public static String getVersionNameForPackage(@NonNull final Context context) {
        try {
            return getPackageInfoForPackage(context).versionName;
        } catch (final Exception e) {
            return null;
        }
    }

    /**
     * Get the {@code SHA-256 digest} of signing certificate for the package associated with the {@code context}.
     *
     * @param context The {@link Context} for the package.
     * @return Returns the {@code SHA-256 digest}. This will be {@code null} if an exception is raised.
     */
    @Nullable
    public static String getSigningCertificateSHA256DigestForPackage(@NonNull final Context context) {
        try {
            /*
            * Todo: We may need AndroidManifest queries entries if package is installed but with a different signature on android 11
            * https://developer.android.com/training/package-visibility
            * Need a device that allows (manual) installation of apk with mismatched signature of
            * sharedUserId apps to test. Currently, if its done, PackageManager just doesn't load
            * the package and removes its apk automatically if its installed as a user app instead of system app
            * W/PackageManager: Failed to parse /path/to/com.termux.tasker.apk: Signature mismatch for shared user: SharedUserSetting{xxxxxxx com.termux/10xxx}
            */
            PackageInfo packageInfo = getPackageInfoForPackage(context, PackageManager.GET_SIGNATURES);
            if (packageInfo == null) return null;
            return DataUtils.bytesToHex(MessageDigest.getInstance("SHA-256").digest(packageInfo.signatures[0].toByteArray()));
        } catch (final Exception e) {
            return null;
        }
    }



    /**
     * Get the serial number for the current user.
     *
     * @param context The {@link Context} for operations.
     * @return Returns the serial number. This will be {@code null} if failed to get it.
     */
    @Nullable
    public static Long getSerialNumberForCurrentUser(@NonNull Context context) {
        UserManager userManager = (UserManager) context.getSystemService(Context.USER_SERVICE);
        if (userManager == null) return null;
        return userManager.getSerialNumberForUser(android.os.Process.myUserHandle());
    }

    /**
     * Check if the current user is the primary user. This is done by checking if the the serial
     * number for the current user equals 0.
     *
     * @param context The {@link Context} for operations.
     * @return Returns {@code true} if the current user is the primary user, otherwise [@code false}.
     */
    public static boolean isCurrentUserThePrimaryUser(@NonNull Context context) {
        Long userId = getSerialNumberForCurrentUser(context);
        return userId != null && userId == 0;
    }

    /**
     * Get the profile owner package name for the current user.
     *
     * @param context The {@link Context} for operations.
     * @return Returns the profile owner package name. This will be {@code null} if failed to get it
     * or no profile owner for the current user.
     */
    @Nullable
    public static String getProfileOwnerPackageNameForUser(@NonNull Context context) {
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        if (devicePolicyManager == null) return null;
        List<ComponentName> activeAdmins = devicePolicyManager.getActiveAdmins();
        if (activeAdmins != null){
            for (ComponentName admin:activeAdmins){
                String packageName = admin.getPackageName();
                if(devicePolicyManager.isProfileOwnerApp(packageName))
                    return packageName;
            }
        }
        return null;
    }

}
