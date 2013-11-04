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

import java.awt.geom.Point2D;

import ahdt.std.AHNullable;

/**
 * 
 * @author chas
 */
public class Coordinates extends AHNullable
{

	private double latitude;
	private double longitude;

	public Coordinates()
	{
		makeNull();
	}

	public Coordinates(double latitude, double longitude)
	{
		makeNull(false);
		this.latitude = latitude;
		this.longitude = longitude;
		if (latitude < -90.0 || latitude > 90.0 || longitude < -180.0 || longitude > 180.0)
		{
			throw new RuntimeException(AHTideBaseStr.getString("Coordinates.BadCoord") + latitude + AHTideBaseStr.getString("Coordinates.LLSep") + longitude); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	public double getLatitude()
	{
		assert ( !isNull());
		return latitude;
	}

	public double getLongitude()
	{
		assert ( !isNull());
		return longitude;
	}

	public enum Pad {
		NO_PADDING, FIXED_WIDTH
	};

	public String print()
	{
		return print(Pad.NO_PADDING);
	}

	public String print(Pad pad)
	{
		return printLat() + AHTideBaseStr.getString("Coordinates.LLSep") + printLng(); //$NON-NLS-1$
	}

	public String printLat()
	{
		return String.format(AHTideBaseStr.getString("Coordinates.StdForm"), latitude); //$NON-NLS-1$
	}

	public String printLng()
	{
		return String.format(AHTideBaseStr.getString("Coordinates.StdForm"), longitude); //$NON-NLS-1$
	}

	public Point2D.Double asPoint()
	{
		return new Point2D.Double(longitude, latitude);
	}
}
