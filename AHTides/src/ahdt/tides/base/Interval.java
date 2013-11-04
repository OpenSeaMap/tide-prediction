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

import java.util.Scanner;

import ahdt.tides.base.AHTUnits.AHTideAngleUnits;

/**
 * 
 * @author chas
 */
public class Interval
{

	private int seconds;

	public Interval()
	{

	}

	public Interval(Interval i)
	{
		this.seconds = i.getSeconds();
	}

	public Interval(int s)
	{
		this.seconds = s;
	}

	public Interval(long s)
	{
		this.seconds = (int) s;
	}

	public Interval(double s)
	{
		this.seconds = (int) s;
	}

	public Interval(NullableInterval interval)
	{
		seconds = interval.getInterval().getSeconds();
	}

	public Interval(String meridian)
	{
		int h, m;
		char s = '+';
		String mer = meridian;
		if (mer.startsWith(AHTideBaseStr.getString("Interval.0")) || mer.startsWith(AHTideBaseStr.getString("Interval.1"))) //$NON-NLS-1$ //$NON-NLS-2$
		{
			s = mer.charAt(0);
			mer = mer.substring(1);
		}
		Scanner hhmm = new Scanner(mer);
		hhmm.useDelimiter(AHTideBaseStr.getString("Interval.2")); //$NON-NLS-1$
		h = hhmm.nextInt();
		if (s == '-')
			h = -h;
		m = hhmm.nextInt();
		if (h < 0 || s == '-')
			m = -m;
		seconds = h * Global.HOURSECONDS + m * 60;
		hhmm.close();
	}

	public int getSeconds()
	{
		return seconds;
	}

	// public Angle multiply(Speed other) {
	//
	// }

	public Interval add(Interval other)
	{
		return new Interval(this.seconds + other.getSeconds());

	}

	public Interval subtract(Interval other)
	{
		return new Interval(this.seconds - other.getSeconds());
	}

	public double divide(Interval other)
	{
		return (double) this.seconds / (double) other.getSeconds();
	}

	public Interval divide(int other)
	{
		return new Interval(this.seconds / other);
	}

	public Interval multiply(int other)
	{
		return new Interval(this.seconds * other);
	}

	public Interval multiply(Interval other)
	{
		return new Interval(this.seconds * other.getSeconds());
	}

	public Interval multiply(double other)
	{
		return new Interval((int) (this.seconds * other));
	}

	public Angle multiply(Speed s)
	{
		return new Angle(AHTideAngleUnits.RADIANS, this.seconds * s.getRadiansPerSecond());
	}

	public void abs()
	{
		seconds = Math.abs(seconds);
	}

	public static Interval abs(Interval interval)
	{
		return new Interval(Math.abs(interval.getSeconds()));
	}

	public Interval negative()
	{
		return new Interval( -this.seconds);
	}

	public boolean lt(Interval other)
	{
		return this.seconds < other.getSeconds();

	}

	public boolean gt(Interval other)
	{
		return this.seconds > other.getSeconds();
	}

	public boolean lte(Interval other)
	{
		return this.seconds <= other.getSeconds();
	}

	public boolean gte(Interval other)
	{
		return this.seconds >= other.getSeconds();
	}

	public boolean equals(Interval other)
	{
		return this.seconds == other.getSeconds();
	}

	public boolean notEquals(Interval other)
	{
		return this.seconds != other.getSeconds();
	}

	public void timesEquals(double other)
	{
		this.seconds *= other;
	}
}
