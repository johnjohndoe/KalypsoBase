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
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.deegree.ogcwebservices.wms.capabilities.Layer;
import org.deegree.ogcwebservices.wms.capabilities.Style;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbench;
import org.kalypso.commons.command.ICommandTarget;
import org.kalypso.ogc.gml.IKalypsoLayerModell;
import org.kalypso.ogc.gml.wms.provider.images.AbstractDeegreeImageProvider;
import org.kalypso.ogc.gml.wms.provider.images.IKalypsoImageProvider;
import org.kalypso.ui.ImageProvider;
import org.kalypso.ui.KalypsoAddLayerPlugin;
import org.kalypso.ui.KalypsoServiceConstants;
import org.kalypso.ui.action.AddThemeCommand;
import org.kalypso.ui.i18n.Messages;
import org.kalypso.ui.wizard.IKalypsoDataImportWizard;
import org.kalypso.ui.wizard.wms.pages.ImportWmsWizardPage;

/**
 * Wizard for importing WMS sources.
 * 
 * @author Kuepferle (original)
 * @author Holger Albert
 */
public class ImportWmsSourceWizard extends Wizard implements IKalypsoDataImportWizard, IKalypsoImportWMSWizard
{
  /**
   * This constant stores the id for the dialog settings of this page.
   */
  private static String IMPORT_WMS_WIZARD = "IMPORT_WMS_WIZARD"; //$NON-NLS-1$

  /**
   * The page needed for import WMS sources.
   */
  private ImportWmsWizardPage m_page;

  /**
   * Command target.
   */
  private ICommandTarget m_outlineviewer;

  /**
   * Catalog.
   */
  private ArrayList<String> m_catalog;

  /**
   * Map modell.
   */
  private IKalypsoLayerModell m_modell;

  /**
   * The constructor.
   */
  public ImportWmsSourceWizard( )
  {
    m_page = null;
    m_outlineviewer = null;
    m_catalog = new ArrayList<String>();
    m_modell = null;

    /* Get the dialog settings. */
    IDialogSettings dialogSettings = getDialogSettings();

    /* If not available, add a section inside the settings of the plugin. */
    if( dialogSettings == null )
    {
      IDialogSettings settings = KalypsoAddLayerPlugin.getDefault().getDialogSettings();

      /* Cannot do anything, if even the plugin has no settings. */
      if( settings == null )
        return;

      /* If available, check, if there is a section from this wizard. */
      IDialogSettings section = settings.getSection( IMPORT_WMS_WIZARD );
      if( section == null )
      {
        /* There is none available, add a new one. */
        section = settings.addNewSection( IMPORT_WMS_WIZARD );
      }

      /* Finally set it. */
      setDialogSettings( section );
    }
  }

  /**
   * @see org.eclipse.jface.wizard.IWizard#performFinish()
   */
  @Override
  public boolean performFinish( )
  {
    /* Finishes the work on this page (dialog settings). */
    m_page.finish();

    IKalypsoLayerModell mapModell = m_modell;
    if( mapModell != null )
      try
      {
        boolean isMulti = m_page.isMultiLayer();
        if( isMulti )
        {
          StringBuffer source = new StringBuffer( IKalypsoImageProvider.KEY_URL + "=" + m_page.getBaseURL().toString() ); //$NON-NLS-1$
          StringBuffer layers = new StringBuffer( IKalypsoImageProvider.KEY_LAYERS + "=" ); //$NON-NLS-1$
          StringBuffer styles = new StringBuffer( IKalypsoImageProvider.KEY_STYLES + "=" ); //$NON-NLS-1$
          StringBuffer provider = new StringBuffer( IKalypsoImageProvider.KEY_PROVIDER + "=" ); //$NON-NLS-1$

          Layer[] layerArray = m_page.getLayersList();
          for( int i = 0; i < layerArray.length; i++ )
          {
            Layer layer = layerArray[i];
            String layerName = layer.getName();
            String styleName;
            Style[] styles2 = layer.getStyles();
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

          String providerID = m_page.getProviderID();
          if( providerID != null )
            provider.append( providerID );

          String layerName = "Multi" + source; //$NON-NLS-1$
          source.append( "#" ).append( layers.toString() ); //$NON-NLS-1$
          source.append( "#" ).append( styles.toString() ); //$NON-NLS-1$
          source.append( "#" ).append( provider.toString() ); //$NON-NLS-1$

          AddThemeCommand command = new AddThemeCommand( mapModell, layerName, "wms", null, source.toString() ); //$NON-NLS-1$
          m_outlineviewer.postCommand( command, null );
        }
        else
        {
          Layer[] layerArray = m_page.getLayersList();
          for( Layer layer : layerArray )
          {
            StringBuffer source = new StringBuffer( IKalypsoImageProvider.KEY_URL + "=" + m_page.getBaseURL().toString() ); //$NON-NLS-1$

            String layerName = layer.getName();
            String styleName;
            Style[] styles2 = layer.getStyles();
            if( styles2.length > 0 )
              styleName = styles2[0].getName();
            else
              styleName = "default"; //$NON-NLS-1$

            String providerID = m_page.getProviderID();
            if( providerID == null )
              providerID = ""; //$NON-NLS-1$

            String layerTitle = layer.getTitle();
            source.append( "#" ).append( AbstractDeegreeImageProvider.KEY_LAYERS ).append( "=" ).append( layerName ); //$NON-NLS-1$ //$NON-NLS-2$
            source.append( "#" ).append( AbstractDeegreeImageProvider.KEY_STYLES ).append( "=" ).append( styleName ); //$NON-NLS-1$ //$NON-NLS-2$
            source.append( "#" ).append( AbstractDeegreeImageProvider.KEY_PROVIDER ).append( "=" ).append( providerID ); //$NON-NLS-1$ //$NON-NLS-2$

            AddThemeCommand command = new AddThemeCommand( mapModell, layerTitle, "wms", null, source.toString() ); //$NON-NLS-1$
            m_outlineviewer.postCommand( command, null );
          }

        }
      }
      catch( Exception e )
      {
        e.printStackTrace();
      }
    return true;
  }

  /**
   * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench,
   *      org.eclipse.jface.viewers.IStructuredSelection)
   */
  @Override
  public void init( IWorkbench workbench, IStructuredSelection selection )
  {
    // read service catalog file
    InputStream is = getClass().getResourceAsStream( "resources/kalypsoOWS.catalog" ); //$NON-NLS-1$
    try
    {
      readCatalog( is );
    }
    catch( IOException e )
    {
      e.printStackTrace();

      m_catalog.clear();
    }
    finally
    {
      IOUtils.closeQuietly( is );
    }
  }

  @Override
  public void addPages( )
  {
    m_page = new ImportWmsWizardPage( "WmsImportPage", Messages.getString("org.kalypso.ui.wizard.wms.ImportWmsSourceWizard.0"), ImageProvider.IMAGE_UTIL_UPLOAD_WIZ ); //$NON-NLS-1$ //$NON-NLS-2$
    addPage( m_page );
  }

  /**
   * @see org.kalypso.ui.wizard.data.IKalypsoDataImportWizard#setOutlineViewer(org.kalypso.ogc.gml.outline.GisMapOutlineViewer)
   */
  @Override
  public void setCommandTarget( ICommandTarget commandTarget )
  {
    m_outlineviewer = commandTarget;
  }

  @Override
  public ArrayList<String> getCatalog( )
  {
    return m_catalog;
  }

  public void readCatalog( InputStream is ) throws IOException
  {
    m_catalog.clear();

    // use properties to parse catalog: dont do everything yourself
    // fixes bug with '=' inside of URLs
    Properties properties = new Properties();
    properties.load( is );

    Set<Entry<Object, Object>> name = properties.entrySet();
    for( Entry<Object, Object> entry : name )
    {
      if( entry.getKey().toString().startsWith( KalypsoServiceConstants.WMS_LINK_TYPE ) )
        m_catalog.add( entry.getValue().toString() );
    }
  }

  /**
   * @see org.eclipse.jface.wizard.IWizard#needsProgressMonitor()
   */
  @Override
  public boolean needsProgressMonitor( )
  {
    return true;
  }

  /**
   * @see org.kalypso.ui.wizard.IKalypsoDataImportWizard#setMapModel(org.kalypso.ogc.gml.IKalypsoLayerModell)
   */
  @Override
  public void setMapModel( IKalypsoLayerModell modell )
  {
    m_modell = modell;
  }
}