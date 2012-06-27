package org.kalypso.ogc.gml.convert.source;

import java.net.URL;

import org.kalypso.contribs.java.net.IUrlResolver;
import org.kalypso.gml.util.Excelsource;
import org.kalypso.gmlschema.GMLSchemaException;
import org.kalypso.ogc.gml.convert.GmlConvertException;
import org.kalypso.ogc.gml.serialize.ExcelFeatureReader;
import org.kalypsodeegree.model.feature.GMLWorkspace;

/**
 * @author Gernot Belger
 */
public class ExcelSourceHandler implements ISourceHandler
{
  private final Excelsource m_type;

  private final URL m_context;

  private final IUrlResolver m_resolver;

  public ExcelSourceHandler( final IUrlResolver resolver, final URL context, final Excelsource type )
  {
    m_resolver = resolver;
    m_context = context;
    m_type = type;
  }

  @Override
  public GMLWorkspace getWorkspace( ) throws GmlConvertException, GMLSchemaException
  {
    final ExcelFeatureReader reader = new ExcelFeatureReader( m_type, m_resolver, m_context );
    reader.read();
    return reader.getWorkspace();
  }
}
