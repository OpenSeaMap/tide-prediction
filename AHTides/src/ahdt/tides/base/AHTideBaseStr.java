package ahdt.tides.base;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class AHTideBaseStr
{
	private static final String BUNDLE_NAME = "ahdt.tides.base.AHTideBaseStr"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

	private AHTideBaseStr()
	{
	}

	public static String getString(String key)
	{
		try
		{
			return RESOURCE_BUNDLE.getString(key);
		}
		catch (MissingResourceException e)
		{
			return '!' + key + '!';
		}
	}
}
