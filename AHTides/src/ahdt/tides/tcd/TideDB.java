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
package ahdt.tides.tcd;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Scanner;
import java.util.logging.Logger;

import ahdt.tides.base.AHTUnits;
import ahdt.tides.base.AHTUnits.AHTidePredictionUnits;
import ahdt.tides.base.AHTideBaseStr;
import ahdt.tides.base.Coordinates;
import ahdt.tides.base.CurrentBearing;
import ahdt.tides.base.HairyOffsets;
import ahdt.tides.base.Interval;
import ahdt.tides.base.MetaField;
import ahdt.tides.base.NullableInterval;
import ahdt.tides.base.PredictionValue;
import ahdt.tides.base.RefStation;
import ahdt.tides.base.SimpleOffsets;
import ahdt.tides.base.Station;
import ahdt.tides.base.StationException;
import ahdt.tides.base.SubStation;
import ahdt.tides.base.XByteBuffer;
import ahdt.tides.tcd.TideRecord.EStatType;

/**
 * 
 * @author chas
 */
public class TideDB
{

	/*****************************************************************************
	 * \
	 * 
	 * DISTRIBUTION STATEMENT
	 * 
	 * This source file is unclassified, distribution unlimited, public domain. It is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
	 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
	 * 
	 * \
	 *****************************************************************************/

	/*
	 * Some of the following commentary is out of date. See the new documentation in libtcd.html.
	 */

	/*****************************************************************************
	 * \
	 * 
	 * Tide Constituent Database API
	 * 
	 * 
	 * Author : Jan C. Depner (depnerj@navo.navy.mil)
	 * 
	 * Date : 08/01/02 (First day of Micro$oft's "Licensing 6" policy - P.T. Barnum was right!!!)
	 * 
	 * Purpose : To replace the ASCII/XML formatted harmonic constituent data files, used in Dave Flater's (http://www.flaterco.com/xtide/) exceptionally fine
	 * open-source XTide program, with a fast, efficient binary format. In addition, we wanted to replace the Naval Oceanographic Office's
	 * (http://www.navo.navy.mil) antiquated ASCII format harmonic constituent data file due to problems with configuration management of the file. The resulting
	 * database will become a Navy OAML (Oceanographic and Atmospheric Master Library) standard harmonic tide constituent database.
	 * 
	 * Design : The following describes the database file and some of the rationale behind the design.
	 * 
	 * First question - Why didn't I use PostgreSQL or MySQL? Mostly portability. What? PostgreSQL runs on everything! Yes, but it doesn't come installed on
	 * everything. This would have meant that the poor, benighted Micro$oft borgs would have had to actually load a software package. In addition, the present
	 * harmonics/offset files only contain a total of 6,409 stations. It hardly seemed worth the effort (or overhead) of a fullblown RDBMS to handle this. Second
	 * question - Why binary and not ASCII or XML? This actually gets into philosophy. At NAVO we have used an ASCII harmonic constituent file for years (we were
	 * founded in 1830 and I think that that's when they designed the file). We have about fifty million copies floating around and each one is slightly
	 * different. Why? Because they're ASCII and everyone thinks they know what they're doing so they tend to modify the file. Same problem with XML, it's still
	 * ASCII. We wanted a file that people weren't going to mess with and that we could version control. We also wanted a file that was small and fast. This is
	 * very difficult to do with ASCII. The big slowdown with the old format was I/O and parsing. Third question - will it run on any OS? Hopefully, yes. After
	 * twenty-five years of working with low bidder systems I've worked on almost every OS known to man. Once you've been bitten by big endian vs little endian or
	 * IEEE floating point format vs VAX floating point format or byte addressable memory vs word addressable memory or 32 bit word vs 36 bit word vs 48 bit word
	 * vs 64 bit word sizes you get the message. All of the data in the file is stored either as ASCII text or scaled integers (32 bits or smaller), bit-packed
	 * and stuffed into an unsigned character buffer for I/O. No endian issues, no floating point issues, no word size issues, no memory mapping issues. I will be
	 * testing this on x86 Linux, HP-UX, and Micro$oft Windoze. By the time you read this it will be portable to those systems at least.
	 * 
	 * Now, on to the file layout. As much as I dislike ASCII it is occasionally handy to be able to see some information about a file without having to resort to
	 * a special purpose program. With that in mind I made the first part of the header of the file ASCII. The following is an example of the ASCII portion of the
	 * header:
	 * 
	 * [VERSION] = PFM Software - tide_db V1.00 - 08/01/02 [LAST MODIFIED] = Thu Aug 1 02:46:29 2002 [HEADER SIZE] = 4096 [NUMBER OF RECORDS] = 10652 [START YEAR]
	 * = 1970 [NUMBER OF YEARS] = 68 [SPEED BITS] = 31 [SPEED SCALE] = 10000000 [SPEED OFFSET] = -410667 [EQUILIBRIUM BITS] = 16 [EQUILIBRIUM SCALE] = 100
	 * [EQUILIBRIUM OFFSET] = 0 [NODE BITS] = 15 [NODE SCALE] = 10000 [NODE OFFSET] = -3949 [AMPLITUDE BITS] = 19 [AMPLITUDE SCALE] = 10000 [EPOCH BITS] = 16
	 * [EPOCH SCALE] = 100 [RECORD TYPE BITS] = 4 [LATITUDE BITS] = 25 [LATITUDE SCALE] = 100000 [LONGITUDE BITS] = 26 [LONGITUDE SCALE] = 100000 [RECORD SIZE
	 * BITS] = 12 [STATION BITS] = 18 [DATUM OFFSET BITS] = 32 [DATUM OFFSET SCALE] = 10000 [DATE BITS] = 27 [MONTHS ON STATION BITS] = 10 [CONFIDENCE VALUE BITS]
	 * = 4 [TIME BITS] = 13 [LEVEL ADD BITS] = 16 [LEVEL ADD SCALE] = 100 [LEVEL MULTIPLY BITS] = 16 [LEVEL MULTIPLY SCALE] = 1000 [DIRECTION BITS] = 9 [LEVEL
	 * UNIT BITS] = 3 [LEVEL UNIT TYPES] = 6 [LEVEL UNIT SIZE] = 15 [DIRECTION UNIT BITS] = 2 [DIRECTION UNIT TYPES] = 3 [DIRECTION UNIT SIZE] = 15 [RESTRICTION
	 * BITS] = 4 [RESTRICTION TYPES] = 2 [RESTRICTION SIZE] = 30 [PEDIGREE BITS] = 6 [PEDIGREE TYPES] = 13 [PEDIGREE SIZE] = 60 [DATUM BITS] = 7 [DATUM TYPES] =
	 * 61 [DATUM SIZE] = 70 [CONSTITUENT BITS] = 8 [CONSTITUENTS] = 173 [CONSTITUENT SIZE] = 10 [COUNTRY BITS] = 9 [COUNTRIES] = 240 [COUNTRY SIZE] = 50 [TZFILE
	 * BITS] = 10 [TZFILES] = 449 [TZFILE SIZE] = 30 [END OF FILE] = 2585170 [END OF ASCII HEADER DATA]
	 * 
	 * Most of these values will make sense in the context of the following description of the rest of the file. Some caveats on the data storage - if no SCALE is
	 * listed for a field, the scale is 1. If no BITS field is listed, this is a variable length character field and is stored as 8 bit ASCII characters. If no
	 * OFFSET is listed, the offset is 0. Offsets are scaled. All SIZE fields refer to the maximum length, in characters, of a variable length character field.
	 * Some of the BITS fields are calculated while others are hardwired (see code). For instance, [DIRECTION BITS] is hardwired because it is an integer field
	 * whose value can only be from 0 to 361 (361 = no direction flag). [NODE BITS], on the other hand, is calculated on creation by checking the min, max, and
	 * range of all of the node factor values. The number of bits needed is easily calculated by taking the log of the adjusted, scaled range, dividing by the log
	 * of 2 and adding 1. Immediately following the ASCII portion of the header is a 32 bit checksum of the ASCII portion of the header. Why? Because some genius
	 * always gets the idea that he/she can modify the header with a text or hex editor. Go figure.
	 * 
	 * The rest of the header is as follows :
	 * 
	 * [LEVEL UNIT TYPES] fields of [LEVEL UNIT SIZE] characters, each field is internally 0 terminated (anything after the zero is garbage)
	 * 
	 * [DIRECTION UNIT TYPES] fields of [DIRECTION UNIT SIZE] characters, 0 terminated
	 * 
	 * [RESTRICTION TYPES] fields of [RESTRICTION SIZE] characters, 0 terminated
	 * 
	 * [PEDIGREE TYPES] fields of [PEDIGREE SIZE] characters, 0 terminated
	 * 
	 * [TZFILES] fields of [TZFILE SIZE] characters, 0 terminated
	 * 
	 * [COUNTRIES] fields of [COUNTRY SIZE] characters, 0 terminated
	 * 
	 * [DATUM TYPES] fields of [DATUM SIZE] characters, 0 terminated
	 * 
	 * [CONSTITUENTS] fields of [CONSTITUENT SIZE] characters, 0 terminated Yes, I know, I wasted some space with these fields but I wasn't worried about a couple
	 * of hundred bytes.
	 * 
	 * [CONSTITUENTS] fields of [SPEED BITS], speed values (scaled and offset)
	 * 
	 * [CONSTITUENTS] groups of [NUMBER OF YEARS] fields of [EQUILIBRIUM BITS], equilibrium arguments (scaled and offset)
	 * 
	 * [CONSTITUENTS] groups of [NUMBER OF YEARS] fields of [NODE BITS], node factors (scaled and offset)
	 * 
	 * 
	 * Finally, the data. At present there are two types of records in the file. These are reference stations (record type 1) and subordinate stations (record
	 * type 2). Reference stations contain a set of constituents while subordinate stations contain a number of offsets to be applied to the reference station
	 * that they are associated with. Note that reference stations (record type 1) may, in actuality, be subordinate stations, albeit with a set of constituents.
	 * All of the records have the following subset of information stored as the first part of the record:
	 * 
	 * [RECORD SIZE BITS] - record size in bytes [RECORD TYPE BITS] - record type (1 or 2) [LATITUDE BITS] - latitude (degrees, south negative, scaled & offset)
	 * [LONGITUDE BITS] - longitude (degrees, west negative, scaled & offset) [TZFILE BITS] - index into timezone array (retrieved from header) variable size -
	 * station name, 0 terminated [STATION BITS] - record number of reference station or -1 [COUNTRY_BITS] index into country array (retrieved from header)
	 * [PEDIGREE BITS] - index into pedigree array (retrieved from header) variable size - source, 0 terminated [RESTRICTION BITS] - index into restriction array
	 * variable size - comments, may contain LFs to indicate newline (no CRs)
	 * 
	 * 
	 * These are the rest of the fields for record type 1:
	 * 
	 * [LEVEL UNIT BITS] - index into level units array [DATUM OFFSET BITS] - datum offset (scaled) [DATUM BITS] - index into datum name array [TIME BITS] - time
	 * zone offset from GMT0 (meridian, integer +/-HHMM) [DATE BITS] - expiration date, (integer YYYYMMDD, default is 0) [MONTHS ON STATION BITS] - months on
	 * station [DATE BITS] - last date on station, default is 0 [CONFIDENCE BITS] - confidence value (TBD) [CONSTITUENT BITS] - "N", number of constituents for
	 * this station
	 * 
	 * N groups of: [CONSTITUENT BITS] - constituent number [AMPLITUDE BITS] - amplitude (scaled & offset) [EPOCH BITS] - epoch (scaled & offset)
	 * 
	 * 
	 * These are the rest of the fields for record type 2:
	 * 
	 * [LEVEL UNIT BITS] - leveladd units, index into level_units array [DIRECTION UNIT BITS] - direction units, index into dir_units array [LEVEL UNIT BITS] -
	 * avglevel units, index into level_units array [TIME BITS] - min timeadd (integer +/-HHMM) or 0 [LEVEL ADD BITS] - min leveladd (scaled) or 0 [LEVEL MULTIPLY
	 * BITS] - min levelmultiply (scaled) or 0 [LEVEL ADD BITS] - min avglevel (scaled) or 0 [DIRECTION BITS] - min direction (0-360 or 361 for no direction)
	 * [TIME BITS] - max timeadd (integer +/-HHMM) or 0 [LEVEL ADD BITS] - max leveladd (scaled) or 0 [LEVEL MULTIPLY BITS] - max levelmultiply (scaled) or 0
	 * [LEVEL ADD BITS] - max avglevel (scaled) or 0 [DIRECTION BITS] - max direction (0-360 or 361 for no direction) [TIME BITS] - floodbegins (integer +/-HHMM)
	 * or NULLSLACKOFFSET [TIME BITS] - ebbbegins (integer +/-HHMM) or NULLSLACKOFFSET
	 * 
	 * 
	 * Back to philosophy! When you design a database of any kind the first thing you should ask yourself is "Self, how am I going to access this data most of the
	 * time?". If you answer yourself out loud you should consider seeing a shrink. 99 and 44/100ths percent of the time this database is going to be read to get
	 * station data. The other 66/100ths percent of the time it will be created/modified. Variable length records are no problem on retrieval. They are no problem
	 * to create. They can be a major pain in the backside if you have to modify/delete them. Since we shouldn't be doing too much editing of the data (usually
	 * just adding records) this is a pretty fair design. At some point though we are going to want to modify or delete a record. There are two possibilities
	 * here. We can dump the database to an ASCII file or files using restore_tide_db, use a text editor to modify them, and then rebuild the database. The other
	 * possibility is to modify the record in place. This is OK if you don't change a variable length field but what if you want to change the station name or add
	 * a couple of constituents? With the design as is we have to read the remainder of the file from the end of the record to be modified, write the modified
	 * record, rewrite the remainder of the file, and then change the end_of_file pointer in the header. So, which fields are going to be a problem? Changes to
	 * station name, source, comments, or the number of constituents for a station will require a resizing of the database. Changes to any of the other fields can
	 * be done in place. The worst thing that you can do though is to delete a record. Not just because the file has to be resized but because it might be a
	 * reference record with subordinate stations. These would have to be deleted as well. The delete_tide_record function will do just that so make sure you
	 * check before you call it. You might not want to do that.
	 * 
	 * Another point to note is that when you open the database the records are indexed at that point. This takes about half a second on a dual 450. Most
	 * applications use the header part of the record very often and the rest of the record only if they are going to actually produce predicted tides. For
	 * instance, XTide plots all of the stations on a world map or globe and lists all of the station names. It also needs the timezone up front. To save
	 * re-indexing to get these values I save them in memory. The only time an application needs to actually read an entire record is when you want to do the
	 * prediction. Otherwise just use get_partial_tide_record or get_next_partial_tide_record to yank the good stuff out of memory.
	 * 
	 * 'Nuff said?
	 * 
	 * 
	 * See libtcd.html for changelog.
	 * 
	 * \
	 *****************************************************************************/

	transient private Logger logger = Logger.getLogger(this.getClass().getName());
//	private String fileName;
	// private List<TideIndex> tindex = new LinkedList<>(); 201302 AH index discarded

//	private int currentRecord = 0;
	private XByteBuffer buffer;

	private TideDBHeader header;

	private List<TideRecord> records = new LinkedList<>();

	/*
	 * DWF: This value signifies "null" or "omitted" slack offsets (flood_begins, ebb_begins). Zero is *not* the same.
	 */
	/**
	 * Time offsets are represented as hours * 100 plus minutes. 0xA00 = 2560 It turns out that offsets do exceed 24 hours (long story), but we should still be
	 * safe with the 60.
	 */
	public static int NULL_SLACK_OFFSET = 0xA00;

	/**
	 * This is the level below which an amplitude rounds to zero. It should be exactly (0.5 / DEFAULT_AMPLITUDE_SCALE).
	 */
	public static double AMPLITUDE_EPSILON = 0.00005;

	/**
	 * Initialize with the named db the whole file is load into a XByteBuffer then the header is extracted as many records as stated in the header are created
	 * 
	 * @param fileName
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws TideDBException
	 */
	public TideDB(String fileName) throws FileNotFoundException, IOException, TideDBException
	{
		// read file into buffer
		FileInputStream input = null;
//		this.fileName = fileName;
		input = new FileInputStream(fileName);
		FileChannel channel = input.getChannel();
		int fileLength = (int) channel.size();
		buffer = new XByteBuffer((channel.map(FileChannel.MapMode.READ_ONLY, 0, fileLength)), ByteOrder.LITTLE_ENDIAN);
		// buffer.order(ByteOrder.LITTLE_ENDIAN);
		// buffer.order(ByteOrder.BIG_ENDIAN);

		// read header data
		header = new TideDBHeader(buffer);

		// now read the records itself
		int nPos = buffer.position();
		for (int i = 0; i < header.getPub().getNumberOfRecords(); i++)
		{
			TideRecord tRec = new TideRecord(this, buffer, i, nPos);
			records.add(tRec);
			nPos += tRec.getRecordSize();
		}
		input.close();
	}

	/**
	 * For fields in the tide record that are indices into tables of character string values, 
	 * these functions are used to retrieve the character string value corresponding to a particular index. 
	 * The value "Unknown" is returned when no translation exists.
	 * 
	 * @param num
	 * @return
	 */
	public String getCountryName(int num)
	{
		return header.getCountries().get(num);
	}

	public String getTzfile(int num)
	{
		return header.getTzfiles().get(num);
	}

	public String getLevelUnits(int num)
	{
		return header.getLevelUnits().get(num);
	}

	public String getDirUnits(int num)
	{
		return header.getDirUnits().get(num);
	}

	public String getRestriction(byte num)
	{
		return header.getRestrictions().get(num);
	}

	public String getDatum(int num)
	{
		return header.getDatums().get(num);
	}

	public String getLegalese(int num)
	{
		return header.getLegalese().get(num);
	}

	/**
	 * Get the name of the constituent corresponding to index num [0,constituents-1]. The return value is a pointer into static memory.
	 */
	public String getConstituent(int num)
	{
		return header.getConstituent().get(num);
	}

	/**
	 * creates a new Station object from the record with the given ID
	 * 
	 * @return the station with the given ID
	 * 
	 * @throws StationException
	 */
	public Station createStation(int nID)
	{
		Station tStat = null;
		TideRecord tRec = getRecord(nID);

		if (tRec != null)
		{
			try
			{
				if (tRec.getRecordType() == EStatType.REFERENCE_STATION)
				{
					tStat = new RefStation(tRec);
					// Fill the station with the data from the TideRecord.
					fillStation(tStat, tRec);
					fillRefStation((RefStation)tStat, tRec);
				}
				else if (tRec.getRecordType() == EStatType.SUBORDINATE_STATION)
				{
					TideRecord tRefRec = getRecord(tRec.getReferenceStation());

					tStat = new SubStation(tRec, tRefRec);
					// Fill the station with the data from the TideRecord.
					fillStation(tStat, tRec);
					fillSubStation((SubStation)tStat, tRec);
				}
			}
			catch (StationException e)
			{
				e.printStackTrace();
			}
		}
		return tStat;
	}

	/**
	 * Fills the station object with the data from the TideRecord.
	 * These are the data common for all types of stations
	 * 
	 * @param tRec
	 */
	public void fillStation(Station tStat, TideRecord tRec)
	{
		tStat.setNotes(tRec.getNotes());
		tStat.setCoordinates(new Coordinates(tRec.getLat(), tRec.getLon()));
		tStat.setTimeZone(AHTideBaseStr.getString("AHTides.Empty"));
		tStat.setAspect(0);
		String strName = tRec.getName();
		if (tRec.getLegalese() != 0)
		{
			strName += AHTideBaseStr.getString("Station.1");
			strName += (char) tRec.getLegalese();
		}
		tStat.setName(strName);
		tStat.setMinCurrentBearing(new CurrentBearing(tRec.getMinDirection()));
		tStat.setMaxCurrentBearing(new CurrentBearing(tRec.getMaxDirection()));
		}

	// private Station(String name, StationRef stationRef, ConstituentSet
	// constituents, String note, CurrentBearing minCurrentBearing,
	// CurrentBearing maxCurrentBearing, Deque<MetaField> metaData)
	// {
	// this.m_strName = name;
	// this.timeZone = stationRef.getTimeZone();
	// this.minCurrentBearing = minCurrentBearing;
	// this.maxCurrentBearing = maxCurrentBearing;
	// this.note = note;
	// this.isCurrent = AHTUnits.isCurrent(constituents.getPredictUnits());
	// this.aspect = 0; // TODO: fix this
	// this.step = new Interval();
	// this.stationRef = stationRef;
	// this.constituents = constituents;
	// this.metadata = metaData;
	//
	// }

	/**
	 * fills the station object with the substation data from the given tRec
	 * 
	 * @param tRec
	 *          record in TideDB
	 * @param tStat
	 *          Station object to be filled
	 */
	public void fillSubStation(SubStation tStat, TideRecord tRec) throws StationException
	{
		TideRecord tRefRec;
		assert (tRec.getReferenceStation() >= 0);
		tRefRec = getRecord(tRec.getReferenceStation());
		AHTidePredictionUnits refStationNativeUnits = AHTUnits.parse(getLevelUnits(tRefRec.getLevelUnits()));
		tStat.setMinCurrentBearing(new CurrentBearing(tRefRec.getMinDirection()));
		tStat.setMaxCurrentBearing(new CurrentBearing(tRefRec.getMaxDirection()));
		
		Deque<MetaField> metadata = new ArrayDeque<>();
		buildMetadata(tStat, metadata, tRec, refStationNativeUnits, tStat.getMinCurrentBearing(), tStat.getMaxCurrentBearing());
		tStat.setMetadata(metadata);

		NullableInterval tempFloodBegins = new NullableInterval(), tempEbbBegins = new NullableInterval();

		// For these, zero is not the same as null.
		if (tRec.getFloodBegins() != TideDB.NULL_SLACK_OFFSET)
			tempFloodBegins = new NullableInterval(new Interval(TideDB.getTimeNeatStr(tRec.getFloodBegins())));
		if (tRec.getEbbBegins() != TideDB.NULL_SLACK_OFFSET)
			tempEbbBegins = new NullableInterval(new Interval(TideDB.getTimeNeatStr(tRec.getEbbBegins())));

		AHTidePredictionUnits lu = levelAddUnits(tRec);
		HairyOffsets ho = new HairyOffsets(new SimpleOffsets(new Interval(getTimeNeatStr(tRec.getMaxTimeAdd())), new PredictionValue(lu, tRec.getMaxLevelAdd()),
				tRec.getMaxLevelMultiply()), new SimpleOffsets(new Interval(getTimeNeatStr(tRec.getMinTimeAdd())), new PredictionValue(lu, tRec.getMinLevelAdd()),
				tRec.getMinLevelMultiply()), tempFloodBegins, tempEbbBegins);

		tStat.ApplyHairyOffsets(ho);

		// If the offsets can be reduced to simple, then we can adjust the constituents and be done with it. 
		// However, in the case of hydraulic currents, level multipliers cannot be treated as simple and applied to the constants 
		// because of the square root operation--i.e., it's nonlinear.		
		 SimpleOffsets so = new SimpleOffsets(); 
		 if (( !AHTUnits.isHydraulicCurrent(refStationNativeUnits) || ho.getMaxLevelMultiply() == 1.0) &&	ho.trySimplify(so)) 
			 tStat.setConst(tRefRec.getConstituents(so));
		 else	 
		 {
			 tStat.setConst(tRefRec.getConstituents(so));
		 }
		
	}

	/**
	 * fills the RefStation object with the data from the given tRec
	 * 
	 * @param tRec
	 *          record in TideDB
	 * @param tStat
	 *          Station object to be filled
	 */
	public void fillRefStation(RefStation tStat, TideRecord tRec) throws StationException
	{
		// Fill the remaining data of the station.
		tStat.setMinCurrentBearing(new CurrentBearing(tRec.getMinDirection()));
		tStat.setMaxCurrentBearing(new CurrentBearing(tRec.getMaxDirection()));
		Deque<MetaField> metadata = new ArrayDeque<>();
		buildMetadata(tStat, metadata, tRec, AHTUnits.parse(getLevelUnits(tRec.getLevelUnits())), tStat.getMinCurrentBearing(), tStat.getMaxCurrentBearing());
		tStat.setMetadata(metadata);
		tStat.setConst(tRec.getConstituents(new SimpleOffsets()));
	}

	/**
	 * Returns the speed of the constituent indicated by num [0,constituents-1].
	 */
	public double getSpeed(int num)
	{
		return header.getSpeed().get(num);
	}

	/**
	 * Get the equilibrium argument and node factor for the constituent indicated by num [0,constituents-1], for the year start_year+year.
	 */
	public double getEquilibrium(int num, int year)
	{
		assert (num >= 0 && num < header.getPub().getConstituents() && year >= 0 && year < header.getPub().getNumberOfYears());
		return header.getEquilibrium().get(num).get(year);
	}

	public double getNodeFactor(int num, int year)
	{
		assert (num >= 0 && num < header.getPub().getConstituents() && year >= 0 && year < header.getPub().getNumberOfYears());
		return header.getNodeFactor().get(num).get(year);
	}

	/**
	 * Get all available equilibrium arguments and node factors for the constituent indicated by num [0,constituents-1]. The return value is a pointer into static
	 * memory which is an array of number_of_years floats, corresponding to the years start_year through start_year+number_of_years-1.
	 */
	public List<Double> getEquilibriums(int num)
	{
		assert (num >= 0 && num < header.getPub().getConstituents());
		return header.getEquilibrium().get(num);
	}

	public List<Double> getNodeFactors(int num)
	{
		assert (num >= 0 && num < header.getPub().getConstituents());
		return header.getNodeFactor().get(num);
	}

	public Double[] getNodeFactorsArray()
	{
		return (Double[]) (header.getNodeFactor().toArray());
	}

	/**
	 * Convert between character strings of the form "[+-]HH:MM" and the encoding Hours * 100 + Minutes. ret_time pads the hours with a leading zero when less
	 * than 10; ret_time_neat omits the leading zero and omits the sign when the value is 0:00. Returned pointers point into static memory.
	 */
	public int getTime(String string)
	{
		int hour;
		int minute;
		int hhmm;

		Scanner scanner = new Scanner(string);
		scanner.useDelimiter(AHTideTCDStr.getString("TideDB.2")); //$NON-NLS-1$
		hour = scanner.nextInt();
		minute = scanner.nextInt();
		// sscanf(string, "%d:%d", & hour, & minute);

		/* Trying to deal with negative 0 (-00:45). */
		if (string.charAt(0) == '-')
		{
			if (hour < 0)
			{
				hour = -hour;
			}
			hhmm = -(hour * 100 + minute);
		}
		else
		{
			hhmm = hour * 100 + minute;
		}
		scanner.close();
		return (hhmm);
	}

	/**
	 * Convert between character strings of the form "[+-]HH:MM" and the encoding Hours * 100 + Minutes. ret_time pads the hours with a leading zero when less
	 * than 10; ret_time_neat omits the leading zero and omits the sign when the value is 0:00. Returned pointers point into static memory.
	 */
	public String getTimeStr(int time)
	{
		int hour;
		int minute;
		String tname = AHTideTCDStr.getString("TideDB.Empty"); //$NON-NLS-1$

		hour = Math.abs(time) / 100;

		assert (hour < 100000); /* 9 chars: +99999:99 */
		minute = Math.abs(time) % 100;
		if (time < 0)
		{
			tname = String.format(AHTideTCDStr.getString("TideDB.4"), hour, minute); //$NON-NLS-1$
		}
		else
		{
			tname = String.format(AHTideTCDStr.getString("TideDB.5"), hour, minute); //$NON-NLS-1$
		}
		return tname;
	}

	/**
	 * Convert between character strings of the form "[+-]HH:MM" and the encoding Hours * 100 + Minutes. ret_time pads the hours with a leading zero when less
	 * than 10; ret_time_neat omits the leading zero and omits the sign when the value is 0:00. Returned pointers point into static memory.
	 */
	static public String getTimeNeatStr(int time)
	{
		int hour;
		int minute;
		String tname = AHTideTCDStr.getString("TideDB.Empty"); //$NON-NLS-1$

		hour = Math.abs(time) / 100;

		assert (hour < 100000); /* 9 chars: +99999:99 */
		minute = Math.abs(time) % 100;
		if (time < 0)
			tname = String.format(AHTideTCDStr.getString("TideDB.7"), hour, minute); //$NON-NLS-1$
		else if (time > 0)
			tname = String.format(AHTideTCDStr.getString("TideDB.8"), hour, minute); //$NON-NLS-1$
		else
			tname = AHTideTCDStr.getString("TideDB.9"); //$NON-NLS-1$
		return tname;
	}

	/**
	 * 
	 * @param date
	 * @return
	 */
	public String getDateStr(int date)
	{
		String tname;
		if (date == 0)
			tname = AHTideTCDStr.getString("TideDB.10"); //$NON-NLS-1$
		else
		{
			int y, m, d;
			y = date / 10000;
			date %= 10000;
			m = date / 100;
			d = date % 100;
			tname = String.format(AHTideTCDStr.getString("TideDB.11"), y, m, d); //$NON-NLS-1$
		}
		return tname;
	}

	/**
	 * When invoked multiple times with the same string, returns record numbers of all stations that have that string anywhere in the station name. This search is
	 * case insensitive. When no more records are found it returns -1.
	 */

	private static String lastSearch = AHTideTCDStr.getString("TideDB.Empty"); //$NON-NLS-1$
	private static int j = 0;

	// public int searchStation(String string)
	// {
	// String name;
	// String search = string.toLowerCase();
	//
	// if ( !search.equals(lastSearch))
	// j = 0;
	//
	// lastSearch = search;
	//
	// while (j < header.getPub().getNumberOfRecords())
	// {
	// name = records.get(j).getName().toLowerCase();
	// if (name.contains(search))
	// {
	// return (j);
	// }
	// ++j;
	// }
	// j = 0;
	// return -1;
	// }

	// /**
	// * Finds a station by exact matching the name. Returns -1 for not found.
	// */
	// public int findStation(String name)
	// {
	// for (int i = 0; i < header.getPub().getNumberOfRecords(); ++i)
	// {
	// if (name.equals(records.get(i).getName()))
	// {
	// return (i);
	// }
	// }
	// return ( -1);
	// }

	// int find_tzfile (String name);
	// int find_country (String name);
	// int find_level_units (String name);

	public int findDirUnits(String name)
	{
		for (int i = 0; i < header.getPub().getDirUnitTypes(); ++i)
		{
			if (getDirUnits(i).equals(name))
				return i;
		}
		return -1;
	}

	public TideDBHeader getHeader()
	{
		return header;
	}

	// int find_restriction (String name);
	// int find_datum (String name);
	// int find_constituent (String name);
	// int find_legalese (String name);

	// local
	public List<TideRecord> getRecords()
	{
		return records;
	}

	public void setRecords(List<TideRecord> records)
	{
		this.records = records;
	}

	/**
	 * looks for a record with a given ID.
	 * 
	 * @param nID
	 * @return the reocord with the given ID or null if nothing found
	 */
	public TideRecord getRecord(int nID)
	{
		TideRecord tRec = null;

		for (ListIterator<TideRecord> lIt = getRecords().listIterator(); lIt.hasNext();)
		{
			tRec = lIt.next();
			if (tRec.getID() == nID)
				break;
		}
		return tRec;
	}

	// refStationNativeUnits: determination of hydraulic vs. regular
	// current is made based on units at reference station. Units are
	// flattened for levelAdd offsets.
	private void buildMetadata(Station tStat, Deque<MetaField> metadata, TideRecord tRec, AHTidePredictionUnits refStationNativeUnits,
			CurrentBearing minCurrentBearing, CurrentBearing maxCurrentBearing)
	{
		String tmpbuf;

		metadata.push(new MetaField(AHTideTCDStr.getString("TideDB.13"), tStat.getName())); //$NON-NLS-1$
		// metadata.push(new MetaField("In file", tStat.getHarmonicsFileName()));
		if (tRec.getLegalese() != 0)
		{
			metadata.push(new MetaField(AHTideTCDStr.getString("TideDB.14"), getLegalese(tRec.getLegalese()))); //$NON-NLS-1$
		}
		if (!tRec.getStationIdContext().equals(AHTideTCDStr.getString("TideDB.Empty"))) //$NON-NLS-1$
		{
			metadata.push(new MetaField(AHTideTCDStr.getString("TideDB.16"), tRec.getStationIdContext())); //$NON-NLS-1$
		}
		if (!tRec.getStationId().equals(AHTideTCDStr.getString("TideDB.Empty"))) //$NON-NLS-1$
		{
			metadata.push(new MetaField(AHTideTCDStr.getString("TideDB.18"), tRec.getStationId())); //$NON-NLS-1$
		}
		if (tRec.getDateImported() != 0)
		{
			metadata.push(new MetaField(AHTideTCDStr.getString("TideDB.19"), getDateStr(tRec.getDateImported()))); //$NON-NLS-1$
		}
		tmpbuf = tStat.getCoordinates().print();
		metadata.push(new MetaField(AHTideTCDStr.getString("TideDB.20"), tmpbuf)); //$NON-NLS-1$
		metadata.push(new MetaField(AHTideTCDStr.getString("TideDB.21"), getCountryName(tRec.getCountry()))); //$NON-NLS-1$
		metadata.push(new MetaField(AHTideTCDStr.getString("TideDB.22"), tStat.getTimeZone())); //$NON-NLS-1$
		metadata.push(new MetaField(AHTideTCDStr.getString("TideDB.23"), refStationNativeUnits.getLongName())); //$NON-NLS-1$
		if (!(maxCurrentBearing.isNull()))
		{
			tmpbuf = maxCurrentBearing.print();
			metadata.push(new MetaField(AHTideTCDStr.getString("TideDB.24"), tmpbuf)); //$NON-NLS-1$
		}
		if (!(minCurrentBearing.isNull()))
		{
			tmpbuf = minCurrentBearing.print();
			metadata.push(new MetaField(AHTideTCDStr.getString("TideDB.25"), tmpbuf)); //$NON-NLS-1$
		}
		if (!tRec.getSource().equals(AHTideTCDStr.getString("TideDB.12"))) //$NON-NLS-1$
		{
			metadata.push(new MetaField(AHTideTCDStr.getString("TideDB.27"), tRec.getSource())); //$NON-NLS-1$
		}
		metadata.push(new MetaField(AHTideTCDStr.getString("TideDB.28"), getRestriction(tRec.getRestriction()))); //$NON-NLS-1$
		if (!tRec.getComments().equals(AHTideTCDStr.getString("TideDB.Empty"))) //$NON-NLS-1$
		{
			metadata.push(new MetaField(AHTideTCDStr.getString("TideDB.30"), tRec.getComments())); //$NON-NLS-1$
		}
		if (!tRec.getNotes().equals(AHTideTCDStr.getString("TideDB.Empty"))) //$NON-NLS-1$
		{
			metadata.push(new MetaField(AHTideTCDStr.getString("TideDB.32"), tRec.getNotes())); //$NON-NLS-1$
		}
		parse_xfields(metadata, tRec.getXfields());

		switch (tRec.getRecordType())
		{
		case REFERENCE_STATION:
			metadata.push(new MetaField(AHTideTCDStr.getString("TideDB.33"), //$NON-NLS-1$
					(AHTUnits.isCurrent(refStationNativeUnits) ? (AHTUnits.isHydraulicCurrent(refStationNativeUnits) ? AHTideTCDStr.getString("TideDB.34") //$NON-NLS-1$
							: AHTideTCDStr.getString("TideDB.35")) : AHTideTCDStr.getString("TideDB.36")))); //$NON-NLS-1$ //$NON-NLS-2$
			metadata.push(new MetaField(AHTideTCDStr.getString("TideDB.37"), TideDB.getTimeNeatStr(tRec.getZoneOffset()))); //$NON-NLS-1$
			if (!AHTUnits.isCurrent(refStationNativeUnits))
				metadata.push(new MetaField(AHTideTCDStr.getString("TideDB.38"), getDatum(tRec.getDatum()))); //$NON-NLS-1$
			if (tRec.getMonthsOnStation() != 0)
			{
				metadata.push(new MetaField(AHTideTCDStr.getString("TideDB.39"), String.format(AHTideTCDStr.getString("TideDB.40"), tRec.getMonthsOnStation()))); //$NON-NLS-1$ //$NON-NLS-2$
			}
			if (tRec.getLastDateOnStation() != 0)
			{
				metadata.push(new MetaField(AHTideTCDStr.getString("TideDB.41"), getDateStr(tRec.getLastDateOnStation()))); //$NON-NLS-1$
			}
			if (tRec.getExpirationDate() != 0)
			{
				metadata.push(new MetaField(AHTideTCDStr.getString("TideDB.42"), getDateStr(tRec.getExpirationDate()))); //$NON-NLS-1$
			}
			metadata.push(new MetaField(AHTideTCDStr.getString("TideDB.43"), String.format(AHTideTCDStr.getString("TideDB.40"), tRec.getConfidence()))); //$NON-NLS-1$ //$NON-NLS-2$
			break;

		case SUBORDINATE_STATION:
			metadata.push(new MetaField(AHTideTCDStr.getString("TideDB.45"), //$NON-NLS-1$
					(AHTUnits.isCurrent(refStationNativeUnits) ? (AHTUnits.isHydraulicCurrent(refStationNativeUnits) ? AHTideTCDStr.getString("TideDB.46") //$NON-NLS-1$
							: AHTideTCDStr.getString("TideDB.47")) : AHTideTCDStr.getString("TideDB.48")))); //$NON-NLS-1$ //$NON-NLS-2$
			// metadata.push(new MetaField("Reference", getStation(tRec.getReferenceStation())));
			appendOffsetsMetadata(metadata, tRec);
			break;

		default:
			assert (false);
		}
	}

	private void parse_xfields(Deque<MetaField> metadata, String xfields)
	{
		assert (xfields.length() != 0);
		String[] x = xfields.split(AHTideTCDStr.getString("TideDB.49")); //$NON-NLS-1$
		String name = null, value = null;
		for (String linebuf: x)
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
					value = String.format(AHTideTCDStr.getString("TideDB.40"), (i + 1)); //$NON-NLS-1$
				}
			}
		}
		if (name != null)
			metadata.push(new MetaField(name, value));
	}

	private void appendOffsetsMetadata(Deque<MetaField> metadata, TideRecord rec)
	{
		String tmp;
		AHTidePredictionUnits lu = levelAddUnits(rec);

		metadata.push(new MetaField(
				AHTideTCDStr.getString("TideDB.51"), rec.getMinTimeAdd() != 0 ? TideDB.getTimeNeatStr(rec.getMinTimeAdd()) : AHTideTCDStr.getString("TideDB.NULL"))); //$NON-NLS-1$ //$NON-NLS-2$
		tmp = String.format(AHTideTCDStr.getString("TideDB.53"), rec.getMinLevelAdd(), lu.getShortName()); //$NON-NLS-1$
		metadata.push(new MetaField(AHTideTCDStr.getString("TideDB.54"), rec.getMinLevelAdd() != 0 ? tmp : AHTideTCDStr.getString("TideDB.NULL"))); //$NON-NLS-1$ //$NON-NLS-2$
		tmp = String.format(AHTideTCDStr.getString("TideDB.56"), rec.getMinLevelMultiply()); //$NON-NLS-1$
		metadata.push(new MetaField(AHTideTCDStr.getString("TideDB.57"), rec.getMinLevelMultiply() > 0.0 ? tmp : AHTideTCDStr.getString("TideDB.NULL"))); //$NON-NLS-1$ //$NON-NLS-2$

		metadata.push(new MetaField(
				AHTideTCDStr.getString("TideDB.59"), rec.getMaxTimeAdd() != 0 ? TideDB.getTimeNeatStr(rec.getMaxTimeAdd()) : AHTideTCDStr.getString("TideDB.NULL"))); //$NON-NLS-1$ //$NON-NLS-2$
		tmp = String.format(AHTideTCDStr.getString("TideDB.53"), rec.getMaxLevelAdd(), lu.getShortName()); //$NON-NLS-1$
		metadata.push(new MetaField(AHTideTCDStr.getString("TideDB.62"), rec.getMaxLevelAdd() != 0 ? tmp : AHTideTCDStr.getString("TideDB.NULL"))); //$NON-NLS-1$ //$NON-NLS-2$
		tmp = String.format(AHTideTCDStr.getString("TideDB.56"), rec.getMaxLevelMultiply()); //$NON-NLS-1$
		metadata.push(new MetaField(AHTideTCDStr.getString("TideDB.65"), rec.getMaxLevelMultiply() > 0.0 ? tmp : AHTideTCDStr.getString("TideDB.NULL"))); //$NON-NLS-1$ //$NON-NLS-2$

		if (AHTUnits.isCurrent(lu))
		{
			metadata
					.push(new MetaField(
							AHTideTCDStr.getString("TideDB.67"), rec.getFloodBegins() == TideDB.NULL_SLACK_OFFSET ? AHTideTCDStr.getString("TideDB.NULL") : TideDB.getTimeNeatStr(rec.getFloodBegins()))); //$NON-NLS-1$ //$NON-NLS-2$
			metadata
					.push(new MetaField(
							AHTideTCDStr.getString("TideDB.69"), rec.getEbbBegins() == TideDB.NULL_SLACK_OFFSET ? AHTideTCDStr.getString("TideDB.NULL") : TideDB.getTimeNeatStr(rec.getEbbBegins()))); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	// Analogous to getDatum, levelAdds for hydraulic currents are always in
	// knots not knots^2. The database can specify either knots or
	// knots^2 in the sub station record, but there is only one sensible
	// interpretation.
	public AHTidePredictionUnits levelAddUnits(TideRecord rec)
	{
		return AHTUnits.flatten(AHTUnits.parse(getLevelUnits(rec.getLevelUnits())));
	}

}
