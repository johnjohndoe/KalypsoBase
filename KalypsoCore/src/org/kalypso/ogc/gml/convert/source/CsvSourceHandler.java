package org.kalypso.ogc.gml.convert.source;

import java.net.URL;

import org.kalypso.contribs.java.net.IUrlResolver;
import org.kalypso.gml.util.CsvSourceType;
import org.kalypso.gmlschema.GMLSchemaException;
import org.kalypso.ogc.gml.convert.GmlConvertException;
import org.kalypso.ogc.gml.serialize.CsvFeatureReader;
import org.kalypsodeegree.model.feature.GMLWorkspace;

/**
 * @author Gernot Belger
 */
public class CsvSourceHandler implements ISourceHandler
{
  private final CsvSourceType m_type;

  private final URL m_context;

  private final IUrlResolver m_resolver;

  public CsvSourceHandler( final IUrlResolver resolver, final URL context, final CsvSourceType type )
  {
    m_resolver = resolver;
    m_context = context;
    m_type = type;
  }

  @Override
  public GMLWorkspace getWorkspace( ) throws GmlConvertException, GMLSchemaException
  {
    final CsvFeatureReader reader = new CsvFeatureReader( m_type, m_resolver, m_context );
    reader.read();
    return reader.getWorkspace();
  }
}
