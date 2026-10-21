import SwiftUI

extension Color {
    init(hex: UInt32) {
        self.init(.sRGB,
                  red: Double((hex >> 16) & 0xFF) / 255.0,
                  green: Double((hex >> 8) & 0xFF) / 255.0,
                  blue: Double(hex & 0xFF) / 255.0,
                  opacity: 1.0)
    }
}

enum T {
    static let accent = Color(hex: 0xC2410C)
    static let accentDark = Color(hex: 0x9A3412)
    static let accentSoft = Color(hex: 0xFFEDD5)
    static let accentTint = Color(hex: 0xFFF7ED)
    static let bg = Color(hex: 0xF3F4F6)
    static let card = Color.white
    static let border = Color(hex: 0xE5E7EB)
    static let divider = Color(hex: 0xF3F4F6)
    static let ink = Color(hex: 0x111827)
    static let text2 = Color(hex: 0x4B5563)
    static let text3 = Color(hex: 0x374151)
    static let gray500 = Color(hex: 0x6B7280)
    static let gray400 = Color(hex: 0x9CA3AF)
    static let gray300 = Color(hex: 0xD1D5DB)
    static let gray200 = Color(hex: 0xE5E7EB)
    static let gray50 = Color(hex: 0xF9FAFB)
    static let coveredBg = Color(hex: 0xBBF7D0)
    static let coveredFg = Color(hex: 0x14532D)
    static let greenBg = Color(hex: 0xDCFCE7)
    static let greenFg = Color(hex: 0x166534)
    static let amberBg = Color(hex: 0xFEF3C7)
    static let amberFg = Color(hex: 0x92400E)
    static let amberBorder = Color(hex: 0xD97706)
    static let blueBg = Color(hex: 0xDBEAFE)
    static let blueFg = Color(hex: 0x1E40AF)
    static let link = Color(hex: 0x1D4ED8)
}

extension Font {
    /// Monospaced system font, used for every number.
    static func mono(_ size: CGFloat, _ weight: Font.Weight = .medium) -> Font {
        .system(size: size, weight: weight, design: .monospaced)
    }
    static func sans(_ size: CGFloat, _ weight: Font.Weight = .regular) -> Font {
        .system(size: size, weight: weight)
    }
}

// MARK: - Building blocks

struct Card<Content: View>: View {
    var padding: EdgeInsets = EdgeInsets(top: 12, leading: 14, bottom: 12, trailing: 14)
    var radius: CGFloat = 14
    var borderColor: Color = T.border
    var borderWidth: CGFloat = 1
    @ViewBuilder var content: () -> Content

    var body: some View {
        content()
            .padding(padding)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(RoundedRectangle(cornerRadius: radius).fill(Color.white))
            .overlay(RoundedRectangle(cornerRadius: radius).strokeBorder(borderColor, lineWidth: borderWidth))
    }
}

struct Chip: View {
    let text: String
    var fg: Color = T.text3
    var bg: Color = T.gray200
    var size: CGFloat = 12
    var hPad: CGFloat = 8
    var vPad: CGFloat = 4
    var radius: CGFloat = 6

    var body: some View {
        Text(text)
            .font(.sans(size, .semibold))
            .foregroundColor(fg)
            .lineLimit(1)
            .fixedSize()
            .padding(.horizontal, hPad)
            .padding(.vertical, vPad)
            .background(RoundedRectangle(cornerRadius: radius).fill(bg))
    }
}

struct Divider1: View {
    var body: some View { Rectangle().fill(T.divider).frame(height: 1) }
}

struct PrimaryButton: View {
    let title: String
    var enabled: Bool = true
    var icon: String? = nil
    var height: CGFloat = 52
    var fontSize: CGFloat = 16
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            HStack(spacing: 8) {
                if let icon = icon {
                    Image(systemName: icon).font(.system(size: 17, weight: .semibold))
                }
                Text(title).font(.sans(fontSize, .semibold)).lineLimit(1).minimumScaleFactor(0.8)
            }
            .foregroundColor(enabled ? .white : T.text2)
            .frame(maxWidth: .infinity)
            .frame(height: height)
            .background(RoundedRectangle(cornerRadius: 12).fill(enabled ? T.accent : T.gray200))
            .contentShape(Rectangle())
        }
        .buttonStyle(PressStyle())
        .disabled(!enabled)
    }
}

struct SecondaryButton: View {
    let title: String
    var height: CGFloat = 52
    var fontSize: CGFloat = 15
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Text(title).font(.sans(fontSize, .semibold)).foregroundColor(T.ink)
                .lineLimit(1).minimumScaleFactor(0.8)
                .frame(maxWidth: .infinity)
                .frame(height: height)
                .background(RoundedRectangle(cornerRadius: 12).fill(Color.white))
                .overlay(RoundedRectangle(cornerRadius: 12).strokeBorder(T.gray300, lineWidth: 1))
                .contentShape(Rectangle())
        }
        .buttonStyle(PressStyle())
    }
}

struct PressStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label.opacity(configuration.isPressed ? 0.75 : 1.0)
    }
}

/// Pill / segmented choice button. Selected = dark fill.
struct PillButton: View {
    let title: String
    let selected: Bool
    var height: CGFloat = 40
    var radius: CGFloat = 20
    var fontSize: CGFloat = 14
    var expand: Bool = false
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Text(title)
                .font(.sans(fontSize, selected ? .semibold : .medium))
                .foregroundColor(selected ? .white : T.text3)
                .lineLimit(1)
                .padding(.horizontal, 14)
                .frame(maxWidth: expand ? .infinity : nil)
                .frame(height: height)
                .background(RoundedRectangle(cornerRadius: radius).fill(selected ? T.ink : Color.white))
                .overlay(RoundedRectangle(cornerRadius: radius).strokeBorder(selected ? T.ink : T.gray300, lineWidth: 1))
                .contentShape(Rectangle())
        }
        .buttonStyle(PressStyle())
    }
}

/// Grey segmented control like the "Plan / 3D / Drawing" switch.
struct Segmented: View {
    let options: [String]
    @Binding var selection: Int

    var body: some View {
        HStack(spacing: 0) {
            ForEach(Array(options.enumerated()), id: \.offset) { idx, title in
                Button {
                    selection = idx
                } label: {
                    Text(title)
                        .font(.sans(14, selection == idx ? .semibold : .medium))
                        .foregroundColor(selection == idx ? T.ink : T.text3)
                        .frame(maxWidth: .infinity)
                        .frame(height: 38)
                        .background(RoundedRectangle(cornerRadius: 8).fill(selection == idx ? Color.white : Color.clear))
                        .contentShape(Rectangle())
                }
                .buttonStyle(PressStyle())
            }
        }
        .padding(3)
        .background(RoundedRectangle(cornerRadius: 10).fill(T.gray200))
    }
}

struct CheckBox: View {
    let checked: Bool
    var size: CGFloat = 22
    var body: some View {
        ZStack {
            RoundedRectangle(cornerRadius: 4)
                .fill(checked ? T.accent : Color.white)
            RoundedRectangle(cornerRadius: 4)
                .strokeBorder(checked ? T.accent : T.gray400, lineWidth: 1.5)
            if checked {
                Image(systemName: "checkmark").font(.system(size: size * 0.6, weight: .bold)).foregroundColor(.white)
            }
        }
        .frame(width: size, height: size)
    }
}

struct CheckRow: View {
    let title: String
    @Binding var checked: Bool
    var fontSize: CGFloat = 15
    var body: some View {
        Button {
            checked.toggle()
        } label: {
            HStack(spacing: 10) {
                CheckBox(checked: checked)
                Text(title).font(.sans(fontSize)).foregroundColor(T.ink).multilineTextAlignment(.leading)
                Spacer(minLength: 0)
            }
            .frame(minHeight: 44)
            .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
    }
}

/// Section label, e.g. "Buildings (3)" + right hint.
struct SectionLabel: View {
    let title: String
    var hint: String? = nil
    var body: some View {
        HStack(alignment: .firstTextBaseline) {
            Text(title).font(.sans(13, .semibold)).foregroundColor(T.text3)
            Spacer()
            if let hint = hint {
                Text(hint).font(.sans(12)).foregroundColor(T.text2)
            }
        }
    }
}

struct Stat: View {
    let label: String
    let value: String
    var unit: String? = nil
    var dark: Bool = false
    var valueSize: CGFloat = 16
    var weight: Font.Weight = .medium
    var valueColor: Color? = nil

    var body: some View {
        VStack(alignment: .leading, spacing: 2) {
            Text(label).font(.sans(12)).foregroundColor(dark ? T.gray300 : T.text2)
            Text(value).font(.mono(valueSize, weight))
                .foregroundColor(valueColor ?? (dark ? .white : T.ink))
                .lineLimit(1).minimumScaleFactor(0.7)
            if let unit = unit {
                Text(unit).font(.sans(11)).foregroundColor(dark ? T.gray300 : T.text2)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }
}

// MARK: - Screen scaffold

struct ScreenHeader: View {
    let title: String
    var subtitle: String? = nil
    var showBack: Bool = true
    var trailing: AnyView? = nil
    var titleSize: CGFloat = 17
    @EnvironmentObject var app: AppState

    var body: some View {
        HStack(spacing: 4) {
            if showBack {
                Button {
                    app.pop()
                } label: {
                    Image(systemName: "chevron.left")
                        .font(.system(size: 20, weight: .semibold))
                        .foregroundColor(T.ink)
                        .frame(width: 44, height: 44)
                        .contentShape(Rectangle())
                }
                .accessibilityLabel("Back")
            }
            VStack(alignment: .leading, spacing: 1) {
                Text(title).font(.sans(subtitle == nil ? 18 : titleSize, .semibold)).foregroundColor(T.ink)
                    .lineLimit(1).minimumScaleFactor(0.8)
                if let subtitle = subtitle {
                    Text(subtitle).font(.sans(12)).foregroundColor(T.text2).lineLimit(2)
                }
            }
            Spacer(minLength: 4)
            if let trailing = trailing { trailing }
        }
        .padding(.horizontal, 8)
        .padding(.top, 6)
        .padding(.bottom, 4)
    }
}

/// Standard screen: custom header, scrolling content, pinned bottom action area.
struct Screen<Content: View, Bottom: View>: View {
    let title: String
    var subtitle: String? = nil
    var trailing: AnyView? = nil
    var spacing: CGFloat = 12
    var bottomBackground: Color = T.bg
    @ViewBuilder var content: () -> Content
    @ViewBuilder var bottom: () -> Bottom

    var body: some View {
        VStack(spacing: 0) {
            ScreenHeader(title: title, subtitle: subtitle, trailing: trailing)
            ScrollView {
                VStack(alignment: .leading, spacing: spacing) {
                    content()
                }
                .padding(.horizontal, 20)
                .padding(.top, 4)
                .padding(.bottom, 16)
                .frame(maxWidth: .infinity, alignment: .leading)
            }
            .scrollDismissesKeyboard(.interactively)
            VStack(spacing: 8) {
                bottom()
            }
            .padding(.horizontal, 20)
            .padding(.top, 10)
            .padding(.bottom, 12)
            .background(bottomBackground)
        }
        .background(T.bg.ignoresSafeArea())
        .toolbar(.hidden, for: .navigationBar)
    }
}

extension View {
    func hideKeyboard() {
        UIApplication.shared.sendAction(#selector(UIResponder.resignFirstResponder), to: nil, from: nil, for: nil)
    }
}

/// Number-style text field with border, like the mock inputs.
struct BoxField: View {
    let placeholder: String
    @Binding var text: String
    var mono: Bool = true
    var width: CGFloat? = nil
    var keyboard: UIKeyboardType = .numberPad
    var highlight: Bool = false

    var body: some View {
        TextField(placeholder, text: $text)
            .keyboardType(keyboard)
            .font(mono ? .mono(16, .medium) : .sans(16))
            .foregroundColor(T.ink)
            .padding(.horizontal, 12)
            .frame(width: width)
            .frame(height: 44)
            .background(RoundedRectangle(cornerRadius: 8).fill(Color.white))
            .overlay(RoundedRectangle(cornerRadius: 8).strokeBorder(highlight ? T.accent : T.gray300, lineWidth: 1))
    }
}

/// A row with a title and an optional detail, used in list cards.
struct ValueRow: View {
    let title: String
    var detail: String? = nil
    let value: String
    var bold: Bool = false

    var body: some View {
        HStack(alignment: .center) {
            VStack(alignment: .leading, spacing: 2) {
                Text(title).font(.sans(15, bold ? .semibold : .medium)).foregroundColor(T.ink)
                if let detail = detail {
                    Text(detail).font(.sans(13)).foregroundColor(T.text2)
                }
            }
            Spacer(minLength: 8)
            Text(value).font(.mono(15, bold ? .semibold : .regular)).foregroundColor(T.ink)
        }
        .padding(.vertical, 10)
    }
}
