package com.toweroflife;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class TowerOfLifePluginLauncher
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(TowerOfLifePlugin.class);
		RuneLite.main(args);
	}
}
