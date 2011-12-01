package org.kalypso.ogc.gml.serialize;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.commons.lang.ArrayUtils;
import org.kalypso.core.i18n.Messages;
import org.kalypso.gmlschema.GMLSchema;
import org.kalypso.gmlschema.GMLSchemaFactory;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.IValuePropertyType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypsodeegree.filterencoding.Filter;
import org.kalypsodeegree.filterencoding.FilterEvaluationException;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree_impl.model.feature.FeatureFactory;
import org.kalypsodeegree_impl.model.feature.FeatureHelper;
import org.kalypsodeegree_impl.model.feature.GMLWorkspace_Impl;

/**
 * Lädt und schreibt ein CSV als {@link org.kalypsodeegree.model.feature.GMLWorkspace}. Die Information, welche Spalte
 * wie gelesen wird, wird per {@link #addInfo(IPropertyType, CSVInfo)}übergeben.
 * 
 * @todo Einerseits ganz schön, genau zu spezifizieren, was die Spalten sind. Alternativ wäre aber auch super, wenn das
 *       auch automatisch anhand der 1.Zeile ginge
 * @todo Koordinatensystem berücksichtigen
 * @author belger
 */
public final class CsvFeatureReader
{
  public static final class CSVInfo
  {
    public final int[] m_columns;

    public final String m_format;

    public final boolean m_ignoreFormatExceptions;

    public final boolean m_handleEmptyAsNull;

    public CSVInfo( final String frmt, final int[] cols, final boolean ignoreFrmtExceptions, final boolean handleEmptyAsNill )
    {
      this.m_format = frmt;
      this.m_columns = cols;
      this.m_ignoreFormatExceptions = ignoreFrmtExceptions;
      this.m_handleEmptyAsNull = handleEmptyAsNill;
    }
  }

  /** featureTypeProperty -> cvsinfo */
  private final Map<IPropertyType, CSVInfo> m_infos = new LinkedHashMap<IPropertyType, CSVInfo>();

  private Filter m_filter;

  public final void addInfo( final IPropertyType ftp, final CSVInfo info )
  {
    m_infos.put( ftp, info );
  }

  public void setFilter( final Filter filter )
  {
    m_filter = filter;
  }

  public final GMLWorkspace loadCSV( final Reader reader, final String comment, final String delemiter, final int lineskip ) throws IOException, CsvException, FilterEvaluationException
  {
    final IPropertyType[] props = m_infos.keySet().toArray( new IPropertyType[0] );
    final IFeatureType ft = GMLSchemaFactory.createFeatureType( new QName( "namespace", "csv" ), props ); //$NON-NLS-1$ //$NON-NLS-2$

    final Feature rootFeature = ShapeSerializer.createShapeRootFeature( ft );
    final IRelationType memberRelation = (IRelationType) rootFeature.getFeatureType().getProperty( ShapeSerializer.PROPERTY_FEATURE_MEMBER );
    final List<Feature> list = (List<Feature>) rootFeature.getProperty( memberRelation );
    loadCSVIntoList( rootFeature, memberRelation, ft, list, reader, comment, delemiter, lineskip );

    final GMLSchema schema = null;
    final URL context = null;
    final String schemaLocation = null;
    return new GMLWorkspace_Impl( schema, new IFeatureType[] { rootFeature.getFeatureType(), ft }, rootFeature, context, null, schemaLocation, null );
  }

  private void loadCSVIntoList( final Feature parent, final IRelationType parentRelation, final IFeatureType ft, final List<Feature> list, final Reader reader, final String comment, final String delemiter, final int lineskip ) throws IOException, CsvException, FilterEvaluationException
  {
    final LineNumberReader lnr = new LineNumberReader( reader );
    int skippedlines = 0;
    while( lnr.ready() )
    {
      final String line = lnr.readLine();
      if( line == null )
        break;
      if( skippedlines < lineskip )
      {
        skippedlines++;
        continue;
      }

      if( line.startsWith( comment ) )
        continue;

      final String[] tokens = line.split( delemiter, -1 );
      final Feature newFeature = createFeatureFromTokens( parent, parentRelation, "" + lnr.getLineNumber(), tokens, ft ); //$NON-NLS-1$
      if( m_filter == null || m_filter.evaluate( newFeature ) )
        list.add( newFeature );
    }
    return;
  }

  private Feature createFeatureFromTokens( final Feature parent, final IRelationType parentRelation, final String index, final String[] tokens, final IFeatureType featureType ) throws CsvException
  {
    final IPropertyType[] properties = featureType.getProperties();
    final Object[] data = new Object[properties.length];
    for( int i = 0; i < data.length; i++ )
    {
      final IPropertyType ftp = properties[i];
      if( !(ftp instanceof IValuePropertyType) )
        continue;
      final IValuePropertyType vpt = (IValuePropertyType) ftp;
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
}