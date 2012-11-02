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
package org.kalypso.ui.addlayer.dnd;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.dialogs.WizardCollectionElement;
import org.eclipse.ui.wizards.IWizardDescriptor;
import org.kalypso.commons.command.ICommandTarget;
import org.kalypso.contribs.eclipse.jface.dialog.DialogSettingsUtils;
import org.kalypso.core.status.StatusDialog;
import org.kalypso.ogc.gml.outline.GisMapOutlineDropData;
import org.kalypso.ui.KalypsoGisPlugin;
import org.kalypso.ui.addlayer.IKalypsoDataImportWizard;
import org.kalypso.ui.addlayer.MapExtensions;
import org.kalypso.ui.internal.i18n.Messages;

/**
 * Helper to dnd themes into a map.
 * 
 * @author Gernot Belger
 */
@SuppressWarnings( "restriction" )
public class AddLayerDndSupport
{
  private static final String WINDOW_TITLE = Messages.getString( "AddLayerDndSupport_0" ); //$NON-NLS-1$

  private final IMapDropTarget[] m_dropTargets;

  private final ICommandTarget m_commandTarget;

  public AddLayerDndSupport( final ICommandTarget commandTarget )
  {
    m_commandTarget = commandTarget;
    m_dropTargets = MapExtensions.getDropTargets();
  }

  public boolean validateDrop( final Object target, final int operation, final TransferData transferType )
  {
    if( !FileTransfer.getInstance().isSupportedType( transferType ) )
      return false;

    // TODO: support other transfer types

    return true;
  }

  public boolean performDrop( final Shell shell, final Object data, final GisMapOutlineDropData dropData )
  {
    final MapDropData[] themeData = translateData( data );
    if( ArrayUtils.isEmpty( themeData ) )
      return false;

    if( themeData.length > 1 )
    {
      MessageDialog.openWarning( shell, WINDOW_TITLE, Messages.getString( "AddLayerDndSupport_1" ) ); //$NON-NLS-1$
      return false;
    }

    final IMapDropTarget[] targets = findTargets( themeData[0] );
    if( targets.length == 0 )
    {
      MessageDialog.openWarning( shell, WINDOW_TITLE, Messages.getString( "AddLayerDndSupport_2" ) ); //$NON-NLS-1$
      return false;
    }

    try
    {
      // TODO: present user with choice dialog if more than one drop target was found
      return addTheme( shell, targets[0], themeData[0], dropData );
    }
    catch( final CoreException e )
    {
      StatusDialog.open( shell, e.getStatus(), WINDOW_TITLE );
      return false;
    }
  }

  private MapDropData[] translateData( final Object data )
  {
    if( data instanceof String[] )
    {
      final String[] paths = (String[])data;
      final MapDropData[] result = new MapDropData[paths.length];
      for( int i = 0; i < result.length; i++ )
        result[i] = new MapDropData( paths[i] );

      return result;
    }

    return null;
  }

  private IMapDropTarget[] findTargets( final MapDropData data )
  {
    final String path = data.getPath();

    final String extension = FilenameUtils.getExtension( path );

    final Collection<IMapDropTarget> targets = new ArrayList<>();

    for( final IMapDropTarget target : m_dropTargets )
    {
      if( target.supportsExtension( extension ) )
        targets.add( target );
    }

    return targets.toArray( new IMapDropTarget[targets.size()] );
  }

  private boolean addTheme( final Shell shell, final IMapDropTarget target, final MapDropData data, final GisMapOutlineDropData dropData ) throws CoreException
  {
    final String wizardId = target.getWizardId();

    final WizardCollectionElement wizards = MapExtensions.getAvailableWizards( null );
    final IWizardDescriptor wizardDescriptor = wizards.findWizard( wizardId );
    if( wizardDescriptor == null )
      throw new IllegalStateException( String.format( Messages.getString( "AddLayerDndSupport_3" ), wizardId ) ); //$NON-NLS-1$

    /* Create and initialize wizard */
    final IKalypsoDataImportWizard wizard = (IKalypsoDataImportWizard)wizardDescriptor.createWizard();
    wizard.setCommandTarget( m_commandTarget );
    wizard.setMapModel( dropData.getLayerModel(), dropData.getInsertionIndex() );

    if( wizard instanceof Wizard )
    {
      ((Wizard)wizard).setWindowTitle( WINDOW_TITLE );
      ((Wizard)wizard).setDialogSettings( DialogSettingsUtils.getDialogSettings( KalypsoGisPlugin.getDefault(), wizardId ) );
    }

    wizard.initFromDrop( data );

    /* Execute wizard */
    final WizardDialog dialog = new WizardDialog( shell, wizard );
    return dialog.open() == Window.OK;
  }
}