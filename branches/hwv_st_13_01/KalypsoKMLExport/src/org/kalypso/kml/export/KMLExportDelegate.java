/**
 *
 */
package org.kalypso.kml.export;

import org.eclipse.core.runtime.IProgressMonitor;
import org.kalypso.kml.export.convert.ConvertFacade;
import org.kalypso.kml.export.interfaces.IKMLAdapter;
import org.kalypso.kml.export.utils.GoogleEarthExportUtils;
import org.kalypso.ogc.gml.painter.IStylePaintable;
import org.kalypsodeegree.graphics.sld.Symbolizer;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree_impl.graphics.displayelements.DisplayElementFactory;
import org.kalypsodeegree_impl.graphics.displayelements.ILabelPlacementStrategy;

import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Style;

/**
 * @author Dirk Kuch
 */
public class KMLExportDelegate implements IStylePaintable
{
  private final IKMLAdapter[] m_provider;

  private final double m_scale;

  private final GM_Envelope m_bbox;

  private final Folder m_folder;

  public KMLExportDelegate( final IKMLAdapter[] provider, final Folder folder, final double scale, final GM_Envelope bbox )
  {
    m_provider = provider;
    m_folder = folder;
    m_scale = scale;
    m_bbox = bbox;
  }

  @Override
  public void paint( final Feature feature, final Symbolizer symbolizer, final IProgressMonitor monitor )
  {
    final Style style = m_folder.createAndAddStyle();

    try
    {
      final GM_Object[] geometries = DisplayElementFactory.findGeometries( feature, symbolizer );

      if( !GoogleEarthExportUtils.updateStyle( style, symbolizer ) )
        return;

      for( final IKMLAdapter adapter : m_provider )
      {
        adapter.registerExportedFeature( feature );
      }

      // TODO perhaps, get rendered GM_Point geometry from symbolizer
      ConvertFacade.convert( m_provider, m_folder, geometries, style, feature );
    }
    catch( final Exception e )
    {
      e.printStackTrace();
    }
  }

  /**
   * @see org.kalypso.ogc.gml.IPaintDelegate#getScale()
   */
  @Override
  public Double getScale( )
  {
    return m_scale;
  }

  /**
   * @see org.kalypso.ogc.gml.IPaintDelegate#getBoundingBox()
   */
  @Override
  public GM_Envelope getBoundingBox( )
  {
    return m_bbox;
  }

  /**
   * @see org.kalypso.ogc.gml.painter.IStylePaintable#shouldPaintFeature(org.kalypsodeegree.model.feature.Feature)
   */
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
