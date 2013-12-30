package ahdt.tides.PredServ;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;

import com.sun.xml.internal.bind.v2.runtime.reflect.ListIterator;

import ahdt.std.AHAppParaStore;
import ahdt.std.AHAppParam.AHAppParamException;
import ahdt.tides.PredServ.AHPredProto;
import ahdt.tides.PredServ.AHPredServerThread;
import ahdt.tides.PredServ.ExtStr;
import ahdt.tides.tcd.TideDB;
import ahdt.tides.tcd.TideDBException;
import ahdt.tides.tcd.TideRecord;

/**
 * simple prediction server
 * it listens on port AHPredProto.PORTNUMBER and starts a new thread for every request there
 * the tide db is loaded by the server and forwarded to the threads
 * 
 * @author humbach
 *
 */
public class AHPredServer
{
	AHAppParaStore m_tPara = new ahdt.std.AHAppParaStore();
	private String strTCDFile = ExtStr.getString("AHTides.TCD_Name");
	private TideDB m_TideDB;

	public AHPredServer(String[] strCmd)
	{
		int nPNum = 0;
		ServerSocket m_ServerSocket = null;
		boolean m_bListening = true;

		try
		{
			initAppParams();
			m_tPara.parseCmdl(strCmd);
			if (m_tPara.findBoolPara("h"))
				printHelp();
			// load TCD to be able to list all stations
			m_TideDB = new TideDB(strTCDFile);
			if (m_tPara.findBoolPara("l"))
				printStationListVerbose();
			// if a port is given (or with the default port) listen on this port for requests
			if ((nPNum = (int) m_tPara.findIntPara("p")) == 0)
				nPNum = AHPredProto.DEFPORTNUMBER;
			m_ServerSocket = new ServerSocket(nPNum);
			// start a new Thread on request. At the moment there is no way to stop the prediction server besides a kill
			while (m_bListening)
				new AHPredServerThread(m_ServerSocket.accept(), m_TideDB).start();
			m_ServerSocket.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			System.err.println("Could not listen on port: " + nPNum + ".");
			System.exit( -1);
		}
		catch (TideDBException e)
		{
			e.printStackTrace();
			System.err.println("Could not initialize tide db. TCD file='" + strTCDFile + "'.");
			System.exit( -2);
		}
		catch (AHAppParamException e)
		{
			e.printStackTrace();
			System.err.println("invalid argument.");
			System.exit( -3);
		}
	}

	protected void initAppParams()
	{
		m_tPara.setBoolPara("list", false);
		m_tPara.setBoolPara("help", false);
		m_tPara.setIntPara("port", AHPredProto.DEFPORTNUMBER);
	}

	/**
	 *  print help message
	 */
	private void printHelp()
	{
		System.out.println("command line options:" + ExtStr.getString("AHTides.StrNewline") + "-h[elp]: prints this help to stdout//file"
				+ ExtStr.getString("AHTides.StrNewline") + "-l[ist]: lists all stations as text file." + ExtStr.getString("AHTides.StrNewline")
				+ "-p[ort]=NNN: port number to listen at." + ExtStr.getString("AHTides.StrNewline"));
	}

	// copy station list to file
	private void printStationList()
	{
		// open outputfile
		FileOutputStream output;
		try
		{
			output = new FileOutputStream(ExtStr.getString("AHTides.StationList")); //$NON-NLS-1$

			// iterate over all stations
			for (java.util.ListIterator<TideRecord> lIt = m_TideDB.getRecords().listIterator(); lIt.hasNext();)
			{
				TideRecord tRec = lIt.next();
				String strStat = tRec.getID() + ExtStr.getString("AHTides.ListSep") + tRec.getRecordType() + ExtStr.getString("AHTides.ListSep") + tRec.getName()
						+ ExtStr.getString("AHTides.ListSep") + ExtStr.getString("AHTides.StrLat") + tRec.getLat()
						+ ExtStr.getString("AHTides.StrLon") + tRec.getLon() + ExtStr.getString("AHTides.StrNewline");
				output.write(strStat.getBytes());
			}
			output.close();
		}
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// copy station list to file
	private void printStationListVerbose()
	{
		// open outputfile
		FileOutputStream output;
		try
		{
			output = new FileOutputStream(ExtStr.getString("AHTides.StationListV"));

			// iterate over all stations
			for (java.util.ListIterator<TideRecord> lIt = m_TideDB.getRecords().listIterator(); lIt.hasNext();)
			{
				String strStat;
				TideRecord tRec = lIt.next();
        if (tRec.getRecordType() == TideRecord.EStatType.SUBORDINATE_STATION)
        {
        	TideRecord tRefRec = m_TideDB.getRecord(tRec.getReferenceStation());
  				strStat = tRec.getID() + ExtStr.getString("AHTides.ListSep") 
  				+ tRec.getStationId() + ExtStr.getString("AHTides.ListSep") 
  				+ tRec.getRecordType() + ExtStr.getString("AHTides.ListSep") 
					+ tRec.getName() + ExtStr.getString("AHTides.ListSep")
					+ ExtStr.getString("AHTides.StrLat") + tRec.getLat() + ExtStr.getString("AHTides.ListSep") 
					+ ExtStr.getString("AHTides.StrLon") + tRec.getLon() + ExtStr.getString("AHTides.ListSep") 
					+ m_TideDB.getCountryName(tRec.getCountry()) + ExtStr.getString("AHTides.ListSep") 
					+ tRec.getComments() + ExtStr.getString("AHTides.ListSep") 
					+ "references: " + tRefRec.getID() + ExtStr.getString("AHTides.ListSep") 
					+ tRefRec.getName() + ExtStr.getString("AHTides.ListSep")
					+ ExtStr.getString("AHTides.StrNewline");
        }
        else 
        	strStat = tRec.getID() + ExtStr.getString("AHTides.ListSep") 
        	+ tRec.getStationId() + ExtStr.getString("AHTides.ListSep") 
        	+ tRec.getRecordType() + ExtStr.getString("AHTides.ListSep") 
					+ tRec.getName() + ExtStr.getString("AHTides.ListSep") 
					+ ExtStr.getString("AHTides.StrLat") + tRec.getLat() + ExtStr.getString("AHTides.ListSep")
					+ ExtStr.getString("AHTides.StrLon") + tRec.getLon() + ExtStr.getString("AHTides.ListSep") 
					+ m_TideDB.getCountryName(tRec.getCountry()) + ExtStr.getString("AHTides.ListSep") 
					+ tRec.getComments() + ExtStr.getString("AHTides.ListSep") 
					+ ExtStr.getString("AHTides.StrNewline");
				output.write(strStat.getBytes());
			}
			output.close();
		}
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	/**
	 * @param args
	 */
	public static void main(String[] strCmd)
	{
		try
		{
			// UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			new AHPredServer(strCmd);
		}
		catch (Throwable t)
		{
			// GUIExceptionHandler.processException(t);
		}
	}

}