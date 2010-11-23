/*
 * --------------- Kalypso-Header --------------------------------------------------------------------
 *
 * This file is part of kalypso. Copyright (C) 2004, 2005 by:
 *
 * Technical University Hamburg-Harburg (TUHH) Institute of River and coastal engineering Denickestr. 22 21073 Hamburg,
 * Germany http://www.tuhh.de/wb
 *
 * and
 *
 * Bjoernsen Consulting Engineers (BCE) Maria Trost 3 56070 Koblenz, Germany http://www.bjoernsen.de
 *
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Contact:
 *
 * E-Mail: belger@bjoernsen.de schlienger@bjoernsen.de v.doemming@tuhh.de
 *
 * ---------------------------------------------------------------------------------------------------
 */
package org.kalypso.ogc.gml;

import java.awt.Graphics;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;
import javax.media.jai.TiledImage;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.commons.i18n.I10nString;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.java.net.UrlResolverSingleton;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.core.i18n.Messages;
import org.kalypso.ogc.gml.mapmodel.IMapModell;
import org.kalypso.template.types.ObjectFactory;
import org.kalypso.template.types.StyledLayerType;
import org.kalypso.transformation.transformer.GeoTransformerFactory;
import org.kalypso.transformation.transformer.IGeoTransformer;
import org.kalypsodeegree.KalypsoDeegreePlugin;
import org.kalypsodeegree.graphics.transformation.GeoTransform;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree_impl.gml.binding.commons.RectifiedGridDomain;
import org.kalypsodeegree_impl.tools.TransformationUtilities;

/**
 * KalypsoPictureTheme
 * <p>
 * created by
 * 
 * @author kuepfer (20.05.2005)
 */
public abstract class KalypsoPictureTheme extends AbstractKalypsoTheme
{
  // TODO: use tracing instead
  private static final Logger LOGGER = Logger.getLogger( KalypsoPictureTheme.class.getName() );

  private TiledImage m_image = null;

  private RectifiedGridDomain m_domain;

  private final StyledLayerType m_layerType;

  public KalypsoPictureTheme( final I10nString layerName, final StyledLayerType layerType, final URL context, final IMapModell modell )
  {
    super( layerName, layerType.getLinktype(), modell );

    setContext( context );

    m_layerType = layerType;
  }

  /**
   * @see org.kalypso.ogc.gml.IKalypsoTheme#dispose()
   */
  @Override
  public void dispose( )
  {
    if( m_image != null )
    {
      m_image.dispose();
      m_image = null;
    }

    super.dispose();
  }

  public void fillLayerType( final StyledLayerType layer, final String id, final boolean visible )
  {
    final ObjectFactory extentFac = new ObjectFactory();

    layer.setName( m_layerType.getName() );
    layer.setFeaturePath( "" ); //$NON-NLS-1$

    layer.setVisible( visible );
    layer.setId( id );
    layer.setHref( m_layerType.getHref() );
    layer.setLinktype( m_layerType.getLinktype() );
    layer.setActuate( "onRequest" ); //$NON-NLS-1$
    layer.setType( "simple" ); //$NON-NLS-1$

    final String legendIcon = getLegendIcon();
    if( legendIcon != null )
      layer.setLegendicon( extentFac.createStyledLayerTypeLegendicon( legendIcon ) );

    layer.setShowChildren( extentFac.createStyledLayerTypeShowChildren( shouldShowLegendChildren() ) );
  }

  /**
   * @see org.kalypso.ogc.gml.IKalypsoTheme#getBoundingBox()
   */
  @Override
  public GM_Envelope getFullExtent( )
  {
    try
    {
      final GM_Envelope gmEnvelope = m_domain.getGM_Envelope( m_domain.getCoordinateSystem() );
      final String kalypsoCrs = KalypsoDeegreePlugin.getDefault().getCoordinateSystem();
      final IGeoTransformer geoTransformer = GeoTransformerFactory.getGeoTransformer( kalypsoCrs );
      return geoTransformer.transform( gmEnvelope );
    }
    catch( final Exception e2 )
    {
      e2.printStackTrace();
      KalypsoPictureTheme.LOGGER.warning( Messages.getString( "org.kalypso.ogc.gml.KalypsoPictureTheme.9" ) ); //$NON-NLS-1$
    }
    return null;
  }

  protected TiledImage getImage( )
  {
    return m_image;
  }

  protected RectifiedGridDomain getRectifiedGridDomain( )
  {
    return m_domain;
  }

  protected StyledLayerType getStyledLayerType( )
  {
    return m_layerType;
  }

  /**
   * @see org.kalypso.ogc.gml.IKalypsoTheme#paint(java.awt.Graphics,
   *      org.kalypsodeegree.graphics.transformation.GeoTransform, java.lang.Boolean,
   *      org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  public IStatus paint( final Graphics g, final GeoTransform p, final Boolean selected, final IProgressMonitor monitor )
  {
    if( selected != null && selected )
      return Status.OK_STATUS;

    if( m_domain == null )
      return Status.OK_STATUS;

    if( m_image == null )
      return Status.OK_STATUS;

    try
    {
      final String pictureCrs = m_domain.getCoordinateSystem();
      final GM_Envelope envelope = m_domain.getGM_Envelope( pictureCrs );
      final String crs = KalypsoDeegreePlugin.getDefault().getCoordinateSystem();
      TransformationUtilities.transformImage( m_image, envelope, crs, p, g );
      return Status.OK_STATUS;
    }
    catch( final Exception e )
    {
      e.printStackTrace();
      final IStatus status = StatusUtilities.statusFromThrowable( e );
      setStatus( status );
      return status;
    }

  }

  protected void setImage( final TiledImage image )
  {
    if( m_image != null )
      m_image.dispose();

    m_image = image;
  }

  protected void setRectifiedGridDomain( final RectifiedGridDomain domain )
  {
    m_domain = domain;
  }

  protected URL loadImage( final String filePath )
  {
    RenderedOp image = null;

    try
    {
      // UGLY HACK: replace backslashes with slashes. The add-picture-theme action seems to put backslashes (on windows)
      // in the relative URLs (which is even wrong in windows). Should be fixed there, but is fixed also here to support
      // older projects.
      final String filePathChecked = filePath.replaceAll( "\\\\", "/" );

      final URL context = getContext();
      final URL imageUrl = UrlResolverSingleton.resolveUrl( context, filePathChecked );

      image = JAI.create( "url", imageUrl ); //$NON-NLS-1$

      // FIXME we get out of memory here, as the whole image is loaded... we should instead access the tiles of the
      // RenderdOp
      setImage( new TiledImage( image, true ) );
      return imageUrl;
    }
    catch( final MalformedURLException e )
    {
      setStatus( e, "Fehlerhafter Dateipfad %s", filePath );
    }
    catch( final OutOfMemoryError error )
    {
      // REMARK: this will happen if we load big images
      // It is safe to catch it here, as the heap will be freed immediately, if the image could not be loaded
      setStatus( error, "Zu wenig Speicher zum Laden von Bild %s. Versuchen Sie die Bildgröße zu verkleinern oder dem Programm mehr Speicher zuzuweisen.", filePath );
    }
    catch( final Throwable error )
    {
      // REMARK: this will happen if we load big images
      // It is safe to catch it here, as the heap will be freed immediately, if the image could not be loaded
      setStatus( error, "Unerwarteter Fehler beim Laden von Bild %s, vermutlich zu wenig Speicher. Versuchen Sie die Bildgröße zu verkleinern oder dem Programm mehr Speicher zuzuweisen.", filePath );
    }
    finally
    {
      if( image != null )
        image.dispose();
    }

    return null;
  }

  private IStatus setStatus( final Throwable e, final String formatString, final Object... formatArguments )
  {
    e.printStackTrace();
    final String msg = String.format( formatString, formatArguments );
    final IStatus status = new Status( IStatus.ERROR, KalypsoCorePlugin.getID(), msg, e );
    setStatus( status );
    return status;
  }

}