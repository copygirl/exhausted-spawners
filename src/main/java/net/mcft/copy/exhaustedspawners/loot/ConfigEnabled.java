package net.mcft.copy.exhaustedspawners.loot;

import javax.annotation.ParametersAreNonnullByDefault;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.mcft.copy.exhaustedspawners.Config;
import net.mcft.copy.exhaustedspawners.ExhaustedSpawners;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

public class ConfigEnabled implements LootItemCondition {

	public static final LootItemConditionType TYPE = new LootItemConditionType(new Serializer());

	private final String targetName;

	public ConfigEnabled(String targetName) {
		this.targetName = targetName;
	}

	@Override
	public LootItemConditionType getType() { return TYPE; }

	@Override
	public boolean test(LootContext ctx) {
		switch (targetName) {
			case "silk_touch":                return Config.SPAWNER_SILK_TOUCH.get();
			case "clear_drops_on_egg":        return Config.CLEAR_DROPS_ON_EGG.get();
			case "clear_drops_on_silk_touch": return Config.CLEAR_DROPS_ON_SILK_TOUCH.get();
			default:
				ExhaustedSpawners.LOGGER.warn(
						"Unknown name '{}' for LootItemCondition '{}:config_enabled'",
						targetName, ExhaustedSpawners.MOD_ID);
				return false;
		}
	}

	@ParametersAreNonnullByDefault
	public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<ConfigEnabled> {

		@Override
		public void serialize(JsonObject object, ConfigEnabled instance, JsonSerializationContext ctx) {
			object.addProperty("name", instance.targetName);
		}

		@Override
		public ConfigEnabled deserialize(JsonObject object, JsonDeserializationContext ctx) {
			return new ConfigEnabled(GsonHelper.getAsString(object, "name"));
		}
	}
}
