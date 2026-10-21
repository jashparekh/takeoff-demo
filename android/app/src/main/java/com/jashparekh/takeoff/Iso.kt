package com.jashparekh.takeoff

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import java.util.Locale
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Very small 2D projector: plan (x, y in metres, y down) + height z.
 * iso = true gives a classic 30° isometric view rotated by [yawDeg]; otherwise a top-down plan.
 * No 3D engine: faces are painted back-to-front, which is cheap on old phones.
 */
class Proj(
    val iso: Boolean,
    yawDeg: Float,
    val scale: Float,
    private val cx: Float,
    private val cy: Float,
    private val px: Float,
    private val py: Float
) {
    private val c = cos(Math.toRadians(yawDeg.toDouble())).toFloat()
    private val s = sin(Math.toRadians(yawDeg.toDouble())).toFloat()

    private fun rx(x: Float, y: Float) = (x - px) * c - (y - py) * s
    private fun ry(x: Float, y: Float) = (x - px) * s + (y - py) * c

    fun pt(x: Float, y: Float, z: Float = 0f): Offset {
        val a = rx(x, y)
        val b = ry(x, y)
        return if (iso) Offset((a - b) * 0.866f * scale + cx, (a + b) * 0.5f * scale - z * scale + cy)
        else Offset(a * scale + cx, b * scale + cy)
    }

    /** Larger = closer to the viewer. */
    fun depth(x: Float, y: Float) = rx(x, y) + ry(x, y)

    fun faceVisible(nx: Float, ny: Float): Boolean {
        val a = nx * c - ny * s
        val b = nx * s + ny * c
        return a + b > 0.001f
    }

    fun faceIsRight(nx: Float, ny: Float): Boolean {
        val a = nx * c - ny * s
        val b = nx * s + ny * c
        return a > b
    }
}

fun polyPath(pts: List<Offset>): Path = Path().apply {
    moveTo(pts[0].x, pts[0].y)
    for (i in 1 until pts.size) lineTo(pts[i].x, pts[i].y)
    close()
}

fun DrawScope.poly(pts: List<Offset>, fill: Color, stroke: Color? = null, strokeWidth: Float = 1f) {
    val p = polyPath(pts)
    drawPath(p, fill)
    if (stroke != null) drawPath(p, stroke, style = Stroke(width = strokeWidth))
}

/** Draws an axis-aligned box: visible side faces first, then the top. */
fun DrawScope.box(
    pr: Proj,
    x0: Float, y0: Float, x1: Float, y1: Float, z0: Float, z1: Float,
    top: Color, left: Color, right: Color, outline: Color? = null
) {
    if (pr.iso) {
        // (normal x, normal y, edge start, edge end)
        val faces = arrayOf(
            floatArrayOf(1f, 0f, x1, y0, x1, y1),
            floatArrayOf(-1f, 0f, x0, y1, x0, y0),
            floatArrayOf(0f, 1f, x1, y1, x0, y1),
            floatArrayOf(0f, -1f, x0, y0, x1, y0)
        )
        for (f in faces) {
            if (!pr.faceVisible(f[0], f[1])) continue
            val pts = listOf(pr.pt(f[2], f[3], z0), pr.pt(f[4], f[5], z0), pr.pt(f[4], f[5], z1), pr.pt(f[2], f[3], z1))
            poly(pts, if (pr.faceIsRight(f[0], f[1])) right else left, outline, 1f)
        }
    }
    poly(topFace(pr, x0, y0, x1, y1, z1), top, outline, 1f)
}

fun topFace(pr: Proj, x0: Float, y0: Float, x1: Float, y1: Float, z: Float) =
    listOf(pr.pt(x0, y0, z), pr.pt(x1, y0, z), pr.pt(x1, y1, z), pr.pt(x0, y1, z))

fun insidePoly(p: Offset, poly: List<Offset>): Boolean {
    var inside = false
    var j = poly.size - 1
    for (i in poly.indices) {
        val a = poly[i]
        val b = poly[j]
        if ((a.y > p.y) != (b.y > p.y) && p.x < (b.x - a.x) * (p.y - a.y) / (b.y - a.y) + a.x) inside = !inside
        j = i
    }
    return inside
}

// ===================== Sample typical floor =====================
data class Panel(
    val id: String, val type: Int, val room: String, val flat: String?,
    val x: Float, val y: Float, val w: Float, val h: Float
) {
    val title: String get() = "S$type · $room" + (flat?.let { ", Flat $it" } ?: "")
    val topZ: Float get() = if (type == 3) -0.25f else 0f
    val thk: Int get() = if (type == 2) 150 else 125
    private val clearW get() = ((w - 0.23f) * 1000).roundToInt()
    private val clearH get() = ((h - 0.23f) * 1000).roundToInt()
    val concrete: Double get() = clearW / 1000.0 * clearH / 1000.0 * thk / 1000.0
    val steelKg: Int get() = (concrete * (if (type == 2) 62.0 else 47.8)).roundToInt()
    val same: Int get() = if (flat != null) 4 else 1
    val spec: String
        get() = "${group(clearW)} × ${group(clearH)} mm clear · $thk thk · " +
            (if (type == 2) "10T @150 both ways" else "8T @150 both ways")
    val concreteText: String get() = String.format(Locale.US, "%.2f m³", concrete)
}

fun group(n: Int): String = String.format(Locale.US, "%,d", n)

private class WingRoom(val u: Float, val v: Float, val du: Float, val dv: Float, val name: String, val type: Int)

data class Seg(val x0: Float, val y0: Float, val x1: Float, val y1: Float)

object FloorPlan {
    const val PX = 4f
    const val PY = 4f
    val panels: List<Panel>
    val beams: List<Seg>
    val columns: List<Offset>

    init {
        val list = ArrayList<Panel>()
        listOf("Lobby", "Stair", "Lift + duct", "Passage").forEachIndexed { i, n ->
            list += Panel("core$i", 2, n, null, (i % 2) * 4f, (i / 2) * 4f, 4f, 4f)
        }
        // Wing template: u across (0..8), v outward from the core.
        val tpl = listOf(
            WingRoom(0f, 0f, 2.871f, 4.73f, "Living room", 1),
            WingRoom(2.871f, 0f, 5.129f, 4.73f, "Bedroom", 1),
            WingRoom(0f, 4.73f, 4f, 3.27f, "Kitchen", 1),
            WingRoom(4f, 4.73f, 4f, 3.27f, "Bedroom 2", 1),
            WingRoom(0f, 8f, 4f, 1.5f, "WC 1", 3),
            WingRoom(4f, 8f, 4f, 1.5f, "WC 2", 3)
        )
        listOf("A", "B", "C", "D").forEachIndexed { wi, flat ->
            tpl.forEachIndexed { ri, r ->
                val (x, y, w, h) = when (wi) {
                    0 -> listOf(r.u, -(r.v + r.dv), r.du, r.dv)           // north
                    1 -> listOf(8f + r.v, r.u, r.dv, r.du)                 // east
                    2 -> listOf(8f - r.u - r.du, 8f + r.v, r.du, r.dv)     // south
                    else -> listOf(-(r.v + r.dv), 8f - r.u - r.du, r.dv, r.du) // west
                }
                list += Panel("$flat$ri", r.type, r.name, flat, x, y, w, h)
            }
        }
        panels = list

        val segKeys = HashSet<String>()
        val segs = ArrayList<Seg>()
        val colKeys = HashSet<String>()
        val cols = ArrayList<Offset>()
        fun k(v: Float) = (v * 100).roundToInt()
        fun addSeg(a: Float, b: Float, c: Float, d: Float) {
            val key = if (k(a) < k(c) || (k(a) == k(c) && k(b) <= k(d))) "${k(a)},${k(b)},${k(c)},${k(d)}" else "${k(c)},${k(d)},${k(a)},${k(b)}"
            if (segKeys.add(key)) segs += Seg(a, b, c, d)
        }
        fun addCol(x: Float, y: Float) {
            if (colKeys.add("${k(x)},${k(y)}")) cols += Offset(x, y)
        }
        for (p in panels) {
            addSeg(p.x, p.y, p.x + p.w, p.y)
            addSeg(p.x, p.y + p.h, p.x + p.w, p.y + p.h)
            addSeg(p.x, p.y, p.x, p.y + p.h)
            addSeg(p.x + p.w, p.y, p.x + p.w, p.y + p.h)
            if (p.type != 3) {
                addCol(p.x, p.y); addCol(p.x + p.w, p.y); addCol(p.x, p.y + p.h); addCol(p.x + p.w, p.y + p.h)
            }
        }
        beams = segs
        columns = cols
    }

    fun proj(mode: Int, yaw: Float, zoom: Float, w: Float, h: Float): Proj =
        if (mode == 1) Proj(true, yaw, w / 50f * zoom, w / 2f, h * 0.52f, PX, PY)
        else Proj(false, yaw, min(w, h) / 29f * zoom, w / 2f, h / 2f, PX, PY)

    fun slabColors(p: Panel, selected: Boolean): Triple<Color, Color, Color> = when {
        selected -> Triple(Color(0xFFC2410C), Color(0xFF9A3412), Color(0xFF7C2D12))
        p.type == 2 -> Triple(Color(0xFFA9C4E4), Color(0xFF8FAED3), Color(0xFF7C9CC3))
        p.type == 3 -> Triple(Color(0xFFF4C9A6), Color(0xFFE3B088), Color(0xFFD89C6E))
        else -> Triple(Color(0xFFDCE3EC), Color(0xFFC3CCD8), Color(0xFFAAB4C2))
    }

    /** Draws the floor. mode 0 = plan, 1 = 3D, 2 = drawing. */
    fun DrawScope.drawFloor(
        mode: Int, pr: Proj, slabs: Boolean, beamsOn: Boolean, colsOn: Boolean, rebar: Boolean, selected: String?
    ) {
        val sc = pr.scale
        if (mode == 1) {
            if (colsOn) {
                for (c in columns.sortedBy { pr.depth(it.x, it.y) }) {
                    box(pr, c.x - 0.15f, c.y - 0.15f, c.x + 0.15f, c.y + 0.15f, -3f, -0.6f,
                        Color(0xFF4B5563), Color(0xFF9CA3AF), Color(0xFF6B7280))
                }
            }
            if (beamsOn) {
                val zTop = if (slabs) -0.15f else 0f
                for (b in beams.sortedBy { pr.depth((it.x0 + it.x1) / 2, (it.y0 + it.y1) / 2) }) {
                    val x0 = minOf(b.x0, b.x1) - 0.115f
                    val x1 = maxOf(b.x0, b.x1) + 0.115f
                    val y0 = minOf(b.y0, b.y1) - 0.115f
                    val y1 = maxOf(b.y0, b.y1) + 0.115f
                    box(pr, x0, y0, x1, y1, -0.6f, zTop, Color(0xFF374151), Color(0xFF6B7280), Color(0xFF4B5563))
                }
            }
            if (slabs) {
                for (p in panels.sortedBy { pr.depth(it.x + it.w / 2, it.y + it.h / 2) }) {
                    val (top, left, right) = slabColors(p, p.id == selected)
                    box(pr, p.x, p.y, p.x + p.w, p.y + p.h, p.topZ - 0.15f, p.topZ, top, left, right, Color(0x994B5563))
                    if (rebar) drawBars(pr, p)
                }
            } else if (rebar) {
                for (p in panels) drawBars(pr, p)
            }
        } else {
            val drawing = mode == 2
            if (slabs) {
                for (p in panels) {
                    val pts = topFace(pr, p.x, p.y, p.x + p.w, p.y + p.h, 0f)
                    if (drawing) poly(pts, Color.White, Color(0xFF9CA3AF), 1.5f)
                    else poly(pts, slabColors(p, p.id == selected).first, Color(0xFF6B7280), 1f)
                    if (rebar) drawBars(pr, p)
                }
            }
            if (beamsOn) {
                val bw = maxOf(2f, 0.23f * sc)
                val col = if (drawing) Color(0xFFD9622B) else Color(0xFF374151)
                for (b in beams) drawLine(col, pr.pt(b.x0, b.y0), pr.pt(b.x1, b.y1), bw)
            }
            if (colsOn) {
                val half = maxOf(2f, 0.2f * sc)
                for (c in columns) {
                    val o = pr.pt(c.x, c.y)
                    drawRect(Color(0xFF6B7280), Offset(o.x - half, o.y - half), androidx.compose.ui.geometry.Size(half * 2, half * 2))
                }
            }
            if (drawing && selected != null) {
                panels.firstOrNull { it.id == selected }?.let { p ->
                    poly(topFace(pr, p.x, p.y, p.x + p.w, p.y + p.h, 0f), Color(0x33C2410C), Color(0xFFC2410C), 3f)
                }
            }
        }
    }

    private fun DrawScope.drawBars(pr: Proj, p: Panel) {
        val step = 0.5f
        val z = p.topZ
        val col = Color(0xCCC2410C)
        var x = p.x + 0.25f
        while (x < p.x + p.w) {
            drawLine(col, pr.pt(x, p.y + 0.1f, z), pr.pt(x, p.y + p.h - 0.1f, z), 1f)
            x += step
        }
        var y = p.y + 0.25f
        while (y < p.y + p.h) {
            drawLine(col, pr.pt(p.x + 0.1f, y, z), pr.pt(p.x + p.w - 0.1f, y, z), 1f)
            y += step
        }
    }

    /** Nearest slab under a tap, or null. */
    fun hit(mode: Int, pr: Proj, tap: Offset): String? {
        val order = if (mode == 1) panels.sortedByDescending { pr.depth(it.x + it.w / 2, it.y + it.h / 2) } else panels
        for (p in order) {
            val z = if (mode == 1) p.topZ else 0f
            if (insidePoly(tap, topFace(pr, p.x, p.y, p.x + p.w, p.y + p.h, z))) return p.id
        }
        return null
    }
}
