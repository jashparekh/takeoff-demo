package com.jashparekh.takeoff

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ---------- Colours (from the mocks) ----------
val Orange = Color(0xFFC2410C)
val Bg = Color(0xFFF3F4F6)
val White = Color(0xFFFFFFFF)
val Border = Color(0xFFE5E7EB)
val Ink = Color(0xFF111827)
val Ink2 = Color(0xFF374151)
val Muted = Color(0xFF4B5563)
val Gray500 = Color(0xFF6B7280)
val Gray400 = Color(0xFF9CA3AF)
val Gray300 = Color(0xFFD1D5DB)
val Gray200 = Color(0xFFE5E7EB)
val Gray50 = Color(0xFFF9FAFB)
val Divider = Color(0xFFF3F4F6)
val GreenBg = Color(0xFFDCFCE7)
val GreenFg = Color(0xFF166534)
val AmberBg = Color(0xFFFEF3C7)
val AmberFg = Color(0xFF92400E)
val OrangeBg = Color(0xFFFFEDD5)
val OrangeFg = Color(0xFF9A3412)
val OrangeTint = Color(0xFFFFF7ED)
val BlueBg = Color(0xFFDBEAFE)
val BlueFg = Color(0xFF1E40AF)
val Link = Color(0xFF1D4ED8)
val Covered = Color(0xFFBBF7D0)
val CoveredFg = Color(0xFF14532D)
val WarnIcon = Color(0xFFB45309)
val CheckGreen = Color(0xFF15803D)

val Mono = FontFamily.Monospace

/** Shows a short snackbar message. */
val LocalToast = staticCompositionLocalOf<(String) -> Unit> { {} }

// ---------- Icons (stroke paths copied from the mocks) ----------
private fun strokeIcon(vararg paths: String, width: Float = 2f): ImageVector {
    val b = ImageVector.Builder(
        defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = 24f, viewportHeight = 24f
    )
    for (p in paths) {
        b.addPath(
            pathData = addPathNodes(p),
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = width,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        )
    }
    return b.build()
}

object Ic {
    val Back by lazy { strokeIcon("M15 18l-6-6 6-6") }
    val Chevron by lazy { strokeIcon("M9 6l6 6-6 6") }
    val Edit by lazy { strokeIcon("M4 20h4L19 9l-4-4L4 16z") }
    val Check by lazy { strokeIcon("M5 12l5 5 9-10", width = 2.4f) }
    val Plus by lazy { strokeIcon("M12 5v14M5 12h14", width = 2.2f) }
    val Info by lazy { strokeIcon("M3 12a9 9 0 1 0 18 0a9 9 0 1 0 -18 0", "M12 11v5M12 8v.5") }
    val File by lazy { strokeIcon("M14 3H7a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V8z", "M14 3v5h5") }
    val Upload by lazy { strokeIcon("M12 16V4M7 9l5-5 5 5", "M4 20h16") }
    val Warning by lazy { strokeIcon("M12 3l10 18H2z", "M12 10v5M12 18v.5") }
    val Fit by lazy { strokeIcon("M4 9V4h5M20 9V4h-5M4 15v5h5M20 15v5h-5") }
    val Rotate by lazy { strokeIcon("M20 12a8 8 0 1 1-2.3-5.7", "M20 4v5h-5") }
    val Share by lazy {
        strokeIcon(
            "M15 5a3 3 0 1 0 6 0a3 3 0 1 0 -6 0",
            "M3 12a3 3 0 1 0 6 0a3 3 0 1 0 -6 0",
            "M15 19a3 3 0 1 0 6 0a3 3 0 1 0 -6 0",
            "M8.6 13.5l6.8 4M15.4 6.5l-6.8 4"
        )
    }
    val Chat by lazy { strokeIcon("M4 4h16v12H8l-4 4z") }
    val Building by lazy { strokeIcon("M4 20V8l8-4 8 4v12", "M4 12h16M9 20v-8M15 20v-8") }
    val Layers by lazy { strokeIcon("M12 3l9 5-9 5-9-5z", "M3 13l9 5 9-5") }
    val Circle by lazy { strokeIcon("M4 12a8 8 0 1 0 16 0a8 8 0 1 0 -16 0") }
}

@Composable
fun Ico(icon: ImageVector, tint: Color = Ink, size: Dp = 20.dp, modifier: Modifier = Modifier) {
    Icon(icon, contentDescription = null, tint = tint, modifier = modifier.size(size))
}

// ---------- Text ----------
@Composable
fun T(
    text: String,
    size: Int = 14,
    weight: FontWeight = FontWeight.Normal,
    color: Color = Ink,
    mono: Boolean = false,
    modifier: Modifier = Modifier,
    align: TextAlign = TextAlign.Start,
    maxLines: Int = Int.MAX_VALUE,
    strike: Boolean = false,
    lineHeight: Float = 1.3f
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = size.sp,
        fontWeight = weight,
        fontFamily = if (mono) Mono else FontFamily.Default,
        textAlign = align,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        textDecoration = if (strike) TextDecoration.LineThrough else null,
        lineHeight = (size * lineHeight).sp
    )
}

@Composable
fun TA(text: AnnotatedString, size: Int = 14, color: Color = Ink, modifier: Modifier = Modifier) {
    Text(text = text, modifier = modifier, color = color, fontSize = size.sp, lineHeight = (size * 1.35f).sp)
}

val SemiBold = FontWeight.SemiBold
val Medium = FontWeight.Medium
val Bold = FontWeight.Bold

// ---------- Surfaces ----------
fun Modifier.card(
    radius: Dp = 14.dp,
    border: Color = Border,
    borderWidth: Dp = 1.dp,
    bg: Color = White
): Modifier {
    val shape = RoundedCornerShape(radius)
    return this.clip(shape).background(bg).border(borderWidth, border, shape)
}

fun Modifier.dashedBorder(color: Color = Gray400, radius: Dp = 14.dp, width: Dp = 1.dp): Modifier =
    this.drawBehind {
        val w = width.toPx()
        drawRoundRect(
            color = color,
            topLeft = androidx.compose.ui.geometry.Offset(w / 2, w / 2),
            size = androidx.compose.ui.geometry.Size(size.width - w, size.height - w),
            cornerRadius = CornerRadius(radius.toPx()),
            style = Stroke(width = w, pathEffect = PathEffect.dashPathEffect(floatArrayOf(4 * w + 4f, 3 * w + 3f)))
        )
    }

@Composable
fun Card(
    modifier: Modifier = Modifier,
    border: Color = Border,
    borderWidth: Dp = 1.dp,
    radius: Dp = 14.dp,
    padH: Dp = 14.dp,
    padV: Dp = 12.dp,
    gap: Dp = 10.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier.fillMaxWidth().card(radius, border, borderWidth).padding(horizontal = padH, vertical = padV),
        verticalArrangement = Arrangement.spacedBy(gap),
        content = content
    )
}

@Composable
fun HDivider() {
    Box(Modifier.fillMaxWidth().height(1.dp).background(Divider))
}

@Composable
fun Chip(text: String, fg: Color, bg: Color, size: Int = 12, radius: Dp = 6.dp, modifier: Modifier = Modifier) {
    Box(modifier.clip(RoundedCornerShape(radius)).background(bg).padding(horizontal = 8.dp, vertical = 4.dp)) {
        T(text, size, SemiBold, fg, maxLines = 1)
    }
}

// ---------- Buttons ----------
@Composable
fun PrimaryButton(
    text: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    enabled: Boolean = true,
    icon: ImageVector? = null,
    height: Dp = 52.dp,
    fontSize: Int = 16,
    onClick: () -> Unit
) {
    Row(
        modifier
            .height(height)
            .clip(RoundedCornerShape(12.dp))
            .background(if (enabled) Orange else Gray200)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Ico(icon, if (enabled) White else Muted, 20.dp)
            Spacer(Modifier.width(8.dp))
        }
        T(text, fontSize, SemiBold, if (enabled) White else Muted, maxLines = 1, align = TextAlign.Center)
    }
}

@Composable
fun SecondaryButton(
    text: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    icon: ImageVector? = null,
    height: Dp = 52.dp,
    fontSize: Int = 15,
    onClick: () -> Unit
) {
    Row(
        modifier
            .height(height)
            .clip(RoundedCornerShape(12.dp))
            .background(White)
            .border(1.dp, Gray300, RoundedCornerShape(12.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Ico(icon, Ink, 20.dp)
            Spacer(Modifier.width(8.dp))
        }
        T(text, fontSize, SemiBold, Ink, maxLines = 1, align = TextAlign.Center)
    }
}

/** Pill / toggle button: dark when selected. */
@Composable
fun Pill(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    height: Dp = 44.dp,
    radius: Dp = 22.dp,
    fontSize: Int = 14,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(radius)
    Box(
        modifier
            .height(height)
            .clip(shape)
            .background(if (selected) Ink else White)
            .border(1.dp, if (selected) Ink else Gray300, shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        T(text, fontSize, if (selected) SemiBold else Medium, if (selected) White else Ink2, maxLines = 1)
    }
}

/** Grey segmented control (Bottom bars / Top bars / Concrete). */
@Composable
fun Segmented(options: List<String>, selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Gray200).padding(3.dp)
    ) {
        options.forEachIndexed { i, label ->
            val sel = i == selected
            Box(
                Modifier
                    .weight(1f)
                    .height(42.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (sel) White else Color.Transparent)
                    .clickable(role = Role.Tab) { onSelect(i) },
                contentAlignment = Alignment.Center
            ) {
                T(label, 14, if (sel) SemiBold else Medium, if (sel) Ink else Ink2, maxLines = 1)
            }
        }
    }
}

@Composable
fun CheckBox(checked: Boolean, size: Dp = 22.dp) {
    val shape = RoundedCornerShape(4.dp)
    Box(
        Modifier
            .size(size)
            .clip(shape)
            .background(if (checked) Orange else White)
            .border(2.dp, if (checked) Orange else Gray400, shape),
        contentAlignment = Alignment.Center
    ) {
        if (checked) Ico(Ic.Check, White, size - 6.dp)
    }
}

@Composable
fun CheckRow(text: String, checked: Boolean, size: Int = 14, color: Color = Ink, onToggle: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(role = Role.Checkbox, onClick = onToggle)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CheckBox(checked)
        T(text, size, color = color)
    }
}

// ---------- Inputs ----------
@Composable
fun Field(
    value: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    mono: Boolean = false,
    number: Boolean = false,
    label: String? = null
) {
    BasicTextField(
        value = value,
        onValueChange = onChange,
        singleLine = true,
        textStyle = TextStyle(
            fontSize = 15.sp,
            fontFamily = if (mono) Mono else FontFamily.Default,
            fontWeight = if (mono) FontWeight.Medium else FontWeight.Normal,
            color = Ink
        ),
        keyboardOptions = KeyboardOptions(keyboardType = if (number) KeyboardType.Number else KeyboardType.Text),
        cursorBrush = SolidColor(Orange),
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(White)
            .border(1.dp, Gray300, RoundedCornerShape(8.dp))
            .semantics { if (label != null) contentDescription = label },
        decorationBox = { inner ->
            Box(Modifier.fillMaxSize().padding(horizontal = 12.dp), contentAlignment = Alignment.CenterStart) {
                if (value.isEmpty()) T(placeholder, 15, color = Gray400, mono = mono)
                inner()
            }
        }
    )
}

// ---------- Screen scaffold ----------
@Composable
fun Header(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)?,
    titleSize: Int = 17,
    trailing: @Composable RowScope.() -> Unit = {}
) {
    Row(
        Modifier.fillMaxWidth().padding(start = 6.dp, end = 8.dp, top = 10.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            Box(
                Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .clickable(role = Role.Button, onClick = onBack)
                    .semantics { contentDescription = "Back" },
                contentAlignment = Alignment.Center
            ) { Ico(Ic.Back, Ink, 22.dp) }
        } else {
            Spacer(Modifier.width(14.dp))
        }
        Column(Modifier.weight(1f).padding(start = 4.dp)) {
            T(title, titleSize, SemiBold, maxLines = 2)
            if (subtitle != null) T(subtitle, 12, color = Muted, maxLines = 2)
        }
        trailing()
    }
}

@Composable
fun Screen(
    header: @Composable () -> Unit,
    bottom: (@Composable ColumnScope.() -> Unit)? = null,
    scroll: Boolean = true,
    gap: Dp = 12.dp,
    hPad: Dp = 20.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(Modifier.fillMaxSize().background(Bg)) {
        header()
        val base = Modifier.weight(1f).fillMaxWidth()
        Column(
            (if (scroll) base.verticalScroll(rememberScrollState()) else base)
                .padding(start = hPad, end = hPad, top = 4.dp, bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(gap),
            content = content
        )
        if (bottom != null) {
            Column(
                Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                content = bottom
            )
        }
    }
}

@Composable
fun SectionLabel(left: String, right: String? = null) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
        T(left, 13, SemiBold, Ink2, modifier = Modifier.weight(1f))
        if (right != null) T(right, 12, color = Muted)
    }
}

@Composable
fun NoteRow(icon: ImageVector, text: String, tint: Color = Muted) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 2.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Ico(icon, tint, 18.dp)
        T(text, 12, color = Muted)
    }
}

@Composable
fun KeyVal(label: String, value: String, labelColor: Color = Muted, valueSize: Int = 16, labelSize: Int = 12, valueColor: Color = Ink, sub: String? = null, subColor: Color = Muted) {
    Column {
        T(label, labelSize, color = labelColor)
        T(value, valueSize, Medium, valueColor, mono = true, maxLines = 1)
        if (sub != null) T(sub, 12, color = subColor)
    }
}
