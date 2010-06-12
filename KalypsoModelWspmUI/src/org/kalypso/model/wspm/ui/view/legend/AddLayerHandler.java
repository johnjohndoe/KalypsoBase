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
package org.kalypso.model.wspm.ui.view.legend;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ListDialog;
import org.kalypso.chart.ui.IChartPart;
import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypso.model.wspm.ui.KalypsoModelWspmUIExtensions;
import org.kalypso.model.wspm.ui.view.chart.IProfilLayerProvider;
import org.kalypso.model.wspm.ui.view.chart.LayerDescriptor;
import org.kalypso.model.wspm.ui.view.chart.ProfilChartView;

/**
 * @author kimwerner
 */
public class AddLayerHandler extends AbstractHandler
{
  /**
   * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
   */
  @Override
  public final Object execute( final ExecutionEvent event ) throws ExecutionException
  {
    // find chartview

    final IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
    final IViewPart view = activePage == null ? null : activePage.findView( "org.kalypso.model.wspm.ui.view.chart.ChartView" ); //$NON-NLS-1$
    final Object chartPart = view == null ? null : view.getAdapter( IChartPart.class );
    final ProfilChartView chartView = (chartPart != null && chartPart instanceof ProfilChartView) ? (ProfilChartView) chartPart : null;
    final IProfil profil = chartView == null ? null : chartView.getProfil();
    if( profil == null )
    {
      return null;
    }

    final IProfilLayerProvider layerProvider = KalypsoModelWspmUIExtensions.createProfilLayerProvider( profil.getType() );
    if( layerProvider == null )
    {
      // TODO: show error message
      return null;
    }

    final LayerDescriptor[] layerDescriptors = layerProvider.getAddableLayers( chartView );

    final ListDialog dialog = new ListDialog( null );
    dialog.setAddCancelButton( true );
    dialog.setBlockOnOpen( true );
    dialog.setContentProvider( new ArrayContentProvider() );
    dialog.setLabelProvider( new LabelProvider()
    {
      @Override
      public final String getText( final Object element )
      {
        return ((LayerDescriptor) element).getLabel();
      }
    } );
    dialog.setInput( layerDescriptors );
    // FIXME: always set message and title to dialogs!
    dialog.setMessage( "" ); //$NON-NLS-1$
    dialog.setTitle( "" ); //$NON-NLS-1$

    dialog.open();

    final Object[] result = dialog.getResult();
    if( result == null || result.length != 1 )
      return null;

    final LayerDescriptor layerToAdd = (LayerDescriptor) result[0];
    try
    {
      layerProvider.addLayerToChart( chartView, layerToAdd.getId() );
    }
    catch( final Exception e )
    {
      // TODO: error handling here
      throw new ExecutionException( e.getLocalizedMessage() );
    }
    return null;
  }
}
