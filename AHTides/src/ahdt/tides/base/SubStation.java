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
import ahdt.tides.tcd.TideRecord;

/**
 * The SubStation is a station which calculates its data by the data of the reference station 
 * and afterwards applies some corrections to these data
 * 
 * @author humbach
 */
public class SubStation extends Station
{
	private HairyOffsets m_tHOffs;
	AHTimestamp uncleftt, uncrightt, subleftt = new AHTimestamp(), subrightt;
	PredictionValue uncleftp, uncrightp, subleftp, subrightp;
	private AHTidePredictionUnits cacheUnits;

	/*
	public SubStation(String name, StationRef stationRef, ConstituentSet constituents, String note, CurrentBearing minCurrentBearing,
			CurrentBearing maxCurrentBearing, Deque<MetaField> metadata, HairyOffsets offsets)
	{
		this.offsets = offsets;

		minimumTimeOffset = new Interval(offsets.getMaxTimeAdd());
		maximumTimeOffset = new Interval(offsets.getMaxTimeAdd());
		if (offsets.getMinTimeAdd().lt(minimumTimeOffset))
		{
			minimumTimeOffset = new Interval(offsets.getMinTimeAdd());
		}
		if (offsets.getMinTimeAdd().gt(maximumTimeOffset))
		{
			maximumTimeOffset = new Interval(offsets.getMinTimeAdd());
		}
		if (m_bIsCurrent)
		{
			if ( !offsets.getFloodBegins().isNull())
			{
				if (offsets.getFloodBegins().getInterval().lt(minimumTimeOffset))
				{
					minimumTimeOffset = new Interval(offsets.getFloodBegins().getInterval());
				}
				if (offsets.getFloodBegins().getInterval().gt(maximumTimeOffset))
				{
					maximumTimeOffset = new Interval(offsets.getFloodBegins().getInterval());
				}
			}
			if ( !offsets.getEbbBegins().isNull())
			{
				if (offsets.getEbbBegins().getInterval().lt(minimumTimeOffset))
				{
					minimumTimeOffset = new Interval(offsets.getEbbBegins().getInterval());
				}
				if (offsets.getEbbBegins().getInterval().gt(maximumTimeOffset))
				{
					maximumTimeOffset = new Interval(offsets.getEbbBegins().getInterval());
				}
			}
		}

		assert (m_bIsCurrent || (offsets.getFloodBegins().isNull() && offsets.getEbbBegins().isNull()));
	}
	*/

	/**
	 * constructs a SubStation from a TideDB record
	 * 
	 * @param tRefRec is the TideRecord whose data are to be loaded into the station object as reference station
	 * @param tRec is the TideRecord whose data are to be loaded into the station object as corrections to the reference station
	 */
	public SubStation(TideRecord tRec, TideRecord tRefRec)
	{
		super(tRec);
	}
	
	/**
	 * Sets the hairyOffsets of this Station and does some evaluation
	 * 
	 * @param HairyOffsets tOff
	 */
	public void ApplyHairyOffsets(HairyOffsets tOff)
	{
		m_tHOffs = tOff;

		minimumTimeOffset = new Interval(m_tHOffs.getMaxTimeAdd());
		maximumTimeOffset = new Interval(m_tHOffs.getMaxTimeAdd());
		if (m_tHOffs.getMinTimeAdd().lt(minimumTimeOffset))
		{
			minimumTimeOffset = new Interval(m_tHOffs.getMinTimeAdd());
		}
		if (m_tHOffs.getMinTimeAdd().gt(maximumTimeOffset))
		{
			maximumTimeOffset = new Interval(m_tHOffs.getMinTimeAdd());
		}
		if (m_bIsCurrent)
		{
			if ( !m_tHOffs.getFloodBegins().isNull())
			{
				if (m_tHOffs.getFloodBegins().getInterval().lt(minimumTimeOffset))
				{
					minimumTimeOffset = new Interval(m_tHOffs.getFloodBegins().getInterval());
				}
				if (m_tHOffs.getFloodBegins().getInterval().gt(maximumTimeOffset))
				{
					maximumTimeOffset = new Interval(m_tHOffs.getFloodBegins().getInterval());
				}
			}
			if ( !m_tHOffs.getEbbBegins().isNull())
			{
				if (m_tHOffs.getEbbBegins().getInterval().lt(minimumTimeOffset))
				{
					minimumTimeOffset = new Interval(m_tHOffs.getEbbBegins().getInterval());
				}
				if (m_tHOffs.getEbbBegins().getInterval().gt(maximumTimeOffset))
				{
					maximumTimeOffset = new Interval(m_tHOffs.getEbbBegins().getInterval());
				}
			}
		}

		assert (m_bIsCurrent || (m_tHOffs.getFloodBegins().isNull() && m_tHOffs.getEbbBegins().isNull()));
	}

	@Override
	public PredictionValue getMinLevel()
	{
		PredictionValue pv = new PredictionValue(super.getMinLevel());
		pv = pv.times(m_tHOffs.getMinLevelMultiply());
		pv.convertAndAdd(m_tHOffs.getMinLevelAdd());
		return pv;
	}

	@Override
	public PredictionValue getMaxLevel()
	{
		PredictionValue pv = new PredictionValue(super.getMaxLevel());
		pv = pv.times(m_tHOffs.getMaxLevelMultiply());
		pv.convertAndAdd(m_tHOffs.getMaxLevelAdd());
		return pv;
	}

	@Override
	public boolean isSubordinateStation()
	{
		return true;
	}

	@Override
	public boolean haveFloodBegins()
	{
		return !m_tHOffs.floodBegins.isNull();
	}

	@Override
	public boolean haveEbbBegins()
	{
		return !m_tHOffs.ebbBegins.isNull();
	}

	@Override
	public PredictionValue predictTideLevel(AHTimestamp predictTime)
	{
		// If units changed, trigger a refresh.
		if (cacheUnits != predictUnits())
		{
			subleftt.makeNull();
			cacheUnits = predictUnits();
		}

		// If we are outside the cached bracket, trigger a refresh.
		if ( !subleftt.isNull())
		{
			if (predictTime.lt(subleftt) || predictTime.gte(subrightt))
			{
				subleftt.makeNull();
			}
		}

		// Refresh cached bracket?
		if (subleftt.isNull())
		{

			// Since we have no way of knowing whether predictTime will follow
			// a contiguous range or jump around, there's no point preserving
			// the organizer beyond a single bracket. This puts some noise in
			// the results since everything will come back slightly different
			// even when predictTime just walks out of the bracket.
			TideEventsOrganizer organizer = new TideEventsOrganizer();
			logger.log(java.util.logging.Level.FINE, AHTideBaseStr.getString("SubStation.0")); //$NON-NLS-1$

			// Initialize organizer with a starting range. (Yes, this is
			// necessary.) Unlikely that a real station could go 48 hours
			// with no tide events, but detect and correct if it does.
			Interval delta = new Interval(Global.DAY);
			// logger.log(java.util.logging.Level.FINE, "day length = " +
			// Global.DAY.getSeconds());
			while (organizer.isEmpty())
			{
				super.predictTideEvents(predictTime.minus(delta), predictTime.plus(delta), organizer, RefStation.TideEventsFilter.KNOWN_TIDE_EVENTS);
				delta.timesEquals(2);
			}

			int count = 0;
			for (TideEvent te : organizer.values())
			{
				logger.log(java.util.logging.Level.FINE, String.format(
						AHTideBaseStr.getString("SubStation.1"), count++ , te.getTime().getSeconds(), te.getType().toString(), te.getLevel().getValue())); //$NON-NLS-1$
			}

			while (subleftt.isNull())
			{

				// If there are multiple events with the same eventTime, it
				// doesn't matter which one we pick, because they will all be
				// different next time anyway.

				// upper_bound: first element whose key is greater than predictTime.
				AHTimestamp right = organizer.getupperbound(predictTime);
				logger.log(java.util.logging.Level.FINE, AHTideBaseStr.getString("SubStation.2") + right.getSeconds()); //$NON-NLS-1$
				delta = new Interval(Global.DAY);
				while (right.equals(organizer.lastKey()))
				{
					// Need more future
					extendRange(organizer, Direction.FORWARD, delta, TideEventsFilter.KNOWN_TIDE_EVENTS);
					delta.timesEquals(2);
					right = organizer.getupperbound(predictTime);
					logger.log(java.util.logging.Level.FINE, String.format(AHTideBaseStr.getString("SubStation.3"), right.getSeconds(), delta.getSeconds())); //$NON-NLS-1$
				}

				// lower_bound: first element whose key is not less than predictTime.
				AHTimestamp left = organizer.getLowerBound(predictTime);
				logger.log(java.util.logging.Level.FINE, AHTideBaseStr.getString("SubStation.4") + left.getSeconds()); //$NON-NLS-1$
				// If upper bound existed, this must also exist.
				// assert (left != organizer.end());
				// But what we usually want is the previous one.
				if (left.gt(predictTime))
				{
					boolean recycle = false;
					delta = new Interval(Global.DAY);
					while (left.equals(organizer.firstKey()))
					{
						// Need more past
						extendRange(organizer, Direction.BACKWARD, delta, TideEventsFilter.KNOWN_TIDE_EVENTS);
						delta.timesEquals(2);
						left = new AHTimestamp(organizer.getLowerBound(predictTime));
						recycle = true;
						logger.log(java.util.logging.Level.FINE, String.format(AHTideBaseStr.getString("SubStation.5"), left.getSeconds(), delta.getSeconds())); //$NON-NLS-1$
					}

					// If the bracket was skewed way off, it's possible that
					// extending the range backward could change the result for
					// right.
					if (recycle)
					{
						continue;
					}

					left = new AHTimestamp(organizer.getPrevious(left));
				}

				// Populate the cached bracket.
				subleftt = new AHTimestamp(organizer.get(left).getTime());
				subleftp = new PredictionValue(organizer.get(left).getLevel());
				subrightt = new AHTimestamp(organizer.get(right).getTime());
				subrightp = new PredictionValue(organizer.get(right).getLevel());
				uncleftt = new AHTimestamp(organizer.get(left).getUncorrectedEventTime());
				uncleftp = new PredictionValue(organizer.get(left).getUncorrectedEventLevel());
				uncrightt = new AHTimestamp(organizer.get(right).getUncorrectedEventTime());
				uncrightp = new PredictionValue(organizer.get(right).getUncorrectedEventLevel());
			}
			logger.log(java.util.logging.Level.FINE, AHTideBaseStr.getString("SubStation.6") + subleftt.getSeconds()); //$NON-NLS-1$
			logger.log(java.util.logging.Level.FINE, AHTideBaseStr.getString("SubStation.7") + subleftp.getValue()); //$NON-NLS-1$
			logger.log(java.util.logging.Level.FINE, AHTideBaseStr.getString("SubStation.8") + subrightt.getSeconds()); //$NON-NLS-1$
			logger.log(java.util.logging.Level.FINE, AHTideBaseStr.getString("SubStation.9") + subrightp.getValue()); //$NON-NLS-1$
			logger.log(java.util.logging.Level.FINE, AHTideBaseStr.getString("AHTides.NewLine") + uncleftt.getSeconds()); //$NON-NLS-1$
			logger.log(java.util.logging.Level.FINE, AHTideBaseStr.getString("SubStation.11") + uncleftp.getValue()); // wrong //$NON-NLS-1$
			logger.log(java.util.logging.Level.FINE, AHTideBaseStr.getString("SubStation.12") + uncrightt.getSeconds()); // wrong //$NON-NLS-1$
			logger.log(java.util.logging.Level.FINE, AHTideBaseStr.getString("SubStation.13") + uncrightp.getValue()); // wrong //$NON-NLS-1$
		}
		assert (subleftt.lte(predictTime) && predictTime.lt(subrightt));

		// All manner of pathologies are possible. We might have skipped
		// over some conflicting events with the same eventTime. uncleftt
		// might be later than uncrightt. The left and right events might
		// even be the same type. Doesn't matter much to us at this point.
		// The math is robust in any case and people don't want assertion
		// failures every time a subordinate station experiences a time
		// warp. It comes with the territory.

		// The only case that really needs fixing is the one where we divide
		// by zero. If the uncorrected prediction values were the same,
		// make a straight line between the corrected values, whatever they
		// are. (Probably they are also the same, but you never know.)
		if (uncrightp.equals(uncleftp))
		{
			return subleftp.plus(subrightp.minus(subleftp)).times(predictTime.minus(subleftt).divide(subrightt.minus(subleftt)));
		}

		// Otherwise, map the time in and map the pv out.
		Interval term1 = subrightt.minus(subleftt);
		Interval term2 = predictTime.minus(subleftt);
		double term3 = term2.divide(term1);
		Interval term4 = uncrightt.minus(uncleftt);
		Interval term5 = term4.multiply(term3);
		AHTimestamp t = uncleftt.plus(uncrightt.minus(uncleftt).multiply(predictTime.minus(subleftt).divide(subrightt.minus(subleftt))));
		// PredictionValue term1 = uncrightp - uncleftp;
		// PredictionValue term2 = (Station::predictTideLevel(t) - uncleftp);
		// double term3 = term2 / term1;
		// PredictionValue term4 = subrightp - subleftp;
		// PredictionValue term5 = term4 * term3;
		// PredictionValue final = subleftp + term5;
		// PredictionValue term1 = uncrightp.minus(uncleftp);
		// PredictionValue term2 = super.predictTideLevel(t).minus(uncleftp);
		// double term3 = term2.divide(term1);
		// PredictionValue term4 = subrightp.minus(subleftp);
		// PredictionValue term5 = term4.times(term3);
		// PredictionValue finl = subleftp.plus(term5);
		return subleftp.plus(subrightp.minus(subleftp).times(super.predictTideLevel(t).minus(uncleftp).divide(uncrightp.minus(uncleftp))));
	}

	// All the nullification in this method serves to guarantee that we
	// don't ever use garbage values in predictTideLevel. Try to use a
	// null uncorrectedEventLevel for anything and foom, assertion
	// failure. In addition, the Calendar constructor requires that
	// uncorrectedEventTime be null if it is not applicable.
	@Override
	protected TideEvent finishTideEvent(TideEvent te)
	{
		if (m_bIsCurrent)
			te.makeCurrent();
		if (te.isSunMoonEvent())
		{
			te.getLevel().makeNull();
			te.getUncorrectedEventTime().makeNull();
			te.getUncorrectedEventLevel().makeNull();
		}
		else
		{
			switch (te.getType())
			{
			case RAWREADING:
				te.setLevel(new PredictionValue(predictTideLevel(te.getTime())));
				te.getUncorrectedEventTime().makeNull();
				te.getUncorrectedEventLevel().makeNull();
				break;
			case MAX:
				te.setUncorrectedEventTime(te.getTime());
				te.setLevel(new PredictionValue(super.predictTideLevel(te.getTime())));
				te.setUncorrectedEventLevel(te.getLevel());
				if (te.isMinCurrentEvent())
				{
					// Handling of min currents is questionable; see http://www.flaterco.com/xtide/mincurrents.html
					if (m_tHOffs.getEbbBegins().isNull())
					{
						te.getTime().plusEquals(m_tHOffs.getMinTimeAdd());
					}
					else
					{
						te.getTime().plusEquals(new Interval(m_tHOffs.getEbbBegins()));
					}
					te.getLevel().multiply(m_tHOffs.getMinLevelMultiply());
					te.getLevel().convertAndAdd(m_tHOffs.getMinLevelAdd());
				}
				else
				{
					te.getTime().plusEquals(m_tHOffs.getMaxTimeAdd());
					te.getLevel().multiply(m_tHOffs.getMaxLevelMultiply());
					te.getLevel().convertAndAdd(m_tHOffs.getMaxLevelAdd());
				}
				logger
						.log(
								java.util.logging.Level.FINE,
								AHTideBaseStr.getString("SubStation.14") + te.getTime().getSeconds() + AHTideBaseStr.getString("SubStation.15") + te.getLevel().getValue()); //$NON-NLS-1$ //$NON-NLS-2$
				break;
			case MIN:
				te.setUncorrectedEventTime(te.getTime());
				te.setLevel(new PredictionValue(super.predictTideLevel(te.getTime())));
				te.setUncorrectedEventLevel(te.getLevel());
				if (te.isMinCurrentEvent())
				{
					// Handling of min currents is questionable; see http://www.flaterco.com/xtide/mincurrents.html
					if (m_tHOffs.getFloodBegins().isNull())
					{
						te.getTime().plusEquals(m_tHOffs.getMaxTimeAdd());
					}
					else
					{
						te.getTime().plusEquals(new Interval(m_tHOffs.getFloodBegins()));
					}
					te.getLevel().multiply(m_tHOffs.getMaxLevelMultiply());
					te.getLevel().convertAndAdd(m_tHOffs.getMaxLevelAdd());
				}
				else
				{
					te.getTime().plusEquals(m_tHOffs.getMinTimeAdd());
					te.getLevel().multiply(m_tHOffs.getMinLevelMultiply());
					te.getLevel().convertAndAdd(m_tHOffs.getMinLevelAdd());
				}
				logger
						.log(
								java.util.logging.Level.FINE,
								AHTideBaseStr.getString("SubStation.16") + te.getTime().getSeconds() + AHTideBaseStr.getString("SubStation.17") + te.getLevel().getValue()); //$NON-NLS-1$ //$NON-NLS-2$
				break;
			case SLACKRISE:
				if (haveFloodBegins())
				{
					te.setUncorrectedEventTime(te.getTime());
					te.setLevel(new PredictionValue(super.predictTideLevel(te.getTime())));
					te.setUncorrectedEventLevel(te.getLevel());
					te.getTime().plusEquals(new Interval(m_tHOffs.getFloodBegins()));
				}
				else
				{
					te.getUncorrectedEventTime().makeNull();
					te.getUncorrectedEventLevel().makeNull();
					// eventTime was fixed in
					// findInterpolatedSubstationMarkCrossing (so hopefully this will be zero)
					te.setLevel(new PredictionValue(predictTideLevel(te.getTime())));
				}
				logger
						.log(
								java.util.logging.Level.FINE,
								AHTideBaseStr.getString("SubStation.18") + te.getTime().getSeconds() + AHTideBaseStr.getString("SubStation.19") + te.getLevel().getValue()); //$NON-NLS-1$ //$NON-NLS-2$
				break;
			case SLACKFALL:
				if (haveEbbBegins())
				{
					te.setUncorrectedEventTime(te.getTime());
					te.setLevel(new PredictionValue(super.predictTideLevel(te.getTime())));
					te.setUncorrectedEventLevel(te.getLevel());
					te.getTime().plusEquals(new Interval(m_tHOffs.getEbbBegins()));
				}
				else
				{
					te.getUncorrectedEventTime().makeNull();
					te.getUncorrectedEventLevel().makeNull();
					// eventTime was fixed in
					// findInterpolatedSubstationMarkCrossing (so hopefully this will be zero)
					te.setLevel(new PredictionValue(predictTideLevel(te.getTime())));
				}
				logger
						.log(
								java.util.logging.Level.FINE,
								AHTideBaseStr.getString("SubStation.20") + te.getTime().getSeconds() + AHTideBaseStr.getString("SubStation.21") + te.getLevel().getValue()); //$NON-NLS-1$ //$NON-NLS-2$
				break;
			case MARKRISE:
			case MARKFALL:
				te.getUncorrectedEventTime().makeNull();
				te.getUncorrectedEventLevel().makeNull();
				// eventTime was fixed in
				// Station::findInterpolatedSubstationMarkCrossing
				te.setLevel(new PredictionValue(predictTideLevel(te.getTime())));
				break;
			default:
				assert (false);
			}
		}
		return te;
	}

	@Override
	public void predictTideEvents(AHTimestamp startTime, AHTimestamp endTime, TideEventsOrganizer organizer, TideEventsFilter filter)
	{

		super.predictTideEvents(startTime, endTime, organizer, filter);
		if (filter == TideEventsFilter.NO_FILTER && ( !markLevel.isNull() || !haveFloodBegins() || !haveEbbBegins()))
		{
			addInterpolatedSubstationMarkCrossingEvents(startTime, endTime, organizer);
		}
	}

	public MarkCrossing findInterpolatedSubstationMarkCrossing(TideEvent tideEvent1, TideEvent tideEvent2, PredictionValue marklev)
	{
		AHTimestamp eventTime = new AHTimestamp();
		MarkCrossing returnVal = new MarkCrossing();

		// Toss any brackets in which reverse interpolation will blow up.
		if (tideEvent2.getLevel() != tideEvent1.getLevel())
		{

			// This time we map the pv in and map the time out.
			MarkCrossing mc = findSimpleMarkCrossing(
					tideEvent1.getUncorrectedEventTime(),
					tideEvent2.getUncorrectedEventTime(),
					tideEvent1.getUncorrectedEventLevel().plus(tideEvent2.getUncorrectedEventLevel().minus(tideEvent1.getUncorrectedEventLevel()))
							.times(marklev.minus(tideEvent1.getLevel()).divide(tideEvent2.getLevel().minus(tideEvent1.getLevel()))));
			eventTime = mc.getT();
			returnVal.setRising(mc.isRising());

			if ( !(eventTime.isNull()))
			{
				// eventTime = tideEvent1.eventTime +
				// (tideEvent2.eventTime - tideEvent1.eventTime) *
				// ((eventTime - tideEvent1.uncorrectedEventTime) /
				// (tideEvent2.uncorrectedEventTime - tideEvent1.uncorrectedEventTime)
				eventTime = tideEvent1.getTime().plus(
						tideEvent2
								.getTime()
								.minus(tideEvent1.getTime())
								.multiply(
										eventTime.minus(tideEvent1.getUncorrectedEventTime()).divide(
												tideEvent2.getUncorrectedEventTime().minus(tideEvent1.getUncorrectedEventTime()))));
			}
			returnVal.setT(eventTime);
		}

		return returnVal;
	}

	private void addInterpolatedSubstationMarkCrossingEvents(AHTimestamp startTime, AHTimestamp endTime, TideEventsOrganizer organizer)
	{
//		boolean isRising;

		// Problem #1. Need to extract the set of relevant events.

		// (Even if the order of invocations in predictTideEvents were
		// changed to plus the sun and moon events afterward, those events as
		// well as future and past ranges could be hanging around from
		// previous calls.)
		// TideEventsIterator it = organizer.lower_bound(startTime);
		// TideEventsIterator stop = organizer.lower_bound(endTime);
		// while (it != stop) {
		// TideEvent & te = it -> second;
		// switch (te.eventType) {
		// case TideEvent:
		// :
		// max:
		// case
		// TideEvent:
		// :
		// min:
		// relevantEvents.plus(te);
		// break;
		// case TideEvent:
		// :
		// slackrise:
		// if (haveFloodBegins()) {
		// relevantEvents.plus(te);
		// }
		// break;
		// case TideEvent:
		// :
		// slackfall:
		// if (haveEbbBegins()) {
		// relevantEvents.plus(te);
		// }
		// break;
		// default:
		// ;
		// }
		// ++it;
		// }
		//
		TideEventsOrganizer relevantEvents = new TideEventsOrganizer();
		for (AHTimestamp it : organizer.subMap(organizer.getLowerBound(startTime), organizer.getLowerBound(endTime)).keySet())
		{
			TideEvent te = organizer.get(it);
			switch (te.getType())
			{
			case MAX:
			case MIN:
				relevantEvents.add(te);
				break;
			case SLACKRISE:
				if (haveFloodBegins())
				{
					relevantEvents.add(te);
				}
				break;
			case SLACKFALL:
				if (haveEbbBegins())
				{
					relevantEvents.add(te);
				}
				break;
			default:

			}
		}

		// // Problem #2. Need to initialize it if empty.
		// Interval delta;
		// for (delta = Global: {
		//
		// }
		// :day;
		// relevantEvents.empty();
		// delta *= 2
		// U)
		// predictTideEvents(startTime - delta,
		// endTime + delta,
		// relevantEvents,
		// knownTideEvents);
		//
		Interval delta = new Interval(Global.DAY);
		while (relevantEvents.isEmpty())
		{
			predictTideEvents(startTime.minus(delta), endTime.plus(delta), relevantEvents, TideEventsFilter.KNOWN_TIDE_EVENTS);
			delta.timesEquals(2);
		}
		// // Problem #3. We need to extend the range to be sure of getting
		// // the first and last mark crossing and slack.
		// for (delta = Global::day;relevantEvents.begin() -> second.eventTime >=
		// startTime;delta *= 2U)
		// extendRange(relevantEvents, backward, delta, knownTideEvents);
		// for (delta = Global::day;relevantEvents.rbegin() -> second.eventTime <
		// endTime;delta *= 2U)
		// extendRange(relevantEvents, forward, delta, knownTideEvents);
		//
		delta = new Interval(Global.DAY);
		while (relevantEvents.firstKey().gte(startTime))
		{
			extendRange(relevantEvents, Direction.BACKWARD, delta, TideEventsFilter.KNOWN_TIDE_EVENTS);
			delta.timesEquals(2);
		}
		delta = new Interval(Global.DAY);
		while (relevantEvents.lastKey().lt(endTime))
		{
			extendRange(relevantEvents, Direction.FORWARD, delta, TideEventsFilter.KNOWN_TIDE_EVENTS);
			delta.timesEquals(2);
		}
		// // OK great.
		// it = relevantEvents.begin();
		// TideEvent left_te = it -> second;
		// while (++it != relevantEvents.end()) {
		// TideEvent right_te = it -> second;
		//
		//
		TideEvent leftTe = relevantEvents.getMap().get(relevantEvents.firstKey());
		assert ( !leftTe.getUncorrectedEventTime().isNull());
		assert ( !leftTe.getUncorrectedEventLevel().isNull());

		TideEvent newTE = new TideEvent();

		for (TideEvent rightTe : relevantEvents.values())
		{
			assert ( !leftTe.getUncorrectedEventTime().isNull());
			assert ( !rightTe.getUncorrectedEventTime().isNull());
			assert ( !leftTe.getUncorrectedEventLevel().isNull());
			assert ( !rightTe.getUncorrectedEventLevel().isNull());

			if (rightTe != leftTe)
			{

				// // We have a bracket as used in
				// // SubordinateStation::predictTideLevel. However, it isn't
				// // necessarily a nice bracket for findMarkCrossing_Dairiki.
				// // findMarkCrossing_Dairiki should return null when there's no
				// // good answer.
				//
				// // Check for slacks, if applicable.
				// if (isCurrent && ((left_te.eventType == TideEvent:max &&
				// !haveEbbBegins()) ||
				// (left_te.eventType == TideEvent::min && !haveFloodBegins())
				// ) ) {
				// new_te.eventTime = findInterpolatedSubstationMarkCrossing
				// (left_te,right_te,PredictionValue(predictUnits(), 0.0),
				// isRising);
				// if (!(new_te.eventTime.isNull())) {
				// new_te.eventType = (isRising ? TideEvent::slackrise :
				// TideEvent::slackfall);
				// finishTideEvent(new_te);
				// }
				//
				if (m_bIsCurrent
						&& ((leftTe.getType() == TideEvent.EventType.MAX && !haveEbbBegins()) || (leftTe.getType() == TideEvent.EventType.MIN && !haveFloodBegins())))
				{
					MarkCrossing mc = findInterpolatedSubstationMarkCrossing(leftTe, rightTe, new PredictionValue(predictUnits(), 0.0));
					newTE.setTime(mc.getT());
					if (newTE.getTime().isNull())
					{
						newTE.setType(mc.isRising() ? TideEvent.EventType.SLACKRISE : TideEvent.EventType.SLACKFALL);
						finishTideEvent(newTE);
						if (newTE.getTime().gte(startTime) && newTE.getTime().lt(endTime))
						{
							organizer.add(newTE);
						}
					}
				}
				//
				// if (new_te.eventTime>=startTime&&new_te.eventTime< endTime )
				// organizer.plus (new_te);
				// }
				// }
				//
				// // Check for mark, if applicable.
				// if (!markLevel.isNull()) {
				// new_te.eventTime = findInterpolatedSubstationMarkCrossing (left_te,
				// right_te,
				// markLevel,
				// isRising);
				// if (!(new_te.eventTime.isNull())) {
				// new_te.eventType = (isRising ? TideEvent::markrise :
				// TideEvent::markfall);
				// finishTideEvent {
				// (new_te);
				// }
				// if (new_te.eventTime >= startTime && new_te.eventTime < endTime)
				// organizer.plus (new_te);
				// }
				// }
			}
			//
			// left_te = right_te;
			// }
			if ( !markLevel.isNull())
			{
				MarkCrossing mc = findInterpolatedSubstationMarkCrossing(leftTe, rightTe, new PredictionValue(markLevel));
				newTE.setTime(mc.getT());
				if ( !newTE.getTime().isNull())
				{
					newTE.setType(mc.isRising() ? TideEvent.EventType.MARKRISE : TideEvent.EventType.MARKFALL);
					finishTideEvent(newTE);
				}
				if (newTE.getTime().gte(startTime) && newTE.getTime().lt(endTime))
				{
					organizer.add(newTE);
				}
			}
			leftTe = rightTe;
		}
	}

}
