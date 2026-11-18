package dev.yaclient.fabric.mixin;

import dev.yaclient.fabric.Badges;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Puts the YA logo (and the gold +) in front of the name above the head of YA CLIENT players. */
@Mixin(PlayerRenderer.class)
public abstract class AvatarRendererMixin {
    @ModifyVariable(method = "renderNameTag(Lnet/minecraft/client/player/AbstractClientPlayer;Lnet/minecraft/network/chat/Component;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IF)V", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private Component yaclient$nametag(Component name, AbstractClientPlayer p) {
        return Badges.nametag(p, name);
    }
}
