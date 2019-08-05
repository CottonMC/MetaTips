package io.github.cottonmc.metatips.mixin;

import io.github.cottonmc.metatips.MetaTips;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.AbstractContainerScreen;
import net.minecraft.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
public abstract class MixinItemChat extends Screen {

	@Shadow protected Slot focusedSlot;

	protected MixinItemChat(Text name) {
		super(name);
	}

	@Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
	public void chatItem(int key, int scanCode, int int_3, CallbackInfoReturnable cir) {
		if (MinecraftClient.getInstance().options.keyChat.matchesKey(key, scanCode) && Screen.hasShiftDown()) {
			if (focusedSlot != null && focusedSlot.hasStack()) {
				ItemStack stack = focusedSlot.getStack();
				MetaTips.chatItem(stack);
				cir.setReturnValue(true);
			}
		}
	}
}
