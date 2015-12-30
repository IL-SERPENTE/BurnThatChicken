package com.samagames.burnthatchicken.task;

import java.util.UUID;

import com.samagames.burnthatchicken.BTCPlugin;
import com.samagames.burnthatchicken.metadata.SpecialChicken;

public class PowerUpTask implements Runnable {
	private BTCPlugin main;
	private UUID name;
	private SpecialChicken special;

	public PowerUpTask(BTCPlugin m, UUID n, SpecialChicken s) {
		main = m;
		name = n;
		special = s;
	}

	@Override
	public void run() {
		main.removePowerUp(this);
	}

	public UUID getName() {
		return name;
	}

	public SpecialChicken getSpecial() {
		return special;
	}
}
