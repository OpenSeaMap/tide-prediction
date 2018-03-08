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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.logging.Logger;

import ahdt.tides.base.AHTUnits.AHTidePredictionUnits;
import ahdt.tides.base.Constants.Format;
import ahdt.tides.base.Constants.Mode;
import ahdt.tides.tcd.AHTideTCDStr;
import ahdt.tides.tcd.TideDB;
import ahdt.tides.tcd.TideRecord;
import ahdt.tides.tcd.TideRecord.EStatType;

/**
 * A Station is the object enabling calculations and predictions.
 * 
 * Station has the subclasses RefStation and SubStation. 
 * 
 * The superclass is used for reference stations and that rare subordinate station where the offsets can be reduced to simple
 * corrections to the constituents and datum. After such corrections are made, there is no operational difference between that 
 * and a reference station.
 * 
 * @author humbach
 * 
 */
public class Station implements Cloneable
{
	protected transient Logger logger = Logger.getLogger(this.getClass().getName());

	protected int m_ID;
	protected String m_strName;
	protected String m_TimeZone;
	protected String m_strNotes;
	protected Coordinates m_Coordinates;
	protected ConstituentSet m_tConst;
	protected CurrentBearing m_tMinCurrentBearing;
	protected CurrentBearing m_tMaxCurrentBearing;
	protected boolean m_bIsCurrent;
	protected boolean m_bIsReferenceStation;

	// Attributes that aren't intrinsic to the station, but must
	// transfer with it is copied. Clients *are* entitled to
	// modify these values. NOTE: markLevel must be specified in
	// predictUnit() units.
	protected PredictionValue markLevel = new PredictionValue();
	protected double aspect;
	protected Interval step;

	// protected StationRef stationRef;

	protected Deque<MetaField> m_tMetadata;

	// To get all tide events falling between t1 and t2, you have to
	// scan the interval from t1 - maximumTimeOffset to t2 - minimumTimeOffset.
	// These will remain zero for reference stations.
	protected Interval minimumTimeOffset = new Interval(); // Most negative, or
	// least positive.
	protected Interval maximumTimeOffset = new Interval(); // Most positive, or

	// least negative.

	/**
	 * constructs a station from a TideDB record
	 * 
	 * @param tRec is the TideRecord whose data are to be loaded into the station object
	 */
	public Station(TideRecord tRec)
	{
		m_ID = tRec.getID();
		step = new Interval();
	}
	
	public void dump()
	{
		System.out.println(AHTideBaseStr.getString("Station.2") + getName()); //$NON-NLS-1$
		System.out.println(AHTideBaseStr.getString("Station.3") + getTimeZone()); //$NON-NLS-1$
		System.out.println(AHTideBaseStr.getString("Station.4") + getMinCurrentBearing().getDegrees()); //$NON-NLS-1$
		System.out.println(AHTideBaseStr.getString("Station.5") + getMaxCurrentBearing().getDegrees()); //$NON-NLS-1$
		System.out.println(AHTideBaseStr.getString("Station.6") + this.getName()); //$NON-NLS-1$
		System.out.println(AHTideBaseStr.getString("Station.7") + this.isIsCurrent()); //$NON-NLS-1$
		// stationRef.dump();
		m_tConst.dump();
		System.out.println(AHTideBaseStr.getString("Station.8")); //$NON-NLS-1$
		String metaFormatString = AHTideBaseStr.getString("Station.9"); //$NON-NLS-1$
		for (MetaField mf: m_tMetadata)
		{
			System.out.printf(metaFormatString, mf.getName(), mf.getValue());
		}
	}

	/**
	 * General method for generating output that fits into a String (CSV, TEXT).
	 * 
	 * @param tStartTime
	 *          - the start of the prediction
	 * @param tEndTime
	 *          - the end of the prediction
	 */

	public String print(AHTimestamp tStartTime, AHTimestamp tEndTime, Constants.Mode eMode, Constants.Format eFormat)
	{
		String strOut = "???";
		switch (eMode)
		{
		case PLAIN:
			strOut = plainMode(tStartTime, tEndTime, eFormat);
			break;
		case RAW:
			strOut = rawMode(tStartTime, tEndTime, eFormat);
			break;
		default:
			strOut = "wrong mode: '" + eMode + "', not yet implemented";
		}
		return strOut;
	}

	/**
	 * iCalendar format output is actually produced by plainMode. From an engineering perspective this makes perfect sense. But from a usability
	 * perspective, iCalendar output is a calendar and ought to appear in calendar mode. So calendarMode falls through to plainMode when i format is chosen.
	 * 
	 * @param tStartTime
	 * @param tEndTime
	 * @param eForm
	 * @return
	 */
	public String plainMode(AHTimestamp tStartTime, AHTimestamp tEndTime, Constants.Format eForm)
	{
		// textBoilerplate(text_out, form);
		String strTxtOut = new String();
		strTxtOut += this.getName() + AHTideBaseStr.getString("AHTides.NewLine");
		TideEventsOrganizer organizer = new TideEventsOrganizer();
		predictTideEvents(tStartTime, tEndTime, organizer, Station.TideEventsFilter.USUAL_TIDE_EVENTS);
		for (TideEvent t: organizer.values())
		{
			strTxtOut += t.print(Constants.Mode.PLAIN, eForm) + AHTideBaseStr.getString("AHTides.NewLine");
		}
		return strTxtOut;
	}

	/**
	 * raw mode predicts values in a fixed time spacing (150s for the time being).
	 * 
	 * @param tStartTime
	 * @param tEndTime
	 * @param eForm
	 * @return
	 */
	public String rawMode(AHTimestamp tStartTime, AHTimestamp tEndTime, Constants.Format eForm)
	{
		// textBoilerplate(text_out, form);
		String strTxtOut = new String();
		strTxtOut += this.getName() + AHTideBaseStr.getString("AHTides.NewLine");
		TideEventsOrganizer organizer = new TideEventsOrganizer();
		predictTideDataRaw(tStartTime, tEndTime, organizer, Station.TideEventsFilter.USUAL_TIDE_EVENTS);
		for (TideEvent t: organizer.values())
		{
			strTxtOut += t.print(Constants.Mode.PLAIN, eForm) + AHTideBaseStr.getString("AHTides.NewLine");
		}
		return strTxtOut;
	}

	/**
	 * Change preferred units of length. Default is as specified by settings. Attempts to set same units are tolerated without complaint. Attempts to set velocity
	 * units are punished. -nyf-
	 * 
	 * @param units
	 */
	public void setUnits(AHTidePredictionUnits units)
	{
		if (m_bIsCurrent)
		{
			m_tConst.setUnits(units);
			if (markLevel.isNull() && (markLevel.getUnits() != units))
					markLevel.setUnits(units);
		}
//		throw new UnsupportedOperationException(AHTideBaseStr.getString("AHTides.NYR")); //$NON-NLS-1$
	}

	/**
	 * Returns units that prediction methods will return (never knots squared).
	 * 
	 * @return
	 */
	public AHTidePredictionUnits predictUnits()
	{
		return AHTUnits.flatten(m_tConst.getPredictUnits());
	}

	/**
	 * Mathematical bounds (considerably wider than lowest/highest astronomical tide)---used to set range in graphs.
	 * 
	 * @return
	 */
	public PredictionValue getMinLevel()
	{
		return m_tConst.getDatum().minus(new PredictionValue(m_tConst.getMaxAmplitude()));
	}

	public PredictionValue getMaxLevel()
	{
		return m_tConst.getDatum().plus(new PredictionValue(m_tConst.getMaxAmplitude()));
	}

	/**
	 * Get heights or velocities for a given moment in time.
	 * 
	 * @param predictTime
	 * @return
	 */
	public PredictionValue predictTideLevel(AHTimestamp predictTime)
	{
		// logger.log(java.util.logging.Level.FINE, "predictTime = " + predictTime.getTime());
		return finishPredictionValue(m_tConst.tideDerivative(predictTime, 0));
	}

	/**
	 * Get heights or velocities for a given event, which knows its time.
	 * 
	 * @param TideEvent tEvent
	 * @return
	 */
	public PredictionValue predictTideLevel(TideEvent tEvent)
	{
		// logger.log(java.util.logging.Level.FINE, "predictTime = " + predictTime.getTime());
		return finishPredictionValue(m_tConst.tideDerivative(tEvent.getTime(), 0));
	}

	// non-overridable version
	final public PredictionValue parentPredictTideLevel(AHTimestamp predictTime)
	{
		logger.log(java.util.logging.Level.FINE, AHTideBaseStr.getString("Station.12") + predictTime.getSeconds());
		return finishPredictionValue(m_tConst.tideDerivative(predictTime, 0));
	}

	/**
	 * 
	 * Filters for predictTideEvents. 
	 * NO_FILTER = maxes, mins, slacks, mark crossings, sun and moon 
	 * USUAL_TIDE_EVENTS = no sun and moon events 
	 * KNOWN_TIDE_EVENTS = tide events that can be determined without interpolation (maxes, mins, and sometimes slacks) 
	 * MAX_MIN = maxes and mins
	 */
	public enum TideEventsFilter
	{
		NO_FILTER, KNOWN_TIDE_EVENTS, MAX_MIN, USUAL_TIDE_EVENTS
	};

	/**
	 * Get all tide events within a range of timestamps and add them to the organizer. The range is >= startTime and < endTime. Because predictions are done to
	 * plus or minus one minute, invoking this multiple times with adjoining ranges could duplicate or delete tide events falling right on the boundary.
	 * TideEventsOrganizer should suppress the duplicates, but omissions will not be detected.
	 * 
	 * Either settings or the filter arg can suppress sun and moon events.
	 * 
	 * @param startTime
	 * @param endTime
	 * @param organizer
	 */

	public void predictAllTideEvents(AHTimestamp startTime, AHTimestamp endTime, TideEventsOrganizer organizer)
	{
		predictTideEvents(startTime, endTime, organizer, TideEventsFilter.NO_FILTER);
	}

	public void predictTideEvents(AHTimestamp startTime, AHTimestamp endTime, TideEventsOrganizer organizer, TideEventsFilter filter)
	{
		if (startTime.gte(endTime))
			return;
		addSimpleTideEvents(startTime, endTime, organizer, filter);

		if (filter == TideEventsFilter.NO_FILTER)
		{
			addSunMoonEvents(startTime, endTime, organizer);
		}
	}

	/**
	 * @param startTime
	 * @param endTime
	 * @param organizer
	 */
	public void predictTideData(AHTimestamp startTime, AHTimestamp endTime, TideEventsOrganizer organizer)
	{
		predictTideDataRaw(startTime, endTime, organizer, TideEventsFilter.USUAL_TIDE_EVENTS);
	}

	/**
	 * @param startTime
	 * @param endTime
	 * @param organizer
	 * @param filter
	 */
	public void predictTideDataRaw(AHTimestamp startTime, AHTimestamp endTime, TideEventsOrganizer organizer, TideEventsFilter filter)
	{		
		AHTimestamp tTs = startTime;
		TideEvent startEvent, endEvent;
		if (startTime.lt(endTime))
		{
			// add the 'usual' events
			addSimpleTideEvents(startTime, endTime, organizer, filter);
			// now add the intermediate 'events'
			startEvent = new TideEvent(startTime);
			startEvent.setLevel(predictTideLevel(startTime));
			System.out.println(startEvent.print(Mode.RAW, Format.TEXT));
			organizer.add(startEvent);
			while (tTs.lt(endTime))
			{
				tTs.plusEquals(150);
				TideEvent tEvent = new TideEvent(tTs);
				tEvent.setLevel(predictTideLevel(tTs));
				System.out.println(tEvent.print(Mode.RAW, Format.TEXT));
				organizer.add(tEvent);
			}
			endEvent = new TideEvent(endTime);
			endEvent.setLevel(predictTideLevel(endTime));
			System.out.println(endEvent.print(Mode.RAW, Format.TEXT));
			organizer.add(endEvent);
		}
	}

	// Direction for extendRange.
	public enum Direction
	{
		FORWARD, BACKWARD
	};

	/**
	 * Add events to an organizer to extend its range in the specified direction by the specified interval. (Number of events is indeterminate.) A safety margin
	 * is used to attempt to prevent tide events from falling through the cracks as discussed above predictTideEvents.
	 * 
	 * Either settings or the filter arg can suppress sun and moon events.
	 */
	public void extendRange(TideEventsOrganizer organizer, Direction direction, Interval howMuch, TideEventsFilter filter)
	{
		/*
		 * assert (howMuch > Global::zeroInterval); Timestamp startTime, endTime; if (direction == forward) { TideEventsReverseIterator it = organizer.rbegin();
		 * assert (it != organizer.rend()); startTime = it->second.eventTime; endTime = startTime + howMuch; startTime -= Global::eventSafetyMargin; } else {
		 * TideEventsIterator it = organizer.begin(); assert (it != organizer.end()); endTime = it->second.eventTime; startTime = endTime - howMuch; endTime +=
		 * Global::eventSafetyMargin; } predictTideEvents (startTime, endTime, organizer, filter);
		 */
		assert (howMuch.gt(Global.ZERO_INTERVAL));
		AHTimestamp startTime = new AHTimestamp();
		AHTimestamp endTime = new AHTimestamp();
		if (direction == Direction.FORWARD)
		{
			TideEvent last = organizer.get(organizer.lastKey());
			startTime = last.getTime();
			endTime = startTime.plus(howMuch);
			startTime.minusEquals(Global.EVENT_SAFETY_MARGIN);
		}
		else
		{
			TideEvent first = organizer.get(organizer.firstKey());
			endTime = first.getTime();
			startTime = endTime.minus(howMuch);
			endTime.plusEquals(Global.EVENT_SAFETY_MARGIN);
		}
		predictTideEvents(startTime, endTime, organizer, filter);
	}

	// Analogous, for raw readings. Specify number of events in howmany.
	public void extendRange(TideEventsOrganizer organizer, Direction direction, int howMany)
	{
		throw new UnsupportedOperationException();
	}

	public double getAspect()
	{
		return aspect;
	}

	public void setAspect(double aspect)
	{
		this.aspect = aspect;
	}

	public PredictionValue getMarkLevel()
	{
		return markLevel;
	}

	public void setMarkLevel(PredictionValue markLevel)
	{
		this.markLevel = markLevel;
	}

	public Interval getStep()
	{
		return step;
	}

	public void setStep(Interval step)
	{
		this.step = step;
	}

	public Coordinates getCoordinates()
	{
		return m_Coordinates;
	}

	public boolean isIsCurrent()
	{
		return m_bIsCurrent;
	}

	public CurrentBearing getMinCurrentBearing()
	{
		return m_tMinCurrentBearing;
	}

	public void setMinCurrentBearing(CurrentBearing tMinCurrentBearing)
	{
		this.m_tMinCurrentBearing = tMinCurrentBearing;
	}

	public CurrentBearing getMaxCurrentBearing()
	{
		return m_tMaxCurrentBearing;
	}

	public void setMaxCurrentBearing(CurrentBearing tMaxCurrentBearing)
	{
		this.m_tMaxCurrentBearing = tMaxCurrentBearing;
	}

	public String getName()
	{
		return m_strName;
	}

	public String getNotes()
	{
		return m_strNotes;
	}

	public String getTimeZone()
	{
		return m_TimeZone;
	}

	public Deque<MetaField> getMetadata()
	{
		return m_tMetadata;
	}

	public void setMetadata(Deque<MetaField> metadata)
	{
		this.m_tMetadata = metadata;
	}

	protected boolean isSubordinateStation()
	{
		return false;
	}

	// These two return true if the offset is known OR is not needed
	// (i.e., it's a reference station or the offsets are simple).
	protected boolean haveFloodBegins()
	{
		return true;
	}

	protected boolean haveEbbBegins()
	{
		return true;
	}

	// G. Dairiki code, slightly revised. See Station.cc for
	// more documentation.

	// Functions to zero out.
	// Option #1 -- find maxima and minima.
	// Marklev is unused (but maxMinZeroFn and markZeroFn must have the same
	// signature).

	// We are guaranteed to find all high and low tides as long as their
	// spacing is greater than Global::eventPrecision.

	public class MaxMinZeroFn implements DairikiFunctor
	{
		public PredictionValue functor(AHTimestamp t, int deriv, PredictionValue marklev)
		{
			return m_tConst.tideDerivative(t, deriv + 1);
		}
	}

	// Option #2 -- find mark crossings or slack water.
	// ** Marklev must be made compatible with the tide as returned by
	// tideDerivative, i.e., no getDatum, no conversion from KnotsSquared.

	public class MarkZeroFn implements DairikiFunctor
	{
		public PredictionValue functor(AHTimestamp t, int deriv, PredictionValue marklev)
		{
			PredictionValue pv_out = m_tConst.tideDerivative(t, deriv);
			if (deriv == 0)
			{
				pv_out.minusEquals(marklev);
			}
			return pv_out;
		}
	}

	/*
	 * findZero (time_t t1, time_t t2, double (*f)(time_t t, int deriv)) 
	 * Find a zero of the function f, which is bracketed by t1 and t2. 
	 * Returns a value which is either an exact zero of f, or slightly past the zero of f.
	 */

	// Root finder.
	// If tl >= tr, assertion failure.
	// If tl and tr do not bracket a root, assertion failure.
	// If a root exists exactly at tl or tr, assertion failure.

	private AHTimestamp findZero(AHTimestamp tl, AHTimestamp tr, DairikiFunctor functor, PredictionValue marklev)
	{
		PredictionValue fl = new PredictionValue(functor.functor(tl, 0, marklev));
		PredictionValue fr = new PredictionValue(functor.functor(tr, 0, marklev));
		double scale = 1.0;
		Interval dt;
		AHTimestamp t = new AHTimestamp();
		PredictionValue fp = new PredictionValue(), ft = new PredictionValue(), f_thresh = new PredictionValue();

		assert (fl.getValue() != 0.0 && fr.getValue() != 0.0);
		assert (tl.lt(tr));
		if (fl.getValue() > 0)
		{
			scale = -1.0;
			fl.negate();
			fr.negate();
		}
		assert (fl.getValue() < 0.0 && fr.getValue() > 0.0);

		while (tr.minus(tl).gt(Global.EVENT_PRECISION))
		{
			// logger.log(java.util.logging.Level.FINE,
			// String.format("still looking because %d - %d (%d) > %d",
			// tr.getTime(), tl.getTime(), tr.minusEquals(tl).getSeconds(),
			// Global.EVENT_PRECISION.getSeconds()));
			if (t.isNull())
				dt = new Interval(Global.ZERO_INTERVAL); // Force bisection on
			// first step
			else if ((PredictionValue.abs(ft).gt(f_thresh)) || (ft.getValue() > 0.0 ? (fp.lte(ft.divide(t.minus(tl).getSeconds()))) : (fp.lte(ft.negative().divide(tr.minus(t).getSeconds())))))
				dt = new Interval(Global.ZERO_INTERVAL); /* Force bisection */
			else
			{
				// Attempt a newton step
				assert (fp.getValue() != 0.0);
				// Here I actually do want to round away from zero.
				dt = new Interval(llround(ft.negative().divide(fp)));

				/*
				 * Since our goal specifically is to reduce our bracket size as quickly as possible (rather than getting as close to the zero as possible) we should
				 * ensure that we don't take steps which are too small. (We'd much rather step over the root than take a series of steps that approach the root rapidly
				 * but from only one side.)
				 */
				if (Interval.abs(dt).lt(Global.EVENT_PRECISION))
					dt = (ft.getValue() < 0.0 ? new Interval(Global.EVENT_PRECISION) : new Interval(Global.EVENT_PRECISION.negative()));

				t.plusEquals(dt);
				if (t.gte(tr) || t.lte(tl))
					dt = new Interval(Global.ZERO_INTERVAL); // Force bisection if outside bracket
				f_thresh = ft.abs().divide(2.0);
			}
			if (dt.equals(Global.ZERO_INTERVAL))
			{
				// Newton step failed, do bisection
				t = tl.plus(tr.minus(tl).divide(2));
				f_thresh = fr.gt(fl.negative()) ? new PredictionValue(fr) : new PredictionValue(fl.negative());
			}
			ft = functor.functor(t, 0, marklev).times(scale);
			if (ft.getValue() == 0.0)
				return t; // Exact zero
			else if (ft.getValue() > 0.0)
			{
				tr = new AHTimestamp(t);
				fr = new PredictionValue(ft);
			}
			else
			{
				tl = new AHTimestamp(t);
				fl = new PredictionValue(ft);
			}
			fp = functor.functor(t, 1, marklev).times(scale);
		}
		return tr;
	}

	public class MarkCrossing
	{
		private AHTimestamp t;
		private boolean rising;

		public MarkCrossing()
		{

		}

		public MarkCrossing(AHTimestamp t, boolean rising)
		{
			this.t = t;
			this.rising = rising;
		}

		public boolean isRising()
		{
			return rising;
		}

		public void setRising(boolean rising)
		{
			this.rising = rising;
		}

		public AHTimestamp getT()
		{
			return t;
		}

		public void setT(AHTimestamp t)
		{
			this.t = t;
		}

	}

	// Find the marklev crossing in this bracket. Used for both markLevel and slacks.
	// * Doesn't matter which of t1 and t2 is greater.
	// * If t1 == t2, returns null.
	// * If t1 and t2 do not bracket a mark crossing, returns null.
	// * If mark crossing is exactly at t1 or t2, returns that.
	private MarkCrossing findMarkCrossing_Dairiki(AHTimestamp t1, AHTimestamp t2, PredictionValue marklev)
	{
		if (t1.compareTo(t2) > 0)
		{
			// std::swap(t1, t2);
			AHTimestamp t3 = t2;
			t2 = t1;
			t1 = t3;
		}
		boolean isRising_out = false;

		PredictionValue f1 = new MarkZeroFn().functor(t1, 0, marklev);
		PredictionValue f2 = new MarkZeroFn().functor(t2, 0, marklev);

		// Fail gently on rotten brackets. (This used to be an assertion.)
		if (f1.equals(f2))
			return new MarkCrossing(new AHTimestamp(), isRising_out); // return
		// null
		// timestamp

		// We need || instead of && to set isRising_out correctly in the
		// case where there's a zero exactly at t1 or t2.
		isRising_out = (f1.getValue() < 0.0 || f2.getValue() > 0.0);
		if (!isRising_out)
		{
			f1.negate();
			f2.negate();
		}

		// Since f1 != f2, we can't get two zeros, so it doesn't matter which
		// one we check first.
		if (f1.getValue() == 0.0)
		{
			return new MarkCrossing(t1, isRising_out);
		}
		else if (f2.getValue() == 0.0)
		{
			return new MarkCrossing(t2, isRising_out);
		}

		if (f1.getValue() < 0.0 && f2.getValue() > 0.0)
			return new MarkCrossing(findZero(t1, t2, new MarkZeroFn(), marklev), isRising_out);

		return new MarkCrossing(new AHTimestamp(), isRising_out); // Don't have
		// a
		// bracket, return
		// null timestamp.
	}

	// Find the next maximum or minimum.
	// eventTime and eventType are set to the next event (uncorrected time).
	// Nothing else in tideEvent_out is changed.
	// finishTideEvent must be called afterward.

	/*
	 * next_zero(time_t t, double (*f)(), double max_fp, double max_fpp) Find the next zero of the function f which occurs after time t. The arguments max_fp and
	 * max_fpp give the maximum possible magnitudes that the first and second derivative of f can achieve.
	 * 
	 * Algorithm: Our goal here is to bracket the next zero of f --- then we can use findZero() to quickly refine the root. So, we will step forward in time until
	 * the sign of f changes, at which point we know we have bracketed a root. The trick is to use large steps in our search, making sure the steps are not so
	 * large that we inadvertently step over more than one root.
	 * 
	 * The big trick, is that since the tides (and derivatives of the tides) are all just harmonic series's, it is easy to place absolute bounds on their values.
	 */
	// This method is only used in one place and is only used for finding
	// maxima and minima, so I renamed it to nextMaxMin, got rid of the
	// max_fp and max_fpp parameters, and installed a more convenient out
	// parameter.
	// Since by definition the tide cannot change direction between maxima
	// and minima, there is at most one crossing of a given mark level
	// between min/max points. Therefore, we already have a bracket of
	// the mark level to give to findZero, and there is no need for a
	// function like this to find the next mark crossing.

	private TideEvent nextMaxMin(AHTimestamp t)
	{
		logger.log(java.util.logging.Level.FINE, AHTideBaseStr.getString("Station.13") + t.getSeconds()); //$NON-NLS-1$
		TideEvent tideEvent_out = new TideEvent();
		Amplitude maxFp = m_tConst.tideDerivativeMax(2);
		Amplitude maxFpp = m_tConst.tideDerivativeMax(3);

		AHTimestamp tLeft, tRight;
		Interval step0, step1, step2;
		PredictionValue fLeft, dfLeft, fRight, junk = new PredictionValue();
		double scale = 1.0;

		tLeft = new AHTimestamp(t);

		// If we start at a zero, step forward until we're past it.
		while ((fLeft = new MaxMinZeroFn().functor(tLeft, 0, junk)).getValue() == 0.0)
			tLeft = tLeft.plus(Global.EVENT_PRECISION);

		if (fLeft.getValue() < 0.0)
		{
			tideEvent_out.setType(TideEvent.EventType.MIN);
		}
		else
		{
			tideEvent_out.setType(TideEvent.EventType.MAX);
			scale = -1.0;
			fLeft.negate();
		}

		logger.log(java.util.logging.Level.FINE, String.format(AHTideBaseStr.getString("Station.14"), fLeft.getValue())); //$NON-NLS-1$

		while (true)
		{

			/* Minimum time to next zero: */
			step1 = new Interval((fLeft.abs()).divide(new PredictionValue(maxFp)));

			/* Minimum time to next turning point: */
			dfLeft = new MaxMinZeroFn().functor(tLeft, 1, junk).times(scale);
			step2 = new Interval((dfLeft.abs()).divide(new PredictionValue(maxFpp)));

			if (dfLeft.getValue() < 0.0)
				/* Derivative is in the wrong direction. */
				step0 = step1.add(step2);
			else
				step0 = step1.gt(step2) ? step1 : step2;

			if (step0.lt(Global.EVENT_PRECISION))
				step0 = new Interval(Global.EVENT_PRECISION); /*
																											 * No ridiculously small steps
																											 */

			tRight = tLeft.plus(step0);

			logger.log(java.util.logging.Level.FINE,
					String.format(AHTideBaseStr.getString("Station.15"), step1.getSeconds(), step2.getSeconds(), dfLeft.getValue(), tRight.getSeconds())); //$NON-NLS-1$
			/*
			 * If we hit upon an exact zero, step right until we're off the zero. If the sign has changed, we are bracketing a desired root. If the sign hasn't
			 * changed, then the zero was at an inflection point (i.e. a double-zero to within Global::eventPrecision) and we want to ignore it.
			 */
			fRight = new MaxMinZeroFn().functor(tRight, 0, junk).times(scale);
			while (fRight.getValue() == 0.0)
			{
				tRight = tRight.plus(Global.EVENT_PRECISION);
				fRight = new MaxMinZeroFn().functor(tRight, 0, junk).times(scale);
			}

			if (fRight.getValue() > 0.0)
			{ /* Found a bracket */
				tideEvent_out.setTime(findZero(tLeft, tRight, new MaxMinZeroFn(), junk));
				return tideEvent_out;
			}

			tLeft = new AHTimestamp(tRight);
			fLeft = new PredictionValue(fRight);
		}
	}

	// Wrapper for findMarkCrossing_Dairiki that does necessary compensations for getDatum, KnotsSquared, and units. 
	// Used for both markLevel and slacks.
	protected MarkCrossing findSimpleMarkCrossing(AHTimestamp t1, AHTimestamp t2, PredictionValue marklev)
	{
		// marklev must compensate for getDatum and KnotsSquared. See
		// markZeroFn.
		// Units should already be comparable to getDatum.
		marklev.minusEquals(m_tConst.getDatum());
		// Correct knots / knots squared
		if (m_tConst.getPredictUnits() != marklev.getUnits())
		{
			marklev.setUnits(m_tConst.getPredictUnits());
		}

		return findMarkCrossing_Dairiki(t1, t2, marklev);
	}

	/**
	 * 
	 * 
	 * @param startTime
	 * @param endTime
	 * @param organizer
	 * @param filter
	 */
	private void addSimpleTideEvents(AHTimestamp startTime, AHTimestamp endTime, TideEventsOrganizer organizer, TideEventsFilter filter)
	{
		boolean isRising = false;
		TideEvent te = new TideEvent();

		// loopTime is the "internal" timestamp for scanning the reference station.
		// The timestamps of each event get mangled for substations.
		AHTimestamp loopTime = startTime.minus(maximumTimeOffset);
		AHTimestamp loopEndTime = endTime.minus(minimumTimeOffset);
		logger.log(java.util.logging.Level.FINE, String.format(AHTideBaseStr.getString("Station.16"), loopTime.getSeconds(), loopEndTime.getSeconds())); //$NON-NLS-1$

		// Patience... range is correctly enforced below.
		while (loopTime.lte(loopEndTime))
		{
			AHTimestamp previousLoopTime = new AHTimestamp(loopTime);

			// Get next max or min.
			te = nextMaxMin(loopTime);
			loopTime.setTime(te.getTime());
			te = finishTideEvent(te);
			if (te.getTime().gte(startTime) && te.getTime().lt(endTime))
			{
				organizer.add(te);
				logger.log(java.util.logging.Level.FINE, String.format(AHTideBaseStr.getString("Station.17"), te.getTime().getSeconds(), te.getType().ordinal(), te.getLevel().getValue())); //$NON-NLS-1$
			}

			// Check for slacks, if applicable. Skip the ones that need interpolation; 
			// those are done in SubordinateStation::addInterpolatedSubstationMarkCrossingEvents.
			if (filter != TideEventsFilter.MAX_MIN && m_bIsCurrent
					&& ((te.getType() == TideEvent.EventType.MAX && haveFloodBegins()) 
							|| (te.getType() == TideEvent.EventType.MIN && haveEbbBegins())))
			{
				te = new TideEvent(te);
				MarkCrossing mc = findSimpleMarkCrossing(previousLoopTime, loopTime, new PredictionValue(predictUnits(), 0.0));
				// te.setEventTime(mc.getT());
				te.setTime(new AHTimestamp(mc.getT()));
				isRising = mc.isRising();
				if (!(te.getTime().isNull()))
				{
					te.setType(isRising ? TideEvent.EventType.SLACKRISE : TideEvent.EventType.SLACKFALL);
					te = finishTideEvent(te);
					if (te.getTime().compareTo(startTime) >= 0 && te.getTime().compareTo(endTime) < 0)
					{
						organizer.add(te);
					}
				}
			}

			// Check for mark, if applicable.
			if ((!isSubordinateStation()) && (!markLevel.isNull()) && (filter == TideEventsFilter.NO_FILTER))
			{
				te = new TideEvent(te);
				MarkCrossing mc = findSimpleMarkCrossing(previousLoopTime, loopTime, markLevel);
				te.setTime(mc.getT());
				isRising = mc.isRising();
				if (!(te.getTime().isNull()))
				{
					te.setType(isRising ? TideEvent.EventType.MARKRISE : TideEvent.EventType.MARKFALL);
					te = finishTideEvent(te);
				}

				if (te.getTime().compareTo(startTime) >= 0 && te.getTime().compareTo(endTime) < 0)
				{
					organizer.add(te);
				}
			}
		}
	}

	private void addSunMoonEvents(AHTimestamp startTime, AHTimestamp endTime, TideEventsOrganizer organizer)
	{
		logger.warning(AHTideBaseStr.getString("Station.18"));
	}

	/**
	 * Given eventTime and eventType, fill in other fields and possibly apply corrections.
	 * Should be done when constructing the TideEvent object TideEvent(AHTimestamp, EventType).
	 * 
	 * @param te
	 */
	protected TideEvent finishTideEvent(TideEvent te)
	{
		if (m_bIsCurrent)
			te.makeCurrent();
		te.setUncorrectedEventTime(new AHTimestamp());
		te.setUncorrectedEventLevel(new PredictionValue());
		if (te.isSunMoonEvent())
			te.setLevel(new PredictionValue());
		else
			te.setLevel(new PredictionValue(parentPredictTideLevel(te.getTime())));
		return te;
	}

	/**
	 * Given PredictionValue from ConstituentSet::tideDerivative, fix up hydraulic current units and apply getDatum.
	 * @param pv partially prediction value
	 * @return finished prediction value
	 */
	private PredictionValue finishPredictionValue(PredictionValue pv)
	{
		if (AHTUnits.isHydraulicCurrent(pv.getUnits()))
			pv.setUnits(AHTUnits.flatten(pv.getUnits()));
		pv = pv.plus(m_tConst.getDatum());
		return pv;
	}

	private long llround(double x)
	{
		long ret;
		if (x < 0)
		{
			ret = (int) (x - .5);
		}
		else
		{
			ret = (int) (x + .5);
		}
		return ret;
	}
	/**
	 * @return the logger
	 */
	public Logger getLogger()
	{
		return logger;
	}

	/**
	 * @param logger the logger to set
	 */
	public void setLogger(Logger logger)
	{
		this.logger = logger;
	}

	/**
	 * @return the m_ID
	 */
	public int getID()
	{
		return m_ID;
	}

	/**
	 * @param m_ID the m_ID to set
	 */
	public void setID(int m_ID)
	{
		this.m_ID = m_ID;
	}

	/**
	 * @param m_strName the m_strName to set
	 */
	public void setName(String m_strName)
	{
		this.m_strName = m_strName;
	}

	/**
	 * @param m_TimeZone the m_TimeZone to set
	 */
	public void setTimeZone(String m_TimeZone)
	{
		this.m_TimeZone = m_TimeZone;
	}

	/**
	 * @param m_strNotes the m_strNotes to set
	 */
	public void setNotes(String m_strNotes)
	{
		this.m_strNotes = m_strNotes;
	}

	/**
	 * @param m_Coordinates the m_Coordinates to set
	 */
	public void setCoordinates(Coordinates m_Coordinates)
	{
		this.m_Coordinates = m_Coordinates;
	}

	/**
	 * @return the m_tConst
	 */
	public ConstituentSet getConst()
	{
		return m_tConst;
	}

	/**
	 * @param m_tConst the m_tConst to set
	 */
	public void setConst(ConstituentSet m_tConst)
	{
		this.m_tConst = m_tConst;
	}

	/**
	 * @return the minimumTimeOffset
	 */
	public Interval getMinimumTimeOffset()
	{
		return minimumTimeOffset;
	}

	/**
	 * @param minimumTimeOffset the minimumTimeOffset to set
	 */
	public void setMinimumTimeOffset(Interval minimumTimeOffset)
	{
		this.minimumTimeOffset = minimumTimeOffset;
	}

	/**
	 * @return the maximumTimeOffset
	 */
	public Interval getMaximumTimeOffset()
	{
		return maximumTimeOffset;
	}

	/**
	 * @param maximumTimeOffset the maximumTimeOffset to set
	 */
	public void setMaximumTimeOffset(Interval maximumTimeOffset)
	{
		this.maximumTimeOffset = maximumTimeOffset;
	}


}
