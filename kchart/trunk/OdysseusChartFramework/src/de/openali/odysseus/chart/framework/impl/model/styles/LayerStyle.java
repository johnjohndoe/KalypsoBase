package de.openali.odysseus.chart.framework.impl.model.styles;

import java.util.ArrayList;


import de.openali.odysseus.chart.framework.impl.logging.Logger;
import de.openali.odysseus.chart.framework.model.styles.ILayerStyle;
import de.openali.odysseus.chart.framework.model.styles.IStyledElement;
import de.openali.odysseus.chart.framework.model.styles.IStyleConstants.SE_TYPE;

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

  public void add( IStyledElement se )
  {
    m_elements.add( se );
  }

  /**
   * @see de.openali.odysseus.chart.framework.styles.ILayerStyle#getElement(de.openali.odysseus.chart.framework.styles.IStyleConstants.SE_TYPE,
   *      int)
   */
  public IStyledElement getElement( SE_TYPE type, int pos )
  {
    IStyledElement elt = null;
    int count = 0;
    for( final IStyledElement se : m_elements )
    {
      if( se.getType() == type )
      {
        elt = se;
        count++;
        if( pos == count )
          break;
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
  SE_TYPE type )
  {
    return new StyleDummy();
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.styles.ILayerStyle#getElements()
   */
  @SuppressWarnings("cast")
  public IStyledElement[] getElements( )
  {
    return (IStyledElement[]) m_elements.toArray( new IStyledElement[] {} );
  }

}
