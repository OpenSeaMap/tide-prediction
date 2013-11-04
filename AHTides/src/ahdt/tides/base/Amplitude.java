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

import ahdt.tides.base.AHTUnits.AHTidePredictionUnits;

/**
 * 
 * 
 * The relationship between Amplitude and PredictionValue is completely
 * analogous to the famous (or infamous) Square and Rectangle example that
 * everybody gets wrong.
 * 
 * Amplitude is not a subclass of PredictionValue. Here's the proof:
 * 
 * void Munge (PredictionValue &foo) { foo *= -2.0; }
 * 
 * Amplitude bar (meters, 3.0); Munge (bar);
 * 
 * Either the attempt to munge bar must fail, in which case substitutability has
 * been violated, or bar must morph into a PredictionValue, which is impossible
 * in C++.
 * 
 * If you take away mutability from both Amplitude and PredictionValue, then the
 * subclassing is valid, but I want my Amplitudes and PredictionValues to be
 * mutable. Consequently, an Amplitude is not a PredictionValue, but an implicit
 * conversion from Amplitude to PredictionValue is available.
 * 
 * @author chas
 */
public class Amplitude
{

	private PredictionValue pv;

	public Amplitude()
	{
		pv = new PredictionValue();
	}

	public Amplitude(Amplitude a)
	{
		this.pv = new PredictionValue(a.getUnits(), a.getValue());
	}

	// It is an error if value is less than zero.
	public Amplitude(AHTidePredictionUnits units, double value)
	{
		pv = new PredictionValue(units, value);
		assert (value >= 0.0);
	}

	@Deprecated
	public double val()
	{
		return pv.getValue();
	}

	public PredictionValue getPv()
	{
		return pv;
	}

	public double getValue()
	{
		return pv.getValue();
	}

	public AHTidePredictionUnits getUnits()
	{
		return pv.getUnits();
	}

	public void setUnits(AHTidePredictionUnits units)
	{
		pv.setUnits(units);
	}

	public boolean lt(Amplitude amplitude)
	{
		return this.pv.getValue() < amplitude.getValue();
	}

	public void timesEquals(double levelMultiply)
	{
		assert (levelMultiply >= 0.0);
		pv.setValue(pv.getValue() * levelMultiply);
	}

	public void plusEquals(Amplitude val)
	{
		pv.plusEquals(val.getPv());
	}

	public Amplitude times(double d)
	{
		return new Amplitude(pv.getUnits(), pv.getValue() * d);
	}

	boolean gt(Amplitude amplitude)
	{
		return this.pv.gt(amplitude.getPv());
	}
}
