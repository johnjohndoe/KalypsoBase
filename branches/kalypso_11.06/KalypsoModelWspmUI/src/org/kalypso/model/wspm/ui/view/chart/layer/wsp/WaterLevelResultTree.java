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
package org.kalypso.model.wspm.ui.view.chart.layer.wsp;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.contribs.eclipse.swt.layout.LayoutHelper;
import org.kalypso.contribs.eclipse.ui.plugin.AbstractUIPluginExt;
import org.kalypso.model.wspm.ui.KalypsoModelWspmUIPlugin;
import org.kalypso.model.wspm.ui.i18n.Messages;

/**
 * @author Dirk Kuch
 */
public class WaterLevelResultTree extends Composite
{

  WaterLevelResultTree( final Composite parent, final WspLayer layer, final FormToolkit toolkit )
  {
    super( parent, SWT.NULL );

    setLayout( LayoutHelper.createGridLayout() );
    render( layer, toolkit );
  }

  private void render( final WspLayer layer, final FormToolkit toolkit )
  {
    final IWspLayerData data = layer.getData();
    if( Objects.isNull( data ) )
    {
      toolkit.createLabel( this, Messages.getString( "org.kalypso.model.wspm.ui.view.chart.layer.WspLegendPopupDialog.2" ) );
      return;
    }

    try
    {
      final Object input = data.getInput();
      if( Objects.isNull( input ) )
      {
        toolkit.createLabel( this, Messages.getString( "org.kalypso.model.wspm.ui.view.chart.layer.WspLegendPopupDialog.r" ) );
        return;
      }

      toolkit.createLabel( this, Messages.getString( "org.kalypso.model.wspm.ui.view.chart.layer.WspLegendPopupDialog.5" ) );

      final CheckboxTreeViewer treeViewer = new CheckboxTreeViewer( this, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL );
      treeViewer.getTree().setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

      final ITreeContentProvider contentProvider = data.createContentProvider();
      treeViewer.setContentProvider( contentProvider );

      final ILabelProvider labelProvider = data.createLabelProvider();
      treeViewer.setLabelProvider( labelProvider );

      treeViewer.setSorter( new ViewerSorter() );

      treeViewer.setInput( input );

      treeViewer.expandToLevel( 2 );

      /* Get all active names. */
      final Object[] activeNames = data.getActiveElements();
      if( activeNames != null )
      {
        treeViewer.setCheckedElements( activeNames );
      }

      treeViewer.addCheckStateListener( new ICheckStateListener()
      {
        @Override
        public void checkStateChanged( final CheckStateChangedEvent event )
        {
          try
          {
            /* Get the source. */
            final CheckboxTreeViewer source = treeViewer;

            /* Get the checked elements. */
            final Object[] checked = source.getCheckedElements();

            /* Activate the newly checked names. */
            data.activateElements( checked );

            /* Refresh the layer. */
            layer.getEventHandler().fireLayerContentChanged( layer );
          }
          catch( final Exception ex )
          {
            /* Log the error message. */
            KalypsoModelWspmUIPlugin.getDefault().getLog().log( new Status( IStatus.ERROR, AbstractUIPluginExt.ID, ex.getLocalizedMessage(), ex ) );
          }
        }
      } );

    }
    catch( final Exception e )
    {
      e.printStackTrace();

      toolkit.createLabel( this, Messages.getString( "org.kalypso.model.wspm.ui.view.chart.layer.WspLegendPopupDialog.6", e.getLocalizedMessage() ) );
    }

  }
}
