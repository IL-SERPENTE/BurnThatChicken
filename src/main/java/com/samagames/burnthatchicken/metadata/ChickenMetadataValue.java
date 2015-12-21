package com.samagames.burnthatchicken.metadata;

import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;


public class ChickenMetadataValue
{
	private int id;
	private SpecialChicken special;
	
	public ChickenMetadataValue(int id, SpecialChicken sc)
	{
		this.id = id;
		this.special = sc;
	}
	
	public int getGameZoneId()
	{
		return id;
	}
	
	public boolean isSpecial()
	{
		return special != null;
	}
	
	public SpecialChicken getSpecialAttribute()
	{
		return special;
	}
	
	public static void setMetadataValueToChicken(Plugin pl, Entity e, int id, SpecialChicken sc)
	{
		MetadataUtils.setMetaData(pl, e, "btc-chickenmeta", new ChickenMetadataValue(id, sc));
	}
	
	public static ChickenMetadataValue getMetadataValueFromChicken(Plugin pl, Entity e)
	{
		Object o = MetadataUtils.getMetaData(pl, e, "btc-chickenmeta");
		if (o != null && o instanceof ChickenMetadataValue)
			return (ChickenMetadataValue)o;
		return null;
	}
}
