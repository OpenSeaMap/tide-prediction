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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import ahdt.std.AHNullable;

/**
 * 
 * @author chas
 */
public class AHTimestamp extends AHNullable implements Comparable
{
	/**
	 *  the time in seconds
	 */
	private long m_tTime;

	public AHTimestamp()
	{
		makeNull(true);
	}

	/**
	 * creates a timestamp of now with a resolution of one minute
	 * 
	 * @return
	 */
	static public AHTimestamp now()
	{
		// return new Timestamp(Calendar.getInstance().getTimeInMillis() / 1000);
		return new AHTimestamp((Calendar.getInstance().getTimeInMillis() / 60000) * 60);
	}

	public AHTimestamp(long tTime)
	{
		m_tTime = tTime;
		makeNull(false);
	}

	public AHTimestamp(AHTimestamp t)
	{
		m_tTime = t.getSeconds();
		makeNull(false);
	}

	public AHTimestamp(Year year)
	{
		Calendar ttime = new GregorianCalendar();
		ttime.set(year.getYear(), 0, 1, 0, 0, 0);
		ttime.setTimeZone(TimeZone.getTimeZone(AHTideBaseStr.getString("Timestamp.0"))); //$NON-NLS-1$
		m_tTime = ttime.getTimeInMillis() / 1000;
		makeNull(false);
	}

	public long getSeconds()
	{
		return m_tTime;
	}

	public void setTime(AHTimestamp t)
	{
		m_tTime = t.getSeconds();
		makeNull(false);
	}

	public void setTime(Calendar c)
	{
		m_tTime = c.getTimeInMillis();
		makeNull(false);
	}

	public int compareTo(AHTimestamp t)
	{
		return (int) (m_tTime - t.getSeconds());
	}

	@Override
	public int compareTo(Object o)
	{
		if (o instanceof AHTimestamp)
		{
			return (int) (m_tTime - ((AHTimestamp) o).getSeconds());
		}
		throw new UnsupportedOperationException(AHTideBaseStr.getString("AHTides.0")); //$NON-NLS-1$
	}

	public void plusEquals(int seconds)
	{
		m_tTime += seconds;
	}

	public void plusEquals(Interval ts)
	{
		m_tTime += ts.getSeconds();
	}

	public void minusEquals(int seconds)
	{
		m_tTime -= seconds;
	}

	public void minusEquals(Interval secs)
	{
		m_tTime -= secs.getSeconds();
	}

	public Interval minus(AHTimestamp sub)
	{
		return new Interval((int) (m_tTime - sub.getSeconds()));
	}

	public AHTimestamp minus(Interval sub)
	{
		return new AHTimestamp(m_tTime - sub.getSeconds());
	}

	public AHTimestamp plus(Interval adder)
	{
		return new AHTimestamp(m_tTime + adder.getSeconds());
	}

	public boolean gt(AHTimestamp t)
	{
		if (m_tTime > t.getSeconds())
			return true;
		return false;
	}

	public boolean gte(AHTimestamp ts)
	{
		return m_tTime >= ts.getSeconds();
	}

	public boolean lte(AHTimestamp ts)
	{
		return m_tTime <= ts.getSeconds();
	}

	public boolean lt(AHTimestamp ts)
	{
		return m_tTime < ts.getSeconds();
	}

	public String printableDate(String timezone)
	{
		// TODO: pay attention to timezone
		SimpleDateFormat sdf = new SimpleDateFormat();
		return sdf.format(new Date(m_tTime * 1000));
	}

	public int getYear()
	{
		Calendar cal = new GregorianCalendar();
		cal.setTime(new Date(m_tTime * 1000));
		return cal.get(Calendar.YEAR);
	}

}
