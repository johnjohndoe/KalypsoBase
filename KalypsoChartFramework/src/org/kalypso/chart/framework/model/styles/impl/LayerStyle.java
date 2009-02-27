package org.kalypso.chart.framework.model.styles.impl;

import java.util.ArrayList;

import org.kalypso.chart.framework.logging.Logger;
import org.kalypso.chart.framework.model.styles.ILayerStyle;
import org.kalypso.chart.framework.model.styles.IStyledElement;
import org.kalypso.chart.framework.model.styles.IStyleConstants.SE_TYPE;

/**
 * @author burtscher
 */
public class LayerStyle implements ILayerStyle
{
  private final ArrayList<IStyledElement> m_elements;

  public LayerStyle( )
  {
    m_elements = new ArrayList<IStyledElement>();
  }

  public void add( final IStyledElement se )
  {
    m_elements.add( se );
  }

  /**
   * @see org.kalypso.chart.framework.styles.ILayerStyle#getElement(org.kalypso.chart.framework.styles.IStyleConstants.SE_TYPE,
   *      int)
   */
  public IStyledElement getElement( final SE_TYPE type, final int pos )
  {
    IStyledElement elt = null;
    int count = 0;
    for( final IStyledElement se : m_elements )
    {
      if( se.getType() == type )
      {
        elt = se;
        if( pos == count )
          break;
        count++;
      }
    }
    if( elt == null )
    {
      Logger.logWarning( Logger.TOPIC_LOG_CONFIG, "No style defined for " + type.toString() + " -> using style dummy" );
      elt = getDefaultElement( type );
    }
    return elt;
  }

  private IStyledElement getDefaultElement( @SuppressWarnings("unused")
  final SE_TYPE type )
  {
    return new StyleDummy();
  }

  /**
   * @see org.kalypso.chart.framework.model.styles.ILayerStyle#getElements()
   */
  @SuppressWarnings("cast")
  public IStyledElement[] getElements( )
  {
    return (IStyledElement[]) m_elements.toArray( new IStyledElement[] {} );
  }

}
