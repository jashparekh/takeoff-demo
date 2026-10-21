package com.jashparekh.takeoff

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import java.util.Locale

/** One building of the sample project. */
data class Building(
    val code: String,        // B1, B2, CH
    val name: String,        // Building 1
    val wings: String?,      // "Wings A, B"
    val tabLabel: String,    // label in the coverage switcher
    val use: String,
    val basements: Int,
    val ground: String,      // Stilt / Ground / Podium
    val upper: Int,
    val terrace: Boolean,
    val oht: Boolean,
    val copies: Int
) {
    val groundShort: String get() = if (ground == "Ground") "G" else ground

    val levelCount: Int
        get() = (if (oht) 1 else 0) + (if (terrace) 1 else 0) + upper + 1 + basements + 1

    val partCount: Int
        get() = (if (oht) 2 else 0) + (if (terrace) (if (oht) 3 else 2) else 0) +
            3 * upper + 3 + 3 * basements + 1

    val subtitle: String
        get() {
            val base = "$use · $groundShort + $upper" + if (oht) " + OHT" else ""
            return when {
                copies > 1 -> "$base · ×$copies identical"
                upper >= 2 -> "$base · $levelCount levels"
                else -> base
            }
        }

    private val firstFloor: Double get() = 0.15 + if (ground == "Stilt") 4.10 else 3.00
    fun floorElev(k: Int): Double = firstFloor + (k - 1) * 2.95
    val terraceElev: Double get() = firstFloor + upper * 2.95
    val ohtElev: Double get() = terraceElev + 4.10

    /** Rows of the coverage grid, top to bottom. */
    fun coverageRows(): List<CovRow> {
        val rows = ArrayList<CovRow>()
        if (oht) rows += CovRow("oht", "OHT / LMR", plus(ohtElev), listOf(0, 1))
        if (terrace) rows += CovRow("terrace", "Terrace", plus(terraceElev), if (oht) listOf(0, 1, 2) else listOf(0, 1))
        for (k in upper downTo 1) rows += CovRow("f$k", "${ordinal(k)} floor", plus(floorElev(k)), listOf(0, 1, 2))
        rows += CovRow("g", "$ground / plinth", "+0.15", listOf(0, 1, 2))
        for (b in 1..basements) rows += CovRow("b$b", "Basement $b", "-" + fmt(3.0 * b), listOf(0, 1, 2))
        rows += CovRow("fdn", "Foundation", null, listOf(3))
        return rows
    }
}

data class CovRow(val key: String, val label: String, val elev: String?, val cols: List<Int>)

data class Drawing(val name: String, val meta: String, val structural: Boolean)

fun fmt(v: Double): String = String.format(Locale.US, "%.2f", v)
fun plus(v: Double): String = "+" + fmt(v)
fun ordinal(n: Int): String {
    val suffix = if (n % 100 in 11..13) "th" else when (n % 10) { 1 -> "st"; 2 -> "nd"; 3 -> "rd"; else -> "th" }
    return "$n$suffix"
}

val ColNames = listOf("slab + beams", "columns", "stair", "footings")

fun sampleBuildings() = listOf(
    Building("B1", "Building 1", "Wings A, B", "B1 · Wings A,B", "Residential", 0, "Stilt", 7, true, true, 2),
    Building("B2", "Building 2", null, "Building 2", "Residential", 0, "Stilt", 4, true, true, 1),
    Building("CH", "Clubhouse", null, "Clubhouse", "Amenity", 0, "Ground", 1, true, false, 1)
)

private val extraDrawingNames = listOf(
    "2nd & 4th Floor Plan" to "A2 · beam + slab schedule found",
    "Terrace & OHT Plan" to "A2 · beam schedule found",
    "Column Layout" to "A1 · column schedule found",
    "Footing Layout" to "A1 · footing schedule found"
)

/** All sample data for the demo. A fresh instance = "Reset demo". */
class DemoState {
    // Project (1a)
    var projectName by mutableStateOf("Shri Basaveshwar Park")
    var city by mutableStateOf("Panvel, MH")
    var consultant by mutableStateOf("Structnik")
    val buildings = mutableStateListOf(*sampleBuildings().toTypedArray())

    // Upload (2)
    var uploadBuilding by mutableIntStateOf(1)
    var uploadScope by mutableIntStateOf(0)
    val drawings = mutableStateListOf(
        Drawing("ND 41-06 1st & 3rd Floor Plan", "A2 · beam + slab schedule found", true),
        Drawing("ND 41-05 Staircase Details", "A3 · beam schedule found", true),
        Drawing("Building-2 Sheet 1 – Plans", "A0 · room dimensions found", false)
    )
    val moreArchSheets = 3
    var nextDrawingNo by mutableIntStateOf(7)
    private var extraAdded = 0

    fun addSampleDrawing() {
        val code = "ND 41-%02d".format(Locale.US, nextDrawingNo)
        val (title, meta) = if (extraAdded < extraDrawingNames.size) extraDrawingNames[extraAdded]
        else "Structural Sheet" to "A2 · schedule found"
        drawings += Drawing("$code $title", meta, true)
        extraAdded++
        nextDrawingNo++
    }

    // Coverage (2a / 2b)
    var coverageBuilding by mutableIntStateOf(1)
    val covered = mutableStateMapOf(
        "B2|f4|2" to "41-05",
        "B2|f3|0" to "41-06",
        "B2|f3|2" to "41-05",
        "B2|f2|2" to "41-05",
        "B2|f1|0" to "41-06",
        "B2|f1|2" to "41-05",
        "B2|g|2" to "41-05"
    )
    var selectedPart by mutableStateOf<String?>("B2|f2|0")

    fun coveredCount(b: Building): Int = covered.keys.count { it.startsWith(b.code + "|") }

    // Values we need (5) — start empty except cement.
    var cement by mutableStateOf("380")
    var sunk by mutableStateOf<String?>(null)
    var coverBeams by mutableStateOf("")
    var coverSlabs by mutableStateOf("")

    val emptyCount: Int
        get() = (if (cement.isBlank()) 1 else 0) + (if (sunk == null) 1 else 0) +
            (if (coverBeams.isBlank() || coverSlabs.isBlank()) 1 else 0)

    // Confirm (5a)
    var grades by mutableStateOf("M30 · Fe550")
    var wasteSteel by mutableStateOf("3")
    var wasteConcrete by mutableStateOf("2")
    var tmtRates by mutableStateOf("₹67/66/65/64")
    var rmcRates by mutableStateOf("₹6,683 · ₹450")
    var floorArea by mutableStateOf("2,145 sq ft")
    var valuesChecked by mutableStateOf(false)
}

class DemoViewModel : ViewModel() {
    var s by mutableStateOf(DemoState())
        private set

    fun reset() {
        s = DemoState()
    }
}
