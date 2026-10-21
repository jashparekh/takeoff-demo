package com.jashparekh.takeoff

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import java.util.Locale

// ===================== 2a · Drawing coverage =====================
@Composable
fun CoverageScreen(s: DemoState, nav: NavHostController) {
    val b = s.buildings[s.coverageBuilding.coerceIn(0, s.buildings.lastIndex)]
    val rows = b.coverageRows()
    val sel = s.selectedPart?.takeIf { it.startsWith(b.code + "|") }
    val selRow = sel?.split("|")?.getOrNull(1)?.let { k -> rows.firstOrNull { it.key == k } }
    val openHistory: (String) -> Unit = { code -> nav.navigate("${R0.HISTORY}/$code") }

    Screen(
        header = { Header(s.projectName, "${s.buildings.size} buildings · drawing coverage", onBack = { nav.back() }) },
        gap = 10.dp,
        bottom = {
            if (selRow != null) {
                PrimaryButton("Upload for ${b.name} · ${selRow.label}", fontSize = 15) { nav.navigate(R0.TARGET) }
            } else {
                PrimaryButton("Tap a grey part to upload", enabled = false, fontSize = 15) {}
            }
        }
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            s.buildings.forEachIndexed { i, bb ->
                val on = i == s.coverageBuilding
                Column(
                    Modifier
                        .weight(1f)
                        .height(52.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (on) Ink else White)
                        .border(if (on) 2.dp else 1.dp, if (on) Ink else Gray300, RoundedCornerShape(10.dp))
                        .clickable(role = Role.Tab) { s.coverageBuilding = i },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    T(bb.tabLabel, 13, if (on) SemiBold else Medium, if (on) White else Ink2, maxLines = 1)
                    T("${s.coveredCount(bb)}/${bb.partCount}", 12, Medium, if (on) Gray300 else Gray500, mono = true)
                }
            }
        }
        TA(buildAnnotatedString {
            append("${b.name}: ")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontFamily = Mono)) { append("${s.coveredCount(b)}/${b.partCount}") }
            append(" parts have structural drawings. Tap a grey part to upload.")
        }, 13, Ink2)

        Column(
            Modifier.fillMaxWidth().card().padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Spacer(Modifier.width(74.dp))
                listOf("Slab + beams", "Columns", "Stair").forEach {
                    T(it, 12, SemiBold, Ink2, align = TextAlign.Center, modifier = Modifier.weight(1f), maxLines = 1)
                }
            }
            for (r in rows) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.width(74.dp)) {
                        T(r.label, 12, if (selRow?.key == r.key) SemiBold else FontWeight.Normal, Ink2, maxLines = 2)
                        if (r.elev != null) T(r.elev, 11, Medium, Gray500, mono = true)
                    }
                    if (r.cols == listOf(3)) {
                        CovCell(s, b, r, 3, Modifier.weight(1f), "Footings", openHistory)
                    } else {
                        for (c in 0..2) {
                            if (c in r.cols) CovCell(s, b, r, c, Modifier.weight(1f), null, openHistory)
                            else Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
            Row(Modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Legend("Has structural") { Box(Modifier.size(14.dp, 10.dp).clip(RoundedCornerShape(3.dp)).background(Covered)) }
                Legend("Missing") { Box(Modifier.size(14.dp, 10.dp).dashedBorder(Gray400, 3.dp)) }
                Legend("Selected") { Box(Modifier.size(14.dp, 10.dp).border(2.dp, Orange, RoundedCornerShape(3.dp))) }
            }
        }
        T("Estimates cover green parts only until the rest are uploaded.", 12, color = Muted)
    }
}

@Composable
private fun Legend(label: String, swatch: @Composable () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        swatch()
        T(label, 12, color = Muted)
    }
}

@Composable
private fun CovCell(
    s: DemoState, b: Building, r: CovRow, col: Int, modifier: Modifier, missingText: String?,
    onOpen: (String) -> Unit
) {
    val id = "${b.code}|${r.key}|$col"
    val code = s.covered[id]
    val selected = s.selectedPart == id
    val shape = RoundedCornerShape(6.dp)
    val desc = "${r.label} ${ColNames[col]}, " + when {
        code != null -> "covered by ND $code"
        selected -> "missing, selected"
        else -> "missing"
    }
    val base = modifier.height(44.dp).semantics { contentDescription = desc }
    when {
        code != null -> Box(
            base.clip(shape).background(Covered).clickable(role = Role.Button) { s.covered[id]?.let { onOpen(it) } },
            contentAlignment = Alignment.Center
        ) { T(code, 11, Medium, CoveredFg, mono = true) }
        selected -> Box(
            base.clip(shape).background(OrangeTint).border(2.dp, Orange, shape).clickable(role = Role.Button) {},
            contentAlignment = Alignment.Center
        ) { T("Selected", 12, SemiBold, OrangeFg) }
        else -> Box(
            base.clip(shape).background(Gray50).dashedBorder(Gray400, 6.dp)
                .clickable(role = Role.Button) { s.selectedPart = id },
            contentAlignment = Alignment.Center
        ) { if (missingText != null) T(missingText, 12, Medium, Muted) }
    }
}

// ===================== 2b · Upload for a missing part =====================
private data class TargetPart(val id: String, val row: CovRow, val col: Int)

@Composable
fun TargetUploadScreen(s: DemoState, nav: NavHostController) {
    val toast = LocalToast.current
    val parts = s.selectedPart?.split("|")
    val b = s.buildings.firstOrNull { it.code == parts?.getOrNull(0) } ?: s.buildings[1]
    val rows = b.coverageRows()
    val row = rows.firstOrNull { it.key == parts?.getOrNull(1) } ?: rows.first { it.key == "f2" || it.key.startsWith("f") }
    val col = parts?.getOrNull(2)?.toIntOrNull() ?: 0

    val main = TargetPart("${b.code}|${row.key}|$col", row, col)
    val companion: TargetPart? = run {
        val k = row.key.removePrefix("f").toIntOrNull() ?: return@run null
        if (!row.key.startsWith("f")) return@run null
        listOf(k + 2, k - 2).firstNotNullOfOrNull { n ->
            val r = rows.firstOrNull { it.key == "f$n" } ?: return@firstNotNullOfOrNull null
            val id = "${b.code}|${r.key}|$col"
            if (col in r.cols && s.covered[id] == null) TargetPart(id, r, col) else null
        }
    }
    val options = listOfNotNull(main, companion)
    var picked by remember { mutableStateOf(false) }
    val checked = remember { mutableStateListOf(*options.map { true }.toTypedArray()) }
    val code = String.format(Locale.US, "41-%02d", s.nextDrawingNo)
    val nChecked = checked.count { it }

    val floors = options.map { it.row }
    val allFloors = floors.all { it.key.matches(Regex("f\\d+")) }
    val planTitle = if (allFloors) floors.joinToString(" & ") { it.label.removeSuffix(" floor") } + " Floor Plan"
    else floors.joinToString(" & ") { it.label } + " Plan"
    val lowerTitle = if (allFloors) floors.joinToString(" & ") { it.label.removeSuffix(" floor") } + " floor"
    else floors.joinToString(" & ") { it.label.lowercase() }

    Screen(
        header = {
            Header(
                "Structural for ${row.label}",
                "${b.name} · ${ColNames[col]}" + (row.elev?.let { " · $it" } ?: ""),
                onBack = { nav.back() }
            )
        },
        bottom = {
            if (!picked) {
                PrimaryButton("Choose a PDF first", enabled = false) {}
            } else {
                PrimaryButton(
                    if (nChecked == 1) "Add to 1 part and read" else "Add to $nChecked parts and read",
                    enabled = nChecked > 0
                ) {
                    options.forEachIndexed { i, p -> if (checked[i]) s.covered[p.id] = code }
                    s.nextDrawingNo++
                    s.selectedPart = null
                    toast("ND $code added to $nChecked part" + if (nChecked == 1) "" else "s")
                    nav.back()
                }
            }
        }
    ) {
        Column(Modifier.fillMaxWidth().card()) {
            Box(Modifier.fillMaxWidth().height(150.dp).background(androidx.compose.ui.graphics.Color(0xFFFAFAFA)), contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxSize().padding(horizontal = 30.dp, vertical = 18.dp)) {
                    val w = size.width
                    val h = size.height
                    drawRect(OrangeTint, Offset.Zero, Size(w, h))
                    val xs = listOf(0.16f, 0.39f, 0.61f, 0.84f)
                    xs.forEach { f -> drawLine(Gray400, Offset(w * f, 0f), Offset(w * f, h), 2f) }
                    listOf(0.32f, 0.68f).forEach { f ->
                        drawLine(Gray400, Offset(0f, h * f), Offset(w * 0.39f, h * f), 2f)
                        drawLine(Gray400, Offset(w * 0.61f, h * f), Offset(w, h * f), 2f)
                    }
                    drawRect(
                        Orange, Offset.Zero, Size(w, h),
                        style = Stroke(width = 5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 9f)))
                    )
                }
                T("${row.label} plan · Arch Sheet 2", 13, SemiBold, OrangeFg)
            }
            HDivider()
            T("Area taken from the architect's $lowerTitle plan", 12, color = Muted, modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp))
        }
        Column(
            Modifier
                .fillMaxWidth()
                .height(96.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(White)
                .dashedBorder(Gray400, 14.dp, 2.dp)
                .clickable(role = Role.Button) { picked = true },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Ico(Ic.Upload, Ink2, 22.dp)
            Spacer(Modifier.height(6.dp))
            T(if (picked) "Choose another PDF" else "Choose PDF · or share from WhatsApp", 15, Medium, Ink2)
        }
        if (picked) {
            Card(gap = 6.dp) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    PdfBadge()
                    Column(Modifier.weight(1f)) {
                        T("ND $code · $planTitle", 14, SemiBold)
                        T("Example · ${s.consultant} · R0 · schedules found", 12, color = Muted)
                    }
                }
                T(if (options.size > 1) "This drawing also covers" else "This drawing covers", 13, SemiBold, Ink2, modifier = Modifier.padding(top = 4.dp))
                options.forEachIndexed { i, p ->
                    CheckRow("${p.row.label} · ${ColNames[p.col]}", checked[i]) { checked[i] = !checked[i] }
                }
                T(
                    "Matched from the title \"$lowerTitle\" and the plan outline. Untick anything that is wrong. Filed under ${b.name} only.",
                    12, color = Muted
                )
            }
        }
    }
}

// ===================== 2c · Drawing history =====================
@Composable
fun DrawingHistoryScreen(nav: NavHostController, code: String) {
    val (title, sub) = when (code) {
        "41-06" -> "ND 41-06 · Typical floor plan" to "Structural · Structnik · covers 1st, 3rd floor"
        "41-05" -> "ND 41-05 · Staircase details" to "Structural · Structnik · covers stair, all floors"
        else -> "ND $code · Floor plan" to "Structural · Structnik · added in this demo"
    }
    Screen(
        header = { Header(title, sub, onBack = { nav.back() }) },
        bottom = {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SecondaryButton("Compare R0 · R1", Modifier.weight(1f)) { nav.navigate(R0.REVISION) }
                PrimaryButton("Upload new revision", Modifier.weight(1f), fontSize = 15) { nav.navigate(R0.TARGET) }
            }
        }
    ) {
        T("Revisions", 13, SemiBold, Ink2)
        Column(Modifier.fillMaxWidth().card().padding(horizontal = 14.dp, vertical = 4.dp)) {
            RevRow("R1", "Uploaded 18 Oct · example", "2 changes · 1 issue resolved · 1 new", "Current", GreenFg, GreenBg, current = true)
            HDivider()
            RevRow("R0", "Dated 02 Apr 2026", "Kept for audit · 4 issues found", "Superseded", Ink2, Gray200, current = false)
        }
        SectionLabel("Issues across revisions", "4 open · 1 resolved")
        Column(Modifier.fillMaxWidth().card().padding(horizontal = 14.dp, vertical = 4.dp)) {
            IssueRow("Resolved R1", GreenFg, GreenBg, "B26 missing from schedule", "Found in R0 · now in the schedule")
            HDivider()
            IssueRow("New in R1", OrangeFg, OrangeBg, "B16/B22 bars changed, not clouded", "3-12T → 3-16T without a revision cloud")
            HDivider()
            IssueRow("Open since R0", AmberFg, AmberBg, "Sunk depth 225/250 vs 350 mm", "Asked Structnik on 21 Oct")
            HDivider()
            IssueRow("Open since R0", AmberFg, AmberBg, "Cover for beams, slabs not stated", "+1 more open")
        }
    }
}

@Composable
private fun RevRow(rev: String, title: String, sub: String, tag: String, fg: androidx.compose.ui.graphics.Color, bg: androidx.compose.ui.graphics.Color, current: Boolean) {
    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        T(rev, 14, SemiBold, if (current) Ink else Gray500, mono = true, modifier = Modifier.width(30.dp))
        Column(Modifier.weight(1f)) {
            T(title, 14, Medium, if (current) Ink else Muted)
            T(sub, 12, color = Muted)
        }
        Chip(tag, fg, bg, size = 11, radius = 5.dp)
    }
}

@Composable
private fun IssueRow(tag: String, fg: androidx.compose.ui.graphics.Color, bg: androidx.compose.ui.graphics.Color, title: String, sub: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
        Chip(tag, fg, bg, size = 11, radius = 4.dp)
        Column(Modifier.weight(1f)) {
            T(title, 14, Medium)
            T(sub, 12, color = Muted)
        }
    }
}
