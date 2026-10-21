package com.jashparekh.takeoff

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

// ===================== 5 · Values we need =====================
@Composable
fun IssuesScreen(s: DemoState, nav: NavHostController) {
    val toast = LocalToast.current
    var otherDialog by remember { mutableStateOf(false) }
    val left = s.emptyCount
    val sunkOptions = listOf("225/250 mm", "350 mm")

    Screen(
        header = {
            Header(
                "Values we need",
                if (left == 0) "Building 2 · all 3 filled" else "Building 2 · $left of 3 still empty",
                onBack = { nav.back() }
            )
        },
        gap = 10.dp,
        bottom = {
            T("Nothing is calculated until every value is filled and confirmed.", 12, color = Muted, modifier = Modifier.fillMaxWidth(), align = androidx.compose.ui.text.style.TextAlign.Center)
            if (left == 0) PrimaryButton("Review all values") { nav.navigate(R0.CONFIRM) }
            else PrimaryButton("Fill $left more to review all values", enabled = false) {}
        }
    ) {
        T("The drawings don't give these. We never fill in a value for you: enter each one, or ask the consultant and come back.", 13, color = Ink2, lineHeight = 1.45f)

        ValueCard("Cement content for M30", s.cement.isNotBlank(), if (s.cement.isNotBlank()) "You entered" else "Needed") {
            T("From your mix design or the RMC supplier's sheet.", 12, color = Muted)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Field(s.cement, { s.cement = it.filter { c -> c.isDigit() }.take(4) }, Modifier.width(100.dp), "Enter", mono = true, number = true, label = "Cement content, kg per cubic metre")
                T("kg per m³", 13, color = Ink2)
            }
        }

        val sunkFilled = s.sunk != null
        ValueCard("Sunk slab depth at WCs", sunkFilled, if (sunkFilled) "You entered" else "Drawings disagree") {
            T("Structural ND 41-06 says 225/250 mm, architectural section says 350 mm. Pick one or ask.", 12, color = Muted)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                sunkOptions.forEach { o ->
                    Pill(o, s.sunk == o, Modifier.weight(1f), height = 48.dp, radius = 10.dp) { s.sunk = o }
                }
                val custom = s.sunk != null && s.sunk !in sunkOptions
                Pill(if (custom) s.sunk ?: "Other" else "Other", custom, Modifier.weight(1f), height = 48.dp, radius = 10.dp) { otherDialog = true }
            }
        }

        val coverFilled = s.coverBeams.isNotBlank() && s.coverSlabs.isNotBlank()
        ValueCard("Clear cover", coverFilled, if (coverFilled) "You entered" else "Not on drawing") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    T("Beams (mm)", 12, color = Ink2)
                    Field(s.coverBeams, { s.coverBeams = it.filter { c -> c.isDigit() }.take(3) }, Modifier.fillMaxWidth(), "Enter", mono = true, number = true, label = "Clear cover for beams in mm")
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    T("Slabs (mm)", 12, color = Ink2)
                    Field(s.coverSlabs, { s.coverSlabs = it.filter { c -> c.isDigit() }.take(3) }, Modifier.fillMaxWidth(), "Enter", mono = true, number = true, label = "Clear cover for slabs in mm")
                }
            }
        }

        if (left > 0) {
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                    .clickable(role = Role.Button) { toast("Demo: question to ${s.consultant} saved, nothing is sent") }
                    .padding(vertical = 12.dp, horizontal = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Ico(Ic.Chat, Link, 18.dp)
                T("Ask ${s.consultant} the $left empty one" + if (left == 1) "" else "s", 14, Medium, Link)
            }
        }
    }

    if (otherDialog) {
        EditDialog(
            title = "Sunk slab depth at WCs",
            fields = listOf("Depth in mm" to (s.sunk?.removeSuffix(" mm")?.takeIf { s.sunk !in sunkOptions } ?: "")),
            onDismiss = { otherDialog = false },
            onSave = { v ->
                val d = v[0].trim()
                if (d.isNotEmpty()) s.sunk = "$d mm"
                otherDialog = false
            }
        )
    }
}

@Composable
private fun ValueCard(title: String, filled: Boolean, tag: String, content: @Composable () -> Unit) {
    Card(
        border = if (filled) Border else Orange,
        borderWidth = if (filled) 1.dp else 2.dp,
        radius = 12.dp,
        padH = 14.dp,
        padV = 14.dp,
        gap = 10.dp
    ) {
        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            T(title, 14, SemiBold, modifier = Modifier.weight(1f))
            if (filled) Chip(tag, GreenFg, GreenBg, size = 11, radius = 5.dp)
            else Chip(tag, OrangeFg, OrangeBg, size = 11, radius = 5.dp)
        }
        content()
    }
}

/** Small dialog with one or more text fields. */
@Composable
fun EditDialog(title: String, fields: List<Pair<String, String>>, onDismiss: () -> Unit, onSave: (List<String>) -> Unit) {
    val values = remember { mutableStateListOf(*fields.map { it.second }.toTypedArray()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = White,
        title = { T(title, 18, SemiBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                fields.forEachIndexed { i, (label, _) ->
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        T(label, 13, SemiBold, Ink2)
                        Field(values[i], { values[i] = it }, Modifier.fillMaxWidth(), "Enter", mono = true, label = label)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(values.toList()) }, modifier = Modifier.height(48.dp)) { T("Save", 16, SemiBold, Orange) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.height(48.dp)) { T("Cancel", 16, Medium, Muted) }
        }
    )
}

// ===================== 5a · Confirm =====================
@Composable
fun ConfirmScreen(s: DemoState, nav: NavHostController) {
    var editing by remember { mutableStateOf<String?>(null) }

    Screen(
        header = { Header("Confirm before calculating", "Building 2 · typical floor · tap any value to change it", onBack = { nav.back() }) },
        gap = 10.dp,
        bottom = {
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                    .clickable(role = Role.Checkbox) { s.valuesChecked = !s.valuesChecked }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CheckBox(s.valuesChecked)
                T("I've checked these values. Changing one later brings me back here.", 13, color = Ink2, modifier = Modifier.weight(1f))
            }
            PrimaryButton("Confirm and generate report", enabled = s.valuesChecked) { nav.navigate(R0.RESULTS) }
        }
    ) {
        Row(Modifier.fillMaxWidth()) {
            T("Read from drawings", 12, SemiBold, Ink2, modifier = Modifier.weight(1f))
            T("source shown", 12, color = Muted)
        }
        Column(Modifier.fillMaxWidth().card(12.dp).padding(horizontal = 14.dp)) {
            ConfirmRow("Concrete · steel grade", "General note · ND 41-06", s.grades, 14) { editing = "grades" }
            HDivider()
            Row(
                Modifier.fillMaxWidth().clickable(role = Role.Button) { nav.navigate(R0.REVIEW) }.padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    T("Beams, slabs, bars, spans", 14)
                    T("Schedules + plan · checked in review", 12, color = Muted)
                }
                T("Open ›", 13, SemiBold, Link)
            }
        }
        T("Entered by you", 12, SemiBold, Ink2)
        Column(Modifier.fillMaxWidth().card(12.dp).padding(horizontal = 14.dp)) {
            ConfirmRow("Cement content", null, if (s.cement.isBlank()) "—" else "${s.cement} kg/m³", 14) { editing = "cement" }
            HDivider()
            ConfirmRow("Sunk depth at WCs", null, s.sunk ?: "—", 14) { editing = "sunk" }
            HDivider()
            ConfirmRow("Clear cover, beams / slabs", null, "${s.coverBeams.ifBlank { "—" }} / ${s.coverSlabs.ifBlank { "—" }} mm", 14) { editing = "cover" }
            HDivider()
            ConfirmRow("Wastage, steel / concrete", null, "${s.wasteSteel}% / ${s.wasteConcrete}%", 14) { editing = "waste" }
        }
        T("Rates and area", 12, SemiBold, Ink2)
        Column(Modifier.fillMaxWidth().card(12.dp).padding(horizontal = 14.dp)) {
            ConfirmRow("TMT 8 / 10 / 12 / 16 mm", "Mumbai price list · Aug 2026", s.tmtRates, 13) { editing = "tmt" }
            HDivider()
            ConfirmRow("RMC M30 · OPC bag", "Mumbai price list · Aug 2026", s.rmcRates, 13) { editing = "rmc" }
            HDivider()
            ConfirmRow("Floor area for ₹/sq ft", "Measured on Arch Sheet 1", s.floorArea, 13) { editing = "area" }
        }
    }

    val key = editing
    if (key != null) {
        val close = { editing = null }
        fun saved() { s.valuesChecked = false; editing = null }
        when (key) {
            "grades" -> EditDialog("Concrete · steel grade", listOf("Grades" to s.grades), close) { v -> s.grades = v[0]; saved() }
            "cement" -> EditDialog("Cement content", listOf("kg per m³" to s.cement), close) { v -> s.cement = v[0].filter { it.isDigit() }; saved() }
            "sunk" -> EditDialog("Sunk depth at WCs", listOf("Depth" to (s.sunk ?: "")), close) { v ->
                val t = v[0].trim(); s.sunk = if (t.isEmpty()) null else if (t.endsWith("mm")) t else "$t mm"; saved()
            }
            "cover" -> EditDialog("Clear cover", listOf("Beams (mm)" to s.coverBeams, "Slabs (mm)" to s.coverSlabs), close) { v ->
                s.coverBeams = v[0].filter { it.isDigit() }; s.coverSlabs = v[1].filter { it.isDigit() }; saved()
            }
            "waste" -> EditDialog("Wastage", listOf("Steel (%)" to s.wasteSteel, "Concrete (%)" to s.wasteConcrete), close) { v ->
                s.wasteSteel = v[0].trim(); s.wasteConcrete = v[1].trim(); saved()
            }
            "tmt" -> EditDialog("TMT rates, ₹ per kg", listOf("8 / 10 / 12 / 16 mm" to s.tmtRates), close) { v -> s.tmtRates = v[0]; saved() }
            "rmc" -> EditDialog("RMC M30 · OPC bag", listOf("Rates" to s.rmcRates), close) { v -> s.rmcRates = v[0]; saved() }
            else -> EditDialog("Floor area", listOf("Area" to s.floorArea), close) { v -> s.floorArea = v[0]; saved() }
        }
    }
}

@Composable
private fun ConfirmRow(label: String, source: String?, value: String, valueSize: Int, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(role = Role.Button, onClick = onClick).padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(Modifier.weight(1f)) {
            T(label, 14)
            if (source != null) T(source, 12, color = Muted)
        }
        T(value, valueSize, SemiBold, mono = true)
        Ico(Ic.Edit, Gray500, 16.dp)
    }
}

// ===================== 6 · Results =====================
@Composable
fun ResultsScreen(nav: NavHostController) {
    val toast = LocalToast.current
    Screen(
        header = { Header("Bldg 2 · Typical floor", "Slab + 77 beams · M30 · Fe550", onBack = { if (!nav.popBackStack(R0.MAIN, false)) nav.navigate(R0.MAIN) }) },
        bottom = {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SecondaryButton("BBS Excel", Modifier.weight(1f)) { toast("Demo: export not available") }
                PrimaryButton("Share report", Modifier.weight(1f), icon = Ic.Share, fontSize = 15) { toast("Demo: export not available") }
            }
        }
    ) {
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Ink).padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BigStat("Steel", "3.79 t", null, Modifier.weight(1f))
            BigStat("Cement", "317", "bags", Modifier.weight(1f))
            BigStat("Concrete", "41.7", "m³", Modifier.weight(1f))
        }
        Card(padH = 16.dp, padV = 14.dp) {
            T("Steel by bar size", 14, SemiBold)
            listOf(
                Triple("8 mm", 1.0f, "2,128"),
                Triple("16 mm", 0.52f, "1,103"),
                Triple("12 mm", 0.18f, "392"),
                Triple("10 mm", 0.08f, "169")
            ).forEach { (size, frac, kg) ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    T(size, 13, mono = true, modifier = Modifier.width(48.dp))
                    Box(Modifier.weight(1f)) {
                        Box(Modifier.fillMaxWidth(frac).height(12.dp).clip(RoundedCornerShape(3.dp)).background(Orange))
                    }
                    T(kg, 13, mono = true, align = androidx.compose.ui.text.style.TextAlign.End, modifier = Modifier.width(64.dp))
                }
            }
            T("kg, incl. 3% wastage · beams 2,717 · slab 963", 12, color = Muted)
        }
        Card(padH = 16.dp, padV = 14.dp) {
            Row(verticalAlignment = Alignment.Bottom) {
                T("Cost per sq ft", 14, SemiBold, modifier = Modifier.weight(1f))
                T("2,145 sq ft floor plate", 12, color = Muted)
            }
            CostRow("Steel", "₹120", "₹2.57 L")
            CostRow("Concrete (ready-mix)", "₹130", "₹2.79 L")
            Box(Modifier.fillMaxWidth().height(1.dp).background(Border))
            Row {
                T("Total", 16, SemiBold, modifier = Modifier.weight(1f))
                T("₹250 / sq ft", 16, SemiBold, mono = true)
            }
            T(
                "Prices: Mumbai region, Aug 2026 · Edit", 13, color = Link,
                modifier = Modifier.clip(RoundedCornerShape(6.dp)).clickable(role = Role.Button) { nav.navigate(R0.CONFIRM) }.padding(vertical = 8.dp)
            )
        }
        NoteRow(Ic.Check, "Every input was confirmed by you on 21 Oct. 2 questions are still open with the consultant.", GreenFg)

        T("More from this estimate", 13, SemiBold, Ink2, modifier = Modifier.padding(top = 4.dp))
        Column(Modifier.fillMaxWidth().card().padding(horizontal = 14.dp, vertical = 2.dp)) {
            val tools = listOf(
                Triple("Cutting plan", "12 m stock bars, offcuts and laps", R0.CUTTING),
                Triple("What changed in R1", "Revision compare for ND 41-06", R0.REVISION),
                Triple("Drawing check", "2 conflicts · 2 missing · 1 minor", R0.CHECK),
                Triple("Order for next pour", "Purchase list and dealer quotes", R0.PURCHASE),
                Triple("Steel used vs planned", "Site reconciliation", R0.RECON)
            )
            tools.forEachIndexed { i, (title, sub, route) ->
                Row(
                    Modifier.fillMaxWidth().clickable(role = Role.Button) { nav.navigate(route) }.padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        T(title, 15, Medium)
                        T(sub, 12, color = Muted)
                    }
                    Ico(Ic.Chevron, Gray500, 18.dp)
                }
                if (i < tools.lastIndex) HDivider()
            }
        }
    }
}

@Composable
private fun BigStat(label: String, value: String, unit: String?, modifier: Modifier) {
    Column(modifier) {
        T(label, 12, color = Gray300)
        T(value, 22, SemiBold, White, mono = true, maxLines = 1)
        if (unit != null) T(unit, 12, color = Gray300)
    }
}

@Composable
private fun CostRow(label: String, perSqft: String, total: String) {
    Row {
        T(label, 14, modifier = Modifier.weight(1f))
        T(perSqft, 14, mono = true)
        T(" · $total", 14, color = Muted, mono = true)
    }
}

/** Dark stat bar used by Cutting plan. */
@Composable
fun StatBar(items: List<Pair<String, String>>) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Ink).padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { (l, v) ->
            Column(Modifier.weight(1f)) {
                T(l, 12, color = Gray300, maxLines = 1)
                T(v, 20, SemiBold, White, mono = true, maxLines = 1)
            }
        }
    }
}
