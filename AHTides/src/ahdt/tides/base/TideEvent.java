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
/**
 * @author humbach
 *
 */
public class TideEvent
{

	public enum EventType {
		MAX, MIN, SLACKRISE, SLACKFALL, MARKRISE, MARKFALL, SUNRISE, SUNSET, MOONRISE, MOONSET, NEWMOON, FIRSTQUARTER, FULLMOON, LASTQUARTER, RAWREADING
	};

	private AHTimestamp eventTime;
	private EventType eventType;
	private PredictionValue eventLevel;
	private boolean isCurrent;

	// For sub stations with residual offsets, these record the time and
	// level of the corresponding event before corrections are applied.
	// This is not necessarily the same as the reference station: the
	// harmonic constants may still have been adjusted. When not
	// applicable, these variables remain null.
	private AHTimestamp uncorrectedEventTime = new AHTimestamp();
	private PredictionValue uncorrectedEventLevel = new PredictionValue();

	public TideEvent()
	{

	}

	public TideEvent(TideEvent te)
	{
		this.eventTime = te.getTime();
		this.eventType = te.getType();
		this.eventLevel = te.getLevel();
		this.isCurrent = te.isCurrent();
		this.uncorrectedEventTime = te.getUncorrectedEventTime();
		this.uncorrectedEventLevel = te.getUncorrectedEventLevel();
	}

	/**
	 * Creates a TideEvent from AHTimestamp. Assumes a RAWREADING as EventType
	 * @param tTs
	 */
	public TideEvent(AHTimestamp tTs)
	{
		this.eventTime = new AHTimestamp(tTs);
		this.eventType = EventType.RAWREADING;
		this.eventLevel = new PredictionValue();
		this.isCurrent = false;
		this.uncorrectedEventTime = new AHTimestamp();
		this.uncorrectedEventLevel = new PredictionValue();	  
	}
	
	/**
	 * Creates a TideEvent from AHTimestamp and EventType
	 * @param tTs
	 * @param tType
	 */
	public TideEvent(AHTimestamp tTs, EventType tType)
	{
		this.eventTime = new AHTimestamp(tTs);
		this.eventType = tType;
		this.eventLevel = new PredictionValue();
		this.isCurrent = false;
		this.uncorrectedEventTime = new AHTimestamp();
		this.uncorrectedEventLevel = new PredictionValue();
	}

	public AHTimestamp getTime()
	{
		return eventTime;
	}

	public void setTime(AHTimestamp t)
	{
		this.eventTime = t;
	}

	public EventType getType()
	{
		return eventType;
	}

	public void setType(EventType type)
	{
		this.eventType = type;
	}

	public boolean isCurrent()
	{
		return isCurrent;
	}

	/**
	 * Changes the type of this event to CURRENT
	 */
	public void makeCurrent()
	{
		this.isCurrent = true;
	}

	public PredictionValue getLevel()
	{
		return eventLevel;
	}

	public void setLevel(PredictionValue predictionValue)
	{
		this.eventLevel = predictionValue;
	}

	public PredictionValue getUncorrectedEventLevel()
	{
		return uncorrectedEventLevel;
	}

	public void setUncorrectedEventLevel(PredictionValue level)
	{
		uncorrectedEventLevel = new PredictionValue(level);
	}

	public AHTimestamp getUncorrectedEventTime()
	{
		return uncorrectedEventTime;
	}

	public void setUncorrectedEventTime(AHTimestamp eventTime)
	{
		uncorrectedEventTime = new AHTimestamp(eventTime);
	}

	/**
	 * Generate one line of text output, applying global formatting rules and so on.
	 * Legal forms are CSV, TEXT.
	 * Legal modes are PLAIN, RAW.
	 * text_out is not newline terminated.
	 * 
	 * print needs timezone and sometimes name and coordinates from station.
	 * 
	 * @param mode
	 * @param form
	 * @param station
	 * @return
	 */
	public String print(Constants.Mode mode, Constants.Format form)
	{
		String strTxtOut = AHTideBaseStr.getString("AHTides.Empty");
		String levelPrint = AHTideBaseStr.getString("AHTides.Empty");
		String timePrint = AHTideBaseStr.getString("AHTides.Empty");
		switch (mode)
		{
		case PLAIN:
		case RAW:
		default:
			switch (form)
			{
			case CSV:
				if ( !isSunMoonEvent())
				{
					levelPrint = eventLevel.printnp();
				}
				// station name goes to the Station.print() method instead of every line
//				strTxtOut = station.m_strName;
//				strTxtOut.replace(',', Global.CSV_REPCHAR);
//				strTxtOut += AHTideBaseStr.getString("TideEvent.CSVSep");
				strTxtOut += eventTime.printTime() + AHTideBaseStr.getString("TideEvent.CSVSep");
				strTxtOut += levelPrint + AHTideBaseStr.getString("TideEvent.CSVSep");
				String mangle = longDescription();
				mangle.replace(',', Global.CSV_REPCHAR);
				strTxtOut += mangle;
				break;
			case TEXT:
			default:
				if ( !isSunMoonEvent())
				{
					levelPrint = eventLevel.printnp();
				}
				// station name goes to the Station.print() method instead of every line
//				strTxtOut = station.m_strName;
//				strTxtOut += AHTideBaseStr.getString("TideEvent.TXTSep");
				strTxtOut += eventTime.printTime() + AHTideBaseStr.getString("TideEvent.TXTSep");
				strTxtOut += levelPrint + AHTideBaseStr.getString("TideEvent.TXTSep");
				strTxtOut += longDescription();
				break;
			}
			break;
		}
		return strTxtOut;
	}

	// This is only used for the bottom blurb line in graphs.
	// Max length 5 characters.
	public String shortDescription()
	{
		switch (eventType)
		{
		case SLACKRISE:
		case SLACKFALL:
			return AHTideBaseStr.getString("TideEvent.6"); //$NON-NLS-1$
		case MARKRISE:
		case MARKFALL:
			return AHTideBaseStr.getString("TideEvent.7"); //$NON-NLS-1$
		case MOONRISE:
			return AHTideBaseStr.getString("TideEvent.8"); //$NON-NLS-1$
		case MOONSET:
			return AHTideBaseStr.getString("TideEvent.9"); //$NON-NLS-1$
		case NEWMOON:
			return AHTideBaseStr.getString("TideEvent.10"); //$NON-NLS-1$
		case FIRSTQUARTER:
			return AHTideBaseStr.getString("TideEvent.11"); //$NON-NLS-1$
		case FULLMOON:
			return AHTideBaseStr.getString("TideEvent.12"); //$NON-NLS-1$
		case LASTQUARTER:
			return AHTideBaseStr.getString("TideEvent.13"); //$NON-NLS-1$
		default:
			assert (false);
		}
		return null; // Silence bogus "control reaches end of non-void function"
	}

	public boolean isSunMoonEvent()
	{
		if (eventType.compareTo(EventType.SUNRISE) >= 0 && eventType.compareTo(EventType.LASTQUARTER) <= 0)
			return true;
		return false;
	}

	public boolean isMaxMinEvent()
	{
		if (eventType == EventType.MAX || eventType == EventType.MIN)
			return true;
		return false;
	}

	// This returns true if the description of the event would be Min Flood or
	// Min Ebb.
	public boolean isMinCurrentEvent()
	{
		switch (eventType)
		{
		case MAX:
			return (isCurrent && eventLevel.getValue() < 0.0);
		case MIN:
			return (isCurrent && eventLevel.getValue() > 0.0);
		default:
			return false;
		}
	}

	public String longDescription()
	{
		switch (eventType)
		{
		case MAX:
			return (isCurrent ? (eventLevel.getValue() >= 0.0 ? AHTideBaseStr.getString("TideEvent.14") : AHTideBaseStr.getString("TideEvent.15")) : AHTideBaseStr.getString("TideEvent.16")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		case MIN:
			return (isCurrent ? (eventLevel.getValue() <= 0.0 ? AHTideBaseStr.getString("TideEvent.17") : AHTideBaseStr.getString("TideEvent.18")) : AHTideBaseStr.getString("TideEvent.19")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		case SLACKRISE:
			return AHTideBaseStr.getString("TideEvent.20"); //$NON-NLS-1$
		case SLACKFALL:
			return AHTideBaseStr.getString("TideEvent.21"); //$NON-NLS-1$
		case MARKRISE:
			if (isCurrent)
			{
				if (eventLevel.getValue() < 0.0)
				{
					return AHTideBaseStr.getString("TideEvent.22"); //$NON-NLS-1$
				}
				if (eventLevel.getValue() > 0.0)
				{
					return AHTideBaseStr.getString("TideEvent.23"); //$NON-NLS-1$
				}
				return AHTideBaseStr.getString("TideEvent.24"); //$NON-NLS-1$
			}
			return AHTideBaseStr.getString("TideEvent.25"); //$NON-NLS-1$
		case MARKFALL:
			if (isCurrent)
			{
				if (eventLevel.getValue() < 0.0)
				{
					return AHTideBaseStr.getString("TideEvent.26"); //$NON-NLS-1$
				}
				if (eventLevel.getValue() > 0.0)
				{
					return AHTideBaseStr.getString("TideEvent.27"); //$NON-NLS-1$
				}
				return AHTideBaseStr.getString("TideEvent.28"); //$NON-NLS-1$
			}
			return AHTideBaseStr.getString("TideEvent.29"); //$NON-NLS-1$
		case SUNRISE:
			return AHTideBaseStr.getString("TideEvent.30"); //$NON-NLS-1$
		case SUNSET:
			return AHTideBaseStr.getString("TideEvent.31"); //$NON-NLS-1$
		case MOONRISE:
			return AHTideBaseStr.getString("TideEvent.32"); //$NON-NLS-1$
		case MOONSET:
			return AHTideBaseStr.getString("TideEvent.33"); //$NON-NLS-1$
		case NEWMOON:
			return AHTideBaseStr.getString("TideEvent.34"); //$NON-NLS-1$
		case FIRSTQUARTER:
			return AHTideBaseStr.getString("TideEvent.35"); //$NON-NLS-1$
		case FULLMOON:
			return AHTideBaseStr.getString("TideEvent.36"); //$NON-NLS-1$
		case LASTQUARTER:
			return AHTideBaseStr.getString("TideEvent.37"); //$NON-NLS-1$
		case RAWREADING:
		default:
			assert (false);
		}
		return null;
	}
}
