package theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

/** Dynamic color palette supporting both Light Luxury and Dark Luxury */
object IvyColors {
    // Current state values
    var isDark by mutableStateOf(false)
        private set

    var Background by mutableStateOf(Color(0xFFFBFBF9))
    var Surface by mutableStateOf(Color(0xFFFFFFFF))
    var SurfaceVariant by mutableStateOf(Color(0xFFF2F2F0))
    var SurfaceElevated by mutableStateOf(Color(0xFFFFFFFF))
    var Primary by mutableStateOf(Color(0xFFB89972))
    var PrimaryDark by mutableStateOf(Color(0xFF9A7B56))
    var PrimaryLight by mutableStateOf(Color(0xFFD4C4B7))
    var OnPrimary by mutableStateOf(Color(0xFFFFFFFF))
    var Secondary by mutableStateOf(Color(0xFF1C1C1C))
    var SecondaryLight by mutableStateOf(Color(0xFF333333))
    var OnSecondary by mutableStateOf(Color(0xFFFFFFFF))
    var Accent by mutableStateOf(Color(0xFFE5DED5))
    var AccentDark by mutableStateOf(Color(0xFFC7BCAD))
    var TextPrimary by mutableStateOf(Color(0xFF1A1A1A))
    var TextSecondary by mutableStateOf(Color(0xFF757575))
    var TextMuted by mutableStateOf(Color(0xFFA3A3A3))
    var Error by mutableStateOf(Color(0xFFD32F2F))
    var Success by mutableStateOf(Color(0xFF388E3C))
    var Warning by mutableStateOf(Color(0xFFF57C00))
    var Border by mutableStateOf(Color(0xFFEAEAEA))
    var BorderLight by mutableStateOf(Color(0xFFF5F5F5))
    var Heart by mutableStateOf(Color(0xFFB89972))
    var HeartOutline by mutableStateOf(Color(0xFFA3A3A3))
    var TagBg by mutableStateOf(Color(0xFFF5F5F5))
    var TagVerified by mutableStateOf(Color(0xFFE8F5E9))
    var TagLive by mutableStateOf(Color(0xFFE8F5E9))
    var TagNotLive by mutableStateOf(Color(0xFFFFEBEE))

    fun setDarkMode(dark: Boolean) {
        isDark = dark
        if (dark) {
            Background = Color(0xFF121212)
            Surface = Color(0xFF1E1E1E)
            SurfaceVariant = Color(0xFF2C2C2C)
            SurfaceElevated = Color(0xFF333333)
            Primary = Color(0xFFC7B090) // Slightly lighter gold for dark mode
            PrimaryDark = Color(0xFF9A7B56)
            PrimaryLight = Color(0xFFDCD2C6)
            OnPrimary = Color(0xFF121212)
            Secondary = Color(0xFFEAEAEA) // Light grey text/icons
            SecondaryLight = Color(0xFFCCCCCC)
            OnSecondary = Color(0xFF121212)
            Accent = Color(0xFF3E3E3E)
            AccentDark = Color(0xFF2C2C2C)
            TextPrimary = Color(0xFFF5F5F5)
            TextSecondary = Color(0xFFAAAAAA)
            TextMuted = Color(0xFF777777)
            Error = Color(0xFFCF6679)
            Success = Color(0xFF81C784)
            Warning = Color(0xFFFFB74D)
            Border = Color(0xFF333333)
            BorderLight = Color(0xFF222222)
            Heart = Color(0xFFC7B090)
            HeartOutline = Color(0xFF777777)
            TagBg = Color(0xFF2C2C2C)
            TagVerified = Color(0xFF1B3B22)
            TagLive = Color(0xFF1B3B22)
            TagNotLive = Color(0xFF4A1F24)
        } else {
            Background = Color(0xFFFBFBF9)
            Surface = Color(0xFFFFFFFF)
            SurfaceVariant = Color(0xFFF2F2F0)
            SurfaceElevated = Color(0xFFFFFFFF)
            Primary = Color(0xFFB89972)
            PrimaryDark = Color(0xFF9A7B56)
            PrimaryLight = Color(0xFFD4C4B7)
            OnPrimary = Color(0xFFFFFFFF)
            Secondary = Color(0xFF1C1C1C)
            SecondaryLight = Color(0xFF333333)
            OnSecondary = Color(0xFFFFFFFF)
            Accent = Color(0xFFE5DED5)
            AccentDark = Color(0xFFC7BCAD)
            TextPrimary = Color(0xFF1A1A1A)
            TextSecondary = Color(0xFF757575)
            TextMuted = Color(0xFFA3A3A3)
            Error = Color(0xFFD32F2F)
            Success = Color(0xFF388E3C)
            Warning = Color(0xFFF57C00)
            Border = Color(0xFFEAEAEA)
            BorderLight = Color(0xFFF5F5F5)
            Heart = Color(0xFFB89972)
            HeartOutline = Color(0xFFA3A3A3)
            TagBg = Color(0xFFF5F5F5)
            TagVerified = Color(0xFFE8F5E9)
            TagLive = Color(0xFFE8F5E9)
            TagNotLive = Color(0xFFFFEBEE)
        }
    }
}

object IvyDimens {
    val CornerRadius = 8
    val CardCorner = 12
    val ButtonCorner = 6
    val PaddingSmall = 8
    val PaddingMedium = 16
    val PaddingLarge = 24
    val PaddingXLarge = 32
}
