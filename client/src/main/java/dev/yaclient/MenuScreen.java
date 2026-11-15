package dev.yaclient;

import dev.yaclient.modules.Module;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;

/** Menu rosso/bianco/nero per attivare i moduli. */
public class MenuScreen extends GuiScreen {
    @Override
    public void initGui() {
        buttonList.clear();
        int y = height / 2 - (YAClient.MODULES.size() * 24) / 2 + 10;
        for (int i = 0; i < YAClient.MODULES.size(); i++) {
            buttonList.add(new GuiButton(i, width / 2 - 90, y + i * 24, 180, 20, label(YAClient.MODULES.get(i))));
        }
    }

    private String label(Module m) {
        return (m.enabled ? "\u00a7c\u25cf \u00a7f" : "\u00a77\u25cb ") + m.name;
    }

    @Override
    protected void actionPerformed(GuiButton b) {
        Module m = YAClient.MODULES.get(b.id);
        m.toggle();
        b.displayString = label(m);
    }

    @Override
    public void drawScreen(int mx, int my, float pt) {
        Gui.drawRect(0, 0, width, height, 0xCC000000);
        Gui.drawRect(width / 2 - 100, 0, width / 2 - 98, height, YAClient.RED);
        drawCenteredString(fontRendererObj, "\u00a7lYA \u00a7c\u00a7lCLIENT", width / 2, height / 2 - 100, 0xFFFFFFFF);
        super.drawScreen(mx, my, pt);
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}
