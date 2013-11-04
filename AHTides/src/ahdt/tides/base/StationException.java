/**
 * 
 */
package ahdt.tides.base;

/**
 * @author humbach
 *
 */
public class StationException extends Exception
{
	/**
	 * 
	 */
	protected static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public StationException()
	{
		super(AHTideBaseStr.getString("StationException.0")); //$NON-NLS-1$
	}

	/**
	 * @param strMsg
	 */
	public StationException(String strMsg)
	{
		super(strMsg);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param tCause
	 */
	public StationException(Throwable tCause)
	{
		super(tCause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param strMsg
	 * @param tCause
	 */
	public StationException(String strMsg, Throwable tCause)
	{
		super(strMsg, tCause);
		// TODO Auto-generated constructor stub
	}

}
