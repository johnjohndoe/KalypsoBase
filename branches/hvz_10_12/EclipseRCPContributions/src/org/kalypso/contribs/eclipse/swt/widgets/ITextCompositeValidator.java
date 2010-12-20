package org.kalypso.contribs.eclipse.swt.widgets;

import org.kalypso.contribs.eclipse.swt.widgets.ITextCompositeEventListener.MODIFY_EVENT;

public interface ITextCompositeValidator
{

  public boolean check( String string, MODIFY_EVENT e );

  public String getToolTip( );

}
