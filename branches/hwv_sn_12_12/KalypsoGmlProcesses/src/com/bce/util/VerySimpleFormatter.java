package com.bce.util;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * @author belger
 */
public class VerySimpleFormatter extends Formatter
{
  /**
   * @see java.util.logging.Formatter#format(java.util.logging.LogRecord)
   */
  @Override
  public String format( final LogRecord record )
  {
    return record.getMessage() + "\n"; //$NON-NLS-1$
  }
}
