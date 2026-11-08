/*
 * Copyright (c) 2026, kschmanski <https://github.com/kschmanski>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.toweroflife;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.VarbitID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class TowerOfLifePluginTest
{
	@Test
	public void everyItemBelongsToExactlyOneAltar()
	{
		Set<Integer> seen = new HashSet<>();
		for (Altar altar : Altar.values())
		{
			for (int itemId : altar.getItemIds())
			{
				assertTrue("duplicate item " + itemId, seen.add(itemId));
				assertEquals(altar, Altar.forItem(itemId));
			}
		}
		assertEquals(12, seen.size());
		assertNull(Altar.forItem(ItemID.COINS));
	}

	@Test
	public void nearestMatchesAltarTiles()
	{
		for (Altar altar : Altar.values())
		{
			WorldPoint center = altar.getLocation().dx(1).dy(1);
			assertEquals(altar, Altar.nearest(center));
		}
		assertNull(Altar.nearest(new WorldPoint(3040, 4385, 0)));
		assertNull(Altar.nearest(new WorldPoint(3058, 4410, 1)));
	}

	@Test
	public void mappingRoundTrip()
	{
		Map<Integer, Integer> mapping = ImmutableMap.of(
			VarbitID.TOL_SYMBOL_OBJ2, ItemID.FEATHER,
			VarbitID.TOL_SYMBOL_OBJ1, ItemID.EYE_OF_NEWT);
		String formatted = TowerOfLifePlugin.formatMapping(mapping);
		assertEquals(VarbitID.TOL_SYMBOL_OBJ1 + ":" + ItemID.EYE_OF_NEWT + "," + VarbitID.TOL_SYMBOL_OBJ2 + ":" + ItemID.FEATHER, formatted);
		assertEquals(mapping, TowerOfLifePlugin.parseMapping(formatted));
	}

	@Test
	public void parseMappingRejectsBadEntries()
	{
		String value = "garbage,1:2," + VarbitID.TOL_SYMBOL_OBJ1 + ":" + ItemID.COINS
			+ "," + VarbitID.TOL_SYMBOL_OBJ3 + ":" + ItemID.FEATHER
			+ "," + VarbitID.TOL_SYMBOL_OBJ4 + ":" + ItemID.FEATHER;
		assertEquals(ImmutableMap.of(VarbitID.TOL_SYMBOL_OBJ3, ItemID.FEATHER), TowerOfLifePlugin.parseMapping(value));
		assertTrue(TowerOfLifePlugin.parseMapping(null).isEmpty());
	}

	@Test
	public void parseItemsKeepsOnlyAltarItems()
	{
		assertEquals(ImmutableSet.of(ItemID.FEATHER, ItemID.RAW_LOBSTER),
			TowerOfLifePlugin.parseItems(ItemID.FEATHER + ", x," + ItemID.COINS + "," + ItemID.RAW_LOBSTER));
	}

	@Test
	public void itemIdForNameMatchesGameNames()
	{
		assertEquals(ItemID.RAW_LOBSTER, Altar.itemIdForName("raw lobster"));
		assertEquals(ItemID.RED_SPIDERS_EGGS, Altar.itemIdForName("Red spiders' eggs"));
		assertEquals(ItemID.RED_SPIDERS_EGGS, Altar.itemIdForName("red spiders eggs"));
		assertEquals(ItemID._100_JUBBLY_MEAT_RAW, Altar.itemIdForName(" Raw jubbly "));
		assertEquals(-1, Altar.itemIdForName("Coins"));
	}

	@Test
	public void placedMessageExtractsItemName()
	{
		java.util.regex.Matcher m = TowerOfLifePlugin.PLACED_MESSAGE.matcher("You place the raw lobster on the altar.");
		assertTrue(m.find());
		assertEquals("raw lobster", m.group(1));

		m = TowerOfLifePlugin.PLACED_MESSAGE.matcher("You already placed the Feather on the altar!");
		assertTrue(m.find());
		assertEquals("Feather", m.group(1));
	}

	@Test
	public void parseInspectReadsPlacedItems()
	{
		assertEquals(ImmutableSet.of(ItemID.EYE_OF_NEWT),
			TowerOfLifePlugin.parseInspect(Altar.NEWTROOST, "There is an eye of newt placed on the altar."));
		assertEquals(ImmutableSet.of(ItemID.RED_SPIDERS_EGGS, ItemID.RAW_SARDINE),
			TowerOfLifePlugin.parseInspect(Altar.SPIDINE, "There is some red spiders' eggs and a raw sardine<br>placed on the altar."));
		assertEquals(ImmutableSet.of(ItemID.RED_SPIDERS_EGGS, ItemID.RAW_SARDINE),
			TowerOfLifePlugin.parseInspect(Altar.SPIDINE, "You have all the materials necessary to activate this<br>altar!"));
		assertEquals(ImmutableSet.of(),
			TowerOfLifePlugin.parseInspect(Altar.SPIDINE, "You see some text scrolled above the altar on a symbol:<br>Red spider's eggs and a sardine raw..."));
		assertNull(TowerOfLifePlugin.parseInspect(Altar.NEWTROOST, "Click here to continue"));
		assertNull(TowerOfLifePlugin.parseInspect(Altar.NEWTROOST, null));
	}

	@Test
	public void knownVarbitsWinOverConflictingLearnedOnes()
	{
		Map<Integer, Integer> merged = TowerOfLifePlugin.withKnownVarbits(ImmutableMap.of(
			VarbitID.TOL_SYMBOL_OBJ7, ItemID.FEATHER,
			VarbitID.TOL_SYMBOL_OBJ1, ItemID.RAW_SARDINE,
			VarbitID.TOL_SYMBOL_OBJ2, ItemID.EYE_OF_NEWT));
		assertEquals(ImmutableMap.of(
			VarbitID.TOL_SYMBOL_OBJ7, ItemID.RED_SPIDERS_EGGS,
			VarbitID.TOL_SYMBOL_OBJ8, ItemID.RAW_SARDINE,
			VarbitID.TOL_SYMBOL_OBJ9, ItemID.RAW_SWORDFISH,
			VarbitID.TOL_SYMBOL_OBJ2, ItemID.EYE_OF_NEWT), merged);
	}
}
