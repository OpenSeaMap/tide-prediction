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
 * PredictionValues have a distinct initial state called 0 Zulu. 0 Zulu can be
 * converted to any units, whether length or velocity. 0 Zulu can be added to
 * any other PredictionValue regardless of its units. 0 Zulu will adopt the
 * units of any PredictionValue added to it. 0 Zulu is comparable with any
 * PredictionValue regardless of its units.
 * 
 * @author chas
 */
public class PredictionValue extends AHNullable
{
	private double value;
	private AHTidePredictionUnits units;

	public PredictionValue()
	{
		this.makeNull(false);
		// Initialize to 0 Zulu.
		value = 0;
		units = AHTidePredictionUnits.ZULU;
	}

	public PredictionValue(PredictionValue pv)
	{
		// Error if null.
		if (pv.isNull())
		{
			throw new UnsupportedOperationException("Attempt to convert null value to PredictionValue");
		}
		this.makeNull(false);
		this.value = pv.getValue();
		this.units = pv.getUnits();
	}

	public PredictionValue(AHTidePredictionUnits units, double value)
	{
		this.makeNull(false);
		this.units = units;
		this.value = value;
		assert (value == 0.0 || units != AHTidePredictionUnits.ZULU);
	}

	public PredictionValue(Amplitude a)
	{
		this.makeNull(false);
		this.units = a.getUnits();
		this.value = a.getValue();
	}

	public boolean equals(PredictionValue pv)
	{
		if (this.units == pv.getUnits() && this.value == pv.getValue())
			return true;
		return false;
	}

	public boolean equals(double value)
	{
		return (this.value == value);
	}

	public PredictionValue times(double value)
	{
		PredictionValue temp = new PredictionValue(this.units, this.value);
		temp.setValue(temp.getValue() * value);
		return temp;
	}

	public PredictionValue times(Interval interval)
	{
		return new PredictionValue(this.units, this.value * interval.getSeconds());
	}

	public void timesEquals(double value)
	{
		this.value *= value;
	}

	public PredictionValue times(PredictionValue pv)
	{
		return new PredictionValue(this.units, this.value * pv.getValue());
	}

	public void negate()
	{
		this.value = -this.value;
	}

	public PredictionValue negative()
	{
		return new PredictionValue(this.units, -this.value);
	}

	public void divideEquals(double divisor)
	{
		this.value /= divisor;
	}

	public PredictionValue divide(double divisor)
	{
		return new PredictionValue(this.units, this.value / divisor);
	}

	public double divide(PredictionValue pv)
	{
		return this.value / pv.getValue();
	}

	public static PredictionValue abs(PredictionValue pv)
	{
		return new PredictionValue(pv.getUnits(), Math.abs(pv.getValue()));
	}

	public PredictionValue abs()
	{
		return new PredictionValue(this.getUnits(), Math.abs(this.value));
	}

	public boolean gt(PredictionValue pv)
	{
		harmonize(this, pv);
		return this.value > pv.getValue();
	}

	public boolean gte(PredictionValue pv)
	{
		harmonize(this, pv);
		return this.value >= pv.getValue();
	}

	public boolean lte(PredictionValue pv)
	{
		harmonize(this, pv);
		return this.value <= pv.getValue();
	}

	private void harmonize(PredictionValue a, PredictionValue b)
	{
		if (a.getUnits() != b.getUnits())
		{
			if (a.getUnits() == AHTidePredictionUnits.ZULU)
				a.setUnits(b.getUnits());
			else
				b.setUnits(a.getUnits());
		}
	}

	// With the exception of the 0 Zulu behaviors discussed above, these
	// operators insist that both values must have the same units.
	public void plusEquals(PredictionValue addend)
	{
		if (addend.getUnits() == AHTidePredictionUnits.ZULU)
		{
			assert (addend.getValue() == 0.0);
		}
		else
		{
			if (units == AHTidePredictionUnits.ZULU)
			{
				assert (value == 0.0);
				// operator = (addend); // Adopt units of addend
				this.value = addend.getValue();
				this.units = addend.getUnits();
			}
			else
			{
				assert (units == addend.getUnits());
				value += addend.getValue();
			}
		}
	}

	public void minusEquals(PredictionValue subtrahend)
	{
		value -= subtrahend.getValue();
	}

	public PredictionValue minus(PredictionValue sub)
	{
		assert ( !sub.isNull());
		return new PredictionValue(this.getUnits(), this.getValue() - sub.getValue());
	}

	public PredictionValue plus(PredictionValue adder)
	{
		return new PredictionValue(this.getUnits(), this.getValue() + adder.getValue());
	}

	// Like += except that conversions are silently performed.
	// Use only when you really mean it.
	public void convertAndAdd(PredictionValue addend)
	{
		if (addend.units.equals(AHTidePredictionUnits.ZULU))
		{
			assert (addend.getValue() == 0.0);
			return;
		}
		if (units != AHTidePredictionUnits.ZULU && units != addend.getUnits())
			addend.setUnits(units);
		plusEquals(addend);
	}

	// Print in the form -XX.YY units (padding as needed)
	public String print()
	{
		return String.format("%05.2f %s", value, units.getLongName());
	}

	// Same thing without padding, with abbreviated units.
	public String printnp()
	{
		return String.format("%.2f %s", value, units.getShortName());
	}

	public AHTidePredictionUnits getUnits()
	{
		return units;
	}

	public double getValue()
	{
		return value;
	}

	public void setUnits(AHTidePredictionUnits units)
	{
		if (this.units.equals(units))
		{
			// barf(Error::NO_CONVERSION,Error::nonfatal);
		}
		else
		{
			switch (this.units)
			{
			case ZULU:
				assert (value == 0.0);
				break;
			case FEET:
				if (units == AHTidePredictionUnits.METERS)
					value *= 0.3048;
				else
					throw new RuntimeException("Can't convert " + this.units + " to " + units);
				break;
			case METERS:
				if (units == AHTidePredictionUnits.FEET)
					value /= 0.3048;
				else
					throw new RuntimeException("Can't convert " + this.units + " to " + units);
				break;
			case KNOTS_SQUARED:
				if (units == AHTidePredictionUnits.KNOTS)
				{
					// This is not mathematically correct, but it is tidematically
					// correct.
					if (value < 0)
					{
						value = -Math.sqrt(Math.abs(value));
					}
					else
					{
						value = Math.sqrt(value);
					}
				}
				else
					throw new RuntimeException("Can't convert " + this.units + " to " + units);
				break;
			case KNOTS:
				if (units == AHTidePredictionUnits.KNOTS_SQUARED)
				{
					// This is used only in Station::predictTideEvents to set up
					// the mark level.
					// This is not mathematically correct, but it is tidematically
					// correct.
					if (value < 0)
					{
						value = -(value * value);
					}
					else
					{
						value *= value;
					}
				}
				else
					throw new RuntimeException("Can't convert " + this.units + " to " + units);
				break;
			default:
				assert (false);
			}
			this.units = units;
		}
	}

	public void setValue(double value)
	{
		makeNull(false);
		this.value = value;
	}

	public void multiply(double value)
	{
		assert (!isNull());
		this.value *= value;
	}

}
