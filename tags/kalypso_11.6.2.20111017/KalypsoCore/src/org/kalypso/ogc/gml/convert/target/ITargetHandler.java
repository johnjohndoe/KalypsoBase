package org.kalypso.ogc.gml.convert.target;

import org.kalypso.ogc.gml.convert.GmlConvertException;
import org.kalypsodeegree.model.feature.GMLWorkspace;

/**
 * @author belger
 */
public interface ITargetHandler
{
  void saveWorkspace( final GMLWorkspace workspace ) throws GmlConvertException;
}
