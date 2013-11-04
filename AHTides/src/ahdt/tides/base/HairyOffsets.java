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
public class HairyOffsets
{

	private SimpleOffsets m_tMaxSO;
	private SimpleOffsets m_tMinSO;
	NullableInterval floodBegins;
	NullableInterval ebbBegins;

	public HairyOffsets(SimpleOffsets maxSO, SimpleOffsets minSO, NullableInterval floodBegins, NullableInterval ebbBegins)
	{
		this.m_tMaxSO = maxSO;
		this.m_tMinSO = minSO;
		this.floodBegins = floodBegins;
		this.ebbBegins = ebbBegins;
	}

	public Interval getMaxTimeAdd()
	{
		return m_tMaxSO.getTimeAdd();
	}

	public PredictionValue getMaxLevelAdd()
	{
		return m_tMaxSO.getLevelAdd();
	}

	public double getMaxLevelMultiply()
	{
		return m_tMaxSO.getLevelMultiply();
	}

	public Interval getMinTimeAdd()
	{
		return m_tMinSO.getTimeAdd();
	}

	public PredictionValue getMinLevelAdd()
	{
		return m_tMinSO.getLevelAdd();
	}

	public double getMinLevelMultiply()
	{
		return m_tMinSO.getLevelMultiply();
	}

	public NullableInterval getFloodBegins()
	{
		return floodBegins;
	}

	public NullableInterval getEbbBegins()
	{
		return ebbBegins;
	}

	/**
	 * 
	 * @param simpleOffsets_out
	 * @return
	 */
	public boolean trySimplify(SimpleOffsets simpleOffsets_out)
	{
		if ( !m_tMaxSO.equals(m_tMinSO))
			return false;
		// equal levelAdds cause the slacks to shift unless they are 0.
		// equal levelMults do not move the slacks, even if there's a permanent
		// current. (The distance between the middle level and the zero level
		// is stretched proportional to the multiplier, so the time at which
		// the slack occurs is unchanged.)
		// See also special case for hydraulics in HarmonicsFile.cc.
		if ( !floodBegins.isNull())
		{
			if ( !(new Interval(floodBegins).equals(m_tMaxSO.getTimeAdd())) || m_tMaxSO.getLevelAdd().getValue() != 0.0)
			{
				return false;
			}
		}
		if ( !ebbBegins.isNull())
		{
			if ( !(new Interval(ebbBegins).equals(m_tMaxSO.getTimeAdd())) || m_tMaxSO.getLevelAdd().getValue() != 0.0)
			{
				return false;
			}
		}
		simpleOffsets_out = m_tMaxSO;
		return true;
	}

	/**
	 * if possible makes a SimpleOffset from this HairyOffset
	 * 
	 * @return SimpleOffset if possible, if impossible null
	 * 
	 */
	public SimpleOffsets makeSimple()
	{
		SimpleOffsets tSO = null;

		if (m_tMaxSO.equals(m_tMinSO))
		{
			if ((floodBegins.isNull()) && (new Interval(floodBegins).equals(m_tMaxSO.getTimeAdd()) || m_tMaxSO.getLevelAdd().getValue() != 0.0))
			{
				tSO = m_tMaxSO;
			}
			if ((ebbBegins.isNull()) && (new Interval(ebbBegins).equals(m_tMaxSO.getTimeAdd()) || m_tMaxSO.getLevelAdd().getValue() != 0.0))
				tSO = m_tMaxSO;
		}
		return tSO;
	}
}
