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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

// ===================== 7 · Cutting plan =====================
private class Piece(val frac: Float, val label: String?, val bg: Color, val fg: Color)

private val Peach = Color(0xFFFED7AA) to Color(0xFF7C2D12)
private val Sky = Color(0xFFBFDBFE) to Color(0xFF1E3A8A)
private val Mint = Color(0xFFBBF7D0) to Color(0xFF14532D)
private val Lilac = Color(0xFFE9D5FF) to Color(0xFF4C1D95)
private val Butter = Color(0xFFFDE68A) to Color(0xFF713F12)

private fun pc(frac: Float, label: String?, c: Pair<Color, Color>) = Piece(frac, label, c.first, c.second)
private fun off(frac: Float) = Piece(frac, null, Gray200, Gray200)

@Composable
fun CuttingPlanScreen(nav: NavHostController) {
    val toast = LocalToast.current
    var barSize by remember { mutableIntStateOf(2) }
    Screen(
        header = { Header("Cutting plan", "From 12 m stock bars · Bldg 2 typical floor", onBack = { nav.back() }) },
        bottom = {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SecondaryButton("Cut list PDF", Modifier.weight(1f)) { toast("Demo: export not available") }
                PrimaryButton("Order 28 bars", Modifier.weight(1f), fontSize = 15) { nav.navigate(R0.PURCHASE) }
            }
        }
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("8 mm", "10 mm", "12 mm", "16 mm").forEachIndexed { i, l ->
                Pill(l, barSize == i, Modifier.weight(1f), height = 44.dp, radius = 10.dp) { barSize = i }
            }
        }
        StatBar(listOf("Stock bars" to "28", "Offcut" to "1.2%", "Saved vs uncut" to "18 kg"))
        Card(padH = 16.dp, padV = 14.dp, gap = 14.dp) {
            T("Cut patterns", 14, SemiBold)
            Pattern(
                "A · ×18 bars", "offcut 96 mm",
                listOf(pc(25.2f, "3,021", Peach), pc(25.2f, "3,021", Peach), pc(25.2f, "3,021", Peach), pc(23.6f, "2,841", Sky), off(0.8f)),
                "B1-group bottom bars · B44-group top bars"
            )
            Pattern(
                "B · ×6 bars", "offcut 116 mm",
                listOf(pc(45.4f, "5,452", Mint), pc(45.4f, "5,452", Mint), pc(8.2f, "980", Lilac), off(1f)),
                "B7-group top bars · stair landing bars"
            )
            Pattern(
                "C · ×4 bars", "offcut 139 mm",
                listOf(pc(61.2f, "7,341", Butter), pc(37.6f, "4,520", Sky), off(1.2f)),
                "B16/B22 bottom bars · B12-group top bars"
            )
        }
        T("Lengths in mm. Laps placed at low-stress zones per IS 456. Example figures.", 12, color = Muted)
    }
}

@Composable
private fun Pattern(title: String, offcut: String, pieces: List<Piece>, note: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row {
            T(title, 13, SemiBold, modifier = Modifier.weight(1f))
            T(offcut, 13, color = Muted, mono = true)
        }
        Row(
            Modifier.fillMaxWidth().height(28.dp).clip(RoundedCornerShape(4.dp)).border(1.dp, Gray400, RoundedCornerShape(4.dp))
        ) {
            pieces.forEachIndexed { i, p ->
                Box(
                    Modifier.weight(p.frac).fillMaxHeight().background(p.bg),
                    contentAlignment = Alignment.Center
                ) {
                    val label = p.label
                    if (label != null) T(label, if (p.frac < 10f) 10 else 11, Medium, p.fg, mono = true, maxLines = 1)
                }
                if (i < pieces.lastIndex && p.label != null) Box(Modifier.size(1.dp, 28.dp).background(White))
            }
        }
        T(note, 12, color = Muted)
    }
}

// ===================== 8 · Revision compare =====================
@Composable
fun RevisionCompareScreen(nav: NavHostController) {
    val toast = LocalToast.current
    Screen(
        header = { Header("What changed in R1", "ND 41-06 · R0 (02 Apr) vs R1 (example)", onBack = { nav.back() }) },
        bottom = {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SecondaryButton("Share change note", Modifier.weight(1f)) { toast("Demo: export not available") }
                PrimaryButton("Use R1", Modifier.weight(1f), fontSize = 15) {
                    toast("Estimate updated to R1 (demo)")
                    nav.navigate(R0.RESULTS) { popUpTo(R0.RESULTS) { inclusive = true } }
                }
            }
        }
    ) {
        Row(Modifier.fillMaxWidth().card().padding(horizontal = 16.dp, vertical = 14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DeltaStat("Steel", "+72 kg", OrangeFg, "3.79 → 3.86 t", Modifier.weight(1f))
            DeltaStat("Concrete", "0.0 m³", Ink, "no change", Modifier.weight(1f))
            DeltaStat("Cost", "+₹4,900", OrangeFg, "+₹2 / sq ft", Modifier.weight(1f))
        }
        T("3 members changed", 13, SemiBold, Ink2)
        ChangeCard("B16, B22", "+46 kg", OrangeFg, "Bot 3-12T", "Bot 3-16T")
        ChangeCard("B44, B45, B53, B54", "+26 kg", OrangeFg, "8T @100/150", "8T @100/125")
        Card(radius = 12.dp, gap = 6.dp) {
            Row {
                T("B26", 15, SemiBold, mono = true, modifier = Modifier.weight(1f))
                T("resolved", 14, SemiBold, GreenFg, mono = true)
            }
            T("Now in the beam schedule. Bars match the values you entered for R0, no quantity change.", 13, color = Ink2)
        }
        NoteRow(Ic.Info, "70 members unchanged. Changes are matched by beam mark, so renumbered beams are flagged for review.")
    }
}

@Composable
private fun DeltaStat(label: String, value: String, color: Color, sub: String, modifier: Modifier) {
    Column(modifier) {
        T(label, 12, color = Muted)
        T(value, 18, SemiBold, color, mono = true, maxLines = 1)
        T(sub, 11, Medium, Muted, mono = true, maxLines = 1)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChangeCard(marks: String, delta: String, deltaColor: Color, from: String, to: String) {
    Card(radius = 12.dp, gap = 6.dp) {
        Row {
            T(marks, 15, SemiBold, mono = true, modifier = Modifier.weight(1f))
            T(delta, 14, SemiBold, deltaColor, mono = true)
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            T(from, 13, color = Gray500, strike = true)
            T("→", 13)
            Box(Modifier.clip(RoundedCornerShape(4.dp)).background(OrangeBg).padding(horizontal = 6.dp, vertical = 1.dp)) {
                T(to, 13, Medium, OrangeFg)
            }
        }
    }
}

// ===================== 9 · Drawing check =====================
@Composable
fun DrawingCheckScreen(nav: NavHostController) {
    val toast = LocalToast.current
    Screen(
        header = { Header("Drawing check", "Bldg 2 · 6 sheets · Structnik + architect", onBack = { nav.back() }) },
        gap = 8.dp,
        bottom = {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SecondaryButton("PDF report", Modifier.weight(1f)) { toast("Demo: export not available") }
                PrimaryButton("Send to consultant", Modifier.weight(1f), fontSize = 15) { toast("Demo: nothing is sent to the consultant") }
            }
        }
    ) {
        Row(Modifier.padding(bottom = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Chip("2 conflicts", OrangeFg, OrangeBg, radius = 14.dp)
            Chip("2 missing", AmberFg, AmberBg, radius = 14.dp)
            Chip("1 minor", Ink2, Gray200, radius = 14.dp)
        }
        CheckCard("Conflict", OrangeFg, OrangeBg, "Beam B26 not in schedule",
            "Shown on ND 41-06 plan between the two stair stub posts. Row \"B48, B51, B52, B59\" lists B59 twice; B26 likely meant.",
            "ND 41-06 · plan + beam schedule")
        CheckCard("Conflict", OrangeFg, OrangeBg, "Sunk depth at WCs",
            "Structural legend: 225 and 250 mm. Architectural Sheet 1: 350 mm sunk.",
            "ND 41-06 legend · Arch Sheet 1")
        CheckCard("Missing", AmberFg, AmberBg, "Clear cover for beams, slabs",
            "Notes give foundation cover (50 mm) only.", "ND 41-06 · note 8")
        CheckCard("Missing", AmberFg, AmberBg, "Slab top bars at supports",
            "Slab schedule gives bottom bars only. Extra top steel not detailed.", "ND 41-06 · slab schedule")
        CheckCard("Minor", Ink2, Gray200, "Taluka differs on title blocks",
            "Khalapur (structural) vs Panvel (architectural).", null)
    }
}

@Composable
private fun CheckCard(tag: String, fg: Color, bg: Color, title: String, body: String, source: String?) {
    Card(radius = 12.dp, gap = 4.dp) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Chip(tag, fg, bg, size = 11, radius = 4.dp)
            T(title, 14, SemiBold, modifier = Modifier.weight(1f))
        }
        T(body, 13, color = Ink2)
        if (source != null) T(source, 11, Medium, Muted, mono = true)
    }
}

// ===================== 10 · Purchase list =====================
@Composable
fun PurchaseListScreen(nav: NavHostController) {
    val toast = LocalToast.current
    var date by remember { mutableStateOf("Tue, 28 Oct 2026") }
    var dealer by remember { mutableIntStateOf(0) }
    Screen(
        header = { Header("Order for next pour", "Bldg 2 · 3rd floor slab + beams", onBack = { nav.back() }) },
        bottom = { PrimaryButton("Request quotes") { toast("Demo: quotes are not requested") } }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            T("Casting date", 13, SemiBold, Ink2)
            Field(date, { date = it }, Modifier.fillMaxWidth(), label = "Casting date")
        }
        Column(Modifier.fillMaxWidth().card().padding(horizontal = 16.dp, vertical = 4.dp)) {
            val rows = listOf(
                "TMT 8 mm" to "2,128 kg",
                "TMT 10 mm" to "169 kg",
                "TMT 12 mm · 28 bars" to "392 kg",
                "TMT 16 mm" to "1,103 kg",
                "Ready-mix M30" to "41.7 m³"
            )
            rows.forEach { (a, b) ->
                Row(Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
                    T(a, 14, modifier = Modifier.weight(1f))
                    T(b, 14, mono = true)
                }
                HDivider()
            }
            Row(Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
                T("or cement, if site-mixed", 14, color = Muted, modifier = Modifier.weight(1f))
                T("317 bags", 14, color = Muted, mono = true)
            }
        }
        T("Quotes from dealers near the site", 13, SemiBold, Ink2)
        DealerCard("Dealer A · Panvel", "Steel delivered in 2 days", "₹2.49 L", true, dealer == 0) { dealer = 0 }
        DealerCard("Dealer B · Navi Mumbai", "Steel delivered in 3 days", "₹2.56 L", false, dealer == 1) { dealer = 1 }
        T("Example quotes. Dealers see quantities and site area only, not your drawings.", 12, color = Muted)
    }
}

@Composable
private fun DealerCard(name: String, sub: String, price: String, lowest: Boolean, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .card(12.dp, if (selected) Orange else Border, if (selected) 2.dp else 1.dp)
            .clickable(role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            T(name, 14, SemiBold)
            T(sub, 12, color = Muted)
        }
        Column(horizontalAlignment = Alignment.End) {
            T(price, 15, SemiBold, mono = true)
            if (lowest) T("Lowest", 12, SemiBold, GreenFg, align = TextAlign.End)
        }
    }
}

// ===================== 11 · Site reconciliation =====================
@Composable
fun SiteReconScreen(nav: NavHostController) {
    val toast = LocalToast.current
    Screen(
        header = { Header("Steel used vs planned", "Bldg 2 · 1st floor slab, cast · example", onBack = { nav.back() }) },
        bottom = { PrimaryButton("Log steel issued", icon = Ic.Plus) { toast("Demo: logging is not available") } }
    ) {
        Row(
            Modifier.fillMaxWidth().card(12.dp, Color(0xFFFCD34D), bg = AmberBg).padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Ico(Ic.Warning, WarnIcon, 20.dp)
            TA(androidx.compose.ui.text.buildAnnotatedString {
                pushStyle(androidx.compose.ui.text.SpanStyle(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold))
                append("8 mm issued 8% over plan.")
                pop()
                append(" 172 kg more than the BBS plus 3% wastage. Check stirrup offcuts and stock at the yard.")
            }, 13, Color(0xFF78350F))
        }
        Card(padH = 16.dp, padV = 14.dp, gap = 12.dp) {
            Row {
                T("By bar size", 13, SemiBold, modifier = Modifier.weight(1f))
                T("planned · issued", 13, color = Muted)
            }
            ReconBar("8 mm", "2,128", "2,300", 0.925f, 1f, over = true)
            ReconBar("16 mm", "1,103", "1,110", 0.48f, 0.483f)
            ReconBar("12 mm", "392", "395", 0.17f, 0.172f)
            ReconBar("10 mm", "169", "168", 0.073f, 0.073f)
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                LegendBar("Planned (BBS)", Gray300)
                LegendBar("Issued from store", Gray500)
            }
        }
        Row(
            Modifier.fillMaxWidth().card(12.dp).padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                T("Issue log", 14, SemiBold)
                T("9 entries · last by site store, photo of challan", 12, color = Muted)
            }
            Box(
                Modifier.clip(RoundedCornerShape(8.dp)).clickable(role = Role.Button) { toast("Demo: issue log is not available") }
                    .padding(horizontal = 12.dp, vertical = 12.dp)
            ) { T("View", 14, SemiBold, Link) }
        }
    }
}

@Composable
private fun ReconBar(barSize: String, planned: String, issued: String, pFrac: Float, iFrac: Float, over: Boolean = false) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row {
            T(barSize, 13, mono = true, modifier = Modifier.weight(1f))
            T("$planned · ", 13, mono = true)
            T(issued, 13, if (over) SemiBold else androidx.compose.ui.text.font.FontWeight.Normal, if (over) OrangeFg else Ink, mono = true)
        }
        Box(Modifier.fillMaxWidth(pFrac).height(8.dp).clip(RoundedCornerShape(4.dp)).background(Gray300))
        Box(Modifier.fillMaxWidth(iFrac).height(8.dp).clip(RoundedCornerShape(4.dp)).background(if (over) Orange else Gray500))
    }
}

@Composable
private fun LegendBar(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(Modifier.size(14.dp, 8.dp).clip(RoundedCornerShape(4.dp)).background(color))
        T(label, 12, color = Muted)
    }
}
