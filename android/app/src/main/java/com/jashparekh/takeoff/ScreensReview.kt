package com.jashparekh.takeoff

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

// ===================== 3 · Reading drawings =====================
private data class ReadStep(val doneTitle: String, val busyLabel: String, val sub: String, val until: Float)

private val readSteps = listOf(
    ReadStep("Scale calibrated", "Calibrating scale", "From 230×500 columns · checked on C11 = 600 mm", 0.15f),
    ReadStep("Schedules read", "Reading schedules", "17 beam rows · 4 slab marks · general notes", 0.35f),
    ReadStep("Measuring beams and slab panels", "Measuring spans", "Matching beam marks to the plan", 0.85f),
    ReadStep("Check for gaps and conflicts", "Checking for gaps", "1 beam not in schedule · 2 values missing", 1.0f)
)

@Composable
fun ReadingScreen(nav: NavHostController) {
    val progress = remember { Animatable(0f) }
    var left by remember { mutableStateOf(false) }
    val goReview = {
        if (!left) {
            left = true
            nav.navigate(R0.REVIEW) { popUpTo(R0.READING) { inclusive = true } }
        }
    }
    LaunchedEffect(Unit) {
        progress.animateTo(1f, tween(durationMillis = 5200, easing = LinearEasing))
        delay(700)
        goReview()
    }
    val p = progress.value
    val current = readSteps.indexOfFirst { p < it.until }.let { if (it == -1) readSteps.size else it }
    val label = if (current < readSteps.size) readSteps[current].busyLabel else "Done"
    val beamsRead = (77 * ((p - 0.35f) / 0.5f).coerceIn(0f, 1f)).roundToInt()

    Screen(
        header = { Header("Reading drawings", null, onBack = { nav.back() }, titleSize = 18) },
        gap = 16.dp,
        bottom = {
            T("About a minute. We'll notify you when it's ready.", 12, color = Muted, modifier = Modifier.fillMaxWidth(), align = androidx.compose.ui.text.style.TextAlign.Center)
            if (p >= 1f) PrimaryButton("Review members") { goReview() }
            else SecondaryButton("Review as it reads", fontSize = 16) { goReview() }
        }
    ) {
        Column(Modifier.fillMaxWidth().card()) {
            Box(Modifier.fillMaxWidth().height(220.dp).background(Color(0xFFFAFAFA))) {
                Canvas(Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 18.dp)) {
                    val w = size.width
                    val h = size.height
                    drawRect(Gray400, Offset.Zero, Size(w, h), style = Stroke(1.5f))
                    val beamW = 9f
                    // beams in reading order: (x0,y0,x1,y1) as fractions
                    val segs = listOf(
                        floatArrayOf(0.21f, 0f, 0.21f, 1f), floatArrayOf(0.42f, 0f, 0.42f, 1f),
                        floatArrayOf(0.58f, 0f, 0.58f, 1f), floatArrayOf(0.79f, 0f, 0.79f, 1f),
                        floatArrayOf(0f, 0.22f, 0.42f, 0.22f), floatArrayOf(0.58f, 0.22f, 1f, 0.22f),
                        floatArrayOf(0f, 0.78f, 0.42f, 0.78f), floatArrayOf(0.58f, 0.78f, 1f, 0.78f),
                        floatArrayOf(0f, 0.5f, 0.21f, 0.5f), floatArrayOf(0.42f, 0.5f, 0.58f, 0.5f),
                        floatArrayOf(0.79f, 0.5f, 1f, 0.5f)
                    )
                    val done = (segs.size * ((p - 0.35f) / 0.5f).coerceIn(0f, 1f)).roundToInt()
                    segs.forEachIndexed { i, s ->
                        val secondary = s[1] == 0.5f && s[3] == 0.5f
                        val col = if (i < done) Color(0xFFD9622B) else Gray400
                        drawLine(col, Offset(s[0] * w, s[1] * h), Offset(s[2] * w, s[3] * h), if (secondary) 5f else beamW)
                    }
                    // lift void
                    val lx = 0.44f * w; val ly = 0.25f * h; val lw = 0.12f * w; val lh = 0.22f * h
                    drawRect(Gray500, Offset(lx, ly), Size(lw, lh), style = Stroke(1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 5f))))
                    drawLine(Gray500, Offset(lx, ly), Offset(lx + lw, ly + lh), 1.5f)
                    drawLine(Gray500, Offset(lx + lw, ly), Offset(lx, ly + lh), 1.5f)
                    // columns
                    val cs = 14f
                    listOf(0f, 0.42f, 0.58f, 1f).forEach { fx ->
                        drawRect(Gray500, Offset(fx * w - cs / 2, -cs / 2), Size(cs, cs))
                    }
                    drawRect(Gray500, Offset(w - cs / 2, h - cs / 2), Size(cs, cs))
                }
                Box(
                    Modifier.align(Alignment.BottomStart).padding(start = 12.dp, bottom = 10.dp)
                        .clip(RoundedCornerShape(6.dp)).background(Ink).padding(horizontal = 8.dp, vertical = 4.dp)
                ) { T("ND 41-06 · $beamsRead of 77 beams", 12, Medium, White, mono = true) }
            }
            Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row {
                    T(label, 14, SemiBold, modifier = Modifier.weight(1f))
                    T("${(p * 100).roundToInt()}%", 14, color = Muted, mono = true)
                }
                Box(Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(Gray200)) {
                    Box(Modifier.fillMaxWidth(p.coerceIn(0f, 1f)).fillMaxHeight().background(Orange))
                }
            }
        }
        Column(Modifier.fillMaxWidth().card().padding(horizontal = 16.dp, vertical = 6.dp)) {
            readSteps.forEachIndexed { i, st ->
                val state = when {
                    i < current -> 2
                    i == current -> 1
                    else -> 0
                }
                Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.size(22.dp), contentAlignment = Alignment.Center) {
                        when (state) {
                            2 -> Ico(Ic.Check, CheckGreen, 22.dp)
                            1 -> CircularProgressIndicator(Modifier.size(18.dp), color = Orange, strokeWidth = 2.5.dp)
                            else -> Ico(Ic.Circle, Gray400, 22.dp)
                        }
                    }
                    Column(Modifier.weight(1f)) {
                        T(st.doneTitle, 14, Medium, if (state == 0) Muted else Ink)
                        if (state != 0 && (i < 3 || state == 2)) T(st.sub, 12, color = Muted)
                    }
                }
                if (i < readSteps.lastIndex) HDivider()
            }
        }
    }
}

// ===================== 4 · Review members =====================
private data class Member(val marks: String, val weight: String, val size: String, val bars: String, val warn: String? = null)

private val beamsList = listOf(
    Member("B26", "58.9 kg", "230×600 · span 3,401 mm · ×1", "Bot 2-16T + 2-10T · Top 2-16T + 2-16T extra · 8T @100/150", "Not in the beam schedule. Using row B48, B51, B52."),
    Member("B1, B3, B4 +5", "21.7 kg ea", "230×600 · span 2,641 mm · ×8", "Bot 2-12T · Top 2-10T · 8T @150/200"),
    Member("B7, B10, B28, B31", "70.9 kg ea", "230×600 · span 5,072 mm (2 bays) · ×4", "Bot 2-16T + 2-10T · Top 2-12T + extras · 8T @100/150"),
    Member("B12, B13, B25, B27", "79.5 kg ea", "230×600 · span 4,802 mm (2 bays) · ×4", "Bot 2-16T + 2-12T · Top 2-16T + 2-16T end · 8T @100/150"),
    Member("B16, B22", "68.9 kg ea", "230×600 · span 5,602 mm · ×2", "Bot 3-12T · Top 3-12T · 8T @100/150")
)

private val chajjaList = listOf(
    Member("CH1, CH2", "9.8 kg ea", "600 projection · 2,641 mm long · ×8", "Main 8T @150 · Dist 8T @200"),
    Member("CH3", "14.2 kg ea", "600 projection · 3,800 mm long · ×4", "Main 8T @150 · Dist 8T @200"),
    Member("CH4, CH5", "7.1 kg ea", "450 projection · 1,800 mm long · ×8", "Main 8T @150 · Dist 8T @200"),
    Member("CH6", "11.6 kg ea", "750 projection · 2,400 mm long · ×4", "Main 8T @125 · Dist 8T @200"),
    Member("CH7", "5.4 kg ea", "450 projection · 1,350 mm long · ×4", "Main 8T @150 · Dist 8T @200")
)

@Composable
fun ReviewScreen(nav: NavHostController) {
    var tab by remember { mutableIntStateOf(0) }
    val slabGroups = remember {
        FloorPlan.panels.groupBy { it.room }.values.map { it.first() }
    }
    Column(Modifier.fillMaxSize().background(Bg)) {
        Header("Review members", null, onBack = { nav.back() }, titleSize = 18) {
            Row(
                Modifier
                    .padding(end = 8.dp)
                    .height(44.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(White)
                    .border(1.dp, Gray300, RoundedCornerShape(22.dp))
                    .clickable(role = Role.Button) { nav.navigate(R0.FLOOR3D) }
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Ico(Ic.Layers, Ink, 18.dp)
                T("3D", 14, SemiBold)
            }
        }
        Row(Modifier.padding(start = 20.dp, end = 20.dp, bottom = 10.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("Beams 77", "Slabs 9", "Chajjas 7").forEachIndexed { i, l ->
                Pill(l, tab == i, Modifier.weight(1f), height = 44.dp, radius = 10.dp) { tab = i }
            }
        }
        Row(Modifier.padding(start = 20.dp, end = 20.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Chip("1 needs a check", AmberFg, AmberBg, radius = 14.dp)
            T("Tap any member to open it", 12, color = Muted)
        }
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            when (tab) {
                0 -> beamsList.forEach { MemberCard(it) { nav.navigate(R0.SLAB) } }
                1 -> slabGroups.forEach { p ->
                    MemberCard(
                        Member(
                            "S${p.type} · ${p.room}", "${p.steelKg} kg ea",
                            "${p.thk} thk · ${p.concreteText} · ×${p.same}",
                            if (p.type == 2) "10T @150 both ways" else "8T @150 both ways"
                        )
                    ) { nav.navigate(R0.SLAB) }
                }
                else -> chajjaList.forEach { MemberCard(it) { nav.navigate(R0.SLAB) } }
            }
            Spacer(Modifier.height(4.dp))
        }
        Row(
            Modifier.fillMaxWidth().background(White).padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(Modifier.weight(1f)) {
                T("Beams so far", 12, color = Muted)
                T("2,717 kg · 21.7 m³", 17, Medium, mono = true)
            }
            PrimaryButton("Next", Modifier.width(120.dp), height = 48.dp, fontSize = 15) { nav.navigate(R0.ISSUES) }
        }
    }
}

@Composable
private fun MemberCard(m: Member, onClick: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .card(12.dp, if (m.warn != null) Color(0xFFD97706) else Border, if (m.warn != null) 2.dp else 1.dp)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            T(m.marks, 15, SemiBold, mono = true, modifier = Modifier.weight(1f))
            T(m.weight, 14, Medium, mono = true)
        }
        T(m.size, 13, color = Ink2)
        T(m.bars, 13, color = if (m.warn != null) Ink2 else Muted)
        if (m.warn != null) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Ico(Ic.Warning, WarnIcon, 16.dp)
                T(m.warn, 12, color = AmberFg)
            }
        }
    }
}

// ===================== 4a · 3D floor =====================
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Floor3DScreen(nav: NavHostController) {
    var mode by remember { mutableIntStateOf(1) }
    var yaw by remember { mutableFloatStateOf(0f) }
    var zoom by remember { mutableFloatStateOf(1f) }
    var slabs by remember { mutableStateOf(true) }
    var beams by remember { mutableStateOf(true) }
    var cols by remember { mutableStateOf(true) }
    var rebar by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<String?>("A0") }
    val modeNow by rememberUpdatedState(mode)
    val panel = FloorPlan.panels.firstOrNull { it.id == selected }

    Column(Modifier.fillMaxSize().background(Bg)) {
        Header("Typical floor · 3D", "Built from ND 41-06 · 28 slab panels · 77 beams", onBack = { nav.back() })
        Segmented(listOf("Plan", "3D", "Drawing"), mode, { mode = it }, Modifier.padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 8.dp))
        Box(
            Modifier
                .padding(horizontal = 12.dp)
                .fillMaxWidth()
                .weight(1f)
                .heightIn(max = 380.dp)
                .card(16.dp)
        ) {
            Canvas(
                Modifier
                    .fillMaxSize()
                    .semantics { contentDescription = "Floor model. Drag to rotate, pinch to zoom, tap a slab." }
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, z, _ ->
                            yaw = (yaw + pan.x * 0.35f) % 360f
                            zoom = (zoom * z).coerceIn(0.6f, 3f)
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures { pos ->
                            val pr = FloorPlan.proj(modeNow, yaw, zoom, size.width.toFloat(), size.height.toFloat())
                            val hit = FloorPlan.hit(modeNow, pr, pos)
                            if (hit != null) selected = hit
                        }
                    }
            ) {
                val pr = FloorPlan.proj(mode, yaw, zoom, size.width, size.height)
                with(FloorPlan) { drawFloor(mode, pr, slabs, beams, cols, rebar, selected) }
            }
            FlowRow(
                Modifier.padding(12.dp).width(300.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                LegendChip("S1 rooms", Color(0xFFDCE3EC), Gray400)
                LegendChip("S2 core", Color(0xFFA9C4E4), Color(0xFF7C9CC3))
                LegendChip("S3 sunk", Color(0xFFF4C9A6), Color(0xFFD89C6E))
            }
            Column(Modifier.align(Alignment.BottomEnd).padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                MapButton(Ic.Rotate, "Rotate view") { yaw = (yaw + 90f) % 360f }
                MapButton(Ic.Fit, "Fit to screen") { yaw = 0f; zoom = 1f }
            }
            T(
                "Drag to rotate · pinch to zoom · tap a slab", 12, color = Muted,
                modifier = Modifier.align(Alignment.BottomStart).padding(start = 12.dp, bottom = 12.dp)
            )
        }
        FlowRow(
            Modifier.padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Pill("Slabs", slabs, height = 44.dp, fontSize = 13) { slabs = !slabs }
            Pill("Beams", beams, height = 44.dp, fontSize = 13) { beams = !beams }
            Pill("Columns", cols, height = 44.dp, fontSize = 13) { cols = !cols }
            Pill("Rebar", rebar, height = 44.dp, fontSize = 13) { rebar = !rebar }
        }
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                .background(White)
                .border(1.dp, Border, RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                .padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(Modifier.align(Alignment.CenterHorizontally).size(40.dp, 4.dp).clip(RoundedCornerShape(2.dp)).background(Gray300))
            if (panel == null) {
                T("Tap a slab to see its size and steel", 15, color = Muted)
            } else {
                Column {
                    T(panel.title, 16, SemiBold)
                    T(panel.spec, 13, color = Muted)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        KeyVal("Steel", "${panel.steelKg} kg", valueSize = 15, labelSize = 12)
                        KeyVal("Concrete", panel.concreteText, valueSize = 15, labelSize = 12)
                        KeyVal("Same panels", "×${panel.same}", valueSize = 15, labelSize = 12)
                    }
                    PrimaryButton("Open slab", Modifier.width(120.dp), height = 48.dp, fontSize = 14) { nav.navigate(R0.SLAB) }
                }
            }
        }
    }
}

@Composable
private fun LegendChip(label: String, fill: Color, edge: Color) {
    Row(
        Modifier.clip(RoundedCornerShape(6.dp)).background(White).border(1.dp, Border, RoundedCornerShape(6.dp)).padding(horizontal = 7.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(fill).border(1.dp, edge, RoundedCornerShape(2.dp)))
        T(label, 12, color = Ink2)
    }
}

@Composable
private fun MapButton(icon: androidx.compose.ui.graphics.vector.ImageVector, desc: String, onClick: () -> Unit) {
    Box(
        Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(White)
            .border(1.dp, Border, RoundedCornerShape(10.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = desc },
        contentAlignment = Alignment.Center
    ) { Ico(icon, Ink, 20.dp) }
}

// ===================== 4b · Slab detail =====================
private class DepthItem(val depth: Float, val draw: () -> Unit)

@Composable
fun SlabDetailScreen(nav: NavHostController) {
    val toast = LocalToast.current
    var tab by remember { mutableIntStateOf(0) }
    val measurer = rememberTextMeasurer()
    val labelStyle = TextStyle(fontSize = 11.sp, fontFamily = Mono, fontWeight = FontWeight.Medium, color = Ink2)

    Screen(
        header = { Header("S1 · Living room, Flat A", "Between B8, B15/B16, B45, B48 · same in 4 flats", onBack = { nav.back() }) },
        hPad = 0.dp,
        bottom = {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SecondaryButton("Flag an issue", Modifier.weight(1f)) { nav.navigate(R0.ISSUES) }
                PrimaryButton("Looks right", Modifier.weight(1f), icon = Ic.Check, fontSize = 15) {
                    toast("Marked S1 as checked")
                    nav.back()
                }
            }
        }
    ) {
        Segmented(listOf("Bottom bars", "Top bars", "Concrete"), tab, { tab = it }, Modifier.padding(horizontal = 20.dp))
        Box(Modifier.padding(horizontal = 12.dp).fillMaxWidth().height(300.dp).card(16.dp)) {
            Canvas(Modifier.fillMaxSize().semantics { contentDescription = "3D view of slab S1 with its beams" }) {
                val W = 4.84f
                val H = 2.98f
                val pr = Proj(true, 0f, size.width / 7.6f, size.width / 2f, size.height * 0.40f, W / 2f, H / 2f)
                val items = ArrayList<DepthItem>()
                val cs = listOf(0f to 0f, W to 0f, 0f to H, W to H)
                for ((x, y) in cs) {
                    items += DepthItem(pr.depth(x, y) - 0.01f) {
                        box(pr, x - 0.2f, y - 0.2f, x + 0.2f, y + 0.2f, -2.4f, -0.6f, Color(0xFF4B5563), Color(0xFF9CA3AF), Color(0xFF6B7280), Color(0x664B5563))
                    }
                }
                val bw = 0.23f
                val beamBoxes = listOf(
                    floatArrayOf(-bw, -bw, W + bw, 0f),     // B45 (back, long)
                    floatArrayOf(W, 0f, W + bw, H),         // B8 (right, short)
                    floatArrayOf(-bw, H, W + bw, H + bw),   // B48 (front, long)
                    floatArrayOf(-bw, 0f, 0f, H)            // B15/B16 (left, short)
                )
                for (b in beamBoxes) {
                    items += DepthItem(pr.depth((b[0] + b[2]) / 2, (b[1] + b[3]) / 2)) {
                        box(pr, b[0], b[1], b[2], b[3], -0.6f, 0f, Color(0xFF6B7280), Color(0xFF64748B), Color(0xFF475569), Color(0x99334155))
                    }
                }
                items += DepthItem(pr.depth(W / 2, H / 2)) {
                    val top = if (tab == 2) Color(0xFFD1D5DB) else Color(0xFFFFF7ED)
                    box(pr, 0f, 0f, W, H, -0.125f, 0f, top, Color(0xFFF4C9A6), Color(0xFFE3B088), Color(0x66C2410C))
                    val bar = Color(0xFFC2410C)
                    if (tab == 0) {
                        var x = 0.075f
                        while (x < W) { drawLine(bar, pr.pt(x, 0.05f), pr.pt(x, H - 0.05f), 1.4f); x += 0.15f }
                        var y = 0.075f
                        while (y < H) { drawLine(bar, pr.pt(0.05f, y), pr.pt(W - 0.05f, y), 1.4f); y += 0.15f }
                    } else if (tab == 1) {
                        val strip = 0.75f
                        var x = 0.075f
                        while (x < W) {
                            drawLine(bar, pr.pt(x, 0.05f), pr.pt(x, strip), 1.6f)
                            drawLine(bar, pr.pt(x, H - strip), pr.pt(x, H - 0.05f), 1.6f)
                            x += 0.15f
                        }
                        var y = 0.075f
                        while (y < H) {
                            drawLine(bar, pr.pt(0.05f, y), pr.pt(strip, y), 1.6f)
                            drawLine(bar, pr.pt(W - strip, y), pr.pt(W - 0.05f, y), 1.6f)
                            y += 0.15f
                        }
                    }
                }
                items.sortedBy { it.depth }.forEach { it.draw() }

                fun label(text: String, at: Offset) {
                    val layout = measurer.measure(text, labelStyle)
                    val tl = Offset(at.x - layout.size.width / 2f, at.y - layout.size.height / 2f)
                    drawRoundRect(White, tl - Offset(5f, 3f), Size(layout.size.width + 10f, layout.size.height + 6f), androidx.compose.ui.geometry.CornerRadius(8f))
                    drawRoundRect(Border, tl - Offset(5f, 3f), Size(layout.size.width + 10f, layout.size.height + 6f), androidx.compose.ui.geometry.CornerRadius(8f), style = Stroke(2f))
                    drawText(layout, topLeft = tl)
                }
                label("B45", pr.pt(W * 0.3f, -0.6f, 0.2f))
                label("B8", pr.pt(W + 0.9f, H * 0.2f, 0f))
                label("B48", pr.pt(W * 0.75f, H + 0.9f, -0.3f))
                label("B15/B16", pr.pt(-0.9f, H * 0.85f, -0.8f))
            }
            T("Bars drawn at true spacing", 12, color = Muted, modifier = Modifier.align(Alignment.BottomStart).padding(start = 12.dp, bottom = 10.dp))
        }
        Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Column(Modifier.fillMaxWidth().card(12.dp).padding(horizontal = 14.dp, vertical = 4.dp)) {
                val rows = when (tab) {
                    0 -> listOf(
                        Triple("Short span bars", "8T @ 150 c/c · 31 nos × 2.98 m", "36.5 kg"),
                        Triple("Long span bars", "8T @ 150 c/c · 18 nos × 4.84 m", "34.4 kg")
                    )
                    1 -> listOf(
                        Triple("Extra top bars at supports", "Not in the slab schedule · asked Structnik", "—"),
                        Triple("Distribution bars", "Not detailed on ND 41-06", "—")
                    )
                    else -> listOf(
                        Triple("Slab concrete, M30", "2,641 × 4,500 mm clear × 125 thk", "1.49 m³"),
                        Triple("Cement, if site-mixed", "At 380 kg per m³", "11.3 bags")
                    )
                }
                rows.forEach { (a, b, c) ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            T(a, 14, Medium)
                            T(b, 12, color = Muted)
                        }
                        T(c, 14, mono = true)
                    }
                    HDivider()
                }
                Row(Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
                    T("Steel · Concrete", 14, SemiBold, modifier = Modifier.weight(1f))
                    T("71 kg · 1.49 m³", 14, SemiBold, mono = true)
                }
            }
            T("Source: slab schedule ND 41-06, mark S1. Size measured from plan.", 12, color = Muted)
        }
    }
}
