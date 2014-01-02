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
import ahdt.tides.base.AHTUnits.AHTidePredictionUnits;

/**
 * 
 * @author chas
 */
public class NullablePredictionValue extends AHNullable
{
	private PredictionValue pv;

	public NullablePredictionValue()
	{
		this.pv = new PredictionValue();
	}

	public NullablePredictionValue(PredictionValue value)
	{
		this.pv = value;
		makeNull(false);
	}

	public NullablePredictionValue(AHTidePredictionUnits units, double value)
	{
		pv.setUnits(units);
		pv.setValue(value);
		makeNull(false);
	}

	public void setValue(double value)
	{
		makeNull(false);
		pv.setValue(value);
	}

	public void setUnits(AHTidePredictionUnits units)
	{
		pv.setUnits(units);
	}

	public String printnp()
	{
		return pv.printnp();
	}

	public String print()
	{
		return pv.print();
	}

	public PredictionValue plus(PredictionValue addend)
	{
		assert ( !isNull());
		return pv.plus(addend);
	}

	public PredictionValue minus(PredictionValue subtrahend)
	{
		assert ( !isNull());
		assert ( !subtrahend.isNull());
		return pv.minus(new PredictionValue(subtrahend));
	}

	public double getValue()
	{
		assert ( !isNull());
		return pv.getValue();
	}

	public AHTidePredictionUnits getUnits()
	{
		assert ( !isNull());
		return pv.getUnits();
	}

	public void divideBy(double divisor)
	{
		assert ( !isNull());
		pv.divideEquals(divisor);
	}

	public void convertAndAdd(PredictionValue addend)
	{
		assert ( !isNull());
		pv.convertAndAdd(addend);
	}

	public PredictionValue asPredictionValue()
	{
		assert ( !isNull());
		return pv;
	}

	public void multiply(double value)
	{
		assert ( !isNull());
		pv.timesEquals(value);
	}
}
