package org.kalypso.ogc.gml.convert.source;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;

import javax.xml.namespace.QName;

import org.kalypso.gml.util.Gmlnew;
import org.kalypso.ogc.gml.convert.GmlConvertException;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree_impl.model.feature.FeatureFactory;

/**
 * @author belger
 */
public class GmlNewHandler implements ISourceHandler
{
  private final URL m_context;

  private final QName m_featureQName;

  public GmlNewHandler( final URL context, final Gmlnew type )
  {
    m_context = context;
    m_featureQName = type.getFeatureQName();
  }

  /**
   * @throws GmlConvertException
   * @see org.kalypso.ogc.gml.convert.source.ISourceHandler#getWorkspace()
   */
  public GMLWorkspace getWorkspace() throws GmlConvertException
  {
    try
    {
      return FeatureFactory.createGMLWorkspace( m_featureQName, m_context, null );
    }
    catch( final InvocationTargetException e )
    {
      final String msg = String.format( "Unable to create GML with root feture '%s'", m_featureQName );//$NON-NLS-1$
      throw new GmlConvertException( msg, e.getTargetException() );
    }
  }

}
