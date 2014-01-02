/*    
    Copyright (C) 1997  David Flater.
    Java port Copyright (C) 2011 Chas Douglass

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

//package net.floogle.jTide;
package ahdt.tides.base;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * 
 * @author chas
 */
public class TideEventsOrganizer
{
	public final static Interval EVENT_SAFETY_MARGIN = new Interval(60);

	private SortedMap<AHTimestamp, TideEvent> map = new TreeMap<>();

	// Add a TideEvent, making an effort to suppress duplicates. An
	// event is considered a duplicate if the difference in times is
	// less than Global::eventSafetyMargin and the event type is the
	// same. This heuristic is almost always correct, and when it is
	// wrong, the affected events are probably anomalous to begin with.

	// The test for duplicates does not satisfy the mathematical
	// definition of an equivalence relation (it is not transitive).
	// Consequently, the duplicate elimination cannot be delegated to
	// the STL. Also, inserting the same set of events in a different
	// order can get different results.

	public SortedMap<AHTimestamp, TideEvent> getMap()
	{
		return map;
	}

	/**
	 * Add a new 'event' to the organizer.
	 * Here it is checked if there is already an event within the bounds of two EVENT_SAFETY_MARGIN around the event in the organizer.
	 *  
	 * @param event
	 */
	public void add(TideEvent event)
	{
		AHTimestamp ts = event.getTime();
		AHTimestamp lowTs = new AHTimestamp(ts);
		lowTs.minusEquals(EVENT_SAFETY_MARGIN);
		AHTimestamp highTs = new AHTimestamp(ts);
		highTs.plusEquals(EVENT_SAFETY_MARGIN);
		SortedMap<AHTimestamp, TideEvent> sub = map.subMap(lowTs, highTs);
		if (sub.size() == 0)
			map.put(ts, event);
	}

	public int size()
	{
		return map.size();
	}

	public TideEvent remove(AHTimestamp key)
	{
		return map.remove(key);
	}

	public TideEvent put(AHTimestamp key, TideEvent value)
	{
		return map.put(key, value);
	}

	public TideEvent get(AHTimestamp key)
	{
		return map.get(key);
	}

	public Collection<TideEvent> values()
	{
		return map.values();
	}

	public SortedMap<AHTimestamp, TideEvent> tailMap(AHTimestamp fromKey)
	{
		return map.tailMap(fromKey);
	}

	public SortedMap<AHTimestamp, TideEvent> subMap(AHTimestamp fromKey, AHTimestamp toKey)
	{
		return map.subMap(fromKey, toKey);
	}

	public AHTimestamp lastKey()
	{
		return map.lastKey();
	}

	public Set<AHTimestamp> keySet()
	{
		return map.keySet();
	}

	public AHTimestamp firstKey()
	{
		return map.firstKey();
	}

	public Set<Entry<AHTimestamp, TideEvent>> entrySet()
	{
		return map.entrySet();
	}

	AHTimestamp getupperbound(AHTimestamp predictTime)
	{
		for (AHTimestamp t : map.keySet())
		{
			if (t.gt(predictTime))
				return t;
		}
		return map.lastKey();
	}

	AHTimestamp getLowerBound(AHTimestamp predictTime)
	{
		for (AHTimestamp t : map.keySet())
		{
			if (t.gte(predictTime))
				return t;
		}
		// LinkedList<Timestamp> reversed = new LinkedList<Timestamp>(keySet());
		// Collections.reverse(reversed);
		// // Iterator<Entry<Timestamp, TideEvent>> it = map.entrySet().iterator();
		// for (Timestamp t : reversed)
		// if (t.lt(predictTime))
		// return t;
		return predictTime;
	}

	AHTimestamp getPrevious(AHTimestamp left)
	{
		LinkedList<AHTimestamp> reversed = new LinkedList<>(keySet());
		Collections.reverse(reversed);
		for (AHTimestamp t : reversed)
		{
			if (left.gt(t))
				return t;
		}
		return left;
	}

	boolean isEmpty()
	{
		return map.isEmpty();
	}

}
