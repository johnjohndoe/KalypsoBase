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
package org.kalypso.model.wspm.ui.view.chart.layer.wsp.utils;

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.kalypso.commons.java.lang.Arrays;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.contribs.eclipse.ui.plugin.AbstractUIPluginExt;
import org.kalypso.model.wspm.ui.KalypsoModelWspmUIPlugin;
import org.kalypso.model.wspm.ui.i18n.Messages;
import org.kalypso.model.wspm.ui.view.chart.layer.wsp.IWspLayerData;
import org.kalypso.model.wspm.ui.view.chart.layer.wsp.WspLayer;

/**
 * @author Dirk Kuch
 */
public class WaterLevelResultTree extends Composite
{
  private CheckboxTreeViewer m_treeViewer;

  public WaterLevelResultTree( final Composite parent, final WspLayer layer, final FormToolkit toolkit )
  {
    super( parent, SWT.NULL );

    setLayout( GridLayoutFactory.fillDefaults().create() );
    render( layer, toolkit );
  }

  private void render( final WspLayer layer, final FormToolkit toolkit )
  {
    final IWspLayerData data = layer == null ? null : layer.getData();
    if( Objects.isNull( data ) )
    {
      toolkit.createLabel( this, Messages.getString( "org.kalypso.model.wspm.ui.view.chart.layer.WspLegendPopupDialog.2" ) ); //$NON-NLS-1$
      return;
    }

    try
    {
      final Object input = data.getInput();
      if( Objects.isNull( input ) )
      {
        toolkit.createLabel( this, Messages.getString( "org.kalypso.model.wspm.ui.view.chart.layer.WspLegendPopupDialog.3" ) ); //$NON-NLS-1$
        return;
      }

      m_treeViewer = new CheckboxTreeViewer( this, SWT.SINGLE );
      m_treeViewer.getTree().setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

      final ITreeContentProvider contentProvider = data.createContentProvider();
      m_treeViewer.setContentProvider( contentProvider );

      final ILabelProvider labelProvider = data.createLabelProvider();
      m_treeViewer.setLabelProvider( labelProvider );
      m_treeViewer.setSorter( new ViewerSorter() );

      m_treeViewer.setInput( input );
      m_treeViewer.expandToLevel( 2 );

      /* Get all active names. */
      final Object[] activeNames = data.getActiveElements();
      if( !Arrays.isEmpty( activeNames ) )
      {
        m_treeViewer.setCheckedElements( activeNames );
      }

      final CheckboxTreeViewer treeViewer = m_treeViewer;
      m_treeViewer.addCheckStateListener( new ICheckStateListener()
      {
        @Override
        public void checkStateChanged( final CheckStateChangedEvent event )
        {
          try
          {
            /* Get the source. */
            final CheckboxTreeViewer source = treeViewer;
            final Object[] checked = source.getCheckedElements();
            data.activateElements( checked );

            /* Refresh the layer. */
            layer.invalidate();
          }
          catch( final Exception ex )
          {
            KalypsoModelWspmUIPlugin.getDefault().getLog().log( new Status( IStatus.ERROR, AbstractUIPluginExt.ID, ex.getLocalizedMessage(), ex ) );
          }
        }
      } );
    }
    catch( final Exception e )
    {
      e.printStackTrace();

      toolkit.createLabel( this, Messages.getString( "org.kalypso.model.wspm.ui.view.chart.layer.WspLegendPopupDialog.6", e.getLocalizedMessage() ) ); //$NON-NLS-1$
    }
  }

  public void addFilter( final ViewerFilter filter )
  {
    if( m_treeViewer == null )
      return;

    m_treeViewer.addFilter( filter );

    /** update tree selection */
    synchronized( this )
    {
      final Set<Object> checked = new LinkedHashSet<>();

      final Object[] elemets = m_treeViewer.getCheckedElements();
      for( final Object element : elemets )
      {
        if( filter.select( m_treeViewer, element, element ) )
          checked.add( element );
      }

      m_treeViewer.setCheckedElements( checked.toArray() );
    }
  }
}
