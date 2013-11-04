/*    
    Copyright (C) 1997  David Flater.
    Java port Copyright (C) 2011 Chas Douglass

 */

package ahdt.tides.tcd;

import ahdt.tides.base.AHTideException;

/**
 * 
 * @author chas
 */
public class TideDBException extends AHTideException
{

	/**
	 * 
	 */
	protected static final long serialVersionUID = 1L;

	public TideDBException()
	{
		super(AHTideTCDStr.getString("TideDBException.0")); //$NON-NLS-1$
	}

	public TideDBException(String strMsg)
	{
		super(strMsg);
	}
}
