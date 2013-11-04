/*    
    Copyright (C) 2013 Alexej Humbach  

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
    
    The software is based on programs written by David Flater and Chas Douglass
 */

package ahdt.tides.base;

/**
 * 
 * @author AH
 */
public class AHTUnits
{

	// public enum PredictionUnits {FEET, METERS, KNOTS, KNOTS_SQUARED, ZULU};
	public enum AHTidePredictionUnits
	{

		FEET(AHTideBaseStr.getString("AHTUnits.0"), AHTideBaseStr.getString("AHTUnits.1")), METERS(AHTideBaseStr.getString("AHTUnits.2"), AHTideBaseStr.getString("AHTUnits.3")), KNOTS(AHTideBaseStr.getString("AHTUnits.4"), AHTideBaseStr.getString("AHTUnits.5")), KNOTS_SQUARED(AHTideBaseStr.getString("AHTUnits.6"), AHTideBaseStr.getString("AHTUnits.7")), ZULU(AHTideBaseStr.getString("AHTUnits.8"), AHTideBaseStr.getString("AHTUnits.9")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$

		private final String m_strLongName;
		private final String m_strShortName;

		AHTidePredictionUnits(String longName, String shortName)
		{
			m_strLongName = longName;
			m_strShortName = shortName;
		}

		/**
		 * @return the short name
		 */
		public String getShortName()
		{
			return m_strShortName;
		}

		/**
		 * @return the long name
		 */
		public String getLongName()
		{
			return m_strLongName;
		}
	}
/**
 * two units are allowed: RADIANS and DEGREES
 * @author humbach
 *
 */
	public enum AHTideAngleUnits
	{
		RADIANS, DEGREES
	};

	public static AHTidePredictionUnits parse(String unitsName)
	{
		for (AHTidePredictionUnits pu: AHTidePredictionUnits.values())
		{
			if (unitsName.equals(pu.getLongName()) || unitsName.equals(pu.getShortName()))
			{
				return pu;
			}
		}
		String details = AHTideBaseStr.getString("AHTUnits.10"); //$NON-NLS-1$
		details += unitsName;
		details += '.';
		throw new RuntimeException(details);
	}

	/**
	 * masks KNOTS_SQUARED as KNOTS
	 * 
	 * @param u
	 * @return KNOTS
	 */
	public static AHTidePredictionUnits flatten(AHTidePredictionUnits u)
	{
		assert (u != AHTidePredictionUnits.ZULU);
		if (u == AHTidePredictionUnits.KNOTS_SQUARED)
			u = AHTidePredictionUnits.KNOTS;
		return u;
	}
/** i
 * if the units are KNOTS or KNOTS_SQUARED it is a current
 * @param u
 * @return
 */
	public static boolean isCurrent(AHTidePredictionUnits u)
	{
		assert (!u.equals(AHTidePredictionUnits.ZULU));
		return (u.equals(AHTidePredictionUnits.KNOTS) || u.equals(AHTidePredictionUnits.KNOTS_SQUARED));
	}

	/**
 * if the units are KNOTS_SQUARED it is a hydraulic current
	 */
	public static boolean isHydraulicCurrent(AHTidePredictionUnits u)
	{
		assert (!u.equals(AHTidePredictionUnits.ZULU));
		return (u.equals(AHTidePredictionUnits.KNOTS_SQUARED));
	}
}
