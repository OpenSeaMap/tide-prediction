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


/**
 * 
 * @author chas
 */
public interface StationRefInt
{

	void dump();

	Coordinates getCoordinates();

	String getHarmonicsFileName();

	String getName();

	int getRecordNumber();

	String getTimeZone();

	boolean isIsReferenceStation();

	RefStation load() throws FileNotFoundException;

	void setCoordinates(Coordinates coordinates);

	void setHarmonicsFileName(String harmonicsFileName);

	void setIsReferenceStation(boolean isReferenceStation);

	void setName(String name);

	void setRecordNumber(int recordNumber);

	void setTimeZone(String timeZone);

}
