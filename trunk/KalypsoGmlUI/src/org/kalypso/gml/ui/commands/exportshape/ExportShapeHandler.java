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
package org.kalypso.gml.ui.commands.exportshape;

import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.kalypso.contribs.eclipse.core.commands.HandlerUtils;
import org.kalypso.contribs.eclipse.core.runtime.AdapterUtils;
import org.kalypso.contribs.eclipse.core.runtime.PluginUtilities;
import org.kalypso.contribs.eclipse.jface.wizard.WizardDialog2;
import org.kalypso.gml.ui.KalypsoGmlUIPlugin;
import org.kalypso.gml.ui.util.GenericFeatureSelection;
import org.kalypso.ogc.gml.IKalypsoTheme;
import org.kalypso.ogc.gml.selection.IFeatureSelection;

/**
 * Handler for shape-export command.<br>
 * Intended to be overwritten by specialised shape-exporters.
 * 
 * @author Gernot Belger
 */
public class ExportShapeHandler extends AbstractHandler implements IHandler
{
  /**
   * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
   */
  public Object execute( final ExecutionEvent event ) throws ExecutionException
  {
    final Shell shell = HandlerUtil.getActiveShellChecked( event );

    final String commandName = HandlerUtils.getCommandName( event );
    final String title = commandName;

    final ISelection selection = HandlerUtil.getCurrentSelectionChecked( event );
    final IFeatureSelection featureSelection = GenericFeatureSelection.create( selection, null );
    if( featureSelection == null || featureSelection.size() == 0 )
    {
      final String msg = "No features in selection. Please select features for export.";
      MessageDialog.openWarning( shell, title, msg );
      return null;
    }

    final String fileName = findFileName( selection );

    final Wizard wizard = createWizard( featureSelection, fileName );
    wizard.setWindowTitle( title );

    final IDialogSettings wizardSettings = PluginUtilities.getDialogSettings( KalypsoGmlUIPlugin.getDefault(), getClass().getName() );
    wizard.setDialogSettings( wizardSettings );

    final WizardDialog2 dialog = new WizardDialog2( shell, wizard );
    dialog.setRememberSize( true );
    dialog.open();

    return null;
  }

  protected Wizard createWizard( final IFeatureSelection featureSelection, final String fileName )
  {
    return new ExportShapeWizard( featureSelection, fileName );
  }

  protected String findFileName( final ISelection selection )
  {
    if( selection.isEmpty() || !(selection instanceof IStructuredSelection) )
      return null;

    final IStructuredSelection structSel = (IStructuredSelection) selection;
    for( final Iterator< ? > iterator = structSel.iterator(); iterator.hasNext(); )
    {
      final Object selectedElement = iterator.next();
      final IKalypsoTheme theme = AdapterUtils.getAdapter( selectedElement, IKalypsoTheme.class );
      if( theme != null )
        return theme.getLabel();

      // TODO: other special cases

      return selectedElement.toString();

      // TODO: more than one element selected?
    }

    return null;
  }
}