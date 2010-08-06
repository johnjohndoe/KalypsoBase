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
package org.kalypso.ogc.gml.om.table.command;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.progress.UIJob;
import org.kalypso.contribs.eclipse.core.commands.HandlerUtils;
import org.kalypso.contribs.java.lang.NumberUtils;
import org.kalypso.i18n.Messages;
import org.kalypso.observation.result.IInterpolationHandler;
import org.kalypso.observation.result.IRecord;
import org.kalypso.observation.result.TupleResult;

/**
 * @author kimwerner
 */
public class AddRowHandler extends AbstractHandler
{
  /**
   * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
   */
  @Override
  public Object execute( final ExecutionEvent event ) throws ExecutionException
  {
    final Shell shell = HandlerUtil.getActiveShell( event );
    final String commandName = HandlerUtils.getCommandName( event );

    final TableViewer viewer = ToolbarCommandUtils.findTableViewer( event );
    final TupleResult tupleResult = ToolbarCommandUtils.findTupleResult( event );
    if( viewer == null || tupleResult == null )
      throw new ExecutionException( Messages.getString( "org.kalypso.ogc.gml.om.table.command.AddRowHandler.0" ) ); //$NON-NLS-1$

    final IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
    if( selection == null )
      throw new ExecutionException( Messages.getString( "org.kalypso.ogc.gml.om.table.command.AddRowHandler.1" ) ); //$NON-NLS-1$

    final Object obj = selection.getFirstElement();
    final int index = tupleResult.indexOf( obj );

    final IRecord row = tupleResult.createRecord();

    if( index != -1 && index < tupleResult.size() - 2 )
    {
      final double faktor = askForFactor( shell, commandName, tupleResult, index );
      if( Double.isNaN( faktor ) )
        return null; // cancel

      final boolean success = tupleResult.doInterpolation( tupleResult, row, index, faktor );
      if( success )
        tupleResult.add( index + 1, row );
    }
    else
      tupleResult.add( row );

    // select the new row; in ui job, as table is also updated in an ui event
    new UIJob( "" ) //$NON-NLS-1$
    {
      @Override
      public IStatus runInUIThread( final IProgressMonitor monitor )
      {
        viewer.setSelection( new StructuredSelection( row ) );
        return Status.OK_STATUS;
      }
    }.schedule();

    return null;
  }

  private double askForFactor( final Shell shell, final String title, final TupleResult tupleResult, final int index )
  {
    final IInterpolationHandler interpolationHandler = tupleResult.getInterpolationHandler();
    if( interpolationHandler == null )
      return 0.5; // whatever

    final String interpolationComponent = interpolationHandler.getInterpolationComponent();
    final int indexOfComponent = tupleResult.indexOfComponent( interpolationComponent );
    if( indexOfComponent == -1 )
      return 0.5;

    final IRecord record1 = tupleResult.get( index );
    final IRecord record2 = tupleResult.get( index + 1 );

    final Object value1 = record1.getValue( indexOfComponent );
    final Object value2 = record2.getValue( indexOfComponent );

    if( value1 instanceof Number && value2 instanceof Number )
    {
      final double d1 = ((Number) value1).doubleValue();
      final double d2 = ((Number) value2).doubleValue();
      final double min = Math.min( d1, d2 );
      final double max = Math.max( d1, d2 );

      final double userInput = askForDouble( shell, title, min, max );
      if( Double.isNaN( userInput ) )
        return Double.NaN;

      return (userInput - min) / (max - min);
    }

    return 0.5;
  }

  private double askForDouble( final Shell shell, final String title, final double min, final double max )
  {
    final String initialValue = String.format( "%.4f", (max + min) / 2 );
    final IInputValidator validator = new IInputValidator()
    {
      @Override
      public String isValid( final String newText )
      {
        final double newValue = NumberUtils.parseQuietDouble( newText );
        if( Double.isNaN( newValue ) )
          return "Please enter a decimal number";

        if( newValue <= min || newValue >= max )
          return String.format( "The new value must lie in the interval [%.4f, %.4f]", min, max );

        return null;
      }
    };

    final InputDialog inputDialog = new InputDialog( shell, title, "Pleaser enter where to interpolate:", initialValue, validator );
    if( inputDialog.open() == Window.CANCEL )
      return Double.NaN;

    final String value = inputDialog.getValue();
    return NumberUtils.parseQuietDouble( value );
  }

}
