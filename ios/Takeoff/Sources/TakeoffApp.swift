import SwiftUI
import UIKit

@main
struct TakeoffApp: App {
    @StateObject var app = AppState()

    var body: some Scene {
        WindowGroup {
            RootView()
                .environmentObject(app)
                .preferredColorScheme(.light)
                .tint(T.accent)
        }
    }
}

struct RootView: View {
    @EnvironmentObject var app: AppState

    var body: some View {
        NavigationStack(path: $app.path) {
            ProjectsScreen()
                .navigationDestination(for: Route.self) { route in
                    destination(route)
                }
        }
        .overlay(alignment: .bottom) {
            if let msg = app.toast {
                Text(msg)
                    .font(.sans(15, .medium))
                    .foregroundColor(.white)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, 18)
                    .padding(.vertical, 12)
                    .background(RoundedRectangle(cornerRadius: 12).fill(T.ink.opacity(0.94)))
                    .padding(.horizontal, 24)
                    .padding(.bottom, 96)
                    .transition(.move(edge: .bottom).combined(with: .opacity))
                    .onTapGesture { app.toast = nil }
                    .accessibilityAddTraits(.isStaticText)
            }
        }
        .sheet(isPresented: $app.showAbout) {
            AboutSheet()
        }
    }

    @ViewBuilder
    func destination(_ route: Route) -> some View {
        switch route {
        case .createProject: CreateProjectScreen()
        case .addBuilding(let idx): AddBuildingScreen(index: idx)
        case .upload: UploadScreen()
        case .coverage: CoverageScreen()
        case .targetUpload: TargetUploadScreen()
        case .drawingHistory(let code): DrawingHistoryScreen(code: code)
        case .reading: ReadingScreen()
        case .review: ReviewScreen()
        case .floor3D: Floor3DScreen()
        case .slabDetail: SlabDetailScreen()
        case .issues: IssuesScreen()
        case .confirm: ConfirmScreen()
        case .results: ResultsScreen()
        case .cuttingPlan: CuttingPlanScreen()
        case .revisionCompare: RevisionCompareScreen()
        case .drawingCheck: DrawingCheckScreen()
        case .purchaseList: PurchaseListScreen()
        case .siteRecon: SiteReconScreen()
        }
    }
}

struct AboutSheet: View {
    @Environment(\.dismiss) var dismiss

    var body: some View {
        VStack(alignment: .leading, spacing: 14) {
            Capsule().fill(T.gray300).frame(width: 40, height: 4).frame(maxWidth: .infinity)
            Text("About this demo").font(.sans(20, .bold)).foregroundColor(T.ink)
            Text("Sample data only, nothing is uploaded.")
                .font(.sans(16, .semibold)).foregroundColor(T.ink)
            Text("Takeoff reads structural drawings and works out steel and cement. This demo shows the screens with example projects. Nothing leaves your phone, there is no account and no internet is used. Use “Reset demo” to start again.")
                .font(.sans(15)).foregroundColor(T.text2).fixedSize(horizontal: false, vertical: true)
            Text("Version \(Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? "0.1.0") (\(Bundle.main.infoDictionary?["CFBundleVersion"] as? String ?? "1"))")
                .font(.mono(13)).foregroundColor(T.text2)
            Spacer(minLength: 0)
            PrimaryButton(title: "OK") { dismiss() }
        }
        .padding(20)
        .background(Color.white.ignoresSafeArea())
        .presentationDetents([.medium])
    }
}

// Keep the iOS swipe-back gesture working while the navigation bar is hidden.
extension UINavigationController: UIGestureRecognizerDelegate {
    override open func viewDidLoad() {
        super.viewDidLoad()
        interactivePopGestureRecognizer?.delegate = self
    }

    public func gestureRecognizerShouldBegin(_ gestureRecognizer: UIGestureRecognizer) -> Bool {
        viewControllers.count > 1
    }
}
