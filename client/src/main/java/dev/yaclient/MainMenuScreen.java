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

/** YA CLIENT title screen: dark animated backdrop, centered logo, stacked buttons and a small icon dock. */
public class MainMenuScreen extends GuiScreen {
    private static final int BW = 220, BH = 26, GAP = 7;
    private static final String[] ICONS = {"shirt", "gear", "wrench", "close"};
    private static final String[] TIPS = {"tab.cosmetics", "menu.options", "tab.settings", "menu.quit"};

    private static final ResourceLocation[] PANO = new ResourceLocation[6];
    static { for (int i = 0; i < 6; i++) PANO[i] = new ResourceLocation("textures/gui/title/background/panorama_" + i + ".png"); }

    private final float[] hover = new float[3];
    private final float[] dock = new float[ICONS.length];
    private final long opened = System.currentTimeMillis();
    private long last = opened;
    private int bx, by0, dockX, dockY;

    private int btnY(int i) { return by0 + i * (BH + GAP); }

    @Override
    public void drawScreen(int mx, int my, float pt) {
        long now = System.currentTimeMillis();
        float dt = Math.min(100, now - last);
        last = now;
        float t = (now - opened) / 1000f;
        float fadeIn = Ui.ease((now - opened) / 500f);

        // backdrop: the slowly rotating Minecraft panorama, lightly darkened (no black screen)
        drawPanorama((now - opened) / 50f);
        drawGradientRect(0, 0, width, height, 0x38000000, 0x70000000);

        int gs = new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor();
        Gfx.setScale(Math.max(1, gs));
        Gfx.fade = fadeIn;

        int logo = Math.min(96, height / 4);
        int cx = width / 2;
        int total = logo + 34 + 3 * BH + 2 * GAP;
        int top = Math.max(16, (height - total) / 2 - 12);
        float bob = (float) Math.sin(t * 1.4f) * 1.5f;
        Logo.draw(cx - logo / 2f, top + bob, logo, logo, fadeIn);
        int wy = top + logo + 6;
        int yaw = Gfx.width("YA ", 14, true), clw = Gfx.width("CLIENT", 14, true);
        Gfx.text("YA ", cx - (yaw + clw) / 2, wy, 14, true, Theme.TEXT);
        Gfx.text("CLIENT", cx - (yaw + clw) / 2 + yaw, wy, 14, true, Theme.BRAND_RED);

        bx = cx - BW / 2;
        by0 = wy + 26;
        String[] labels = {Lang.t("menu.play"), Lang.t("menu.multiplayer"), Lang.t("menu.client_settings")};
        for (int i = 0; i < 3; i++) {
            int y = btnY(i);
            boolean over = mx >= bx && mx < bx + BW && my >= y && my < y + BH;
            hover[i] = Ui.approach(hover[i], over ? 1f : 0f, dt, 16f);
            boolean primary = i == 2;
            Gfx.rr(bx, y, BW, BH, 8, primary ? Ui.mix(0xF0F4F4F5, 0xFFFFFFFF, hover[i]) : Ui.mix(0x90101014, 0xB0242429, hover[i]));
            if (!primary) Gfx.rrOutline(bx, y, BW, BH, 8, 1, Ui.mix(0x2EFFFFFF, 0x66FFFFFF, hover[i]));
            int fg = primary ? 0xFF101014 : Theme.TEXT;
            Gfx.textCentered(labels[i], bx + BW / 2, y + (BH - Gfx.lineHeight(12, true)) / 2, 12, true, fg);
        }

        // icon dock
        int dsz = 22, dg = 6;
        dockX = cx - (ICONS.length * dsz + (ICONS.length - 1) * dg) / 2;
        dockY = btnY(3) + 6;
        for (int i = 0; i < ICONS.length; i++) {
            int x = dockX + i * (dsz + dg);
            boolean over = mx >= x && mx < x + dsz && my >= dockY && my < dockY + dsz;
            dock[i] = Ui.approach(dock[i], over ? 1f : 0f, dt, 16f);
            Gfx.rr(x, dockY, dsz, dsz, 7, Ui.mix(0x90101014, 0xB0242429, dock[i]));
            Gfx.rrOutline(x, dockY, dsz, dsz, 7, 1, Ui.mix(0x2EFFFFFF, 0x66FFFFFF, dock[i]));
            Gfx.icon(ICONS[i], x + 4, dockY + 4, 14, Ui.mix(Theme.MUTED, Theme.TEXT, dock[i]));
            if (over) Gfx.textCentered(Lang.t(TIPS[i]), cx, dockY + dsz + 6, 9, false, Theme.MUTED);
        }

        Gfx.text("YA CLIENT 1.8.9", 8, height - 16, 10, false, Theme.DIM);
        String acc = Minecraft.getMinecraft().getSession().getUsername();
        Gfx.text(acc, width - Gfx.width(acc, 10, false) - 8, height - 16, 10, false, Theme.MUTED);
        Gfx.fade = 1f;
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
                wr.pos(-1.0D, 1.0D, 1.0D).tex(0.0D, 1.0D).color(255, 255, 255, a).endVertex();
                wr.pos(1.0D, 1.0D, 1.0D).tex(1.0D, 1.0D).color(255, 255, 255, a).endVertex();
                wr.pos(1.0D, -1.0D, 1.0D).tex(1.0D, 0.0D).color(255, 255, 255, a).endVertex();
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
        for (int i = 0; i < 3; i++) {
            int y = btnY(i);
            if (mx >= bx && mx < bx + BW && my >= y && my < y + BH) {
                if (i == 0) mc.displayGuiScreen(new GuiSelectWorld(this));
                else if (i == 1) mc.displayGuiScreen(new GuiMultiplayer(this));
                else mc.displayGuiScreen(new MenuScreen());
                return;
            }
        }
        int dsz = 22, dg = 6;
        for (int i = 0; i < ICONS.length; i++) {
            int x = dockX + i * (dsz + dg);
            if (mx >= x && mx < x + dsz && my >= dockY && my < dockY + dsz) {
                if (i == 0) mc.displayGuiScreen(new MenuScreen(4));
                else if (i == 1) mc.displayGuiScreen(new GuiOptions(this, mc.gameSettings));
                else if (i == 2) mc.displayGuiScreen(new MenuScreen(5));
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
