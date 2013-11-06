package ahdt.std;

import java.util.Calendar;
import java.util.Date;

public class AHJulian
{
	/**
	 *  Sonderzustände des Zeitpunktes
	 *  
	 *  NAT - Kein Zeitpunkt
	 *  RT- regular Time: gewöhnlicher Zeitpunkt
	 *  INFP - unendlich spät
	 *  INFN - Urknall
	 *  
	 * @author humbach
	 *
	 */
	protected enum eTimeStat {
		NAT, RT, INFP, INFN
	};

	private eTimeStat m_eValid;
	private double fJDate = 0.0;

	public AHJulian()
	{
		m_eValid = eTimeStat.NAT;
	}

	public AHJulian(Date tDate)
	{

	}

	public boolean isValid()
	{
		// assert (NAT == m_eValid || RT == m_eValid || INFN == m_eValid || INFP == m_eValid);
		return eTimeStat.NAT != m_eValid;
	}

	public double getJulDat()
	{
		return fJDate;
	}

	public Date getDate()
	{
		Date tDate = null;
		return tDate;
	}

	static private Calendar JulianToCalendar(double dJD, boolean bDate, boolean bTime)
	{
		Calendar tDate = Calendar.getInstance();

		double dA, dB, dF;
		int nAlpha, nC, nE, nD, nZ;
		int nDay, nMonth, nYear;
		int nHour, nMinute, nSecond;

		nZ = (int) Math.floor(dJD + 0.5);
		dF = (dJD + 0.5) - nZ;

		if (bDate)
		{
			if (nZ < 2299161)
			{
				dA = nZ;
			}
			else
			{
				nAlpha = (int) ((nZ - 1867216.25) / 36524.25);
				dA = nZ + 1 + nAlpha - nAlpha / 4;
			}
			dB = dA + 1524;
			nC = (int) ((dB - 122.1) / 365.25);
			nD = (int) (365.25 * nC);
			nE = (int) ((dB - nD) / 30.6001);

			nDay = (int) (dB - nD - Math.floor(30.6001 * nE) + dF);
			if (nE <= 14)
				nMonth = (int) (nE - 1);
			else
				nMonth = (int) (nE - 13);
			if (nMonth > 2)
				nYear = (int) (nC - 4716);
			else
				nYear = (int) (nC - 4715);
			tDate.set(nYear, nMonth, nDay);
		}
		else
		{
			nYear = 0;
			nMonth = 0;
			nDay = 0;
		}

		if (bTime)
		{
			// Zuerst dF in Anzahl der Millisekunden als double umrechnen. Diesen Wert
			// runden und dann in int umwandeln. Nun mit Div/Modulo-Operationen
			// die Anteile für Stunden, Min., Sek. und ms berechnen.
			int nFAnzMS = (int) (dF * 24.0 * 60.0 * 60.0 * 1000.0 + 0.5);
			// Stunde hat 3600000 ms
			nHour = (int) (nFAnzMS / 3600000);
			nFAnzMS %= 3600000;
			// eine Min. hat 60000 ms
			nMinute = (int) (nFAnzMS / 60000);
			nFAnzMS %= 60000;
			// eine Sek. hat 1000 ms
			nSecond = (int) (nFAnzMS / 1000);
			nFAnzMS %= 1000;
			// ms
			// nMilliseconds = (int)nFAnzMS;
			tDate.set(nYear, nMonth, nDay, nHour, nMinute, nSecond);
		}
		return tDate;
	}

	private double GetJulDay(long nYear, long nMonth, long nDay)
	{
		long A, B, m, y;
		double fHelp1, fHelp2, dJD;

		if (nMonth > 2)
		{
			y = nYear;
			m = nMonth;
		}
		else
		{
			y = nYear - 1;
			m = nMonth + 12;
		}
		A = y / 100;
		if (nYear > 1582 || (nYear == 1582 && (nMonth > 10 || (nMonth == 10 && nDay > 4))))
			B = 2 - A + A / 4;
		else
			B = 0;
		dJD = (int) (365.25 * (y + 4716)) + (int) (30.6001 * (m + 1)) + nDay + B - 1524.5;
		return dJD;
	}

	private double GetJulTime(long nHour, long nMinute, long nSecond, double dSecFrac)
	{
		double fFrac;
		// Tageszeit berechnen in Sekunden/Tag und dann durch die Sekunden des vollen Tages teilen
		fFrac = (nHour * 3600.0 + nMinute * 60.0 + nSecond + dSecFrac) / 86400.0;
		return fFrac;
	}

}
