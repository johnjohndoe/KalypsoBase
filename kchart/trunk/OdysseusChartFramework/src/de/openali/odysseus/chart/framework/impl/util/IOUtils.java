package de.openali.odysseus.chart.framework.impl.util;

import java.io.IOException;
import java.io.InputStream;

public class IOUtils
{

	public static void closeQuietly(InputStream input)
	{
		try
		{
			if (input != null)
			{
				input.close();
			}
		}
		catch (IOException ioe)
		{
			// do nothing
		}
	}
}
