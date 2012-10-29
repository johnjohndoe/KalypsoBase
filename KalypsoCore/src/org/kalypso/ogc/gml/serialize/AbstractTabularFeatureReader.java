package org.kalypso.ogc.gml.serialize;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.commons.lang3.ArrayUtils;
import org.kalypso.contribs.java.net.IUrlResolver;
import org.kalypso.core.i18n.Messages;
import org.kalypso.gml.util.TabularSourceType;
import org.kalypso.gml.util.TabularSourceType.Featureproperty;
import org.kalypso.gmlschema.GMLSchemaException;
import org.kalypso.gmlschema.GMLSchemaFactory;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.IValuePropertyType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.gmlschema.types.IMarshallingTypeHandler;
import org.kalypso.gmlschema.types.ITypeRegistry;
import org.kalypso.gmlschema.types.MarshallingTypeRegistrySingleton;
import org.kalypso.ogc.gml.convert.GmlConvertException;
import org.kalypsodeegree.filterencoding.Filter;
import org.kalypsodeegree.filterencoding.FilterConstructionException;
import org.kalypsodeegree.filterencoding.FilterEvaluationException;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree.model.feature.IFeatureBindingCollection;
import org.kalypsodeegree_impl.filterencoding.AbstractFilter;
import org.kalypsodeegree_impl.gml.binding.shape.AbstractShape;
import org.kalypsodeegree_impl.gml.binding.shape.ShapeCollection;
import org.kalypsodeegree_impl.model.feature.FeatureFactory;
import org.kalypsodeegree_impl.model.feature.FeatureHelper;

/**
 * Lädt und schreibt ein CSV als {@link org.kalypsodeegree.model.feature.GMLWorkspace}. Die Information, welche Spalte
 * wie gelesen wird, wird per {@link #addInfo(IPropertyType, CSVInfo)}übergeben.
 * 
 * @todo Einerseits ganz schön, genau zu spezifizieren, was die Spalten sind. Alternativ wäre aber auch super, wenn das
 *       auch automatisch anhand der 1.Zeile ginge
 * @todo Koordinatensystem berücksichtigen
 * @author belger
 */
public abstract class AbstractTabularFeatureReader
{
  public static final class CSVInfo
  {
    public final int[] m_columns;

    public final String m_format;

    public final boolean m_ignoreFormatExceptions;

    public final boolean m_handleEmptyAsNull;

    public CSVInfo( final String frmt, final int[] cols, final boolean ignoreFrmtExceptions, final boolean handleEmptyAsNill )
    {
      m_format = frmt;
      m_columns = cols;
      m_ignoreFormatExceptions = ignoreFrmtExceptions;
      m_handleEmptyAsNull = handleEmptyAsNill;
    }
  }

  /** featureTypeProperty -> cvsinfo */
  private final Map<IPropertyType, CSVInfo> m_infos = new LinkedHashMap<>();

  private final Filter m_filter;

  private final TabularSourceType m_type;

  private final GMLWorkspace m_workspace;

  private final IUrlResolver m_resolver;

  private final URL m_context;

  public AbstractTabularFeatureReader( final TabularSourceType type, final IUrlResolver resolver, final URL context ) throws GmlConvertException, GMLSchemaException
  {
    m_type = type;
    m_resolver = resolver;
    m_context = context;

    m_filter = createFilter();

    initInfos();

    m_workspace = FeatureFactory.createGMLWorkspace( ShapeCollection.FEATURE_SHAPE_COLLECTION, null, null );

  }

  private void initInfos( ) throws GmlConvertException
  {
    final List<Featureproperty> propList = m_type.getFeatureproperty();
    final ITypeRegistry<IMarshallingTypeHandler> typeRegistry = MarshallingTypeRegistrySingleton.getTypeRegistry();
    for( final Featureproperty element : propList )
    {
      final int[] columns = createColumnList( element );
      final QName qname = new QName( "namespace", element.getName() ); //$NON-NLS-1$
      final IMarshallingTypeHandler typeHandler = typeRegistry.getTypeHandlerForTypeName( element.getType() );

      if( typeHandler == null )
        throw new GmlConvertException( String.format( Messages.getString( "AbstractTabularFeatureReader.0" ), element.getName(), element.getType() ) ); //$NON-NLS-1$

      final IPropertyType ftp = GMLSchemaFactory.createValuePropertyType( qname, typeHandler, 0, 1, false );
      final boolean ignoreFormatExceptions = element.isIgnoreFormatExceptions();
      final String format = element.getFormat();
      final boolean handleEmptyAsNull = element.isHandleEmptyAsNull();
      final CSVInfo info = new CsvFeatureReader.CSVInfo( format, columns, ignoreFormatExceptions, handleEmptyAsNull );
      addInfo( ftp, info );
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

  public GMLWorkspace getWorkspace( )
  {
    return m_workspace;
  }

  public int getLineskip( )
  {
    return m_type.getLineskip();
  }

  protected IFeatureBindingCollection<AbstractShape> getFeatureList( )
  {
    final ShapeCollection rootFeature = (ShapeCollection)m_workspace.getRootFeature();
    return rootFeature.getShapes();
  }

  private void addInfo( final IPropertyType ftp, final CSVInfo info )
  {
    m_infos.put( ftp, info );
  }

  private Filter createFilter( ) throws GmlConvertException
  {
    try
    {
      final Object anyType = m_type.getFilter();
      if( anyType == null )
        return null;

      return AbstractFilter.buildFromAnyType( anyType );
    }
    catch( final FilterConstructionException e )
    {
      e.printStackTrace();
      throw new GmlConvertException( Messages.getString( "AbstractTabularFeatureReader.1" ), e ); //$NON-NLS-1$
    }
  }

  public abstract void read( ) throws GmlConvertException;

  protected final Feature createFeatureFromTokens( final Feature parent, final IRelationType parentRelation, final String index, final String[] tokens, final IFeatureType featureType ) throws CsvException
  {
    final IPropertyType[] properties = featureType.getProperties();
    final Object[] data = new Object[properties.length];
    for( int i = 0; i < data.length; i++ )
    {
      final IPropertyType ftp = properties[i];
      if( !(ftp instanceof IValuePropertyType) )
        continue;
      final IValuePropertyType vpt = (IValuePropertyType)ftp;
      final CSVInfo info = m_infos.get( ftp );

      // check column numbers
      for( final int colNumber : info.m_columns )
      {
        if( colNumber >= tokens.length )
          throw new CsvException( Messages.getString( "org.kalypso.ogc.gml.serialize.CsvFeatureReader.3" ) + index + Messages.getString( "org.kalypso.ogc.gml.serialize.CsvFeatureReader.4" ) + colNumber + Messages.getString( "org.kalypso.ogc.gml.serialize.CsvFeatureReader.5" ) + ftp.getQName() + "'" + Messages.getString( "org.kalypso.ogc.gml.serialize.CsvFeatureReader.7" ) + tokens.length + Messages.getString( "org.kalypso.ogc.gml.serialize.CsvFeatureReader.8" ) ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
      }

      data[i] = parseColumns( vpt, info, tokens );
    }

    return FeatureFactory.createFeature( parent, parentRelation, index, featureType, data );
  }

  private static Object parseColumns( final IValuePropertyType vpt, final CSVInfo info, final String[] tokens ) throws CsvException
  {
    try
    {
      final String[] input = new String[info.m_columns.length];
      for( int i = 0; i < input.length; i++ )
        input[i] = tokens[info.m_columns[i]];

      return FeatureHelper.createFeaturePropertyFromStrings( vpt, info.m_format, input, info.m_handleEmptyAsNull );
    }
    catch( final NumberFormatException nfe )
    {
      if( info.m_ignoreFormatExceptions )
        return null;

      final String colStr = ArrayUtils.toString( info.m_columns );
      throw new CsvException( Messages.getString( "org.kalypso.ogc.gml.serialize.CsvFeatureReader.10" ) + colStr, nfe ); //$NON-NLS-1$
    }
  }

  protected final URL resolve( final String href ) throws MalformedURLException
  {
    return m_resolver.resolveURL( m_context, href );
  }

  public boolean acceptFeature( final Feature newFeature ) throws FilterEvaluationException
  {
    return m_filter == null || m_filter.evaluate( newFeature );
  }
}