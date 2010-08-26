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
package org.kalypso.ogc.gml.map.widgets;

import java.awt.Point;
import java.net.URL;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;
import org.kalypso.commons.command.ICommandTarget;
import org.kalypso.grid.GeoGridUtilities;
import org.kalypso.grid.IGeoGrid;
import org.kalypso.i18n.Messages;
import org.kalypso.ogc.gml.IKalypsoFeatureTheme;
import org.kalypso.ogc.gml.IKalypsoTheme;
import org.kalypso.ogc.gml.map.IMapPanel;
import org.kalypso.ogc.gml.map.utilities.MapUtilities;
import org.kalypso.ogc.gml.mapmodel.CommandableWorkspace;
import org.kalypso.ogc.gml.outline.MapOutline;
import org.kalypsodeegree.KalypsoDeegreePlugin;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureList;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree_impl.gml.binding.commons.ICoverage;
import org.kalypsodeegree_impl.model.feature.FeatureHelper;
import org.kalypsodeegree_impl.model.geometry.JTSAdapter;

import com.vividsolutions.jts.geom.Coordinate;

public class LabViewWidget extends AbstractThemeInfoWidget
{
  private final ISelectionChangedListener m_selectionListener = new ISelectionChangedListener()
  {
    public void selectionChanged( final SelectionChangedEvent event )
    {
      handleSelectionChanged( event.getSelection() );
    }
  };

  private ISelectionProvider m_selectionProvider = null;

  private String m_labViewServerURLFormat = System.getProperty( "org.kalypso.LabViewServerURLFormat", "http://localhost/command.htm?password=klimawoche,value=%.4f" );

  private IKalypsoFeatureTheme m_theme;

  private double m_value = 0.0;

  public LabViewWidget( )
  {
    super( Messages.getString( "org.kalypso.ogc.gml.map.widgets.ThemeInfoWidget.0" ), Messages.getString( "org.kalypso.ogc.gml.map.widgets.ThemeInfoWidget.1" ) ); //$NON-NLS-1$ //$NON-NLS-2$

    setNoThemesTooltip( Messages.getString( "org.kalypso.ogc.gml.map.widgets.ThemeInfoWidget.2" ) ); //$NON-NLS-1$
  }

  public LabViewWidget( final String name, final String toolTip )
  {
    super( name, toolTip );
  }

  /**
   * @see org.kalypso.ogc.gml.map.widgets.AbstractWidget#activate(org.kalypso.commons.command.ICommandTarget,
   *      org.kalypso.ogc.gml.map.MapPanel)
   */
  @Override
  public void activate( final ICommandTarget commandPoster, final IMapPanel mapPanel )
  {
    super.activate( commandPoster, mapPanel );

    final IWorkbench workbench = PlatformUI.getWorkbench();
    final IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
    final IWorkbenchPage page = window.getActivePage();
    final MapOutline outlineView = (MapOutline) page.findView( MapOutline.ID );
    if( outlineView == null )
    {
      getMapPanel().setMessage( Messages.getString( "org.kalypso.ogc.gml.map.widgets.ThemeInfoWidget.3" ) ); //$NON-NLS-1$
      return;
    }

    final IMapPanel outlineMapPanel = outlineView.getMapPanel();
    if( outlineMapPanel != mapPanel )
    {
      getMapPanel().setMessage( Messages.getString( "org.kalypso.ogc.gml.map.widgets.ThemeInfoWidget.4" ) ); //$NON-NLS-1$
      return;
    }

    m_selectionProvider = outlineView.getSite().getSelectionProvider();
    m_selectionProvider.addSelectionChangedListener( m_selectionListener );

    handleSelectionChanged( m_selectionProvider.getSelection() );
  }

  /**
   * @see org.kalypso.ogc.gml.map.widgets.AbstractWidget#finish()
   */
  @Override
  public void finish( )
  {
    super.finish();

    if( m_selectionProvider != null )
    {
      m_selectionProvider.removeSelectionChangedListener( m_selectionListener );
      m_selectionProvider = null;
    }
  }

  /**
   * @see org.kalypso.ogc.gml.widgets.AbstractWidget#leftClicked(java.awt.Point)
   */
  @Override
  public void leftClicked( final Point p )
  {
    super.leftClicked( p );
    if( m_theme == null )
      return;
    final CommandableWorkspace workspace = m_theme.getWorkspace();
    final FeatureList featureList = m_theme.getFeatureList();
    if( featureList == null )
      return;
    final GM_Point location = MapUtilities.transform( getMapPanel(), p );
    final GM_Position pos = location.getPosition();

    final List< ? > coverages = featureList.query( pos, null );
    try
    {
      for( final Object object : coverages )
      {
        /* Search for the first grid which provides a value */
        final Feature feature = FeatureHelper.getFeature( workspace, object );
        final ICoverage coverage = (ICoverage) feature.getAdapter( ICoverage.class );
        final IGeoGrid grid = GeoGridUtilities.toGrid( coverage );
        final Coordinate crd = JTSAdapter.export( pos );
        final Coordinate gridCrd = GeoGridUtilities.transformCoordinate( grid, crd, KalypsoDeegreePlugin.getDefault().getCoordinateSystem() );

        m_value = GeoGridUtilities.getValueChecked( grid, gridCrd );
        if( Double.isNaN( m_value ) )
          return;
      }
    }
    catch( final Exception e )
    {
      return;
    }

    new UIJob( "LabView request" )
    {
      @Override
      public IStatus runInUIThread( final IProgressMonitor monitor )
      {
        try
        {
          final MessageDialog dialog = new MessageDialog( this.getDisplay().getActiveShell(), "Frage", null, String.format( "Sollen die Zylinder wirklich gefüllt werden (%.2f m)?", getValue() ), MessageDialog.QUESTION, new String[] {
              " Ja ", " Nein " }, 1 );
          if( dialog.open() == 0 )
          {
            final URL url = new URL( String.format( m_labViewServerURLFormat, getValue() ) );
            url.openStream();
          }
        }
        catch( final Exception e )
        {
        }
        return Status.OK_STATUS;
      }
    }.schedule( 100 );
  }

  private double getValue( )
  {
    return m_value;
  }

  protected void handleSelectionChanged( final ISelection selection )
  {
    m_theme = null;
    final IStructuredSelection sel = (IStructuredSelection) selection;
    final Object[] selectedElements = sel.toArray();
    for( final Object object : selectedElements )
    {
      if( object instanceof IKalypsoFeatureTheme )
      {
        m_theme = (IKalypsoFeatureTheme) object;
        break;
      }
    }
    if( m_theme != null )
      setThemes( new IKalypsoTheme[] { m_theme } );
    else
      setThemes( new IKalypsoTheme[0] );

    if( getMapPanel() != null )
      moved( getCurrentPoint() );
  }
}