import SwiftUI

// MARK: - 5 Values we need

struct IssuesScreen: View {
    @EnvironmentObject var app: AppState
    @State var askOther = false
    @State var otherText = ""

    let sunkOptions = ["225/250 mm", "350 mm"]

    var body: some View {
        let missing = app.emptyValueCount
        let cementFilled = !app.cement.trimmingCharacters(in: .whitespaces).isEmpty
        let coverFilled = !app.coverBeams.trimmingCharacters(in: .whitespaces).isEmpty
            && !app.coverSlabs.trimmingCharacters(in: .whitespaces).isEmpty
        let subtitle = missing == 0 ? "Building 2 · all 3 filled" : "Building 2 · \(missing) of 3 still empty"

        Screen(title: "Values we need", subtitle: subtitle, spacing: 10) {
            Text("The drawings don't give these. We never fill in a value for you: enter each one, or ask the consultant and come back.")
                .font(.sans(14)).foregroundColor(T.text3).lineSpacing(2)
                .fixedSize(horizontal: false, vertical: true)

            valueCard(title: "Cement content for M30", filled: cementFilled, emptyChip: "Not entered") {
                Text("From your mix design or the RMC supplier's sheet.").font(.sans(13)).foregroundColor(T.text2)
                HStack(spacing: 8) {
                    BoxField(placeholder: "Enter", text: $app.cement, width: 110)
                        .accessibilityLabel("Cement content, kg per cubic metre")
                    Text("kg per m³").font(.sans(14)).foregroundColor(T.text3)
                }
            }

            valueCard(title: "Sunk slab depth at WCs", filled: app.sunk != nil, emptyChip: "Drawings disagree") {
                Text("Structural ND 41-06 says 225/250 mm, architectural section says 350 mm. Pick one or ask.")
                    .font(.sans(13)).foregroundColor(T.text2).fixedSize(horizontal: false, vertical: true)
                HStack(spacing: 8) {
                    ForEach(sunkOptions, id: \.self) { o in
                        choice(o, selected: app.sunk == o) { app.sunk = o }
                    }
                    let isOther = app.sunk != nil && !sunkOptions.contains(app.sunk ?? "")
                    choice(isOther ? (app.sunk ?? "Other") : "Other", selected: isOther) {
                        otherText = ""
                        askOther = true
                    }
                }
            }

            valueCard(title: "Clear cover", filled: coverFilled, emptyChip: "Not on drawing") {
                HStack(spacing: 8) {
                    VStack(alignment: .leading, spacing: 4) {
                        Text("Beams (mm)").font(.sans(13)).foregroundColor(T.text3)
                        BoxField(placeholder: "Enter", text: $app.coverBeams)
                    }
                    VStack(alignment: .leading, spacing: 4) {
                        Text("Slabs (mm)").font(.sans(13)).foregroundColor(T.text3)
                        BoxField(placeholder: "Enter", text: $app.coverSlabs)
                    }
                }
            }

            if missing > 0 {
                Button {
                    app.showToast("Demo: question saved for \(app.consultant)")
                } label: {
                    HStack(spacing: 10) {
                        Image(systemName: "bubble.left").font(.system(size: 18))
                        Text("Ask \(app.consultant) the \(missing) empty one\(missing == 1 ? "" : "s")")
                            .font(.sans(15, .medium))
                    }
                    .foregroundColor(T.link)
                    .frame(minHeight: 44)
                    .contentShape(Rectangle())
                }
                .buttonStyle(PressStyle())
                .padding(.horizontal, 2)
            }
        } bottom: {
            Text("Nothing is calculated until every value is filled and confirmed.")
                .font(.sans(12)).foregroundColor(T.text2).multilineTextAlignment(.center)
            PrimaryButton(title: missing == 0 ? "Review all values" : "Fill \(missing) more to review all values",
                          enabled: missing == 0) {
                hideKeyboard()
                app.valuesChecked = false
                app.push(.confirm)
            }
        }
        .toolbar {
            ToolbarItemGroup(placement: .keyboard) {
                Spacer()
                Button("Done") { hideKeyboard() }
            }
        }
        .alert("Sunk depth at WCs", isPresented: $askOther) {
            TextField("Depth in mm", text: $otherText)
                .keyboardType(.numberPad)
            Button("Cancel", role: .cancel) {}
            Button("Use") {
                let v = otherText.trimmingCharacters(in: .whitespaces)
                if !v.isEmpty { app.sunk = v + " mm" }
            }
        } message: {
            Text("Enter the depth your consultant gave you.")
        }
    }

    func valueCard<C: View>(title: String, filled: Bool, emptyChip: String, @ViewBuilder _ content: () -> C) -> some View {
        VStack(alignment: .leading, spacing: 10) {
            HStack(alignment: .top, spacing: 8) {
                Text(title).font(.sans(15, .semibold)).foregroundColor(T.ink)
                Spacer(minLength: 4)
                if filled {
                    Chip(text: "You entered", fg: T.greenFg, bg: T.greenBg, size: 11, hPad: 7, vPad: 3, radius: 5)
                } else {
                    Chip(text: emptyChip, fg: T.accentDark, bg: T.accentSoft, size: 11, hPad: 7, vPad: 3, radius: 5)
                }
            }
            content()
        }
        .padding(14)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(RoundedRectangle(cornerRadius: 12).fill(Color.white))
        .overlay(RoundedRectangle(cornerRadius: 12).strokeBorder(filled ? T.border : T.accent, lineWidth: filled ? 1 : 2))
    }

    func choice(_ title: String, selected: Bool, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Text(title).font(.sans(14, selected ? .semibold : .medium))
                .foregroundColor(selected ? .white : T.text3)
                .lineLimit(1).minimumScaleFactor(0.7)
                .frame(maxWidth: .infinity).frame(height: 44)
                .background(RoundedRectangle(cornerRadius: 10).fill(selected ? T.ink : Color.white))
                .overlay(RoundedRectangle(cornerRadius: 10).strokeBorder(selected ? T.ink : T.gray300, lineWidth: 1))
                .contentShape(Rectangle())
        }
        .buttonStyle(PressStyle())
    }
}

// MARK: - 5a Confirm

struct EditTarget: Identifiable {
    let id: String
    let title: String
    let fields: [(String, ReferenceWritableKeyPath<AppState, String>)]
    var numeric: Bool = true
}

struct ConfirmScreen: View {
    @EnvironmentObject var app: AppState
    @State var editing: EditTarget? = nil

    var body: some View {
        Screen(title: "Confirm before calculating", subtitle: "Building 2 · typical floor · tap any value to change it", spacing: 10) {
            HStack {
                Text("Read from drawings").font(.sans(13, .semibold)).foregroundColor(T.text3)
                Spacer()
                Text("source shown").font(.sans(13)).foregroundColor(T.text2)
            }
            group {
                row("Concrete · steel grade", "General note · ND 41-06", app.grades) {
                    EditTarget(id: "grades", title: "Concrete · steel grade", fields: [("Grades", \AppState.grades)], numeric: false)
                }
                Divider1()
                Button {
                    app.push(.review)
                } label: {
                    HStack(spacing: 10) {
                        VStack(alignment: .leading, spacing: 1) {
                            Text("Beams, slabs, bars, spans").font(.sans(15)).foregroundColor(T.ink)
                            Text("Schedules + plan · checked in review").font(.sans(12)).foregroundColor(T.text2)
                        }
                        Spacer()
                        Text("Open ›").font(.sans(14, .semibold)).foregroundColor(T.link)
                    }
                    .frame(minHeight: 48)
                    .contentShape(Rectangle())
                }
                .buttonStyle(PressStyle())
            }

            Text("Entered by you").font(.sans(13, .semibold)).foregroundColor(T.text3)
            group {
                row("Cement content", nil, "\(app.cement) kg/m³") {
                    EditTarget(id: "cement", title: "Cement content (kg/m³)", fields: [("kg per m³", \AppState.cement)])
                }
                Divider1()
                row("Sunk depth at WCs", nil, app.sunk ?? "—") {
                    EditTarget(id: "sunk", title: "Sunk depth at WCs", fields: [("Depth", \AppState.sunkText)], numeric: false)
                }
                Divider1()
                row("Clear cover, beams / slabs", nil, "\(app.coverBeams) / \(app.coverSlabs) mm") {
                    EditTarget(id: "cover", title: "Clear cover (mm)", fields: [("Beams (mm)", \AppState.coverBeams), ("Slabs (mm)", \AppState.coverSlabs)])
                }
                Divider1()
                row("Wastage, steel / concrete", nil, app.wastage) {
                    EditTarget(id: "wastage", title: "Wastage, steel / concrete", fields: [("Wastage", \AppState.wastage)], numeric: false)
                }
            }

            Text("Rates and area").font(.sans(13, .semibold)).foregroundColor(T.text3)
            group {
                row("TMT 8 / 10 / 12 / 16 mm", "Mumbai price list · Aug 2026", app.tmtRates, small: true) {
                    EditTarget(id: "tmt", title: "TMT rates (₹ per kg)", fields: [("8 / 10 / 12 / 16 mm", \AppState.tmtRates)], numeric: false)
                }
                Divider1()
                row("RMC M30 · OPC bag", "Mumbai price list · Aug 2026", app.rmcRates, small: true) {
                    EditTarget(id: "rmc", title: "RMC M30 · OPC bag", fields: [("Rates", \AppState.rmcRates)], numeric: false)
                }
                Divider1()
                row("Floor area for ₹/sq ft", "Measured on Arch Sheet 1", "\(app.floorArea) sq ft", small: true) {
                    EditTarget(id: "area", title: "Floor area (sq ft)", fields: [("sq ft", \AppState.floorArea)], numeric: false)
                }
            }
        } bottom: {
            Button {
                app.valuesChecked.toggle()
            } label: {
                HStack(spacing: 10) {
                    CheckBox(checked: app.valuesChecked)
                    Text("I've checked these values. Changing one later brings me back here.")
                        .font(.sans(14)).foregroundColor(T.text3).multilineTextAlignment(.leading)
                        .fixedSize(horizontal: false, vertical: true)
                    Spacer(minLength: 0)
                }
                .frame(minHeight: 44)
                .contentShape(Rectangle())
            }
            .buttonStyle(.plain)
            PrimaryButton(title: "Confirm and generate report", enabled: app.valuesChecked) {
                app.push(.results)
            }
        }
        .sheet(item: $editing) { target in
            EditValueSheet(target: target)
                .environmentObject(app)
        }
    }

    func group<C: View>(@ViewBuilder _ content: () -> C) -> some View {
        VStack(spacing: 0) { content() }
            .padding(.horizontal, 14)
            .background(RoundedRectangle(cornerRadius: 12).fill(Color.white))
            .overlay(RoundedRectangle(cornerRadius: 12).strokeBorder(T.border, lineWidth: 1))
    }

    func row(_ title: String, _ source: String?, _ value: String, small: Bool = false, target: @escaping () -> EditTarget) -> some View {
        Button {
            editing = target()
        } label: {
            HStack(spacing: 10) {
                VStack(alignment: .leading, spacing: 1) {
                    Text(title).font(.sans(15)).foregroundColor(T.ink)
                    if let s = source { Text(s).font(.sans(12)).foregroundColor(T.text2) }
                }
                Spacer(minLength: 6)
                Text(value).font(.mono(small ? 13 : 14, .semibold)).foregroundColor(T.ink).lineLimit(1).minimumScaleFactor(0.7)
                Image(systemName: "pencil").font(.system(size: 14)).foregroundColor(T.gray500)
            }
            .frame(minHeight: 48)
            .contentShape(Rectangle())
        }
        .buttonStyle(PressStyle())
    }
}

extension AppState {
    /// Sunk depth as editable text (empty when not chosen yet).
    var sunkText: String {
        get { sunk ?? "" }
        set {
            let v = newValue.trimmingCharacters(in: .whitespaces)
            sunk = v.isEmpty ? nil : v
        }
    }
}

struct EditValueSheet: View {
    @EnvironmentObject var app: AppState
    @Environment(\.dismiss) var dismiss
    let target: EditTarget
    @State var values: [String] = []

    var body: some View {
        VStack(alignment: .leading, spacing: 14) {
            Text(target.title).font(.sans(19, .bold)).foregroundColor(T.ink)
            ForEach(Array(target.fields.enumerated()), id: \.offset) { i, f in
                VStack(alignment: .leading, spacing: 4) {
                    Text(f.0).font(.sans(13, .semibold)).foregroundColor(T.text3)
                    BoxField(placeholder: "Enter", text: Binding(
                        get: { i < values.count ? values[i] : "" },
                        set: { v in if i < values.count { values[i] = v } }
                    ), keyboard: target.numeric ? .numberPad : .default)
                }
            }
            Text("Changing a value un-ticks “I've checked these values”.")
                .font(.sans(13)).foregroundColor(T.text2)
            Spacer(minLength: 0)
            HStack(spacing: 10) {
                SecondaryButton(title: "Cancel") { dismiss() }
                PrimaryButton(title: "Save", fontSize: 15) {
                    var changed = false
                    for (i, f) in target.fields.enumerated() where i < values.count {
                        if app[keyPath: f.1] != values[i] {
                            app[keyPath: f.1] = values[i]
                            changed = true
                        }
                    }
                    if changed { app.valuesChecked = false }
                    dismiss()
                }
            }
        }
        .padding(20)
        .background(Color.white.ignoresSafeArea())
        .presentationDetents([.medium])
        .onAppear {
            values = target.fields.map { app[keyPath: $0.1] }
        }
    }
}

// MARK: - 6 Results

struct ResultsScreen: View {
    @EnvironmentObject var app: AppState

    var body: some View {
        Screen(title: "Bldg 2 · Typical floor", subtitle: "Slab + 77 beams · M30 · Fe550") {
            HStack(alignment: .top, spacing: 8) {
                Stat(label: "Steel", value: "3.79 t", dark: true, valueSize: 22, weight: .semibold)
                Stat(label: "Cement", value: "317", unit: "bags", dark: true, valueSize: 22, weight: .semibold)
                Stat(label: "Concrete", value: "41.7", unit: "m³", dark: true, valueSize: 22, weight: .semibold)
            }
            .padding(16)
            .background(RoundedRectangle(cornerRadius: 14).fill(T.ink))
            .padding(.top, 4)

            Card(padding: EdgeInsets(top: 14, leading: 16, bottom: 14, trailing: 16)) {
                VStack(alignment: .leading, spacing: 10) {
                    Text("Steel by bar size").font(.sans(15, .semibold)).foregroundColor(T.ink)
                    barRow("8 mm", 1.0, "2,128")
                    barRow("16 mm", 0.52, "1,103")
                    barRow("12 mm", 0.18, "392")
                    barRow("10 mm", 0.08, "169")
                    Text("kg, incl. 3% wastage · beams 2,717 · slab 963").font(.sans(12)).foregroundColor(T.text2)
                }
            }

            Card(padding: EdgeInsets(top: 14, leading: 16, bottom: 14, trailing: 16)) {
                VStack(alignment: .leading, spacing: 10) {
                    HStack(alignment: .firstTextBaseline) {
                        Text("Cost per sq ft").font(.sans(15, .semibold)).foregroundColor(T.ink)
                        Spacer()
                        Text("\(app.floorArea) sq ft floor plate").font(.sans(12)).foregroundColor(T.text2)
                    }
                    costRow("Steel", "₹120", "₹2.57 L")
                    costRow("Concrete (ready-mix)", "₹130", "₹2.79 L")
                    Rectangle().fill(T.border).frame(height: 1)
                    HStack {
                        Text("Total").font(.sans(16, .semibold))
                        Spacer()
                        Text("₹250 / sq ft").font(.mono(16, .semibold))
                    }
                    .foregroundColor(T.ink)
                    Button {
                        app.showToast("Demo: prices are fixed sample rates")
                    } label: {
                        Text("Prices: Mumbai region, Aug 2026 · Edit").font(.sans(14)).foregroundColor(T.link)
                            .frame(minHeight: 32)
                    }
                    .buttonStyle(PressStyle())
                }
            }

            HStack(alignment: .top, spacing: 8) {
                Image(systemName: "checkmark.shield").font(.system(size: 15)).foregroundColor(T.text2)
                Text("Every input was confirmed by you on 21 Oct. 2 questions are still open with the consultant.")
                    .font(.sans(13)).foregroundColor(T.text2).fixedSize(horizontal: false, vertical: true)
            }
            .padding(.horizontal, 2)

            Text("More for this estimate").font(.sans(13, .semibold)).foregroundColor(T.text3).padding(.top, 4)
            VStack(spacing: 0) {
                toolRow("scissors", "Cutting plan", "28 stock bars · 1.2% offcut", .cuttingPlan)
                Divider1()
                toolRow("arrow.left.arrow.right", "What changed in R1", "+72 kg steel · +₹4,900", .revisionCompare)
                Divider1()
                toolRow("exclamationmark.triangle", "Drawing check", "2 conflicts · 2 missing · 1 minor", .drawingCheck)
                Divider1()
                toolRow("cart", "Order for next pour", "Quantities and dealer quotes", .purchaseList)
                Divider1()
                toolRow("chart.bar", "Steel used vs planned", "8 mm issued 8% over plan", .siteRecon)
            }
            .padding(.horizontal, 14)
            .background(RoundedRectangle(cornerRadius: 14).fill(Color.white))
            .overlay(RoundedRectangle(cornerRadius: 14).strokeBorder(T.border, lineWidth: 1))
        } bottom: {
            HStack(spacing: 10) {
                SecondaryButton(title: "BBS Excel") { app.exportNotAvailable() }
                PrimaryButton(title: "Share report", icon: "square.and.arrow.up", fontSize: 15) { app.exportNotAvailable() }
            }
        }
    }

    func barRow(_ size: String, _ frac: CGFloat, _ kg: String) -> some View {
        HStack(spacing: 10) {
            Text(size).font(.mono(13)).foregroundColor(T.ink).frame(width: 50, alignment: .leading)
            GeometryReader { g in
                RoundedRectangle(cornerRadius: 3).fill(T.accent)
                    .frame(width: max(4, g.size.width * frac), height: 12)
                    .frame(maxHeight: .infinity)
            }
            .frame(height: 16)
            Text(kg).font(.mono(13)).foregroundColor(T.ink).frame(width: 64, alignment: .trailing)
        }
    }

    func costRow(_ label: String, _ perSqft: String, _ total: String) -> some View {
        HStack {
            Text(label).font(.sans(15)).foregroundColor(T.ink)
            Spacer()
            (Text(perSqft).foregroundColor(T.ink) + Text(" · " + total).foregroundColor(T.text2))
                .font(.mono(14))
        }
    }

    func toolRow(_ icon: String, _ title: String, _ detail: String, _ route: Route) -> some View {
        Button {
            app.push(route)
        } label: {
            HStack(spacing: 12) {
                Image(systemName: icon).font(.system(size: 17)).foregroundColor(T.accent).frame(width: 26)
                VStack(alignment: .leading, spacing: 1) {
                    Text(title).font(.sans(15, .medium)).foregroundColor(T.ink)
                    Text(detail).font(.sans(13)).foregroundColor(T.text2)
                }
                Spacer()
                Image(systemName: "chevron.right").font(.system(size: 13, weight: .semibold)).foregroundColor(T.gray400)
            }
            .frame(minHeight: 56)
            .contentShape(Rectangle())
        }
        .buttonStyle(PressStyle())
    }
}
