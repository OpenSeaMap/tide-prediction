package ahdt.tides.base;

import ahdt.tides.base.Constants.Mode;

public class AHTideJob
{
	private int m_nStatID;
	private int m_nNOAAID;
	private AHTimestamp m_tStart;
	private AHTimestamp m_nEnd;
	private String m_strStatName;
	private Mode m_Mode;

	public AHTideJob()
	{
		// TODO Auto-generated constructor stub
	}

	/**
	 * @return the m_nStatID
	 */
	public int getStatID()
	{
		return m_nStatID;
	}

	/**
	 * @param m_nStatID the m_nStatID to set
	 */
	public void setStatID(int m_nStatID)
	{
		this.m_nStatID = m_nStatID;
	}

	/**
	 * @return the m_nNOAAID
	 */
	public int getNOAAID()
	{
		return m_nNOAAID;
	}

	/**
	 * @param m_nNOAAID the m_nNOAAID to set
	 */
	public void setNOAAID(int m_nNOAAID)
	{
		this.m_nNOAAID = m_nNOAAID;
	}

	/**
	 * @return the m_tStart
	 */
	public AHTimestamp getStart()
	{
		return m_tStart;
	}

	/**
	 * @param m_tStart the m_tStart to set
	 */
	public void setStart(AHTimestamp m_tStart)
	{
		this.m_tStart = m_tStart;
	}

	/**
	 * @return the m_nEnd
	 */
	public AHTimestamp getEnd()
	{
		return m_nEnd;
	}

	/**
	 * @param m_nEnd the m_nEnd to set
	 */
	public void setEnd(AHTimestamp m_nEnd)
	{
		this.m_nEnd = m_nEnd;
	}

	/**
	 * @return the m_strStatName
	 */
	public String getStatName()
	{
		return m_strStatName;
	}

	/**
	 * @param m_strStatName the m_strStatName to set
	 */
	public void setStatName(String m_strStatName)
	{
		this.m_strStatName = m_strStatName;
	}

	/**
	 * @return the Mode
	 */
	public Mode getMode()
	{
		return m_Mode;
	}

	/**
	 * @param m_Mode the Mode to set
	 */
	public void setMode(Mode m_Mode)
	{
		this.m_Mode = m_Mode;
	}

}
