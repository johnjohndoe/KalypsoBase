package de.openali.diagram.factory.configuration.parameters.impl;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;

import de.openali.diagram.factory.configuration.parameters.IStringParser;
import de.openali.diagram.framework.logging.Logger;

/**
 * @author alibu
 *
 */
public class FontDataParser implements IStringParser<FontData>
{

  String m_defaultFontName="Times New Roman";

  /**
   * @see de.openali.diagram.factory.configuration.parameters.IStringParser#createValueFromString(java.lang.String)
   */
  public FontData createValueFromString( String value )
  {
    FontData fd=null;
    try
    {
      //if no Exception is thrown when construction fontData,
      fd=new FontData(value, 10, SWT.NORMAL);
    }
    catch (IllegalArgumentException iae)
    {
         Logger.logError(Logger.TOPIC_LOG_CONFIG, "invalid font name; using font "+m_defaultFontName);
         fd=new FontData(m_defaultFontName, 10, SWT.NORMAL);
    }
    return fd;
  }

  /**
   * @see de.openali.diagram.factory.configuration.parameters.IStringParser#getFormatHint()
   */
  public String getFormatHint( )
  {
    String formatHint="font name - for example "+Display.getDefault().getSystemFont().getFontData()[0].getName();
    return formatHint;
  }

}
