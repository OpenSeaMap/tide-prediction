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
public class Constants
{
	/**
	 * 20130220 - yet only PLAIN implemented
	 * 
	 * @author humbach
	 *
	 */
	public enum Mode {

		ABOUT('a'), BANNER('b'), CALENDAR('c'), ALT_CALENDAR('C'), GRAPH('g'), CLOCK('k'), LIST('l'), MEDIUM_RARE('m'), PLAIN('p'), RAW('r'), STATS('s');
		private final char value;

		Mode(char value)
		{
			this.value = value;
		}
	}

	/**
	 * 20130220 - yet only CSV and TEXT implemented
	 * 
	 * @author humbach
	 *
	 */
	public enum Format {

		CSV('c'), HTML('h'), ICALENDAR('i'), LATEX('l'), PNG('p'), TEXT('t');
		private final char value;

		Format(char value)
		{
			this.value = value;
		}
	}
}
