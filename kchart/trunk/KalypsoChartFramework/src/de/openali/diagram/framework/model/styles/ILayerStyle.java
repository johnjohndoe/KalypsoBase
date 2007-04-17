package de.openali.diagram.framework.model.styles;

import de.openali.diagram.framework.model.styles.IStyleConstants.SE_TYPE;

/**
 * @author burtscher
 * 
 * Container for IStyledElements
 * 
 */
public interface ILayerStyle
{
  /**
   * @return IStyledElement with given type on given position pos; if there's no element
   * at the designated position, the last element by the favoured type is returned. If there's
   * no element by the designated type is available, null is returned;
   * 
   */
  public IStyledElement getElement( SE_TYPE type, int pos );
}
