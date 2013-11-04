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

/**
 * 
 * @author chas
 */
public class DbHeaderPublic
{

	public static final int ONELINER_LENGTH = 90;
	public static final int MONOLOGUE_LENGTH = 10000;

	private String version;
	private int majorRev;
	private int minorRev;
	private String lastModified;
	private int numberOfRecords;
	private int startYear;
	private int numberOfYears;
	private int constituents;
	private int levelUnitTypes;
	private int dirUnitTypes;
	private int restrictionTypes;
	private int datumTypes;
	private int countries;
	private int tzfiles;
	private int legaleses;

	/* Need this to read V1 files. */
	private int pedigreeTypes;

	public DbHeaderPublic()
	{

	}

	public long getConstituents()
	{
		return constituents;
	}

	public void setConstituents(int constituents)
	{
		this.constituents = constituents;
	}

	public long getCountries()
	{
		return countries;
	}

	public void setCountries(int countries)
	{
		this.countries = countries;
	}

	public int getDatumTypes()
	{
		return datumTypes;
	}

	public void setDatumTypes(int datumTypes)
	{
		this.datumTypes = datumTypes;
	}

	public int getDirUnitTypes()
	{
		return dirUnitTypes;
	}

	public void setDirUnitTypes(int dirUnitTypes)
	{
		this.dirUnitTypes = dirUnitTypes;
	}

	public String getLastModified()
	{
		return lastModified;
	}

	public void setLastModified(String lastModified)
	{
		this.lastModified = lastModified;
	}

	public int getLegaleses()
	{
		return legaleses;
	}

	public void setLegaleses(int legaleses)
	{
		this.legaleses = legaleses;
	}

	public int getLevelUnitTypes()
	{
		return levelUnitTypes;
	}

	public void setLevelUnitTypes(int levelUnitTypes)
	{
		this.levelUnitTypes = levelUnitTypes;
	}

	public int getMajorRev()
	{
		return majorRev;
	}

	public void setMajorRev(int majorRev)
	{
		this.majorRev = majorRev;
	}

	public int getMinorRev()
	{
		return minorRev;
	}

	public void setMinorRev(int minorRev)
	{
		this.minorRev = minorRev;
	}

	public int getNumberOfRecords()
	{
		return numberOfRecords;
	}

	public void setNumberOfRecords(int numberOfRecords)
	{
		this.numberOfRecords = numberOfRecords;
	}

	public int getNumberOfYears()
	{
		return numberOfYears;
	}

	public void setNumberOfYears(int numberOfYears)
	{
		this.numberOfYears = numberOfYears;
	}

	public int getPedigreeTypes()
	{
		return pedigreeTypes;
	}

	public void setPedigreeTypes(int pedigreeTypes)
	{
		this.pedigreeTypes = pedigreeTypes;
	}

	public int getRestrictionTypes()
	{
		return restrictionTypes;
	}

	public void setRestrictionTypes(int restrictionTypes)
	{
		this.restrictionTypes = restrictionTypes;
	}

	public int getStartYear()
	{
		return startYear;
	}

	public void setStartYear(int startYear)
	{
		this.startYear = startYear;
	}

	public int getTzfiles()
	{
		return tzfiles;
	}

	public void setTzfiles(int tzfiles)
	{
		this.tzfiles = tzfiles;
	}

	public String getVersion()
	{
		return version;
	}

	public void setVersion(String version)
	{
		this.version = version;
	}

}
