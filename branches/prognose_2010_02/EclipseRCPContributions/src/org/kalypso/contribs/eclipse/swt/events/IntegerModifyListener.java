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
  private Color m_goodColor;

  /**
   * The bad color.
   */
  private Color m_badColor;

  /**
   * The constructor.
   * 
   * @param goodColor
   *          The good color.
   * @param badColor
   *          The bad color.
   */
  public IntegerModifyListener( Color goodColor, Color badColor )
  {
    m_goodColor = goodColor;
    m_badColor = badColor;
  }

  /**
   * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
   */
  public void modifyText( ModifyEvent e )
  {
    if( e.widget instanceof Text )
    {
      Text text = (Text) e.widget;
      String number = text.getText();

      if( NumberUtils.isInteger( number ) )
        text.setForeground( m_goodColor );
      else
        text.setForeground( m_badColor );
    }
  }
}