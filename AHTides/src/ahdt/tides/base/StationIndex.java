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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import ahdt.tides.tcd.TideDBException;

/**
 * 
 * @author chas
 */
/**
 * @author humbach
 *
 */
public class StationIndex
{

	private transient Logger logger = Logger.getLogger(this.getClass().getName());
	private List<StationRef> stations = new LinkedList<>();

	public enum SortKey {
		NAME, LAT, LONG
	};

	public enum QueryType {
		PERCENT_EQUALS, CONTAINS
	};

	// Import refs for all stations in the specified harmonics file.
	public void addHarmonicsFile(String fileName) throws FileNotFoundException, TideDBException, IOException
	{
		HarmonicsFile h = HarmonicsFile.getInstance(fileName);
		// for (StationRef sr : h.getStationList())
		// {
		// stations.add(sr);
		// logger.log(java.util.logging.Level.FINE, "Adding station " + sr.getName());
		// }
	}

	public List<StationRef> getStations()
	{
		return stations;
	}

	public void sort()
	{
		sort(SortKey.NAME);
	}

	public void sort(SortKey key)
	{
		throw new UnsupportedOperationException(AHTideBaseStr.getString("StationIndex.0")); //$NON-NLS-1$
	}

	/**
	// QueryType refers to the applicable Dstr operation, %= or
	// contains. Selected station refs are added to index (any refs
	// already in there are unchanged).
	 * 
	 * @param pattern
	 * @param index
	 * @param queryType
	 */
	public void query(String pattern, StationIndex index, QueryType queryType)
	{
		throw new UnsupportedOperationException(AHTideBaseStr.getString("StationIndex.1")); //$NON-NLS-1$
	}

	public void print()
	{
		throw new UnsupportedOperationException(AHTideBaseStr.getString("StationIndex.2")); //$NON-NLS-1$
	}

	/**
	 *  Construct array of character strings used by xxLocationList.
	 * @param startAt
	 * @param maxLength
	 * @return
	 */
	public String[] makeStringList(int startAt, int maxLength)
	{
		throw new UnsupportedOperationException(AHTideBaseStr.getString("StationIndex.3")); //$NON-NLS-1$
	}

	/**
	 * Return identifiers for imported harmonics files in HTML format.
	 */
	public String hFileIds()
	{
		throw new UnsupportedOperationException(AHTideBaseStr.getString("StationIndex.4")); //$NON-NLS-1$
	}

	/** 
	 * Find a station by %= name, with UTF-8 / Latin-1 conversion as needed. Return NULL if not found.
	 * 
	 * @param name
	 */
	public StationRef getStationRefByName(String name)
	{
		// TODO: non UTF-8 handling
		return getStationRefByLatinName(name);
	}

	/**
	 *  Return longitude with the most stations.
	 * @return
	 */
	public double bestCenterLongitude()
	{
		throw new UnsupportedOperationException(AHTideBaseStr.getString("StationIndex.5")); //$NON-NLS-1$
	}

	/**
	// station refs. This method sets those fields in all station refs
	// in the current index. It should be called on the root
	// StationIndex after all harmonics files have been indexed.
	 * 
	 */
	public void setRootStationIndexIndices()
	{
		throw new UnsupportedOperationException(AHTideBaseStr.getString("StationIndex.6")); //$NON-NLS-1$
	}

	protected StationRef getStationRefByLatinName(String name)
	{
		for (StationRef sr : stations)
		{
			if (sr.getName().toLowerCase().startsWith(name.toLowerCase()))
			{
				return sr;
			}
		}
		return null;
	}
}
