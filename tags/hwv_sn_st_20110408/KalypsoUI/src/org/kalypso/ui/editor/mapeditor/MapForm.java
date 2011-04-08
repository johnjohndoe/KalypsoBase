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
package org.kalypso.ui.editor.mapeditor;

import java.awt.event.ComponentListener;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.progress.UIJob;
import org.kalypso.commons.command.ICommandTarget;
import org.kalypso.contribs.eclipse.core.runtime.jobs.MutexRule;
import org.kalypso.contribs.eclipse.ui.forms.MessageUtilitites;
import org.kalypso.ogc.gml.map.IMapPanel;
import org.kalypso.ogc.gml.map.listeners.MapPanelAdapter;
import org.kalypso.ogc.gml.selection.IFeatureSelectionManager;

/**
 * A form that contains a {@link org.kalypso.ogc.gml.map.IMapPanel}. Should be used to display the map, in order to have
 * a common ui-behaviour.
 * 
 * @author Gernot Belger
 */
public class MapForm extends Form
{
  private final ISchedulingRule UI_JOB_MUTEXT = new MutexRule();

  private IMapPanel m_mapPanel;

  /**
   * Does nothing more than create an empty form containing a {@link FillLayout}'ed body. This form is suitable to be
   * filled next by {@link #createMapPanel(Form, ICommandTarget, IFeatureSelectionManager).
   */
  public static MapForm createMapForm( final Composite parent )
  {
    final FormToolkit formToolkit = new FormToolkit( parent.getDisplay() );
    final MapForm form = new MapForm( parent, SWT.NONE );
    formToolkit.adapt( form, false, false );
    form.setFont( JFaceResources.getHeaderFont() );
    form.setSeparatorVisible( true );
    final Composite body = form.getBody();
    body.setLayout( new FillLayout() );

    form.addDisposeListener( new DisposeListener()
    {
      @Override
      public void widgetDisposed( final DisposeEvent e )
      {
        formToolkit.dispose();
      }
    } );

    return form;
  }

  public MapForm( final Composite parent, final int style )
  {
    super( parent, style );
  }

  /**
   * Creates a {@link MapPanel} on this form. The body's layout must already have been set.<br>
   * The form message will be reflecting the status of the {@link MapPanel}.
   */
  public IMapPanel createMapPanel( final ICommandTarget commandTarget, final IFeatureSelectionManager selectionManager )
  {
    m_mapPanel = MapPartHelper.createMapPanel( getBody(), SWT.NONE, null, commandTarget, selectionManager );

    m_mapPanel.addMapPanelListener( new MapPanelAdapter()
    {
      /**
       * @see org.kalypso.ogc.gml.map.listeners.MapPanelAdapter#onStatusChanged(org.kalypso.ogc.gml.map.IMapPanel)
       */
      @Override
      public void onStatusChanged( final IMapPanel source )
      {
        final IStatus status = source.getStatus();
        setStatus( status );
      }
    } );

    return m_mapPanel;
  }

  /**
   * Set the status of the form and display it as message in the header.<br>
   * If called from outside, the status is only visible until the next status-change of the underlying map-panel.
   */
  public void setStatus( final IStatus status )
  {
    final IMapPanel source = m_mapPanel;

    final UIJob job = new UIJob( "Update status" ) //$NON-NLS-1$
    {
      @Override
      public IStatus runInUIThread( final IProgressMonitor monitor )
      {
        if( isDisposed() )
          return Status.OK_STATUS;

        final String oldMessage = getMessage();
        final boolean ok = status.isOK();
        if( ok )
          setMessage( null );
        else
          MessageUtilitites.setMessage( MapForm.this, status );
        // TODO: add hyperlink listener if sub-messages exist. Show these sub-messages on click
        // Same for tooltip support

        // If visibility of header changed, send resize event, else the map has not the right extent
        if( source instanceof ComponentListener && ((oldMessage == null && !ok) || (oldMessage != null && ok)) )
          ((ComponentListener) source).componentResized( null );

        return Status.OK_STATUS;
      }
    };
    job.setRule( UI_JOB_MUTEXT );
    job.setSystem( true );
    job.schedule();
  }

}
