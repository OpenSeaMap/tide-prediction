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

import ahdt.std.AHNullable;

/**
 *     CurrentBearing:  Store and print <int>° or <int>° true, or null.
// The redundancy between CurrentBearing and Angle is illusory.  XTide
// never needs to do anything with current directions except store
// them and print them in degrees.  They can't even be floats (libtcd
// ships ints).  However, they can be null.

 * @author chas
 */
public class CurrentBearing extends AHNullable
{

  // Units must be degrees.  Value must be between 0 and 359.  Set the
  // bool if they are "degrees true."

  // AFAIK the only alternative is "degrees relative," which would be
  // useless, but, whatever.

	private int degrees;
	private boolean degreesTrue;

	public CurrentBearing()
	{
		degrees = 0;
		degreesTrue = true;
	}

  // Print value in degrees with a degree sign and possibly a "true"
  // qualifier attached. This does not have fixed field width. It is an
  // error to attempt to print a null CurrentBearing.
  // This function always uses the Latin-1 degree sign.  If and when
  // necessary, it is replaced with the UTF-8 or VT100 sequence by the
  // caller.
	// ???? really Latin-1 or in fact utf-8
	public String print()
	{
		assert ( !isNull());
		String textOut = String.format(AHTideBaseStr.getString("CurrentBearing.0"), degrees); //$NON-NLS-1$
		if (degreesTrue)
		{
			textOut += AHTideBaseStr.getString("CurrentBearing.1"); //$NON-NLS-1$
		}
		return textOut;
	}

	public CurrentBearing(int degrees, boolean degreesTrue)
	{
		this.degrees = degrees;
		this.degreesTrue = degreesTrue;
	}

	public int getDegrees()
	{
		return degrees;
	}

	public void setDegrees(int degrees)
	{
		this.degrees = degrees;
	}

	public boolean isDegreesTrue()
	{
		return degreesTrue;
	}

	public void setDegreesTrue(boolean degreesTrue)
	{
		this.degreesTrue = degreesTrue;
	}

}
