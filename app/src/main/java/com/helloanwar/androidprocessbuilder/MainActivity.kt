package com.helloanwar.androidprocessbuilder

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import com.helloanwar.androidprocessbuilder.process.Redirect
import com.helloanwar.androidprocessbuilder.process.process
import java.io.File
import java.io.FileOutputStream

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        copyAssets()

        setContent {
//            MainView(file = filesDir)
            Greeting()
        }
    }

    private fun copyAssets() {
        for (filename in assets.list("data")!!) {
            assets.open("data/$filename").use { assetStream ->
                val file = File(filesDir, filename)
                FileOutputStream(file).use { fileStream ->
                    assetStream.copyTo(fileStream)
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
suspend fun getEchoBack() {
    val res = process(
        "echo",
        "hello world",
        stdout = Redirect.CAPTURE,
        stderr = Redirect.CAPTURE,
        consumer = {
            println(it)
        }
    )
}

@Composable
fun Greeting() {
    val sourceCode = """
        # This program adds two numbers

        num1 = 1.5
        num2 = 6.3

        # Add two numbers
        sum = num1 + num2

        # Display the sum
        print('The sum of {0} and {1} is {2}'.format(num1, num2, sum))
    """.trimIndent()

    var code by remember { mutableStateOf(TextFieldValue(text = sourceCode)) }
    BasicTextField(
        value = code,
        onValueChange = {
            code = it
            println(it.selection.start)
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
//    val file = File("demo")
//    MainView(file)

    Greeting()
}