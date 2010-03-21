/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraﬂe 22
 *  21073 Hamburg, Germany
 *  http://www.tuhh.de/wb
 *
 *  and
 *
 *  Bjoernsen Consulting Engineers (BCE)
 *  Maria Trost 3
 *  56070 Koblenz, Germany
 *  http://www.bjoernsen.de
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *  Contact:
 *
 *  E-Mail:
 *  belger@bjoernsen.de
 *  schlienger@bjoernsen.de
 *  v.doemming@tuhh.de
 *
 *  ---------------------------------------------------------------------------*/
package org.kalypso.ogc.gml;

import java.net.URL;

import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;
import javax.media.jai.TiledImage;

import org.eclipse.core.runtime.IStatus;
import org.kalypso.commons.i18n.I10nString;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.java.net.UrlResolverSingleton;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.core.i18n.Messages;
import org.kalypso.grid.GridFileVerifier;
import org.kalypso.grid.IGridMetaReader;
import org.kalypso.ogc.gml.mapmodel.IMapModell;
import org.kalypso.template.types.StyledLayerType;
import org.kalypsodeegree.model.coverage.GridRange;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree_impl.gml.binding.commons.RectifiedGridDomain;
import org.kalypsodeegree_impl.gml.binding.commons.RectifiedGridDomain.OffsetVector;
import org.kalypsodeegree_impl.model.cv.GridRange_Impl;
import org.kalypsodeegree_impl.model.geometry.GeometryFactory;

/**
 * @author Dirk Kuch
 */
public class KalypsoPictureThemeWorldFile extends KalypsoPictureTheme
{
  public KalypsoPictureThemeWorldFile( final I10nString layerName, final StyledLayerType layerType, final URL context, final IMapModell modell, final String system ) 
  {
    super( layerName, layerType, context, modell );

    final String href = layerType.getHref();
    if( href == null || !href.contains( "." ) ) //$NON-NLS-1$
      throw new IllegalStateException();

    // FIXME: the second argument is never read...!
    final String[] arrFileName = href.split( "#" ); //$NON-NLS-1$
    if( arrFileName.length != 2 )
      throw new IllegalStateException();

    RenderedOp image = null; 
    try
    {
      final String relativeURL = arrFileName[0];
      final String srsName = arrFileName[1];
      
      // UGLY HACK: replace backslashes with slashes. The add-picture-theme action seems to put backslashes (on windows)
      // in the relative URLs (which is even wrong in windows). Should be fixed there, but is fixed also here to support older projects.
      final String relativeURLchecked = relativeURL.replaceAll( "\\\\", "/" );
      
      final URL imageUrl = UrlResolverSingleton.resolveUrl( context, relativeURLchecked );

      if( GridFileVerifier.verify( imageUrl ) )
      {
        final IGridMetaReader reader = GridFileVerifier.getRasterMetaReader( imageUrl, null );

        image = JAI.create( "url", imageUrl ); //$NON-NLS-1$
        setImage( new TiledImage( image, true ) );

        final int height = getImage().getHeight();
        final int width = getImage().getWidth();
        final GM_Point origin = GeometryFactory.createGM_Point( reader.getOriginCornerX(), reader.getOriginCornerY(), srsName );

        final OffsetVector offsetX = new OffsetVector( new Double( reader.getVectorXx() ), new Double( reader.getVectorXy() ) );
        final OffsetVector offsetY = new OffsetVector( new Double( reader.getVectorYx() ), new Double( reader.getVectorYy() ) );
        final GridRange gridRange = new GridRange_Impl( new double[] { 0, 0 }, new double[] { width, height } );

        setRectifiedGridDomain( new RectifiedGridDomain( origin, offsetX, offsetY, gridRange ) );
      }
    }
    catch( final Exception e )
    {
      final IStatus status = StatusUtilities.createStatus( IStatus.ERROR, Messages.getString("org.kalypso.ogc.gml.KalypsoPictureThemeWorldFile.0"), e ); //$NON-NLS-1$
      KalypsoCorePlugin.getDefault().getLog().log( status );
      setStatus( status );
      // We cannot throw exceptions here, it will break the whole map. This holds for all themes...
    }
    finally
    {
      if( image != null )
        image.dispose();
    }
  }
}
