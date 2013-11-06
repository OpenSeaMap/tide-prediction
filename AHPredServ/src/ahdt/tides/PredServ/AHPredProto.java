package ahdt.tides.PredServ;

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ahdt.tides.base.AHTideJob;
import ahdt.tides.base.AHTimestamp;
import ahdt.tides.base.Interval;

/**
 * handles both sides of the communication between client and server 
 * for the request recognized by AHPredServerThread
 * the protocol accepts the following verbs:
 * 
 * S[tat]=NNN; - internal Station ID (see stations.lst)
 * N[oaaID]=NNN; - NOAA Station ID (usually 6 digits)
 * B[egin]=DATE; - start of prediction interval
 * E[nd]=DATE; - end of prediction interval
 * H[ours]=NN; - timespan in hours to be predicted
 * D[ays]=NN; - timespan in days to be predicted
 * 
 * these verbs are delimited by a single ; char and can appear in any sequence. 
 * esp. is a semicolon necessary after the last verb
 * if a verb is provided multiple times the last appearance will be used
 * 
 * 20130220 AH at the moment exactly one mode and format are implemented:
 * PLAIN mode and TEXT format
 * 
 * @author humbach
 *
 */
public class AHPredProto
{
	static public final int DEFPORTNUMBER = 4444;

	enum eProtoState {
		UNKNOWN, WAITING, OK
	}

	private eProtoState m_eState = eProtoState.WAITING;

	/**
	 * default construction with UNKNOWN state
	 */
	public AHPredProto()
	{
		m_eState = eProtoState.UNKNOWN;
	}

	/**
	 * construction with given state
	 * especially useful to construct protocols in WAITING state, not in UNKNOWN
	 * @param m_eState
	 */
	public AHPredProto(eProtoState eState)
	{
		m_eState = eState;
	}

	public eProtoState getState()
	{
		return m_eState;
	}

	public void setState(eProtoState eState)
	{
		this.m_eState = eState;
	}

	/**
	 * processes the request and returns the data contained in an AHTideJob
	 * @param strInput the string as provided by the http request
	 * @return AHTideJob for thread
	 */
	public AHTideJob processRequest(String strInput)
	{
		AHTideJob tJob = new AHTideJob();
		int nStatID = 62;

		if (m_eState == eProtoState.WAITING)
		{
			int nVal = 1;
			AHTimestamp tStart = AHTimestamp.now();
			AHTimestamp tEnd = AHTimestamp.now().plus(new Interval(3600 * 48));
			Pattern tPat = Pattern.compile("[BDEHNS]=\\d+");
			Matcher tMatch = tPat.matcher(strInput);
			while (tMatch.find())
			{
				String strIn1 = strInput.substring(tMatch.start());
				Scanner tScan = new Scanner(strIn1).useDelimiter("[;=?/ ]");
				System.out.println("Match: " + strIn1);
				while (tScan.hasNext())
				{
					if (tScan.hasNextLong())
					{
						nVal = (int) tScan.nextLong();
						System.out.println("1: " + nVal);
						break;
					}
					else
						System.out.println("n: " + tScan.next());
				}
				tScan.close();				
				System.out.println("2: " + nVal);
				if (strInput.charAt(tMatch.start()) == 'S')
					nStatID = nVal;
				if (strInput.charAt(tMatch.start()) == 'H')
					tEnd = AHTimestamp.now().plus(new Interval(3600 * nVal));
				if (strInput.charAt(tMatch.start()) == 'D')
					tEnd = AHTimestamp.now().plus(new Interval(3600 * 24 * nVal));
			}

			System.out.println(tEnd.printableDate(null));

			tJob.setStatID(nStatID);
			tJob.setStart(tStart);
			tJob.setEnd(tEnd);
			m_eState = eProtoState.OK;
		}
		return tJob;
	}

	/**
	 * creates a request string from the given job
	 * @return - the correct request string to be sent to the server
	 */
	public String sendRequest(AHTideJob tJob)
	{
		String strOut = "";
		return strOut;
	}
}
