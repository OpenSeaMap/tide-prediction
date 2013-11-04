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

/**
 * 
 * @author chas
 */
public class Year
{
	private int year;

	public Year(Year year)
	{
		this.year = year.getYear();
	}

	public Year(int year)
	{
		this.year = year;
	}

	public Year incYear()
	{
		return new Year(year + 1);
	}

	public int getYear()
	{
		return year;
	}

	public void setYear(int year)
	{
		this.year = year;
	}

	public boolean lte(Year tempyear)
	{
		return year <= tempyear.getYear();
	}

	public boolean gte(Year tempYear)
	{
		return year >= tempYear.getYear();
	}

	public boolean lt(Year y)
	{
		return year < y.getYear();
	}

	public boolean gt(Year y)
	{
		return year > y.getYear();
	}

	public void plusplus()
	{
		this.year++ ;
	}

	public Year plus(int inc)
	{
		return new Year(this.getYear() + inc);
	}

	@Override
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
		final Year other = (Year) obj;
		if (this.year != other.year)
		{
			return false;
		}
		return true;
	}

}
