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

import java.util.Deque;
import java.util.logging.Logger;

import ahdt.tides.base.AHTUnits.AHTidePredictionUnits;
import ahdt.tides.tcd.TideRecord;

/**
 * 
 * @author chas
 */
/**
 * A Station is the object enabling calculations and predictions.
 * 
 * @author humbach
 *
 */
public class RefStation extends Station implements Cloneable
{
	private transient Logger logger = Logger.getLogger(this.getClass().getName());

	/**
	 * constructs a RefStation from a TideDB record
	 * 
	 * @param tRec is the TideRecord whose data are to be loaded into the station object
	 */
	public RefStation(TideRecord tRec)
	{
		super(tRec);
	}

	public void dump()
	{
		System.out.println(AHTideBaseStr.getString("RefStation.2") + getName()); //$NON-NLS-1$
		System.out.println(AHTideBaseStr.getString("RefStation.3") + getTimeZone()); //$NON-NLS-1$
		System.out.println(AHTideBaseStr.getString("RefStation.4") + getMinCurrentBearing().getDegrees()); //$NON-NLS-1$
		System.out.println(AHTideBaseStr.getString("RefStation.5") + getMaxCurrentBearing().getDegrees()); //$NON-NLS-1$
		System.out.println(AHTideBaseStr.getString("RefStation.6") + this.getName()); //$NON-NLS-1$
		System.out.println(AHTideBaseStr.getString("RefStation.7") + this.isIsCurrent()); //$NON-NLS-1$
		// stationRef.dump();
		m_tConst.dump();
		System.out.println(AHTideBaseStr.getString("RefStation.8")); //$NON-NLS-1$
		String metaFormatString = AHTideBaseStr.getString("RefStation.9"); //$NON-NLS-1$
		for (MetaField mf : m_tMetadata)
		{
			System.out.printf(metaFormatString, mf.getName(), mf.getValue());
		}
	}

	/**
	 * General method for generating output that fits into a String 
	 * (any form except PNG or X-Windows). Note that list mode is implemented as StationIndex::print.
	 */
		public String print(AHTimestamp startTime, AHTimestamp endTime, Constants.Mode mode, Constants.Format format)
	{
		return plainMode(startTime, endTime, format);
	}

	/**
		// iCalendar format output is actually produced by plainMode. From
		// an engineering perspective this makes perfect sense. But from a
		// usability perspective, iCalendar output is a calendar and ought
		// to appear in calendar mode. So calendarMode falls through to
		// plainMode when i format is chosen.
	**/
	public String plainMode(AHTimestamp startTime, AHTimestamp endTime, Constants.Format form)
	{
		// textBoilerplate(text_out, form);
		String textOut = new String();
		TideEventsOrganizer organizer = new TideEventsOrganizer();
		predictTideEvents(startTime, endTime, organizer, RefStation.TideEventsFilter.NO_FILTER);
		for (TideEvent t : organizer.values())
		{
			textOut += t.print(Constants.Mode.PLAIN, form, this) + AHTideBaseStr.getString("AHTides.NewLine"); //$NON-NLS-1$
		}
		return textOut;
	}

	/**
		// Change preferred units of length.
		// Default is as specified by settings.
		// Attempts to set same units are tolerated without complaint.
		// Attempts to set velocity units are punished.
		 * 
		 * @param units
		 */
	public void setUnits(AHTidePredictionUnits units)
	{
		throw new UnsupportedOperationException(AHTideBaseStr.getString("RefStation.11")); //$NON-NLS-1$
	}


	/**
	// Mathematical bounds (considerably wider than lowest/highest
	// astronomical tide)---used to set range in graphs.
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
	 *  Get heights or velocities.
	 *  
	 * @param predictTime
	 * @return
	 */
	public PredictionValue predictTideLevel(AHTimestamp predictTime)
	{
		// logger.log(java.util.logging.Level.FINE, "predictTime = " +
		// predictTime.getTime());
		return finishPredictionValue(m_tConst.tideDerivative(predictTime, 0));
	}

	/**
	 Get all tide events within a range of timestamps and add them to
	 the organizer. The range is >= startTime and < endTime. Because
	 predictions are done to plus or minus one minute, invoking this
	 multiple times with adjoining ranges could duplicate or delete
	 tide events falling right on the boundary. TideEventsOrganizer
	 should suppress the duplicates, but omissions will not be
	 detected.
	
	 Either settings or the filter arg can suppress sun and moon events.
	 * 
	 * @param startTime
	 * @param endTime
	 * @param organizer
	 */

	public void predictTideEvents(AHTimestamp startTime, AHTimestamp endTime, TideEventsOrganizer organizer)
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

	// Analogous, for raw readings.
	public void predictRawEvents(AHTimestamp startTime, AHTimestamp endTime, TideEventsOrganizer organizer)
	{
		throw new UnsupportedOperationException();
	}

	// Direction for extendRange.
	public enum Direction {
		FORWARD, BACKWARD
	};

	/**
	 * Add events to an organizer to extend its range in the specified direction by the specified interval. (Number of events is indeterminate.) 
	 * A safety margin is used to attempt to prevent tide events from falling through the cracks as discussed above predictTideEvents.
	 * 
	 * Either settings or the filter arg can suppress sun and moon events.
	 */
	public void extendRange(TideEventsOrganizer organizer, Direction direction, Interval howMuch, TideEventsFilter filter)
	{
		/*
		 * assert (howMuch > Global::zeroInterval); Timestamp startTime, endTime; if
		 * (direction == forward) { TideEventsReverseIterator it =
		 * organizer.rbegin(); assert (it != organizer.rend()); startTime =
		 * it->second.eventTime; endTime = startTime + howMuch; startTime -=
		 * Global::eventSafetyMargin; } else { TideEventsIterator it =
		 * organizer.begin(); assert (it != organizer.end()); endTime =
		 * it->second.eventTime; startTime = endTime - howMuch; endTime +=
		 * Global::eventSafetyMargin; } predictTideEvents (startTime, endTime,
		 * organizer, filter);
		 */
		assert (howMuch.gt(Global.ZERO_INTERVAL));
		AHTimestamp startTime = new AHTimestamp();
		AHTimestamp endTime = new AHTimestamp();
		if (direction == Direction.FORWARD)
		{
			TideEvent last = organizer.get(organizer.lastKey());
			startTime = last.getEventTime();
			endTime = startTime.plus(howMuch);
			startTime.minusEquals(Global.EVENT_SAFETY_MARGIN);
		}
		else
		{
			TideEvent first = organizer.get(organizer.firstKey());
			endTime = first.getEventTime();
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

	public NullablePredictionValue getMarkLevel()
	{
		return markLevel;
	}

	public void setMarkLevel(NullablePredictionValue markLevel)
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

	// Root finder.
	// * If tl >= tr, assertion failure.
	// * If tl and tr do not bracket a root, assertion failure.
	// * If a root exists exactly at tl or tr, assertion failure.

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
				dt = new Interval(Global.ZERO_INTERVAL); // Force bisection on first
																									// step
			else if (PredictionValue.abs(ft).gt(f_thresh) /*
																										 * not decreasing fast
																										 * enough
																										 */
					|| (ft.getValue() > 0.0 ? /* newton step would go outside bracket */
					(fp.lte(ft.divide(t.minus(tl).getSeconds()))) : (fp.lte(ft.negative().divide(tr.minus(t).getSeconds())))))
				dt = new Interval(Global.ZERO_INTERVAL); /* Force bisection */
			else
			{
				/* Attempt a newton step */
				assert (fp.getValue() != 0.0);
				// Here I actually do want to round away from zero.
				dt = new Interval(llround(ft.negative().divide(fp)));

				/*
				 * Since our goal specifically is to reduce our bracket size as quickly
				 * as possible (rather than getting as close to the zero as possible) we
				 * should ensure that we don't take steps which are too small. (We'd
				 * much rather step over the root than take a series of steps that
				 * approach the root rapidly but from only one side.)
				 */
				if (Interval.abs(dt).lt(Global.EVENT_PRECISION))
					dt = (ft.getValue() < 0.0 ? new Interval(Global.EVENT_PRECISION) : new Interval(Global.EVENT_PRECISION.negative()));

				t.plusEquals(dt);
				if (t.gte(tr) || t.lte(tl))
					dt = new Interval(Global.ZERO_INTERVAL); /*
																										 * Force bisection if
																										 * outside bracket
																										 */
				f_thresh = ft.abs().divide(2.0);
			}
			if (dt.equals(Global.ZERO_INTERVAL))
			{
				/* Newton step failed, do bisection */
				t = tl.plus(tr.minus(tl).divide(2));
				f_thresh = fr.gt(fl.negative()) ? new PredictionValue(fr) : new PredictionValue(fl.negative());
			}
			ft = functor.functor(t, 0, marklev).times(scale);
			if (ft.getValue() == 0.0)
				return t; /* Exact zero */
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

	// Find the marklev crossing in this bracket. Used for both
	// markLevel and slacks.
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
			return new MarkCrossing(new AHTimestamp(), isRising_out); // return null
																															// timestamp

		// We need || instead of && to set isRising_out correctly in the
		// case where there's a zero exactly at t1 or t2.
		isRising_out = (f1.getValue() < 0.0 || f2.getValue() > 0.0);
		if ( !isRising_out)
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

		return new MarkCrossing(new AHTimestamp(), isRising_out); // Don't have a
																														// bracket, return
																														// null timestamp.
	}

	/**
	 * Find the next maximum or minimum.
	 * eventTime and eventType are set to the next event (uncorrected time).
	 * Nothing else in tideEvent_out is changed.
	 * finishTideEvent must be called afterward.
	 * 
	 * @param t time 
	 * @return
	 */

	/*
	 * next_zero(time_t t, double (*f)(), double max_fp, double max_fpp) Find the
	 * next zero of the function f which occurs after time t. The arguments max_fp
	 * and max_fpp give the maximum possible magnitudes that the first and second
	 * derivative of f can achieve.
	 * 
	 * Algorithm: Our goal here is to bracket the next zero of f --- then we can
	 * use findZero() to quickly refine the root. So, we will step forward in time
	 * until the sign of f changes, at which point we know we have bracketed a
	 * root. The trick is to use large steps in our search, making sure the steps
	 * are not so large that we inadvertently step over more than one root.
	 * 
	 * The big trick, is that since the tides (and derivatives of the tides) are
	 * all just harmonic series's, it is easy to place absolute bounds on their
	 * values.
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
		logger.log(java.util.logging.Level.FINE, AHTideBaseStr.getString("RefStation.12") + t.getSeconds()); //$NON-NLS-1$
		TideEvent tideEvent_out = new TideEvent();
		Amplitude maxFp = m_tConst.tideDerivativeMax(2);
		Amplitude maxFpp = m_tConst.tideDerivativeMax(3);

		AHTimestamp tLeft, tRight;
		Interval step0, step1, step2;
		PredictionValue fLeft, dfLeft, fRight, junk = new PredictionValue();
		double scale = 1.0;

		tLeft = new AHTimestamp(t);

		/* If we start at a zero, step forward until we're past it. */
		while ((fLeft = new MaxMinZeroFn().functor(tLeft, 0, junk)).getValue() == 0.0)
			tLeft = tLeft.plus(Global.EVENT_PRECISION);

		if (fLeft.getValue() < 0.0)
		{
			tideEvent_out.setEventType(TideEvent.EventType.MIN);
		}
		else
		{
			tideEvent_out.setEventType(TideEvent.EventType.MAX);
			scale = -1.0;
			fLeft.negate();
		}

		logger.log(java.util.logging.Level.FINE, String.format(AHTideBaseStr.getString("RefStation.13"), fLeft.getValue())); //$NON-NLS-1$

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
																											 * No ridiculously small
																											 * steps
																											 */

			tRight = tLeft.plus(step0);

			logger.log(java.util.logging.Level.FINE,
					String.format(AHTideBaseStr.getString("RefStation.14"), step1.getSeconds(), step2.getSeconds(), dfLeft.getValue(), tRight.getSeconds())); //$NON-NLS-1$
			/*
			 * If we hit upon an exact zero, step right until we're off the zero. If
			 * the sign has changed, we are bracketing a desired root. If the sign
			 * hasn't changed, then the zero was at an inflection point (i.e. a
			 * double-zero to within Global::eventPrecision) and we want to ignore it.
			 */
			fRight = new MaxMinZeroFn().functor(tRight, 0, junk).times(scale);
			while (fRight.getValue() == 0.0)
			{
				tRight = tRight.plus(Global.EVENT_PRECISION);
				fRight = new MaxMinZeroFn().functor(tRight, 0, junk).times(scale);
			}

			if (fRight.getValue() > 0.0)
			{ /* Found a bracket */
				tideEvent_out.setEventTime(findZero(tLeft, tRight, new MaxMinZeroFn(), junk));
				return tideEvent_out;
			}

			tLeft = new AHTimestamp(tRight);
			fLeft = new PredictionValue(fRight);
		}
	}

	/**
	 * Wrapper for findMarkCrossing_Dairiki that does necessary compensations for getDatum, KnotsSquared, and units. Used for both markLevel and slacks.
	 */
	protected MarkCrossing findSimpleMarkCrossing(AHTimestamp t1, AHTimestamp t2, PredictionValue marklev)
	{
		// marklev must compensate for getDatum and KnotsSquared. See markZeroFn.
		// Units should already be comparable to getDatum.
		marklev.minusEquals(m_tConst.getDatum());
		// Correct knots / knots squared
		if (m_tConst.getPredictUnits() != marklev.getUnits())
		{
			marklev.setUnits(m_tConst.getPredictUnits());
		}

		return findMarkCrossing_Dairiki(t1, t2, marklev);
	}

	// Submethods of predictTideEvents.
	private void addSimpleTideEvents(AHTimestamp startTime, AHTimestamp endTime, TideEventsOrganizer organizer, TideEventsFilter filter)
	{
		boolean isRising = false;
		TideEvent te = new TideEvent();

		// loopTime is the "internal" timestamp for scanning the reference
		// station. The timestamps of each event get mangled for sub
		// stations.
		AHTimestamp loopTime = startTime.minus(maximumTimeOffset);
		AHTimestamp loopEndTime = endTime.minus(minimumTimeOffset);
		logger.log(java.util.logging.Level.FINE, String.format(AHTideBaseStr.getString("RefStation.15"), loopTime.getSeconds(), loopEndTime.getSeconds())); //$NON-NLS-1$

		// Patience... range is correctly enforced below.
		while (loopTime.lte(loopEndTime))
		{
			AHTimestamp previousLoopTime = new AHTimestamp(loopTime);

			// Get next max or min.
			te = nextMaxMin(loopTime);
			loopTime.setTime(te.getEventTime());
			finishTideEvent(te);
			if (te.getEventTime().gte(startTime) && te.getEventTime().lt(endTime))
			{
				organizer.add(te);
				logger.log(java.util.logging.Level.FINE,
						String.format(AHTideBaseStr.getString("RefStation.16"), te.getEventTime().getSeconds(), te.getEventType().ordinal(), te.getEventLevel().getValue())); //$NON-NLS-1$
			}

			// Check for slacks, if applicable. Skip the ones that need
			// interpolation; those are done in
			// SubordinateStation::addInterpolatedSubstationMarkCrossingEvents.
			if (filter != TideEventsFilter.MAX_MIN && m_bIsCurrent
					&& ((te.getEventType() == TideEvent.EventType.MAX && haveFloodBegins()) || (te.getEventType() == TideEvent.EventType.MIN && haveEbbBegins())))
			{
				te = new TideEvent(te);
				MarkCrossing mc = findSimpleMarkCrossing(previousLoopTime, loopTime, new PredictionValue(predictUnits(), 0.0));
				// te.setEventTime(mc.getT());
				te.setEventTime(new AHTimestamp(mc.getT()));
				isRising = mc.isRising();
				if ( !(te.getEventTime().isNull()))
				{
					te.setEventType(isRising ? TideEvent.EventType.SLACKRISE : TideEvent.EventType.SLACKFALL);
					finishTideEvent(te);
					if (te.getEventTime().compareTo(startTime) >= 0 && te.getEventTime().compareTo(endTime) < 0)
					{
						organizer.add(te);
					}
				}
			}

			// Check for mark, if applicable.
			if (( !isSubordinateStation()) && ( !markLevel.isNull()) && (filter == TideEventsFilter.NO_FILTER))
			{
				te = new TideEvent(te);
				MarkCrossing mc = findSimpleMarkCrossing(previousLoopTime, loopTime, markLevel.asPredictionValue());
				te.setEventTime(mc.getT());
				isRising = mc.isRising();
				if ( !(te.getEventTime().isNull()))
				{
					te.setEventType(isRising ? TideEvent.EventType.MARKRISE : TideEvent.EventType.MARKFALL);
					finishTideEvent(te);
				}

				if (te.getEventTime().compareTo(startTime) >= 0 && te.getEventTime().compareTo(endTime) < 0)
				{
					organizer.add(te);
				}
			}
		}
	}

	private void addSunMoonEvents(AHTimestamp startTime, AHTimestamp endTime, TideEventsOrganizer organizer)
	{
		logger.warning(AHTideBaseStr.getString("RefStation.17")); //$NON-NLS-1$
	}

	// Given eventTime and eventType, fill in other fields and possibly
	// apply corrections.
	protected void finishTideEvent(TideEvent te)
	{
		te.setIsCurrent(m_bIsCurrent);
		te.getUncorrectedEventTime().makeNull();
		te.getUncorrectedEventLevel().makeNull();
		if (te.isSunMoonEvent())
		{
			te.getEventLevel().makeNull();
		}
		else
		{
			te.setEventLevel(new NullablePredictionValue(parentPredictTideLevel(te.getEventTime())));
		}
	}

	// Given PredictionValue from ConstituentSet::tideDerivative, fix up
	// hydraulic current units and apply getDatum.
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
}
