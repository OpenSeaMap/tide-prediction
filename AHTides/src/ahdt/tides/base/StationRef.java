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
import java.util.logging.Level;
import java.util.logging.Logger;

import ahdt.tides.tcd.TideDBException;

/**
 * 
 * @author chas
 */

public class StationRef implements StationRefInt
{

	private String harmonicsFileName;
	private int recordNumber;
	private String name;
	private Coordinates coordinates;
	private String timeZone;
	private boolean isReferenceStation;
	private RefStation station;

	public StationRef(String harmonicsFileName, int recordNumber, String name, Coordinates coordinates, String timeZone, boolean isReferenceStation)
	{
		this.harmonicsFileName = harmonicsFileName;
		this.recordNumber = recordNumber;
		this.name = name;
		this.coordinates = coordinates;
		this.timeZone = timeZone;
		this.isReferenceStation = isReferenceStation;
	}

	public RefStation load() throws FileNotFoundException
	{
		try
		{
			if (station == null)
			{
				HarmonicsFile f = HarmonicsFile.getInstance(harmonicsFileName);
				station = f.getStation(this);
			}
			return station;
		}
		catch (final TideDBException | IOException ex)
		{
			Logger.getLogger(StationRef.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	public void dump()
	{
		System.out.println(AHTideBaseStr.getString("StationRef.0")); //$NON-NLS-1$
		System.out.println(AHTideBaseStr.getString("StationRef.1") + harmonicsFileName); //$NON-NLS-1$
		System.out.println(AHTideBaseStr.getString("StationRef.2") + recordNumber); //$NON-NLS-1$
		System.out.println(AHTideBaseStr.getString("StationRef.3") + name); //$NON-NLS-1$
		System.out.println(AHTideBaseStr.getString("StationRef.4") + coordinates.print()); //$NON-NLS-1$
		System.out.println(AHTideBaseStr.getString("StationRef.5") + timeZone); //$NON-NLS-1$
		System.out.println(AHTideBaseStr.getString("StationRef.6") + isReferenceStation); //$NON-NLS-1$

	}

	public Coordinates getCoordinates()
	{
		return coordinates;
	}

	public void setCoordinates(Coordinates coordinates)
	{
		this.coordinates = coordinates;
	}

	public String getHarmonicsFileName()
	{
		return harmonicsFileName;
	}

	public void setHarmonicsFileName(String harmonicsFileName)
	{
		this.harmonicsFileName = harmonicsFileName;
	}

	public boolean isIsReferenceStation()
	{
		return isReferenceStation;
	}

	public void setIsReferenceStation(boolean isReferenceStation)
	{
		this.isReferenceStation = isReferenceStation;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public int getRecordNumber()
	{
		return recordNumber;
	}

	public void setRecordNumber(int recordNumber)
	{
		this.recordNumber = recordNumber;
	}

	public String getTimeZone()
	{
		return timeZone;
	}

	public void setTimeZone(String timeZone)
	{
		this.timeZone = timeZone;
	}

	public boolean equals(Object obj)
	{
		if (obj == null)
		{
			return false;
		}
		if (getClass() != obj.getClass())
		{
			return false;
		}
		final StationRef other = (StationRef) obj;
		if ( !this.harmonicsFileName.equals(other.harmonicsFileName))
		{
			return false;
		}
		if (this.recordNumber != other.recordNumber)
		{
			return false;
		}
		return true;
	}

	public int hashCode()
	{
		int hash = 3;
		hash = 43 * hash + this.harmonicsFileName.hashCode();
		hash = 43 * hash + this.recordNumber;
		return hash;
	}

}
