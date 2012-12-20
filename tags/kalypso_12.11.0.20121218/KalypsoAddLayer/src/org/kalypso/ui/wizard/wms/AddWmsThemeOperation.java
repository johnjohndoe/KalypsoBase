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
package org.kalypso.ui.wizard.wms;

import org.deegree.ogcwebservices.wms.capabilities.Layer;
import org.deegree.ogcwebservices.wms.capabilities.Style;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.commons.command.ICommandTarget;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.ogc.gml.IKalypsoLayerModell;
import org.kalypso.ogc.gml.wms.provider.images.IKalypsoImageProvider;
import org.kalypso.ui.action.AddThemeCommand;
import org.kalypso.ui.addlayer.internal.wms.CapabilitiesInfo;
import org.kalypso.ui.addlayer.internal.wms.ImportWmsData;

/**
 * @author Gernot Belger
 */
public class AddWmsThemeOperation implements ICoreRunnableWithProgress
{
  private final ICommandTarget m_cmdTarget;

  private final IKalypsoLayerModell m_mapModell;

  private final ImportWmsData m_data;

  public AddWmsThemeOperation( final ImportWmsData data, final ICommandTarget cmdTarget, final IKalypsoLayerModell mapModell )
  {
    m_data = data;
    m_cmdTarget = cmdTarget;
    m_mapModell = mapModell;
  }

  @Override
  public IStatus execute( final IProgressMonitor monitor )
  {
    /* Finishes the work on this page (dialog settings). */
    // FIXME: should be done by each wizard
    // m_wmsPage.finish();

    final CapabilitiesInfo info = m_data.getCurrentService();
    // FIXME: finish button should not be enabled
    if( !info.isLoaded() )
      return Status.CANCEL_STATUS;

    final Layer[] layerArray = m_data.getChosenLayers();

    final boolean isMulti = m_data.getMultiLayer();
    if( isMulti )
    {
      final String source = formatSource( info, layerArray );

      final String title = info.getTitle();
      final String layerName = title;

      addTheme( layerName, source );
    }
    else
    {
      for( final Layer layer : layerArray )
      {
        final String source = formatSource( info, layer );

        final String layerTitle = layer.getTitle();

        addTheme( layerTitle, source );
      }
    }

    return Status.OK_STATUS;
  }

  private void addTheme( final String layerName, final String source )
  {
    final AddThemeCommand command = new AddThemeCommand( m_mapModell, layerName, "wms", source ); //$NON-NLS-1$
    m_cmdTarget.postCommand( command, null );
  }

  private String formatSource( final CapabilitiesInfo info, final Layer... layers )
  {
    final String address = info.getAddress();
    final String providerID = info.getImageProvider();

    final StringBuilder layersBuffer = new StringBuilder( IKalypsoImageProvider.KEY_LAYERS + "=" ); //$NON-NLS-1$
    final StringBuilder stylesBuffer = new StringBuilder( IKalypsoImageProvider.KEY_STYLES + "=" ); //$NON-NLS-1$

    for( int i = 0; i < layers.length; i++ )
    {
      final Layer layer = layers[i];
      // TODO: we should let the user choose between the available style
      final String styleName = guessStyleName( layer );

      layersBuffer.append( layer.getName() );
      stylesBuffer.append( styleName );

      if( i < layers.length - 1 )
      {
        layersBuffer.append( "," ); //$NON-NLS-1$
        stylesBuffer.append( "," ); //$NON-NLS-1$
      }
    }

    /* Build source string */
    final StringBuffer source = new StringBuffer();
    source.append( IKalypsoImageProvider.KEY_URL ).append( '=' ).append( address );

    source.append( "#" ).append( layersBuffer.toString() ); //$NON-NLS-1$
    source.append( "#" ).append( stylesBuffer.toString() ); //$NON-NLS-1$

    if( providerID != null )
      source.append( "#" ).append( IKalypsoImageProvider.KEY_PROVIDER ).append( '=' ).append( providerID ); //$NON-NLS-1$

    return source.toString();
  }

  private String guessStyleName( final Layer layer )
  {
    final Style[] styles2 = layer.getStyles();
    if( styles2.length > 0 )
      return styles2[0].getName();

    return "default"; //$NON-NLS-1$
  }
}