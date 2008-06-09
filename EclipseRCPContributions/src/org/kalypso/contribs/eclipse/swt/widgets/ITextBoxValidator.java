package org.kalypso.contribs.eclipse.swt.widgets;

public interface ITextBoxValidator
{
  public enum EVENT_TYPE
  {
    eFocusLost,
    eModify;
  }

  public boolean check( String string, EVENT_TYPE e );

  public String getToolTip( );

}
