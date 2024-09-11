package net.mcft.copy.exhaustedspawners.loot;

import javax.annotation.ParametersAreNonnullByDefault;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

import net.mcft.copy.exhaustedspawners.Config;
import net.mcft.copy.exhaustedspawners.Constants;

public class ConfigEnabledCondition implements LootItemCondition {

	public static final String ID = "config_enabled";
	public static final LootItemConditionType TYPE
		= new LootItemConditionType(new Serializer());

	private final String targetName;

	public ConfigEnabledCondition(String targetName) {
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
			default: Constants.LOG.warn(
					"Unknown name '{}' for LootItemCondition '{}:config_enabled'",
					targetName, Constants.MOD_ID);
				return false;
		}
	}

	@ParametersAreNonnullByDefault
	public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<ConfigEnabledCondition> {

		@Override
		public void serialize(JsonObject object, ConfigEnabledCondition instance, JsonSerializationContext ctx) {
			object.addProperty("name", instance.targetName);
		}

		@Override
		public ConfigEnabledCondition deserialize(JsonObject object, JsonDeserializationContext ctx) {
			return new ConfigEnabledCondition(GsonHelper.getAsString(object, "name"));
		}
	}
}
