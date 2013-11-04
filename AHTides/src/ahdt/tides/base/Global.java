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
 * @author hd214c
 */
public class Global
{
	public final static int DAYSECONDS = 86400;
	public final static int HOURSECONDS = 60 * 60;
	public final static Interval EVENT_PRECISION = new Interval(15);
	public final static Interval ZERO_INTERVAL = new Interval(0);
	public final static Interval DAY = new Interval(DAYSECONDS);
	public final static Interval DEFAULT_PREDICT_INTERVAL = new Interval(345600);
	public final static Interval EVENT_SAFETY_MARGIN = new Interval(60);
	public final static char CSV_REPCHAR = '|';
}
