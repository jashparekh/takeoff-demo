import SwiftUI

/// A tiny painter's-algorithm renderer for axis-aligned boxes (slabs, beams, columns).
/// Pure 2D projection: cheap enough for old phones, no 3D engine.
struct IsoBox {
    var x0: Double, y0: Double, x1: Double, y1: Double, z0: Double, z1: Double
    var top: Color
    var sideX: Color   // faces whose normal is ±x
    var sideY: Color   // faces whose normal is ±y
    var tag: String? = nil
    var layer: Int = 0
}

struct RebarSpec {
    var tag: String
    var spacing: Double = 0.15
    var edgesOnly: Bool = false
    var color: Color = T.accent
}

struct IsoCamera {
    var yaw: Double
    var elevation: Double
    var zoom: Double = 1

    func rotate(_ x: Double, _ y: Double) -> (Double, Double) {
        let c = cos(yaw), s = sin(yaw)
        return (x * c - y * s, x * s + y * c)
    }
}

struct IsoScene {
    let boxes: [IsoBox]
    let camera: IsoCamera
    let size: CGSize
    private let cx: Double
    private let cy: Double
    private let cz: Double
    private let s: Double

    init(boxes: [IsoBox], camera: IsoCamera, size: CGSize, padding: Double = 18) {
        self.boxes = boxes
        self.camera = camera
        self.size = size
        var minX = Double.greatestFiniteMagnitude, maxX = -Double.greatestFiniteMagnitude
        var minY = Double.greatestFiniteMagnitude, maxY = -Double.greatestFiniteMagnitude
        var minZ = Double.greatestFiniteMagnitude, maxZ = -Double.greatestFiniteMagnitude
        for b in boxes {
            minX = min(minX, b.x0); maxX = max(maxX, b.x1)
            minY = min(minY, b.y0); maxY = max(maxY, b.y1)
            minZ = min(minZ, b.z0); maxZ = max(maxZ, b.z1)
        }
        if boxes.isEmpty { minX = 0; maxX = 1; minY = 0; maxY = 1; minZ = 0; maxZ = 1 }
        let ccx = (minX + maxX) / 2, ccy = (minY + maxY) / 2, ccz = (minZ + maxZ) / 2
        var r = 0.0
        var zr = 0.0
        for b in boxes {
            r = max(r, hypot(b.x0 - ccx, b.y0 - ccy), hypot(b.x1 - ccx, b.y0 - ccy),
                    hypot(b.x0 - ccx, b.y1 - ccy), hypot(b.x1 - ccx, b.y1 - ccy))
            zr = max(zr, abs(b.z0 - ccz), abs(b.z1 - ccz))
        }
        let w = Double(size.width) - 2 * padding
        let h = Double(size.height) - 2 * padding
        let el = camera.elevation
        let needW = max(0.001, 2 * r)
        let needH = max(0.001, 2 * r * sin(el) + 2 * zr * cos(el))
        self.cx = ccx
        self.cy = ccy
        self.cz = ccz
        self.s = max(0.001, min(w / needW, h / needH)) * camera.zoom
    }

    /// World -> screen point.
    func project(_ x: Double, _ y: Double, _ z: Double) -> CGPoint {
        let (X, Y) = camera.rotate(x - cx, y - cy)
        let el = camera.elevation
        let u = X
        let v = Y * sin(el) - (z - cz) * cos(el)
        return CGPoint(x: Double(size.width) / 2 + u * s, y: Double(size.height) / 2 + v * s)
    }

    private func depth(_ b: IsoBox) -> Double {
        let (_, Y) = camera.rotate((b.x0 + b.x1) / 2, (b.y0 + b.y1) / 2)
        let el = camera.elevation
        return Y * cos(el) + ((b.z0 + b.z1) / 2) * sin(el)
    }

    func topPath(_ b: IsoBox) -> Path {
        var p = Path()
        p.move(to: project(b.x0, b.y0, b.z1))
        p.addLine(to: project(b.x1, b.y0, b.z1))
        p.addLine(to: project(b.x1, b.y1, b.z1))
        p.addLine(to: project(b.x0, b.y1, b.z1))
        p.closeSubpath()
        return p
    }

    func sortedBoxes() -> [IsoBox] {
        boxes.sorted { a, b in
            if a.layer != b.layer { return a.layer < b.layer }
            return depth(a) < depth(b)
        }
    }

    func draw(in ctx: GraphicsContext, selectedTag: String?, selectedColors: (Color, Color, Color)?,
              rebar: RebarSpec?, lineColor: Color = Color(hex: 0x374151)) {
        let showSides = camera.elevation < 1.45
        for b in sortedBoxes() {
            var top = b.top, sx = b.sideX, sy = b.sideY
            if let t = selectedTag, b.tag == t, let sc = selectedColors {
                top = sc.0; sx = sc.1; sy = sc.2
            }
            if showSides {
                // faces: (normal x, normal y, corner points)
                let faces: [(Double, Double, [(Double, Double)])] = [
                    (0, -1, [(b.x0, b.y0), (b.x1, b.y0)]),
                    (0, 1, [(b.x1, b.y1), (b.x0, b.y1)]),
                    (-1, 0, [(b.x0, b.y1), (b.x0, b.y0)]),
                    (1, 0, [(b.x1, b.y0), (b.x1, b.y1)]),
                ]
                for f in faces {
                    let (_, ny) = camera.rotate(f.0, f.1)
                    if ny <= 0.0001 { continue }
                    var p = Path()
                    let a = f.2[0], c = f.2[1]
                    p.move(to: project(a.0, a.1, b.z1))
                    p.addLine(to: project(c.0, c.1, b.z1))
                    p.addLine(to: project(c.0, c.1, b.z0))
                    p.addLine(to: project(a.0, a.1, b.z0))
                    p.closeSubpath()
                    ctx.fill(p, with: .color(f.0 != 0 ? sx : sy))
                    ctx.stroke(p, with: .color(lineColor), lineWidth: 0.5)
                }
            }
            let tp = topPath(b)
            ctx.fill(tp, with: .color(top))
            ctx.stroke(tp, with: .color(lineColor), lineWidth: 0.5)

            if let r = rebar, b.tag == r.tag {
                drawRebar(ctx, b, r)
            }
        }
    }

    private func drawRebar(_ ctx: GraphicsContext, _ b: IsoBox, _ r: RebarSpec) {
        let inset = 0.06
        let z = b.z1
        let xa = b.x0 + inset, xb = b.x1 - inset
        let ya = b.y0 + inset, yb = b.y1 - inset
        var p = Path()
        let lenX = xb - xa, lenY = yb - ya
        // bars parallel to y (spaced along x)
        var x = xa
        while x <= xb + 0.0001 {
            if r.edgesOnly {
                p.move(to: project(x, ya, z)); p.addLine(to: project(x, ya + lenY * 0.25, z))
                p.move(to: project(x, yb - lenY * 0.25, z)); p.addLine(to: project(x, yb, z))
            } else {
                p.move(to: project(x, ya, z)); p.addLine(to: project(x, yb, z))
            }
            x += r.spacing
        }
        var y = ya
        while y <= yb + 0.0001 {
            if r.edgesOnly {
                p.move(to: project(xa, y, z)); p.addLine(to: project(xa + lenX * 0.25, y, z))
                p.move(to: project(xb - lenX * 0.25, y, z)); p.addLine(to: project(xb, y, z))
            } else {
                p.move(to: project(xa, y, z)); p.addLine(to: project(xb, y, z))
            }
            y += r.spacing
        }
        ctx.stroke(p, with: .color(r.color), lineWidth: 0.8)
    }

    /// Tag of the top-most slab under the point.
    func hitTag(at point: CGPoint, layer: Int) -> String? {
        for b in sortedBoxes().reversed() where b.layer == layer && b.tag != nil {
            if topPath(b).contains(point) { return b.tag }
        }
        return nil
    }
}
