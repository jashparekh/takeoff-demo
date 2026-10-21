package com.jashparekh.takeoff

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

// ===================== 1 · Projects list =====================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(vm: DemoViewModel, nav: NavHostController) {
    val s = vm.s
    val toast = LocalToast.current
    var menu by remember { mutableStateOf(false) }
    var about by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().background(Bg)) {
        Row(
            Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(Ink), contentAlignment = Alignment.Center) {
                Ico(Ic.Building, White, 18.dp)
            }
            Spacer(Modifier.width(10.dp))
            T("Takeoff", 20, Bold)
            Spacer(Modifier.width(8.dp))
            Chip("DEMO", OrangeFg, OrangeBg, size = 11, radius = 5.dp)
            Spacer(Modifier.weight(1f))
            Box {
                Box(
                    Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(White)
                        .border(1.dp, Gray300, RoundedCornerShape(24.dp))
                        .clickable(role = Role.Button) { menu = true }
                        .semantics { contentDescription = "Account and demo settings" },
                    contentAlignment = Alignment.Center
                ) { T("JP", 15, SemiBold) }
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                    DropdownMenuItem(
                        text = { T("Reset demo", 16) },
                        onClick = { menu = false; vm.reset(); toast("Demo reset. All sample data restored.") }
                    )
                    DropdownMenuItem(
                        text = { T("About this demo", 16) },
                        onClick = { menu = false; about = true }
                    )
                }
            }
        }

        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(Modifier.padding(top = 4.dp, bottom = 4.dp)) {
                T("Projects", 26, Bold)
                T("Steel and cement from your structural drawings", 14, color = Muted, modifier = Modifier.padding(top = 4.dp))
            }
            ProjectCard(
                name = s.projectName,
                sub = "3 buildings · Bldg 2 typical floor · ${s.city.substringBefore(",")}",
                status = "Estimated", statusFg = GreenFg, statusBg = GreenBg,
                steel = "3.79 t", cement = "317 bags", perSqft = "₹250"
            ) { nav.navigate(R0.COVERAGE) }
            ProjectCard(
                name = "Work Lounge",
                sub = "Second floor slab + beams · Hubballi",
                status = "3 to confirm", statusFg = AmberFg, statusBg = AmberBg,
                steel = "11.99 t", cement = "586 bags", perSqft = "₹223"
            ) { nav.navigate(R0.ISSUES) }
            Row(
                Modifier.fillMaxWidth().dashedBorder(Gray400, 14.dp).padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Ico(Ic.File, Muted, 22.dp)
                T("Works best with vector PDFs exported from AutoCAD or ZWCAD. Scanned drawings coming later.", 13, color = Muted, lineHeight = 1.4f)
            }
        }
        Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 20.dp)) {
            PrimaryButton("New project", icon = Ic.Plus) { nav.navigate(R0.CREATE) }
        }
    }

    if (about) {
        ModalBottomSheet(onDismissRequest = { about = false }, containerColor = White) {
            Column(
                Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                T("About this demo", 20, Bold)
                T("Sample data only, nothing is uploaded.", 16, SemiBold)
                T(
                    "Every project, drawing and number in this app is example data kept on this phone. " +
                        "Nothing is sent anywhere and no drawing is really read. Use \"Reset demo\" to start again.",
                    15, color = Muted, lineHeight = 1.45f
                )
                T("Takeoff demo · version 0.1.0", 13, color = Muted, mono = true)
                PrimaryButton("OK") { about = false }
            }
        }
    }
}

@Composable
private fun ProjectCard(
    name: String, sub: String, status: String,
    statusFg: androidx.compose.ui.graphics.Color, statusBg: androidx.compose.ui.graphics.Color,
    steel: String, cement: String, perSqft: String, onClick: () -> Unit
) {
    Column(
        Modifier.fillMaxWidth().card().clickable(role = Role.Button, onClick = onClick).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                T(name, 16, SemiBold)
                T(sub, 13, color = Muted, modifier = Modifier.padding(top = 2.dp))
            }
            Spacer(Modifier.width(8.dp))
            Chip(status, statusFg, statusBg)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.weight(1f)) { KeyVal("Steel", steel) }
            Box(Modifier.weight(1f)) { KeyVal("Cement", cement) }
            Box(Modifier.weight(1f)) { KeyVal("Per sq ft", perSqft) }
        }
    }
}

// ===================== 1a · New project =====================
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateProjectScreen(s: DemoState, nav: NavHostController) {
    val toast = LocalToast.current
    Screen(
        header = {
            Header("New project", null, onBack = { nav.back() }, titleSize = 18) {
                T("Step 1 of 2", 12, color = Muted, modifier = Modifier.padding(end = 12.dp))
            }
        },
        bottom = { PrimaryButton("Create project · add drawings") { nav.navigate(R0.UPLOAD) } }
    ) {
        Card(gap = 10.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                T("Project name", 12, SemiBold, Ink2)
                Field(s.projectName, { s.projectName = it }, Modifier.fillMaxWidth(), label = "Project name")
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    T("Site city", 12, SemiBold, Ink2)
                    Field(s.city, { s.city = it }, Modifier.fillMaxWidth(), label = "Site city")
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    T("Structural consultant", 12, SemiBold, Ink2, maxLines = 1)
                    Field(s.consultant, { s.consultant = it }, Modifier.fillMaxWidth(), label = "Structural consultant")
                }
            }
            T("City sets the steel, RMC and cement rates. Consultant is who we send questions to.", 12, color = Muted)
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
            T("Buildings (${s.buildings.size})", 13, SemiBold, Ink2, modifier = Modifier.weight(1f))
            T("Drawings are filed per building", 12, color = Muted)
        }
        Column(Modifier.fillMaxWidth().card().padding(horizontal = 14.dp, vertical = 2.dp)) {
            s.buildings.forEachIndexed { i, b ->
                val (bg, fg) = when (i) {
                    0 -> OrangeBg to OrangeFg
                    1 -> Orange to White
                    else -> Gray200 to Ink2
                }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable(role = Role.Button) { nav.navigate("${R0.BUILDING}/$i") }
                        .padding(vertical = 11.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(bg), contentAlignment = Alignment.Center) {
                        T(b.code, 13, SemiBold, fg, mono = true)
                    }
                    Column(Modifier.weight(1f)) {
                        TA(buildAnnotatedString {
                            withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) { append(b.name) }
                            if (b.wings != null) withStyle(SpanStyle(fontWeight = FontWeight.Medium, color = Muted)) { append(" · ${b.wings}") }
                        }, 14)
                        T(b.subtitle, 12, color = Muted)
                    }
                    Ico(Ic.Chevron, Gray500, 18.dp)
                }
                if (i < s.buildings.lastIndex) HDivider()
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                Modifier.weight(1f).height(48.dp).dashedBorder(Gray400, 10.dp).clip(RoundedCornerShape(10.dp))
                    .clickable(role = Role.Button) { toast("Demo: this sample project already has its 3 buildings") },
                contentAlignment = Alignment.Center
            ) { T("+ Add building", 14, Medium, Ink2) }
            Box(
                Modifier.weight(1f).height(48.dp).card(10.dp, Gray300)
                    .clickable(role = Role.Button) { toast("Demo: reading a site plan is not available") },
                contentAlignment = Alignment.Center
            ) { T("Read from site plan", 14, Medium, Ink2) }
        }
        Card(gap = 8.dp) {
            TA(buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = Ink2)) { append("Values you already know") }
                withStyle(SpanStyle(color = Muted)) { append(" · blanks are asked later") }
            }, 12)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                KnownValue("Concrete ", "M30", true)
                KnownValue("Steel ", "Fe550", true)
                KnownValue("Units ", "mm", false)
                Box(Modifier.dashedBorder(Gray400, 8.dp).padding(horizontal = 10.dp, vertical = 6.dp)) {
                    T("Cement content · ask me", 13, color = Muted)
                }
            }
        }
    }
}

@Composable
private fun KnownValue(label: String, value: String, mono: Boolean) {
    Box(Modifier.clip(RoundedCornerShape(8.dp)).background(Bg).padding(horizontal = 10.dp, vertical = 6.dp)) {
        TA(buildAnnotatedString {
            append(label)
            withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontFamily = if (mono) Mono else null)) { append(value) }
        }, 13)
    }
}

// ===================== 1b · Building setup =====================
@Composable
fun AddBuildingScreen(s: DemoState, nav: NavHostController, idx: Int) {
    val original = s.buildings.getOrNull(idx) ?: s.buildings[1]
    var b by remember(idx) { mutableStateOf(original) }

    Screen(
        header = { Header(b.name, s.projectName, onBack = { nav.back() }) },
        bottom = {
            PrimaryButton("Save building") {
                if (idx in s.buildings.indices) s.buildings[idx] = b
                nav.back()
            }
        }
    ) {
        Card(gap = 12.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                T("Use", 12, SemiBold, Ink2)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Residential", "Commercial", "Mixed").forEach { u ->
                        Pill(u, b.use == u, Modifier.weight(1f), height = 44.dp, fontSize = 13) { b = b.copy(use = u) }
                    }
                }
            }
            SetupRow("Basements") {
                Stepper(b.basements, "basements", 0, 4) { b = b.copy(basements = it) }
            }
            SetupRow("Ground level") {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("Stilt", "Ground", "Podium").forEach { g ->
                        val sel = b.ground == g
                        Box(
                            Modifier
                                .height(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(White)
                                .border(if (sel) 2.dp else 1.dp, if (sel) Ink else Gray300, RoundedCornerShape(8.dp))
                                .clickable(role = Role.Button) { b = b.copy(ground = g) }
                                .padding(horizontal = 10.dp),
                            contentAlignment = Alignment.Center
                        ) { T(g, 13, if (sel) SemiBold else Medium, if (sel) Ink else Ink2) }
                    }
                }
            }
            SetupRow("Upper floors") {
                Stepper(b.upper, "floors", 1, 40) { b = b.copy(upper = it) }
            }
            SetupRow("Roof") {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MiniCheck("Terrace", b.terrace) { b = b.copy(terrace = !b.terrace) }
                    MiniCheck("OHT / LMR", b.oht) { b = b.copy(oht = !b.oht) }
                }
            }
            SetupRow("Identical copies") {
                Stepper(b.copies, "copies", 1, 9) { b = b.copy(copies = it) }
            }
            T("Wings built from the same drawings? Set copies to 2 and quantities are multiplied, not uploaded twice.", 12, color = Muted)
        }
        Card(gap = 8.dp) {
            Row(verticalAlignment = Alignment.Bottom) {
                T("Levels · tap to edit height", 12, SemiBold, Ink2, modifier = Modifier.weight(1f))
                T("Filled from arch section", 12, color = Muted)
            }
            if (b.oht) LevelRow("OHT / LMR", plus(b.ohtElev))
            if (b.terrace) LevelRow("Terrace", plus(b.terraceElev))
            when {
                b.upper >= 2 -> LevelRow("1st – ${ordinal(b.upper)} floor", "2.95 m", "Typical ×${b.upper}", bold = true)
                b.upper == 1 -> LevelRow("1st floor", "2.95 m")
            }
            LevelRow("${b.ground} / plinth", "+0.15")
            for (k in 1..b.basements) LevelRow("Basement $k", "-" + fmt(3.0 * k))
            LevelRow("Foundation", "—")
        }
        Row(
            Modifier.padding(horizontal = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Ico(Ic.Info, Muted, 18.dp)
            T("${b.levelCount} levels · ${b.partCount} parts to cover with structural drawings", 13, color = Ink2)
        }
    }
}

@Composable
private fun SetupRow(label: String, control: @Composable () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        T(label, 14, modifier = Modifier.weight(1f))
        control()
    }
}

@Composable
private fun Stepper(value: Int, what: String, min: Int, max: Int, onChange: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        StepBtn("−", "Fewer $what", value > min) { onChange(value - 1) }
        T("$value", 15, SemiBold, mono = true, align = TextAlign.Center, modifier = Modifier.width(32.dp))
        StepBtn("+", "More $what", value < max) { onChange(value + 1) }
    }
}

@Composable
private fun StepBtn(label: String, desc: String, enabled: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(White)
            .border(1.dp, Gray300, RoundedCornerShape(8.dp))
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .semantics { contentDescription = desc },
        contentAlignment = Alignment.Center
    ) { T(label, 20, color = if (enabled) Ink else Gray400) }
}

@Composable
private fun MiniCheck(label: String, checked: Boolean, onToggle: () -> Unit) {
    Row(
        Modifier.height(44.dp).clip(RoundedCornerShape(6.dp)).clickable(role = Role.Checkbox, onClick = onToggle).padding(horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        CheckBox(checked, 20.dp)
        T(label, 13)
    }
}

@Composable
private fun LevelRow(name: String, value: String, tag: String? = null, bold: Boolean = false) {
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
        T(name, 13, if (bold) SemiBold else FontWeight.Normal, modifier = Modifier.weight(1f))
        T(value, 12, Medium, Muted, mono = true)
        Spacer(Modifier.width(12.dp))
        Box(Modifier.width(78.dp), contentAlignment = Alignment.CenterEnd) {
            if (tag != null) Chip(tag, OrangeFg, OrangeBg, size = 11, radius = 4.dp)
        }
    }
}

// ===================== 2 · Add drawings =====================
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UploadScreen(s: DemoState, nav: NavHostController) {
    Screen(
        header = { Header("Add drawings", null, onBack = { nav.back() }, titleSize = 18) },
        gap = 18.dp,
        bottom = {
            T("Structural sheets give the bars. Architectural sheets give the spans.", 12, color = Muted, align = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            PrimaryButton("Read drawings") { nav.navigate(R0.READING) }
        }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            T("For building", 13, SemiBold, Ink2)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val names = s.buildings.map { it.name } + "Site-wide"
                names.forEachIndexed { i, n -> Pill(n, s.uploadBuilding == i) { s.uploadBuilding = i } }
            }
            T("We also read the title block and flag a sheet that names another building.", 12, color = Muted)
        }
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            T("What to estimate", 13, SemiBold, Ink2)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Slab + beams", "Columns", "Footings").forEachIndexed { i, n ->
                    Pill(n, s.uploadScope == i) { s.uploadScope = i }
                }
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionLabel("Drawings (${s.drawings.size + s.moreArchSheets})", "Tap a tag to change it")
            Column(Modifier.fillMaxWidth().card(12.dp)) {
                s.drawings.forEachIndexed { i, d ->
                    DrawingRow(d.name, d.meta, if (d.structural) "Structural" else "Architectural", d.structural) {
                        s.drawings[i] = d.copy(structural = !d.structural)
                    }
                    HDivider()
                }
                DrawingRow("+ ${s.moreArchSheets} more architectural sheets", "Sections, elevation, terrace", null, false) {}
            }
        }
        Row(
            Modifier
                .fillMaxWidth()
                .height(52.dp)
                .dashedBorder(Gray400, 12.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable(role = Role.Button) { s.addSampleDrawing() },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Ico(Ic.Upload, Ink2, 18.dp)
            Spacer(Modifier.width(8.dp))
            T("Add PDF or DWG", 15, Medium, Ink2)
        }
    }
}

@Composable
private fun DrawingRow(name: String, meta: String, tag: String?, structural: Boolean, onTag: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PdfBadge()
        Column(Modifier.weight(1f)) {
            T(name, 14, Medium, maxLines = 1)
            T(meta, 12, color = Muted)
        }
        if (tag != null) {
            Box(
                Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (structural) BlueBg else Gray200)
                    .clickable(role = Role.Button, onClick = onTag)
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            ) { T(tag, 12, SemiBold, if (structural) BlueFg else Ink2) }
        }
    }
}

@Composable
fun PdfBadge() {
    Box(Modifier.size(36.dp, 44.dp).clip(RoundedCornerShape(6.dp)).background(OrangeBg), contentAlignment = Alignment.Center) {
        T("PDF", 10, SemiBold, OrangeFg, mono = true)
    }
}
