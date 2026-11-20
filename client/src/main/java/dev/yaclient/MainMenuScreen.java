package dev.yaclient;

import dev.yaclient.cosmetics.Logo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSelectWorld;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.util.glu.Project;

/** YA CLIENT title screen: panorama backdrop, centered logo only (no title text), stacked buttons and a dock of labeled icon tiles. */
public class MainMenuScreen extends GuiScreen {
    private static final int BW = 140, BH = 20, GAP = 5, TW = 31, TH = 24, DG = 5;
    private static final String[] ICONS = {"shirt", "friends", "gear", "close"};

    private static final ResourceLocation[] PANO = new ResourceLocation[6];
    static { for (int i = 0; i < 6; i++) PANO[i] = new ResourceLocation("textures/gui/title/background/panorama_" + i + ".png"); }

    private final float[] hover = new float[3];
    private final float[] dock = new float[ICONS.length];
    private final long opened = System.currentTimeMillis();
    private long last = opened;
    private int bx, by0, dockX, dockY;
    private int nx, ny, nw = 120, nh = 82;
    private float newsHover;

    private int btnY(int i) { return by0 + i * (BH + GAP); }

    @Override
    public void drawScreen(int mx, int my, float pt) {
        long now = System.currentTimeMillis();
        float dt = Math.min(100, now - last);
        last = now;
        float t = (now - opened) / 1000f;
        float fadeIn = Ui.ease((now - opened) / 500f);

        // backdrop: bright, soft gradient with slowly drifting color blobs
        drawLightBackdrop(t);

        int gs = new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor();
        Gfx.setScale(Math.max(1, gs));
        Gfx.fade = fadeIn;

        int logo = Math.min(38, height / 7), badge = logo + 16;
        boolean wideScr = width >= 520;
        int plW = 104, gapC = 34;
        int contentH = badge + 12 + 3 * BH + 2 * GAP + 9 + TH;
        int total = wideScr ? BW + gapC + plW : BW;
        int leftX = wideScr ? Math.max(138, (width - total) / 2) : (width - total) / 2;
        int top = Math.max(10, (height - contentH) / 2);
        int cx = leftX + BW / 2;
        if (wideScr) {
            int pcx = leftX + BW + gapC + plW / 2;
            int feet = top + contentH - 20;
            blob(pcx, feet + 2, 34f, 5f, 0x1A2238, 70);
            MenuPlayer.draw(pcx, feet, Math.max(30, (contentH - 30) * 10 / 19), ((now - opened) % 9000L) / 9000f * 360f);
            String nm = Minecraft.getMinecraft().getSession().getUsername();
            Gfx.textCentered(nm, pcx, feet + 9, 10, true, INK);
        }
        // logo on a dark rounded badge (the logo has a white Y, it needs a dark base on a bright screen)
        float bob = (float) Math.sin(t * 1.4f) * 1.2f;
        Gfx.rr(cx - badge / 2, Math.round(top + bob), badge, badge, 12, 0xFF1A1C26);
        Logo.draw(cx - logo / 2f, top + bob + 8, logo, logo, fadeIn);

        bx = cx - BW / 2;
        by0 = top + badge + 12;
        String[] labels = {Lang.t("menu.play"), Lang.t("menu.multiplayer"), Lang.t("menu.client_settings")};
        for (int i = 0; i < 3; i++) {
            int x = bx, y = btnY(i);
            boolean over = mx >= x && mx < x + BW && my >= y && my < y + BH;
            hover[i] = Ui.approach(hover[i], over ? 1f : 0f, dt, 16f);
            boolean primary = i == 2;
            if (primary) Gfx.rr(x, y, BW, BH, 7, Ui.mix(0xFF1A1C26, 0xFF2D3042, hover[i]));
            else {
                Gfx.rr(x, y, BW, BH, 7, Ui.mix(0xE6FFFFFF, 0xFFFFFFFF, hover[i]));
                Gfx.rrOutline(x, y, BW, BH, 7, 1, Ui.mix(0x24000000, 0x44000000, hover[i]));
            }
            Gfx.textCentered(labels[i], x + BW / 2, y + (BH - Gfx.lineHeight(9, true)) / 2, 9, true, primary ? 0xFFFFFFFF : INK);
        }

        // icon dock: four clear tiles, big icon
        int dockW = ICONS.length * TW + (ICONS.length - 1) * DG;
        dockX = cx - dockW / 2;
        dockY = btnY(2) + BH + 9;
        for (int i = 0; i < ICONS.length; i++) {
            int x = dockX + i * (TW + DG);
            boolean over = mx >= x && mx < x + TW && my >= dockY && my < dockY + TH;
            dock[i] = Ui.approach(dock[i], over ? 1f : 0f, dt, 16f);
            Gfx.rr(x, dockY, TW, TH, 8, Ui.mix(0xD8FFFFFF, 0xFFFFFFFF, dock[i]));
            Gfx.rrOutline(x, dockY, TW, TH, 8, 1, Ui.mix(0x22000000, 0x44000000, dock[i]));
            Gfx.icon(ICONS[i], x + (TW - 16) / 2, dockY + (TH - 16) / 2, 16, Ui.mix(0xFF3A3F52, INK, dock[i]));
        }

        // news card (bottom left): banner + title, opens the changelog sheet
        nx = 10;
        ny = height - 20 - 8 - nh;
        boolean nOver = mx >= nx && mx < nx + nw && my >= ny && my < ny + nh;
        newsHover = Ui.approach(newsHover, nOver ? 1f : 0f, dt, 16f);
        Gfx.rr(nx, ny, nw, nh, 8, Ui.mix(0xE6FFFFFF, 0xFFFFFFFF, newsHover));
        int artH = Math.round((nw - 8) / NewsArt.ASPECT);
        NewsArt.draw(nx + 4, ny + 4, nw - 8, artH, fadeIn);
        Gfx.rrOutline(nx, ny, nw, nh, 8, 1, Ui.mix(0x22000000, 0x44000000, newsHover));
        Gfx.text(Lang.t("news.card"), nx + 8, ny + 4 + artH + 5, 9, true, INK);
        Gfx.text(Gfx.trim("Update " + NewsScreen.VERSION + " · " + Lang.t("news.open"), 7, false, nw - 14), nx + 8, ny + 4 + artH + 18, 7, false, INK2);

        RecentServers.draw(mx, my, height);
        Gfx.text("CLIENT 1.8.9", 8, height - 14, 10, false, INK2);
        String acc = Minecraft.getMinecraft().getSession().getUsername();
        Gfx.text(acc, width - Gfx.width(acc, 10, false) - 8, height - 14, 10, false, INK2);
        Gfx.fade = 1f;
        FriendToast.render();
    }

    static final int INK = 0xFF1A1C26, INK2 = 0xFF5B6272;

    /** Soft radial blob (an ellipse of color fading to transparent), alpha 0..255 at the center. */
    private static void blob(float cx, float cy, float rx, float ry, int rgb, int alpha) {
        Tessellator t = Tessellator.getInstance();
        WorldRenderer wr = t.getWorldRenderer();
        int r = (rgb >> 16) & 255, g = (rgb >> 8) & 255, b = rgb & 255;
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.disableAlpha();
        wr.begin(6, DefaultVertexFormats.POSITION_COLOR);
        wr.pos(cx, cy, 0).color(r, g, b, alpha).endVertex();
        for (int i = 0; i <= 32; i++) {
            double a = i * Math.PI * 2 / 32;
            wr.pos(cx + Math.cos(a) * rx, cy + Math.sin(a) * ry, 0).color(r, g, b, 0).endVertex();
        }
        t.draw();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    private void drawLightBackdrop(float t) {
        drawGradientRect(0, 0, width, height, 0xFFEEF3FC, 0xFFD5E0F4);
        float w = width, h = height;
        blob(w * 0.82f + (float) Math.sin(t * 0.21f) * 18f, h * 0.18f, w * 0.55f, h * 0.75f, 0x7FA2FF, 120);
        blob(w * 0.12f + (float) Math.cos(t * 0.17f) * 16f, h * 0.88f, w * 0.5f, h * 0.7f, 0xC9A4FF, 95);
        blob(w * 0.52f + (float) Math.sin(t * 0.13f) * 22f, h * 0.55f, w * 0.38f, h * 0.55f, 0xFFFFFF, 150);
        blob(w * 0.95f, h * 0.95f, w * 0.3f, h * 0.45f, 0xFF9A9A, 55);
    }

    /** The vanilla title panorama (same technique as the original menu), rendered at the screen's aspect. */
    private void drawPanorama(float timer) {
        Tessellator t = Tessellator.getInstance();
        WorldRenderer wr = t.getWorldRenderer();
        GlStateManager.matrixMode(5889);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();
        Project.gluPerspective(85.0F, (float) width / (float) height, 0.05F, 10.0F);
        GlStateManager.matrixMode(5888);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.disableCull();
        GlStateManager.depthMask(false);
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        final int n = 4;
        for (int j = 0; j < n * n; j++) {
            GlStateManager.pushMatrix();
            float dx = ((float) (j % n) / n - 0.5F) / 64.0F, dy = ((float) (j / n) / n - 0.5F) / 64.0F;
            GlStateManager.translate(dx, dy, 0.0F);
            GlStateManager.rotate((float) Math.sin(timer / 400.0F) * 25.0F + 20.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(-timer * 0.1F, 0.0F, 1.0F, 0.0F);
            for (int k = 0; k < 6; k++) {
                GlStateManager.pushMatrix();
                if (k == 1) GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
                if (k == 2) GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
                if (k == 3) GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
                if (k == 4) GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
                if (k == 5) GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
                Minecraft.getMinecraft().getTextureManager().bindTexture(PANO[k]);
                wr.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                int a = 255 / (j + 1);
                wr.pos(-1.0D, -1.0D, 1.0D).tex(0.0D, 0.0D).color(255, 255, 255, a).endVertex();
                wr.pos(-1.0D, 1.0D, 1.0D).tex(1.0D, 0.0D).color(255, 255, 255, a).endVertex();
                wr.pos(1.0D, 1.0D, 1.0D).tex(1.0D, 1.0D).color(255, 255, 255, a).endVertex();
                wr.pos(1.0D, -1.0D, 1.0D).tex(0.0D, 1.0D).color(255, 255, 255, a).endVertex();
                t.draw();
                GlStateManager.popMatrix();
            }
            GlStateManager.popMatrix();
            GlStateManager.colorMask(true, true, true, false);
        }
        GlStateManager.colorMask(true, true, true, true);
        GlStateManager.matrixMode(5889);
        GlStateManager.popMatrix();
        GlStateManager.matrixMode(5888);
        GlStateManager.popMatrix();
        GlStateManager.depthMask(true);
        GlStateManager.enableCull();
        GlStateManager.enableDepth();
        GlStateManager.enableAlpha();
    }

    @Override
    protected void mouseClicked(int mx, int my, int button) {
        if (button != 0) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (RecentServers.click(mx, my, height, this)) return;
        if (mx >= nx && mx < nx + nw && my >= ny && my < ny + nh) { mc.displayGuiScreen(new NewsScreen(this)); return; }
        for (int i = 0; i < 3; i++) {
            int y = btnY(i);
            if (mx >= bx && mx < bx + BW && my >= y && my < y + BH) {
                if (i == 0) mc.displayGuiScreen(new GuiSelectWorld(this));
                else if (i == 1) mc.displayGuiScreen(new ServerListScreen(this));
                else mc.displayGuiScreen(new MenuScreen());
                return;
            }
        }
        for (int i = 0; i < ICONS.length; i++) {
            int x = dockX + i * (TW + DG);
            if (mx >= x && mx < x + TW && my >= dockY && my < dockY + TH) {
                if (i == 0) mc.displayGuiScreen(new MenuScreen(4));
                else if (i == 1) mc.displayGuiScreen(new FriendsScreen(this));
                else if (i == 2) mc.displayGuiScreen(new GuiOptions(this, mc.gameSettings));
                else mc.shutdown();
                return;
            }
        }
    }

    @Override
    protected void keyTyped(char typed, int key) {}

    @Override
    public boolean doesGuiPauseGame() { return false; }
}
