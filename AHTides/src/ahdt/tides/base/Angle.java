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

import ahdt.tides.base.AHTUnits.AHTideAngleUnits;

/**
 * 
 * @author chas
 */
public class Angle
{

	private double radians;

	public Angle()
	{
		radians = 0.0;
	}

	public Angle(AHTideAngleUnits radians2, double value)
	{
		radians = (radians2 == AHTideAngleUnits.DEGREES ? Math.toRadians(value) : value);
	}

	public Angle add(double d)
	{
		return new Angle(AHTideAngleUnits.RADIANS, radians + d);
	}

	public Angle add(Angle a)
	{
		return new Angle(AHTideAngleUnits.RADIANS, this.radians + a.asRadians());
	}

	public void plusEquals(Angle a)
	{
		this.radians += a.asRadians();
	}

	public double asRadians()
	{
		return radians;
	}

	public double asDegrees()
	{
		return Math.toDegrees(radians);
	}

	public void minusEquals(Angle a)
	{
		radians -= a.asRadians();
	}
}
