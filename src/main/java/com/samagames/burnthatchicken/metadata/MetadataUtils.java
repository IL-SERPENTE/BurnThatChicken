package com.samagames.burnthatchicken.metadata;

import java.util.List;

import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.Metadatable;
import org.bukkit.plugin.Plugin;

public class MetadataUtils {
	private MetadataUtils() {
	}

	public static void setMetaData(Plugin plugin, Metadatable entity,
			String key, Object value) {
		entity.setMetadata(key, new FixedMetadataValue(plugin, value));
	}

	public static Object getMetaData(Plugin plugin, Metadatable entity,
			String key) {
		List<MetadataValue> list = entity.getMetadata(key);
		for (MetadataValue v : list) {
			if (v.getOwningPlugin().equals(plugin))
				return v.value();
		}
		return null;
	}
}
