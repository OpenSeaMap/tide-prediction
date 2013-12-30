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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import ahdt.tides.base.AHTUnits.AHTidePredictionUnits;
import ahdt.tides.tcd.DbHeaderPublic;
import ahdt.tides.tcd.TideDB;
import ahdt.tides.tcd.TideDBException;
import ahdt.tides.tcd.TideRecord;

// Version string of the harmonics file.
/**
 * 
 * @author chas
 */
public class HarmonicsFile
{

	private transient Logger logger = Logger.getLogger(this.getClass().getName());

	private static String fileName = AHTideBaseStr.getString("HarmonicsFile.0"); //$NON-NLS-1$
	private static String version;
	private static TideDB tideDB;
	private static HarmonicsFile hFile;

	// libtcd is stateful and cannot handle multiple harmonics files
	// simultaneously. XTide currently has no need to open multiple
	// harmonics files simultaneously, so for now, the constructor will
	// barf if the attempt is made to have more than one instance at a
	// time. If this class is modified to deal with different
	// databases, that trap can be removed.
	private HarmonicsFile(String name) throws FileNotFoundException, TideDBException, IOException
	{
		HarmonicsFile.fileName = name;
		tideDB = new TideDB(name);
		DbHeaderPublic db = tideDB.getHeader().getPub();
		File file = new File(name);
		version = file.getName() + AHTideBaseStr.getString("HarmonicsFile.1") + db.getLastModified() + AHTideBaseStr.getString("HarmonicsFile.2") + db.getVersion(); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static HarmonicsFile getInstance(String fileName) throws FileNotFoundException, TideDBException, IOException
	{
		if ( !fileName.equals(HarmonicsFile.fileName))
		{
			hFile = new HarmonicsFile(fileName);
		}
		return hFile;
	}

	// Starting from the beginning of the file, allocate and return a
	// new StationRef for the next station. Each StationRef gets a
	// reference to the filename Dstr passed in the constructor, so
	// don't destroy it. Returns NULL on end of file.
	public StationRef getNextStationRef()
	{
		// TideStationHeader tsh = new TideStationHeader(tideDB, buffer, tideIndex,
		// recNum);
		// StationRef stationRef = new StationRef(fileName, recordNumber, fileName,
		// coordinates, fileName, isReferenceStation)
		throw new UnsupportedOperationException(AHTideBaseStr.getString("HarmonicsFile.3")); //$NON-NLS-1$
	}

	/**
	 * not used
	 * @return
	 */
	public List<StationRef> getStationList()
	{
		LinkedList<StationRef> list = new LinkedList<>();
		int i = 0;
		for (TideRecord t : tideDB.getRecords())
		{
			Coordinates c = null;
			if (t.getLat() == 0.0 && t.getLon() == 0.0)
			{
				c = new Coordinates();
			}
			else
				c = new Coordinates(t.getLat(), t.getLon());
			list.add(new StationRef(fileName, i++ , t.getName(), c, tideDB.getTzfile(t.getTzFile()), t.getRecordType() == TideRecord.EStatType.REFERENCE_STATION));
		}
		return list;
	}

	// Load the reffed station. This is allowed to invalidate the
	// iterator for getNextStationRef.
	public RefStation getStation(StationRef stationRef)
	{
		RefStation s = null;
		TideRecord rec = getTideRecord(stationRef.getRecordNumber());

		String note = rec.getNotes();
		CurrentBearing minCurrentBearing = new CurrentBearing(), maxCurrentBearing = new CurrentBearing();
		boolean isDegreesTrue = !(tideDB.findDirUnits(AHTideBaseStr.getString("HarmonicsFile.4")) == rec.getDirection_units()); //$NON-NLS-1$
			minCurrentBearing = new CurrentBearing(rec.getMinDirection());
			maxCurrentBearing = new CurrentBearing(rec.getMaxDirection());
		String name = rec.getName();
		if (rec.getLegalese() != 0)
		{
			name += AHTideBaseStr.getString("HarmonicsFile.5"); //$NON-NLS-1$
			name += /* get_legalese(rec.legalese) */(char) rec.getLegalese();
		}

		switch (rec.getRecordType())
		{
		case REFERENCE_STATION:
		{
			Deque<MetaField> metadata = new ArrayDeque<>();
			buildMetadata(stationRef, metadata, rec, AHTUnits.parse(tideDB.getLevelUnits(rec.getLevelUnits())), minCurrentBearing, maxCurrentBearing);
			// s = new Station(name, stationRef, getConstituents(rec, new SimpleOffsets()), note, minCurrentBearing, maxCurrentBearing, metadata);
		}
			break;

		case SUBORDINATE_STATION:
		{
			TideRecord referenceStationRec;
			assert (rec.getReferenceStation() >= 0);
			referenceStationRec = getTideRecord(rec.getReferenceStation());
			AHTidePredictionUnits refStationNativeUnits = AHTUnits.parse(tideDB.getLevelUnits(referenceStationRec.getLevelUnits()));
			Deque<MetaField> metadata = new ArrayDeque<>();
			buildMetadata(stationRef, metadata, rec, refStationNativeUnits, minCurrentBearing, maxCurrentBearing);

			NullableInterval tempFloodBegins = new NullableInterval(), tempEbbBegins = new NullableInterval();

			// For these, zero is not the same as null.
			if (rec.getFloodBegins() != TideDB.NULL_SLACK_OFFSET)
				tempFloodBegins = new NullableInterval(new Interval(TideDB.getTimeNeatStr(rec.getFloodBegins())));
			if (rec.getEbbBegins() != TideDB.NULL_SLACK_OFFSET)
				tempEbbBegins = new NullableInterval(new Interval(TideDB.getTimeNeatStr(rec.getEbbBegins())));

			AHTidePredictionUnits lu = levelAddUnits(rec);
			HairyOffsets ho = new HairyOffsets(new SimpleOffsets(new Interval(TideDB.getTimeNeatStr(rec.getMaxTimeAdd())), new PredictionValue(lu,
					rec.getMaxLevelAdd()), rec.getMaxLevelMultiply()), new SimpleOffsets(new Interval(TideDB.getTimeNeatStr(rec.getMinTimeAdd())), new PredictionValue(
					lu, rec.getMinLevelAdd()), rec.getMinLevelMultiply()), tempFloodBegins, tempEbbBegins);

			// If the offsets can be reduced to simple, then we can adjust
			// the constituents and be done with it. However, in the case
			// of hydraulic currents, level multipliers cannot be treated as
			// simple and applied to the constants because of the square
			// root operation--i.e., it's nonlinear.
			/*
						SimpleOffsets so = new SimpleOffsets();
						if (( !AHTUnits.isHydraulicCurrent(refStationNativeUnits) || ho.getMaxLevelMultiply() == 1.0) && ho.trySimplify(so))

							// Can simplify.
			//				s = new Station(name, stationRef, getConstituents(referenceStationRec, so), note, minCurrentBearing, maxCurrentBearing, metadata);

						else
							// Cannot simplify.
							s = new SubordinateStation(name, stationRef, getConstituents(referenceStationRec, new SimpleOffsets()), note, minCurrentBearing, maxCurrentBearing,
									metadata, ho);

					*/}
			break;

		default:
			assert (false);
		}

		/*
		// If this sanity check is deferred until Graph::draw it has
		// unhealthy consequences (clock windows can be killed while only
		// partially constructed; graph windows can double-barf due to
		// events queued on the X server).
		if (s.getMinLevel().gte(s.maxLevel()))
			// Global.barf(Error.ABSURD_OFFSETS, s.getName());
			throw new RuntimeException("Absurd offsets: " + s.getName());
		*/
		return s;
	}

	public String getVersion()
	{
		return version;
	}

	private TideRecord getTideRecord(int recordNumber)
	{
		return null;
		/*
		try
		{
		//			return tideDB.readTideRecord(recordNumber);
		}
		catch (TideDBException ex)
		{
			logger.log(java.util.logging.Level.SEVERE, "Error reading tide record", ex);
			return null;
		}
		*/
		/*
		 * require(read_tide_record((NV_INT32) recordNumber, & rec) == (NV_INT32)
		 * recordNumber); if (Global { : } :settings["in"].c == 'y' &&
		 * rec.header.record_type == REFERENCE_STATION) infer_constituents(& rec);
		 */
	}

	// refStationNativeUnits: determination of hydraulic vs. regular
	// current is made based on units at reference station. Units are
	// flattened for levelAdd offsets.
	private void buildMetadata(StationRef sr, Deque<MetaField> metadata, TideRecord rec, AHTidePredictionUnits refStationNativeUnits, CurrentBearing minCurrentBearing,
			CurrentBearing maxCurrentBearing)
	{
		String tmpbuf;

		metadata.push(new MetaField(AHTideBaseStr.getString("HarmonicsFile.6"), sr.getName())); //$NON-NLS-1$
		metadata.push(new MetaField(AHTideBaseStr.getString("HarmonicsFile.7"), sr.getHarmonicsFileName())); //$NON-NLS-1$
		if (rec.getLegalese() != 0)
		{
			metadata.push(new MetaField(AHTideBaseStr.getString("HarmonicsFile.8"), tideDB.getLegalese(rec.getLegalese()))); //$NON-NLS-1$
		}
		if ( !rec.getStationIdContext().equals(AHTideBaseStr.getString("HarmonicsFile.9"))) //$NON-NLS-1$
		{
			metadata.push(new MetaField(AHTideBaseStr.getString("HarmonicsFile.10"), rec.getStationIdContext())); //$NON-NLS-1$
		}
		if ( !rec.getStationId().equals(AHTideBaseStr.getString("HarmonicsFile.11"))) //$NON-NLS-1$
		{
			metadata.push(new MetaField(AHTideBaseStr.getString("HarmonicsFile.12"), rec.getStationId())); //$NON-NLS-1$
		}
		if (rec.getDateImported() != 0)
		{
			metadata.push(new MetaField(AHTideBaseStr.getString("HarmonicsFile.13"), tideDB.getDateStr(rec.getDateImported()))); //$NON-NLS-1$
		}
		tmpbuf = sr.getCoordinates().print();
		metadata.push(new MetaField(AHTideBaseStr.getString("HarmonicsFile.14"), tmpbuf)); //$NON-NLS-1$
		metadata.push(new MetaField(AHTideBaseStr.getString("HarmonicsFile.15"), tideDB.getCountryName(rec.getCountry()))); //$NON-NLS-1$
		metadata.push(new MetaField(AHTideBaseStr.getString("HarmonicsFile.16"), sr.getTimeZone())); //$NON-NLS-1$
		metadata.push(new MetaField(AHTideBaseStr.getString("HarmonicsFile.17"), refStationNativeUnits.getLongName())); //$NON-NLS-1$
		if ( !(maxCurrentBearing.isNull()))
		{
			tmpbuf = maxCurrentBearing.print();
			metadata.push(new MetaField(AHTideBaseStr.getString("HarmonicsFile.18"), tmpbuf)); //$NON-NLS-1$
		}
		if ( !(minCurrentBearing.isNull()))
		{
			tmpbuf = minCurrentBearing.print();
			metadata.push(new MetaField(AHTideBaseStr.getString("HarmonicsFile.19"), tmpbuf)); //$NON-NLS-1$
		}
		if ( !rec.getSource().equals(AHTideBaseStr.getString("HarmonicsFile.20"))) //$NON-NLS-1$
		{
			metadata.push(new MetaField(AHTideBaseStr.getString("HarmonicsFile.21"), rec.getSource())); //$NON-NLS-1$
		}
		metadata.push(new MetaField(AHTideBaseStr.getString("HarmonicsFile.22"), tideDB.getRestriction(rec.getRestriction()))); //$NON-NLS-1$
		if ( !rec.getComments().equals(AHTideBaseStr.getString("HarmonicsFile.23"))) //$NON-NLS-1$
		{
			metadata.push(new MetaField(AHTideBaseStr.getString("HarmonicsFile.24"), rec.getComments())); //$NON-NLS-1$
		}
		if ( !rec.getNotes().equals(AHTideBaseStr.getString("HarmonicsFile.25"))) //$NON-NLS-1$
		{
			metadata.push(new MetaField(AHTideBaseStr.getString("HarmonicsFile.26"), rec.getNotes())); //$NON-NLS-1$
		}
		parse_xfields(metadata, rec.getXfields());

		switch (rec.getRecordType())
		{
		case REFERENCE_STATION:
			metadata.push(new MetaField(AHTideBaseStr.getString("HarmonicsFile.27"), //$NON-NLS-1$
					(AHTUnits.isCurrent(refStationNativeUnits) ? (AHTUnits.isHydraulicCurrent(refStationNativeUnits) ? AHTideBaseStr.getString("HarmonicsFile.28") //$NON-NLS-1$
							: AHTideBaseStr.getString("HarmonicsFile.29")) : AHTideBaseStr.getString("HarmonicsFile.30")))); //$NON-NLS-1$ //$NON-NLS-2$
			metadata.push(new MetaField(AHTideBaseStr.getString("HarmonicsFile.31"), TideDB.getTimeNeatStr(rec.getZoneOffset()))); //$NON-NLS-1$
			if ( !AHTUnits.isCurrent(refStationNativeUnits))
				metadata.push(new MetaField(AHTideBaseStr.getString("HarmonicsFile.32"), tideDB.getDatum(rec.getDatum()))); //$NON-NLS-1$
			if (rec.getMonthsOnStation() != 0)
			{
				metadata.push(new MetaField(AHTideBaseStr.getString("HarmonicsFile.33"), String.format(AHTideBaseStr.getString("HarmonicsFile.34"), rec.getMonthsOnStation()))); //$NON-NLS-1$ //$NON-NLS-2$
			}
			if (rec.getLastDateOnStation() != 0)
			{
				metadata.push(new MetaField(AHTideBaseStr.getString("HarmonicsFile.35"), tideDB.getDateStr(rec.getLastDateOnStation()))); //$NON-NLS-1$
			}
			if (rec.getExpirationDate() != 0)
			{
				metadata.push(new MetaField(AHTideBaseStr.getString("HarmonicsFile.36"), tideDB.getDateStr(rec.getExpirationDate()))); //$NON-NLS-1$
			}
			metadata.push(new MetaField(AHTideBaseStr.getString("HarmonicsFile.37"), String.format(AHTideBaseStr.getString("HarmonicsFile.34"), rec.getConfidence()))); //$NON-NLS-1$ //$NON-NLS-2$
			break;

		case SUBORDINATE_STATION:
			metadata.push(new MetaField(AHTideBaseStr.getString("HarmonicsFile.39"), //$NON-NLS-1$
					(AHTUnits.isCurrent(refStationNativeUnits) ? (AHTUnits.isHydraulicCurrent(refStationNativeUnits) ? AHTideBaseStr.getString("HarmonicsFile.40") //$NON-NLS-1$
							: AHTideBaseStr.getString("HarmonicsFile.41")) : AHTideBaseStr.getString("HarmonicsFile.42")))); //$NON-NLS-1$ //$NON-NLS-2$
			// metadata.push(new MetaField("Reference", tideDB.getStation(rec.getReferenceStation())));
			// appendOffsetsMetadata(metadata, rec);
			break;

		default:
			assert (false);
		}
	}

	// Analogous to getDatum, levelAdds for hydraulic currents are always in
	// knots not knots^2. The database can specify either knots or
	// knots^2 in the sub station record, but there is only one sensible
	// interpretation.
	private AHTidePredictionUnits levelAddUnits(TideRecord rec)
	{
		return AHTUnits.flatten(AHTUnits.parse(tideDB.getLevelUnits(rec.getLevelUnits())));
	}

	private void parse_xfields(Deque<MetaField> metadata, String xfields)
	{
		assert (xfields.length() != 0);
		String[] x = xfields.split(AHTideBaseStr.getString("AHTides.NewLine")); //$NON-NLS-1$
		String name = null, value = null;
		for (String linebuf : x)
		{
			if (linebuf.charAt(0) == ' ')
			{
				if (name != null)
				{
					linebuf = linebuf.substring(1);
					value += '\n';
					value += linebuf;
				}
			}
			else
			{
				if (name != null)
				{
					metadata.push(new MetaField(name, value));
					name = null;
					value = null;
				}
				int i = linebuf.indexOf(':');
				if (i > 0)
				{
					name = linebuf;
					name = name.substring(0, i);
					value = String.format(AHTideBaseStr.getString("HarmonicsFile.34"), (i + 1)); //$NON-NLS-1$
				}
			}
		}
		if (name != null)
			metadata.push(new MetaField(name, value));
	}
}
