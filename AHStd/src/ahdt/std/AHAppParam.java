package ahdt.std;

public class AHAppParam implements Comparable<AHAppParam>
{
	public class AHAppParamException extends Exception
	{
		/**
		 * 20130201 AH version 0.1
		 */
		private static final long serialVersionUID = 1L;

		public AHAppParamException()
		{
			super("Application parameter type mismatch");
		}

		public AHAppParamException(String strMsg)
		{
			super(strMsg);
		}
	}

	public enum EParaType {
		SWITCH, STRING, NUMBER,
	}

	public enum EParaSrc {
		DEF, CMDLN, REG, INI, XML,
	}

	protected String m_strName = "";
	protected EParaType m_eType = EParaType.STRING;
	protected EParaSrc m_eSrc = EParaSrc.DEF;
	protected String m_strVal = "";
	protected boolean m_boolVal = false;
	protected long m_intVal = 0;
	protected double m_dblVal = 0.0;

	public AHAppParam(String strName, String strDefVal)
	{
		m_strVal = strDefVal;
		m_strName = strName;
		m_eType = EParaType.STRING;
		m_eSrc = EParaSrc.DEF;
	}

	public AHAppParam(String strName, boolean bDefVal)
	{
		m_boolVal = bDefVal;
		m_strName = strName;
		m_eType = EParaType.SWITCH;
		m_eSrc = EParaSrc.DEF;
	}

	public AHAppParam(String strName, long nDefVal)
	{
		m_intVal = nDefVal;
		m_dblVal = nDefVal;
		m_strName = strName;
		m_eType = EParaType.NUMBER;
		m_eSrc = EParaSrc.DEF;
	}

	public AHAppParam(String strName, double nDefVal)
	{
		m_intVal = Math.round(nDefVal);
		m_dblVal = nDefVal;
		m_strName = strName;
		m_eType = EParaType.NUMBER;
		m_eSrc = EParaSrc.DEF;
	}

	public String getStrValue() throws AHAppParamException
	{
		String strVal = null;
		if (m_eType == EParaType.STRING)
			strVal = m_strVal;
		else
			throw new AHAppParamException();
		return strVal;
	}

	public boolean getBoolValue() throws AHAppParamException
	{
		boolean bVal = false;
		if (m_eType == EParaType.SWITCH)
			bVal = m_boolVal;
		else
			throw new AHAppParamException();
		return bVal;
	}

	public long getIntValue() throws AHAppParamException
	{
		long nVal = 0;
		if (m_eType == EParaType.NUMBER)
			nVal = m_intVal;
		else
			throw new AHAppParamException();
		return nVal;
	}

	public double getDblValue() throws AHAppParamException
	{
		double fVal = 0.0;
		if (m_eType == EParaType.NUMBER)
			fVal = m_dblVal;
		else
			throw new AHAppParamException();
		return fVal;
	}

	public void setStrValue(String strVal) throws AHAppParamException
	{
		if (m_eType == EParaType.STRING)
		{
			m_strVal = strVal;
		}
		else
			throw new AHAppParamException();
	}

	public void setIntValue(int nVal) throws AHAppParamException
	{
		if (m_eType == EParaType.NUMBER)
		{
			m_intVal = nVal;
			m_dblVal = nVal;
		}
		else
			throw new AHAppParamException();
	}

	public void setDblValue(double fVal) throws AHAppParamException
	{
		if (m_eType == EParaType.NUMBER)
		{
			m_intVal = Math.round(fVal);
			m_dblVal = fVal;
		}
		else
			throw new AHAppParamException();
	}

	public void setBoolValue(boolean bVal) throws AHAppParamException
	{
		if (m_eType == EParaType.SWITCH)
		{
			m_boolVal = bVal;
		}
		else
			throw new AHAppParamException();
	}

	public EParaType getType()
	{
		return m_eType;
	}

	public void setType(EParaType m_eType)
	{
		this.m_eType = m_eType;
	}

	public EParaSrc getSrc()
	{
		return m_eSrc;
	}

	public void setSrc(EParaSrc m_eSrc)
	{
		this.m_eSrc = m_eSrc;
	}

	/**
	 * if both param match, i.e. the given params name is a left substring of this one and the type is the same
	 * copies the value and source of the given param into this one
	 * @param tPara the parameter to be copied
	 * @return true if both params match and a copy has been made
	 */
	public boolean copyValue(AHAppParam tPara)
	{
		boolean bOk = false;
		try
		{
			// if it is the same parameter by name and type, copy the value and source
			if (matches(tPara) && (m_eType == tPara.getType()))
			{
				m_eSrc = tPara.getSrc();
				switch (m_eType)
				{
				case NUMBER:
					m_dblVal = tPara.getDblValue();
					m_intVal = tPara.getIntValue();
					bOk = true;
					break;
				case SWITCH:
					m_boolVal = tPara.getBoolValue();
					bOk = true;
					break;
				case STRING:
					m_strVal = tPara.getStrValue();
					bOk = true;
					break;
				}
			}
		}
		catch (AHAppParamException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bOk;

	}

	/**
	 * copies the complete data of tPara into this param
	 * 
	 * @param tPara
	 * @return
	 */
	public boolean copyValueAnyway(AHAppParam tPara)
	{
		boolean bOk = false;
		try
		{
			// no check if it is the same parameter, copy the type and value anyway
			// only if the current name extends the 'new' one leave it as it is, else copy it
			if ( !m_strName.startsWith(tPara.m_strName))
				m_strName = tPara.m_strName;
			m_eType = tPara.getType();
			m_eSrc = tPara.getSrc();
			switch (m_eType)
			{
			case NUMBER:
				m_dblVal = tPara.getDblValue();
				bOk = true;
				break;
			case SWITCH:
				m_boolVal = tPara.getBoolValue();
				bOk = true;
				break;
			case STRING:
				m_strVal = tPara.getStrValue();
				bOk = true;
				break;
			}
		}
		catch (AHAppParamException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bOk;

	}

	public boolean equals(AHAppParam tPara)
	{
		boolean bEq = false;
		try
		{
			// if it is the same parameter by name, test the value
			if (matches(tPara) && (m_eType == tPara.getType()))
			{
				switch (m_eType)
				{
				case NUMBER:
					bEq = m_dblVal == tPara.getDblValue();
					break;
				case SWITCH:
					bEq = m_boolVal == tPara.getBoolValue();
					break;
				case STRING:
					bEq = (m_strVal.compareTo(tPara.getStrValue()) == 0);
					break;
				}
			}
		}
		catch (AHAppParamException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bEq;
	}

	@Override
	public int compareTo(AHAppParam tPara)
	{
		int nCmp = m_strName.compareTo(tPara.m_strName);
		return nCmp;
	}

	/**
	 * a match is considered if:
	 * the given parameters name is a leftbound substring of this parameters name
	 * e.g
	 * this name is "list" given is "li" -> match
	 * this name is "help" given is "hi" -> no match
	**/
	public boolean matches(AHAppParam tPara)
	{
		return m_strName.startsWith(tPara.m_strName);
	}

	public String getName()
	{
		return m_strName;
	}
}
