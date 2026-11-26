package com.github.avbox.osc.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.drawable.ColorDrawable
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RippleConfiguration
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DelegatingNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.materialkolor.PaletteStyle
import kotlinx.coroutines.launch

internal fun ComponentActivity.enableTransparentEdgeToEdge() {
    enableEdgeToEdge(
        statusBarStyle = SystemBarStyle.auto(
            android.graphics.Color.TRANSPARENT,
            android.graphics.Color.TRANSPARENT,
        ),
        navigationBarStyle = SystemBarStyle.light(
            android.graphics.Color.TRANSPARENT,
            android.graphics.Color.TRANSPARENT,
        ),
    )
    window.setBackgroundDrawable(ColorDrawable(AppThemeState.windowSurfaceArgb(this)))
}

@Composable
fun AVBoxTheme(
    config: ThemeConfig = AppThemeState.config,
    manageStatusBarIcons: Boolean = true,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (config.mode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        else -> isSystemInDarkTheme()
    }

    val context = LocalContext.current
    val baseScheme = when {
        config.source == ThemeSource.CUSTOM -> remember(config.seedArgb, darkTheme, config.style) {
            AppThemeState.customScheme(config.seedArgb, darkTheme, config.style)
        }

        else -> AppThemeState.resolveScheme(context, config, darkTheme)
    }
    val colorScheme = if (darkTheme && config.pureBlack) baseScheme.toPureBlack() else baseScheme

    if (manageStatusBarIcons) {
        ApplyAppThemeBars(isDark = darkTheme)
    }

    val focusColor = colorScheme.primary
    val baseRipple = ripple()
    val tvIndication = remember(baseRipple, focusColor) {
        TvFocusIndication(baseRipple, focusColor)
    }

    CompositionLocalProvider(
        LocalRippleConfiguration provides rememberRippleConfiguration(),
        LocalIndication provides tvIndication,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AVBoxTypography,
            content = content,
        )
    }
}

private class TvFocusIndication(
    private val delegate: IndicationNodeFactory,
    private val focusColor: Color,
) : IndicationNodeFactory {

    override fun create(interactionSource: InteractionSource): DelegatableNode =
        TvFocusIndicationNode(interactionSource, delegate.create(interactionSource), focusColor)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TvFocusIndication) return false
        return delegate == other.delegate && focusColor == other.focusColor
    }

    override fun hashCode(): Int = 31 * delegate.hashCode() + focusColor.hashCode()
}

private class TvFocusIndicationNode(
    private val interactionSource: InteractionSource,
    rippleNode: DelegatableNode,
    private val focusColor: Color,
) : DelegatingNode(), DrawModifierNode {

    private var isFocused = false

    init {
        delegate(rippleNode)
    }

    override fun onAttach() {
        coroutineScope.launch {
            var focusCount = 0
            interactionSource.interactions.collect { interaction ->
                when (interaction) {
                    is FocusInteraction.Focus -> focusCount++
                    is FocusInteraction.Unfocus -> focusCount--
                }
                val focused = focusCount > 0
                if (isFocused != focused) {
                    isFocused = focused
                    invalidateDraw()
                }
            }
        }
    }

    override fun ContentDrawScope.draw() {
        drawContent()
        if (isFocused) {
            val stroke = 2.dp.toPx()
            val radius = 8.dp.toPx()
            drawRoundRect(
                color = focusColor,
                cornerRadius = CornerRadius(radius, radius),
                style = Stroke(width = stroke),
            )
        }
    }
}

@Suppress("DEPRECATION")
@Composable
private fun rememberRippleConfiguration(): RippleConfiguration = remember {
    RippleConfiguration(
        rippleAlpha = RippleAlpha(
            hoveredAlpha = 2f * 0.08f,
            focusedAlpha = 2f * 0.10f,
            pressedAlpha = 2f * 0.10f,
            draggedAlpha = 2f * 0.16f,
        ),
    )
}

@Composable
private fun ApplyAppThemeBars(isDark: Boolean) {
    val view = LocalView.current
    if (view.isInEditMode) return
    SideEffect {
        val activity = view.context.findActivity() ?: return@SideEffect
        @Suppress("DEPRECATION")
        activity.window.statusBarColor = android.graphics.Color.TRANSPARENT
        @Suppress("DEPRECATION")
        activity.window.navigationBarColor = android.graphics.Color.TRANSPARENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            activity.window.isNavigationBarContrastEnforced = false
            activity.window.isStatusBarContrastEnforced = false
        }
        WindowCompat.getInsetsController(activity.window, view).apply {
            isAppearanceLightStatusBars = !isDark
            isAppearanceLightNavigationBars = !isDark
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Preview(name = "浅色", showBackground = true) // i18n: keep(IDE 预览名,不进应用)
@Composable
private fun AVBoxThemeLightPreview() {
    AVBoxTheme(
        config = ThemeConfig(ThemeSource.CUSTOM, ThemeMode.LIGHT, DefaultSeedArgb, PaletteStyle.TonalSpot, pureBlack = false),
    ) {
        Surface(color = MaterialTheme.colorScheme.surfaceContainer) {
            Text("AVBox")
        }
    }
}

@Preview(name = "深色", showBackground = true) // i18n: keep(IDE 预览名,不进应用)
@Composable
private fun AVBoxThemeDarkPreview() {
    AVBoxTheme(
        config = ThemeConfig(ThemeSource.CUSTOM, ThemeMode.DARK, DefaultSeedArgb, PaletteStyle.TonalSpot, pureBlack = false),
    ) {
        Surface(color = MaterialTheme.colorScheme.surfaceContainer) {
            Text("AVBox")
        }
    }
}
