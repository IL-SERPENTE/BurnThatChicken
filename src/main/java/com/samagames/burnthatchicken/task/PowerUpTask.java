package com.samagames.burnthatchicken.task;

import com.samagames.burnthatchicken.BTCPlugin;
import com.samagames.burnthatchicken.metadata.SpecialChicken;

public class PowerUpTask implements Runnable
{
	private BTCPlugin main;
	private String name;
	private SpecialChicken special;
	
	public PowerUpTask(BTCPlugin m, String n, SpecialChicken s)
	{
		main = m;
		name = n;
		special = s;
	}
	
	@Override
	public void run()
	{
		main.removePowerUp(this);
	}

	public String getName()
	{
		return name;
	}
	
	public SpecialChicken getSpecial()
	{
		return special;
	}
}
