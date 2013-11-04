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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import ahdt.tides.base.XByteBuffer;

/**
 * 
 * @author chas
 */
public final class TideDBHeader
{
	Logger logger = Logger.getLogger(this.getClass().getName());

	//	private final String NO_VERSION_STRING = Messages.getString("TideDBHeader.0"); //$NON-NLS-1$

	private DbHeaderPublic pub;
	private List<String> constituent = new LinkedList<>();
	private List<Double> speeds = new LinkedList<>();
	private List<List<Double>> equilibrium = new LinkedList<>();
	private List<List<Double>> nodeFactor = new LinkedList<>();
	private List<String> levelUnits = new LinkedList<>();
	private List<String> dirUnits = new LinkedList<>();
	private List<String> restrictions = new LinkedList<>();
	private List<String> tzfiles = new LinkedList<>();
	private List<String> countries = new LinkedList<>();
	private List<String> datums = new LinkedList<>();
	private List<String> legaleses = new LinkedList<>();
	private long headerSize;
	private long speedBits;
	private long speedScale;
	private int speedOffset;
	private long equilibriumBits;
	private long equilibriumScale;
	private int equilibriumOffset;
	private long nodeBits;
	private long nodeScale;
	private int nodeOffset;
	private long amplitudeBits;
	private long amplitudeScale;
	private long epochBits;
	private long epochScale;
	private long constituentBits;
	private long recordTypeBits;
	private long latitudeScale;
	private long latitudeBits;
	private long longitudeScale;
	private long longitudeBits;
	private long recordSizeBits;
	private long stationBits;
	private long levelUnitBits;
	private long dirUnitBits;
	private long restrictionBits;
	private long maxRestrictionTypes;
	private long tzfileBits;
	private long maxTzfiles;
	private long countryBits;
	private long maxCountries;
	private long datumBits;
	private long maxDatumTypes;
	private long legaleseBits;
	private long maxLegaleses;
	private long datumOffsetBits;
	private long datumOffsetScale;
	private long dateBits;
	private long monthsOnStationBits;
	private long confidenceValueBits;
	private long timeBits;
	private long levelAddBits;
	private long levelAddScale;
	private long levelMultiplyBits;
	private long levelMultiplyScale;
	private long directionBits;
	private long constituentSize;
	private long levelUnitSize;
	private long dirUnitSize;
	private long restrictionSize;
	private long tzfileSize;
	private long countrySize;
	private long datumSize;
	private long legaleseSize;
	private long endOfFile;

	/* Need these to read V1 files. */
	private long pedigreeBits;
	private long pedigreeSize;

	private XByteBuffer buffer;
	private List<TideIndex> tindex;

	/**
	 * TideHeaderData reads first the plain text header part of the tcd file and then fills the index list with selected data of the records.
	 * 
	 * @param buffer
	 * @param tindex
	 * @throws TideDBException
	 */
	public TideDBHeader(XByteBuffer buffer) throws TideDBException
	{
		this.buffer = buffer;
		this.tindex = tindex;

		pub = new DbHeaderPublic();
		pub.setVersion(AHTideTCDStr.getString("TideDBHeader.0"));
		String read;
		int info = 0;
		do
		{
			read = buffer.getStringTo('\n');
			read = read.trim();
			// skip length check -- we don't care
			if (read.equals(AHTideTCDStr.getString("TideDBHeader.1"))) //$NON-NLS-1$
			{
				break;
				/* All other lines must be field = value */
			}
			String[] parts = read.split(AHTideTCDStr.getString("TideDBHeader.2")); //$NON-NLS-1$
			if (parts.length != 2)
			{
				throw new TideDBException(AHTideTCDStr.getString("TideDBHeader.3") + read); //$NON-NLS-1$
			}
			++info;
			switch (parts[0].trim())
			{
			case "[VERSION]": //$NON-NLS-1$
				pub.setVersion(parts[1].trim());
				break;
			case "[MAJOR REV]": //$NON-NLS-1$
				pub.setMajorRev(Integer.parseInt(parts[1].trim()));
				break;
			case "[MINOR REV]": //$NON-NLS-1$
				pub.setMinorRev(Integer.parseInt(parts[1].trim()));
				break;
			case "[LAST MODIFIED]": //$NON-NLS-1$
				pub.setLastModified(parts[1].trim());
				break;
			case "[NUMBER OF RECORDS]": //$NON-NLS-1$
				pub.setNumberOfRecords(Integer.parseInt(parts[1].trim()));
				break;
			case "[START YEAR]": //$NON-NLS-1$
				pub.setStartYear(Integer.parseInt(parts[1].trim()));
				break;
			case "[NUMBER OF YEARS]": //$NON-NLS-1$
				pub.setNumberOfYears(Integer.parseInt(parts[1].trim()));
				break;
			case "[CONSTITUENTS]": //$NON-NLS-1$
				pub.setConstituents(Integer.parseInt(parts[1].trim()));
				break;
			case "[LEVEL UNIT TYPES]": //$NON-NLS-1$
				pub.setLevelUnitTypes(Integer.parseInt(parts[1].trim()));
				break;
			case "[DIRECTION UNIT TYPES]": //$NON-NLS-1$
				pub.setDirUnitTypes(Integer.parseInt(parts[1].trim()));
				break;
			case "[RESTRICTION TYPES]": //$NON-NLS-1$
				pub.setRestrictionTypes(Integer.parseInt(parts[1].trim()));
				break;
			case "[PEDIGREE TYPES]": //$NON-NLS-1$
				pub.setPedigreeTypes(Integer.parseInt(parts[1].trim()));
				break;
			case "[TZFILES]": //$NON-NLS-1$
				pub.setTzfiles(Integer.parseInt(parts[1].trim()));
				break;
			case "[COUNTRIES]": //$NON-NLS-1$
				pub.setCountries(Integer.parseInt(parts[1].trim()));
				break;
			case "[DATUM TYPES]": //$NON-NLS-1$
				pub.setDatumTypes(Integer.parseInt(parts[1].trim()));
				break;
			case "[LEGALESES]": //$NON-NLS-1$
				pub.setLegaleses(Integer.parseInt(parts[1].trim()));
				break;
			case "[HEADER SIZE]": //$NON-NLS-1$
				headerSize = Integer.parseInt(parts[1].trim());
				break;
			case "[SPEED BITS]": //$NON-NLS-1$
				speedBits = Integer.parseInt(parts[1].trim());
				break;
			case "[SPEED SCALE]": //$NON-NLS-1$
				speedScale = Integer.parseInt(parts[1].trim());
				break;
			case "[SPEED OFFSET]": //$NON-NLS-1$
				speedOffset = Integer.parseInt(parts[1].trim());
				break;
			case "[EQUILIBRIUM BITS]": //$NON-NLS-1$
				equilibriumBits = Integer.parseInt(parts[1].trim());
				break;
			case "[EQUILIBRIUM SCALE]": //$NON-NLS-1$
				equilibriumScale = Integer.parseInt(parts[1].trim());
				break;
			case "[EQUILIBRIUM OFFSET]": //$NON-NLS-1$
				equilibriumOffset = Integer.parseInt(parts[1].trim());
				break;
			case "[NODE BITS]": //$NON-NLS-1$
				nodeBits = Integer.parseInt(parts[1].trim());
				break;
			case "[NODE SCALE]": //$NON-NLS-1$
				nodeScale = Integer.parseInt(parts[1].trim());
				break;
			case "[NODE OFFSET]": //$NON-NLS-1$
				nodeOffset = Integer.parseInt(parts[1].trim());
				break;
			case "[AMPLITUDE BITS]": //$NON-NLS-1$
				amplitudeBits = Integer.parseInt(parts[1].trim());
				break;
			case "[AMPLITUDE SCALE]": //$NON-NLS-1$
				amplitudeScale = Integer.parseInt(parts[1].trim());
				break;
			case "[EPOCH BITS]": //$NON-NLS-1$
				epochBits = Integer.parseInt(parts[1].trim());
				break;
			case "[EPOCH SCALE]": //$NON-NLS-1$
				epochScale = Integer.parseInt(parts[1].trim());
				break;
			case "[CONSTITUENT BITS]": //$NON-NLS-1$
				constituentBits = Integer.parseInt(parts[1].trim());
				break;
			case "[LEVEL UNIT BITS]": //$NON-NLS-1$
				levelUnitBits = Integer.parseInt(parts[1].trim());
				break;
			case "[DIRECTION UNIT BITS]": //$NON-NLS-1$
				dirUnitBits = Integer.parseInt(parts[1].trim());
				break;
			case "[RESTRICTION BITS]": //$NON-NLS-1$
				restrictionBits = Integer.parseInt(parts[1].trim());
				break;
			case "[PEDIGREE BITS]": //$NON-NLS-1$
				pedigreeBits = Integer.parseInt(parts[1].trim());
				break;
			case "[TZFILE BITS]": //$NON-NLS-1$
				tzfileBits = Integer.parseInt(parts[1].trim());
				break;
			case "[COUNTRY BITS]": //$NON-NLS-1$
				countryBits = Integer.parseInt(parts[1].trim());
				break;
			case "[DATUM BITS]": //$NON-NLS-1$
				datumBits = Integer.parseInt(parts[1].trim());
				break;
			case "[LEGALESE BITS]": //$NON-NLS-1$
				legaleseBits = Integer.parseInt(parts[1].trim());
				break;
			case "[RECORD TYPE BITS]": //$NON-NLS-1$
				recordTypeBits = Integer.parseInt(parts[1].trim());
				break;
			case "[LATITUDE SCALE]": //$NON-NLS-1$
				latitudeScale = Integer.parseInt(parts[1].trim());
				break;
			case "[LATITUDE BITS]": //$NON-NLS-1$
				latitudeBits = Integer.parseInt(parts[1].trim());
				break;
			case "[LONGITUDE SCALE]": //$NON-NLS-1$
				longitudeScale = Integer.parseInt(parts[1].trim());
				break;
			case "[LONGITUDE BITS]": //$NON-NLS-1$
				longitudeBits = Integer.parseInt(parts[1].trim());
				break;
			case "[RECORD SIZE BITS]": //$NON-NLS-1$
				recordSizeBits = Integer.parseInt(parts[1].trim());
				break;
			case "[STATION BITS]": //$NON-NLS-1$
				stationBits = Integer.parseInt(parts[1].trim());
				break;
			case "[DATUM OFFSET BITS]": //$NON-NLS-1$
				datumOffsetBits = Integer.parseInt(parts[1].trim());
				break;
			case "[DATUM OFFSET SCALE]": //$NON-NLS-1$
				datumOffsetScale = Integer.parseInt(parts[1].trim());
				break;
			case "[DATE BITS]": //$NON-NLS-1$
				dateBits = Integer.parseInt(parts[1].trim());
				break;
			case "[MONTHS ON STATION BITS]": //$NON-NLS-1$
				monthsOnStationBits = Integer.parseInt(parts[1].trim());
				break;
			case "[CONFIDENCE VALUE BITS]": //$NON-NLS-1$
				confidenceValueBits = Integer.parseInt(parts[1].trim());
				break;
			case "[TIME BITS]": //$NON-NLS-1$
				timeBits = Integer.parseInt(parts[1].trim());
				break;
			case "[LEVEL ADD BITS]": //$NON-NLS-1$
				levelAddBits = Integer.parseInt(parts[1].trim());
				break;
			case "[LEVEL ADD SCALE]": //$NON-NLS-1$
				levelAddScale = Integer.parseInt(parts[1].trim());
				break;
			case "[LEVEL MULTIPLY BITS]": //$NON-NLS-1$
				levelMultiplyBits = Integer.parseInt(parts[1].trim());
				break;
			case "[LEVEL MULTIPLY SCALE]": //$NON-NLS-1$
				levelMultiplyScale = Integer.parseInt(parts[1].trim());
				break;
			case "[DIRECTION BITS]": //$NON-NLS-1$
				directionBits = Integer.parseInt(parts[1].trim());
				break;
			case "[CONSTITUENT SIZE]": //$NON-NLS-1$
				constituentSize = Integer.parseInt(parts[1].trim());
				break;
			case "[LEVEL UNIT SIZE]": //$NON-NLS-1$
				levelUnitSize = Integer.parseInt(parts[1].trim());
				break;
			case "[DIRECTION UNIT SIZE]": //$NON-NLS-1$
				dirUnitSize = Integer.parseInt(parts[1].trim());
				break;
			case "[RESTRICTION SIZE]": //$NON-NLS-1$
				restrictionSize = Integer.parseInt(parts[1].trim());
				break;
			case "[PEDIGREE SIZE]": //$NON-NLS-1$
				pedigreeSize = Integer.parseInt(parts[1].trim());
				break;
			case "[TZFILE SIZE]": //$NON-NLS-1$
				tzfileSize = Integer.parseInt(parts[1].trim());
				break;
			case "[COUNTRY SIZE]": //$NON-NLS-1$
				countrySize = Integer.parseInt(parts[1].trim());
				break;
			case "[DATUM SIZE]": //$NON-NLS-1$
				datumSize = Integer.parseInt(parts[1].trim());
				break;
			case "[LEGALESE SIZE]": //$NON-NLS-1$
				legaleseSize = Integer.parseInt(parts[1].trim());
				break;
			case "[END OF FILE]": //$NON-NLS-1$
				endOfFile = Integer.parseInt(parts[1].trim());
				break;
			case "[LEGALESE TYPES]": //$NON-NLS-1$
				break;
			default:
				throw new TideDBException(AHTideTCDStr.getString("TideDBHeader.72") + parts[0] + AHTideTCDStr.getString("TideDBHeader.73")); //$NON-NLS-1$ //$NON-NLS-2$
			}

		} while ( !read.equals(AHTideTCDStr.getString("TideDBHeader.74"))); //$NON-NLS-1$

		if (pub.getVersion().equals(AHTideTCDStr.getString("TideDBHeader.0")))
			throw new TideDBException(AHTideTCDStr.getString("TideDBHeader.75")); //$NON-NLS-1$

		if (pub.getMajorRev() > ahdt.tides.base.Defaults.LIBTCD_COMPATIBILITY_MAJOR_REV)
			throw new TideDBException(AHTideTCDStr.getString("TideDBHeader.76")); //$NON-NLS-1$

		// checksum resides behind header
		buffer.position((int) headerSize);

		int checksum = buffer.getInt();
		int calcChecksum = headerChecksum();
		if ((checksum != 0) && (checksum != calcChecksum))
		{
			// throw new
			// TideDBException(String.format("Checksum calculated was %X, expected %X.",
			// calcChecksum, checksum));
			// TODO: fix checksum
			logger.warning(String.format(AHTideTCDStr.getString("TideDBHeader.77"), calcChecksum, checksum)); //$NON-NLS-1$
		}

		/* Set the max possible restrictions types based on the number of bits used. */
		setMaxRestrictionTypes((long) Math.pow(2.0, getRestrictionBits()));

		/* Set the max possible tzfiles based on the number of bits used. */
		setMaxTzfiles((long) (Math.pow(2.0, getTzfileBits())));

		/* Set the max possible countries based on the number of bits used. */
		setMaxCountries((long) (Math.pow(2.0, getCountryBits())));

		/* Set the max possible datums types based on the number of bits used. */
		setMaxDatumTypes((long) (Math.pow(2.0, getDatumBits())));

		/* Set the max possible legaleses based on the number of bits used. */
		if (getPub().getMajorRev() < 2)
			setMaxLegaleses(1);
		else
			setMaxLegaleses((long) (Math.pow(2.0, getLegaleseBits())));

		/* Read level units. */
		readStringList(getLevelUnits(), getPub().getLevelUnitTypes(), getLevelUnitSize());

		/* Read direction units. */
		readStringList(getDirUnits(), getPub().getDirUnitTypes(), getDirUnitSize());

		/* Read restrictions. */
		int count = readStringListVariable(getRestrictions(), getMaxRestrictionTypes(), getRestrictionSize());
		getPub().setRestrictionTypes(count);

		/* Skip pedigrees. */
		if (getPub().getMajorRev() < 2)
			throw new TideDBException(AHTideTCDStr.getString("TideDBHeader.78")); //$NON-NLS-1$

		getPub().setPedigreeTypes(1);

		/* Read tzfiles. */
		count = readStringListVariable(getTzfiles(), getMaxTzfiles(), getTzfileSize());
		getPub().setTzfiles(count);

		/* Read countries. */
		count = readStringListVariable(getCountries(), getMaxCountries(), getCountrySize());
		getPub().setCountries(count);

		/* Read datums. */
		count = readStringListVariable(getDatums(), getMaxDatumTypes(), getDatumSize());
		getPub().setDatumTypes(count);

		/* Read legaleses. */
		if (getPub().getMajorRev() < 2)
			throw new TideDBException(AHTideTCDStr.getString("TideDBHeader.79")); //$NON-NLS-1$

		count = readStringListVariable(getLegalese(), getMaxLegaleses(), getLegaleseSize());
		getPub().setLegaleses(count);

		/* Read constituent names. */
		readStringList(getConstituent(), getPub().getConstituents(), getConstituentSize());

		if (getSpeedOffset() < 0 || getEquilibriumOffset() < 0 || getNodeOffset() < 0)
			throw new TideDBException(AHTideTCDStr.getString("TideDBHeader.80")); //$NON-NLS-1$

		/* NOTE: Using bit_unpack to get integers. */

		/* Read speeds. */
		buffer.bitMark();
		buffer.roundUpToNextByte();
		speeds = readDoubleList(getPub().getConstituents(), getSpeedBits(), getSpeedOffset(), getSpeedScale());
		// int c = 0;
		// for (Double dub : speeds) {
		// logger.debug(String.format("const %d speed = %f", c++, dub));
		// }

		logger.log(java.util.logging.Level.FINE, AHTideTCDStr.getString("TideDBHeader.81") + buffer.position()); //$NON-NLS-1$
		logger
				.log(
						java.util.logging.Level.FINE,
						AHTideTCDStr.getString("TideDBHeader.82") + getPub().getConstituents() + AHTideTCDStr.getString("TideDBHeader.83") + getPub().getNumberOfYears() + AHTideTCDStr.getString("TideDBHeader.84") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
								+ getEquilibriumBits() + AHTideTCDStr.getString("TideDBHeader.85")); //$NON-NLS-1$
		// round up to next byte

		/* Read equilibrium arguments. */
		buffer.roundUpToNextByte();
		readDoubleListList(getEquilibrium(), getPub().getConstituents(), getPub().getNumberOfYears(), getEquilibriumBits(), getEquilibriumOffset(),
				getEquilibriumScale());
		// int c = 0;
		// for (List<Double> subList : getEquilibrium()) {
		// String output = "equi " + c++ + " ";
		// for (Double dub : subList) {
		// output += " " + dub;
		// }
		// logger.debug(output);
		// }

		buffer.roundUpToNextByte();
		/* Read node factors. */
		readDoubleListList(getNodeFactor(), getPub().getConstituents(), getPub().getNumberOfYears(), getNodeBits(), getNodeOffset(), getNodeScale());

		buffer.roundUpToNextByte();

		/* 20130206 AH moved outside
		// all header information in tcd file read. now the records are following
		// Read the header portion of all of the records in the file and save the record size, address, and name.
		for (int i = 0; i < getPub().getNumberOfRecords(); i++ )
		{
			int bufferPos = buffer.position();
			if (i != 0)
			{
				bufferPos = (int) (tindex.get(i - 1).getAddress() + tindex.get(i - 1).getRecordSize());
			}
			TideIndex index = new TideIndex(this, buffer, i, bufferPos);
			tindex.add(index);
		}
		logger.log(java.util.logging.Level.FINE, "Indexes = " + tindex.size());
		*/

	}

	public long getAmplitudeBits()
	{
		return amplitudeBits;
	}

	public void setAmplitudeBits(long amplitudeBits)
	{
		this.amplitudeBits = amplitudeBits;
	}

	public long getAmplitudeScale()
	{
		return amplitudeScale;
	}

	public void setAmplitudeScale(long amplitudeScale)
	{
		this.amplitudeScale = amplitudeScale;
	}

	public long getConfidenceValueBits()
	{
		return confidenceValueBits;
	}

	public void setConfidenceValueBits(long confidenceValueBits)
	{
		this.confidenceValueBits = confidenceValueBits;
	}

	public List<String> getConstituent()
	{
		return constituent;
	}

	public long getConstituentBits()
	{
		return constituentBits;
	}

	public void setConstituentBits(long constituentBits)
	{
		this.constituentBits = constituentBits;
	}

	public long getConstituentSize()
	{
		return constituentSize;
	}

	public void setConstituentSize(long constituentSize)
	{
		this.constituentSize = constituentSize;
	}

	public List<String> getCountries()
	{
		return countries;
	}

	public long getCountryBits()
	{
		return countryBits;
	}

	public void setCountryBits(long countryBits)
	{
		this.countryBits = countryBits;
	}

	public long getCountrySize()
	{
		return countrySize;
	}

	public void setCountrySize(long countrySize)
	{
		this.countrySize = countrySize;
	}

	public long getDateBits()
	{
		return dateBits;
	}

	public void setDateBits(long dateBits)
	{
		this.dateBits = dateBits;
	}

	public List<String> getDatums()
	{
		return datums;
	}

	public long getDatumBits()
	{
		return datumBits;
	}

	public void setDatumBits(long datumBits)
	{
		this.datumBits = datumBits;
	}

	public long getDatumOffsetBits()
	{
		return datumOffsetBits;
	}

	public void setDatumOffsetBits(long datumOffsetBits)
	{
		this.datumOffsetBits = datumOffsetBits;
	}

	public long getDatumOffsetScale()
	{
		return datumOffsetScale;
	}

	public void setDatumOffsetScale(long datumOffsetScale)
	{
		this.datumOffsetScale = datumOffsetScale;
	}

	public long getDatumSize()
	{
		return datumSize;
	}

	public void setDatumSize(long datumSize)
	{
		this.datumSize = datumSize;
	}

	public List<String> getDirUnits()
	{
		return dirUnits;
	}

	public long getDirUnitBits()
	{
		return dirUnitBits;
	}

	public void setDirUnitBits(long dirUnitBits)
	{
		this.dirUnitBits = dirUnitBits;
	}

	public long getDirUnitSize()
	{
		return dirUnitSize;
	}

	public void setDirUnitSize(long dirUnitSize)
	{
		this.dirUnitSize = dirUnitSize;
	}

	public long getDirectionBits()
	{
		return directionBits;
	}

	public void setDirectionBits(long directionBits)
	{
		this.directionBits = directionBits;
	}

	public long getEndOfFile()
	{
		return endOfFile;
	}

	public void setEndOfFile(long endOfFile)
	{
		this.endOfFile = endOfFile;
	}

	public long getEpochBits()
	{
		return epochBits;
	}

	public void setEpochBits(long epochBits)
	{
		this.epochBits = epochBits;
	}

	public long getEpochScale()
	{
		return epochScale;
	}

	public void setEpochScale(long epochScale)
	{
		this.epochScale = epochScale;
	}

	public List<List<Double>> getEquilibrium()
	{
		return equilibrium;
	}

	public long getEquilibriumBits()
	{
		return equilibriumBits;
	}

	public void setEquilibriumBits(long equilibriumBits)
	{
		this.equilibriumBits = equilibriumBits;
	}

	public int getEquilibriumOffset()
	{
		return equilibriumOffset;
	}

	public void setEquilibriumOffset(int equilibriumOffset)
	{
		this.equilibriumOffset = equilibriumOffset;
	}

	public long getEquilibriumScale()
	{
		return equilibriumScale;
	}

	public void setEquilibriumScale(long equilibriumScale)
	{
		this.equilibriumScale = equilibriumScale;
	}

	public long getHeaderSize()
	{
		return headerSize;
	}

	public void setHeaderSize(long headerSize)
	{
		this.headerSize = headerSize;
	}

	public long getLatitudeBits()
	{
		return latitudeBits;
	}

	public void setLatitudeBits(long latitudeBits)
	{
		this.latitudeBits = latitudeBits;
	}

	public long getLatitudeScale()
	{
		return latitudeScale;
	}

	public void setLatitudeScale(long latitudeScale)
	{
		this.latitudeScale = latitudeScale;
	}

	public List<String> getLegalese()
	{
		return legaleses;
	}

	public long getLegaleseBits()
	{
		return legaleseBits;
	}

	public void setLegaleseBits(long legaleseBits)
	{
		this.legaleseBits = legaleseBits;
	}

	public long getLegaleseSize()
	{
		return legaleseSize;
	}

	public void setLegaleseSize(long legaleseSize)
	{
		this.legaleseSize = legaleseSize;
	}

	public long getLevelAddBits()
	{
		return levelAddBits;
	}

	public void setLevelAddBits(long levelAddBits)
	{
		this.levelAddBits = levelAddBits;
	}

	public long getLevelAddScale()
	{
		return levelAddScale;
	}

	public void setLevelAddScale(long levelAddScale)
	{
		this.levelAddScale = levelAddScale;
	}

	public long getLevelMultiplyBits()
	{
		return levelMultiplyBits;
	}

	public void setLevelMultiplyBits(long levelMultiplyBits)
	{
		this.levelMultiplyBits = levelMultiplyBits;
	}

	public long getLevelMultiplyScale()
	{
		return levelMultiplyScale;
	}

	public void setLevelMultiplyScale(long levelMultiplyScale)
	{
		this.levelMultiplyScale = levelMultiplyScale;
	}

	public List<String> getLevelUnits()
	{
		return levelUnits;
	}

	public long getLevelUnitBits()
	{
		return levelUnitBits;
	}

	public void setLevelUnitBits(long levelUnitBits)
	{
		this.levelUnitBits = levelUnitBits;
	}

	public long getLevelUnitSize()
	{
		return levelUnitSize;
	}

	public void setLevelUnitSize(long levelUnitSize)
	{
		this.levelUnitSize = levelUnitSize;
	}

	public long getLongitudeBits()
	{
		return longitudeBits;
	}

	public void setLongitudeBits(long longitudeBits)
	{
		this.longitudeBits = longitudeBits;
	}

	public long getLongitudeScale()
	{
		return longitudeScale;
	}

	public void setLongitudeScale(long longitudeScale)
	{
		this.longitudeScale = longitudeScale;
	}

	public long getMaxCountries()
	{
		return maxCountries;
	}

	public void setMaxCountries(long maxCountries)
	{
		this.maxCountries = maxCountries;
	}

	public long getMaxDatumTypes()
	{
		return maxDatumTypes;
	}

	public void setMaxDatumTypes(long maxDatumTypes)
	{
		this.maxDatumTypes = maxDatumTypes;
	}

	public long getMaxLegaleses()
	{
		return maxLegaleses;
	}

	public void setMaxLegaleses(long maxLegaleses)
	{
		this.maxLegaleses = maxLegaleses;
	}

	public long getMaxRestrictionTypes()
	{
		return maxRestrictionTypes;
	}

	public void setMaxRestrictionTypes(long maxRestrictionTypes)
	{
		this.maxRestrictionTypes = maxRestrictionTypes;
	}

	public long getMaxTzfiles()
	{
		return maxTzfiles;
	}

	public void setMaxTzfiles(long maxTzfiles)
	{
		this.maxTzfiles = maxTzfiles;
	}

	public long getMonthsOnStationBits()
	{
		return monthsOnStationBits;
	}

	public void setMonthsOnStationBits(long monthsOnStationBits)
	{
		this.monthsOnStationBits = monthsOnStationBits;
	}

	public long getNodeBits()
	{
		return nodeBits;
	}

	public void setNodeBits(long nodeBits)
	{
		this.nodeBits = nodeBits;
	}

	public List<List<Double>> getNodeFactor()
	{
		return nodeFactor;
	}

	public int getNodeOffset()
	{
		return nodeOffset;
	}

	public void setNodeOffset(int nodeOffset)
	{
		this.nodeOffset = nodeOffset;
	}

	public long getNodeScale()
	{
		return nodeScale;
	}

	public void setNodeScale(long nodeScale)
	{
		this.nodeScale = nodeScale;
	}

	public long getPedigreeBits()
	{
		return pedigreeBits;
	}

	public void setPedigreeBits(long pedigreeBits)
	{
		this.pedigreeBits = pedigreeBits;
	}

	public long getPedigreeSize()
	{
		return pedigreeSize;
	}

	public void setPedigreeSize(long pedigreeSize)
	{
		this.pedigreeSize = pedigreeSize;
	}

	public DbHeaderPublic getPub()
	{
		return pub;
	}

	public void setPub(DbHeaderPublic pub)
	{
		this.pub = pub;
	}

	public long getRecordSizeBits()
	{
		return recordSizeBits;
	}

	public void setRecordSizeBits(long recordSizeBits)
	{
		this.recordSizeBits = recordSizeBits;
	}

	public long getRecordTypeBits()
	{
		return recordTypeBits;
	}

	public void setRecordTypeBits(long recordTypeBits)
	{
		this.recordTypeBits = recordTypeBits;
	}

	public List<String> getRestrictions()
	{
		return restrictions;
	}

	public long getRestrictionBits()
	{
		return restrictionBits;
	}

	public void setRestrictionBits(long restrictionBits)
	{
		this.restrictionBits = restrictionBits;
	}

	public long getRestrictionSize()
	{
		return restrictionSize;
	}

	public void setRestrictionSize(long restrictionSize)
	{
		this.restrictionSize = restrictionSize;
	}

	public List<Double> getSpeed()
	{
		return speeds;
	}

	public long getSpeedBits()
	{
		return speedBits;
	}

	public void setSpeedBits(long speedBits)
	{
		this.speedBits = speedBits;
	}

	public int getSpeedOffset()
	{
		return speedOffset;
	}

	public void setSpeedOffset(int speedOffset)
	{
		this.speedOffset = speedOffset;
	}

	public long getSpeedScale()
	{
		return speedScale;
	}

	public void setSpeedScale(long speedScale)
	{
		this.speedScale = speedScale;
	}

	public long getStationBits()
	{
		return stationBits;
	}

	public void setStationBits(long stationBits)
	{
		this.stationBits = stationBits;
	}

	public long getTimeBits()
	{
		return timeBits;
	}

	public void setTimeBits(long timeBits)
	{
		this.timeBits = timeBits;
	}

	public List<String> getTzfiles()
	{
		return tzfiles;
	}

	public long getTzfileBits()
	{
		return tzfileBits;
	}

	public void setTzfileBits(long tzfileBits)
	{
		this.tzfileBits = tzfileBits;
	}

	public long getTzfileSize()
	{
		return tzfileSize;
	}

	public void setTzfileSize(long tzfileSize)
	{
		this.tzfileSize = tzfileSize;
	}

	private int headerChecksum()
	{
		int[] CRC_TABLE = new int[]
		{ 0x00000000, 0x77073096, 0xEE0E612C, 0x990951BA, 0x076DC419, 0x706AF48F, 0xE963A535, 0x9E6495A3, 0x0EDB8832, 0x79DCB8A4, 0xE0D5E91E, 0x97D2D988,
				0x09B64C2B, 0x7EB17CBD, 0xE7B82D07, 0x90BF1D91, 0x1DB71064, 0x6AB020F2, 0xF3B97148, 0x84BE41DE, 0x1ADAD47D, 0x6DDDE4EB, 0xF4D4B551, 0x83D385C7,
				0x136C9856, 0x646BA8C0, 0xFD62F97A, 0x8A65C9EC, 0x14015C4F, 0x63066CD9, 0xFA0F3D63, 0x8D080DF5, 0x3B6E20C8, 0x4C69105E, 0xD56041E4, 0xA2677172,
				0x3C03E4D1, 0x4B04D447, 0xD20D85FD, 0xA50AB56B, 0x35B5A8FA, 0x42B2986C, 0xDBBBC9D6, 0xACBCF940, 0x32D86CE3, 0x45DF5C75, 0xDCD60DCF, 0xABD13D59,
				0x26D930AC, 0x51DE003A, 0xC8D75180, 0xBFD06116, 0x21B4F4B5, 0x56B3C423, 0xCFBA9599, 0xB8BDA50F, 0x2802B89E, 0x5F058808, 0xC60CD9B2, 0xB10BE924,
				0x2F6F7C87, 0x58684C11, 0xC1611DAB, 0xB6662D3D, 0x76DC4190, 0x01DB7106, 0x98D220BC, 0xEFD5102A, 0x71B18589, 0x06B6B51F, 0x9FBFE4A5, 0xE8B8D433,
				0x7807C9A2, 0x0F00F934, 0x9609A88E, 0xE10E9818, 0x7F6A0DBB, 0x086D3D2D, 0x91646C97, 0xE6635C01, 0x6B6B51F4, 0x1C6C6162, 0x856530D8, 0xF262004E,
				0x6C0695ED, 0x1B01A57B, 0x8208F4C1, 0xF50FC457, 0x65B0D9C6, 0x12B7E950, 0x8BBEB8EA, 0xFCB9887C, 0x62DD1DDF, 0x15DA2D49, 0x8CD37CF3, 0xFBD44C65,
				0x4DB26158, 0x3AB551CE, 0xA3BC0074, 0xD4BB30E2, 0x4ADFA541, 0x3DD895D7, 0xA4D1C46D, 0xD3D6F4FB, 0x4369E96A, 0x346ED9FC, 0xAD678846, 0xDA60B8D0,
				0x44042D73, 0x33031DE5, 0xAA0A4C5F, 0xDD0D7CC9, 0x5005713C, 0x270241AA, 0xBE0B1010, 0xC90C2086, 0x5768B525, 0x206F85B3, 0xB966D409, 0xCE61E49F,
				0x5EDEF90E, 0x29D9C998, 0xB0D09822, 0xC7D7A8B4, 0x59B33D17, 0x2EB40D81, 0xB7BD5C3B, 0xC0BA6CAD, 0xEDB88320, 0x9ABFB3B6, 0x03B6E20C, 0x74B1D29A,
				0xEAD54739, 0x9DD277AF, 0x04DB2615, 0x73DC1683, 0xE3630B12, 0x94643B84, 0x0D6D6A3E, 0x7A6A5AA8, 0xE40ECF0B, 0x9309FF9D, 0x0A00AE27, 0x7D079EB1,
				0xF00F9344, 0x8708A3D2, 0x1E01F268, 0x6906C2FE, 0xF762575D, 0x806567CB, 0x196C3671, 0x6E6B06E7, 0xFED41B76, 0x89D32BE0, 0x10DA7A5A, 0x67DD4ACC,
				0xF9B9DF6F, 0x8EBEEFF9, 0x17B7BE43, 0x60B08ED5, 0xD6D6A3E8, 0xA1D1937E, 0x38D8C2C4, 0x4FDFF252, 0xD1BB67F1, 0xA6BC5767, 0x3FB506DD, 0x48B2364B,
				0xD80D2BDA, 0xAF0A1B4C, 0x36034AF6, 0x41047A60, 0xDF60EFC3, 0xA867DF55, 0x316E8EEF, 0x4669BE79, 0xCB61B38C, 0xBC66831A, 0x256FD2A0, 0x5268E236,
				0xCC0C7795, 0xBB0B4703, 0x220216B9, 0x5505262F, 0xC5BA3BBE, 0xB2BD0B28, 0x2BB45A92, 0x5CB36A04, 0xC2D7FFA7, 0xB5D0CF31, 0x2CD99E8B, 0x5BDEAE1D,
				0x9B64C2B0, 0xEC63F226, 0x756AA39C, 0x026D930A, 0x9C0906A9, 0xEB0E363F, 0x72076785, 0x05005713, 0x95BF4A82, 0xE2B87A14, 0x7BB12BAE, 0x0CB61B38,
				0x92D28E9B, 0xE5D5BE0D, 0x7CDCEFB7, 0x0BDBDF21, 0x86D3D2D4, 0xF1D4E242, 0x68DDB3F8, 0x1FDA836E, 0x81BE16CD, 0xF6B9265B, 0x6FB077E1, 0x18B74777,
				0x88085AE6, 0xFF0F6A70, 0x66063BCA, 0x11010B5C, 0x8F659EFF, 0xF862AE69, 0x616BFFD3, 0x166CCF45, 0xA00AE278, 0xD70DD2EE, 0x4E048354, 0x3903B3C2,
				0xA7672661, 0xD06016F7, 0x4969474D, 0x3E6E77DB, 0xAED16A4A, 0xD9D65ADC, 0x40DF0B66, 0x37D83BF0, 0xA9BCAE53, 0xDEBB9EC5, 0x47B2CF7F, 0x30B5FFE9,
				0xBDBDF21C, 0xCABAC28A, 0x53B39330, 0x24B4A3A6, 0xBAD03605, 0xCDD70693, 0x54DE5729, 0x23D967BF, 0xB3667A2E, 0xC4614AB8, 0x5D681B02, 0x2A6F2B94,
				0xB40BBE37, 0xC30C8EA1, 0x5A05DF1B, 0x2D02EF8D };

		int savePos = buffer.position();
		buffer.position(0);

		// long checksum = 0xFFFFFFFFL;
		// for (int i = 0; i < headerSize; i++) {
		// long value = buffer.getInt();
		// // checksum = crc_table[(checksum ^ buf[i]) & 0xff] ^ (checksum >> 8);
		// checksum = (CRC_TABLE[(int)((checksum ^ value) & 0xFF)] ^ (checksum >>
		// 8)) & 0xFFFFFFFFL;
		// }
		// checksum ^= 0xFFFFFFFF;
		int checksum = ~0;
		for (int i = 0; i < headerSize; i++ )
		{
			int value = buffer.get();
			// checksum = crc_table[(checksum ^ buf[i]) & 0xff] ^ (checksum >> 8);
			checksum = (CRC_TABLE[(checksum ^ value) & 0xFF] ^ (checksum >>> 8)) & 0xFFFFFFFF;
			// System.out.printf("buf[i] = %x, checksum >> 8 = %x, new checksum = %x\n",
			// value, checksum >>> 8, checksum);
		}
		checksum ^= ~0;

		buffer.position(savePos);
		int reversedChecksum = ((checksum & 0xFF) << 24) + ((checksum >>> 8 & 0xFF) << 16) + ((checksum >>> 16 & 0xFF) << 8) + (checksum >>> 24);

		return reversedChecksum;

	}

	private List<Double> readDoubleList(long number, long unitSize, long offset, long scale)
	{
		List<Double> list = new ArrayList<>();
		for (int i = 0; i < number; i++ )
		{
			int temp = buffer.unpack(unitSize);
			// logger.info("Add double " + (temp + offset) / scale);
			list.add((double) (temp + offset) / scale);
		}
		return list;
	}

	private void readDoubleListList(List<List<Double>> list, long listI, long listJ, long unitSize, int offset, long scale)
	{
		for (int i = 0; i < listI; i++ )
		{
			List<Double> innerList = new LinkedList<>();
			innerList = readDoubleList(listJ, unitSize, offset, scale);
			list.add(innerList);
		}
	}

	private void readStringList(List<String> list, long number, long unitSize)
	{
		for (int i = 0; i < number; i++ )
		{
			byte[] charBuff = new byte[(int) unitSize];
			buffer.get(charBuff);
			String goodString = new String(charBuff);
			goodString = goodString.substring(0, goodString.indexOf('\0'));
			list.add(goodString);
		}
	}

	private int readStringListVariable(List<String> list, long maxNumber, long unitSize) throws TideDBException
	{
		long pos = buffer.position();
		for (int i = 0; i < maxNumber; i++ )
		{
			byte[] charBuff = new byte[(int) unitSize];
			buffer.get(charBuff);
			String goodString = new String(charBuff);
			goodString = goodString.substring(0, goodString.indexOf('\0'));
			if (goodString.equals(AHTideTCDStr.getString("TideDBHeader.86"))) //$NON-NLS-1$
			{
				buffer.position((int) (pos + maxNumber * unitSize));
				return i;
			}
			list.add(goodString);
		}
		throw new TideDBException(AHTideTCDStr.getString("TideDBHeader.87")); //$NON-NLS-1$
	}

}
