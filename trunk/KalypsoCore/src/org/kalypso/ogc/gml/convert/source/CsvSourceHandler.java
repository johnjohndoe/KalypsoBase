package org.kalypso.ogc.gml.convert.source;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.commons.io.IOUtils;
import org.kalypso.commons.java.net.UrlUtilities;
import org.kalypso.contribs.java.net.IUrlResolver;
import org.kalypso.core.i18n.Messages;
import org.kalypso.gml.util.CsvSourceType;
import org.kalypso.gml.util.CsvSourceType.Featureproperty;
import org.kalypso.gmlschema.GMLSchemaFactory;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.types.IMarshallingTypeHandler;
import org.kalypso.gmlschema.types.ITypeRegistry;
import org.kalypso.gmlschema.types.MarshallingTypeRegistrySingleton;
import org.kalypso.ogc.gml.convert.GmlConvertException;
import org.kalypso.ogc.gml.serialize.CsvException;
import org.kalypso.ogc.gml.serialize.CsvFeatureReader;
import org.kalypso.ogc.gml.serialize.CsvFeatureReader.CSVInfo;
import org.kalypsodeegree.filterencoding.Filter;
import org.kalypsodeegree.filterencoding.FilterConstructionException;
import org.kalypsodeegree.filterencoding.FilterEvaluationException;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree_impl.filterencoding.AbstractFilter;

/**
 * @author belger
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

  /**
   * @throws GmlConvertException
   * @see org.kalypso.ogc.gml.convert.source.ISourceHandler#getWorkspace()
   */
  public GMLWorkspace getWorkspace( ) throws GmlConvertException
  {
    final String href = m_type.getHref();

    final InputStream stream = null;
    try
    {
      final CsvFeatureReader reader = createReader();

      return loadCSV( reader, href );
    }
    catch( final Exception e )
    {
      e.printStackTrace();
      throw new GmlConvertException( Messages.getString( "org.kalypso.ogc.gml.convert.source.CsvSourceHandler.1" ) + href, e ); //$NON-NLS-1$
    }
    finally
    {
      IOUtils.closeQuietly( stream );
    }
  }

  private GMLWorkspace loadCSV( final CsvFeatureReader reader, final String href ) throws IOException, CsvException, FilterEvaluationException
  {
    InputStream stream = null;
    try
    {
      final URL url = m_resolver.resolveURL( m_context, href );
      final URLConnection connection = url.openConnection();
      stream = connection.getInputStream();

      final String encoding = UrlUtilities.findEncoding( connection );

      final InputStreamReader isr = encoding == null ? new InputStreamReader( stream ) : new InputStreamReader( stream, encoding );
      final GMLWorkspace workspace = reader.loadCSV( isr, m_type.getComment(), m_type.getDelemiter(), m_type.getLineskip() );
      stream.close();
      return workspace;
    }
    finally
    {
      IOUtils.closeQuietly( stream );
    }
  }

  private CsvFeatureReader createReader( ) throws GmlConvertException
  {
    final CsvFeatureReader reader = new CsvFeatureReader();
    configureFilter( reader );

    final List<Featureproperty> propList = m_type.getFeatureproperty();
    final ITypeRegistry<IMarshallingTypeHandler> typeRegistry = MarshallingTypeRegistrySingleton.getTypeRegistry();
    for( final Featureproperty element : propList )
    {
      final int[] columns = createColumnList( element );
      final QName qname = new QName( "namespace", element.getName() ); //$NON-NLS-1$
      final IMarshallingTypeHandler typeHandler = typeRegistry.getTypeHandlerForTypeName( element.getType() );

      if( typeHandler == null )
        throw new GmlConvertException( String.format( "No TypeHandler for '%s'with type '%s'", element.getName(), element.getType() ) );

      final IPropertyType ftp = GMLSchemaFactory.createValuePropertyType( qname, typeHandler, 0, 1, false );
      final boolean ignoreFormatExceptions = element.isIgnoreFormatExceptions();
      final String format = element.getFormat();
      final boolean handleEmptyAsNull = element.isHandleEmptyAsNull();
      final CSVInfo info = new CsvFeatureReader.CSVInfo( format, columns, ignoreFormatExceptions, handleEmptyAsNull );
      reader.addInfo( ftp, info );
    }

    return reader;
  }

  private void configureFilter( final CsvFeatureReader reader ) throws GmlConvertException
  {
    try
    {
      final Object anyType = m_type.getFilter();
      if( anyType == null )
        return;
      
      final Filter filter = AbstractFilter.buildFromAnyType( anyType );
      reader.setFilter( filter );
    }
    catch( final FilterConstructionException e )
    {
      e.printStackTrace();
      throw new GmlConvertException( "Failed to construct filter expression", e );
    }
  }

  private int[] createColumnList( final Featureproperty element )
  {
    final List<Integer> columnList = element.getColumn();
    final int[] columns = new int[columnList.size()];
    for( int i = 0; i < columnList.size(); i++ )
    {
      final Integer col = columnList.get( i );
      columns[i] = col.intValue();
    }
    return columns;
  }
}
