package ahdt.tides.PredServ;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import ahdt.tides.PredServ.AHPredProto.eProtoState;
import ahdt.tides.base.AHTideJob;
import ahdt.tides.base.Constants.Format;
import ahdt.tides.base.Constants.Mode;
import ahdt.tides.base.Station;
import ahdt.tides.tcd.TideDB;

/**
 * 
 * @author humbach
 *
 */
public class AHPredServerThread extends Thread
{
	private Socket m_Socket = null;
	private TideDB m_TideDB = null;

	public AHPredServerThread(Socket tSocket, TideDB tTideDB)
	{
		super("AHPredServerThread");
		m_Socket = tSocket;
		m_TideDB = tTideDB;
	}

	/**
	 * automatically called by the framework, when the thread is started.
	 * each thread answers exactly one request.
	 */
	public void run()
	{
		try
		{
			Mode tMode = Mode.PLAIN;
			Format tForm = Format.TEXT;
			AHTideJob tJob = null;

			PrintWriter tOut = new PrintWriter(m_Socket.getOutputStream(), true);
			BufferedReader tIn = new BufferedReader(new InputStreamReader(m_Socket.getInputStream()));

			String strOutput, strIn;
			strIn = tIn.readLine();
			AHPredProto tAHPP = new AHPredProto(eProtoState.WAITING);
			tJob = tAHPP.processRequest(strIn);
			Station tStat = m_TideDB.createStation(tJob.getStatID());
			strOutput = tStat.print(tJob.getStart(), tJob.getEnd(), tJob.getMode(), tForm);
			System.out.println(strOutput);
			tOut.println(strOutput);

			tOut.close();
			tIn.close();
			m_Socket.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

}
