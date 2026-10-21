import SwiftUI

// MARK: - 1 Projects

struct LogoMark: View {
    var size: CGFloat = 32
    var body: some View {
        ZStack {
            RoundedRectangle(cornerRadius: size / 4).fill(T.ink)
            Canvas { ctx, sz in
                let s = sz.width / 24
                var p = Path()
                func pt(_ x: CGFloat, _ y: CGFloat) -> CGPoint { CGPoint(x: x * s, y: y * s) }
                p.move(to: pt(4, 20)); p.addLine(to: pt(4, 8)); p.addLine(to: pt(12, 4)); p.addLine(to: pt(20, 8)); p.addLine(to: pt(20, 20))
                p.move(to: pt(4, 12)); p.addLine(to: pt(20, 12))
                p.move(to: pt(9, 20)); p.addLine(to: pt(9, 12))
                p.move(to: pt(15, 20)); p.addLine(to: pt(15, 12))
                ctx.stroke(p, with: .color(.white), style: StrokeStyle(lineWidth: 2 * s, lineCap: .round, lineJoin: .round))
            }
            .frame(width: size * 0.56, height: size * 0.56)
        }
        .frame(width: size, height: size)
    }
}

struct ProjectCard: View {
    let name: String
    let detail: String
    let status: String
    let statusFg: Color
    let statusBg: Color
    let steel: String
    let cement: String
    let perSqft: String
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            VStack(alignment: .leading, spacing: 12) {
                HStack(alignment: .top, spacing: 8) {
                    VStack(alignment: .leading, spacing: 2) {
                        Text(name).font(.sans(17, .semibold)).foregroundColor(T.ink)
                        Text(detail).font(.sans(14)).foregroundColor(T.text2)
                    }
                    Spacer(minLength: 4)
                    Chip(text: status, fg: statusFg, bg: statusBg)
                }
                HStack(spacing: 8) {
                    Stat(label: "Steel", value: steel)
                    Stat(label: "Cement", value: cement)
                    Stat(label: "Per sq ft", value: perSqft)
                }
            }
            .padding(16)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(RoundedRectangle(cornerRadius: 14).fill(Color.white))
            .overlay(RoundedRectangle(cornerRadius: 14).strokeBorder(T.border, lineWidth: 1))
            .contentShape(Rectangle())
        }
        .buttonStyle(PressStyle())
    }
}

struct ProjectsScreen: View {
    @EnvironmentObject var app: AppState
    @State var confirmReset = false

    var body: some View {
        VStack(spacing: 0) {
            HStack {
                HStack(spacing: 10) {
                    LogoMark()
                    Text("Takeoff").font(.sans(20, .bold)).foregroundColor(T.ink)
                    Text("DEMO")
                        .font(.mono(11, .bold))
                        .foregroundColor(T.accentDark)
                        .padding(.horizontal, 6).padding(.vertical, 3)
                        .background(RoundedRectangle(cornerRadius: 5).fill(T.accentSoft))
                        .accessibilityLabel("Demo version")
                }
                Spacer()
                Menu {
                    Button {
                        confirmReset = true
                    } label: {
                        Label("Reset demo", systemImage: "arrow.counterclockwise")
                    }
                    Button {
                        app.showAbout = true
                    } label: {
                        Label("About this demo", systemImage: "info.circle")
                    }
                } label: {
                    Text("JP")
                        .font(.sans(15, .semibold))
                        .foregroundColor(T.ink)
                        .frame(width: 44, height: 44)
                        .background(Circle().fill(Color.white))
                        .overlay(Circle().strokeBorder(T.gray300, lineWidth: 1))
                }
                .accessibilityLabel("Account and settings")
            }
            .padding(.horizontal, 20)
            .padding(.top, 12)
            .padding(.bottom, 12)

            ScrollView {
                VStack(alignment: .leading, spacing: 12) {
                    VStack(alignment: .leading, spacing: 4) {
                        Text("Projects").font(.sans(26, .bold)).foregroundColor(T.ink)
                        Text("Steel and cement from your structural drawings")
                            .font(.sans(15)).foregroundColor(T.text2)
                    }
                    .padding(.top, 4)
                    .padding(.bottom, 4)

                    ProjectCard(name: "Shri Basaveshwar Park",
                                detail: "3 buildings · Bldg 2 typical floor · Panvel",
                                status: "Estimated", statusFg: T.greenFg, statusBg: T.greenBg,
                                steel: "3.79 t", cement: "317 bags", perSqft: "₹250") {
                        app.push(.coverage)
                    }
                    ProjectCard(name: "Work Lounge",
                                detail: "Second floor slab + beams · Hubballi",
                                status: "3 to confirm", statusFg: T.amberFg, statusBg: T.amberBg,
                                steel: "11.99 t", cement: "586 bags", perSqft: "₹223") {
                        app.push(.issues)
                    }

                    HStack(alignment: .center, spacing: 12) {
                        Image(systemName: "doc").font(.system(size: 20)).foregroundColor(T.text2)
                        Text("Works best with vector PDFs exported from AutoCAD or ZWCAD. Scanned drawings coming later.")
                            .font(.sans(14)).foregroundColor(T.text2)
                            .fixedSize(horizontal: false, vertical: true)
                    }
                    .padding(16)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .overlay(RoundedRectangle(cornerRadius: 14).strokeBorder(T.gray400, style: StrokeStyle(lineWidth: 1, dash: [4, 3])))
                }
                .padding(.horizontal, 20)
                .padding(.bottom, 16)
            }

            PrimaryButton(title: "New project", icon: "plus") {
                app.push(.createProject)
            }
            .padding(.horizontal, 20)
            .padding(.top, 10)
            .padding(.bottom, 12)
        }
        .background(T.bg.ignoresSafeArea())
        .toolbar(.hidden, for: .navigationBar)
        .alert("Reset demo?", isPresented: $confirmReset) {
            Button("Cancel", role: .cancel) {}
            Button("Reset", role: .destructive) {
                app.reset()
                app.showToast("Demo reset to the sample data")
            }
        } message: {
            Text("All sample values go back to how they started.")
        }
    }
}

// MARK: - 1a New project

struct LabeledField: View {
    let label: String
    @Binding var text: String

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(label).font(.sans(13, .semibold)).foregroundColor(T.text3).lineLimit(1).minimumScaleFactor(0.8)
            TextField(label, text: $text)
                .font(.sans(16))
                .foregroundColor(T.ink)
                .padding(.horizontal, 12)
                .frame(height: 44)
                .background(RoundedRectangle(cornerRadius: 8).fill(Color.white))
                .overlay(RoundedRectangle(cornerRadius: 8).strokeBorder(T.gray300, lineWidth: 1))
        }
    }
}

struct BuildingBadge: View {
    let short: String
    let style: Int // 0 soft orange, 1 accent, 2 grey
    var body: some View {
        let bg: Color = style == 0 ? T.accentSoft : (style == 1 ? T.accent : T.gray200)
        let fg: Color = style == 0 ? T.accentDark : (style == 1 ? .white : T.text3)
        Text(short)
            .font(.mono(13, .semibold))
            .foregroundColor(fg)
            .frame(width: 36, height: 36)
            .background(RoundedRectangle(cornerRadius: 8).fill(bg))
    }
}

struct CreateProjectScreen: View {
    @EnvironmentObject var app: AppState

    var body: some View {
        Screen(title: "New project", trailing: AnyView(
            Text("Step 1 of 2").font(.sans(12)).foregroundColor(T.text2).padding(.trailing, 12)
        )) {
            Card {
                VStack(alignment: .leading, spacing: 10) {
                    LabeledField(label: "Project name", text: $app.projectName)
                    HStack(spacing: 10) {
                        LabeledField(label: "Site city", text: $app.city)
                        LabeledField(label: "Structural consultant", text: $app.consultant)
                    }
                    Text("City sets the steel, RMC and cement rates. Consultant is who we send questions to.")
                        .font(.sans(12)).foregroundColor(T.text2)
                        .fixedSize(horizontal: false, vertical: true)
                }
            }

            SectionLabel(title: "Buildings (\(app.buildings.count))", hint: "Drawings are filed per building")

            Card(padding: EdgeInsets(top: 2, leading: 14, bottom: 2, trailing: 14)) {
                VStack(spacing: 0) {
                    ForEach(Array(app.buildings.enumerated()), id: \.element.id) { idx, b in
                        Button {
                            app.push(.addBuilding(idx))
                        } label: {
                            HStack(spacing: 12) {
                                BuildingBadge(short: b.short, style: idx == 0 ? 0 : (idx == 1 ? 1 : 2))
                                VStack(alignment: .leading, spacing: 1) {
                                    HStack(spacing: 0) {
                                        Text(b.name).font(.sans(15, .semibold)).foregroundColor(T.ink)
                                        if let w = b.wings {
                                            Text(" · \(w)").font(.sans(15, .medium)).foregroundColor(T.text2)
                                        }
                                    }
                                    Text(b.summary).font(.sans(13)).foregroundColor(T.text2)
                                }
                                Spacer(minLength: 4)
                                Image(systemName: "chevron.right").font(.system(size: 14, weight: .semibold)).foregroundColor(T.gray400)
                            }
                            .padding(.vertical, 11)
                            .contentShape(Rectangle())
                        }
                        .buttonStyle(PressStyle())
                        if idx < app.buildings.count - 1 { Divider1() }
                    }
                }
            }

            HStack(spacing: 8) {
                Button {
                    app.showToast("Demo: the sample project already has its 3 buildings")
                } label: {
                    Text("+ Add building").font(.sans(15, .medium)).foregroundColor(T.text3)
                        .frame(maxWidth: .infinity).frame(height: 44)
                        .overlay(RoundedRectangle(cornerRadius: 10).strokeBorder(T.gray400, style: StrokeStyle(lineWidth: 1, dash: [4, 3])))
                        .contentShape(Rectangle())
                }
                .buttonStyle(PressStyle())
                Button {
                    app.showToast("Demo: buildings were read from the site plan")
                } label: {
                    Text("Read from site plan").font(.sans(15, .medium)).foregroundColor(T.text3)
                        .frame(maxWidth: .infinity).frame(height: 44)
                        .background(RoundedRectangle(cornerRadius: 10).fill(Color.white))
                        .overlay(RoundedRectangle(cornerRadius: 10).strokeBorder(T.gray300, lineWidth: 1))
                        .contentShape(Rectangle())
                }
                .buttonStyle(PressStyle())
            }

            Card {
                VStack(alignment: .leading, spacing: 8) {
                    (Text("Values you already know ").font(.sans(13, .semibold)).foregroundColor(T.text3)
                     + Text("· blanks are asked later").font(.sans(13)).foregroundColor(T.text2))
                    FlowChips()
                }
            }
        } bottom: {
            PrimaryButton(title: "Create project · add drawings") {
                app.push(.upload)
            }
        }
    }
}

struct FlowChips: View {
    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack(spacing: 8) {
                knownChip("Concrete ", "M30")
                knownChip("Steel ", "Fe550")
                knownChip("Units ", "mm")
            }
            Text("Cement content · ask me")
                .font(.sans(14)).foregroundColor(T.text2)
                .padding(.horizontal, 10).padding(.vertical, 6)
                .overlay(RoundedRectangle(cornerRadius: 8).strokeBorder(T.gray400, style: StrokeStyle(lineWidth: 1, dash: [4, 3])))
        }
    }

    func knownChip(_ label: String, _ value: String) -> some View {
        (Text(label).font(.sans(14)).foregroundColor(T.ink) + Text(value).font(.mono(14, .bold)).foregroundColor(T.ink))
            .lineLimit(1)
            .padding(.horizontal, 10).padding(.vertical, 6)
            .background(RoundedRectangle(cornerRadius: 8).fill(T.bg))
    }
}

// MARK: - 1b Building setup

struct Stepper3: View {
    let value: Int
    let label: String
    let minValue: Int
    let maxValue: Int
    let onChange: (Int) -> Void

    var body: some View {
        HStack(spacing: 4) {
            stepButton("minus", enabled: value > minValue) { onChange(value - 1) }
                .accessibilityLabel("Fewer \(label)")
            Text("\(value)").font(.mono(16, .semibold)).foregroundColor(T.ink).frame(width: 30)
            stepButton("plus", enabled: value < maxValue) { onChange(value + 1) }
                .accessibilityLabel("More \(label)")
        }
    }

    func stepButton(_ icon: String, enabled: Bool, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Image(systemName: icon).font(.system(size: 16, weight: .semibold))
                .foregroundColor(enabled ? T.ink : T.gray300)
                .frame(width: 44, height: 44)
                .background(RoundedRectangle(cornerRadius: 8).fill(Color.white))
                .overlay(RoundedRectangle(cornerRadius: 8).strokeBorder(T.gray300, lineWidth: 1))
        }
        .buttonStyle(PressStyle())
        .disabled(!enabled)
    }
}

struct AddBuildingScreen: View {
    @EnvironmentObject var app: AppState
    let index: Int
    @State var draft: BuildingConfig? = nil

    var body: some View {
        let b = draft ?? app.buildings[index]
        Screen(title: b.name, subtitle: app.projectName) {
            Card {
                VStack(alignment: .leading, spacing: 12) {
                    VStack(alignment: .leading, spacing: 6) {
                        Text("Use").font(.sans(13, .semibold)).foregroundColor(T.text3)
                        HStack(spacing: 6) {
                            ForEach(["Residential", "Commercial", "Mixed"], id: \.self) { u in
                                PillButton(title: u, selected: b.use == u, height: 40, fontSize: 14, expand: true) {
                                    update { $0.use = u }
                                }
                            }
                        }
                    }
                    row("Basements") {
                        Stepper3(value: b.basements, label: "basements", minValue: 0, maxValue: 4) { v in update { $0.basements = v } }
                    }
                    row("Ground level") {
                        HStack(spacing: 4) {
                            ForEach(["Stilt", "Ground", "Podium"], id: \.self) { g in
                                Button {
                                    update { $0.ground = g }
                                } label: {
                                    Text(g).font(.sans(14, b.ground == g ? .semibold : .medium))
                                        .foregroundColor(b.ground == g ? T.ink : T.text3)
                                        .padding(.horizontal, 9)
                                        .frame(height: 44)
                                        .background(RoundedRectangle(cornerRadius: 8).fill(Color.white))
                                        .overlay(RoundedRectangle(cornerRadius: 8).strokeBorder(b.ground == g ? T.ink : T.gray300, lineWidth: b.ground == g ? 2 : 1))
                                }
                                .buttonStyle(PressStyle())
                            }
                        }
                    }
                    row("Upper floors") {
                        Stepper3(value: b.upper, label: "floors", minValue: 1, maxValue: 30) { v in update { $0.upper = v } }
                    }
                    row("Roof") {
                        HStack(spacing: 12) {
                            miniCheck("Terrace", b.terrace) { update { $0.terrace.toggle() } }
                            miniCheck("OHT / LMR", b.oht) { update { $0.oht.toggle() } }
                        }
                    }
                    row("Identical copies") {
                        Stepper3(value: b.copies, label: "copies", minValue: 1, maxValue: 9) { v in update { $0.copies = v } }
                    }
                    Text("Wings built from the same drawings? Set copies to 2 and quantities are multiplied, not uploaded twice.")
                        .font(.sans(12)).foregroundColor(T.text2).fixedSize(horizontal: false, vertical: true)
                }
            }

            Card {
                VStack(alignment: .leading, spacing: 8) {
                    HStack(alignment: .firstTextBaseline) {
                        Text("Levels · tap to edit height").font(.sans(13, .semibold)).foregroundColor(T.text3)
                        Spacer()
                        Text("Filled from arch section").font(.sans(12)).foregroundColor(T.text2)
                    }
                    ForEach(levelRows(b)) { item in
                        HStack(spacing: 12) {
                            Text(item.name).font(.sans(14, item.tag != nil ? .semibold : .regular)).foregroundColor(T.ink)
                            Spacer()
                            Text(item.height).font(.mono(13)).foregroundColor(T.text2)
                            if let tag = item.tag {
                                Text(tag).font(.sans(12, .semibold)).foregroundColor(T.accentDark)
                                    .padding(.horizontal, 6).padding(.vertical, 2)
                                    .background(RoundedRectangle(cornerRadius: 4).fill(T.accentSoft))
                            }
                        }
                        .frame(minHeight: 22)
                    }
                }
            }

            HStack(spacing: 10) {
                Image(systemName: "info.circle").foregroundColor(T.text3)
                Text("\(b.levelCount) levels · \(b.partCount) parts to cover with structural drawings")
                    .font(.sans(14)).foregroundColor(T.text3)
            }
            .padding(.horizontal, 2)
        } bottom: {
            PrimaryButton(title: "Save building") {
                if let d = draft { app.buildings[index] = d }
                app.pop()
                app.showToast("\(b.name) saved")
            }
        }
    }

    func update(_ change: (inout BuildingConfig) -> Void) {
        var d = draft ?? app.buildings[index]
        change(&d)
        d.fixedSummary = nil
        draft = d
    }

    func row<C: View>(_ label: String, @ViewBuilder _ content: () -> C) -> some View {
        HStack {
            Text(label).font(.sans(15)).foregroundColor(T.ink)
            Spacer(minLength: 8)
            content()
        }
    }

    func miniCheck(_ title: String, _ on: Bool, _ action: @escaping () -> Void) -> some View {
        Button(action: action) {
            HStack(spacing: 6) {
                CheckBox(checked: on, size: 20)
                Text(title).font(.sans(14)).foregroundColor(T.ink)
            }
            .frame(minHeight: 44)
            .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
    }

    func levelRows(_ b: BuildingConfig) -> [LevelRow] {
        var r: [LevelRow] = []
        let terraceLevel = 4.25 + Double(b.upper) * 2.95
        if b.oht { r.append(LevelRow("OHT / LMR", String(format: "+%.2f", terraceLevel + 4.10), nil)) }
        if b.terrace { r.append(LevelRow("Terrace", String(format: "+%.2f", terraceLevel), nil)) }
        if b.upper > 1 {
            r.append(LevelRow("1st – \(ordinal(b.upper)) floor", "2.95 m", "Typical ×\(b.upper)"))
        } else {
            r.append(LevelRow("1st floor", "2.95 m", nil))
        }
        let gname = b.ground == "Ground" ? "Ground / plinth" : (b.ground == "Podium" ? "Podium" : "Stilt / plinth")
        r.append(LevelRow(gname, "+0.15", nil))
        if b.basements > 0 {
            for n in 1...b.basements { r.append(LevelRow("Basement \(n)", String(format: "%.2f", -3.0 * Double(n)), nil)) }
        }
        r.append(LevelRow("Foundation", "—", nil))
        return r
    }
}

struct LevelRow: Identifiable {
    let name: String
    let height: String
    let tag: String?
    var id: String { name }
    init(_ name: String, _ height: String, _ tag: String?) {
        self.name = name
        self.height = height
        self.tag = tag
    }
}
