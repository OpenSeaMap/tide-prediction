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

import java.util.Deque;
import java.util.logging.Logger;

import ahdt.tides.base.AHTUnits.AHTidePredictionUnits;
import ahdt.tides.tcd.TideRecord;

/**
 * 
 * @author chas
 */
/**
 * A Station is the object enabling calculations and predictions.
 * 
 * @author humbach
 *
 */
public class RefStation extends Station implements Cloneable
{
	private transient Logger logger = Logger.getLogger(this.getClass().getName());

	/**
	 * constructs a RefStation from a TideDB record
	 * 
	 * @param tRec is the TideRecord whose data are to be loaded into the station object
	 */
	public RefStation(TideRecord tRec)
	{
		super(tRec);
	}

	public void dump()
	{
		System.out.println(AHTideBaseStr.getString("RefStation.2") + getName());
		System.out.println(AHTideBaseStr.getString("RefStation.3") + getTimeZone());
		System.out.println(AHTideBaseStr.getString("RefStation.4") + getMinCurrentBearing().getDegrees());
		System.out.println(AHTideBaseStr.getString("RefStation.5") + getMaxCurrentBearing().getDegrees());
		System.out.println(AHTideBaseStr.getString("RefStation.6") + this.getName());
		System.out.println(AHTideBaseStr.getString("RefStation.7") + this.isIsCurrent());
		// stationRef.dump();
		m_tConst.dump();
		System.out.println(AHTideBaseStr.getString("RefStation.8"));
		String metaFormatString = AHTideBaseStr.getString("RefStation.9");
		for (MetaField mf : m_tMetadata)
		{
			System.out.printf(metaFormatString, mf.getName(), mf.getValue());
		}
	}

	protected boolean isSubordinateStation()
	{
		return false;
	}

}
