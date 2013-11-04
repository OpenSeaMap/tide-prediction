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

import ahdt.tides.tcd.DbHeaderPublic;
import ahdt.tides.tcd.TideDB;
import ahdt.tides.tcd.TideIndex;
import ahdt.tides.tcd.TideRecord;

/**
 * 
 * @author chas
 */
public final class TideStationHeader
{
	private int recordNumber;
	private int recordSize;
	private TideRecord.EStatType recordType;
	private double latitude;
	private double longitude;
	private int referenceStation;
	private int tzFile;
	private String name;

	public TideStationHeader(TideDB tideDB, XByteBuffer buffer, TideIndex tideIndex, int recNum)
	{
		setRecordNumber(recNum);
		// need to set buffer position
		buffer.bitMark();

		setRecordSize(buffer.unpack(tideDB.getHeader().getRecordSizeBits()));

		setRecordType(TideRecord.EStatType.fromInt((byte) buffer.unpack(tideDB.getHeader().getRecordTypeBits())));

		setLatitude((double) buffer.signedUnpack(tideDB.getHeader().getLatitudeBits()) / tideDB.getHeader().getLatitudeScale());

		setLongitude((double) buffer.signedUnpack(tideDB.getHeader().getLongitudeBits()) / tideDB.getHeader().getLongitudeScale());

		/*
		 * This ordering doesn't match everywhere else but there's no technical
		 * reason to change it from its V1 ordering.
		 */

		setTzFile(buffer.unpack(tideDB.getHeader().getTzfileBits()));

		setName(buffer.unpackString(DbHeaderPublic.ONELINER_LENGTH, AHTideBaseStr.getString("TideStationHeader.0"))); //$NON-NLS-1$

		setReferenceStation(buffer.signedUnpack(tideDB.getHeader().getStationBits()));

		// assert (*pos <= bufsize*8);
	}

	public double getLatitude()
	{
		return latitude;
	}

	public void setLatitude(double latitude)
	{
		this.latitude = latitude;
	}

	public double getLongitude()
	{
		return longitude;
	}

	public void setLongitude(double longitude)
	{
		this.longitude = longitude;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public int getRecordNumber()
	{
		return recordNumber;
	}

	public void setRecordNumber(int recordNumber)
	{
		this.recordNumber = recordNumber;
	}

	public int getRecordSize()
	{
		return recordSize;
	}

	public void setRecordSize(int recordSize)
	{
		this.recordSize = recordSize;
	}

	public TideRecord.EStatType getRecordType()
	{
		return recordType;
	}

	public void setRecordType(TideRecord.EStatType recordType)
	{
		this.recordType = recordType;
	}

	public int getReferenceStation()
	{
		return referenceStation;
	}

	public void setReferenceStation(int referenceStation)
	{
		this.referenceStation = referenceStation;
	}

	public int getTzFile()
	{
		return tzFile;
	}

	public void setTzFile(int tzFile)
	{
		this.tzFile = tzFile;
	}

}
