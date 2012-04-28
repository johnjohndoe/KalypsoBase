/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.deegree.ogcwebservices.wms.capabilities.Layer;
import org.deegree.ogcwebservices.wms.capabilities.Style;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbench;
import org.kalypso.contribs.eclipse.jface.dialog.DialogSettingsUtils;
import org.kalypso.ogc.gml.IKalypsoLayerModell;
import org.kalypso.ogc.gml.wms.provider.images.IKalypsoImageProvider;
import org.kalypso.ui.ImageProvider;
import org.kalypso.ui.KalypsoAddLayerPlugin;
import org.kalypso.ui.KalypsoServiceConstants;
import org.kalypso.ui.action.AddThemeCommand;
import org.kalypso.ui.addlayer.internal.wms.ImportWmsWizardPage;
import org.kalypso.ui.i18n.Messages;
import org.kalypso.ui.wizard.AbstractDataImportWizard;

/**
 * Wizard for importing WMS sources.
 *
 * @author Kuepferle (original)
 * @author Holger Albert
 */
public class ImportWmsSourceWizard extends AbstractDataImportWizard
{
  /**
   * This constant stores the id for the dialog settings of this page.
   */
  private static String IMPORT_WMS_WIZARD = "IMPORT_WMS_WIZARD"; //$NON-NLS-1$

  /**
   * The page needed for import WMS sources.
   */
  private ImportWmsWizardPage m_page;

  private final List<String> m_catalog = new ArrayList<String>();

  public ImportWmsSourceWizard( )
  {
    setNeedsProgressMonitor( true );

    /* Get the dialog settings. */
    final IDialogSettings dialogSettings = getDialogSettings();

    /* If not available, add a section inside the settings of the plugin. */
    if( dialogSettings == null )
    {
      final IDialogSettings settings = DialogSettingsUtils.getDialogSettings( KalypsoAddLayerPlugin.getDefault(), IMPORT_WMS_WIZARD );
      setDialogSettings( settings );
    }
  }

  @Override
  public void init( final IWorkbench workbench, final IStructuredSelection selection )
  {
    try (final InputStream is = getClass().getResourceAsStream( "resources/kalypsoOWS.catalog" )) //$NON-NLS-1$
    {
      // read service catalog file
      readCatalog( is );
    }
    catch( final IOException e )
    {
      e.printStackTrace();

      m_catalog.clear();
    }
  }

  @Override
  public void addPages( )
  {
    m_page = new ImportWmsWizardPage( "WmsImportPage", Messages.getString( "org.kalypso.ui.wizard.wms.ImportWmsSourceWizard.0" ), ImageProvider.IMAGE_UTIL_UPLOAD_WIZ ); //$NON-NLS-1$ //$NON-NLS-2$
    addPage( m_page );
  }

  private void readCatalog( final InputStream is ) throws IOException
  {
    m_catalog.clear();

    // use properties to parse catalog: dont do everything yourself
    // fixes bug with '=' inside of URLs
    final Properties properties = new Properties();
    properties.load( is );

    final Set<Entry<Object, Object>> name = properties.entrySet();
    for( final Entry<Object, Object> entry : name )
    {
      if( entry.getKey().toString().startsWith( KalypsoServiceConstants.WMS_LINK_TYPE ) )
        m_catalog.add( entry.getValue().toString() );
    }
  }

  @Override
  public boolean performFinish( )
  {
    /* Finishes the work on this page (dialog settings). */
    m_page.finish();

    final IKalypsoLayerModell mapModell = getMapModel();
    if( mapModell == null )
      return false;

    try
    {
      final boolean isMulti = m_page.isMultiLayer();
      if( isMulti )
      {
        final StringBuffer source = new StringBuffer( IKalypsoImageProvider.KEY_URL + "=" + m_page.getBaseURL().toString() ); //$NON-NLS-1$
        final StringBuffer layers = new StringBuffer( IKalypsoImageProvider.KEY_LAYERS + "=" ); //$NON-NLS-1$
        final StringBuffer styles = new StringBuffer( IKalypsoImageProvider.KEY_STYLES + "=" ); //$NON-NLS-1$
        final StringBuffer provider = new StringBuffer( IKalypsoImageProvider.KEY_PROVIDER + "=" ); //$NON-NLS-1$

        final Layer[] layerArray = m_page.getLayersList();
        for( int i = 0; i < layerArray.length; i++ )
        {
          final Layer layer = layerArray[i];
          final String layerName = layer.getName();
          String styleName;
          final Style[] styles2 = layer.getStyles();
          if( styles2.length > 0 )
            styleName = styles2[0].getName();
          else
            styleName = "default"; //$NON-NLS-1$
          layers.append( layerName );
          styles.append( styleName );
          if( i < layerArray.length - 1 )
          {
            layers.append( "," ); //$NON-NLS-1$
            styles.append( "," ); //$NON-NLS-1$
          }
        }

        final String providerID = m_page.getProviderID();
        if( providerID != null )
          provider.append( providerID );

        // FIXME: should use label from capabilities instead of this ugly layerName

        final String layerName = "Multi" + source; //$NON-NLS-1$
        source.append( "#" ).append( layers.toString() ); //$NON-NLS-1$
        source.append( "#" ).append( styles.toString() ); //$NON-NLS-1$
        source.append( "#" ).append( provider.toString() ); //$NON-NLS-1$

        final AddThemeCommand command = new AddThemeCommand( mapModell, layerName, "wms", source.toString() ); //$NON-NLS-1$
        postCommand( command, null );
      }
      else
      {
        final Layer[] layerArray = m_page.getLayersList();
        for( final Layer layer : layerArray )
        {
          final StringBuffer source = new StringBuffer( IKalypsoImageProvider.KEY_URL + "=" + m_page.getBaseURL().toString() ); //$NON-NLS-1$

          final String layerName = layer.getName();
          String styleName;
          final Style[] styles2 = layer.getStyles();
          if( styles2.length > 0 )
            styleName = styles2[0].getName();
          else
            styleName = "default"; //$NON-NLS-1$

          String providerID = m_page.getProviderID();
          if( providerID == null )
            providerID = ""; //$NON-NLS-1$

          final String layerTitle = layer.getTitle();
          source.append( "#" ).append( IKalypsoImageProvider.KEY_LAYERS ).append( "=" ).append( layerName ); //$NON-NLS-1$ //$NON-NLS-2$
          source.append( "#" ).append( IKalypsoImageProvider.KEY_STYLES ).append( "=" ).append( styleName ); //$NON-NLS-1$ //$NON-NLS-2$
          source.append( "#" ).append( IKalypsoImageProvider.KEY_PROVIDER ).append( "=" ).append( providerID ); //$NON-NLS-1$ //$NON-NLS-2$

          final AddThemeCommand command = new AddThemeCommand( mapModell, layerTitle, "wms", source.toString() ); //$NON-NLS-1$
          postCommand( command, null );
        }
      }
    }
    catch( final Exception e )
    {
      e.printStackTrace();
    }

    return true;
  }
}