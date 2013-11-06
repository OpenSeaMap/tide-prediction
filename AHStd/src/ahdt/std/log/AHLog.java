package ahdt.std.log;

import java.util.logging.Level;
import java.util.logging.Logger;

public class AHLog extends Logger
{
	public AHLog(String name, String resourceBundleName)
	{
		super(name, resourceBundleName);
		super.setLevel(Level.WARNING);
	}
}
