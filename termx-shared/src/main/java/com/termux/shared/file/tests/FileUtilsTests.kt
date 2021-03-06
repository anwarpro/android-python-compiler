package com.termux.shared.file.tests

import android.content.Context
import com.termux.shared.file.FileUtils.clearDirectory
import com.termux.shared.file.FileUtils.copyDirectoryFile
import com.termux.shared.file.FileUtils.copyRegularFile
import com.termux.shared.file.FileUtils.copySymlinkFile
import com.termux.shared.file.FileUtils.createDirectoryFile
import com.termux.shared.file.FileUtils.createRegularFile
import com.termux.shared.file.FileUtils.createSymlinkFile
import com.termux.shared.file.FileUtils.deleteDirectoryFile
import com.termux.shared.file.FileUtils.deleteRegularFile
import com.termux.shared.file.FileUtils.deleteSymlinkFile
import com.termux.shared.file.FileUtils.directoryFileExists
import com.termux.shared.file.FileUtils.fileExists
import com.termux.shared.file.FileUtils.getCanonicalPath
import com.termux.shared.file.FileUtils.getFileType
import com.termux.shared.file.FileUtils.moveDirectoryFile
import com.termux.shared.file.FileUtils.readStringFromFile
import com.termux.shared.file.FileUtils.regularFileExists
import com.termux.shared.file.FileUtils.symlinkFileExists
import com.termux.shared.file.FileUtils.writeStringToFile
import com.termux.shared.logger.Logger
import com.termux.shared.models.errors.Error
import java.io.File
import java.nio.charset.Charset

object FileUtilsTests {
    private const val LOG_TAG = "FileUtilsTests"

    /**
     * Run basic tests for [FileUtils] class.
     *
     * Move tests need to be written, specially for failures.
     *
     * The log level must be set to verbose.
     *
     * Run at app startup like in an activity
     * FileUtilsTests.runTests(this, TermuxConstants.TERMUX_HOME_DIR_PATH + "/FileUtilsTests");
     *
     * @param context The [Context] for operations.
     */
    fun runTests(context: Context, testRootDirectoryPath: String) {
        try {
            Logger.logInfo(LOG_TAG, "Running tests")
            Logger.logInfo(LOG_TAG, "testRootDirectoryPath: \"$testRootDirectoryPath\"")
            val fileUtilsTestsDirectoryCanonicalPath = getCanonicalPath(testRootDirectoryPath, null)
            assertEqual(
                "FileUtilsTests directory path is not a canonical path",
                testRootDirectoryPath,
                fileUtilsTestsDirectoryCanonicalPath
            )
            runTestsInner(testRootDirectoryPath)
            Logger.logInfo(LOG_TAG, "All tests successful")
        } catch (e: Exception) {
            Logger.logErrorExtended(LOG_TAG, e.message)
            Logger.showToast(
                context,
                if (e.message != null) e.message!!.replace(
                    "(?s)\nFull Error:\n.*".toRegex(),
                    ""
                ) else null,
                true
            )
        }
    }

    @Throws(Exception::class)
    private fun runTestsInner(testRootDirectoryPath: String) {
        var error: Error?
        var label: String
        var path: String?

        /*
         * - dir1
         *  - sub_dir1
         *  - sub_reg1
         *  - sub_sym1 (absolute symlink to dir2)
         *  - sub_sym2 (copy of sub_sym1 for symlink to dir2)
         *  - sub_sym3 (relative symlink to dir4)
         * - dir2
         *  - sub_reg1
         *  - sub_reg2 (copy of dir2/sub_reg1)
         * - dir3 (copy of dir1)
         * - dir4 (moved from dir3)
         */
        val dir1_label = "dir1"
        val dir1_path = "$testRootDirectoryPath/dir1"
        val dir1__sub_dir1_label = "dir1/sub_dir1"
        val dir1__sub_dir1_path = "$dir1_path/sub_dir1"
        val dir1__sub_reg1_label = "dir1/sub_reg1"
        val dir1__sub_reg1_path = "$dir1_path/sub_reg1"
        val dir1__sub_sym1_label = "dir1/sub_sym1"
        val dir1__sub_sym1_path = "$dir1_path/sub_sym1"
        val dir1__sub_sym2_label = "dir1/sub_sym2"
        val dir1__sub_sym2_path = "$dir1_path/sub_sym2"
        val dir1__sub_sym3_label = "dir1/sub_sym3"
        val dir1__sub_sym3_path = "$dir1_path/sub_sym3"
        val dir2_label = "dir2"
        val dir2_path = "$testRootDirectoryPath/dir2"
        val dir2__sub_reg1_label = "dir2/sub_reg1"
        val dir2__sub_reg1_path = "$dir2_path/sub_reg1"
        val dir2__sub_reg2_label = "dir2/sub_reg2"
        val dir2__sub_reg2_path = "$dir2_path/sub_reg2"
        val dir3_label = "dir3"
        val dir3_path = "$testRootDirectoryPath/dir3"
        val dir4_label = "dir4"
        val dir4_path = "$testRootDirectoryPath/dir4"


        // Create or clear test root directory file
        label = "testRootDirectoryPath"
        error = clearDirectory(label, testRootDirectoryPath)
        assertEqual("Failed to create $label directory file", null, error)
        if (!directoryFileExists(testRootDirectoryPath, false)) throwException(
            "The $label directory file does not exist as expected after creation"
        )


        // Create dir1 directory file
        error = createDirectoryFile(dir1_label, dir1_path)
        assertEqual("Failed to create $dir1_label directory file", null, error)

        // Create dir2 directory file
        error = createDirectoryFile(dir2_label, dir2_path)
        assertEqual("Failed to create $dir2_label directory file", null, error)


        // Create dir1/sub_dir1 directory file
        label = dir1__sub_dir1_label
        path = dir1__sub_dir1_path
        error = createDirectoryFile(label, path)
        assertEqual("Failed to create $label directory file", null, error)
        if (!directoryFileExists(path, false)) throwException(
            "The $label directory file does not exist as expected after creation"
        )

        // Create dir1/sub_reg1 regular file
        label = dir1__sub_reg1_label
        path = dir1__sub_reg1_path
        error = createRegularFile(label, path)
        assertEqual("Failed to create $label regular file", null, error)
        if (!regularFileExists(path, false)) throwException(
            "The $label regular file does not exist as expected after creation"
        )

        // Create dir1/sub_sym1 -> dir2 absolute symlink file
        label = dir1__sub_sym1_label
        path = dir1__sub_sym1_path
        error = createSymlinkFile(label, dir2_path, path)
        assertEqual("Failed to create $label symlink file", null, error)
        if (!symlinkFileExists(path)) throwException(
            "The $label symlink file does not exist as expected after creation"
        )

        // Copy dir1/sub_sym1 symlink file to dir1/sub_sym2
        label = dir1__sub_sym2_label
        path = dir1__sub_sym2_path
        error = copySymlinkFile(label, dir1__sub_sym1_path, path, false)
        assertEqual("Failed to copy $dir1__sub_sym1_label symlink file to $label", null, error)
        if (!symlinkFileExists(path)) throwException(
            "The $label symlink file does not exist as expected after copying it from $dir1__sub_sym1_label"
        )
        if (File(path).canonicalPath != dir2_path) throwException("The $label symlink file does not point to $dir2_label")


        // Write "line1" to dir2/sub_reg1 regular file
        label = dir2__sub_reg1_label
        path = dir2__sub_reg1_path
        error = writeStringToFile(label, path, Charset.defaultCharset(), "line1", false)
        assertEqual("Failed to write string to $label file with append mode false", null, error)
        if (!regularFileExists(path, false)) throwException(
            "The $label file does not exist as expected after writing to it with append mode false"
        )

        // Write "line2" to dir2/sub_reg1 regular file
        error = writeStringToFile(label, path, Charset.defaultCharset(), "\nline2", true)
        assertEqual("Failed to write string to $label file with append mode true", null, error)

        // Read dir2/sub_reg1 regular file
        val dataStringBuilder = StringBuilder()
        error = readStringFromFile(label, path, Charset.defaultCharset(), dataStringBuilder, false)
        assertEqual("Failed to read from $label file", null, error)
        assertEqual(
            "The data read from $label file in not as expected",
            "line1\nline2",
            dataStringBuilder.toString()
        )

        // Copy dir2/sub_reg1 regular file to dir2/sub_reg2 file
        label = dir2__sub_reg2_label
        path = dir2__sub_reg2_path
        error = copyRegularFile(label, dir2__sub_reg1_path, path, false)
        assertEqual("Failed to copy $dir2__sub_reg1_label regular file to $label", null, error)
        if (!regularFileExists(path, false)) throwException(
            "The $label regular file does not exist as expected after copying it from $dir2__sub_reg1_label"
        )


        // Copy dir1 directory file to dir3
        label = dir3_label
        path = dir3_path
        error = copyDirectoryFile(label, dir2_path, path, false)
        assertEqual("Failed to copy $dir2_label directory file to $label", null, error)
        if (!directoryFileExists(path, false)) throwException(
            "The $label directory file does not exist as expected after copying it from $dir2_label"
        )

        // Copy dir1 directory file to dir3 again to test overwrite
        label = dir3_label
        path = dir3_path
        error = copyDirectoryFile(label, dir2_path, path, false)
        assertEqual("Failed to copy $dir2_label directory file to $label", null, error)
        if (!directoryFileExists(path, false)) throwException(
            "The $label directory file does not exist as expected after copying it from $dir2_label"
        )

        // Move dir3 directory file to dir4
        label = dir4_label
        path = dir4_path
        error = moveDirectoryFile(label, dir3_path, path, false)
        assertEqual("Failed to move $dir3_label directory file to $label", null, error)
        if (!directoryFileExists(path, false)) throwException(
            "The $label directory file does not exist as expected after copying it from $dir3_label"
        )


        // Create dir1/sub_sym3 -> dir4 relative symlink file
        label = dir1__sub_sym3_label
        path = dir1__sub_sym3_path
        error = createSymlinkFile(label, "../dir4", path)
        assertEqual("Failed to create $label symlink file", null, error)
        if (!symlinkFileExists(path)) throwException(
            "The $label symlink file does not exist as expected after creation"
        )

        // Create dir1/sub_sym3 -> dirX relative dangling symlink file
        // This is to ensure that symlinkFileExists returns true if a symlink file exists but is dangling
        label = dir1__sub_sym3_label
        path = dir1__sub_sym3_path
        error = createSymlinkFile(label, "../dirX", path)
        assertEqual("Failed to create $label symlink file", null, error)
        if (!symlinkFileExists(path)) throwException(
            "The $label dangling symlink file does not exist as expected after creation"
        )


        // Delete dir1/sub_sym2 symlink file
        label = dir1__sub_sym2_label
        path = dir1__sub_sym2_path
        error = deleteSymlinkFile(label, path, false)
        assertEqual("Failed to delete $label symlink file", null, error)
        if (fileExists(
                path,
                false
            )
        ) throwException("The $label symlink file still exist after deletion")

        // Check if dir2 directory file still exists after deletion of dir1/sub_sym2 since it was a symlink to dir2
        // When deleting a symlink file, its target must not be deleted
        label = dir2_label
        path = dir2_path
        if (!directoryFileExists(path, false)) throwException(
            "The $label directory file has unexpectedly been deleted after deletion of $dir1__sub_sym2_label"
        )


        // Delete dir1 directory file
        label = dir1_label
        path = dir1_path
        error = deleteDirectoryFile(label, path, false)
        assertEqual("Failed to delete $label directory file", null, error)
        if (fileExists(
                path,
                false
            )
        ) throwException("The $label directory file still exist after deletion")


        // Check if dir2 directory file and dir2/sub_reg1 regular file still exist after deletion of
        // dir1 since there was a dir1/sub_sym1 symlink to dir2 in it
        // When deleting a directory, any targets of symlinks must not be deleted when deleting symlink files
        label = dir2_label
        path = dir2_path
        if (!directoryFileExists(path, false)) throwException(
            "The $label directory file has unexpectedly been deleted after deletion of $dir1_label"
        )
        label = dir2__sub_reg1_label
        path = dir2__sub_reg1_path
        if (!fileExists(path, false)) throwException(
            "The $label regular file has unexpectedly been deleted after deletion of $dir1_label"
        )


        // Delete dir2/sub_reg1 regular file
        label = dir2__sub_reg1_label
        path = dir2__sub_reg1_path
        error = deleteRegularFile(label, path, false)
        assertEqual("Failed to delete $label regular file", null, error)
        if (fileExists(
                path,
                false
            )
        ) throwException("The $label regular file still exist after deletion")
        getFileType("/dev/ptmx", false)
        getFileType("/dev/null", false)
    }

    @Throws(Exception::class)
    fun assertEqual(message: String, expected: String?, actual: Error?) {
        val actualString = actual?.message
        if (!equalsRegardingNull(expected, actualString)) throwException(
            """
    $message
    expected: "$expected"
    actual: "$actualString"
    Full Error:
    ${actual?.toString() ?: ""}
    """.trimIndent()
        )
    }

    @Throws(Exception::class)
    fun assertEqual(message: String, expected: String, actual: String) {
        if (!equalsRegardingNull(
                expected,
                actual
            )
        ) throwException("$message\nexpected: \"$expected\"\nactual: \"$actual\"")
    }

    private fun equalsRegardingNull(expected: String?, actual: String?): Boolean {
        return if (expected == null) {
            actual == null
        } else isEquals(expected, actual)
    }

    private fun isEquals(expected: String, actual: String?): Boolean {
        return expected == actual
    }

    @Throws(Exception::class)
    fun throwException(message: String) {
        throw Exception(message)
    }
}