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
            case "reset":
                g.draw(new Ellipse2D.Float(4f, 4f, 16f, 16f));
                g.draw(poly(false, 4f, 4f, 4f, 9f, 9f, 9f));
                break;
            default:
                g.draw(new Ellipse2D.Float(4f, 4f, 16f, 16f));
        }
    }
}
