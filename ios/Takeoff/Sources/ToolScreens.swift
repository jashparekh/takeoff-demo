import SwiftUI

// MARK: - 7 Cutting plan

struct CutPiece {
    let frac: CGFloat
    let label: String
    let bg: Color
    let fg: Color
}

struct CutPattern {
    let title: String
    let offcut: String
    let pieces: [CutPiece]
    let note: String
}

struct CuttingPlanScreen: View {
    @EnvironmentObject var app: AppState

    static let orange = (Color(hex: 0xFED7AA), Color(hex: 0x7C2D12))
    static let blue = (Color(hex: 0xBFDBFE), Color(hex: 0x1E3A8A))
    static let green = (Color(hex: 0xBBF7D0), Color(hex: 0x14532D))
    static let purple = (Color(hex: 0xE9D5FF), Color(hex: 0x4C1D95))
    static let yellow = (Color(hex: 0xFDE68A), Color(hex: 0x713F12))

    static func piece(_ f: CGFloat, _ l: String, _ c: (Color, Color)) -> CutPiece {
        CutPiece(frac: f, label: l, bg: c.0, fg: c.1)
    }

    let sizes = ["8 mm", "10 mm", "12 mm", "16 mm"]

    // (stock bars, offcut %, saved kg, patterns)
    var data: (String, String, String, [CutPattern]) {
        let P = CuttingPlanScreen.self
        switch app.cutSize {
        case 0:
            return ("182", "2.1%", "64 kg", [
                CutPattern(title: "A · ×120 bars", offcut: "offcut 180 mm",
                           pieces: [P.piece(0.329, "3,940", P.orange), P.piece(0.329, "3,940", P.orange), P.piece(0.327, "3,920", P.blue)],
                           note: "Slab S1/S2 short span bars"),
                CutPattern(title: "B · ×62 bars", offcut: "offcut 220 mm",
                           pieces: [P.piece(0.403, "4,840", P.green), P.piece(0.403, "4,840", P.green), P.piece(0.176, "2,100", P.purple)],
                           note: "Slab long span bars · stirrup lengths"),
            ])
        case 1:
            return ("14", "1.6%", "7 kg", [
                CutPattern(title: "A · ×14 bars", offcut: "offcut 190 mm",
                           pieces: [P.piece(0.49, "5,880", P.blue), P.piece(0.49, "5,880", P.blue)],
                           note: "B7-group bottom extras"),
            ])
        case 3:
            return ("71", "0.9%", "41 kg", [
                CutPattern(title: "A · ×40 bars", offcut: "offcut 88 mm",
                           pieces: [P.piece(0.5, "6,002", P.yellow), P.piece(0.493, "5,910", P.blue)],
                           note: "B16/B22 bottom bars (R1) · B12-group top bars"),
                CutPattern(title: "B · ×31 bars", offcut: "offcut 130 mm",
                           pieces: [P.piece(0.395, "4,740", P.orange), P.piece(0.395, "4,740", P.orange), P.piece(0.199, "2,390", P.green)],
                           note: "B7-group bottom bars · end extras"),
            ])
        default:
            return ("28", "1.2%", "18 kg", [
                CutPattern(title: "A · ×18 bars", offcut: "offcut 96 mm",
                           pieces: [P.piece(0.252, "3,021", P.orange), P.piece(0.252, "3,021", P.orange), P.piece(0.252, "3,021", P.orange), P.piece(0.236, "2,841", P.blue)],
                           note: "B1-group bottom bars · B44-group top bars"),
                CutPattern(title: "B · ×6 bars", offcut: "offcut 116 mm",
                           pieces: [P.piece(0.454, "5,452", P.green), P.piece(0.454, "5,452", P.green), P.piece(0.082, "980", P.purple)],
                           note: "B7-group top bars · stair landing bars"),
                CutPattern(title: "C · ×4 bars", offcut: "offcut 139 mm",
                           pieces: [P.piece(0.612, "7,341", P.yellow), P.piece(0.376, "4,520", P.blue)],
                           note: "B16/B22 bottom bars · B12-group top bars"),
            ])
        }
    }

    var body: some View {
        let d = data
        Screen(title: "Cutting plan", subtitle: "From 12 m stock bars · Bldg 2 typical floor") {
            HStack(spacing: 6) {
                ForEach(0..<sizes.count, id: \.self) { i in
                    Button {
                        app.cutSize = i
                    } label: {
                        Text(sizes[i]).font(.sans(14, app.cutSize == i ? .semibold : .medium))
                            .foregroundColor(app.cutSize == i ? .white : T.text3)
                            .frame(maxWidth: .infinity).frame(height: 44)
                            .background(RoundedRectangle(cornerRadius: 10).fill(app.cutSize == i ? T.ink : Color.white))
                            .overlay(RoundedRectangle(cornerRadius: 10).strokeBorder(app.cutSize == i ? T.ink : T.gray300, lineWidth: 1))
                            .contentShape(Rectangle())
                    }
                    .buttonStyle(PressStyle())
                }
            }
            HStack(alignment: .top, spacing: 8) {
                Stat(label: "Stock bars", value: d.0, dark: true, valueSize: 20, weight: .semibold)
                Stat(label: "Offcut", value: d.1, dark: true, valueSize: 20, weight: .semibold)
                Stat(label: "Saved vs uncut", value: d.2, dark: true, valueSize: 20, weight: .semibold)
            }
            .padding(.horizontal, 16).padding(.vertical, 14)
            .background(RoundedRectangle(cornerRadius: 14).fill(T.ink))

            Card(padding: EdgeInsets(top: 14, leading: 16, bottom: 14, trailing: 16)) {
                VStack(alignment: .leading, spacing: 14) {
                    Text("Cut patterns").font(.sans(15, .semibold)).foregroundColor(T.ink)
                    ForEach(Array(d.3.enumerated()), id: \.offset) { _, p in
                        patternView(p)
                    }
                }
            }
            Text("Lengths in mm. Laps placed at low-stress zones per IS 456. Example figures.")
                .font(.sans(12)).foregroundColor(T.text2)
        } bottom: {
            HStack(spacing: 10) {
                SecondaryButton(title: "Cut list PDF") { app.exportNotAvailable() }
                PrimaryButton(title: "Order \(d.0) bars", fontSize: 15) { app.push(.purchaseList) }
            }
        }
    }

    func patternView(_ p: CutPattern) -> some View {
        VStack(alignment: .leading, spacing: 6) {
            HStack {
                Text(p.title).font(.sans(14, .semibold)).foregroundColor(T.ink)
                Spacer()
                Text(p.offcut).font(.mono(13)).foregroundColor(T.text2)
            }
            GeometryReader { g in
                HStack(spacing: 0) {
                    ForEach(Array(p.pieces.enumerated()), id: \.offset) { _, piece in
                        Text(piece.label).font(.mono(11)).foregroundColor(piece.fg)
                            .lineLimit(1).minimumScaleFactor(0.6)
                            .frame(width: max(0, g.size.width * piece.frac - 1), height: 26)
                            .background(piece.bg)
                        Rectangle().fill(Color.white).frame(width: 1, height: 26)
                    }
                    Rectangle().fill(T.gray200).frame(height: 26)
                }
            }
            .frame(height: 26)
            .clipShape(RoundedRectangle(cornerRadius: 4))
            .overlay(RoundedRectangle(cornerRadius: 4).strokeBorder(T.gray400, lineWidth: 1))
            Text(p.note).font(.sans(12)).foregroundColor(T.text2)
        }
    }
}

// MARK: - 8 Revision compare

struct RevisionCompareScreen: View {
    @EnvironmentObject var app: AppState

    var body: some View {
        Screen(title: "What changed in R1", subtitle: "ND 41-06 · R0 (02 Apr) vs R1 (example)") {
            HStack(alignment: .top, spacing: 8) {
                deltaStat("Steel", "+72 kg", "3.79 → 3.86 t", T.accentDark)
                deltaStat("Concrete", "0.0 m³", "no change", T.ink)
                deltaStat("Cost", "+₹4,900", "+₹2 / sq ft", T.accentDark)
            }
            .padding(.horizontal, 16).padding(.vertical, 14)
            .background(RoundedRectangle(cornerRadius: 14).fill(Color.white))
            .overlay(RoundedRectangle(cornerRadius: 14).strokeBorder(T.border, lineWidth: 1))

            Text("3 members changed").font(.sans(13, .semibold)).foregroundColor(T.text3)

            change("B16, B22", "+46 kg", from: "Bot 3-12T", to: "Bot 3-16T")
            change("B44, B45, B53, B54", "+26 kg", from: "8T @100/150", to: "8T @100/125")

            Card(radius: 12) {
                VStack(alignment: .leading, spacing: 6) {
                    HStack {
                        Text("B26").font(.mono(15, .semibold)).foregroundColor(T.ink)
                        Spacer()
                        Text("resolved").font(.mono(14, .semibold)).foregroundColor(T.greenFg)
                    }
                    Text("Now in the beam schedule. Bars match the values you entered for R0, no quantity change.")
                        .font(.sans(14)).foregroundColor(T.text3).fixedSize(horizontal: false, vertical: true)
                }
            }

            HStack(alignment: .top, spacing: 8) {
                Image(systemName: "info.circle").font(.system(size: 14)).foregroundColor(T.text2)
                Text("70 members unchanged. Changes are matched by beam mark, so renumbered beams are flagged for review.")
                    .font(.sans(13)).foregroundColor(T.text2).fixedSize(horizontal: false, vertical: true)
            }
            .padding(.horizontal, 2)
        } bottom: {
            HStack(spacing: 10) {
                SecondaryButton(title: "Share change note") { app.exportNotAvailable() }
                PrimaryButton(title: "Use R1", fontSize: 15) {
                    app.showToast("Estimate updated to R1")
                    app.pop()
                }
            }
        }
    }

    func deltaStat(_ label: String, _ value: String, _ sub: String, _ color: Color) -> some View {
        VStack(alignment: .leading, spacing: 2) {
            Text(label).font(.sans(12)).foregroundColor(T.text2)
            Text(value).font(.mono(18, .semibold)).foregroundColor(color).lineLimit(1).minimumScaleFactor(0.7)
            Text(sub).font(.mono(11)).foregroundColor(T.text2).lineLimit(1).minimumScaleFactor(0.7)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }

    func change(_ marks: String, _ delta: String, from: String, to: String) -> some View {
        Card(radius: 12) {
            VStack(alignment: .leading, spacing: 6) {
                HStack {
                    Text(marks).font(.mono(15, .semibold)).foregroundColor(T.ink)
                    Spacer()
                    Text(delta).font(.mono(14, .semibold)).foregroundColor(T.accentDark)
                }
                HStack(spacing: 6) {
                    Text(from).font(.sans(14)).strikethrough().foregroundColor(T.gray500)
                    Text("→").font(.sans(14)).foregroundColor(T.ink).accessibilityHidden(true)
                    Text(to).font(.sans(14, .medium)).foregroundColor(T.accentDark)
                        .padding(.horizontal, 6).padding(.vertical, 1)
                        .background(RoundedRectangle(cornerRadius: 4).fill(T.accentSoft))
                }
            }
        }
    }
}

// MARK: - 9 Drawing check

struct DrawingCheckScreen: View {
    @EnvironmentObject var app: AppState

    var body: some View {
        Screen(title: "Drawing check", subtitle: "Bldg 2 · 6 sheets · \(app.consultant) + architect", spacing: 8) {
            HStack(spacing: 8) {
                Chip(text: "2 conflicts", fg: T.accentDark, bg: T.accentSoft, size: 12, hPad: 10, vPad: 6, radius: 14)
                Chip(text: "2 missing", fg: T.amberFg, bg: T.amberBg, size: 12, hPad: 10, vPad: 6, radius: 14)
                Chip(text: "1 minor", fg: T.text3, bg: T.gray200, size: 12, hPad: 10, vPad: 6, radius: 14)
            }
            .padding(.bottom, 2)

            finding("Conflict", T.accentDark, T.accentSoft, "Beam B26 not in schedule",
                    "Shown on ND 41-06 plan between the two stair stub posts. Row \"B48, B51, B52, B59\" lists B59 twice; B26 likely meant.",
                    "ND 41-06 · plan + beam schedule")
            finding("Conflict", T.accentDark, T.accentSoft, "Sunk depth at WCs",
                    "Structural legend: 225 and 250 mm. Architectural Sheet 1: 350 mm sunk.",
                    "ND 41-06 legend · Arch Sheet 1")
            finding("Missing", T.amberFg, T.amberBg, "Clear cover for beams, slabs",
                    "Notes give foundation cover (50 mm) only.", "ND 41-06 · note 8")
            finding("Missing", T.amberFg, T.amberBg, "Slab top bars at supports",
                    "Slab schedule gives bottom bars only. Extra top steel not detailed.", "ND 41-06 · slab schedule")
            finding("Minor", T.text3, T.gray200, "Taluka differs on title blocks",
                    "Khalapur (structural) vs Panvel (architectural).", nil)
        } bottom: {
            HStack(spacing: 10) {
                SecondaryButton(title: "PDF report") { app.exportNotAvailable() }
                PrimaryButton(title: "Send to consultant", fontSize: 15) {
                    app.showToast("Demo: nothing is sent to \(app.consultant)")
                }
            }
        }
    }

    func finding(_ kind: String, _ fg: Color, _ bg: Color, _ title: String, _ body: String, _ source: String?) -> some View {
        Card(radius: 12) {
            VStack(alignment: .leading, spacing: 4) {
                HStack(spacing: 8) {
                    Chip(text: kind, fg: fg, bg: bg, size: 11, hPad: 6, vPad: 2, radius: 4)
                    Text(title).font(.sans(15, .semibold)).foregroundColor(T.ink)
                }
                Text(body).font(.sans(14)).foregroundColor(T.text3).fixedSize(horizontal: false, vertical: true)
                if let s = source {
                    Text(s).font(.mono(11)).foregroundColor(T.text2)
                }
            }
        }
    }
}

// MARK: - 10 Purchase list

struct PurchaseListScreen: View {
    @EnvironmentObject var app: AppState
    @State var dealer = 0

    var body: some View {
        Screen(title: "Order for next pour", subtitle: "Bldg 2 · 3rd floor slab + beams") {
            VStack(alignment: .leading, spacing: 6) {
                Text("Casting date").font(.sans(13, .semibold)).foregroundColor(T.text3)
                HStack {
                    DatePicker("Casting date", selection: $app.pourDate, displayedComponents: .date)
                        .labelsHidden()
                        .tint(T.accent)
                    Spacer()
                }
                .padding(.horizontal, 10)
                .frame(height: 50)
                .background(RoundedRectangle(cornerRadius: 10).fill(Color.white))
                .overlay(RoundedRectangle(cornerRadius: 10).strokeBorder(T.gray300, lineWidth: 1))
            }

            Card(padding: EdgeInsets(top: 4, leading: 16, bottom: 4, trailing: 16)) {
                VStack(spacing: 0) {
                    line("TMT 8 mm", "2,128 kg")
                    Divider1()
                    line("TMT 10 mm", "169 kg")
                    Divider1()
                    line("TMT 12 mm · 28 bars", "392 kg")
                    Divider1()
                    line("TMT 16 mm", "1,103 kg")
                    Divider1()
                    line("Ready-mix M30", "41.7 m³")
                    Divider1()
                    line("or cement, if site-mixed", "317 bags", muted: true)
                }
            }

            Text("Quotes from dealers near the site").font(.sans(13, .semibold)).foregroundColor(T.text3)
            dealerCard(0, "Dealer A · Panvel", "Steel delivered in 2 days", "₹2.49 L", lowest: true)
            dealerCard(1, "Dealer B · Navi Mumbai", "Steel delivered in 3 days", "₹2.56 L", lowest: false)
            Text("Example quotes. Dealers see quantities and site area only, not your drawings.")
                .font(.sans(12)).foregroundColor(T.text2)
        } bottom: {
            PrimaryButton(title: "Request quotes") {
                app.showToast("Demo: quote requests are not sent")
            }
        }
    }

    func line(_ label: String, _ value: String, muted: Bool = false) -> some View {
        HStack {
            Text(label).font(.sans(15))
            Spacer()
            Text(value).font(.mono(15))
        }
        .foregroundColor(muted ? T.text2 : T.ink)
        .padding(.vertical, 10)
    }

    func dealerCard(_ i: Int, _ name: String, _ detail: String, _ price: String, lowest: Bool) -> some View {
        Button {
            dealer = i
        } label: {
            HStack {
                VStack(alignment: .leading, spacing: 2) {
                    Text(name).font(.sans(15, .semibold)).foregroundColor(T.ink)
                    Text(detail).font(.sans(13)).foregroundColor(T.text2)
                }
                Spacer()
                VStack(alignment: .trailing, spacing: 2) {
                    Text(price).font(.mono(15, .semibold)).foregroundColor(T.ink)
                    if lowest {
                        Text("Lowest").font(.sans(12, .semibold)).foregroundColor(T.greenFg)
                    }
                }
            }
            .padding(.horizontal, 14).padding(.vertical, 12)
            .background(RoundedRectangle(cornerRadius: 12).fill(Color.white))
            .overlay(RoundedRectangle(cornerRadius: 12).strokeBorder(dealer == i ? T.accent : T.border, lineWidth: dealer == i ? 2 : 1))
            .contentShape(Rectangle())
        }
        .buttonStyle(PressStyle())
    }
}

// MARK: - 11 Site reconciliation

struct SiteReconScreen: View {
    @EnvironmentObject var app: AppState

    var body: some View {
        Screen(title: "Steel used vs planned", subtitle: "Bldg 2 · 1st floor slab, cast · example") {
            HStack(alignment: .top, spacing: 10) {
                Image(systemName: "exclamationmark.triangle").font(.system(size: 16)).foregroundColor(Color(hex: 0xB45309))
                (Text("8 mm issued 8% over plan. ").fontWeight(.semibold)
                 + Text("172 kg more than the BBS plus 3% wastage. Check stirrup offcuts and stock at the yard."))
                    .font(.sans(14)).foregroundColor(Color(hex: 0x78350F))
                    .fixedSize(horizontal: false, vertical: true)
            }
            .padding(.horizontal, 14).padding(.vertical, 12)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(RoundedRectangle(cornerRadius: 12).fill(T.amberBg))
            .overlay(RoundedRectangle(cornerRadius: 12).strokeBorder(Color(hex: 0xFCD34D), lineWidth: 1))

            Card(padding: EdgeInsets(top: 14, leading: 16, bottom: 14, trailing: 16)) {
                VStack(alignment: .leading, spacing: 12) {
                    HStack {
                        Text("By bar size").font(.sans(14, .semibold)).foregroundColor(T.ink)
                        Spacer()
                        Text("planned · issued").font(.sans(13)).foregroundColor(T.text2)
                    }
                    bars("8 mm", "2,128", "2,300", 0.925, 1.0, over: true)
                    bars("16 mm", "1,103", "1,110", 0.48, 0.483, over: false)
                    bars("12 mm", "392", "395", 0.17, 0.172, over: false)
                    bars("10 mm", "169", "168", 0.073, 0.073, over: false)
                    HStack(spacing: 14) {
                        legend(T.gray300, "Planned (BBS)")
                        legend(T.gray500, "Issued from store")
                    }
                }
            }

            HStack {
                VStack(alignment: .leading, spacing: 2) {
                    Text("Issue log").font(.sans(15, .semibold)).foregroundColor(T.ink)
                    Text("9 entries · last by site store, photo of challan").font(.sans(13)).foregroundColor(T.text2)
                }
                Spacer()
                Button {
                    app.showToast("Demo: the issue log is sample data")
                } label: {
                    Text("View").font(.sans(15, .semibold)).foregroundColor(T.link).frame(minWidth: 44, minHeight: 44)
                }
            }
            .padding(.horizontal, 14).padding(.vertical, 8)
            .background(RoundedRectangle(cornerRadius: 12).fill(Color.white))
            .overlay(RoundedRectangle(cornerRadius: 12).strokeBorder(T.border, lineWidth: 1))
        } bottom: {
            PrimaryButton(title: "Log steel issued", icon: "plus") {
                app.showToast("Demo: logging is not available")
            }
        }
    }

    func bars(_ size: String, _ planned: String, _ issued: String, _ pf: CGFloat, _ iff: CGFloat, over: Bool) -> some View {
        VStack(alignment: .leading, spacing: 4) {
            HStack {
                Text(size).font(.mono(13)).foregroundColor(T.ink)
                Spacer()
                (Text(planned + " · ").foregroundColor(T.ink)
                 + Text(issued).foregroundColor(over ? T.accentDark : T.ink).fontWeight(over ? .semibold : .regular))
                    .font(.mono(13))
            }
            GeometryReader { g in
                VStack(alignment: .leading, spacing: 4) {
                    Capsule().fill(T.gray300).frame(width: g.size.width * pf, height: 8)
                    Capsule().fill(over ? T.accent : T.gray500).frame(width: g.size.width * iff, height: 8)
                }
            }
            .frame(height: 20)
        }
    }

    func legend(_ c: Color, _ text: String) -> some View {
        HStack(spacing: 6) {
            Capsule().fill(c).frame(width: 14, height: 8)
            Text(text).font(.sans(12)).foregroundColor(T.text2)
        }
    }
}
