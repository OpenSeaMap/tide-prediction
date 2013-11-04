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
public class Defaults
{

	public static int LIBTCD_COMPATIBILITY_MAJOR_REV = 2;
	public static int LIBTCD_COMPATIBILITY_MINOR_REV = 2;

	public static int HEADER_SIZE = 4096;
	public static int NUMBER_OF_RECORDS = 0;
	public static int LEVEL_UNIT_TYPES = 5;
	public static int DIR_UNIT_TYPES = 3;
	public static int RESTRICTION_TYPES = 2;
	public static int RESTRICTION_BITS = 4;
	public static int TZFILES = 406;
	public static int TZFILE_BITS = 10;
	public static int COUNTRIES = 240;
	public static int COUNTRY_BITS = 9;
	public static int DATUM_TYPES = 61;
	public static int DATUM_BITS = 7;
	public static int LEGALESES = 1;
	public static int LEGALESE_BITS = 4;
	public static int SPEED_SCALE = 10000000;
	public static int EQUILIBRIUM_SCALE = 100;
	public static int NODE_SCALE = 10000;
	public static int AMPLITUDE_BITS = 19;
	public static int AMPLITUDE_SCALE = 10000;
	public static int EPOCH_BITS = 16;
	public static int EPOCH_SCALE = 100;
	public static int RECORD_TYPE_BITS = 4;
	public static int LATITUDE_BITS = 25;
	public static int LATITUDE_SCALE = 100000;
	public static int LONGITUDE_BITS = 26;
	public static int LONGITUDE_SCALE = 100000;
	public static int RECORD_SIZE_BITS = 16;
	public static int STATION_BITS = 18;
	public static int DATUM_OFFSET_BITS = 28;
	public static int DATUM_OFFSET_SCALE = 10000;
	public static int DATE_BITS = 27;
	public static int MONTHS_ON_STATION_BITS = 10;
	public static int CONFIDENCE_VALUE_BITS = 4;
	public static int NUMBER_OF_CONSTITUENTS_BITS = 8;
	public static int TIME_BITS = 13;
	public static int LEVEL_ADD_BITS = 17;
	public static int LEVEL_ADD_SCALE = 1000;
	public static int LEVEL_MULTIPLY_BITS = 16;
	public static int LEVEL_MULTIPLY_SCALE = 1000;
	public static int DIRECTION_BITS = 9;
	public static int CONSTITUENT_SIZE = 10;
	public static int LEVEL_UNIT_SIZE = 15;
	public static int DIR_UNIT_SIZE = 15;
	public static int RESTRICTION_SIZE = 30;
	public static int DATUM_SIZE = 70;
	public static int LEGALESE_SIZE = 70;
	public static int TZFILE_SIZE = 30;
	public static int COUNTRY_SIZE = 50;

	/* Stuff for inferring constituents (NAVO short duration tide stations). */

	public static int INFERRED_SEMI_DIURNAL_COUNT = 10;
	public static int INFERRED_DIURNAL_COUNT = 10;

	public static String[] inferredSemiDiurnal = new String[]
	{ AHTideBaseStr.getString("Defaults.0"), AHTideBaseStr.getString("Defaults.1"), AHTideBaseStr.getString("Defaults.2"), AHTideBaseStr.getString("Defaults.3"), AHTideBaseStr.getString("Defaults.4"), AHTideBaseStr.getString("Defaults.5"), AHTideBaseStr.getString("Defaults.6"), AHTideBaseStr.getString("Defaults.7"), AHTideBaseStr.getString("Defaults.8"), AHTideBaseStr.getString("Defaults.9") }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$

	public static String[] inferredDiurnal = new String[]
	{ AHTideBaseStr.getString("Defaults.10"), AHTideBaseStr.getString("Defaults.11"), AHTideBaseStr.getString("Defaults.12"), AHTideBaseStr.getString("Defaults.13"), AHTideBaseStr.getString("Defaults.14"), AHTideBaseStr.getString("Defaults.15"), AHTideBaseStr.getString("Defaults.16"), AHTideBaseStr.getString("Defaults.17"), AHTideBaseStr.getString("Defaults.18"), AHTideBaseStr.getString("Defaults.19") }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$

	public static float[] semiDiurnalCoeff = new float[]
	{ .1759f, .0341f, .0219f, .0235f, .0066f, .0248f, .0035f, .0251f, .1151f, .0064f };

	public static float[] diurnalCoeff = new float[]
	{ .0163f, .0209f, .0297f, .0142f, .0730f, .0097f, .1755f, .0103f, .0076f, .0042f };

	/* These represent M2 and O1. */

	public static float[] coeff = new float[]
	{ .9085f, .3771f };

}
