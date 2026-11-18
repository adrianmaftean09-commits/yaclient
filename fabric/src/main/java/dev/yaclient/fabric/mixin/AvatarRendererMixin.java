package dev.yaclient.fabric.mixin;

import dev.yaclient.fabric.CosmeticsApi;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

/** Hides the vanilla cape when a YA CLIENT cape is worn, so the two never overlap. */
@Mixin(AvatarRenderer.class)
public abstract class AvatarRendererMixin {
    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V", at = @At("TAIL"))
    private void ya$hideVanillaCape(Avatar entity, AvatarRenderState state, float partialTick, CallbackInfo ci) {
        if (!(entity instanceof Player p)) return;
        try {
            Map<String, String> eq = CosmeticsApi.equippedFor(p);
            String cape = eq == null ? null : eq.get("cape");
            if (cape != null && !cape.isEmpty() && !"none".equals(cape)) state.showCape = false;
        } catch (Throwable ignored) { }
    }
}
