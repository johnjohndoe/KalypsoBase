package org.kalypso.contribs.eclipse.swt.widgets;

import org.kalypso.contribs.eclipse.swt.widgets.ITextCompositeEventListener.MODIFY_EVENT;

public interface ITextCompositeValidator
{
  boolean check( String string, MODIFY_EVENT e );

  String getToolTip( );
}
