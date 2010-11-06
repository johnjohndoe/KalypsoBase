package com.bce.util;

import java.text.MessageFormat;

/**
 * Utility Klasse zum Erzeugen von Messages
 * 
 * @author belger
 */
public class MessageFormatUtility
{
  public final static String formatMessage( final String formatString, int lineNumber )
  {
    return MessageFormat.format( formatString, new Object[] { new Integer( lineNumber ) } );
  }
}
