#!/usr/bin/env python3
"""Generate the 1024x1024 App Store icon (no alpha) with only the standard library.

Orange #C2410C background with a simple white building glyph (roof + frame),
matching the Takeoff logo used on the Projects screen.
Writes ios/Takeoff/Assets.xcassets/AppIcon.appiconset/AppIcon.png
"""
import os
import struct
import zlib

SIZE = 1024
BG = (0xC2, 0x41, 0x0C)
FG = (0xFF, 0xFF, 0xFF)


def dist_to_segment(px, py, ax, ay, bx, by):
    dx, dy = bx - ax, by - ay
    l2 = dx * dx + dy * dy
    t = 0.0 if l2 == 0 else max(0.0, min(1.0, ((px - ax) * dx + (py - ay) * dy) / l2))
    cx, cy = ax + t * dx, ay + t * dy
    return ((px - cx) ** 2 + (py - cy) ** 2) ** 0.5


def main():
    # Glyph from the 24x24 logo: M4 20V8l8-4 8 4v12  M4 12h16  M9 20v-8  M15 20v-8
    s = SIZE / 24.0 * 0.62
    off = SIZE * 0.5 - 12 * s
    def p(x, y):
        return (off + x * s, off + y * s)
    segs = [
        (p(4, 20), p(4, 8)), (p(4, 8), p(12, 4)), (p(12, 4), p(20, 8)), (p(20, 8), p(20, 20)),
        (p(4, 12), p(20, 12)), (p(9, 20), p(9, 12)), (p(15, 20), p(15, 12)),
    ]
    half = 2.0 * s / 2.0  # stroke width 2 in glyph units
    rows = []
    for y in range(SIZE):
        row = bytearray([0])  # filter: none
        py = y + 0.5
        for x in range(SIZE):
            px = x + 0.5
            d = min(dist_to_segment(px, py, a[0], a[1], b[0], b[1]) for a, b in segs)
            # anti-aliased edge (1px)
            cov = max(0.0, min(1.0, half - d + 0.5))
            r = int(BG[0] + (FG[0] - BG[0]) * cov)
            g = int(BG[1] + (FG[1] - BG[1]) * cov)
            b_ = int(BG[2] + (FG[2] - BG[2]) * cov)
            row += bytes((r, g, b_))
        rows.append(bytes(row))
    raw = b"".join(rows)

    def chunk(tag, data):
        c = struct.pack(">I", len(data)) + tag + data
        return c + struct.pack(">I", zlib.crc32(tag + data) & 0xFFFFFFFF)

    png = b"\x89PNG\r\n\x1a\n"
    png += chunk(b"IHDR", struct.pack(">IIBBBBB", SIZE, SIZE, 8, 2, 0, 0, 0))  # RGB, no alpha
    png += chunk(b"IDAT", zlib.compress(raw, 9))
    png += chunk(b"IEND", b"")

    here = os.path.dirname(os.path.abspath(__file__))
    out = os.path.join(here, "..", "Takeoff", "Assets.xcassets", "AppIcon.appiconset", "AppIcon.png")
    out = os.path.normpath(out)
    with open(out, "wb") as f:
        f.write(png)
    print("wrote", out, len(png), "bytes")


if __name__ == "__main__":
    main()
