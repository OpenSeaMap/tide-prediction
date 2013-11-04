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

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import ahdt.tides.base.AHTUnits.AHTideAngleUnits;
import ahdt.tides.base.AHTUnits.AHTidePredictionUnits;

/**
 * 
 * @author chas
 */
public class ConstituentSet
{

	private transient Logger logger = Logger.getLogger(this.getClass().getName());

	private List<Constituent> constituents = new LinkedList<>();
	private PredictionValue datum;

	// The following are what get accessed directly in tideDerivative.
	// Amplitudes are constituent amplitudes timesEquals node factors.
	// Phases are constituent phases plusEquals equilibrium arguments.

	// Optimization: Conversion of amplitudes to PredictionValue is
	// done in advance instead of on every reference inside of
	// tideDerivative. The conversion from Amplitude to PredictionValue
	// could not be inlined because of a circular dependency.
	private List<PredictionValue> amplitudes = new LinkedList<>();
	private List<Angle> phases = new LinkedList<>();
	// Maximum derivative supported by tideDerivative and family.
	private static int maxDeriv = 2;

	// TODO: remove unnecessary elements
	private Amplitude maxAmplitude;
	private Amplitude[] maxdt = new Amplitude[maxDeriv + 2];
	private Year currentYear;
	private AHTimestamp epoch;
	private AHTimestamp nextEpoch;
	private AHTidePredictionUnits preferredLengthUnits;

	// Null constituents should have been eliminated in HarmonicsFile.
	// On construction, constituents and getDatum are (permanently)
	// adjusted according to adjustments.
	public ConstituentSet(List<Constituent> constituents, PredictionValue datum, SimpleOffsets adjustments)
	{

		this.constituents = constituents;
		this.datum = datum;
		this.currentYear = new Year(2000);
		this.preferredLengthUnits = AHTidePredictionUnits.METERS;

		for (int i = 0; i < maxDeriv + 2; i++ )
			maxdt[i] = new Amplitude();

		int i;
		if ( !AHTUnits.isCurrent((datum.getUnits())))
			preferredLengthUnits = datum.getUnits();

		// Null constituents should have been eliminated in HarmonicsFile.

		// Apply adjustments.
		datum = datum.times(adjustments.getLevelMultiply());
		datum.convertAndAdd(adjustments.getLevelAdd());
		for (i = 0; i < constituents.size(); ++i)
		{
			constituents.get(i).getAmplitude().timesEquals(adjustments.getLevelMultiply());
			// To move tides one hour later, you need to turn BACK the phases.
			double prephase = constituents.get(i).getPhase().asRadians();
			// logger.log(java.util.logging.Level.FINE,
			// String.format("adjustments.timeadd = %d, contituents[i].speed = %f",
			// adjustments.getTimeAdd().getSeconds(),
			// constituents.get(i).getSpeed().getRadiansPerSecond()));
			constituents.get(i).getPhase().minusEquals(adjustments.getTimeAdd().multiply(constituents.get(i).getSpeed()));
			// logger.log(java.util.logging.Level.FINE,
			// String.format("prephase = %f, postphase = %f", prephase,
			// constituents.get(i).getPhase().asRadians()));
		}

		// Nasty loop to figure maxdt and getMaxAmplitude.
		for (int deriv = 0; deriv <= maxDeriv + 1; ++deriv)
		{
			for (Year tempyear = new Year(constituents.get(0).firstValidYear()); tempyear.lte(constituents.get(0).lastValidYear()); tempyear.plusplus())
			{
				Amplitude max = new Amplitude();
				for (i = 0; i < constituents.size(); ++i)
				{
					Amplitude temp = new Amplitude(constituents.get(i).getAmplitude()
							.times(constituents.get(i).getNode(tempyear) * Math.pow(constituents.get(i).getSpeed().getRadiansPerSecond(), (double) deriv)));
					max.plusEquals(temp);
				}
				if (max.gt(maxdt[deriv]))
					maxdt[deriv] = max;
			}
			if (deriv == 0)
				maxAmplitude = maxdt[deriv];
			maxdt[deriv].timesEquals(1.1); /* Add a little safety margin... */
		}
		if (AHTUnits.isHydraulicCurrent(maxAmplitude.getUnits()))
			maxAmplitude.setUnits(AHTUnits.flatten(maxAmplitude.getUnits()));
		assert (maxAmplitude.getValue() > 0.0);

		// Harmonics file range of years may exceed that of this platform.
		// Try valiantly to find a safe initial value.
		int b = constituents.get(0).getFirstValidYear().getYear();
		int e = constituents.get(0).getLastValidYear().getYear();
		if (b <= 2000 && e >= 2000)
			currentYear = new Year(2000);
		else if (b <= 1970 && e >= 1970)
			currentYear = new Year(1970);
		else if (b <= 2037 && e >= 2037)
			currentYear = new Year(2037);
		else
			currentYear = new Year((b + e) / 2);

		// amplitudes.resize(length);
		// phases.resize(length);
		changeYear(currentYear);
	}

	public void dump()
	{
		int i = 0;
		for (Constituent c : constituents)
		{
			System.out.println(AHTideBaseStr.getString("ConstituentSet.0") + i++ ); //$NON-NLS-1$
			c.dump();
		}
	}

	// Change preferred units of length.
	// Default is as specified by settings.
	// Attempts to set same units are tolerated without complaint.
	// Attempts to set velocity units are punished.
	public void setUnits(AHTidePredictionUnits units)
	{
		assert ( !AHTUnits.isCurrent(units));
		preferredLengthUnits = units;
	}

	// Tell me what units tideDerivative will return.
	public AHTidePredictionUnits getPredictUnits()
	{
		AHTidePredictionUnits temp = constituents.get(0).getAmplitude().getUnits();
		if (AHTUnits.isCurrent(temp))
			return temp;
		return preferredLengthUnits;
	}

	// These will never have a value of type KnotsSquared.
	public Amplitude getMaxAmplitude()
	{
		return prefer(maxAmplitude, preferredLengthUnits);
	}

	// TODO: Check for implementation AH 20131017
	public PredictionValue getDatum()
	{
		return prefer(datum, preferredLengthUnits);
	}

	// Calculate (deriv)th time derivative of the normalized tide (for
	// time in s). The result does not have the getDatum added in and will
	// not be converted from KnotsSquared.
	public PredictionValue tideDerivative(AHTimestamp predictTime, int deriv)
	{
		Year year = new Year(predictTime.getYear());
		if ( !year.equals(currentYear))
			changeYear(year);
		// TODO: Handle end of year blending
		Interval sinceEpoch = predictTime.minus(epoch);
		logger
				.log(
						java.util.logging.Level.FINE,
						AHTideBaseStr.getString("ConstituentSet.1") + predictTime.getSeconds() + AHTideBaseStr.getString("ConstituentSet.2") + epoch.getSeconds() + AHTideBaseStr.getString("ConstituentSet.3") + sinceEpoch.getSeconds()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		return prefer(tideDerivative(sinceEpoch, deriv), preferredLengthUnits);
	}

	// Return the maximum that the absolute value of the (deriv)th
	// derivative of the tide can ever attain, plusEquals "a little safety
	// margin." tideDerivativeMax(0) == getMaxAmplitude() * 1.1
	public Amplitude tideDerivativeMax(int deriv)
	{
		assert (deriv <= maxDeriv + 1);
		return prefer(maxdt[deriv], preferredLengthUnits);
	}

	// Update amplitudes, phases, epoch, nextEpoch, and currentYear.
	public final void changeYear(Year newYear)
	{
		currentYear = newYear;
		// logger.log(java.util.logging.Level.FINE, "Changing year to " +
		// newYear.getYear());
		amplitudes = new LinkedList<>();
		phases = new LinkedList<>();
		for (int i = 0; i < constituents.size(); ++i)
		{
			// Apply node factor. (Implicit conversion to PredictionValue.)
			amplitudes.add(new PredictionValue(constituents.get(i).getAmplitude().times(constituents.get(i).getNode(currentYear))));

			// Apply equilibrium argument. Recall that phases have been pre-negated
			// per -k'.
			phases.add(constituents.get(i).getPhase().add(constituents.get(i).getArg(currentYear)));
		}

		epoch = new AHTimestamp(currentYear);
		nextEpoch = new AHTimestamp(currentYear.plus(1));

		// nextEpoch is allowed to fail, which allows us to get tides for
		// the first few days of 2038, but epoch we need.
		if (epoch.isNull())
			throw new RuntimeException(AHTideBaseStr.getString("ConstituentSet.4")); //$NON-NLS-1$
	}

	// Calculate (deriv)th time derivative of the normalized tide for
	// time in s since the beginning (UTC) of currentYear, WITHOUT
	// changing years or blending.

	// XTide spends more time in this method than anywhere else.
	// In XTide 2.8.3 and previous, the high-level data types (Speed,
	// Amplitude, Interval, etc.) were used to shuttle data around, but at
	// the last minute everything reverted to C arrays of doubles just to
	// make this loop run faster. The Great Cleanup of 2006 got rid of
	// that hypocrisy. Most use cases showed no noticeable impact, but
	// those that involved generating a really long series of predictions
	// (e.g., for stats mode or calendar mode) initially showed alarming
	// slowdowns of 300% and worse. Conversion of select methods and
	// functions to inlines, plusEquals the avoidance of one type conversion
	// that could not be inlined, shaved the performance hit to about 15%,
	// which is close enough to argue that the benefits of high-level data
	// types exceed the costs.

	public PredictionValue tideDerivative(Interval sinceEpoch, int deriv)
	{
		PredictionValue dt_tide = new PredictionValue();
		Angle tempd = new Angle(AHTideAngleUnits.RADIANS, Math.PI / 2.0 * deriv);
		// logger.log(java.util.logging.Level.FINE, "sinceEpoch = " +
		// sinceEpoch.getSeconds());
		for (int a = 0; a < constituents.size(); ++a)
		{
			PredictionValue term = new PredictionValue();
			Angle temp = tempd.add(constituents.get(a).getSpeed().getRadiansPerSecond() * sinceEpoch.getSeconds() + phases.get(a).asRadians());
			term = amplitudes.get(a).times(Math.cos(temp.asRadians()));
			logger.log(java.util.logging.Level.FINE,
					String.format(AHTideBaseStr.getString("ConstituentSet.5"), amplitudes.get(a).getValue(), constituents.get(a).getSpeed().getRadiansPerSecond(), //$NON-NLS-1$
							phases.get(a).asRadians()));
			String deb = AHTideBaseStr.getString("ConstituentSet.6"); //$NON-NLS-1$
			for (int b = deriv; b > 0; --b)
			{
				term = term.times(constituents.get(a).getSpeed().getRadiansPerSecond());
				deb += String.format(AHTideBaseStr.getString("ConstituentSet.7"), term.getValue()); //$NON-NLS-1$
			}
			logger.log(java.util.logging.Level.FINE, deb);
			dt_tide.plusEquals(term);
		}
		logger.log(java.util.logging.Level.FINE, AHTideBaseStr.getString("ConstituentSet.8") + dt_tide.getValue()); //$NON-NLS-1$
		return dt_tide;
	}

	// Called by tideDerivative(Timestamp) to blend tides near year ends.
	public PredictionValue blendTide(AHTimestamp predictTime, int deriv, Year firstYear, double blend)
	{
		throw new UnsupportedOperationException(AHTideBaseStr.getString("ConstituentSet.9")); //$NON-NLS-1$
	}

	// Convert to preferredLengthUnits if this conversion makes sense;
	// return value unchanged otherwise.

	private Amplitude prefer(Amplitude v, AHTidePredictionUnits preferredLengthUnits)
	{
		assert ( !AHTUnits.isCurrent(preferredLengthUnits));
		Amplitude vl = new Amplitude(v);
		if ( !AHTUnits.isCurrent(v.getUnits()) && v.getUnits() != preferredLengthUnits)
		{
			vl.setUnits(preferredLengthUnits);
		}
		return vl;
	}

	private PredictionValue prefer(PredictionValue v, AHTidePredictionUnits preferredLengthUnits)
	{
		assert ( !AHTUnits.isCurrent(preferredLengthUnits));
		PredictionValue pv = new PredictionValue(v);
		if ( !AHTUnits.isCurrent(v.getUnits()) && v.getUnits() != preferredLengthUnits)
			pv.setUnits(preferredLengthUnits);
		return pv;
	}
}
