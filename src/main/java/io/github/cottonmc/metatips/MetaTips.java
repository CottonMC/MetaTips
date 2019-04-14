package io.github.cottonmc.metatips;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.packet.CustomPayloadC2SPacket;
import net.minecraft.text.StringTextComponent;
import net.minecraft.text.TextComponent;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.registry.Registry;

public class MetaTips implements ModInitializer {
	public static final Identifier ITEM_CHAT = new Identifier("metatips", "item_chat");

	@Override
	public void onInitialize() {
		ServerSidePacketRegistry.INSTANCE.register(ITEM_CHAT, (ctx, buf) -> {
			ItemStack stack = buf.readItemStack();
			PlayerEntity player = ctx.getPlayer();
			TextComponent message = new StringTextComponent("<").append(player.getDisplayName()).append("> ").append(stack.toTextComponent());
			player.getServer().getPlayerManager().broadcastChatMessage(message, false);
		});

		Registry.register(Registry.ITEM, "c:test", new Item(new Item.Settings().itemGroup(ItemGroup.MISC)));
	}

	@Environment(EnvType.CLIENT)
	public static void chatItem(ItemStack stack) {
		PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
		buf.writeItemStack(stack);
		MinecraftClient.getInstance().getNetworkHandler().getClientConnection().sendPacket(new CustomPayloadC2SPacket(ITEM_CHAT, buf));
	}
}
