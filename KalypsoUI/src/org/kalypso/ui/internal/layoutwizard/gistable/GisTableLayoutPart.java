/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
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
package org.kalypso.ui.internal.layoutwizard.gistable;

import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.kalypso.commons.arguments.Arguments;
import org.kalypso.commons.command.ICommand;
import org.kalypso.core.layoutwizard.ILayoutPageContext;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.ogc.gml.GisTemplateHelper;
import org.kalypso.ogc.gml.featureview.IFeatureChangeListener;
import org.kalypso.ogc.gml.selection.FeatureSelectionManager2;
import org.kalypso.ogc.gml.selection.IFeatureSelectionManager;
import org.kalypso.ogc.gml.table.ILayerTableInput;
import org.kalypso.ogc.gml.table.LayerTableViewer;
import org.kalypso.template.gistableview.Gistableview;
import org.kalypso.template.gistableview.Gistableview.Layer;
import org.kalypso.ui.KalypsoGisPlugin;
import org.kalypso.ui.internal.i18n.Messages;
import org.kalypso.ui.layoutwizard.AbstractWizardLayoutPart;
import org.kalypsodeegree.model.feature.Feature;

/**
 * @author Gernot Belger
 */
public class GisTableLayoutPart extends AbstractWizardLayoutPart
{
  /** Argument: Pfad auf Vorlage für die Gis-Tabelle (.gtt Datei) */
  private static final String PROP_TABLETEMPLATE = "tableTemplate"; //$NON-NLS-1$

  /**
   * Argument: Falls gesetzt, wird das Feature mit dieser ID selektiert, nachdem die Tabelle geladen wurde.
   */
  private static final String PROP_FEATURE_TO_SELECT_ID = "selectFeatureID"; //$NON-NLS-1$

  private LayerTableViewer m_gisTableViewer;

  private Gistableview m_gisTableview;

  private URL m_templateContext;

  public GisTableLayoutPart( final String id, final ILayoutPageContext context )
  {
    super( id, context );
  }

  @Override
  public void dispose( )
  {
  }

  /**
   * @see org.kalypso.hwv.ui.wizards.calculation.modelpages.layout.ILayoutPart#init()
   */
  @Override
  public void init( ) throws CoreException
  {
    final ILayoutPageContext context = getContext();
    final Arguments arguments = context.getArguments();

    final String templateFileName = getTemplateFileName( arguments );
    if( templateFileName == null )
    {
      final IStatus status = new Status( IStatus.ERROR, KalypsoGisPlugin.PLUGIN_ID, Messages.getString( "GisTableLayoutPart_2" ) ); //$NON-NLS-1$
      throw new CoreException( status );
    }

    try
    {
      final URL templateURL = context.resolveURI( templateFileName );
      if( templateURL == null )
      {
        final String msg = String.format( Messages.getString( "GisTableLayoutPart_3" ), templateFileName ); //$NON-NLS-1$
        final IStatus status = new Status( IStatus.ERROR, KalypsoGisPlugin.PLUGIN_ID, msg );
        throw new CoreException( status );
      }

      m_gisTableview = GisTemplateHelper.loadGisTableview( templateURL );
      m_templateContext = templateURL;
    }
    catch( final CoreException e )
    {
      throw e;
    }
    catch( final Exception e )
    {
      final String message = String.format( Messages.getString( "GisTableLayoutPart_4" ), templateFileName ); //$NON-NLS-1$
      final IStatus status = new Status( IStatus.ERROR, KalypsoGisPlugin.PLUGIN_ID, message, e );
      throw new CoreException( status );
    }
  }

  public static String getTemplateFileName( final Arguments arguments )
  {
    return (String)arguments.get( PROP_TABLETEMPLATE );
  }

  @Override
  public Control createControl( final Composite parent, final FormToolkit toolkit )
  {
    final ILayoutPageContext context = getContext();

    final IFeatureSelectionManager selectionManager = new FeatureSelectionManager2();

    final Arguments arguments = context.getArguments();

    final String selectFid = arguments.getProperty( PROP_FEATURE_TO_SELECT_ID, null );

    final IFeatureChangeListener fcl = new IFeatureChangeListener()
    {
      @Override
      public void featureChanged( final ICommand changeCommand )
      {
        // do nothing in wizard modus
      }

      @Override
      public void openFeatureRequested( final Feature feature, final IPropertyType ftp )
      {
        // do nothing in wizard modus
      }
    };
    m_gisTableViewer = new LayerTableViewer( parent, SWT.BORDER, context, KalypsoGisPlugin.getDefault().getFeatureTypeCellEditorFactory(), selectionManager, fcl );
    toolkit.adapt( m_gisTableViewer.getTable() );

    final Layer layer = m_gisTableview.getLayer();

    m_gisTableViewer.setInput( layer, context.getContext() );
    m_gisTableViewer.applyLayer( layer, m_templateContext );

    final ILayerTableInput input = m_gisTableViewer.getInput();
    // FIXME: comment, why is that needed??
    input.addFeaturesProviderListener( new GisTableLayoutPartFeatureListener( m_gisTableViewer, selectFid, getModificationProvider() ) );

    return m_gisTableViewer.getControl();
  }

  @Override
  public void saveData( final boolean doSaveGml ) throws CoreException
  {
    if( doSaveGml && m_gisTableViewer != null )
      m_gisTableViewer.saveData( new NullProgressMonitor() );
  }

  @Override
  public ISelectionProvider getSelectionProvider( )
  {
    return m_gisTableViewer;
  }
}