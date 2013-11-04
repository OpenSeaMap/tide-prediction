/**
 * 
 */
package ahdt.tides.base;

/**
 * @author humbach 2013
 * @copyright AHDT 2013
 *
 */
public class AHTideException extends Exception
{

	/**
	 * 
	 */
	protected static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public AHTideException()
	{
		super(AHTideBaseStr.getString("AHTideException.0")); //$NON-NLS-1$
	}

	/**
	 * @param strMsg
	 */
	public AHTideException(String strMsg)
	{
		super(strMsg);
	}

	/**
	 * @param tCause
	 */
	public AHTideException(Throwable tCause)
	{
		super(tCause);
	}

	/**
	 * @param strMsg
	 * @param tCause
	 */
	public AHTideException(String strMsg, Throwable tCause)
	{
		super(strMsg, tCause);
	}

}
