package com.helloanwar.androidprocessbuilder

import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.helloanwar.androidprocessbuilder.editor.Editors
import com.helloanwar.androidprocessbuilder.filetree.FileTree
import com.helloanwar.androidprocessbuilder.ui.theme.AppTheme
import com.helloanwar.androidprocessbuilder.ui.theme.Settings
import java.io.File

@Composable
fun MainView(file: File) {
    val codeViewer = remember {
        val editors = Editors()
        
        CodeViewer(
            editors = editors,
            fileTree = FileTree(file, editors),
            settings = Settings()
        )
    }

    DisableSelection {
        MaterialTheme(
            colors = AppTheme.colors.material
        ) {
            Surface {
                CodeViewerView(codeViewer)
            }
        }
    }
}