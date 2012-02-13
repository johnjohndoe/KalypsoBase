package org.kalypso.ogc.gml.convert.source;

import org.kalypso.ogc.gml.convert.GmlConvertException;
import org.kalypsodeegree.model.feature.GMLWorkspace;

/**
 * @author belger
 */
public interface ISourceHandler
{
  GMLWorkspace getWorkspace( ) throws GmlConvertException;
}