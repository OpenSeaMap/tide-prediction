package ahdt.std;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ahdt.std.AHAppParam.AHAppParamException;
import ahdt.std.AHAppParam.EParaType;
import ahdt.std.log.AHLog;

public class AHAppParaStore
{
	// an app parameter consists of
	// - einem Optionszeichen (beliebige Sonderzeichen wie z.B. '-', '+' oder '/')
	// - einem Optionsnamen (ein weiteres Zeichen bzw. ein String)
	// - einem optionalen Leerzeichen (bei nicht eindeutigen String-Optionen
	// erforderlich,
	// bei eindeutigen (Zeichen-)Optionen optional und abstellbar)
	// - einem Wert (außer bei Switches)
	//
	// Format der Kommandozeile: MeinTool [<Option>|<Dateiname>]
	// [<Option>|<Dateiname>] ...
	//
	// - Das Leerzeichen zwischen Option und Wert ist nur obligatorisch, wenn nicht eindeutige Optionen verwendet werden. Nicht eindeutig sind die Optionen, wenn
	// eine Option mit dem Namen einer kürzeren Option beginnt.
	// - Wenn die Optionen eindeutig sind, kann das Leerzeichen abgestellt werden.
	// - switches können als Vorgabe bereits 'On' oder 'Off' sein.
	// - Wert-Optionen für Strings und Zahlen können optionale Werte haben. Wenn kein Wert angegeben wird, behält die Option den Wert aus der Vorgabe, die Quelle
	// wird aber auf 'Scanned' bzw. 'Loaded' geändert, so daß die REQUIRED-Bedingungen ohne Änderung des Wertes erfüllt sind.
	// - Format der Wert-Optionen bei eindeutigen Optionsnamen: <Option>[ ][<Wert>]
	// - Format der Wert-Optionen bei nicht eindeutigen Optionsnamen: <Option> [<Wert>]

	private AHLog log = new AHLog(this.getClass().getName(), null);

	private char m_cArgDelim = '-';
	// AHAppParam capsules all apps parameters
	private List<AHAppParam> m_ParaList = new ArrayList<AHAppParam>();
	// Besides the 'real' parameters an app can have several filenames without a parameter class, e.g. input files into a compiler/linker
	private List<String> m_FilenameList = new ArrayList<String>();

	/**
	 * the default constructor supports the following standard options: - h[elp] - prints a program banner / help info to stdout default is - as an option
	 * delimiter
	 **/
	public AHAppParaStore()
	{
		m_cArgDelim = '-';
	}

	public AHAppParaStore(String[] str)
	{
		parseCmdl(str);
	}

	/**
	 * parses a command line and stores the found arguments as params
	 * @throws AHAppParamException 
	 **/
	public void parseCmdl(String[] strCmdLn)
	{
		boolean bOpt = false;
		for (int nArg = 0; nArg < strCmdLn.length; ++nArg)
		{
			AHAppParam tParam = null;
			String strCmd = strCmdLn[nArg];
			String strVal = "";
			if ((strCmd.charAt(0) == '-') || (strCmd.charAt(0) == '/') || (strCmd.charAt(0) == m_cArgDelim))
			{
				int nPos = strCmd.indexOf("=");
				if (nPos > 0)
				{
					tParam = new AHAppParam(strCmd.substring(1, nPos), "");
					Pattern tPat = Pattern.compile("\\d+");
					strVal = strCmd.substring(nPos + 1);
					Matcher tMatch = tPat.matcher(strVal);
					if (tMatch.find())
					{
						Scanner tScan = new Scanner(strVal);
						tParam.m_eType = EParaType.NUMBER;
						try
						{
							tParam.setIntValue((int) tScan.nextLong());
						}
						catch (AHAppParamException e)
						{
							e.printStackTrace();
						}
						tScan.close();
					}
					else
					{
						tParam.m_eType = EParaType.STRING;
					}
				}
				else
				{
					tParam = new AHAppParam(strCmd.substring(1), "");
					tParam.m_eType = EParaType.SWITCH;
					tParam.m_boolVal = true;
				}
				// parameter name
				AHAppParam tParamEx;
				boolean bFound = false;
				// check if parameter already exists
				for (Iterator<AHAppParam> tIt = m_ParaList.iterator(); tIt.hasNext();)
				{
					tParamEx = tIt.next();
					if (tParamEx.matches(tParam))
					{
						tParamEx.copyValue(tParam);
						bFound = true;
						break;
					}
				}
				if ( !bFound)
					m_ParaList.add(tParam);
				bOpt = true;
			}
			else if (bOpt)
			{
				// parameter value, only one value allowed, belongs to the last added parameter
				try
				{
					m_ParaList.get(m_ParaList.size() - 1).setStrValue(strCmd);
				}
				catch (AHAppParamException e)
				{
					log.log(Level.WARNING, e.getMessage());
					e.printStackTrace();
				}
				bOpt = false;
			}
			else
			{
				// must be file or pathname
				// add check if valid file/pathname (jawa.nio ???)
				m_FilenameList.add(strCmd);
			}
		}
	}

	public void setStrPara()
	{

	}

	public void setIntPara()
	{

	}

	public void setBoolPara(String strParaName, boolean bDef)
	{
		AHAppParam tNewPara = new AHAppParam(strParaName, bDef);
		m_ParaList.add(tNewPara);
	}

	public void setIntPara(String strParaName, int nDef)
	{
		AHAppParam tNewPara = new AHAppParam(strParaName, nDef);
		m_ParaList.add(tNewPara);
	}

	public void setDblPara(String strParaName, double nDef)
	{
		AHAppParam tNewPara = new AHAppParam(strParaName, nDef);
		m_ParaList.add(tNewPara);
	}

	protected boolean findPara(AHAppParam tNewPara)
	{
		boolean bFound = false;
		return bFound;
	}

	public String findStrPara(String strParaName) throws AHAppParamException
	{
		String strVal = "";
		AHAppParam tParam;
		for (Iterator<AHAppParam> tIt = m_ParaList.iterator(); tIt.hasNext();)
		{
			tParam = tIt.next();
			if (tParam.getName().startsWith(strParaName))
			{
				strVal = tParam.getStrValue();
				break;
			}
		}
		return strVal;
	}

	public boolean findBoolPara(String strParaName) throws AHAppParamException
	{
		boolean bVal = false;
		AHAppParam tParam;
		for (Iterator<AHAppParam> tIt = m_ParaList.iterator(); tIt.hasNext();)
		{
			tParam = tIt.next();
			if (tParam.getName().startsWith(strParaName))
			{
				bVal = tParam.getBoolValue();
				break;
			}
		}
		return bVal;
	}

	public long findIntPara(String strParaName) throws AHAppParamException
	{
		long nVal = 0;
		AHAppParam tParam;
		for (Iterator<AHAppParam> tIt = m_ParaList.iterator(); tIt.hasNext();)
		{
			tParam = tIt.next();
			if (tParam.getName().startsWith(strParaName))
			{
				nVal = tParam.getIntValue();
				break;
			}
		}
		return nVal;
	}
}
