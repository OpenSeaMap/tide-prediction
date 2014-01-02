/*    
    Copyright (C) 1997  David Flater.
    Java port Copyright (C) 2011 Chas Douglass
    Modifications about null and timezone Copyright (C) 2013 Alexej Humbach - ahumbach@humbach-ac.de

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

package ahdt.tides.base;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import ahdt.std.AHNullable;

/**
 * based on code from chas
 * 
 * @author humbach
 */
public class AHTimestamp extends AHNullable implements Comparable
{
	/**
	 *  the time in seconds. 
	 *  the base is 19700101 00:00:00 UTC.
	 */
	private long m_tTime;

	/**
	 * If nothing else is given the timestamp is null. 
	 */
	public AHTimestamp()
	{
		makeNull(true);
	}

	/**
	 * Creates a timestamp of now with a resolution of one minute. On a usual pc operating system the millisecondes are 'nonsense' anyway.
	 * For a server based internet interfaced app the seconds are nonsense too. Should they mean the moment the user pressed the key 
	 * or when the request arrived at the server or else?
	 * 
	 * @return timestamp of now [minute]
	 */
	static public AHTimestamp now()
	{
		return new AHTimestamp((Calendar.getInstance().getTimeInMillis() / 60000) * 60);
	}

	/**
	 * Creates a timestamp from the given time in seconds.
	 * 
	 * @param tTime [s] from 19700101 00:00:00 UTC
	 */
	public AHTimestamp(long tTime)
	{
		m_tTime = tTime;
		makeNull(false);
	}

	/**
	 * Creates a timestamp from the given timestamp.
	 * 
	 * @param tTime timestamp
	 */
	public AHTimestamp(AHTimestamp t)
	{
		m_tTime = t.getSeconds();
		makeNull(false);
	}

	public AHTimestamp(Year year)
	{
		Calendar ttime = new GregorianCalendar();
		ttime.set(year.getYear(), 0, 1, 0, 0, 0);
		ttime.setTimeZone(TimeZone.getTimeZone(AHTideBaseStr.getString("Timestamp.0")));
		m_tTime = ttime.getTimeInMillis() / 1000;
		makeNull(false);
	}

	/**
	 * @return the time in seconds from 19700101 00:00:00 UTC
	 */
	public long getSeconds()
	{
		return m_tTime;
	}

	public void setTime(AHTimestamp t)
	{
		if (t != null)
		{
			makeNull(false);
			m_tTime = t.getSeconds();
		}
	}

	public void setTime(Calendar c)
	{
		makeNull(false);
		m_tTime = c.getTimeInMillis();
	}

	public int compareTo(AHTimestamp t)
	{
		if (t == null)
			throw new NullPointerException();
		return (int) (m_tTime - t.getSeconds());
	}

	@Override
	public int compareTo(Object o)
	{
		if (o instanceof AHTimestamp)
		{
			return (int) (m_tTime - ((AHTimestamp) o).getSeconds());
		}
		throw new UnsupportedOperationException(AHTideBaseStr.getString("AHTides.0"));
	}

	/**
	 * Adds seconds to the current timestamp
	 * @param seconds
	 */
	public void plusEquals(int seconds)
	{
		m_tTime += seconds;
	}

	/**
	 * Adds a given interval to the current timestamp
	 * @param interval
	 */
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

	/**
	 * Puts the current time into a String
	 * It uses the current timezone in the java environment
	 * 
	 * @param timezone - not used at the moment
	 * @return formatted time as a String
	 */
	public String printTime()
	{
		SimpleDateFormat sdf = new SimpleDateFormat();
		return sdf.format(new Date(m_tTime * 1000));
	}

	public String printableDate(String timezone)
	{
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
