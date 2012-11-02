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
package org.kalypso.ui.views.map;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.menus.WorkbenchWindowControlContribution;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.eclipse.ui.partlistener.AdapterPartListener;
import org.kalypso.contribs.eclipse.ui.partlistener.EditorFirstAdapterFinder;
import org.kalypso.contribs.eclipse.ui.partlistener.IAdapterEater;
import org.kalypso.contribs.eclipse.ui.partlistener.IAdapterFinder;
import org.kalypso.ogc.gml.map.IMapPanel;
import org.kalypso.ogc.gml.map.listeners.IMapPanelListener;
import org.kalypso.ogc.gml.map.listeners.MapPanelAdapter;
import org.kalypso.ogc.gml.map.utilities.MapUtilities;
import org.kalypso.ui.internal.i18n.Messages;
import org.kalypsodeegree.model.geometry.GM_Envelope;

/**
 * This class dipslays a small bar which enables the user to change the scale.
 * 
 * @author Holger Albert
 */
public class MapScaleStatusLineItem extends WorkbenchWindowControlContribution implements IAdapterEater<IMapPanel>
{
  private final IMapPanelListener m_panelListener = new MapPanelAdapter()
  {
    @Override
    public void onExtentChanged( final IMapPanel source, final GM_Envelope oldExtent, final GM_Envelope newExtent )
    {
      doScheduleUpdateScale();
    }
  };

  private final IAdapterFinder<IMapPanel> m_closeFinder = new EditorFirstAdapterFinder<>();

  private final IAdapterFinder<IMapPanel> m_initFinder = m_closeFinder;

  private final AdapterPartListener<IMapPanel> m_adapterListener = new AdapterPartListener<>( IMapPanel.class, this, m_initFinder, m_closeFinder );

  private final UpdateItemJob m_updateScaleJob = new UpdateItemJob( "Updating scale box ...", this ); //$NON-NLS-1$

  /**
   * The main composite.
   */
  private Composite m_composite;

  private IMapPanel m_panel;

  /**
   * The text field for displaying and typing the scale.
   */
  private Text m_text;

  /**
   * The constructor.
   */
  public MapScaleStatusLineItem( )
  {
    m_updateScaleJob.setSystem( true );
  }

  @Override
  public void update( )
  {
    if( m_text == null || m_text.isDisposed() )
      return;

    final double mapScale = getCurrentScale();

    if( Double.isNaN( mapScale ) || Double.isInfinite( mapScale ) )
    {
      m_text.setText( Messages.getString( "MapScaleStatusLineItem.0" ) ); //$NON-NLS-1$
      m_text.setEnabled( false );
    }
    else
    {
      final BigDecimal bigScale = new BigDecimal( mapScale, new MathContext( 3, RoundingMode.HALF_UP ) );
      final String scaleString = bigScale.toPlainString();

      m_text.setText( scaleString );
      m_text.setEnabled( true );
    }
  }

  private double getCurrentScale( )
  {
    // thread safe
    final IMapPanel panel = m_panel;

    if( panel == null )
      return Double.NaN;

    return panel.getCurrentScale();
  }

  @Override
  protected Control createControl( final Composite parent )
  {
    /* The main composite */
    m_composite = new Composite( parent, SWT.NONE );
    m_composite.setLayout( GridLayoutFactory.fillDefaults().numColumns( 2 ).create() );

    /* Create the components. */

    /* Create the label. */
    final Label label = new Label( m_composite, SWT.NONE );
    label.setLayoutData( new GridData( SWT.END, SWT.CENTER, false, true ) );
    label.setText( Messages.getString( "org.kalypso.ui.views.map.MapScaleStatusLineItem.1" ) ); //$NON-NLS-1$

    /* Create the text. */
    m_text = new Text( m_composite, SWT.BORDER );
    final GridData gridData = new GridData( SWT.FILL, SWT.CENTER, true, true );
    gridData.widthHint = 75;
    m_text.setLayoutData( gridData );
    m_text.setText( "" ); //$NON-NLS-1$

    /* Add the selection listener. */
    m_text.addSelectionListener( new SelectionAdapter()
    {
      @Override
      public void widgetDefaultSelected( final SelectionEvent e )
      {
        final Text source = (Text)e.getSource();
        handleTextDefaultSelected( source );
      }
    } );

    /* Add a dispose listener. */
    m_composite.addDisposeListener( new DisposeListener()
    {
      @Override
      public void widgetDisposed( final DisposeEvent e )
      {
        handleTextDisposed();
      }
    } );

    /* Hook the adapter listener to the active page. */
    final IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
    if( activePage != null )
      m_adapterListener.init( activePage );

    return m_composite;
  }

  protected void handleTextDisposed( )
  {
    /* Remove the adapter listener from the active page. */
    m_adapterListener.dispose();
  }

  protected void handleTextDefaultSelected( final Text source )
  {
    try
    {
      /* Get the contained text. */
      final String text = source.getText();

      /* Parse the text. It must be a double. */
      final double scale = Double.parseDouble( text.replace( ",", "." ) ); //$NON-NLS-1$ //$NON-NLS-2$

      /* Set the map scale. */
      MapUtilities.setMapScale( m_panel, scale );
    }
    catch( final NumberFormatException ex )
    {
      /* Tell the user. */
      ErrorDialog.openError( source.getShell(), Messages.getString( "org.kalypso.ui.views.map.MapScaleStatusLineItem.5" ), Messages.getString( "org.kalypso.ui.views.map.MapScaleStatusLineItem.6" ), StatusUtilities.statusFromThrowable( ex ) ); //$NON-NLS-1$ //$NON-NLS-2$
    }
  }

  @Override
  public void setAdapter( final IWorkbenchPart part, final IMapPanel adapter )
  {
    if( !m_composite.isDisposed() )
      m_composite.setVisible( adapter != null );

    if( m_panel != null )
      m_panel.removeMapPanelListener( m_panelListener );

    m_panel = adapter;

    if( m_panel != null )
    {
      m_panel.addMapPanelListener( m_panelListener );

      doScheduleUpdateScale();
    }
  }

  @Override
  public void dispose( )
  {
    m_adapterListener.dispose();
    if( m_panel != null )
      m_panel.removeMapPanelListener( m_panelListener );

    super.dispose();
  }

  protected void doScheduleUpdateScale( )
  {
    m_updateScaleJob.cancel();

    if( m_text == null || m_text.isDisposed() )
      return;

    m_updateScaleJob.schedule( 50 );
  }
}