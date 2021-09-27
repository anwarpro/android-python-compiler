package com.helloanwar.androidprocessbuilder

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.helloanwar.androidprocessbuilder.editor.EditorEmptyView
import com.helloanwar.androidprocessbuilder.editor.EditorTabsView
import com.helloanwar.androidprocessbuilder.editor.EditorView
import com.helloanwar.androidprocessbuilder.filetree.FileTreeView
import com.helloanwar.androidprocessbuilder.filetree.FileTreeViewTabView
import com.helloanwar.androidprocessbuilder.util.StatusBar

@Composable
fun CodeViewerView(model: CodeViewer) {
    val scaffoldState = rememberScaffoldState()

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(title = {
                Text(text = "Code Editor")
            })
        },
        drawerContent = {
            Column {
                FileTreeViewTabView()
                FileTreeView(model.fileTree)
            }
        }
    ) {
        Box(Modifier.fillMaxSize()) {
            if (model.editors.active != null) {
                Column(Modifier.fillMaxSize()) {
                    EditorTabsView(model.editors)
                    Box(
                        Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        EditorView(model.editors.active!!, model.settings)
                    }
                    StatusBar(model.settings)
                }
            } else {
                EditorEmptyView()
            }
        }
    }
}