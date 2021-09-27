package com.termux.shared.termux

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.termux.shared.R
import com.termux.shared.file.TermuxFileUtils
import com.termux.shared.logger.Logger
import com.termux.shared.markdown.MarkdownUtils
import com.termux.shared.models.ExecutionCommand
import com.termux.shared.models.errors.Error
import com.termux.shared.packages.PackageUtils
import com.termux.shared.shell.TermuxShellEnvironmentClient
import com.termux.shared.shell.TermuxTask
import java.io.IOException
import java.nio.charset.Charset
import java.util.regex.Pattern

object TermuxUtils {
    private const val LOG_TAG = "TermuxUtils"

    /**
     * Get the [Context] for [TermuxConstants.TERMUX_PACKAGE_NAME] package.
     *
     * @param context The [Context] to use to get the [Context] of the package.
     * @return Returns the [Context]. This will `null` if an exception is raised.
     */
    @JvmStatic
    fun getTermuxPackageContext(context: Context): Context? {
        return PackageUtils.getContextForPackage(context, TermuxConstants.TERMUX_PACKAGE_NAME)
    }

    /**
     * Get the [Context] for [TermuxConstants.TERMUX_API_PACKAGE_NAME] package.
     *
     * @param context The [Context] to use to get the [Context] of the package.
     * @return Returns the [Context]. This will `null` if an exception is raised.
     */
    fun getTermuxAPIPackageContext(context: Context): Context? {
        return PackageUtils.getContextForPackage(context, TermuxConstants.TERMUX_API_PACKAGE_NAME)
    }

    /**
     * Get the [Context] for [TermuxConstants.TERMUX_BOOT_PACKAGE_NAME] package.
     *
     * @param context The [Context] to use to get the [Context] of the package.
     * @return Returns the [Context]. This will `null` if an exception is raised.
     */
    fun getTermuxBootPackageContext(context: Context): Context? {
        return PackageUtils.getContextForPackage(context, TermuxConstants.TERMUX_BOOT_PACKAGE_NAME)
    }

    /**
     * Get the [Context] for [TermuxConstants.TERMUX_FLOAT_PACKAGE_NAME] package.
     *
     * @param context The [Context] to use to get the [Context] of the package.
     * @return Returns the [Context]. This will `null` if an exception is raised.
     */
    fun getTermuxFloatPackageContext(context: Context): Context? {
        return PackageUtils.getContextForPackage(context, TermuxConstants.TERMUX_FLOAT_PACKAGE_NAME)
    }

    /**
     * Get the [Context] for [TermuxConstants.TERMUX_STYLING_PACKAGE_NAME] package.
     *
     * @param context The [Context] to use to get the [Context] of the package.
     * @return Returns the [Context]. This will `null` if an exception is raised.
     */
    fun getTermuxStylingPackageContext(context: Context): Context? {
        return PackageUtils.getContextForPackage(
            context,
            TermuxConstants.TERMUX_STYLING_PACKAGE_NAME
        )
    }

    /**
     * Get the [Context] for [TermuxConstants.TERMUX_TASKER_PACKAGE_NAME] package.
     *
     * @param context The [Context] to use to get the [Context] of the package.
     * @return Returns the [Context]. This will `null` if an exception is raised.
     */
    fun getTermuxTaskerPackageContext(context: Context): Context? {
        return PackageUtils.getContextForPackage(
            context,
            TermuxConstants.TERMUX_TASKER_PACKAGE_NAME
        )
    }

    /**
     * Get the [Context] for [TermuxConstants.TERMUX_WIDGET_PACKAGE_NAME] package.
     *
     * @param context The [Context] to use to get the [Context] of the package.
     * @return Returns the [Context]. This will `null` if an exception is raised.
     */
    fun getTermuxWidgetPackageContext(context: Context): Context? {
        return PackageUtils.getContextForPackage(
            context,
            TermuxConstants.TERMUX_WIDGET_PACKAGE_NAME
        )
    }

    /**
     * Send the [TermuxConstants.BROADCAST_TERMUX_OPENED] broadcast to notify apps that Termux
     * app has been opened.
     *
     * @param context The Context to send the broadcast.
     */
    @JvmStatic
    fun sendTermuxOpenedBroadcast(context: Context) {
        val broadcast = Intent(TermuxConstants.BROADCAST_TERMUX_OPENED)
        val matches = context.packageManager.queryBroadcastReceivers(broadcast, 0)

        // send broadcast to registered Termux receivers
        // this technique is needed to work around broadcast changes that Oreo introduced
        for (info in matches) {
            val explicitBroadcast = Intent(broadcast)
            val cname = ComponentName(
                info.activityInfo.applicationInfo.packageName,
                info.activityInfo.name
            )
            explicitBroadcast.component = cname
            context.sendBroadcast(explicitBroadcast)
        }
    }

    /**
     * Get a markdown [String] for the apps info of all/any termux plugin apps installed.
     *
     * @param currentPackageContext The context of current package.
     * @return Returns the markdown [String].
     */
    fun getTermuxPluginAppsInfoMarkdownString(currentPackageContext: Context?): String? {
        if (currentPackageContext == null) return "null"
        val markdownString = StringBuilder()
        val termuxPluginAppPackageNamesList = TermuxConstants.TERMUX_PLUGIN_APP_PACKAGE_NAMES_LIST
        if (termuxPluginAppPackageNamesList != null) {
            for (i in termuxPluginAppPackageNamesList.indices) {
                val termuxPluginAppPackageName = termuxPluginAppPackageNamesList[i]
                val termuxPluginAppContext = PackageUtils.getContextForPackage(
                    currentPackageContext,
                    termuxPluginAppPackageName
                )
                // If the package context for the plugin app is not null, then assume its installed and get its info
                if (termuxPluginAppContext != null) {
                    if (i != 0) markdownString.append("\n\n")
                    markdownString.append(getAppInfoMarkdownString(termuxPluginAppContext, false))
                }
            }
        }
        return if (markdownString.toString().isEmpty()) null else markdownString.toString()
    }

    /**
     * Get a markdown [String] for the app info. If the `context` passed is different
     * from the [TermuxConstants.TERMUX_PACKAGE_NAME] package context, then this function
     * must have been called by a different package like a plugin, so we return info for both packages
     * if `returnTermuxPackageInfoToo` is `true`.
     *
     * @param currentPackageContext The context of current package.
     * @param returnTermuxPackageInfoToo If set to `true`, then will return info of the
     * [TermuxConstants.TERMUX_PACKAGE_NAME] package as well if its different from current package.
     * @return Returns the markdown [String].
     */
    @JvmStatic
    fun getAppInfoMarkdownString(
        currentPackageContext: Context?,
        returnTermuxPackageInfoToo: Boolean
    ): String {
        if (currentPackageContext == null) return "null"
        val markdownString = StringBuilder()
        val termuxPackageContext = getTermuxPackageContext(currentPackageContext)
        var termuxPackageName: String? = null
        var termuxAppName: String? = null
        if (termuxPackageContext != null) {
            termuxPackageName = PackageUtils.getPackageNameForPackage(termuxPackageContext)
            termuxAppName = PackageUtils.getAppNameForPackage(termuxPackageContext)
        }
        val currentPackageName = PackageUtils.getPackageNameForPackage(currentPackageContext)
        val currentAppName = PackageUtils.getAppNameForPackage(currentPackageContext)
        val isTermuxPackage = termuxPackageName != null && termuxPackageName == currentPackageName
        if (returnTermuxPackageInfoToo && !isTermuxPackage) markdownString.append("## ")
            .append(currentAppName)
            .append(" App Info (Current)\n") else markdownString.append("## ")
            .append(currentAppName).append(" App Info\n")
        markdownString.append(getAppInfoMarkdownStringInner(currentPackageContext))
        if (returnTermuxPackageInfoToo && termuxPackageContext != null && !isTermuxPackage) {
            markdownString.append("\n\n## ").append(termuxAppName).append(" App Info\n")
            markdownString.append(getAppInfoMarkdownStringInner(termuxPackageContext))
        }
        markdownString.append("\n##\n")
        return markdownString.toString()
    }

    /**
     * Get a markdown [String] for the app info for the package associated with the `context`.
     *
     * @param context The context for operations for the package.
     * @return Returns the markdown [String].
     */
    fun getAppInfoMarkdownStringInner(context: Context): String {
        val markdownString = StringBuilder()
        markdownString.append(AndroidUtils.getAppInfoMarkdownString(context))
        val error: Error?
        error = TermuxFileUtils.isTermuxFilesDirectoryAccessible(context, true, true)
        if (error != null) {
            AndroidUtils.appendPropertyToMarkdown(
                markdownString,
                "TERMUX_FILES_DIR",
                TermuxConstants.TERMUX_FILES_DIR_PATH
            )
            AndroidUtils.appendPropertyToMarkdown(
                markdownString,
                "IS_TERMUX_FILES_DIR_ACCESSIBLE",
                "false - " + Error.getMinimalErrorString(error)
            )
        }
        val signingCertificateSHA256Digest =
            PackageUtils.getSigningCertificateSHA256DigestForPackage(context)
        if (signingCertificateSHA256Digest != null) {
            AndroidUtils.appendPropertyToMarkdown(
                markdownString,
                "APK_RELEASE",
                getAPKRelease(signingCertificateSHA256Digest)
            )
            AndroidUtils.appendPropertyToMarkdown(
                markdownString,
                "SIGNING_CERTIFICATE_SHA256_DIGEST",
                signingCertificateSHA256Digest
            )
        }
        return markdownString.toString()
    }

    /**
     * Get a markdown [String] for reporting an issue.
     *
     * @param context The context for operations.
     * @return Returns the markdown [String].
     */
    @JvmStatic
    fun getReportIssueMarkdownString(context: Context): String {
        if (context == null) return "null"
        val markdownString = StringBuilder()
        markdownString.append("## Where To Report An Issue")
        markdownString.append("\n\n")
            .append(context.getString(R.string.msg_report_issue, TermuxConstants.TERMUX_WIKI_URL))
            .append("\n")
        markdownString.append("\n\n### Email\n")
        markdownString.append("\n").append(
            MarkdownUtils.getLinkMarkdownString(
                TermuxConstants.TERMUX_SUPPORT_EMAIL_URL,
                TermuxConstants.TERMUX_SUPPORT_EMAIL_MAILTO_URL
            )
        ).append("  ")
        markdownString.append("\n\n### Reddit\n")
        markdownString.append("\n").append(
            MarkdownUtils.getLinkMarkdownString(
                TermuxConstants.TERMUX_REDDIT_SUBREDDIT,
                TermuxConstants.TERMUX_REDDIT_SUBREDDIT_URL
            )
        ).append("  ")
        markdownString.append("\n\n### Github Issues for Termux apps\n")
        markdownString.append("\n").append(
            MarkdownUtils.getLinkMarkdownString(
                TermuxConstants.TERMUX_APP_NAME,
                TermuxConstants.TERMUX_GITHUB_ISSUES_REPO_URL
            )
        ).append("  ")
        markdownString.append("\n").append(
            MarkdownUtils.getLinkMarkdownString(
                TermuxConstants.TERMUX_API_APP_NAME,
                TermuxConstants.TERMUX_API_GITHUB_ISSUES_REPO_URL
            )
        ).append("  ")
        markdownString.append("\n").append(
            MarkdownUtils.getLinkMarkdownString(
                TermuxConstants.TERMUX_BOOT_APP_NAME,
                TermuxConstants.TERMUX_BOOT_GITHUB_ISSUES_REPO_URL
            )
        ).append("  ")
        markdownString.append("\n").append(
            MarkdownUtils.getLinkMarkdownString(
                TermuxConstants.TERMUX_FLOAT_APP_NAME,
                TermuxConstants.TERMUX_FLOAT_GITHUB_ISSUES_REPO_URL
            )
        ).append("  ")
        markdownString.append("\n").append(
            MarkdownUtils.getLinkMarkdownString(
                TermuxConstants.TERMUX_STYLING_APP_NAME,
                TermuxConstants.TERMUX_STYLING_GITHUB_ISSUES_REPO_URL
            )
        ).append("  ")
        markdownString.append("\n").append(
            MarkdownUtils.getLinkMarkdownString(
                TermuxConstants.TERMUX_TASKER_APP_NAME,
                TermuxConstants.TERMUX_TASKER_GITHUB_ISSUES_REPO_URL
            )
        ).append("  ")
        markdownString.append("\n").append(
            MarkdownUtils.getLinkMarkdownString(
                TermuxConstants.TERMUX_WIDGET_APP_NAME,
                TermuxConstants.TERMUX_WIDGET_GITHUB_ISSUES_REPO_URL
            )
        ).append("  ")
        markdownString.append("\n\n### Github Issues for Termux packages\n")
        markdownString.append("\n").append(
            MarkdownUtils.getLinkMarkdownString(
                TermuxConstants.TERMUX_PACKAGES_GITHUB_REPO_NAME,
                TermuxConstants.TERMUX_PACKAGES_GITHUB_ISSUES_REPO_URL
            )
        ).append("  ")
        markdownString.append("\n").append(
            MarkdownUtils.getLinkMarkdownString(
                TermuxConstants.TERMUX_GAME_PACKAGES_GITHUB_REPO_NAME,
                TermuxConstants.TERMUX_GAME_PACKAGES_GITHUB_ISSUES_REPO_URL
            )
        ).append("  ")
        markdownString.append("\n").append(
            MarkdownUtils.getLinkMarkdownString(
                TermuxConstants.TERMUX_SCIENCE_PACKAGES_GITHUB_REPO_NAME,
                TermuxConstants.TERMUX_SCIENCE_PACKAGES_GITHUB_ISSUES_REPO_URL
            )
        ).append("  ")
        markdownString.append("\n").append(
            MarkdownUtils.getLinkMarkdownString(
                TermuxConstants.TERMUX_ROOT_PACKAGES_GITHUB_REPO_NAME,
                TermuxConstants.TERMUX_ROOT_PACKAGES_GITHUB_ISSUES_REPO_URL
            )
        ).append("  ")
        markdownString.append("\n").append(
            MarkdownUtils.getLinkMarkdownString(
                TermuxConstants.TERMUX_UNSTABLE_PACKAGES_GITHUB_REPO_NAME,
                TermuxConstants.TERMUX_UNSTABLE_PACKAGES_GITHUB_ISSUES_REPO_URL
            )
        ).append("  ")
        markdownString.append("\n").append(
            MarkdownUtils.getLinkMarkdownString(
                TermuxConstants.TERMUX_X11_PACKAGES_GITHUB_REPO_NAME,
                TermuxConstants.TERMUX_X11_PACKAGES_GITHUB_ISSUES_REPO_URL
            )
        ).append("  ")
        markdownString.append("\n##\n")
        return markdownString.toString()
    }

    /**
     * Get a markdown [String] for important links.
     *
     * @param context The context for operations.
     * @return Returns the markdown [String].
     */
    fun getImportantLinksMarkdownString(context: Context): String {
        if (context == null) return "null"
        val markdownString = StringBuilder()
        markdownString.append("## Important Links")
        markdownString.append("\n\n### Github\n")
        markdownString.append("\n").append(
            MarkdownUtils.getLinkMarkdownString(
                TermuxConstants.TERMUX_APP_NAME,
                TermuxConstants.TERMUX_GITHUB_REPO_URL
            )
        ).append("  ")
        markdownString.append("\n").append(
            MarkdownUtils.getLinkMarkdownString(
                TermuxConstants.TERMUX_API_APP_NAME,
                TermuxConstants.TERMUX_API_GITHUB_REPO_URL
            )
        ).append("  ")
        markdownString.append("\n").append(
            MarkdownUtils.getLinkMarkdownString(
                TermuxConstants.TERMUX_BOOT_APP_NAME,
                TermuxConstants.TERMUX_BOOT_GITHUB_REPO_URL
            )
        ).append("  ")
        markdownString.append("\n").append(
            MarkdownUtils.getLinkMarkdownString(
                TermuxConstants.TERMUX_FLOAT_APP_NAME,
                TermuxConstants.TERMUX_FLOAT_GITHUB_REPO_URL
            )
        ).append("  ")
        markdownString.append("\n").append(
            MarkdownUtils.getLinkMarkdownString(
                TermuxConstants.TERMUX_STYLING_APP_NAME,
                TermuxConstants.TERMUX_STYLING_GITHUB_REPO_URL
            )
        ).append("  ")
        markdownString.append("\n").append(
            MarkdownUtils.getLinkMarkdownString(
                TermuxConstants.TERMUX_TASKER_APP_NAME,
                TermuxConstants.TERMUX_TASKER_GITHUB_REPO_URL
            )
        ).append("  ")
        markdownString.append("\n").append(
            MarkdownUtils.getLinkMarkdownString(
                TermuxConstants.TERMUX_WIDGET_APP_NAME,
                TermuxConstants.TERMUX_WIDGET_GITHUB_REPO_URL
            )
        ).append("  ")
        markdownString.append("\n").append(
            MarkdownUtils.getLinkMarkdownString(
                TermuxConstants.TERMUX_PACKAGES_GITHUB_REPO_NAME,
                TermuxConstants.TERMUX_PACKAGES_GITHUB_REPO_URL
            )
        ).append("  ")
        markdownString.append("\n\n### Email\n")
        markdownString.append("\n").append(
            MarkdownUtils.getLinkMarkdownString(
                TermuxConstants.TERMUX_SUPPORT_EMAIL_URL,
                TermuxConstants.TERMUX_SUPPORT_EMAIL_MAILTO_URL
            )
        ).append("  ")
        markdownString.append("\n\n### Reddit\n")
        markdownString.append("\n").append(
            MarkdownUtils.getLinkMarkdownString(
                TermuxConstants.TERMUX_REDDIT_SUBREDDIT,
                TermuxConstants.TERMUX_REDDIT_SUBREDDIT_URL
            )
        ).append("  ")
        markdownString.append("\n\n### Wiki\n")
        markdownString.append("\n").append(
            MarkdownUtils.getLinkMarkdownString(
                TermuxConstants.TERMUX_WIKI,
                TermuxConstants.TERMUX_WIKI_URL
            )
        ).append("  ")
        markdownString.append("\n").append(
            MarkdownUtils.getLinkMarkdownString(
                TermuxConstants.TERMUX_APP_NAME,
                TermuxConstants.TERMUX_GITHUB_WIKI_REPO_URL
            )
        ).append("  ")
        markdownString.append("\n").append(
            MarkdownUtils.getLinkMarkdownString(
                TermuxConstants.TERMUX_PACKAGES_GITHUB_REPO_NAME,
                TermuxConstants.TERMUX_PACKAGES_GITHUB_WIKI_REPO_URL
            )
        ).append("  ")
        markdownString.append("\n##\n")
        return markdownString.toString()
    }

    /**
     * Get a markdown [String] for APT info of the app.
     *
     * This will take a few seconds to run due to running `apt update` command.
     *
     * @param context The context for operations.
     * @return Returns the markdown [String].
     */
    @JvmStatic
    fun geAPTInfoMarkdownString(context: Context): String? {
        var aptInfoScript: String? = null

        try {
            context.resources.openRawResource(R.raw.apt_info_script).use {
                aptInfoScript = it.bufferedReader(Charset.defaultCharset()).readLines()
                    .joinToString("\n")
            }
        } catch (e: IOException) {
            Logger.logError(LOG_TAG, "Failed to get APT info script: " + e.message)
            return null
        }

        if (aptInfoScript == null || aptInfoScript?.isEmpty() == true) {
            Logger.logError(LOG_TAG, "The APT info script is null or empty")
            return null
        }
        aptInfoScript = aptInfoScript?.replace(
            Pattern.quote("@TERMUX_PREFIX@").toRegex(),
            TermuxConstants.TERMUX_PREFIX_DIR_PATH
        )

        val executionCommand = ExecutionCommand(
            1,
            TermuxConstants.TERMUX_BIN_PREFIX_DIR_PATH + "/bash",
            null,
            aptInfoScript,
            null,
            true,
            false
        )
        executionCommand.commandLabel = "APT Info Command"
        executionCommand.backgroundCustomLogLevel = Logger.LOG_LEVEL_OFF
        val termuxTask = TermuxTask.execute(
            context,
            executionCommand,
            null,
            TermuxShellEnvironmentClient(),
            true
        )
        if (termuxTask == null || !executionCommand.isSuccessful || executionCommand.resultData.exitCode != 0) {
            Logger.logErrorExtended(LOG_TAG, executionCommand.toString())
            return null
        }
        if (!executionCommand.resultData.stderr.toString().isEmpty()) Logger.logErrorExtended(
            LOG_TAG, executionCommand.toString()
        )
        val markdownString = StringBuilder()
        markdownString.append("## ").append(TermuxConstants.TERMUX_APP_NAME).append(" APT Info\n\n")
        markdownString.append(executionCommand.resultData.stdout.toString())
        markdownString.append("\n##\n")
        return markdownString.toString()
    }

    /**
     * Get a markdown [String] for info for termux debugging.
     *
     * @param context The context for operations.
     * @return Returns the markdown [String].
     */
    @JvmStatic
    fun getTermuxDebugMarkdownString(context: Context): String? {
        val statInfo = TermuxFileUtils.getTermuxFilesStatMarkdownString(context)
        val logcatInfo = getLogcatDumpMarkdownString(context)
        return if (statInfo != null && logcatInfo != null) """
     $statInfo
     
     $logcatInfo
     """.trimIndent() else statInfo ?: logcatInfo
    }

    /**
     * Get a markdown [String] for logcat command dump.
     *
     * @param context The context for operations.
     * @return Returns the markdown [String].
     */
    fun getLogcatDumpMarkdownString(context: Context): String? {
        // Build script
        // We need to prevent OutOfMemoryError since StreamGobbler StringBuilder + StringBuilder.toString()
        // may require lot of memory if dump is too large.
        // Putting a limit at 3000 lines. Assuming average 160 chars/line will result in 500KB usage
        // per object.
        // That many lines should be enough for debugging for recent issues anyways assuming termux
        // has not been granted READ_LOGS permission s.
        val logcatScript = "/system/bin/logcat -d -t 3000 2>&1"

        // Run script
        // Logging must be disabled for output of logcat command itself in StreamGobbler
        val executionCommand = ExecutionCommand(
            1, "/system/bin/sh", null, """
     $logcatScript
     
     """.trimIndent(), "/", true, true
        )
        executionCommand.commandLabel = "Logcat dump command"
        executionCommand.backgroundCustomLogLevel = Logger.LOG_LEVEL_OFF
        val termuxTask = TermuxTask.execute(
            context,
            executionCommand,
            null,
            TermuxShellEnvironmentClient(),
            true
        )
        if (termuxTask == null || !executionCommand.isSuccessful) {
            Logger.logErrorExtended(LOG_TAG, executionCommand.toString())
            return null
        }

        // Build script output
        val logcatOutput = StringBuilder()
        logcatOutput.append("$ ").append(logcatScript)
        logcatOutput.append("\n").append(executionCommand.resultData.stdout.toString())
        val stderrSet = !executionCommand.resultData.stderr.toString().isEmpty()
        if (executionCommand.resultData.exitCode != 0 || stderrSet) {
            Logger.logErrorExtended(LOG_TAG, executionCommand.toString())
            if (stderrSet) logcatOutput.append("\n")
                .append(executionCommand.resultData.stderr.toString())
            logcatOutput.append("\n").append("exit code: ")
                .append(executionCommand.resultData.exitCode.toString())
        }

        // Build markdown output
        val markdownString = StringBuilder()
        markdownString.append("## Logcat Dump\n\n")
        markdownString.append("\n\n")
            .append(MarkdownUtils.getMarkdownCodeForString(logcatOutput.toString(), true))
        markdownString.append("\n##\n")
        return markdownString.toString()
    }

    fun getAPKRelease(signingCertificateSHA256Digest: String?): String {
        return if (signingCertificateSHA256Digest == null) "null" else when (signingCertificateSHA256Digest.toUpperCase()) {
            TermuxConstants.APK_RELEASE_FDROID_SIGNING_CERTIFICATE_SHA256_DIGEST -> TermuxConstants.APK_RELEASE_FDROID
            TermuxConstants.APK_RELEASE_GITHUB_DEBUG_BUILD_SIGNING_CERTIFICATE_SHA256_DIGEST -> TermuxConstants.APK_RELEASE_GITHUB_DEBUG_BUILD
            TermuxConstants.APK_RELEASE_GOOGLE_PLAYSTORE_SIGNING_CERTIFICATE_SHA256_DIGEST -> TermuxConstants.APK_RELEASE_GOOGLE_PLAYSTORE
            else -> "Unknown"
        }
    }
}