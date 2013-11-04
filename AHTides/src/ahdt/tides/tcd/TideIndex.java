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

import ahdt.tides.base.XByteBuffer;

/**
 * 
 * @author chas
 */
public class TideIndex
{
	private int address;
	private long recordSize;
	private int tzfile;
	private int referenceStation;
	private double lat;
	private double lon;
	private TideRecord.EStatType recordType;
	private String name;

	// private XByteBuffer buffer;
	private int recNum;

	/**
	 * partial information from record data it includes 
	 * the internal number, 
	 * the address in the buffer, 
	 * the size of the record in bytes, 
	 * the stations type, position and name, 
	 * as well as timezone and referenced station if subordinate station
	 * 
	 * @param header
	 * @param buffer
	 * @param recNum
	 * @param bufferPos
	 */
	TideIndex(TideDBHeader header, XByteBuffer buffer, int recNum, int bufferPos)
	{
		this.recNum = recNum;
		this.address = bufferPos;
		buffer.position(bufferPos);
		this.recordSize = buffer.unpack(header.getRecordSizeBits());
		this.recordType = TideRecord.EStatType.fromInt(buffer.unpack(header.getRecordTypeBits()));
		double tempLat = buffer.signedUnpack(header.getLatitudeBits());
		this.lat = tempLat / header.getLatitudeScale();
		double tempLon = buffer.signedUnpack(header.getLongitudeBits());
		this.lon = tempLon / header.getLongitudeScale();
		this.tzfile = buffer.unpack(header.getTzfileBits());
		this.name = buffer.unpackString(DbHeaderPublic.ONELINER_LENGTH, AHTideTCDStr.getString("TideIndex.0")); //$NON-NLS-1$
		this.referenceStation = buffer.signedUnpack(header.getStationBits());
	}

	public int getID()
	{
		return recNum;
	}

	/**
	 * gets the buffer position of the record
	 */
	public int getAddress()
	{
		return address;
	}

	public void setAddress(int address)
	{
		this.address = address;
	}

	public double getLat()
	{
		return lat;
	}

	public void setLat(int lat)
	{
		this.lat = lat;
	}

	public double getLon()
	{
		return lon;
	}

	public void setLon(int lon)
	{
		this.lon = lon;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public long getRecordSize()
	{
		return recordSize;
	}

	public void setRecordSize(long recordSize)
	{
		this.recordSize = recordSize;
	}

	public TideRecord.EStatType getRecord_type()
	{
		return recordType;
	}

	public void setRecord_type(TideRecord.EStatType record_type)
	{
		this.recordType = record_type;
	}

	public int getReferenceStation()
	{
		return referenceStation;
	}

	public void setReferenceStation(int referenceStation)
	{
		this.referenceStation = referenceStation;
	}

	public int getTzfile()
	{
		return tzfile;
	}

	public void setTzfile(int tzfile)
	{
		this.tzfile = tzfile;
	}

}
