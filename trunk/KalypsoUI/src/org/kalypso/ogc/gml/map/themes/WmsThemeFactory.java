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
package org.kalypso.ogc.gml.map.themes;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.commons.i18n.I10nString;
import org.kalypso.commons.java.util.PropertiesHelper;
import org.kalypso.contribs.java.net.IUrlResolver2;
import org.kalypso.ogc.gml.IKalypsoTheme;
import org.kalypso.ogc.gml.mapmodel.IMapModell;
import org.kalypso.ogc.gml.selection.IFeatureSelectionManager;
import org.kalypso.ogc.gml.wms.provider.images.IKalypsoImageProvider;
import org.kalypso.ogc.gml.wms.utils.KalypsoWMSUtilities;
import org.kalypso.template.types.StyledLayerType;
import org.kalypso.template.types.StyledLayerType.Style;
import org.kalypso.ui.KalypsoGisPlugin;
import org.kalypsodeegree.graphics.sld.FeatureTypeStyle;
import org.kalypsodeegree.graphics.sld.NamedLayer;
import org.kalypsodeegree.graphics.sld.StyledLayerDescriptor;
import org.kalypsodeegree.graphics.sld.UserStyle;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree_impl.graphics.sld.SLDFactory;
import org.kalypsodeegree_impl.graphics.sld.StyleFactory;

/**
 * Theme factory for {@link KalypsoWMSTheme}s.
 * 
 * @author Gernot Belger
 */
public class WmsThemeFactory extends AbstractThemeFactory
{
  @Override
  public IKalypsoTheme createTheme( final I10nString layerName, final StyledLayerType layerType, final URL context, final IMapModell mapModell, final IFeatureSelectionManager selectionManager ) throws CoreException
  {
    try
    {
      final String source = layerType.getHref();
      final String linktype = layerType.getLinktype();

      /* Parse the source into properties. */
      final Properties sourceProps = PropertiesHelper.parseFromString( source, '#' );

      /* Get the provider attribute. */
      final String layerProp = sourceProps.getProperty( IKalypsoImageProvider.KEY_LAYERS, null );
      final String styleProp = sourceProps.getProperty( IKalypsoImageProvider.KEY_STYLES, null );
      final String service = sourceProps.getProperty( IKalypsoImageProvider.KEY_URL, null );
      final String providerID = sourceProps.getProperty( IKalypsoImageProvider.KEY_PROVIDER, null );

      /* Create the image provider. */
      final String[] layers = StringUtils.isBlank( layerProp ) ? new String[0] : layerProp.split( "," ); //$NON-NLS-1$
      final String[] styles = StringUtils.isBlank( styleProp ) ? new String[0] : styleProp.split( "," ); //$NON-NLS-1$
      String sldBody = null;
      final List<Style> styleList = layerType.getStyle();
      if( styleList.size() > 0 )
      {
        final IUrlResolver2 resolver = new IUrlResolver2()
        {
          @Override
          public URL resolveURL( final String relativeOrAbsolute ) throws MalformedURLException
          {
            return new URL( relativeOrAbsolute );
          }
        };

        final List<NamedLayer> namedLayers = new ArrayList<>();
        for( final Style style : styleList )
        {
          final URL styleUrl = new URL( style.getHref() );
          final FeatureTypeStyle ftStyle = SLDFactory.createFeatureTypeStyle( resolver, styleUrl );
          final UserStyle userStyle = StyleFactory.createUserStyle( null, null, null, false, new FeatureTypeStyle[] { ftStyle } );
          final NamedLayer layer = SLDFactory.createNamedLayer( style.getStyle(), null, new UserStyle[] { userStyle } );
          namedLayers.add( layer );
        }

        final StyledLayerDescriptor descriptor = SLDFactory.createStyledLayerDescriptor( namedLayers.toArray( new NamedLayer[] {} ) );
        sldBody = descriptor.exportAsXML();
      }

      final IKalypsoImageProvider imageProvider = KalypsoWMSUtilities.getImageProvider( layerName.getValue(), layers, styles, service, providerID, sldBody );

      return new KalypsoWMSTheme( linktype, layerName, layerType, imageProvider, mapModell );
    }
    catch( final Exception ex )
    {
      throw new CoreException( new Status( IStatus.ERROR, KalypsoGisPlugin.getId(), ex.getLocalizedMessage(), ex ) );
    }
  }

  @Override
  public void configureLayer( final IKalypsoTheme theme, final String id, final GM_Envelope bbox, final String srsName, final StyledLayerType layer, final IProgressMonitor monitor )
  {
    final KalypsoWMSTheme wmsTheme = (KalypsoWMSTheme)theme;

    layer.setHref( wmsTheme.getSource() );

    /* Configure the style. */
    configureStyle( wmsTheme, layer );
  }

  private static void configureStyle( final KalypsoWMSTheme theme, final StyledLayerType layer )
  {
    final org.kalypso.template.types.ObjectFactory extentFac = new org.kalypso.template.types.ObjectFactory();
    final Style[] oldStyles = theme.getStyles();
    for( final Style oldStyle : oldStyles )
    {
      final Style newStyle = extentFac.createStyledLayerTypeStyle();
      newStyle.setActuate( oldStyle.getActuate() );
      newStyle.setArcrole( oldStyle.getArcrole() );
      newStyle.setHref( oldStyle.getHref() );
      newStyle.setLinktype( oldStyle.getLinktype() );
      newStyle.setRole( oldStyle.getRole() );
      newStyle.setShow( oldStyle.getShow() );
      newStyle.setStyle( oldStyle.getStyle() );
      newStyle.setTitle( oldStyle.getTitle() );
      newStyle.setType( oldStyle.getType() );

      layer.getStyle().add( newStyle );
    }
  }
}