import SwiftUI

// MARK: - 4 Review members

struct Member: Identifiable {
    let id: String
    let value: String
    let size: String
    let bars: String
    var warning: String? = nil
}

struct ReviewScreen: View {
    @EnvironmentObject var app: AppState
    @State var tab = 0

    let beams: [Member] = [
        Member(id: "B26", value: "58.9 kg", size: "230×600 · span 3,401 mm · ×1",
               bars: "Bot 2-16T + 2-10T · Top 2-16T + 2-16T extra · 8T @100/150",
               warning: "Not in the beam schedule. Using row B48, B51, B52."),
        Member(id: "B1, B3, B4 +5", value: "21.7 kg ea", size: "230×600 · span 2,641 mm · ×8",
               bars: "Bot 2-12T · Top 2-10T · 8T @150/200"),
        Member(id: "B7, B10, B28, B31", value: "70.9 kg ea", size: "230×600 · span 5,072 mm (2 bays) · ×4",
               bars: "Bot 2-16T + 2-10T · Top 2-12T + extras · 8T @100/150"),
        Member(id: "B12, B13, B25, B27", value: "79.5 kg ea", size: "230×600 · span 4,802 mm (2 bays) · ×4",
               bars: "Bot 2-16T + 2-12T · Top 2-16T + 2-16T end · 8T @100/150"),
        Member(id: "B16, B22", value: "68.9 kg ea", size: "230×600 · span 5,602 mm · ×2",
               bars: "Bot 3-12T · Top 3-12T · 8T @100/150"),
    ]
    let slabs: [Member] = [
        Member(id: "S1 · Living room", value: "71 kg ea", size: "2,641 × 4,500 mm · 125 thk · ×4", bars: "8T @150 both ways"),
        Member(id: "S1 · Bedroom", value: "78 kg ea", size: "3,000 × 4,500 mm · 125 thk · ×4", bars: "8T @150 both ways"),
        Member(id: "S2 · Lift lobby, stair", value: "129 kg ea", size: "3,000 × 6,000 mm · 150 thk · ×2", bars: "10T @150 both ways"),
        Member(id: "S3 · WC (sunk)", value: "21 kg ea", size: "1,500 × 3,250 mm · 150 thk · ×4", bars: "8T @150 · sunk depth to confirm"),
    ]
    let chajjas: [Member] = [
        Member(id: "CH1 · Window", value: "6.2 kg ea", size: "600 wide · 1,500 long · ×5", bars: "8T @150 main · 8T @200 dist"),
        Member(id: "CH2 · Balcony", value: "14.8 kg ea", size: "900 wide · 2,400 long · ×2", bars: "10T @150 main · 8T @200 dist"),
    ]

    var body: some View {
        let list = tab == 0 ? beams : (tab == 1 ? slabs : chajjas)
        let footer = tab == 0 ? ("Beams so far", "2,717 kg · 21.7 m³") :
            (tab == 1 ? ("Slabs so far", "963 kg · 18.4 m³") : ("Chajjas so far", "61 kg · 1.6 m³"))

        VStack(spacing: 0) {
            ScreenHeader(title: "Review members", trailing: AnyView(
                Button {
                    app.push(.floor3D)
                } label: {
                    HStack(spacing: 6) {
                        Image(systemName: "cube").font(.system(size: 16, weight: .semibold))
                        Text("3D").font(.sans(15, .semibold))
                    }
                    .foregroundColor(T.ink)
                    .padding(.horizontal, 14)
                    .frame(height: 44)
                    .background(Capsule().fill(Color.white))
                    .overlay(Capsule().strokeBorder(T.gray300, lineWidth: 1))
                    .contentShape(Rectangle())
                }
                .buttonStyle(PressStyle())
                .padding(.trailing, 8)
                .accessibilityLabel("3D view")
            ))
            HStack(spacing: 6) {
                tabButton(0, "Beams 77")
                tabButton(1, "Slabs 9")
                tabButton(2, "Chajjas 7")
            }
            .padding(.horizontal, 20).padding(.bottom, 10)
            HStack(spacing: 8) {
                if tab == 0 {
                    Chip(text: "1 needs a check", fg: T.amberFg, bg: T.amberBg, size: 12, hPad: 10, vPad: 6, radius: 14)
                }
                Text("Tap any member to open it").font(.sans(13)).foregroundColor(T.text2)
                Spacer()
            }
            .padding(.horizontal, 20).padding(.bottom, 12)

            ScrollView {
                VStack(spacing: 8) {
                    ForEach(list) { m in
                        Button {
                            app.push(.slabDetail)
                        } label: {
                            memberCard(m)
                        }
                        .buttonStyle(PressStyle())
                    }
                }
                .padding(.horizontal, 20)
                .padding(.bottom, 16)
            }

            HStack(spacing: 12) {
                VStack(alignment: .leading, spacing: 2) {
                    Text(footer.0).font(.sans(12)).foregroundColor(T.text2)
                    Text(footer.1).font(.mono(17)).foregroundColor(T.ink).lineLimit(1).minimumScaleFactor(0.8)
                }
                Spacer()
                Button {
                    app.push(.issues)
                } label: {
                    Text("Next").font(.sans(16, .semibold)).foregroundColor(.white)
                        .padding(.horizontal, 28)
                        .frame(height: 50)
                        .background(RoundedRectangle(cornerRadius: 12).fill(T.accent))
                }
                .buttonStyle(PressStyle())
            }
            .padding(.horizontal, 20)
            .padding(.top, 12)
            .padding(.bottom, 12)
            .background(Color.white.ignoresSafeArea(edges: .bottom))
            .overlay(Rectangle().fill(T.border).frame(height: 1), alignment: .top)
        }
        .background(T.bg.ignoresSafeArea())
        .toolbar(.hidden, for: .navigationBar)
    }

    func tabButton(_ i: Int, _ title: String) -> some View {
        Button {
            tab = i
        } label: {
            Text(title)
                .font(.sans(14, tab == i ? .semibold : .medium))
                .foregroundColor(tab == i ? .white : T.text3)
                .frame(maxWidth: .infinity)
                .frame(height: 44)
                .background(RoundedRectangle(cornerRadius: 10).fill(tab == i ? T.ink : Color.white))
                .overlay(RoundedRectangle(cornerRadius: 10).strokeBorder(tab == i ? T.ink : T.gray300, lineWidth: 1))
                .contentShape(Rectangle())
        }
        .buttonStyle(PressStyle())
    }

    func memberCard(_ m: Member) -> some View {
        VStack(alignment: .leading, spacing: 5) {
            HStack(alignment: .firstTextBaseline) {
                Text(m.id).font(.mono(15, .semibold)).foregroundColor(T.ink)
                Spacer(minLength: 6)
                Text(m.value).font(.mono(14)).foregroundColor(T.ink)
            }
            Text(m.size).font(.sans(14)).foregroundColor(T.text3)
            Text(m.bars).font(.sans(14)).foregroundColor(m.warning == nil ? T.text2 : T.text3)
                .fixedSize(horizontal: false, vertical: true)
            if let w = m.warning {
                HStack(alignment: .top, spacing: 6) {
                    Image(systemName: "exclamationmark.triangle").font(.system(size: 13))
                    Text(w).font(.sans(13)).fixedSize(horizontal: false, vertical: true)
                }
                .foregroundColor(T.amberFg)
            }
        }
        .multilineTextAlignment(.leading)
        .padding(.horizontal, 14).padding(.vertical, 12)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(RoundedRectangle(cornerRadius: 12).fill(Color.white))
        .overlay(RoundedRectangle(cornerRadius: 12).strokeBorder(m.warning == nil ? T.border : T.amberBorder, lineWidth: m.warning == nil ? 1 : 2))
        .contentShape(Rectangle())
    }
}

// MARK: - 4a 3D floor view

struct SlabInfo {
    let title: String
    let detail: String
    let steel: String
    let concrete: String
    let same: String
}

enum FloorModel {
    static let s1 = (Color(hex: 0xDCE3EC), Color(hex: 0xC3CDD9), Color(hex: 0xAEB9C7))
    static let s2 = (Color(hex: 0xA9C4E4), Color(hex: 0x93B1D4), Color(hex: 0x7C9CC3))
    static let s3 = (Color(hex: 0xF4C9A6), Color(hex: 0xE6B48B), Color(hex: 0xD89C6E))
    static let sel = (Color(hex: 0xC2410C), Color(hex: 0xA33709), Color(hex: 0x8A2E08))
    static let beam = (Color(hex: 0x6B7280), Color(hex: 0x525A66), Color(hex: 0x4B5563))
    static let column = (Color(hex: 0x9CA3AF), Color(hex: 0x7B828E), Color(hex: 0x6B7280))

    struct Panel {
        let tag: String
        let kind: Int // 1, 2, 3
        let x0: Double, y0: Double, x1: Double, y1: Double
        let info: SlabInfo
    }

    static let flats = ["A", "B", "C", "D"]

    static let panels: [Panel] = {
        var out: [Panel] = []
        out.append(Panel(tag: "core-1", kind: 2, x0: -3, y0: -3, x1: 0, y1: 3,
                         info: SlabInfo(title: "S2 · Lift lobby", detail: "3,000 × 6,000 mm · 150 thk · 10T @150 both ways",
                                        steel: "129 kg", concrete: "2.70 m³", same: "×1")))
        out.append(Panel(tag: "core-2", kind: 2, x0: 0, y0: -3, x1: 3, y1: 3,
                         info: SlabInfo(title: "S2 · Stair passage", detail: "3,000 × 6,000 mm · 150 thk · 10T @150 both ways",
                                        steel: "129 kg", concrete: "2.70 m³", same: "×1")))
        // North wing in local coordinates; rotated for the other flats.
        let local: [(String, Int, Double, Double, Double, Double)] = [
            ("living", 1, -3.5, -7.5, 0, -3),
            ("bed", 1, 0, -7.5, 3.5, -3),
            ("wc", 3, -3.5, -9, 0, -7.5),
        ]
        for (fi, flat) in FloorModel.flats.enumerated() {
            for l in local {
                let a = FloorModel.rot(l.2, l.3, fi), b = FloorModel.rot(l.4, l.5, fi)
                let info: SlabInfo
                switch l.0 {
                case "living":
                    info = SlabInfo(title: "S1 · Living room, Flat \(flat)", detail: "2,641 × 4,500 mm clear · 125 thk · 8T @150 both ways",
                                    steel: "71 kg", concrete: "1.49 m³", same: "×4")
                case "bed":
                    info = SlabInfo(title: "S1 · Bedroom, Flat \(flat)", detail: "3,000 × 4,500 mm clear · 125 thk · 8T @150 both ways",
                                    steel: "78 kg", concrete: "1.69 m³", same: "×4")
                default:
                    info = SlabInfo(title: "S3 · WC (sunk), Flat \(flat)", detail: "1,500 × 3,250 mm · 150 thk · sunk 225/250 · 8T @150",
                                    steel: "21 kg", concrete: "0.73 m³", same: "×4")
                }
                out.append(Panel(tag: "\(flat)-\(l.0)", kind: l.1,
                                 x0: min(a.0, b.0), y0: min(a.1, b.1), x1: max(a.0, b.0), y1: max(a.1, b.1), info: info))
            }
        }
        return out
    }()

    static func rot(_ x: Double, _ y: Double, _ quarter: Int) -> (Double, Double) {
        switch quarter % 4 {
        case 1: return (-y, x)
        case 2: return (-x, -y)
        case 3: return (y, -x)
        default: return (x, y)
        }
    }

    static func boxes(slabs: Bool, beams: Bool, columns: Bool) -> [IsoBox] {
        var out: [IsoBox] = []
        let beamW = 0.23, beamD = 0.6, slabT = 0.18
        var colPoints = Set<String>()
        for p in panels {
            if slabs {
                let c = p.kind == 2 ? s2 : (p.kind == 3 ? s3 : s1)
                let zTop = p.kind == 3 ? -0.08 : 0.0
                out.append(IsoBox(x0: p.x0, y0: p.y0, x1: p.x1, y1: p.y1, z0: zTop - slabT, z1: zTop,
                                  top: c.0, sideX: c.1, sideY: c.2, tag: p.tag, layer: 2))
            }
            if beams {
                let bz0 = -slabT - beamD, bz1 = -slabT
                let h = beamW / 2
                out.append(IsoBox(x0: p.x0, y0: p.y0 - h, x1: p.x1, y1: p.y0 + h, z0: bz0, z1: bz1, top: beam.0, sideX: beam.1, sideY: beam.2, layer: 1))
                out.append(IsoBox(x0: p.x0, y0: p.y1 - h, x1: p.x1, y1: p.y1 + h, z0: bz0, z1: bz1, top: beam.0, sideX: beam.1, sideY: beam.2, layer: 1))
                out.append(IsoBox(x0: p.x0 - h, y0: p.y0, x1: p.x0 + h, y1: p.y1, z0: bz0, z1: bz1, top: beam.0, sideX: beam.1, sideY: beam.2, layer: 1))
                out.append(IsoBox(x0: p.x1 - h, y0: p.y0, x1: p.x1 + h, y1: p.y1, z0: bz0, z1: bz1, top: beam.0, sideX: beam.1, sideY: beam.2, layer: 1))
            }
            if columns {
                for (x, y) in [(p.x0, p.y0), (p.x1, p.y0), (p.x0, p.y1), (p.x1, p.y1)] {
                    let key = "\(Int((x * 10).rounded())),\(Int((y * 10).rounded()))"
                    if colPoints.contains(key) { continue }
                    colPoints.insert(key)
                    out.append(IsoBox(x0: x - 0.15, y0: y - 0.3, x1: x + 0.15, y1: y + 0.3, z0: -2.0, z1: -slabT - beamD,
                                      top: column.0, sideX: column.1, sideY: column.2, layer: 0))
                }
            }
        }
        return out
    }
}

struct LegendTag: View {
    let color: Color
    let border: Color
    let text: String
    var body: some View {
        HStack(spacing: 5) {
            RoundedRectangle(cornerRadius: 2).fill(color)
                .overlay(RoundedRectangle(cornerRadius: 2).strokeBorder(border, lineWidth: 1))
                .frame(width: 10, height: 10)
            Text(text).font(.sans(12)).foregroundColor(T.text3)
        }
        .padding(.horizontal, 7).padding(.vertical, 3)
        .background(RoundedRectangle(cornerRadius: 6).fill(Color.white))
        .overlay(RoundedRectangle(cornerRadius: 6).strokeBorder(T.border, lineWidth: 1))
    }
}

struct Floor3DScreen: View {
    @EnvironmentObject var app: AppState
    @State var mode = 1
    @State var yaw = Double.pi / 4
    @State var baseYaw = Double.pi / 4
    @State var zoom = 1.0
    @State var baseZoom = 1.0
    @State var selectedTag = "A-living"
    @State var showSlabs = true
    @State var showBeams = true
    @State var showColumns = true
    @State var showRebar = false

    var body: some View {
        let info = FloorModel.panels.first(where: { $0.tag == selectedTag })?.info ?? FloorModel.panels[2].info
        VStack(spacing: 0) {
            ScreenHeader(title: "Typical floor · 3D", subtitle: "Built from ND 41-06 · 28 slab panels · 77 beams")
            Segmented(options: ["Plan", "3D", "Drawing"], selection: $mode)
                .padding(.horizontal, 20).padding(.top, 4).padding(.bottom, 8)

            ZStack(alignment: .topLeading) {
                if mode == 2 {
                    PlanDrawing(progress: 1).padding(10)
                } else {
                    viewport
                }
                HStack(spacing: 6) {
                    LegendTag(color: FloorModel.s1.0, border: T.gray400, text: "S1 rooms")
                    LegendTag(color: FloorModel.s2.0, border: FloorModel.s2.2, text: "S2 core")
                    LegendTag(color: FloorModel.s3.0, border: FloorModel.s3.2, text: "S3 sunk")
                }
                .padding(12)
                .allowsHitTesting(false)

                VStack {
                    Spacer()
                    HStack(alignment: .bottom) {
                        Text(mode == 2 ? "ND 41-06 · as drawn" : "Drag to rotate · pinch to zoom · tap a slab")
                            .font(.sans(12)).foregroundColor(T.text2)
                        Spacer()
                        if mode != 2 {
                            VStack(spacing: 6) {
                                iconButton("arrow.clockwise", label: "Rotate view") {
                                    yaw += Double.pi / 2
                                    baseYaw = yaw
                                }
                                iconButton("arrow.up.left.and.arrow.down.right", label: "Fit to screen") {
                                    zoom = 1; baseZoom = 1
                                    yaw = Double.pi / 4; baseYaw = yaw
                                }
                            }
                        }
                    }
                    .padding(10)
                }
            }
            .frame(height: 330)
            .background(RoundedRectangle(cornerRadius: 16).fill(Color.white))
            .clipShape(RoundedRectangle(cornerRadius: 16))
            .overlay(RoundedRectangle(cornerRadius: 16).strokeBorder(T.border, lineWidth: 1))
            .padding(.horizontal, 12)

            HStack(spacing: 8) {
                PillButton(title: "Slabs", selected: showSlabs, height: 40, fontSize: 14) { showSlabs.toggle() }
                PillButton(title: "Beams", selected: showBeams, height: 40, fontSize: 14) { showBeams.toggle() }
                PillButton(title: "Columns", selected: showColumns, height: 40, fontSize: 14) { showColumns.toggle() }
                PillButton(title: "Rebar", selected: showRebar, height: 40, fontSize: 14) { showRebar.toggle() }
            }
            .padding(.horizontal, 20).padding(.top, 10)

            Spacer(minLength: 8)

            VStack(alignment: .leading, spacing: 10) {
                Capsule().fill(T.gray300).frame(width: 40, height: 4).frame(maxWidth: .infinity)
                VStack(alignment: .leading, spacing: 2) {
                    Text(info.title).font(.sans(17, .semibold)).foregroundColor(T.ink)
                    Text(info.detail).font(.sans(14)).foregroundColor(T.text2).fixedSize(horizontal: false, vertical: true)
                }
                HStack(spacing: 10) {
                    HStack(spacing: 16) {
                        miniStat("Steel", info.steel)
                        miniStat("Concrete", info.concrete)
                        miniStat("Same panels", info.same)
                    }
                    Spacer(minLength: 4)
                    Button {
                        app.push(.slabDetail)
                    } label: {
                        Text("Open slab").font(.sans(15, .semibold)).foregroundColor(.white)
                            .padding(.horizontal, 16).frame(height: 48)
                            .background(RoundedRectangle(cornerRadius: 10).fill(T.accent))
                    }
                    .buttonStyle(PressStyle())
                }
            }
            .padding(.horizontal, 20).padding(.top, 10).padding(.bottom, 14)
            .background(
                UnevenTopRounded(radius: 18).fill(Color.white).ignoresSafeArea(edges: .bottom)
            )
            .overlay(UnevenTopRounded(radius: 18).stroke(T.border, lineWidth: 1))
        }
        .background(T.bg.ignoresSafeArea())
        .toolbar(.hidden, for: .navigationBar)
    }

    var viewport: some View {
        let boxes = FloorModel.boxes(slabs: showSlabs, beams: showBeams, columns: showColumns)
        let cam = IsoCamera(yaw: yaw, elevation: mode == 0 ? Double.pi / 2 : 0.6, zoom: zoom)
        return GeometryReader { geo in
            let area = CGSize(width: geo.size.width, height: geo.size.height)
            let scene = IsoScene(boxes: boxes, camera: cam, size: area, padding: 34)
            Canvas { ctx, _ in
                scene.draw(in: ctx, selectedTag: selectedTag,
                           selectedColors: FloorModel.sel,
                           rebar: showRebar ? RebarSpec(tag: selectedTag, spacing: 0.3, color: Color(hex: 0xFED7AA)) : nil)
            }
            .contentShape(Rectangle())
            .gesture(
                DragGesture(minimumDistance: 6)
                    .onChanged { v in yaw = baseYaw + Double(v.translation.width) * 0.012 }
                    .onEnded { _ in baseYaw = yaw }
                    .simultaneously(with:
                        MagnificationGesture()
                            .onChanged { s in zoom = min(3.0, max(0.6, baseZoom * Double(s))) }
                            .onEnded { _ in baseZoom = zoom }
                    )
            )
            .simultaneousGesture(
                SpatialTapGesture().onEnded { v in
                    if let t = scene.hitTag(at: v.location, layer: 2) {
                        selectedTag = t
                    }
                }
            )
        }
        .padding(.top, 30)
        .accessibilityLabel("3D model of the typical floor")
    }

    func iconButton(_ name: String, label: String, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Image(systemName: name).font(.system(size: 18, weight: .semibold)).foregroundColor(T.ink)
                .frame(width: 44, height: 44)
                .background(RoundedRectangle(cornerRadius: 10).fill(Color.white))
                .overlay(RoundedRectangle(cornerRadius: 10).strokeBorder(T.border, lineWidth: 1))
        }
        .buttonStyle(PressStyle())
        .accessibilityLabel(label)
    }

    func miniStat(_ label: String, _ value: String) -> some View {
        VStack(alignment: .leading, spacing: 1) {
            Text(label).font(.sans(12)).foregroundColor(T.text2)
            Text(value).font(.mono(15)).foregroundColor(T.ink)
        }
    }
}

/// Rectangle with only the top corners rounded (iOS 16 compatible).
struct UnevenTopRounded: Shape {
    var radius: CGFloat
    func path(in rect: CGRect) -> Path {
        var p = Path()
        p.move(to: CGPoint(x: rect.minX, y: rect.maxY))
        p.addLine(to: CGPoint(x: rect.minX, y: rect.minY + radius))
        p.addArc(center: CGPoint(x: rect.minX + radius, y: rect.minY + radius), radius: radius,
                 startAngle: .degrees(180), endAngle: .degrees(270), clockwise: false)
        p.addLine(to: CGPoint(x: rect.maxX - radius, y: rect.minY))
        p.addArc(center: CGPoint(x: rect.maxX - radius, y: rect.minY + radius), radius: radius,
                 startAngle: .degrees(270), endAngle: .degrees(0), clockwise: false)
        p.addLine(to: CGPoint(x: rect.maxX, y: rect.maxY))
        return p
    }
}

// MARK: - 4b Slab detail

struct SlabDetailScreen: View {
    @EnvironmentObject var app: AppState
    @State var tab = 0

    // Slab S1: 4.5 m long (x) × 2.641 m (y)
    var boxes: [IsoBox] {
        let L = 4.5, W = 2.641, t = 0.125, bw = 0.23, bd = 0.6
        let slab = tab == 2 ? (Color(hex: 0xD1D5DB), Color(hex: 0xB9BEC6), Color(hex: 0x9CA3AF))
                            : (Color(hex: 0xFFF1E6), Color(hex: 0xF4C9A6), Color(hex: 0xE6B48B))
        let beam = (Color(hex: 0x6B7280), Color(hex: 0x525A66), Color(hex: 0x4B5563))
        let col = (Color(hex: 0x9CA3AF), Color(hex: 0x7B828E), Color(hex: 0x6B7280))
        var out: [IsoBox] = []
        out.append(IsoBox(x0: 0, y0: 0, x1: L, y1: W, z0: -t, z1: 0, top: slab.0, sideX: slab.1, sideY: slab.2, tag: "S1", layer: 1))
        out.append(IsoBox(x0: -bw, y0: -bw, x1: L + bw, y1: 0, z0: -bd, z1: 0, top: beam.0, sideX: beam.1, sideY: beam.2, layer: 2))
        out.append(IsoBox(x0: -bw, y0: W, x1: L + bw, y1: W + bw, z0: -bd, z1: 0, top: beam.0, sideX: beam.1, sideY: beam.2, layer: 2))
        out.append(IsoBox(x0: -bw, y0: 0, x1: 0, y1: W, z0: -bd, z1: 0, top: beam.0, sideX: beam.1, sideY: beam.2, layer: 2))
        out.append(IsoBox(x0: L, y0: 0, x1: L + bw, y1: W, z0: -bd, z1: 0, top: beam.0, sideX: beam.1, sideY: beam.2, layer: 2))
        for (x, y) in [(-bw, -bw), (L, -bw), (-bw, W), (L, W)] {
            out.append(IsoBox(x0: x, y0: y, x1: x + bw, y1: y + bw, z0: -1.6, z1: -bd, top: col.0, sideX: col.1, sideY: col.2, layer: 0))
        }
        return out
    }

    var body: some View {
        Screen(title: "S1 · Living room, Flat A", subtitle: "Between B8, B15/B16, B45, B48 · same in 4 flats", spacing: 8) {
            Segmented(options: ["Bottom bars", "Top bars", "Concrete"], selection: $tab)
                .padding(.bottom, 4)

            ZStack(alignment: .bottomLeading) {
                GeometryReader { geo in
                    let cam = IsoCamera(yaw: 0.52, elevation: 0.62)
                    let scene = IsoScene(boxes: boxes, camera: cam, size: geo.size, padding: 22)
                    let rebar: RebarSpec? = tab == 2 ? nil : RebarSpec(tag: "S1", spacing: 0.15, edgesOnly: tab == 1)
                    ZStack {
                        Canvas { ctx, _ in
                            scene.draw(in: ctx, selectedTag: nil, selectedColors: nil, rebar: rebar)
                        }
                        beamLabel("B45", scene.project(2.25, -0.1, 0.1))
                        beamLabel("B48", scene.project(2.25, 2.75, -0.6))
                        beamLabel("B8", scene.project(4.65, 1.3, 0.2))
                        beamLabel("B15/B16", scene.project(-0.2, 1.3, -0.9))
                    }
                }
                .padding(.bottom, 26)
                Text(tab == 2 ? "Concrete volume incl. beam haunch" : "Bars drawn at true spacing")
                    .font(.sans(12)).foregroundColor(T.text2)
                    .padding(12)
            }
            .frame(height: 300)
            .background(RoundedRectangle(cornerRadius: 16).fill(Color.white))
            .clipShape(RoundedRectangle(cornerRadius: 16))
            .overlay(RoundedRectangle(cornerRadius: 16).strokeBorder(T.border, lineWidth: 1))
            .padding(.horizontal, -8)

            Card(padding: EdgeInsets(top: 4, leading: 14, bottom: 4, trailing: 14), radius: 12) {
                VStack(spacing: 0) {
                    if tab == 0 {
                        ValueRow(title: "Short span bars", detail: "8T @ 150 c/c · 31 nos × 2.98 m", value: "36.5 kg")
                        Divider1()
                        ValueRow(title: "Long span bars", detail: "8T @ 150 c/c · 18 nos × 4.84 m", value: "34.4 kg")
                    } else if tab == 1 {
                        ValueRow(title: "Top bars at supports", detail: "Not detailed in the slab schedule", value: "—")
                        Divider1()
                        ValueRow(title: "Asked \(app.consultant)", detail: "Shown here once they reply", value: "open")
                    } else {
                        ValueRow(title: "Concrete " + concreteGrade, detail: "4,500 × 2,641 × 125 mm + haunch", value: "1.49 m³")
                        Divider1()
                        ValueRow(title: "Cement", detail: "at " + cementText + " kg/m³ + 2% wastage", value: "11.6 bags")
                    }
                    Divider1()
                    ValueRow(title: "Steel · Concrete", value: "71 kg · 1.49 m³", bold: true)
                }
            }
            Text("Source: slab schedule ND 41-06, mark S1. Size measured from plan.")
                .font(.sans(12)).foregroundColor(T.text2)
        } bottom: {
            HStack(spacing: 10) {
                SecondaryButton(title: "Flag an issue") { app.push(.issues) }
                PrimaryButton(title: "Looks right", icon: "checkmark", fontSize: 15) {
                    app.pop()
                    app.showToast("S1 marked as checked")
                }
            }
        }
    }

    var concreteGrade: String { app.grades.components(separatedBy: " ").first ?? "M30" }
    var cementText: String { app.cement.isEmpty ? "380" : app.cement }

    func beamLabel(_ text: String, _ at: CGPoint) -> some View {
        Text(text).font(.mono(11)).foregroundColor(T.text3)
            .padding(.horizontal, 5).padding(.vertical, 2)
            .background(RoundedRectangle(cornerRadius: 4).fill(Color.white))
            .overlay(RoundedRectangle(cornerRadius: 4).strokeBorder(T.border, lineWidth: 1))
            .position(at)
    }
}
