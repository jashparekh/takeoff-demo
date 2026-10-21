import SwiftUI

enum Route: Hashable {
    case createProject
    case addBuilding(Int)
    case upload
    case coverage
    case targetUpload
    case drawingHistory(String)
    case reading
    case review
    case floor3D
    case slabDetail
    case issues
    case confirm
    case results
    case cuttingPlan
    case revisionCompare
    case drawingCheck
    case purchaseList
    case siteRecon
}

// MARK: - Buildings

struct BuildingConfig: Identifiable, Equatable {
    let id: Int
    var name: String
    var short: String          // B1, B2, CH
    var wings: String?         // "Wings A, B"
    var tabLabel: String       // label on the coverage switcher
    var use: String            // Residential / Commercial / Mixed / Amenity
    var basements: Int
    var ground: String         // Stilt / Ground / Podium
    var upper: Int
    var terrace: Bool
    var oht: Bool
    var copies: Int
    var fixedSummary: String?  // text from the mock until the user edits the building

    var levelCount: Int {
        basements + 1 + upper + (terrace ? 1 : 0) + (oht ? 1 : 0) + 1
    }

    var summary: String {
        if let s = fixedSummary { return s }
        let g = ground == "Ground" ? "G" : ground
        var s = "\(use) · \(g) + \(upper)"
        if oht { s += " + OHT" }
        if copies > 1 { s += " · ×\(copies) identical" } else { s += " · \(levelCount) levels" }
        return s
    }

    /// Rows for the coverage grid, top to bottom.
    var rows: [CoverageRow] {
        var r: [CoverageRow] = []
        let first = 4.25
        let typ = 2.95
        let terraceLevel = first + Double(upper) * typ
        if oht {
            r.append(CoverageRow(key: "oht", name: "OHT / LMR", level: terraceLevel + 4.10, cells: [0, 1]))
        }
        if terrace {
            r.append(CoverageRow(key: "terrace", name: "Terrace", level: terraceLevel, cells: [0, 1, 2]))
        }
        if upper > 0 {
            for n in stride(from: upper, through: 1, by: -1) {
                r.append(CoverageRow(key: "f\(n)", name: "\(ordinal(n)) floor", level: first + Double(n - 1) * typ, cells: [0, 1, 2]))
            }
        }
        let gname: String
        switch ground {
        case "Ground": gname = "Ground / plinth"
        case "Podium": gname = "Podium"
        default: gname = "Stilt / plinth"
        }
        r.append(CoverageRow(key: "g", name: gname, level: 0.15, cells: [0, 1, 2]))
        if basements > 0 {
            for n in 1...basements {
                r.append(CoverageRow(key: "b\(n)", name: "Basement \(n)", level: -3.0 * Double(n), cells: [0, 1, 2]))
            }
        }
        r.append(CoverageRow(key: "fdn", name: "Foundation", level: nil, cells: [0]))
        return r
    }

    var partCount: Int { rows.reduce(0) { $0 + $1.cells.count } }
}

struct CoverageRow: Identifiable {
    var id: String { key }
    let key: String
    let name: String
    let level: Double?
    let cells: [Int]   // 0 = slab + beams, 1 = columns, 2 = stair

    var levelText: String? {
        guard let l = level else { return nil }
        return l >= 0 ? String(format: "+%.2f", l) : String(format: "%.2f", l)
    }
}

func ordinal(_ n: Int) -> String {
    let suffix: String
    let mod100 = n % 100
    if mod100 >= 11 && mod100 <= 13 { suffix = "th" } else {
        switch n % 10 {
        case 1: suffix = "st"
        case 2: suffix = "nd"
        case 3: suffix = "rd"
        default: suffix = "th"
        }
    }
    return "\(n)\(suffix)"
}

let columnNames = ["Slab + beams", "Columns", "Stair"]
let columnNamesLower = ["slab + beams", "columns", "stair"]

struct CellRef: Hashable {
    let building: Int
    let row: String
    let col: Int
    var key: String { "\(building)|\(row)|\(col)" }
}

struct DrawingFile: Identifiable, Equatable {
    let id = UUID()
    let name: String
    let detail: String
    let tag: String // Structural / Architectural
}

// MARK: - App state

final class AppState: ObservableObject {
    @Published var path: [Route] = []
    @Published var toast: String? = nil
    @Published var showAbout = false

    // 1a
    @Published var projectName = ""
    @Published var city = ""
    @Published var consultant = ""
    @Published var buildings: [BuildingConfig] = []

    // 2
    @Published var uploadBuilding = 1
    @Published var estimateKind = 0
    @Published var drawings: [DrawingFile] = []
    @Published var extraSampleIndex = 0

    // 2a / 2b
    @Published var coverageBuilding = 1
    @Published var covered: [String: String] = [:]
    @Published var selected: [Int: CellRef] = [:]

    // 5 / 5a
    @Published var cement = ""
    @Published var sunk: String? = nil
    @Published var coverBeams = ""
    @Published var coverSlabs = ""
    @Published var grades = "M30 · Fe550"
    @Published var wastage = "3% / 2%"
    @Published var tmtRates = "₹67/66/65/64"
    @Published var rmcRates = "₹6,683 · ₹450"
    @Published var floorArea = "2,145"
    @Published var valuesChecked = false

    // 7
    @Published var cutSize = 2

    // 10
    @Published var pourDate = Calendar.current.date(byAdding: .day, value: 7, to: Date()) ?? Date()

    private var toastToken = 0

    init() { reset() }

    func reset() {
        path = []
        projectName = "Shri Basaveshwar Park"
        city = "Panvel, MH"
        consultant = "Structnik"
        buildings = [
            BuildingConfig(id: 0, name: "Building 1", short: "B1", wings: "Wings A, B", tabLabel: "B1 · Wings A,B",
                           use: "Residential", basements: 0, ground: "Stilt", upper: 7, terrace: true, oht: true,
                           copies: 2, fixedSummary: "Residential · Stilt + 7 + OHT · ×2 identical"),
            BuildingConfig(id: 1, name: "Building 2", short: "B2", wings: nil, tabLabel: "Building 2",
                           use: "Residential", basements: 0, ground: "Stilt", upper: 4, terrace: true, oht: true,
                           copies: 1, fixedSummary: "Residential · Stilt + 4 + OHT · 8 levels"),
            BuildingConfig(id: 2, name: "Clubhouse", short: "CH", wings: nil, tabLabel: "Clubhouse",
                           use: "Amenity", basements: 0, ground: "Ground", upper: 1, terrace: false, oht: true,
                           copies: 1, fixedSummary: "Amenity · G + 1"),
        ]
        uploadBuilding = 1
        estimateKind = 0
        drawings = [
            DrawingFile(name: "ND 41-06 1st & 3rd Floor Plan", detail: "A2 · beam + slab schedule found", tag: "Structural"),
            DrawingFile(name: "ND 41-05 Staircase Details", detail: "A3 · beam schedule found", tag: "Structural"),
            DrawingFile(name: "Building-2 Sheet 1 – Plans", detail: "A0 · room dimensions found", tag: "Architectural"),
        ]
        extraSampleIndex = 0
        coverageBuilding = 1
        covered = [
            CellRef(building: 1, row: "f4", col: 2).key: "41-05",
            CellRef(building: 1, row: "f3", col: 0).key: "41-06",
            CellRef(building: 1, row: "f3", col: 2).key: "41-05",
            CellRef(building: 1, row: "f2", col: 2).key: "41-05",
            CellRef(building: 1, row: "f1", col: 0).key: "41-06",
            CellRef(building: 1, row: "f1", col: 2).key: "41-05",
            CellRef(building: 1, row: "g", col: 2).key: "41-05",
        ]
        selected = [1: CellRef(building: 1, row: "f2", col: 0)]
        cement = "380"
        sunk = nil
        coverBeams = ""
        coverSlabs = ""
        grades = "M30 · Fe550"
        wastage = "3% / 2%"
        tmtRates = "₹67/66/65/64"
        rmcRates = "₹6,683 · ₹450"
        floorArea = "2,145"
        valuesChecked = false
        cutSize = 2
        pourDate = Calendar.current.date(byAdding: .day, value: 7, to: Date()) ?? Date()
        toast = nil
    }

    // Navigation
    func push(_ r: Route) { path.append(r) }
    func pop() { if !path.isEmpty { path.removeLast() } }
    func popToRoot() { path = [] }
    func replaceTop(with r: Route) {
        if !path.isEmpty { path.removeLast() }
        path.append(r)
    }

    func showToast(_ message: String) {
        toastToken += 1
        let token = toastToken
        withAnimation(.easeOut(duration: 0.2)) { toast = message }
        DispatchQueue.main.asyncAfter(deadline: .now() + 2.2) { [weak self] in
            guard let self = self, self.toastToken == token else { return }
            withAnimation(.easeIn(duration: 0.2)) { self.toast = nil }
        }
    }

    func exportNotAvailable() { showToast("Demo: export not available") }

    // Coverage helpers
    func coveredCount(_ b: Int) -> Int {
        let bc = buildings[b]
        var n = 0
        for row in bc.rows {
            for c in row.cells where covered[CellRef(building: b, row: row.key, col: c).key] != nil { n += 1 }
        }
        return n
    }

    func rowName(_ ref: CellRef) -> String {
        buildings[ref.building].rows.first(where: { $0.key == ref.row })?.name ?? ref.row
    }

    func rowLevel(_ ref: CellRef) -> String? {
        buildings[ref.building].rows.first(where: { $0.key == ref.row })?.levelText
    }

    func partName(_ ref: CellRef) -> String {
        if ref.row == "fdn" { return "Footings" }
        return "\(rowName(ref)) · \(columnNamesLower[ref.col])"
    }

    /// Parts that the demo drawing ND 41-07 covers: the selected part, plus the same part two floors up
    /// (the "2nd & 4th floor" pattern) when that one is still missing.
    func targetParts(for ref: CellRef) -> [CellRef] {
        var parts = [ref]
        if ref.row.hasPrefix("f"), let n = Int(ref.row.dropFirst()) {
            let up = CellRef(building: ref.building, row: "f\(n + 2)", col: ref.col)
            if n + 2 <= buildings[ref.building].upper, covered[up.key] == nil {
                parts.append(up)
            }
        }
        return parts
    }

    // 5 helpers
    var emptyValueCount: Int {
        var n = 0
        if cement.trimmingCharacters(in: .whitespaces).isEmpty { n += 1 }
        if sunk == nil { n += 1 }
        if coverBeams.trimmingCharacters(in: .whitespaces).isEmpty || coverSlabs.trimmingCharacters(in: .whitespaces).isEmpty { n += 1 }
        return n
    }
}
