/**
 *
 */
package org.kalypso.kml.export;

import java.util.List;

import javax.xml.bind.JAXBElement;

import net.opengis.kml.AbstractFeatureType;
import net.opengis.kml.FolderType;
import net.opengis.kml.GroundOverlayType;
import net.opengis.kml.ObjectFactory;
import net.opengis.kml.PlacemarkType;
import net.opengis.kml.StyleType;

import org.apache.commons.lang.NotImplementedException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.kalypso.kml.export.convert.ConvertFacade;
import org.kalypso.kml.export.interfaces.IKMLAdapter;
import org.kalypso.kml.export.utils.GoogleEarthExportUtils;
import org.kalypso.ogc.gml.painter.IStylePaintable;
import org.kalypsodeegree.graphics.displayelements.DisplayElement;
import org.kalypsodeegree.graphics.displayelements.GeometryDisplayElement;
import org.kalypsodeegree.graphics.sld.Symbolizer;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.geometry.GM_Envelope;

/**
 * @author Dirk Kuch
 */
public class KMLExportDelegate implements IStylePaintable
{
  private final FolderType m_folderType;

  private final ObjectFactory m_factory;

  private final IKMLAdapter[] m_provider;

  private final double m_scale;

  private final GM_Envelope m_bbox;

  public KMLExportDelegate( final IKMLAdapter[] provider, final ObjectFactory factory, final FolderType folderType, final double scale, final GM_Envelope bbox )
  {
    m_provider = provider;
    m_factory = factory;
    m_folderType = folderType;
    m_scale = scale;
    m_bbox = bbox;
  }

  /*
   * (non-Javadoc)
   * @see org.kalypso.ogc.gml.IPaintDelegate#paint(org.kalypsodeegree.graphics.displayelements.DisplayElement)
   */
  public void paint( final DisplayElement displayElement, final IProgressMonitor monitor )
  {
    final StyleType styleType;

    if( displayElement instanceof GeometryDisplayElement )
    {
      final GeometryDisplayElement element = (GeometryDisplayElement) displayElement;
      final Symbolizer symbolizer = element.getSymbolizer();

      try
      {
        styleType = GoogleEarthExportUtils.getStyleType( m_factory, symbolizer );
        if( styleType == null )
          return;

        final Feature feature = displayElement.getFeature();
        for( final IKMLAdapter adapter : m_provider )
        {
          adapter.registerExportedFeature( feature );
        }

        // TODO perhaps, get rendered GM_Point geometry from symbolizer
        final AbstractFeatureType[] featureTypes = ConvertFacade.convert( m_provider, m_factory, element.getGeometry(), styleType, feature );

        final List<JAXBElement< ? extends AbstractFeatureType>> features = m_folderType.getAbstractFeatureGroup();
        for( final AbstractFeatureType featureType : featureTypes )
        {
          if( featureType instanceof PlacemarkType )
          {
            features.add( m_factory.createPlacemark( (PlacemarkType) featureType ) );
          }
          else if( featureType instanceof GroundOverlayType )
          {
            features.add( m_factory.createGroundOverlay( (GroundOverlayType) featureType ) );
          }
          else
            throw new NotImplementedException();
        }
      }
      catch( final Exception e )
      {
        e.printStackTrace();
      }
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
}
