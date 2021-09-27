package com.helloanwar.androidprocessbuilder.ui.theme

import androidx.compose.material.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.helloanwar.androidprocessbuilder.R

// Set of Material typography styles to start with
val Typography = Typography(
    body1 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    )
    /* Other default text styles to override
    button = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W500,
        fontSize = 14.sp
    ),
    caption = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp
    )
    */
)

@Composable
fun jetbrainsMono() = FontFamily(
    Font(
        R.font.jetbrainsmono_regular,
        FontWeight.Normal,
        FontStyle.Normal
    ),
    Font(
        R.font.jetbrainsmono_italic,
        FontWeight.Normal,
        FontStyle.Italic
    ),

    Font(
        R.font.jetbrainsmono_bold,
        FontWeight.Bold,
        FontStyle.Normal
    ),
    Font(
        R.font.jetbrainsmono_bold_italic,
        FontWeight.Bold,
        FontStyle.Italic
    ),

    Font(
        R.font.jetbrainsmono_extrabold,
        FontWeight.ExtraBold,
        FontStyle.Normal
    ),
    Font(
        R.font.jetbrainsmono_extrabold_italic,
        FontWeight.ExtraBold,
        FontStyle.Italic
    ),

    Font(
        R.font.jetbrainsmono_medium,
        FontWeight.Medium,
        FontStyle.Normal
    ),
    Font(
        R.font.jetbrainsmono_medium_italic,
        FontWeight.Medium,
        FontStyle.Italic
    )
)