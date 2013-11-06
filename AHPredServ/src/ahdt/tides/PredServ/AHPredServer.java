package ahdt.tides.PredServ;

import java.io.IOException;
import java.net.ServerSocket;

import ahdt.std.AHAppParaStore;
import ahdt.std.AHAppParam.AHAppParamException;
import ahdt.tides.PredServ.AHPredProto;
import ahdt.tides.PredServ.AHPredServerThread;
import ahdt.tides.PredServ.ExtStr;
import ahdt.tides.tcd.TideDB;
import ahdt.tides.tcd.TideDBException;

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
	private String strTCDFile = ExtStr.getString("AHTides.TCD_Name"); //$NON-NLS-1$
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
			if ((nPNum = (int) m_tPara.findIntPara("p")) == 0)
				nPNum = AHPredProto.DEFPORTNUMBER;
			m_ServerSocket = new ServerSocket(nPNum);
			m_TideDB = new TideDB(strTCDFile);
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