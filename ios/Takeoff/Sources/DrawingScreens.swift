import SwiftUI

// MARK: - 2 Add drawings

struct PdfIcon: View {
    var width: CGFloat = 36
    var height: CGFloat = 44
    var body: some View {
        Text("PDF").font(.mono(10, .semibold)).foregroundColor(T.accentDark)
            .frame(width: width, height: height)
            .background(RoundedRectangle(cornerRadius: 6).fill(T.accentSoft))
    }
}

struct UploadScreen: View {
    @EnvironmentObject var app: AppState

    let buildingOptions = ["Building 1", "Building 2", "Clubhouse", "Site-wide"]
    let kinds = ["Slab + beams", "Columns", "Footings"]
    let samples: [DrawingFile] = [
        DrawingFile(name: "ND 41-07 2nd & 4th Floor Plan", detail: "A2 · beam + slab schedule found", tag: "Structural"),
        DrawingFile(name: "ND 41-08 Column Layout", detail: "A1 · column schedule found", tag: "Structural"),
        DrawingFile(name: "Building-2 Sheet 5 – Sections", detail: "A1 · floor heights found", tag: "Architectural"),
        DrawingFile(name: "ND 41-09 Footing Layout", detail: "A1 · footing schedule found", tag: "Structural"),
    ]

    var body: some View {
        Screen(title: "Add drawings", spacing: 18) {
            VStack(alignment: .leading, spacing: 6) {
                Text("For building").font(.sans(13, .semibold)).foregroundColor(T.text3)
                VStack(alignment: .leading, spacing: 8) {
                    HStack(spacing: 8) {
                        ForEach(0..<3, id: \.self) { i in
                            PillButton(title: buildingOptions[i], selected: app.uploadBuilding == i, height: 44, radius: 22) {
                                app.uploadBuilding = i
                            }
                        }
                    }
                    PillButton(title: buildingOptions[3], selected: app.uploadBuilding == 3, height: 44, radius: 22) {
                        app.uploadBuilding = 3
                    }
                }
                Text("We also read the title block and flag a sheet that names another building.")
                    .font(.sans(12)).foregroundColor(T.text2).fixedSize(horizontal: false, vertical: true)
            }

            VStack(alignment: .leading, spacing: 6) {
                Text("What to estimate").font(.sans(13, .semibold)).foregroundColor(T.text3)
                HStack(spacing: 8) {
                    ForEach(0..<kinds.count, id: \.self) { i in
                        PillButton(title: kinds[i], selected: app.estimateKind == i, height: 44, radius: 22) {
                            app.estimateKind = i
                        }
                    }
                }
            }

            VStack(alignment: .leading, spacing: 8) {
                SectionLabel(title: "Drawings (\(app.drawings.count + 3))", hint: "Tap a tag to change it")
                VStack(spacing: 0) {
                    ForEach(app.drawings) { d in
                        HStack(spacing: 12) {
                            PdfIcon()
                            VStack(alignment: .leading, spacing: 2) {
                                Text(d.name).font(.sans(15, .medium)).foregroundColor(T.ink).lineLimit(1).truncationMode(.tail)
                                Text(d.detail).font(.sans(13)).foregroundColor(T.text2).lineLimit(1)
                            }
                            Spacer(minLength: 4)
                            Button {
                                toggleTag(d)
                            } label: {
                                Chip(text: d.tag,
                                     fg: d.tag == "Structural" ? T.blueFg : T.text3,
                                     bg: d.tag == "Structural" ? T.blueBg : T.gray200)
                                    .frame(minHeight: 44)
                                    .contentShape(Rectangle())
                            }
                            .buttonStyle(PressStyle())
                        }
                        .padding(.horizontal, 14)
                        .padding(.vertical, 8)
                        Divider1()
                    }
                    HStack(spacing: 12) {
                        PdfIcon()
                        VStack(alignment: .leading, spacing: 2) {
                            Text("+ 3 more architectural sheets").font(.sans(15, .medium)).foregroundColor(T.ink)
                            Text("Sections, elevation, terrace").font(.sans(13)).foregroundColor(T.text2)
                        }
                        Spacer()
                    }
                    .padding(.horizontal, 14)
                    .padding(.vertical, 12)
                }
                .background(RoundedRectangle(cornerRadius: 12).fill(Color.white))
                .overlay(RoundedRectangle(cornerRadius: 12).strokeBorder(T.border, lineWidth: 1))
            }

            Button {
                let s = samples[app.extraSampleIndex % samples.count]
                withAnimation { app.drawings.append(DrawingFile(name: s.name, detail: s.detail, tag: s.tag)) }
                app.extraSampleIndex += 1
                app.showToast("Added \(s.name)")
            } label: {
                HStack(spacing: 8) {
                    Image(systemName: "arrow.up.doc").font(.system(size: 18))
                    Text("Add PDF or DWG").font(.sans(16, .medium))
                }
                .foregroundColor(T.text3)
                .frame(maxWidth: .infinity)
                .frame(height: 52)
                .overlay(RoundedRectangle(cornerRadius: 12).strokeBorder(T.gray400, style: StrokeStyle(lineWidth: 1, dash: [5, 4])))
                .contentShape(Rectangle())
            }
            .buttonStyle(PressStyle())
        } bottom: {
            Text("Structural sheets give the bars. Architectural sheets give the spans.")
                .font(.sans(12)).foregroundColor(T.text2).multilineTextAlignment(.center)
            PrimaryButton(title: "Read drawings") { app.push(.reading) }
        }
    }

    func toggleTag(_ d: DrawingFile) {
        guard let i = app.drawings.firstIndex(of: d) else { return }
        let newTag = d.tag == "Structural" ? "Architectural" : "Structural"
        app.drawings[i] = DrawingFile(name: d.name, detail: d.detail, tag: newTag)
    }
}

// MARK: - 2a Coverage

struct CoverageScreen: View {
    @EnvironmentObject var app: AppState

    var body: some View {
        let bi = app.coverageBuilding
        let b = app.buildings[bi]
        let done = app.coveredCount(bi)
        let total = b.partCount
        let sel = app.selected[bi]

        Screen(title: app.projectName, subtitle: "\(app.buildings.count) buildings · drawing coverage", spacing: 10) {
            HStack(spacing: 6) {
                ForEach(app.buildings) { bb in
                    let on = bb.id == bi
                    Button {
                        app.coverageBuilding = bb.id
                    } label: {
                        VStack(spacing: 1) {
                            Text(bb.tabLabel).font(.sans(13, on ? .semibold : .medium)).lineLimit(1).minimumScaleFactor(0.75)
                                .foregroundColor(on ? .white : T.text3)
                            Text("\(app.coveredCount(bb.id))/\(bb.partCount)").font(.mono(11))
                                .foregroundColor(on ? T.gray300 : T.gray500)
                        }
                        .padding(.horizontal, 4)
                        .frame(maxWidth: .infinity)
                        .frame(height: 48)
                        .background(RoundedRectangle(cornerRadius: 10).fill(on ? T.ink : Color.white))
                        .overlay(RoundedRectangle(cornerRadius: 10).strokeBorder(on ? T.ink : T.gray300, lineWidth: on ? 2 : 1))
                        .contentShape(Rectangle())
                    }
                    .buttonStyle(PressStyle())
                    .accessibilityAddTraits(on ? .isSelected : [])
                }
            }

            (Text("\(b.name): ").font(.sans(14)).foregroundColor(T.text3)
             + Text("\(done)/\(total)").font(.mono(14, .bold)).foregroundColor(T.text3)
             + Text(" parts have structural drawings. Tap a grey part to upload.").font(.sans(14)).foregroundColor(T.text3))
                .fixedSize(horizontal: false, vertical: true)

            Card(padding: EdgeInsets(top: 12, leading: 12, bottom: 14, trailing: 12)) {
                VStack(alignment: .leading, spacing: 6) {
                    HStack(spacing: 6) {
                        Color.clear.frame(width: 74, height: 1)
                        ForEach(0..<3, id: \.self) { c in
                            Text(columnNames[c]).font(.sans(12, .semibold)).foregroundColor(T.text3)
                                .multilineTextAlignment(.center).frame(maxWidth: .infinity)
                        }
                    }
                    ForEach(b.rows) { row in
                        HStack(spacing: 6) {
                            VStack(alignment: .leading, spacing: 0) {
                                Text(row.name).font(.sans(12, sel?.row == row.key ? .semibold : .regular)).foregroundColor(T.text3)
                                    .lineLimit(1).minimumScaleFactor(0.75)
                                if let lt = row.levelText {
                                    Text(lt).font(.mono(10)).foregroundColor(T.gray500)
                                }
                            }
                            .frame(width: 74, alignment: .leading)
                            if row.key == "fdn" {
                                cell(CellRef(building: bi, row: row.key, col: 0), label: "Footings", selected: sel)
                            } else {
                                ForEach(0..<3, id: \.self) { c in
                                    if row.cells.contains(c) {
                                        cell(CellRef(building: bi, row: row.key, col: c), label: nil, selected: sel)
                                    } else {
                                        Color.clear.frame(maxWidth: .infinity).frame(height: 40)
                                    }
                                }
                            }
                        }
                    }
                    HStack(spacing: 14) {
                        legend(fill: T.coveredBg, stroke: nil, dashed: false, text: "Has structural")
                        legend(fill: .clear, stroke: T.gray400, dashed: true, text: "Missing")
                        legend(fill: .clear, stroke: T.accent, dashed: false, text: "Selected")
                    }
                    .padding(.top, 6)
                }
            }

            Text("Estimates cover green parts only until the rest are uploaded.")
                .font(.sans(12)).foregroundColor(T.text2)
        } bottom: {
            if let s = sel {
                PrimaryButton(title: "Upload for \(b.name) · \(s.row == "fdn" ? "footings" : app.rowName(s))", fontSize: 15) {
                    app.push(.targetUpload)
                }
            } else {
                PrimaryButton(title: done == total ? "All parts have drawings" : "Tap a grey part to upload", enabled: false, fontSize: 15) {}
            }
        }
    }

    @ViewBuilder
    func cell(_ ref: CellRef, label: String?, selected: CellRef?) -> some View {
        let code = app.covered[ref.key]
        let isSel = selected == ref
        let state: String = code != nil ? "covered by ND " + (code ?? "") : (isSel ? "missing, selected" : "missing")
        let a11y: String = app.partName(ref) + ", " + state
        Button {
            if code != nil {
                app.push(.drawingHistory(code ?? "41-06"))
            } else {
                app.selected[ref.building] = isSel ? nil : ref
            }
        } label: {
            ZStack {
                if let code = code {
                    RoundedRectangle(cornerRadius: 6).fill(T.coveredBg)
                    Text(code).font(.mono(11)).foregroundColor(T.coveredFg)
                } else if isSel {
                    RoundedRectangle(cornerRadius: 6).fill(T.accentTint)
                    RoundedRectangle(cornerRadius: 6).strokeBorder(T.accent, lineWidth: 2)
                    Text("Selected").font(.sans(11, .semibold)).foregroundColor(T.accentDark).lineLimit(1).minimumScaleFactor(0.7)
                } else {
                    RoundedRectangle(cornerRadius: 6).fill(T.gray50)
                    RoundedRectangle(cornerRadius: 6).strokeBorder(T.gray400, style: StrokeStyle(lineWidth: 1, dash: [4, 3]))
                    if let label = label {
                        Text(label).font(.sans(12, .medium)).foregroundColor(T.text2)
                    }
                }
            }
            .frame(maxWidth: .infinity)
            .frame(height: 40)
            .contentShape(Rectangle())
        }
        .buttonStyle(PressStyle())
        .accessibilityLabel(a11y)
    }

    func legend(fill: Color, stroke: Color?, dashed: Bool, text: String) -> some View {
        HStack(spacing: 6) {
            ZStack {
                RoundedRectangle(cornerRadius: 3).fill(fill)
                if let s = stroke {
                    RoundedRectangle(cornerRadius: 3).strokeBorder(s, style: StrokeStyle(lineWidth: dashed ? 1 : 2, dash: dashed ? [3, 2] : []))
                }
            }
            .frame(width: 14, height: 10)
            Text(text).font(.sans(12)).foregroundColor(T.text2)
        }
    }
}

// MARK: - 2b Upload for a missing part

struct TargetUploadScreen: View {
    @EnvironmentObject var app: AppState
    @State var picked = false
    @State var ticks: [Bool] = []

    var body: some View {
        let bi = app.coverageBuilding
        let b = app.buildings[bi]
        let ref = app.selected[bi] ?? CellRef(building: bi, row: "f2", col: 0)
        let parts = app.targetParts(for: ref)
        let count = zip(parts, paddedTicks(parts.count)).filter { $0.1 }.count
        let rowName = ref.row == "fdn" ? "footings" : app.rowName(ref)
        let levelText = app.rowLevel(ref).map { " · \($0)" } ?? ""
        let rowTitle: String = rowName.prefix(1).uppercased() + String(rowName.dropFirst())
        let floorsTitle = parts.map { app.rowName($0).replacingOccurrences(of: " floor", with: "") }.joined(separator: " & ")

        Screen(title: "Structural for \(rowName)",
               subtitle: "\(b.name) · \(columnNamesLower[ref.col])\(levelText)") {
            VStack(spacing: 0) {
                PlanOutline(label: rowTitle + " plan · Arch Sheet 2")
                    .frame(height: 150)
                    .background(Color(hex: 0xFAFAFA))
                Rectangle().fill(T.border).frame(height: 1)
                Text("Area taken from the architect's \(floorsTitle) floor plan")
                    .font(.sans(12)).foregroundColor(T.text2)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(.horizontal, 14).padding(.vertical, 10)
            }
            .background(RoundedRectangle(cornerRadius: 14).fill(Color.white))
            .clipShape(RoundedRectangle(cornerRadius: 14))
            .overlay(RoundedRectangle(cornerRadius: 14).strokeBorder(T.border, lineWidth: 1))

            Button {
                withAnimation { picked = true }
                ticks = Array(repeating: true, count: parts.count)
            } label: {
                VStack(spacing: 6) {
                    Image(systemName: picked ? "checkmark.circle" : "arrow.up").font(.system(size: 22, weight: .semibold))
                    Text(picked ? "Choose a different PDF" : "Choose PDF · or share from WhatsApp").font(.sans(16, .medium))
                }
                .foregroundColor(T.text3)
                .frame(maxWidth: .infinity)
                .frame(height: 92)
                .background(RoundedRectangle(cornerRadius: 14).fill(Color.white))
                .overlay(RoundedRectangle(cornerRadius: 14).strokeBorder(T.gray400, style: StrokeStyle(lineWidth: 2, dash: [6, 4])))
                .contentShape(Rectangle())
            }
            .buttonStyle(PressStyle())

            if picked {
                Card {
                    VStack(alignment: .leading, spacing: 10) {
                        HStack(spacing: 10) {
                            PdfIcon(width: 34, height: 42)
                            VStack(alignment: .leading, spacing: 2) {
                                Text("ND 41-07 · \(floorsTitle) Floor Plan").font(.sans(15, .semibold)).foregroundColor(T.ink)
                                Text("Example · \(app.consultant) · R0 · schedules found").font(.sans(13)).foregroundColor(T.text2)
                            }
                        }
                        Text("This drawing also covers").font(.sans(14, .semibold)).foregroundColor(T.text3)
                        ForEach(Array(parts.enumerated()), id: \.offset) { i, p in
                            CheckRow(title: app.partName(p), checked: Binding(
                                get: { paddedTicks(parts.count)[i] },
                                set: { v in
                                    var t = paddedTicks(parts.count)
                                    t[i] = v
                                    ticks = t
                                }))
                        }
                        Text("Matched from the title \"\(floorsTitle) floor\" and the plan outline. Untick anything that is wrong. Filed under \(b.name) only.")
                            .font(.sans(12)).foregroundColor(T.text2).fixedSize(horizontal: false, vertical: true)
                    }
                }
                .transition(.opacity)
            }
        } bottom: {
            PrimaryButton(title: picked ? "Add to \(count) part\(count == 1 ? "" : "s") and read" : "Choose a PDF first",
                          enabled: picked && count > 0) {
                let t = paddedTicks(parts.count)
                for (i, p) in parts.enumerated() where t[i] {
                    app.covered[p.key] = "41-07"
                }
                app.selected[bi] = nil
                app.pop()
                app.showToast("ND 41-07 added to \(count) part\(count == 1 ? "" : "s")")
            }
        }
    }

    func paddedTicks(_ n: Int) -> [Bool] {
        if ticks.count == n { return ticks }
        return Array(repeating: true, count: n)
    }
}

struct PlanOutline: View {
    let label: String
    var body: some View {
        ZStack {
            Canvas { ctx, size in
                let w = min(size.width - 60, 280)
                let h: CGFloat = 110
                let x0 = (size.width - w) / 2
                let y0 = (size.height - h) / 2
                let rect = CGRect(x: x0, y: y0, width: w, height: h)
                ctx.fill(Path(rect), with: .color(T.accentTint))
                var grid = Path()
                let xs: [CGFloat] = [0.16, 0.39, 0.61, 0.84]
                for f in xs { grid.move(to: CGPoint(x: x0 + w * f, y: y0)); grid.addLine(to: CGPoint(x: x0 + w * f, y: y0 + h)) }
                for f in [0.32, 0.68] as [CGFloat] {
                    grid.move(to: CGPoint(x: x0, y: y0 + h * f)); grid.addLine(to: CGPoint(x: x0 + w * 0.39, y: y0 + h * f))
                    grid.move(to: CGPoint(x: x0 + w * 0.61, y: y0 + h * f)); grid.addLine(to: CGPoint(x: x0 + w, y: y0 + h * f))
                }
                ctx.stroke(grid, with: .color(T.gray400), lineWidth: 1)
                ctx.stroke(Path(rect), with: .color(T.accent), style: StrokeStyle(lineWidth: 2, dash: [6, 4]))
            }
            Text(label).font(.sans(12, .semibold)).foregroundColor(T.accentDark)
                .padding(.horizontal, 4)
                .background(T.accentTint.opacity(0.85))
        }
    }
}

// MARK: - 2c Drawing history

struct DrawingHistoryScreen: View {
    @EnvironmentObject var app: AppState
    let code: String

    var titleText: String {
        switch code {
        case "41-05": return "ND 41-05 · Staircase details"
        case "41-07": return "ND 41-07 · 2nd & 4th floor plan"
        default: return "ND 41-06 · Typical floor plan"
        }
    }

    var subtitleText: String {
        switch code {
        case "41-05": return "Structural · \(app.consultant) · covers stair, all floors"
        case "41-07": return "Structural · \(app.consultant) · covers 2nd, 4th floor"
        default: return "Structural · \(app.consultant) · covers 1st, 3rd floor"
        }
    }

    var body: some View {
        Screen(title: titleText, subtitle: subtitleText) {
            Text("Revisions").font(.sans(13, .semibold)).foregroundColor(T.text3)
            Card(padding: EdgeInsets(top: 4, leading: 14, bottom: 4, trailing: 14)) {
                VStack(spacing: 0) {
                    revision("R1", "Uploaded 18 Oct · example", "2 changes · 1 issue resolved · 1 new", chip: "Current", current: true)
                    Divider1()
                    revision("R0", "Dated 02 Apr 2026", "Kept for audit · 4 issues found", chip: "Superseded", current: false)
                }
            }
            SectionLabel(title: "Issues across revisions", hint: "4 open · 1 resolved")
            Card(padding: EdgeInsets(top: 4, leading: 14, bottom: 4, trailing: 14)) {
                VStack(spacing: 0) {
                    issue("Resolved R1", T.greenFg, T.greenBg, "B26 missing from schedule", "Found in R0 · now in the schedule")
                    Divider1()
                    issue("New in R1", T.accentDark, T.accentSoft, "B16/B22 bars changed, not clouded", "3-12T → 3-16T without a revision cloud")
                    Divider1()
                    issue("Open since R0", T.amberFg, T.amberBg, "Sunk depth 225/250 vs 350 mm", "Asked \(app.consultant) on 21 Oct")
                    Divider1()
                    issue("Open since R0", T.amberFg, T.amberBg, "Cover for beams, slabs not stated", "+1 more open")
                }
            }
        } bottom: {
            HStack(spacing: 10) {
                SecondaryButton(title: "Compare R0 · R1") { app.push(.revisionCompare) }
                PrimaryButton(title: "Upload new revision", fontSize: 15) { app.push(.targetUpload) }
            }
        }
    }

    func revision(_ r: String, _ title: String, _ detail: String, chip: String, current: Bool) -> some View {
        HStack(spacing: 12) {
            Text(r).font(.mono(15, .semibold)).foregroundColor(current ? T.ink : T.gray500).frame(width: 30, alignment: .leading)
            VStack(alignment: .leading, spacing: 2) {
                Text(title).font(.sans(15, .medium)).foregroundColor(current ? T.ink : T.text2)
                Text(detail).font(.sans(13)).foregroundColor(T.text2)
            }
            Spacer(minLength: 4)
            Chip(text: chip, fg: current ? T.greenFg : T.text3, bg: current ? T.greenBg : T.gray200, size: 11, hPad: 7, vPad: 3, radius: 5)
        }
        .padding(.vertical, 10)
    }

    func issue(_ chip: String, _ fg: Color, _ bg: Color, _ title: String, _ detail: String) -> some View {
        HStack(alignment: .top, spacing: 10) {
            Chip(text: chip, fg: fg, bg: bg, size: 11, hPad: 6, vPad: 2, radius: 4)
            VStack(alignment: .leading, spacing: 2) {
                Text(title).font(.sans(15, .medium)).foregroundColor(T.ink).fixedSize(horizontal: false, vertical: true)
                Text(detail).font(.sans(13)).foregroundColor(T.text2).fixedSize(horizontal: false, vertical: true)
            }
            Spacer(minLength: 0)
        }
        .padding(.vertical, 10)
    }
}

// MARK: - 3 Reading drawings

struct ReadingScreen: View {
    @EnvironmentObject var app: AppState
    @State var progress: Double = 0
    @State var finished = false

    let steps: [(String, String?)] = [
        ("Scale calibrated", "From 230×500 columns · checked on C11 = 600 mm"),
        ("Schedules read", "17 beam rows · 4 slab marks · general notes"),
        ("Measuring beams and slab panels", "Matching beam marks to the plan"),
        ("Check for gaps and conflicts", "B26 not in schedule · sunk depth differs"),
    ]
    let ends: [Double] = [0.15, 0.32, 0.85, 1.0]

    var phase: String {
        if progress < ends[0] { return "Calibrating scale" }
        if progress < ends[1] { return "Reading schedules" }
        if progress < ends[2] { return "Measuring spans" }
        if progress < 1.0 { return "Checking for gaps" }
        return "Done"
    }

    var body: some View {
        let beams = min(77, Int((progress / ends[2]) * 77))
        Screen(title: "Reading drawings", spacing: 16) {
            VStack(spacing: 0) {
                ZStack(alignment: .bottomLeading) {
                    PlanDrawing(progress: min(1, progress / ends[2]))
                        .frame(height: 220)
                        .background(Color(hex: 0xFAFAFA))
                    Text("ND 41-06 · \(beams) of 77 beams")
                        .font(.mono(12))
                        .foregroundColor(.white)
                        .padding(.horizontal, 8).padding(.vertical, 4)
                        .background(RoundedRectangle(cornerRadius: 6).fill(T.ink))
                        .padding(.leading, 12).padding(.bottom, 10)
                }
                Rectangle().fill(T.border).frame(height: 1)
                VStack(spacing: 8) {
                    HStack {
                        Text(phase).font(.sans(15, .semibold)).foregroundColor(T.ink)
                        Spacer()
                        Text("\(Int(progress * 100))%").font(.mono(14)).foregroundColor(T.text2)
                    }
                    GeometryReader { g in
                        ZStack(alignment: .leading) {
                            Capsule().fill(T.gray200)
                            Capsule().fill(T.accent).frame(width: g.size.width * progress)
                        }
                    }
                    .frame(height: 8)
                }
                .padding(.horizontal, 16).padding(.vertical, 14)
            }
            .background(RoundedRectangle(cornerRadius: 14).fill(Color.white))
            .clipShape(RoundedRectangle(cornerRadius: 14))
            .overlay(RoundedRectangle(cornerRadius: 14).strokeBorder(T.border, lineWidth: 1))

            Card(padding: EdgeInsets(top: 6, leading: 16, bottom: 6, trailing: 16)) {
                VStack(spacing: 0) {
                    ForEach(0..<steps.count, id: \.self) { i in
                        stepRow(i)
                        if i < steps.count - 1 { Divider1() }
                    }
                }
            }
        } bottom: {
            Text(finished ? "All read. Opening the review." : "About a minute. We'll notify you when it's ready.")
                .font(.sans(12)).foregroundColor(T.text2).multilineTextAlignment(.center)
            if finished {
                PrimaryButton(title: "Review members") { app.replaceTop(with: .review) }
            } else {
                SecondaryButton(title: "Review as it reads", fontSize: 16) { app.replaceTop(with: .review) }
            }
        }
        .task {
            if finished { return }
            let total = 5.0
            let tick = 0.05
            while progress < 1.0 {
                try? await Task.sleep(nanoseconds: UInt64(tick * 1_000_000_000))
                if Task.isCancelled { return }
                withAnimation(.linear(duration: tick)) {
                    progress = min(1.0, progress + tick / total)
                }
            }
            finished = true
            try? await Task.sleep(nanoseconds: 700_000_000)
            if Task.isCancelled { return }
            if app.path.last == .reading {
                app.replaceTop(with: .review)
            }
        }
    }

    @ViewBuilder
    func stepRow(_ i: Int) -> some View {
        let start = i == 0 ? 0 : ends[i - 1]
        let done = progress >= ends[i]
        let active = !done && progress >= start
        HStack(alignment: .top, spacing: 12) {
            ZStack {
                if done {
                    Image(systemName: "checkmark").font(.system(size: 16, weight: .bold)).foregroundColor(Color(hex: 0x15803D))
                } else if active {
                    ProgressView().tint(T.accent)
                } else {
                    Circle().strokeBorder(T.gray400, lineWidth: 2).frame(width: 18, height: 18)
                }
            }
            .frame(width: 22, height: 22)
            VStack(alignment: .leading, spacing: 2) {
                Text(steps[i].0).font(.sans(15, .medium)).foregroundColor(done || active ? T.ink : T.text2)
                if let d = steps[i].1, done || active {
                    Text(d).font(.sans(13)).foregroundColor(T.text2).fixedSize(horizontal: false, vertical: true)
                }
            }
            Spacer(minLength: 0)
        }
        .padding(.vertical, 10)
    }
}

/// Plan drawing with beams turning orange as they are measured.
struct PlanDrawing: View {
    var progress: Double

    var body: some View {
        Canvas { ctx, size in
            let x0: CGFloat = 20, y0: CGFloat = 20
            let w = size.width - 40, h = size.height - 40
            func p(_ fx: CGFloat, _ fy: CGFloat) -> CGPoint { CGPoint(x: x0 + w * fx, y: y0 + h * fy) }
            // outline
            ctx.stroke(Path(CGRect(x: x0, y: y0, width: w, height: h)), with: .color(T.gray500), lineWidth: 1)
            // beams: (from, to)
            let beams: [(CGPoint, CGPoint)] = [
                (p(0.21, 0), p(0.21, 1)), (p(0.42, 0), p(0.42, 1)), (p(0.58, 0), p(0.58, 1)), (p(0.79, 0), p(0.79, 1)),
                (p(0, 0.22), p(0.42, 0.22)), (p(0.58, 0.22), p(1, 0.22)),
                (p(0, 0.78), p(0.42, 0.78)), (p(0.58, 0.78), p(1, 0.78)),
                (p(0, 0.5), p(0.21, 0.5)), (p(0.42, 0.5), p(0.58, 0.5)), (p(0.79, 0.5), p(1, 0.5)),
            ]
            let lit = Int((Double(beams.count) * progress).rounded(.down))
            for (i, b) in beams.enumerated() {
                var path = Path()
                path.move(to: b.0); path.addLine(to: b.1)
                let isLit = i < lit
                ctx.stroke(path, with: .color(isLit ? Color(hex: 0xD2602E) : T.gray400), lineWidth: isLit ? 6 : 3)
            }
            // lift void
            let v = CGRect(x: p(0.44, 0.25).x, y: p(0.44, 0.25).y, width: w * 0.12, height: h * 0.22)
            ctx.stroke(Path(v), with: .color(T.gray500), style: StrokeStyle(lineWidth: 1, dash: [3, 2]))
            var x = Path()
            x.move(to: CGPoint(x: v.minX, y: v.minY)); x.addLine(to: CGPoint(x: v.maxX, y: v.maxY))
            x.move(to: CGPoint(x: v.maxX, y: v.minY)); x.addLine(to: CGPoint(x: v.minX, y: v.maxY))
            ctx.stroke(x, with: .color(T.gray500), lineWidth: 1)
            // columns
            for c in [p(0, 0), p(0.42, 0), p(0.58, 0), p(1, 0), p(1, 1), p(0, 1)] {
                ctx.fill(Path(CGRect(x: c.x - 4, y: c.y - 6, width: 8, height: 12)), with: .color(T.gray500))
            }
        }
    }
}
