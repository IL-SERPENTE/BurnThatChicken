package com.samagames.burnthatchicken.metadata;

import java.util.List;

import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.Metadatable;
import org.bukkit.plugin.Plugin;

/*
 * This file is part of BurnThatChicken.
 *
 * BurnThatChicken is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BurnThatChicken is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with BurnThatChicken.  If not, see <http://www.gnu.org/licenses/>.
 */
public class MetadataUtils {
    private MetadataUtils() {
    }

    public static void setMetaData(Plugin plugin, Metadatable entity, String key, Object value) {
        entity.setMetadata(key, new FixedMetadataValue(plugin, value));
    }

    public static Object getMetaData(Plugin plugin, Metadatable entity, String key) {
        List<MetadataValue> list = entity.getMetadata(key);
        for (MetadataValue v : list) {
            if (v.getOwningPlugin().equals(plugin))
                return v.value();
        }
        return null;
    }
}
