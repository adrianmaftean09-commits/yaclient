package dev.yaclient.modules;

import dev.yaclient.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.MathHelper;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * 1.7 animations: the sword swings while you block (block-hit) and sits a little lower, like in 1.7.
 * The first-person item renderer is swapped for a subclass (found by field type, so it works with any mapping).
 */
public class OldAnimationsModule extends Module {
    static final Setting.Bool BLOCKHIT = new Setting.Bool("blockhit", true);
    static final Setting.Bool LOW = new Setting.Bool("low_sword", true);
    static volatile boolean ACTIVE;

    private boolean tried;

    public OldAnimationsModule() {
        super("old_animations", Category.PVP, false);
        add(BLOCKHIT);
        add(LOW);
    }

    @Override
    public void onAlwaysTick() {
        ACTIVE = enabled;
        if (tried || !enabled || mc == null || mc.entityRenderer == null) return;
        tried = true;
        try {
            EntityRenderer er = mc.entityRenderer;
            Field slot = null;
            for (Field f : EntityRenderer.class.getDeclaredFields()) {
                if (f.getType() == ItemRenderer.class && !Modifier.isStatic(f.getModifiers())) { slot = f; break; }
            }
            if (slot == null) return;
            slot.setAccessible(true);
            ItemRenderer old = (ItemRenderer) slot.get(er);
            if (old instanceof OldItemRenderer) return;
            OldItemRenderer neu = new OldItemRenderer(mc);
            for (Field f : ItemRenderer.class.getDeclaredFields()) {     // keep the equipped item and its progress
                if (Modifier.isStatic(f.getModifiers())) continue;
                f.setAccessible(true);
                f.set(neu, f.get(old));
            }
            slot.set(er, neu);
        } catch (Throwable ignored) { }
    }

    private static final class OldItemRenderer extends ItemRenderer {
        OldItemRenderer(Minecraft mc) { super(mc); }

        @Override
        public void renderItemInFirstPerson(float pt) {
            Minecraft mc = Minecraft.getMinecraft();
            EntityPlayerSP p = mc.thePlayer;
            ItemStack held = p == null ? null : p.getHeldItem();
            boolean block = ACTIVE && held != null && held.getItem() instanceof ItemSword && p.isUsingItem()
                    && held.getItemUseAction() == EnumAction.BLOCK && mc.gameSettings.thirdPersonView == 0;
            if (!block) { super.renderItemInFirstPerson(pt); return; }
            GlStateManager.pushMatrix();
            // everything below happens "inside" the item's own position, exactly where vanilla does its swing
            GlStateManager.translate(0.56f, -0.52f, -0.72f);
            if (LOW.value) GlStateManager.translate(0f, -0.06f, 0f);
            float sw = p.getSwingProgress(pt);
            if (BLOCKHIT.value && sw > 0f) {
                float s1 = MathHelper.sin(sw * sw * (float) Math.PI);
                float s2 = MathHelper.sin(MathHelper.sqrt_float(sw) * (float) Math.PI);
                GlStateManager.translate(-s2 * 0.4f, MathHelper.sin(MathHelper.sqrt_float(sw) * (float) Math.PI * 2f) * 0.2f,
                        -MathHelper.sin(sw * (float) Math.PI) * 0.2f);
                GlStateManager.rotate(45f, 0f, 1f, 0f);
                GlStateManager.rotate(s1 * -20f, 0f, 1f, 0f);
                GlStateManager.rotate(s2 * -20f, 0f, 0f, 1f);
                GlStateManager.rotate(s2 * -80f, 1f, 0f, 0f);
                GlStateManager.rotate(-45f, 0f, 1f, 0f);
            }
            GlStateManager.translate(-0.56f, 0.52f, 0.72f);
            super.renderItemInFirstPerson(pt);
            GlStateManager.popMatrix();
        }
    }
}
