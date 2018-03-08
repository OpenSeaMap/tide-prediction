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

import java.util.LinkedList;

import ahdt.tides.base.AHTUnits;
import ahdt.tides.base.AHTUnits.AHTidePredictionUnits;
import ahdt.tides.base.Amplitude;
import ahdt.tides.base.Constituent;
import ahdt.tides.base.ConstituentSet;
import ahdt.tides.base.HairyOffsets;
import ahdt.tides.base.Interval;
import ahdt.tides.base.NullableInterval;
import ahdt.tides.base.PredictionValue;
import ahdt.tides.base.SimpleOffsets;
import ahdt.tides.base.XByteBuffer;

/**
 * 
 * @author chas
 */
/**
 * 
 * @author humbach 2013
 */
/**
 * 
 * encapsulates the 'raw' data as read from the tcd.
 * basic operation regarding the database are possible here.
 *
 * For calculations etc. you will need to construct a Station object from this TideRecord
 */
public final class TideRecord
{

	public enum EStatType {

		REFERENCE_STATION(1), SUBORDINATE_STATION(2);
		private static java.util.Map<Integer, EStatType> ikMap;
		private int i;

		static
		{
			if (EStatType.ikMap == null)
			{
				EStatType.ikMap = new java.util.HashMap<>();
			}

			for (EStatType a : values())
			{
				EStatType.ikMap.put(new Integer(a.i), a);
			}

		}

		EStatType(int i)
		{
			this.i = i;
		}

		public int getValue()
		{
			return i;
		}

		public static EStatType fromInt(int i)
		{
			// 20130128 AH temp override of throw
			if (ikMap.get(i) == null)
				throw new IndexOutOfBoundsException(AHTideTCDStr.getString("TideRecord.0") + i); //$NON-NLS-1$
			// return ikMap.get(1);
			return ikMap.get(i);
		}
	}

	// private transient Logger logger = Logger.getLogger(this.getClass().getName());
	// private TideStationHeader header;
	private byte restriction;
	private byte legalese;
	private byte directionUnits;
	private byte levelUnits;
	private int m_nCountry;
	private int minDirection;
	private int maxDirection;
	private int dateImported;
	private int tzfile;
	private int referenceStation;
	private int m_nRecNum;
	private int m_nRecSize;
	private double m_fLatitude;
	private double m_fLongitude;
	private TideRecord.EStatType recordType;
	private String source;
	private String comments;
	private String notes;
	private String m_NOAAStationIDContext;
	private String m_NOAAStationID;
	private String xfields;
	private String m_strName;

	/* Type 1 aka reference station */
	private byte confidence;
	private int datum;
	private int zoneOffset;
	private int expirationDate;
	private int monthsOnStation;
	private int lastDateOnStation;
	private double datumOffset;
	private double[] m_afAmplitudes;
	private double[] m_afEpochs;

	/* Type 2 aka subordinate station */
	private int minTimeAdd;
	private int maxTimeAdd;
	private int floodBegins;
	private int ebbBegins;
	private double minLevelAdd;
	private double minLevelMultiply;
	private double maxLevelAdd;
	private double maxLevelMultiply;

	transient private TideDB tideDB;

	/**
	 * TideRecord 
	 * @param tideDB
	 * @param buffer
	 * @param tideIndex
	 * @param recNum
	 * @throws TideDBException
	 */
	TideRecord(TideDB tideDB, XByteBuffer buffer, int recNum, int nPos) throws TideDBException
	{
		this.setDB(tideDB);
		TideDBHeader tcdHeader = tideDB.getHeader();
		buffer.position(nPos);
		int r = tideDB.findDirUnits(AHTideTCDStr.getString("TideRecord.1")); //$NON-NLS-1$
		setDirectionUnits((byte) r);
		setMinDirection(361);
		setMaxDirection(361);
		setFloodBegins(TideDB.NULL_SLACK_OFFSET);
		setEbbBegins(TideDB.NULL_SLACK_OFFSET);
		// setHeader(new TideStationHeader(tideDB, buffer, tideIndex, recNum));

		m_nRecNum = recNum;
		m_nRecSize = buffer.unpack(tcdHeader.getRecordSizeBits());
		recordType = TideRecord.EStatType.fromInt(buffer.unpack(tcdHeader.getRecordTypeBits()));
		double tempLat = buffer.signedUnpack(tcdHeader.getLatitudeBits());
		m_fLatitude = tempLat / tcdHeader.getLatitudeScale();
		double tempLon = buffer.signedUnpack(tcdHeader.getLongitudeBits());
		m_fLongitude = tempLon / tcdHeader.getLongitudeScale();
		tzfile = buffer.unpack(tcdHeader.getTzfileBits());
		m_strName = buffer.unpackString(DbHeaderPublic.ONELINER_LENGTH, AHTideTCDStr.getString("TideRecord.2")); //$NON-NLS-1$
		referenceStation = buffer.signedUnpack(tcdHeader.getStationBits());

		if (tcdHeader.getPub().getMajorRev() != 2)
		{
			throw new TideDBException(String.format(AHTideTCDStr.getString("TideRecord.3"), tcdHeader.getPub().getMajorRev())); //$NON-NLS-1$
		}

		setCountry(buffer.unpack(tcdHeader.getCountryBits()));
		setSource(buffer.unpackString(DbHeaderPublic.ONELINER_LENGTH, AHTideTCDStr.getString("TideRecord.4"))); //$NON-NLS-1$
		setRestriction((byte) buffer.unpack(tcdHeader.getRestrictionBits()));
		setComments(buffer.unpackString(DbHeaderPublic.MONOLOGUE_LENGTH, AHTideTCDStr.getString("TideRecord.5"))); //$NON-NLS-1$
		setNotes(buffer.unpackString(DbHeaderPublic.MONOLOGUE_LENGTH, AHTideTCDStr.getString("TideRecord.6"))); //$NON-NLS-1$
		setLegalese((byte) buffer.unpack(tcdHeader.getLegaleseBits()));
		setStationIdContext(buffer.unpackString(DbHeaderPublic.ONELINER_LENGTH, AHTideTCDStr.getString("TideRecord.7"))); //$NON-NLS-1$
		setStationId(buffer.unpackString(DbHeaderPublic.ONELINER_LENGTH, AHTideTCDStr.getString("TideRecord.8"))); //$NON-NLS-1$
		setDateImported(buffer.unpack(tcdHeader.getDateBits()));
		setXfields(buffer.unpackString(DbHeaderPublic.MONOLOGUE_LENGTH, AHTideTCDStr.getString("TideRecord.9"))); //$NON-NLS-1$
		setDirectionUnits((byte) buffer.unpack(tcdHeader.getDirUnitBits()));
		setMinDirection(buffer.unpack(tideDB.getHeader().getDirectionBits()));
		setMaxDirection(buffer.unpack(tideDB.getHeader().getDirectionBits()));
		setLevelUnits((byte) buffer.unpack(tideDB.getHeader().getLevelUnitBits()));
		if (recordType == EStatType.REFERENCE_STATION)
		{
			// reference station
			setDatumOffset((double) buffer.signedUnpack(tideDB.getHeader().getDatumOffsetBits()) / tideDB.getHeader().getDatumOffsetScale());
			setDatum(buffer.signedUnpack(tideDB.getHeader().getDatumBits()));
			setZoneOffset(buffer.signedUnpack(tideDB.getHeader().getTimeBits()));
			setExpirationDate(buffer.unpack(tideDB.getHeader().getDateBits()));
			setMonthsOnStation(buffer.unpack(tideDB.getHeader().getMonthsOnStationBits()));
			setLastDateOnStation(buffer.unpack(tideDB.getHeader().getDateBits()));
			setConfidence((byte) buffer.unpack(tideDB.getHeader().getConfidenceValueBits()));
			double[] localAmplitude = new double[(int) tideDB.getHeader().getPub().getConstituents()];
			double[] localEpoch = new double[(int) tideDB.getHeader().getPub().getConstituents()];
			for (int i = 0; i < tideDB.getHeader().getPub().getConstituents(); i++ )
			{
				localAmplitude[i] = 0.0;
				localEpoch[i] = 0.0;
			}
			int count = buffer.unpack(tideDB.getHeader().getConstituentBits());
			// logger.debug("name = " + getHeader().getName());
			for (int i = 0; i < count; i++ )
			{
				int index = buffer.unpack(tideDB.getHeader().getConstituentBits());
				localAmplitude[index] = (double) buffer.unpack(tideDB.getHeader().getAmplitudeBits()) / tideDB.getHeader().getAmplitudeScale();
				localEpoch[index] = (double) buffer.unpack(tideDB.getHeader().getEpochBits()) / tideDB.getHeader().getEpochScale();
				// logger.debug("i = " + i + " ampl = " + localAmplitude[index] +
				// " epoch = " + localEpoch[index]);
			}
			setAmplitude(localAmplitude);
			setEpoch(localEpoch);
		}
		else if (recordType == EStatType.SUBORDINATE_STATION)
		{
			// subordinate station
			setMinTimeAdd(buffer.signedUnpack(tideDB.getHeader().getTimeBits()));
			setMinLevelAdd((double) buffer.signedUnpack(tideDB.getHeader().getLevelAddBits()) / tideDB.getHeader().getLevelAddScale());
			setMinLevelMultiply((double) buffer.unpack(tideDB.getHeader().getLevelMultiplyBits()) / tideDB.getHeader().getLevelMultiplyScale());
			setMaxTimeAdd(buffer.signedUnpack(tideDB.getHeader().getTimeBits()));
			setMaxLevelAdd((double) buffer.signedUnpack(tideDB.getHeader().getLevelAddBits()) / tideDB.getHeader().getLevelAddScale());
			setMaxLevelMultiply((double) buffer.unpack(tideDB.getHeader().getLevelMultiplyBits()) / tideDB.getHeader().getLevelMultiplyScale());
			setFloodBegins(buffer.signedUnpack(tideDB.getHeader().getTimeBits()));
			setEbbBegins(buffer.signedUnpack(tideDB.getHeader().getTimeBits()));
		}

		// for debugging
		// dump();
	}

	/**
	 * dump()
	 */
	public void dump()
	{

		System.out.printf(AHTideTCDStr.getString("TideRecord.10"), getID()); //$NON-NLS-1$
		System.out.printf(AHTideTCDStr.getString("TideRecord.11"), m_nRecSize); //$NON-NLS-1$
		System.out.printf(AHTideTCDStr.getString("TideRecord.12"), recordType); //$NON-NLS-1$
		System.out.printf(AHTideTCDStr.getString("TideRecord.13"), m_fLatitude); //$NON-NLS-1$
		System.out.printf(AHTideTCDStr.getString("TideRecord.14"), m_fLongitude); //$NON-NLS-1$
		System.out.printf(AHTideTCDStr.getString("TideRecord.15"), referenceStation); //$NON-NLS-1$
		System.out.printf(AHTideTCDStr.getString("TideRecord.16"), getDB().getTzfile(tzfile)); //$NON-NLS-1$
		System.out.printf(AHTideTCDStr.getString("TideRecord.17"), m_strName); //$NON-NLS-1$

		System.out.printf(AHTideTCDStr.getString("TideRecord.18"), getDB().getCountryName(m_nCountry)); //$NON-NLS-1$
		System.out.printf(AHTideTCDStr.getString("TideRecord.19"), source); //$NON-NLS-1$
		System.out.printf(AHTideTCDStr.getString("TideRecord.20"), getDB().getRestriction(restriction)); //$NON-NLS-1$
		System.out.printf(AHTideTCDStr.getString("TideRecord.21"), comments); //$NON-NLS-1$
		System.out.printf(AHTideTCDStr.getString("TideRecord.22"), notes); //$NON-NLS-1$
		System.out.printf(AHTideTCDStr.getString("TideRecord.23"), getDB().getLegalese(legalese)); //$NON-NLS-1$
		System.out.printf(AHTideTCDStr.getString("TideRecord.24"), m_NOAAStationIDContext); //$NON-NLS-1$
		System.out.printf(AHTideTCDStr.getString("TideRecord.25"), m_NOAAStationID); //$NON-NLS-1$
		System.out.printf(AHTideTCDStr.getString("TideRecord.26"), dateImported); //$NON-NLS-1$
		System.out.printf(AHTideTCDStr.getString("TideRecord.27"), xfields); //$NON-NLS-1$

		System.out.printf(AHTideTCDStr.getString("TideRecord.28"), getDB().getDirUnits(directionUnits)); //$NON-NLS-1$
		System.out.printf(AHTideTCDStr.getString("TideRecord.29"), minDirection); //$NON-NLS-1$
		System.out.printf(AHTideTCDStr.getString("TideRecord.30"), maxDirection); //$NON-NLS-1$
		System.out.printf(AHTideTCDStr.getString("TideRecord.31"), getDB().getLevelUnits(levelUnits)); //$NON-NLS-1$
		//
		if (recordType == EStatType.REFERENCE_STATION)
		{
			System.out.printf(AHTideTCDStr.getString("TideRecord.32"), getDatumOffset()); //$NON-NLS-1$
			System.out.printf(AHTideTCDStr.getString("TideRecord.33"), getDatum()); //$NON-NLS-1$
			System.out.printf(AHTideTCDStr.getString("TideRecord.34"), getZoneOffset()); //$NON-NLS-1$
			System.out.printf(AHTideTCDStr.getString("TideRecord.35"), getExpirationDate()); //$NON-NLS-1$
			System.out.printf(AHTideTCDStr.getString("TideRecord.36"), getMonthsOnStation()); //$NON-NLS-1$
			System.out.printf(AHTideTCDStr.getString("TideRecord.37"), getLastDateOnStation()); //$NON-NLS-1$
			System.out.printf(AHTideTCDStr.getString("TideRecord.38"), getConfidence()); //$NON-NLS-1$
			for (int i = 0; i < getDB().getHeader().getPub().getConstituents(); ++i)
			{
				if (getAmplitude()[i] != 0.0 || getEpoch()[i] != 0.0)
				{
					System.out.printf(AHTideTCDStr.getString("TideRecord.39"), i, getAmplitude()[i]); //$NON-NLS-1$
					System.out.printf(AHTideTCDStr.getString("TideRecord.40"), i, getEpoch()[i]); //$NON-NLS-1$
				}
			}
		}
		else if (recordType == EStatType.SUBORDINATE_STATION)
		{
			System.out.printf(AHTideTCDStr.getString("TideRecord.41"), getMinTimeAdd()); //$NON-NLS-1$
			System.out.printf(AHTideTCDStr.getString("TideRecord.42"), getMinLevelAdd()); //$NON-NLS-1$
			System.out.printf(AHTideTCDStr.getString("TideRecord.43"), getMinLevelMultiply()); //$NON-NLS-1$
			System.out.printf(AHTideTCDStr.getString("TideRecord.44"), getMaxTimeAdd()); //$NON-NLS-1$
			System.out.printf(AHTideTCDStr.getString("TideRecord.45"), getMaxLevelAdd()); //$NON-NLS-1$
			System.out.printf(AHTideTCDStr.getString("TideRecord.46"), getMaxLevelMultiply()); //$NON-NLS-1$
			System.out.printf(AHTideTCDStr.getString("TideRecord.47"), getFloodBegins()); //$NON-NLS-1$
			System.out.printf(AHTideTCDStr.getString("TideRecord.48"), getEbbBegins()); //$NON-NLS-1$
		}
	}

	public double[] getAmplitude()
	{
		return m_afAmplitudes;
	}

	public String getComments()
	{
		return comments;
	}

	public byte getConfidence()
	{
		return confidence;
	}

	public int getCountry()
	{
		return m_nCountry;
	}

	public int getDateImported()
	{
		return dateImported;
	}

	public int getDatum()
	{
		return datum;
	}

	public double getDatumOffset()
	{
		return datumOffset;
	}

	public byte getDirection_units()
	{
		return directionUnits;
	}

	public int getEbbBegins()
	{
		return ebbBegins;
	}

	public double[] getEpoch()
	{
		return m_afEpochs;
	}

	public int getExpirationDate()
	{
		return expirationDate;
	}

	public int getFloodBegins()
	{
		return floodBegins;
	}

	// public TideStationHeader getHeader()
	// {
	// return header;
	// }
	//
	// public void setHeader(TideStationHeader header)
	// {
	// this.header = header;
	// }
	//

	public int getID()
	{
		return m_nRecNum;
	}

	public int getLastDateOnStation()
	{
		return lastDateOnStation;
	}

	public double getLat()
	{
		return m_fLatitude;
	}

	public byte getLegalese()
	{
		return legalese;
	}

	public byte getLevelUnits()
	{
		return levelUnits;
	}

	public double getLon()
	{
		return m_fLongitude;
	}

	public int getMaxDirection()
	{
		return maxDirection;
	}

	public double getMaxLevelAdd()
	{
		return maxLevelAdd;
	}

	public double getMaxLevelMultiply()
	{
		return maxLevelMultiply;
	}

	public int getMaxTimeAdd()
	{
		return maxTimeAdd;
	}

	public int getMinDirection()
	{
		return minDirection;
	}

	public double getMinLevelAdd()
	{
		return minLevelAdd;
	}

	public double getMinLevelMultiply()
	{
		return minLevelMultiply;
	}

	public int getMinTimeAdd()
	{
		return minTimeAdd;
	}

	public int getMonthsOnStation()
	{
		return monthsOnStation;
	}

	public String getName()
	{
		return m_strName;
	}

	public String getNotes()
	{
		return notes;
	}

	public int getRecordSize()
	{
		return m_nRecSize;
	}

	/**
	 * 
	 * @return TideRecord.EStatType
	 */
	public TideRecord.EStatType getRecordType()
	{
		return recordType;
	}

	public int getReferenceStation()
	{
		return referenceStation;
	}

	public byte getRestriction()
	{
		return restriction;
	}

	public String getSource()
	{
		return source;
	}

	public String getStationId()
	{
		return m_NOAAStationID;
	}

	public String getStationIdContext()
	{
		return m_NOAAStationIDContext;
	}

	public int getTzFile()
	{
		return tzfile;
	}

	public String getXfields()
	{
		return xfields;
	}

	public int getZoneOffset()
	{
		return zoneOffset;
	}

	public void setAmplitude(double[] amplitude)
	{
		this.m_afAmplitudes = amplitude;
	}

	public void setComments(String comments)
	{
		this.comments = comments;
	}

	public void setConfidence(byte confidence)
	{
		this.confidence = confidence;
	}

	public void setCountry(int country)
	{
		this.m_nCountry = country;
	}

	public void setDateImported(int dateImported)
	{
		this.dateImported = dateImported;
	}

	public void setDatum(int datum)
	{
		this.datum = datum;
	}

	public void setDatumOffset(double datumOffset)
	{
		this.datumOffset = datumOffset;
	}

	public void setDirectionUnits(byte direction_units)
	{
		this.directionUnits = direction_units;
	}

	public void setEbbBegins(int ebbBegins)
	{
		this.ebbBegins = ebbBegins;
	}

	public void setEpoch(double[] epoch)
	{
		this.m_afEpochs = epoch;
	}

	public void setExpirationDate(int expirationDate)
	{
		this.expirationDate = expirationDate;
	}

	public void setFloodBegins(int floodBegins)
	{
		this.floodBegins = floodBegins;
	}

	public void setLastDateOnStation(int lastDateOnStation)
	{
		this.lastDateOnStation = lastDateOnStation;
	}

	public void setLat(int lat)
	{
		this.m_fLatitude = lat;
	}

	public void setLegalese(byte legalese)
	{
		this.legalese = legalese;
	}

	public void setLevelUnits(byte levelUnits)
	{
		this.levelUnits = levelUnits;
	}

	public void setLon(int lon)
	{
		this.m_fLongitude = lon;
	}

	public void setMaxDirection(int maxDirection)
	{
		this.maxDirection = maxDirection;
	}

	public void setMaxLevelAdd(double maxLevelAdd)
	{
		this.maxLevelAdd = maxLevelAdd;
	}

	public void setMaxLevelMultiply(double maxLevelMultiply)
	{
		this.maxLevelMultiply = maxLevelMultiply;
	}

	public void setMaxTimeAdd(int maxTimeAdd)
	{
		this.maxTimeAdd = maxTimeAdd;
	}

	public void setMinDirection(int minDirection)
	{
		this.minDirection = minDirection;
	}

	public void setMinLevelAdd(double minLevelAdd)
	{
		this.minLevelAdd = minLevelAdd;
	}

	public void setMinLevelMultiply(double minLevelMultiply)
	{
		this.minLevelMultiply = minLevelMultiply;
	}

	public void setMinTimeAdd(int minTimeAdd)
	{
		this.minTimeAdd = minTimeAdd;
	}

	public void setMonthsOnStation(int monthsOnStation)
	{
		this.monthsOnStation = monthsOnStation;
	}

	public void setName(String name)
	{
		this.m_strName = name;
	}

	public void setNotes(String notes)
	{
		this.notes = notes;
	}

	public void setRecordSize(int recordSize)
	{
		this.m_nRecSize = recordSize;
	}

	public void setRecordType(TideRecord.EStatType record_type)
	{
		this.recordType = record_type;
	}

	public void setReferenceStation(int referenceStation)
	{
		this.referenceStation = referenceStation;
	}

	public void setRestriction(byte restriction)
	{
		this.restriction = restriction;
	}

	public void setSource(String source)
	{
		this.source = source;
	}

	public void setStationId(String stationId)
	{
		this.m_NOAAStationID = stationId;
	}

	public void setStationIdContext(String stationIdContext)
	{
		this.m_NOAAStationIDContext = stationIdContext;
	}

	public void setTzFile(int tzfile)
	{
		this.tzfile = tzfile;
	}

	public void setXfields(String xfields)
	{
		this.xfields = xfields;
	}

	public void setZoneOffset(int zoneOffset)
	{
		this.zoneOffset = zoneOffset;
	}

	/**
	 * reads one record from buffer and return new record object
	 * 
	 * @param recNum
	 * @return
	 * @throws TideDBException
	public TideRecord readTideRecord(int recNum) throws TideDBException
	{
		if (recNum < 0 || recNum >= header.getPub().getNumberOfRecords())
			throw new TideDBException("Invalid record number requested");
	
		currentRecord = recNum;
	//		buffer.position(tindex[recNum].getAddress());
		return new TideRecord(this, buffer, recNum, buffer.position(records[recNum].getAddress()));
		// require (fseek (fp, tindex[num].address, SEEK_SET) == 0);
		// chk_fread (buf, tindex[num].record_size, 1, fp);
		// unpack_tide_record (buf, bufsize, rec);
		// free (buf);
		// return num;
	}
	 */

	/** 
	 * Gets constituents from a TIDE_RECORD, adjusting if needed.
	 * 
	 * @param adjustments
	 * @return
	 */
	public ConstituentSet getConstituents(SimpleOffsets adjustments)
	{
		assert (getRecordType() == TideRecord.EStatType.REFERENCE_STATION);

		DbHeaderPublic dbHeader = getDB().getHeader().getPub();
		AHTidePredictionUnits amp_units = AHTUnits.parse(getDB().getLevelUnits(getLevelUnits()));
		LinkedList<Constituent> constituents = new LinkedList<>();

		// logger.log(java.util.logging.Level.FINE, "get constituents for " + rec.getName());

		// Null constituents are eliminated here.
		for (int i = 0; i < dbHeader.getConstituents(); ++i)
		{
			if (getAmplitude()[i] > 0.0)
			{
				// logger.log(java.util.logging.Level.FINE,
				// String.format("new const speed = %f, amp = %f, phase = %f",
				// tideDB.getSpeed(i), rec.getAmplitude()[i], rec.getEpoch()[i]));
				constituents.add(new Constituent(getDB().getSpeed(i), dbHeader.getStartYear(), dbHeader.getNumberOfYears(), getDB().getEquilibriums(i), getDB()
						.getNodeFactors(i), new Amplitude(amp_units, getAmplitude()[i]), getEpoch()[i]));
			}
		}

		assert (constituents.size() != 0);

		PredictionValue datum = new PredictionValue(AHTUnits.flatten(amp_units), getDatumOffset());

		// Normalize the meridian to UTC.
		// To compensate for a negative meridian requires a positive offset.
		// (This adjustment is combined with any that were passed in.)

		// This is the only place where mutable offsets would make even a
		// little bit of sense.
		adjustments = new SimpleOffsets(adjustments.getTimeAdd().subtract(new Interval(TideDB.getTimeNeatStr(getZoneOffset()))), adjustments.getLevelAdd(),
				adjustments.getLevelMultiply());

		ConstituentSet cs = new ConstituentSet(constituents, datum, adjustments);

		// String u(Global::settings["u"].s);
		// if
		// ( u
		// != "x")
		// cs.setUnits (Units::parse (u));

		return cs;
	}
	
	/**
	 * 
	 * @return HairyOffsets of the record
	 */
	public HairyOffsets getHairyOffsets()
	{
		NullableInterval tempFloodBegins = new NullableInterval(), tempEbbBegins = new NullableInterval();

		// For these, zero is not the same as null.
		if (getFloodBegins() != TideDB.NULL_SLACK_OFFSET)
			tempFloodBegins = new NullableInterval(new Interval(TideDB.getTimeNeatStr(getFloodBegins())));
		if (getEbbBegins() != TideDB.NULL_SLACK_OFFSET)
			tempEbbBegins = new NullableInterval(new Interval(TideDB.getTimeNeatStr(getEbbBegins())));

		AHTidePredictionUnits lu = tideDB.levelAddUnits(this);
		HairyOffsets ho = new HairyOffsets(new SimpleOffsets(new Interval(TideDB.getTimeNeatStr(getMaxTimeAdd())), new PredictionValue(lu, getMaxLevelAdd()),
				getMaxLevelMultiply()), new SimpleOffsets(new Interval(TideDB.getTimeNeatStr(getMinTimeAdd())), new PredictionValue(lu, getMinLevelAdd()),
				getMinLevelMultiply()), tempFloodBegins, tempEbbBegins);
		return ho;
	}

	/**
	 * @return the tideDB
	 */
	public TideDB getDB()
	{
		return tideDB;
	}

	/**
	 * @param tideDB the tideDB to set
	 */
	public void setDB(TideDB tideDB)
	{
		this.tideDB = tideDB;
	}
}
