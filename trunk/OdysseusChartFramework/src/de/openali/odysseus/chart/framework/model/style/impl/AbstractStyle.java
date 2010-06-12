package de.openali.odysseus.chart.framework.model.style.impl;

import java.util.HashMap;
import java.util.Map;

import de.openali.odysseus.chart.framework.model.style.IStyle;

public abstract class AbstractStyle implements IStyle
{
  private int m_alpha;

  private boolean m_isVisible;

  private String m_title;

  private final Map<String, Object> m_data = new HashMap<String, Object>();

  /**
   * @param alpha
   *          0 <= alpha <= 255
   */
  @Override
  public void setAlpha( final int alpha )
  {
    if( alpha < 0 || alpha > 255 )
    {
      m_alpha = 255;
    }
    else
    {
      m_alpha = alpha;
    }
  }

  @Override
  public int getAlpha( )
  {
    return m_alpha;
  }

  @Override
  public void setVisible( final boolean isVisible )
  {
    m_isVisible = isVisible;
  }

  @Override
  public boolean isVisible( )
  {
    return m_isVisible;
  }

  @Override
  public void setTitle( final String title )
  {
    m_title = title;
  }

  @Override
  public String getTitle( )
  {
    return m_title;
  }

  /**
   * @see org.kalypso.chart.framework.model.layer.IChartLayer#setData()
   */
  @Override
  public void setData( final String id, final Object data )
  {
    m_data.put( id, data );
  }

  /**
   * @see org.kalypso.chart.framework.model.layer.IChartLayer#getData()
   */
  @Override
  public Object getData( final String id )
  {
    return m_data.get( id );
  }

}
