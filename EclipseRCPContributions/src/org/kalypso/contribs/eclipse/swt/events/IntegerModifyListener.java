package org.kalypso.contribs.eclipse.swt.events;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Text;
import org.kalypso.contribs.java.lang.NumberUtils;

/**
 * On each modification, checks if widget contains an Integer-Text, if not, setForeground Color *
 * 
 * @author Holger Albert
 */
public class IntegerModifyListener implements ModifyListener
{
  /**
   * The good color.
   */
  private final Color m_goodColor;

  /**
   * The bad color.
   */
  private final Color m_badColor;

  /**
   * The constructor.
   * 
   * @param goodColor
   *          The good color.
   * @param badColor
   *          The bad color.
   */
  public IntegerModifyListener( final Color goodColor, final Color badColor )
  {
    m_goodColor = goodColor;
    m_badColor = badColor;
  }

  /**
   * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
   */
  @Override
  public void modifyText( final ModifyEvent e )
  {
    if( e.widget instanceof Text )
    {
      final Text text = (Text) e.widget;
      final String number = text.getText();

      if( NumberUtils.isInteger( number ) )
        text.setForeground( m_goodColor );
      else
        text.setForeground( m_badColor );
    }
  }
}