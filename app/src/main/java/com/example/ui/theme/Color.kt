package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Glassmorphism Primary Palette
val NavyDeep = Color(0xFF070B14)
val NavyDark = Color(0xFF0B132B)
val SlateDark = Color(0xFF1C2541)
val SlateCard = Color(0xFF1E293B)

val CyanAccent = Color(0xFF38BDF8)
val BlueElectric = Color(0xFF3B82F6)
val IndigoVibrant = Color(0xFF6366F1)
val PurpleGlow = Color(0xFF8B5CF6)
val EmeraldAccent = Color(0xFF10B981)
val AmberAccent = Color(0xFFF59E0B)
val RoseAccent = Color(0xFFF43F5E)

// Text Colors
val TextPrimaryDark = Color(0xFFF8FAFC)
val TextSecondaryDark = Color(0xFF94A3B8)
val TextMutedDark = Color(0xFF64748B)

val TextPrimaryLight = Color(0xFF0F172A)
val TextSecondaryLight = Color(0xFF475569)

// Glass Translucency Tokens
val GlassBgDark = Color(0xB30F172A)      // 70% opacity deep slate
val GlassSurfaceDark = Color(0x661E293B) // 40% opacity
val GlassBorderDark = Color(0x33FFFFFF)  // 20% white glow border
val GlassHighlightDark = Color(0x1A38BDF8)

val GlassBgLight = Color(0xE6FFFFFF)     // 90% white
val GlassSurfaceLight = Color(0x99F1F5F9)
val GlassBorderLight = Color(0x3394A3B8)

// Gradients
val GlassMeshGradient = Brush.radialGradient(
    colors = listOf(
        Color(0x333B82F6),
        Color(0x1A8B5CF6),
        Color(0x00000000)
    )
)

val GlassBorderGradient = Brush.linearGradient(
    colors = listOf(
        Color(0x8038BDF8),
        Color(0x338B5CF6),
        Color(0x20FFFFFF)
    )
)

val HeroCardGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xCC0F172A),
        Color(0x991E293B)
    )
)
