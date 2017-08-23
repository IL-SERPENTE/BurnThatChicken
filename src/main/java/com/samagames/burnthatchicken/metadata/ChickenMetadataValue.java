package com.samagames.burnthatchicken.metadata;

import org.bukkit.entity.Entity;
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
public class ChickenMetadataValue {
    private int id;
    private SpecialChicken special;

    public ChickenMetadataValue(int id, SpecialChicken sc) {
        this.id = id;
        this.special = sc;
    }

    public int getGameZoneId() {
        return id;
    }

    public boolean isSpecial() {
        return special != null;
    }

    public SpecialChicken getSpecialAttribute() {
        return special;
    }

    public static void setMetadataValueToChicken(Plugin pl, Entity e, int id, SpecialChicken sc) {
        MetadataUtils.setMetaData(pl, e, "btc-chickenmeta",
                new ChickenMetadataValue(id, sc));
    }

    public static ChickenMetadataValue getMetadataValueFromChicken(Plugin pl, Entity e) {
        Object o = MetadataUtils.getMetaData(pl, e, "btc-chickenmeta");
        if (o != null && o instanceof ChickenMetadataValue)
            return (ChickenMetadataValue) o;
        return null;
    }
}
