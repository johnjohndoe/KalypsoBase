package org.kalypso.ogc.gml.serialize;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.io.IOUtils;
import org.kalypso.commons.java.net.UrlUtilities;
import org.kalypso.contribs.java.net.IUrlResolver;
import org.kalypso.core.i18n.Messages;
import org.kalypso.gml.util.CsvSourceType;
import org.kalypso.gmlschema.GMLSchemaException;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.ogc.gml.convert.GmlConvertException;
import org.kalypsodeegree.filterencoding.FilterEvaluationException;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.IFeatureBindingCollection;
import org.kalypsodeegree_impl.gml.binding.shape.AbstractShape;

/**
 * Lädt und schreibt ein CSV als {@link org.kalypsodeegree.model.feature.GMLWorkspace}. Die Information, welche Spalte
 * wie gelesen wird, wird per {@link #addInfo(IPropertyType, CSVInfo)}übergeben.
 * 
 * @todo Einerseits ganz schön, genau zu spezifizieren, was die Spalten sind. Alternativ wäre aber auch super, wenn das
 *       auch automatisch anhand der 1.Zeile ginge
 * @todo Koordinatensystem berücksichtigen
 * @author belger
 */
public final class CsvFeatureReader extends AbstractTabularFeatureReader
{
  private final String m_comment;

  private final String m_delemiter;

  private final String m_href;

  public CsvFeatureReader( final CsvSourceType type, final IUrlResolver resolver, final URL context ) throws GmlConvertException, GMLSchemaException
  {
    super( type, resolver, context );

    m_comment = type.getComment();
    m_delemiter = type.getDelemiter();
    m_href = type.getHref();
  }

  @Override
  public void read( ) throws GmlConvertException
  {
    InputStream stream = null;
    try
    {
      final URL url = super.resolve( m_href );
      final URLConnection connection = url.openConnection();
      stream = connection.getInputStream();

      final String encoding = UrlUtilities.findEncoding( connection );

      final InputStreamReader isr = encoding == null ? new InputStreamReader( stream ) : new InputStreamReader( stream, encoding );
      loadCSV( isr, m_comment, m_delemiter, getLineskip() );
      stream.close();
    }
    catch( final Exception e )
    {
      e.printStackTrace();
      throw new GmlConvertException( Messages.getString( "org.kalypso.ogc.gml.convert.source.CsvSourceHandler.1" ) + m_href, e ); //$NON-NLS-1$
    }
    finally
    {
      IOUtils.closeQuietly( stream );
    }
  }

  private void loadCSV( final InputStreamReader isr, final String comment, final String delemiter, final int lineskip ) throws CsvException, IOException, FilterEvaluationException
  {
    final IFeatureBindingCollection<AbstractShape> featureList = getFeatureList();
    final Feature parentFeature = featureList.getParentFeature();
    final IRelationType parentRelation = featureList.getFeatureList().getPropertyType();
    final IFeatureType featureType = parentRelation.getTargetFeatureType();

    final LineNumberReader lnr = new LineNumberReader( isr );
    int skippedlines = 0;
    while( lnr.ready() )
    {
      final String line = lnr.readLine();
      if( line == null )
        break;
      if( skippedlines < getLineskip() )
      {
        skippedlines++;
        continue;
      }

      if( line.startsWith( m_comment ) )
        continue;

      final String[] tokens = line.split( m_delemiter, -1 );

      final Feature newFeature = createFeatureFromTokens( parentFeature, parentRelation, "" + lnr.getLineNumber(), tokens, featureType ); //$NON-NLS-1$
      if( acceptFeature( newFeature ) )
        featureList.getFeatureList().add( newFeature );
    }
  }
}