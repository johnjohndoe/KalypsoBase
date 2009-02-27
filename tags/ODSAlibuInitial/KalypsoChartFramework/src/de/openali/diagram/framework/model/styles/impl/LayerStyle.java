package de.openali.diagram.framework.model.styles.impl;

import java.util.ArrayList;
import de.openali.diagram.framework.logging.Logger;
import de.openali.diagram.framework.model.styles.ILayerStyle;
import de.openali.diagram.framework.model.styles.IStyleConstants;
import de.openali.diagram.framework.model.styles.IStyledElement;
import de.openali.diagram.framework.model.styles.IStyleConstants.SE_TYPE;

/**
 * @author burtscher
 */
public class LayerStyle implements ILayerStyle
{
  private ArrayList<IStyledElement> m_elements;

  public LayerStyle( )
  {
    m_elements = new ArrayList<IStyledElement>();
  }

  public void add( IStyledElement se )
  {
    m_elements.add( se );
  }

  /**
   * @see de.openali.diagram.framework.styles.ILayerStyle#getElement(de.openali.diagram.framework.styles.IStyleConstants.SE_TYPE, int)
   */
  public IStyledElement getElement( SE_TYPE type, int pos )
  {
    IStyledElement elt = null;
    int count = 0;
    for( IStyledElement se : m_elements )
    {
      if( se.getType() == type )
      {
        elt = se;
        count++;
        if( pos == count )
          break;
      }
    }
    if (elt==null)
    {
      Logger.logWarning(Logger.TOPIC_LOG_CONFIG, "No style defined for "+type.toString()+" -> using style dummy");
      elt=getDefaultElement(type);
    }
    return elt;
  }
  
  private IStyledElement getDefaultElement(SE_TYPE type)
  {
	return new StyleDummy();
  }

}
