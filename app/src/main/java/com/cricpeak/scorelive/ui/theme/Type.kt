package com.cricpeak.scorelive.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontStyle
import com.cricpeak.scorelive.R

// Define Bayon font
val BayonFont = FontFamily(Font(R.font.bayonregular))

// Define Gilroy font family with all weights
val GilroyFontFamily = FontFamily(
    Font(R.font.gilroy_thin, FontWeight.Thin),
    Font(R.font.gilroy_thin_italic, FontWeight.Thin, FontStyle.Italic),
    Font(R.font.gilroy_ultralight, FontWeight.ExtraLight),
    Font(R.font.gilroy_ultralight_italic, FontWeight.ExtraLight, FontStyle.Italic),
    Font(R.font.gilroy_light, FontWeight.Light),
    Font(R.font.gilroy_light_italic, FontWeight.Light, FontStyle.Italic),
    Font(R.font.gilroy_regular, FontWeight.Normal),
    Font(R.font.gilroy_regular_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.gilroy_medium, FontWeight.Medium),
    Font(R.font.gilroy_medium_italic, FontWeight.Medium, FontStyle.Italic),
    Font(R.font.gilroy_semibold, FontWeight.SemiBold),
    Font(R.font.gilroy_semibold_italic, FontWeight.SemiBold, FontStyle.Italic),
    Font(R.font.gilroy_bold, FontWeight.Bold),
    Font(R.font.gilroy_bold_italic, FontWeight.Bold, FontStyle.Italic),
    Font(R.font.gilroy_extrabold, FontWeight.ExtraBold),
    Font(R.font.gilroy_extrabold_italic, FontWeight.ExtraBold, FontStyle.Italic),
    Font(R.font.gilroy_heavy, FontWeight.Black),
    Font(R.font.gilroy_heavy_italic, FontWeight.Black, FontStyle.Italic),
    Font(R.font.gilroy_black, FontWeight.Black),
    Font(R.font.gilroy_black_italic, FontWeight.Black, FontStyle.Italic)
)

// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
    /* Other default text styles to override
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    */
)