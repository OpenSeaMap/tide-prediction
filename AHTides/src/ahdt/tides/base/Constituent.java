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

/**
 * 
 * @author chas
 */
public class Constituent
{

	private transient Logger logger = Logger.getLogger(this.getClass().getName());

	private Speed speed;
	private Amplitude amplitude;
	private Angle phase;
	private List<Angle> args = new LinkedList<>();
	private List<Double> nods = new LinkedList<>();
	private Year firstValidYear;
	private Year lastValidYear;

	public Constituent(double speedDegreesPerHour, int startYear, int numberOfYears, List<Double> argsDegrees, List<Double> nodes, Amplitude amplitude,
			double phaseDegrees)
	{
		this.speed = new Speed(speedDegreesPerHour);
		this.amplitude = amplitude;
		phase = new Angle(AHTideAngleUnits.DEGREES, -phaseDegrees);
		this.firstValidYear = new Year(startYear);
		this.lastValidYear = new Year(startYear + numberOfYears - 1);
		assert (lastValidYear.gte(firstValidYear));
		for (int looper = 0; looper < numberOfYears; ++looper)
		{
			args.add(new Angle(AHTideAngleUnits.DEGREES, argsDegrees.get(looper)));
			nods.add(nodes.get(looper));
			// logger.debug("constituent: " + argsDegrees.get(looper) + ":" +
			// nodes.get(looper));
		}
	}

	void dump()
	{
		System.out.println(AHTideBaseStr.getString("Constituent.0") + speed.getRadiansPerSecond()); //$NON-NLS-1$
		System.out.println(AHTideBaseStr.getString("Constituent.1") + amplitude.getValue() + AHTideBaseStr.getString("Constituent.2") + amplitude.getUnits()); //$NON-NLS-1$ //$NON-NLS-2$
		System.out.println(AHTideBaseStr.getString("Constituent.3") + phase.asDegrees()); //$NON-NLS-1$
		System.out.println(AHTideBaseStr.getString("Constituent.4") + firstValidYear.getYear()); //$NON-NLS-1$
		System.out.println(AHTideBaseStr.getString("Constituent.5") + lastValidYear.getYear()); //$NON-NLS-1$
	}

	public Angle arg(Year year)
	{
		checkValid(year);
		return args.get(year.getYear() - firstValidYear.getYear());
	}

	public Year firstValidYear()
	{
		return new Year(firstValidYear.getYear());
	}

	public Year lastValidYear()
	{
		return new Year(lastValidYear.getYear());
	}

	public void checkValid(Year year)
	{
		if (year.lt(firstValidYear) || year.gt(lastValidYear))
		{
			String details = AHTideBaseStr.getString("Constituent.6"); //$NON-NLS-1$
			details += firstValidYear.getYear();
			details += AHTideBaseStr.getString("Constituent.7"); //$NON-NLS-1$
			details += lastValidYear.getYear();
			details += AHTideBaseStr.getString("AHTides.NewLine"); //$NON-NLS-1$
			details += AHTideBaseStr.getString("Constituent.9"); //$NON-NLS-1$
			details += year.getYear();
			details += AHTideBaseStr.getString("Constituent.10"); //$NON-NLS-1$
			throw new RuntimeException(details);
		}
	}

	public Amplitude getAmplitude()
	{
		return new Amplitude(amplitude);
	}

	public void setAmplitude(Amplitude amplitude)
	{
		this.amplitude = amplitude;
	}

	@Deprecated
	public List<Angle> getArgs()
	{
		return args;
	}

	public Angle getArg(Year year)
	{
		checkValid(year);
		return args.get(year.getYear() - firstValidYear.getYear());
	}

	@Deprecated
	public void setArgs(List<Angle> args)
	{
		this.args = args;
	}

	public Year getFirstValidYear()
	{
		return firstValidYear;
	}

	public void setFirstValidYear(Year firstValidYear)
	{
		this.firstValidYear = firstValidYear;
	}

	public Year getLastValidYear()
	{
		return lastValidYear;
	}

	public void setLastValidYear(Year lastValidYear)
	{
		this.lastValidYear = lastValidYear;
	}

	@Deprecated
	public List<Double> getNods()
	{
		return nods;
	}

	public double getNode(Year year)
	{
		checkValid(year);
		return nods.get(year.getYear() - firstValidYear.getYear());
	}

	@Deprecated
	public void setNods(List<Double> nods)
	{
		this.nods = nods;
	}

	public Angle getPhase()
	{
		return phase;
	}

	public void setPhase(Angle phase)
	{
		this.phase = phase;
	}

	public Speed getSpeed()
	{
		return new Speed(speed);
	}

	public void setSpeed(Speed speed)
	{
		this.speed = speed;
	}

}
