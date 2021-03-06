package io.github.cottonmc.metatips.mixin;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.tag.ItemTags;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Collection;
import java.util.List;

@Mixin(ItemStack.class)
public class MixinItemTooltip {

	@Inject(method = "getTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/item/TooltipContext;isAdvanced()Z", ordinal = 2), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
	public void injectModName(PlayerEntity player, TooltipContext ctx, CallbackInfoReturnable cir, List<Text> tooltips) {
		Identifier id = Registry.ITEM.getId(((ItemStack)(Object)this).getItem());
		if (FabricLoader.getInstance().getModContainer(id.getNamespace()).isPresent()) {
			ModContainer mod = FabricLoader.getInstance().getModContainer(id.getNamespace()).get();
			tooltips.add(new LiteralText(mod.getMetadata().getName()).formatted(Formatting.BLUE));
		} else if (id.getNamespace().equals("minecraft")) {
			tooltips.add(new LiteralText("Minecraft").formatted(Formatting.BLUE));
		} else if (id.getNamespace().equals("c")) {
			tooltips.add(new TranslatableText("tooltip.metatips.common").formatted(Formatting.BLUE, Formatting.ITALIC));
		} else {
			tooltips.add(new TranslatableText("tooltip.metatips.unknown").formatted(Formatting.BLUE, Formatting.ITALIC));
		}
	}

	@Inject(method = "getTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;hasTag()Z", ordinal = 5), locals = LocalCapture.CAPTURE_FAILEXCEPTION, cancellable = true)
	public void appendStackInfo(PlayerEntity player, TooltipContext ctx, CallbackInfoReturnable cir, List<Text> tooltips) {
		if (((ItemStack)(Object)this).hasTag()) {
			CompoundTag tag = ((ItemStack)(Object)this).getTag();
			if (Screen.hasControlDown()) {
				tooltips.add((new TranslatableText("item.nbt_tags", tag.getKeys().size())).formatted(Formatting.DARK_GRAY));
				for (String key : tag.getKeys()) {
					int type = tag.getType(key);
					String tooltip = "  \"" + key + "\" (" + getTagName(type) + ", ";
					// only display raw values for non-collection tags
					if (type != 7 && type < 9) {
						tooltip += tag.getTag(key).asString();
					} else {
						int items = 0;
						switch(type) {
							case 7:
								items = tag.getByteArray(key).length;
								break;
							case 9:
								items = getArbitraryList(tag, key).size();
								break;
							case 10:
								items = tag.getCompound(key).getSize();
								break;
							case 11:
								items = tag.getIntArray(key).length;
								break;
							case 12:
								items = tag.getLongArray(key).length;
								break;
							default:
								break;
						}
						tooltip += new TranslatableText("tooltip.metatips.nbt_collection_size", items).asString();
					}
					tooltip += ")";
					tooltips.add(new LiteralText(tooltip).formatted(Formatting.DARK_GRAY));
				}
			} else {
				if (MinecraftClient.IS_SYSTEM_MAC) {
					tooltips.add(new TranslatableText("tooltip.metatips.nbt_tags_mac", tag.getKeys().size()).formatted(Formatting.DARK_GRAY));
				} else {
					tooltips.add(new TranslatableText("tooltip.metatips.nbt_tags", tag.getKeys().size()).formatted(Formatting.DARK_GRAY));
				}
			}
		}
		Collection<Identifier> tags = ItemTags.getContainer().getTagsFor(((ItemStack) (Object) this).getItem());
		if (!tags.isEmpty()) {
			if (Screen.hasAltDown()) {
				tooltips.add(new TranslatableText("tooltip.metatips.tags_header", tags.size()).formatted(Formatting.DARK_GRAY));
				for (Identifier id : tags) {
					tooltips.add(new LiteralText("  #" + id.toString()).formatted(Formatting.DARK_GRAY));
				}
			} else {
				tooltips.add(new TranslatableText("tooltip.metatips.data_tags", tags.size()).formatted(Formatting.DARK_GRAY));
			}
		}
		cir.setReturnValue(tooltips);
	}

	// because the CompoundTag class doesn't let you grab a list unless you already know its type,
	// but we don't care about the type here since we're just showing how many elements the list has
	private static ListTag getArbitraryList(CompoundTag tag, String key) {
		if (!tag.containsKey(key)) return new ListTag();
		try {
			if (tag.getTag(key).getType() == 9) return (ListTag)tag.getTag(key);
		} catch (ClassCastException e) {
			return new ListTag();
		}

		return new ListTag();
	}

	private static String getTagName(int id) {
		switch(id) {
			case 0:
				return "End";
			case 1:
				return "Byte";
			case 2:
				return "Short";
			case 3:
				return "Int";
			case 4:
				return "Long";
			case 5:
				return "Float";
			case 6:
				return "Double";
			case 7:
				return "Byte Array";
			case 8:
				return "String";
			case 9:
				return "List";
			case 10:
				return "Compound";
			case 11:
				return "Int Array";
			case 12:
				return "Long Array";
			case 99:
				return "Any Numeric Tag";
			default:
				return "UNKNOWN";
		}
	}

}
