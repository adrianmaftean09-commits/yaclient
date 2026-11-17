package dev.yaclient.cosmetics;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;

/** Cape emblems of the anime bundles, drawn with vector strokes into a 512x512 texture (pure Java2D, no Minecraft deps). */
final class AnimeArt {
    private AnimeArt() {}
    static final int S = 512;

    static BufferedImage paint(int theme) {
        BufferedImage img = new BufferedImage(S, S, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        switch (theme) {
            case 0: sakura(g); break;
            case 1: shinobi(g); break;
            case 2: neon(g); break;
            default: kitsune(g); break;
        }
        g.dispose();
        return img;
    }

    private static Color a(int rgb, int alpha) { return new Color((rgb >> 16) & 255, (rgb >> 8) & 255, rgb & 255, alpha); }

    private static void glow(Graphics2D g, Shape s, int rgb, float w) {
        for (int i = 4; i >= 1; i--) {
            g.setStroke(new BasicStroke(w + i * 9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(a(rgb, 22));
            g.draw(s);
        }
        g.setStroke(new BasicStroke(w, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(a(rgb, 255));
        g.draw(s);
        g.setStroke(new BasicStroke(Math.max(2f, w * 0.35f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(255, 255, 255, 230));
        g.draw(s);
    }

    private static void flower(Graphics2D g, double cx, double cy, double r, double rot) {
        for (int i = 0; i < 5; i++) {
            AffineTransform t = g.getTransform();
            g.translate(cx, cy);
            g.rotate(rot + i * Math.PI * 2 / 5);
            Path2D.Double petal = new Path2D.Double();
            petal.moveTo(0, 0);
            petal.curveTo(-r * 0.75, -r * 0.35, -r * 0.55, -r * 1.05, -r * 0.14, -r * 1.00);   // notched petal tip
            petal.lineTo(0, -r * 0.86);
            petal.lineTo(r * 0.14, -r * 1.00);
            petal.curveTo(r * 0.55, -r * 1.05, r * 0.75, -r * 0.35, 0, 0);
            g.setPaint(new GradientPaint(0, 0, a(0xFFFFFF, 255), 0, (float) -r, a(0xF7A8C4, 255)));
            g.fill(petal);
            g.setColor(a(0xE0709A, 255));
            g.setStroke(new BasicStroke(3f));
            g.draw(petal);
            g.setTransform(t);
        }
        g.setColor(a(0xF2C94C, 255));
        g.fill(new Ellipse2D.Double(cx - r * 0.12, cy - r * 0.12, r * 0.24, r * 0.24));
        for (int i = 0; i < 6; i++) {
            double an = rot + i * Math.PI / 3;
            g.setColor(a(0xE0709A, 255));
            g.setStroke(new BasicStroke(3f));
            g.draw(new Line2D.Double(cx, cy, cx + Math.cos(an) * r * 0.32, cy + Math.sin(an) * r * 0.32));
            g.fill(new Ellipse2D.Double(cx + Math.cos(an) * r * 0.32 - 4, cy + Math.sin(an) * r * 0.32 - 4, 8, 8));
        }
    }

    private static void sakura(Graphics2D g) {
        // a blossoming branch
        Path2D.Double br = new Path2D.Double();
        br.moveTo(40, 470); br.curveTo(140, 400, 190, 330, 260, 280); br.curveTo(330, 230, 380, 200, 470, 70);
        g.setStroke(new BasicStroke(15f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(a(0x7A4A52, 255)); g.draw(br);
        Path2D.Double tw = new Path2D.Double();
        tw.moveTo(190, 335); tw.curveTo(190, 290, 160, 250, 120, 220);
        tw.moveTo(330, 232); tw.curveTo(350, 280, 390, 310, 440, 320);
        g.setStroke(new BasicStroke(8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)); g.draw(tw);
        flower(g, 120, 214, 52, 0.2); flower(g, 262, 278, 64, 0.7); flower(g, 445, 322, 50, 1.2);
        flower(g, 400, 160, 56, 0.4); flower(g, 90, 410, 44, 1.0); flower(g, 340, 100, 36, 0.1);
        // falling petals
        double[][] pt = {{60, 120, 0.8}, {200, 60, 2.2}, {440, 240, 1.4}, {330, 420, 0.3}, {170, 450, 2.6}, {470, 400, 1.9}, {40, 290, 0.5}, {230, 180, 1.2}};
        for (double[] p : pt) {
            AffineTransform t = g.getTransform();
            g.translate(p[0], p[1]); g.rotate(p[2]);
            g.setPaint(new GradientPaint(-12, 0, a(0xFFFFFF, 240), 12, 0, a(0xF7A8C4, 240)));
            g.fill(new Ellipse2D.Double(-14, -8, 28, 16));
            g.setTransform(t);
        }
    }

    private static Path2D.Double star4(double cx, double cy, double r, double in) {
        Path2D.Double p = new Path2D.Double();
        for (int i = 0; i < 8; i++) {
            double rr = (i % 2 == 0) ? r : in, an = -Math.PI / 2 + i * Math.PI / 4;
            double x = cx + Math.cos(an) * rr, y = cy + Math.sin(an) * rr;
            if (i == 0) p.moveTo(x, y); else p.lineTo(x, y);
        }
        p.closePath();
        return p;
    }

    private static void shinobi(Graphics2D g) {
        // ring, crescent moon and a four-point shuriken star
        g.setStroke(new BasicStroke(10f));
        g.setColor(a(0xB8BCD8, 255)); g.draw(new Ellipse2D.Double(36, 36, 440, 440));
        g.setStroke(new BasicStroke(3f));
        g.setColor(a(0x8C6BFF, 255)); g.draw(new Ellipse2D.Double(58, 58, 396, 396));
        Area moon = new Area(new Ellipse2D.Double(130, 100, 250, 250));
        moon.subtract(new Area(new Ellipse2D.Double(190, 80, 230, 230)));
        for (int i = 3; i >= 1; i--) { g.setColor(a(0x8C6BFF, 28)); g.setStroke(new BasicStroke(i * 10f)); g.draw(moon); }
        g.setPaint(new GradientPaint(150, 120, a(0xFFFFFF, 255), 300, 340, a(0xB8BCD8, 255)));
        g.fill(moon);
        Path2D.Double sh = star4(256, 400, 62, 18);
        g.setColor(a(0x8C6BFF, 60)); g.setStroke(new BasicStroke(18f)); g.draw(sh);
        g.setColor(a(0xDDE0F5, 255)); g.fill(sh);
        g.setColor(a(0x1B1B33, 255)); g.fill(new Ellipse2D.Double(246, 390, 20, 20));
        g.setColor(a(0xDDE0F5, 255)); g.fill(star4(380, 150, 16, 5)); g.fill(star4(120, 360, 12, 4)); g.fill(star4(400, 330, 10, 3));
    }

    private static void neon(Graphics2D g) {
        Path2D.Double heart = new Path2D.Double();
        heart.moveTo(256, 440);
        heart.curveTo(90, 330, 40, 220, 100, 140);
        heart.curveTo(150, 80, 230, 100, 256, 170);
        heart.curveTo(282, 100, 362, 80, 412, 140);
        heart.curveTo(472, 220, 422, 330, 256, 440);
        heart.closePath();
        glow(g, heart, 0x00F0FF, 16f);
        Path2D.Double bolt = new Path2D.Double();
        bolt.moveTo(286, 130); bolt.lineTo(206, 270); bolt.lineTo(262, 270); bolt.lineTo(226, 390); bolt.lineTo(322, 232); bolt.lineTo(266, 232); bolt.closePath();
        g.setColor(a(0xFF2FBF, 70)); g.setStroke(new BasicStroke(22f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)); g.draw(bolt);
        g.setColor(a(0xFF2FBF, 255)); g.fill(bolt);
        g.setColor(new Color(255, 255, 255, 230)); g.setStroke(new BasicStroke(3f)); g.draw(bolt);
        glow(g, star4(70, 70, 30, 8), 0xFF2FBF, 5f);
        glow(g, star4(450, 90, 24, 6), 0x00F0FF, 4f);
        glow(g, star4(440, 440, 20, 5), 0xFF2FBF, 4f);
        glow(g, star4(60, 440, 16, 4), 0x00F0FF, 3f);
    }

    private static void kitsune(Graphics2D g) {
        Path2D.Double face = new Path2D.Double();
        face.moveTo(256, 470);
        face.curveTo(190, 450, 80, 380, 52, 270);
        face.curveTo(40, 220, 60, 170, 70, 100);          // left cheek up to the ear
        face.lineTo(70, 30);
        face.lineTo(170, 96);
        face.curveTo(230, 80, 282, 80, 342, 96);
        face.lineTo(442, 30);
        face.lineTo(442, 100);
        face.curveTo(452, 170, 472, 220, 460, 270);
        face.curveTo(432, 380, 322, 450, 256, 470);
        face.closePath();
        g.setPaint(new GradientPaint(0, 60, a(0xFFFFFF, 255), 0, 470, a(0xF3E9E2, 255)));
        g.fill(face);
        g.setColor(a(0xD62A1C, 255)); g.setStroke(new BasicStroke(12f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)); g.draw(face);
        // inner ears
        for (int s = -1; s <= 1; s += 2) {
            Path2D.Double ear = new Path2D.Double();
            ear.moveTo(256 + s * 150, 96); ear.lineTo(256 + s * 178, 58); ear.lineTo(256 + s * 200, 90); ear.closePath();
            g.setColor(a(0xD62A1C, 255)); g.fill(ear);
            // eye slits and the red swooping eye markings
            Path2D.Double eye = new Path2D.Double();
            eye.moveTo(256 + s * 52, 232); eye.quadTo(256 + s * 100, 214, 256 + s * 138, 246);
            g.setColor(a(0x2A1215, 255)); g.setStroke(new BasicStroke(11f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)); g.draw(eye);
            Path2D.Double mark = new Path2D.Double();
            mark.moveTo(256 + s * 60, 214); mark.quadTo(256 + s * 120, 160, 256 + s * 196, 190);
            mark.moveTo(256 + s * 150, 280); mark.quadTo(256 + s * 190, 330, 256 + s * 196, 380);
            g.setColor(a(0xD62A1C, 255)); g.setStroke(new BasicStroke(13f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)); g.draw(mark);
            // whisker marks
            for (int k = 0; k < 3; k++) {
                g.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.draw(new Line2D.Double(256 + s * (130 + k * 18), 330 + k * 22, 256 + s * (186 + k * 10), 346 + k * 26));
            }
        }
        // forehead mark and nose
        Path2D.Double fm = new Path2D.Double();
        fm.moveTo(256, 100); fm.lineTo(232, 176); fm.lineTo(280, 176); fm.closePath();
        g.setColor(a(0xD62A1C, 255)); g.fill(fm);
        g.setColor(a(0x2A1215, 255)); g.fill(new Ellipse2D.Double(236, 330, 40, 28));
        g.setColor(a(0xF4B93B, 255)); g.fill(new Ellipse2D.Double(246, 392, 20, 20));
    }
}
