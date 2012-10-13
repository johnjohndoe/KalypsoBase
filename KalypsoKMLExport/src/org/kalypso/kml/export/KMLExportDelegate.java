/**
 *
 */
package org.kalypso.kml.export;

import org.eclipse.core.runtime.IProgressMonitor;
import org.kalypso.kml.export.convert.StyleConverter;
import org.kalypso.kml.export.interfaces.IKMLAdapter;
import org.kalypso.ogc.gml.painter.IStylePaintable;
import org.kalypsodeegree.graphics.sld.Symbolizer;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree_impl.graphics.displayelements.ILabelPlacementStrategy;

import de.micromata.opengis.kml.v_2_2_0.Folder;

/**
 * @author Dirk Kuch
 */
public class KMLExportDelegate implements IStylePaintable
{
  private final IKMLAdapter[] m_provider;

  private final double m_scale;

  private final GM_Envelope m_bbox;

  private final Folder m_folder;

  private final StyleConverter m_converter;

  public KMLExportDelegate( final IKMLAdapter[] provider, final Folder folder, final double scale, final GM_Envelope bbox, final StyleConverter converter )
  {
    m_provider = provider;
    m_folder = folder;
    m_scale = scale;
    m_bbox = bbox;
    m_converter = converter;
  }

  @Override
  public void paint( final Feature feature, final Symbolizer symbolizer, final IProgressMonitor newChild )
  {
    try
    {
      for( final IKMLAdapter adapter : m_provider )
      {
        adapter.registerExportedFeature( feature );
      }

      m_converter.convert( m_provider, m_folder, symbolizer, feature );
    }
    catch( final Exception e )
    {
      e.printStackTrace();
    }
  }

  @Override
  public Double getScale( )
  {
    return m_scale;
  }

  @Override
  public GM_Envelope getBoundingBox( )
  {
    return m_bbox;
  }

  @Override
  public boolean shouldPaintFeature( final Feature feature )
  {
    return true;
  }

  @Override
  public ILabelPlacementStrategy createLabelStrategy( )
  {
    return null;
  }

  @Override
  public void paintLabels( final ILabelPlacementStrategy strategy )
  {
  }
}
