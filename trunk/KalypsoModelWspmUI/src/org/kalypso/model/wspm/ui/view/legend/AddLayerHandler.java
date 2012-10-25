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
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ListDialog;
import org.eclipse.ui.handlers.HandlerUtil;
import org.kalypso.chart.ui.editor.commandhandler.ChartHandlerUtilities;
import org.kalypso.commons.java.lang.Arrays;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.model.wspm.core.gml.IProfileSelection;
import org.kalypso.model.wspm.core.profil.IProfile;
import org.kalypso.model.wspm.ui.KalypsoModelWspmUIExtensions;
import org.kalypso.model.wspm.ui.i18n.Messages;
import org.kalypso.model.wspm.ui.view.chart.IProfilLayerProvider;
import org.kalypso.model.wspm.ui.view.chart.LayerDescriptor;
import org.kalypso.model.wspm.ui.view.chart.ProfilChartModel;

import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.view.IChartComposite;

/**
 * @author kimwerner
 */
public class AddLayerHandler extends AbstractHandler
{
  @Override
  public final Object execute( final ExecutionEvent event ) throws ExecutionException
  {
    final IViewPart view = getView();
    if( Objects.isNull( view ) )
      return null;

    final Shell shell = HandlerUtil.getActiveShell( event );
    final IEvaluationContext context = (IEvaluationContext)event.getApplicationContext();

    final IChartComposite chartComposite = ChartHandlerUtilities.getChartChecked( context );

    final ProfilChartModel model = getProfileChartModel( chartComposite );
    if( model == null )
      return null;

    final IProfileSelection profileSelection = model.getProfileSelection();
    if( profileSelection == null )
      return null;

    final IProfile profil = profileSelection.getProfile();
    if( profil == null )
      return null;

    final IProfilLayerProvider layerProvider = KalypsoModelWspmUIExtensions.createProfilLayerProvider( profil.getType() );
    if( layerProvider == null )
      return null; // TODO: show error message

    final LayerDescriptor[] layerDescriptors = layerProvider.getAddableLayers( model );

    final ListDialog dialog = new ListDialog( shell );
    dialog.setAddCancelButton( true );
    dialog.setBlockOnOpen( true );
    dialog.setContentProvider( new ArrayContentProvider() );
    dialog.setLabelProvider( new LabelProvider()
    {
      @Override
      public final String getText( final Object element )
      {
        return ((LayerDescriptor)element).getLabel();
      }
    } );
    dialog.setInput( layerDescriptors );
    dialog.setMessage( Messages.getString( "org.kalypso.model.wspm.ui.view.legend.AddLayerHandler.0" ) ); //$NON-NLS-1$
    dialog.setTitle( view.getTitle() ); //$NON-NLS-1$

    dialog.open();

    final Object[] result = dialog.getResult();
    if( Arrays.isEmpty( result ) )
      return null;

    final LayerDescriptor layerToAdd = (LayerDescriptor)result[0];
    try
    {
      layerProvider.addLayerToProfile( shell, profil, layerToAdd.getId() );
    }
    catch( final Exception e )
    {
      // TODO: error handling here
      throw new ExecutionException( e.getLocalizedMessage() );
    }
    return null;
  }

  private ProfilChartModel getProfileChartModel( final IChartComposite chartComposite )
  {
    if( chartComposite == null )
      return null;

    final IChartModel model = chartComposite.getChartModel();
    if( model instanceof ProfilChartModel )
      return (ProfilChartModel)model;

    return null;
  }

  private IViewPart getView( )
  {
    final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
    if( Objects.isNull( page ) )
      return null;

    return page.findView( "org.kalypso.model.wspm.ui.view.chart.ChartView" ); //$NON-NLS-1$
  }
}