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
 * Constants used generally when predicting tide data.
 * Currently this includes format of the output and mode of the prediction
 * 
 * @author humbach
 *
 */
public class Constants
{
	/**
	 * 20130220 - yet only PLAIN implemented.
	 * The exact interpretation of the mode is made in Station.
	 * 
	 * @author humbach
	 * 
	 * PLAIN - the tide events in a human readable text format
	 * RAW - the tide data (about 576/day, i.e. 2.5 minutes) in a (human readable) text format
	 *
	 */
	public enum Mode {
		// original modes used in xtide
		//		ABOUT('a'), BANNER('b'), CALENDAR('c'), ALT_CALENDAR('C'), GRAPH('g'), CLOCK('k'), LIST('l'), MEDIUM_RARE('m'), PLAIN('p'), RAW('r'), STATS('s');
		PLAIN(1), RAW(2);
		private final int value;

		Mode(int value)	{this.value = value;}
	}

	/**
	 * 20131231 - yet only CSV and TEXT implemented. The output formats are still to be decided, 
	 * because this implementation is intended for use with OpenSeaMap server.
	 * 
	 * @author humbach
	 * 
	 * For the time being TEXT de facto is CSV (or more precise SSV, semicolon separated)
	 *
	 */
	public enum Format {
		TEXT(1), CSV(2);
		private final int value;

		Format(int value)	{this.value = value;}
	}
}
