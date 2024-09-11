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

	private final String path;

	public ConfigEnabledCondition(String path) {
		this.path = path;
	}

	@Override
	public LootItemConditionType getType() { return TYPE; }

	@Override
	public boolean test(LootContext ctx) {
		switch (path) {
			case "spawner_loot.silk_touch":
				return Config.SPAWNER_SILK_TOUCH.get();
			case "spawn_egg_loot.player_kill_required":
				return Config.PLAYER_KILL_REQUIRED.get();
			case "spawn_egg_loot.clear_drops_on_egg":
				return Config.CLEAR_DROPS_ON_EGG.get();
			case "spawn_egg_loot.clear_drops_on_silk_touch":
				return Config.CLEAR_DROPS_ON_SILK_TOUCH.get();
			default:
				var message = "Unknown config '{}' for LootItemCondition '{}:config_enabled'";
				Constants.LOG.warn(message, path, Constants.MOD_ID);
				return false;
		}
	}

	@ParametersAreNonnullByDefault
	public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<ConfigEnabledCondition> {

		@Override
		public void serialize(JsonObject object, ConfigEnabledCondition instance, JsonSerializationContext ctx) {
			object.addProperty("path", instance.path);
		}

		@Override
		public ConfigEnabledCondition deserialize(JsonObject object, JsonDeserializationContext ctx) {
			return new ConfigEnabledCondition(GsonHelper.getAsString(object, "path"));
		}
	}
}
