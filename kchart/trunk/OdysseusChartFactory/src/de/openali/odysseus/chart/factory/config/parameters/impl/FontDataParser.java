package de.openali.odysseus.chart.factory.config.parameters.impl;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;

import de.openali.odysseus.chart.framework.impl.logging.Logger;
import de.openali.odysseus.chart.framework.model.data.IStringParser;

/**
 * @author alibu
 */
public class FontDataParser implements IStringParser<FontData>
{

  String m_defaultFontName = "Times New Roman";

  /**
   * @see org.kalypso.chart.framework.model.data.IStringParser#stringToLogical(java.lang.String)
   */
  public FontData stringToLogical( String value )
  {
    FontData fd = null;
    try
    {
      // if no Exception is thrown when construction fontData,
      fd = new FontData( value, 10, SWT.NORMAL );
    }
    catch( final IllegalArgumentException iae )
    {
      Logger.logError( Logger.TOPIC_LOG_CONFIG, "invalid font name; using font " + m_defaultFontName );
      fd = new FontData( m_defaultFontName, 10, SWT.NORMAL );
    }
    return fd;
  }

  /**
   * @see org.kalypso.chart.framework.model.data.IStringParser#getFormatHint()
   */
  public String getFormatHint( )
  {
    final String formatHint = "font name - for example " + Display.getDefault().getSystemFont().getFontData()[0].getName();
    return formatHint;
  }

}
