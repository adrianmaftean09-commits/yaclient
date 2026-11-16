package dev.yaclient;

import java.awt.Graphics2D;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

/** Vector icons drawn on a 24x24 grid (stroke style). Rendered to textures by Gfx.icon. */
final class Icons {
    private Icons() {}

    private static Path2D poly(boolean close, float... p) {
        Path2D.Float path = new Path2D.Float();
        path.moveTo(p[0], p[1]);
        for (int i = 2; i < p.length; i += 2) path.lineTo(p[i], p[i + 1]);
        if (close) path.closePath();
        return path;
    }

    static void draw(Graphics2D g, String name) {
        switch (name) {
            case "grid":
                g.draw(new RoundRectangle2D.Float(3.5f, 3.5f, 7f, 7f, 2.5f, 2.5f));
                g.draw(new RoundRectangle2D.Float(13.5f, 3.5f, 7f, 7f, 2.5f, 2.5f));
                g.draw(new RoundRectangle2D.Float(3.5f, 13.5f, 7f, 7f, 2.5f, 2.5f));
                g.draw(new RoundRectangle2D.Float(13.5f, 13.5f, 7f, 7f, 2.5f, 2.5f));
                break;
            case "info":
                g.draw(new Ellipse2D.Float(3f, 3f, 18f, 18f));
                g.draw(new Line2D.Float(12f, 11f, 12f, 17f));
                g.fill(new Ellipse2D.Float(11f, 6.6f, 2f, 2f));
                break;
            case "sword":
                g.draw(new Line2D.Float(5f, 5f, 19f, 19f));
                g.draw(new Line2D.Float(19f, 5f, 5f, 19f));
                g.draw(new Line2D.Float(4f, 9f, 9f, 4f));
                g.draw(new Line2D.Float(20f, 9f, 15f, 4f));
                break;
            case "wrench":
                g.draw(new Arc2D.Float(3f, 3f, 11f, 11f, 160f, 310f, Arc2D.OPEN));
                g.draw(new Line2D.Float(11.5f, 11.5f, 20f, 20f));
                g.draw(new Line2D.Float(5f, 5.5f, 7.5f, 8f));
                break;
            case "shirt":
                g.draw(poly(true, 8f, 3f, 3.5f, 5.5f, 1.8f, 10f, 5.5f, 11.5f, 7f, 10f, 7f, 21f, 17f, 21f, 17f, 10f, 18.5f, 11.5f, 22.2f, 10f, 20.5f, 5.5f, 16f, 3f));
                g.draw(poly(false, 8f, 3f, 12f, 6.5f, 16f, 3f));
                break;
            case "star":
                g.draw(poly(true, 12f, 3f, 14.6f, 9f, 21f, 9.4f, 16f, 13.6f, 17.7f, 20f, 12f, 16.5f, 6.3f, 20f, 8f, 13.6f, 3f, 9.4f, 9.4f, 9f));
                break;
            case "move":
                g.draw(new Line2D.Float(12f, 3f, 12f, 21f));
                g.draw(new Line2D.Float(3f, 12f, 21f, 12f));
                g.draw(poly(false, 9f, 6f, 12f, 3f, 15f, 6f));
                g.draw(poly(false, 9f, 18f, 12f, 21f, 15f, 18f));
                g.draw(poly(false, 6f, 9f, 3f, 12f, 6f, 15f));
                g.draw(poly(false, 18f, 9f, 21f, 12f, 18f, 15f));
                break;
            case "gear":
                g.draw(new Ellipse2D.Float(9f, 9f, 6f, 6f));
                g.draw(new Ellipse2D.Float(5f, 5f, 14f, 14f));
                for (int k = 0; k < 8; k++) {
                    double a = Math.PI * 2 * k / 8.0;
                    g.draw(new Line2D.Double(12 + Math.cos(a) * 7, 12 + Math.sin(a) * 7,
                            12 + Math.cos(a) * 10, 12 + Math.sin(a) * 10));
                }
                break;
            case "search":
                g.draw(new Ellipse2D.Float(4f, 4f, 12f, 12f));
                g.draw(new Line2D.Float(14f, 14f, 20f, 20f));
                break;
            case "close":
                g.draw(new Line2D.Float(6f, 6f, 18f, 18f));
                g.draw(new Line2D.Float(18f, 6f, 6f, 18f));
                break;
            case "check":
                g.draw(poly(false, 5f, 12.5f, 10f, 17.5f, 19f, 7f));
                break;
            case "globe":
                g.draw(new Ellipse2D.Float(3f, 3f, 18f, 18f));
                g.draw(new Ellipse2D.Float(8f, 3f, 8f, 18f));
                g.draw(new Line2D.Float(3f, 12f, 21f, 12f));
                break;
            case "palette":
                g.draw(new Ellipse2D.Float(3f, 3f, 18f, 18f));
                g.fill(new Ellipse2D.Float(7f, 9f, 2.4f, 2.4f));
                g.fill(new Ellipse2D.Float(11f, 6.5f, 2.4f, 2.4f));
                g.fill(new Ellipse2D.Float(15f, 9f, 2.4f, 2.4f));
                break;
            case "lock":
                g.draw(new RoundRectangle2D.Float(5f, 10.5f, 14f, 10f, 3f, 3f));
                g.draw(new Arc2D.Float(7.5f, 3.5f, 9f, 11f, 0f, 180f, Arc2D.OPEN));
                g.fill(new Ellipse2D.Float(11f, 14f, 2f, 2f));
                break;
            case "cart":
                g.draw(poly(false, 3f, 4f, 6f, 4f, 8.5f, 15f, 18f, 15f, 20f, 7f, 7f, 7f));
                g.fill(new Ellipse2D.Float(8.5f, 18f, 2.6f, 2.6f));
                g.fill(new Ellipse2D.Float(15f, 18f, 2.6f, 2.6f));
                break;
            case "reset":
                g.draw(new Ellipse2D.Float(4f, 4f, 16f, 16f));
                g.draw(poly(false, 4f, 4f, 4f, 9f, 9f, 9f));
                break;
            case "cape":
                g.draw(poly(true, 8.5f, 3.5f, 15.5f, 3.5f, 19.5f, 20.5f, 12f, 18f, 4.5f, 20.5f));
                g.draw(new Line2D.Float(8.5f, 3.5f, 12f, 7f));
                g.draw(new Line2D.Float(15.5f, 3.5f, 12f, 7f));
                break;
            case "wings": {
                Path2D.Float w = new Path2D.Float();
                w.moveTo(11f, 12f);
                w.curveTo(9.5f, 7f, 6f, 5f, 2f, 5.5f);
                w.curveTo(2.6f, 11f, 5.5f, 16f, 11f, 19.5f);
                g.draw(w);
                Path2D.Float w2 = new Path2D.Float();
                w2.moveTo(13f, 12f);
                w2.curveTo(14.5f, 7f, 18f, 5f, 22f, 5.5f);
                w2.curveTo(21.4f, 11f, 18.5f, 16f, 13f, 19.5f);
                g.draw(w2);
                g.draw(new Line2D.Float(10f, 13f, 5f, 10f));
                g.draw(new Line2D.Float(10.5f, 15.5f, 5.5f, 14f));
                g.draw(new Line2D.Float(14f, 13f, 19f, 10f));
                g.draw(new Line2D.Float(13.5f, 15.5f, 18.5f, 14f));
                break;
            }
            case "bandana": {
                Path2D.Float b = new Path2D.Float();
                b.moveTo(3.5f, 9.5f);
                b.quadTo(12f, 5f, 20.5f, 9.5f);
                b.lineTo(20.5f, 14.5f);
                b.quadTo(12f, 10f, 3.5f, 14.5f);
                b.closePath();
                g.draw(b);
                g.draw(poly(false, 20.5f, 12f, 22f, 19f));
                g.draw(poly(false, 20f, 13f, 17.5f, 20f));
                break;
            }
            case "pet":
                g.draw(new Ellipse2D.Float(7f, 12f, 10f, 8f));
                g.draw(new Ellipse2D.Float(2.5f, 7f, 4f, 5f));
                g.draw(new Ellipse2D.Float(7.5f, 2.5f, 4f, 5f));
                g.draw(new Ellipse2D.Float(12.5f, 2.5f, 4f, 5f));
                g.draw(new Ellipse2D.Float(17.5f, 7f, 4f, 5f));
                break;
            case "gift":
                g.draw(new RoundRectangle2D.Float(4f, 10f, 16f, 10.5f, 2f, 2f));
                g.draw(new RoundRectangle2D.Float(3f, 7f, 18f, 3.5f, 1.5f, 1.5f));
                g.draw(new Line2D.Float(12f, 7f, 12f, 20.5f));
                g.draw(new Ellipse2D.Float(6.5f, 2.5f, 5.5f, 4.5f));
                g.draw(new Ellipse2D.Float(12f, 2.5f, 5.5f, 4.5f));
                break;
            default:
                g.draw(new Ellipse2D.Float(4f, 4f, 16f, 16f));
        }
    }
}
