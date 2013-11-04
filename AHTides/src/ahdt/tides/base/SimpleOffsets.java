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
public class SimpleOffsets
{

	private Interval timeAdd;
	private PredictionValue levelAdd;
	private double levelMultiply;

	public SimpleOffsets()
	{
		timeAdd = new Interval();
		levelAdd = new PredictionValue();
		levelMultiply = 1.0;
	}

	// levelMultiply = 0 is treated as null and defaulted to 1.0.
	// levelMultiply < 0 is an error.
	public SimpleOffsets(Interval timeAdd, PredictionValue levelAdd, double levelMultiply)
	{
		this.timeAdd = timeAdd;
		this.levelAdd = levelAdd;
		this.levelMultiply = levelMultiply;
		if (levelMultiply == 0.0)
			this.levelMultiply = 1.0;
		assert (this.levelMultiply > 0.0);
	}

	public PredictionValue getLevelAdd()
	{
		return levelAdd;
	}

	public double getLevelMultiply()
	{
		return levelMultiply;
	}

	public Interval getTimeAdd()
	{
		return timeAdd;
	}

}
