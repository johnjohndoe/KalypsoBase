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
import org.kalypso.commons.java.lang.Doubles;
import org.kalypso.contribs.eclipse.core.commands.HandlerUtils;
import org.kalypso.contribs.java.lang.NumberUtils;
import org.kalypso.observation.result.ComponentUtilities;
import org.kalypso.observation.result.IInterpolationHandler;
import org.kalypso.observation.result.IRecord;
import org.kalypso.observation.result.TupleResult;
import org.kalypso.ui.internal.i18n.Messages;

/**
 * @author kimwerner
 */
public class AddRowHandler extends AbstractHandler
{
  @Override
  public Object execute( final ExecutionEvent event ) throws ExecutionException
  {
    final Shell shell = HandlerUtil.getActiveShell( event );
    final String commandName = HandlerUtils.getCommandName( event );

    final TableViewer viewer = ToolbarCommandUtils.findTableViewer( event );
    final TupleResult tupleResult = ToolbarCommandUtils.findTupleResult( event );
    if( viewer == null || tupleResult == null )
      throw new ExecutionException( Messages.getString( "org.kalypso.ogc.gml.om.table.command.AddRowHandler.0" ) ); //$NON-NLS-1$

    final IStructuredSelection selection = (IStructuredSelection)viewer.getSelection();
    if( selection == null )
      throw new ExecutionException( Messages.getString( "org.kalypso.ogc.gml.om.table.command.AddRowHandler.1" ) ); //$NON-NLS-1$

    final Object obj = selection.getFirstElement();

    final IRecord newRecord = addRecord( shell, commandName, tupleResult, obj );
    if( newRecord == null )
      return null;

    /* select new row */
    // select the new row; in ui job, as table is also updated in an ui event
    final UIJob selectJob = new UIJob( "" ) //$NON-NLS-1$
    {
      @Override
      public IStatus runInUIThread( final IProgressMonitor monitor )
      {
        viewer.setSelection( new StructuredSelection( newRecord ) );
        return Status.OK_STATUS;
      }
    };

    selectJob.setSystem( true );
    // REMARK: delay is needed, because table will also have a delay for the refresh
    selectJob.schedule( 250 );

    return null;
  }

  private IRecord addRecord( final Shell shell, final String commandName, final TupleResult tupleResult, final Object selectedRow )
  {
    final int index = tupleResult.indexOf( selectedRow );

    if( index != -1 && index < tupleResult.size() - 1 )
      return addInterpolatedRecord( shell, commandName, tupleResult, index );
    else
    {
      return addRecordAtEnd( shell, commandName, tupleResult, index );
    }
  }

  private IRecord addInterpolatedRecord( final Shell shell, final String commandName, final TupleResult tupleResult, final int index )
  {
    final double faktor = askForFactor( shell, commandName, tupleResult, index );
    if( Double.isNaN( faktor ) )
      return null; // cancel

    final IRecord row = tupleResult.createRecord();
    final boolean success = tupleResult.doInterpolation( row, index, faktor );
    if( success )
    {
      tupleResult.add( index + 1, row );
      return row;
    }
    else
      return null;
  }

  private IRecord addRecordAtEnd( final Shell shell, final String commandName, final TupleResult tupleResult, final int index )
  {
    final int keyComponentIndex = getInterpolationComponentIndex( tupleResult );
    if( keyComponentIndex == -1 )
      return null;

    final String componentLabel = ComponentUtilities.getComponentLabel( tupleResult.getComponent( keyComponentIndex ) );

    final IRecord record = tupleResult.get( index );

    final double value = getNumberValue( record, keyComponentIndex );

    final double min = value;
    final double max = Double.MAX_VALUE;

    final double initialValue = min + 1.0;

    final double newWidth = askForDouble( shell, commandName, componentLabel, min, max, initialValue );
    if( Double.isNaN( newWidth ) )
      return null;

    final IRecord newRow = tupleResult.createRecord();

    /* copy values of last row */
    final IInterpolationHandler interpolationHandler = tupleResult.getInterpolationHandler();
    final String[] extrapolationsIDs = interpolationHandler.getExtrapolationsIDs();
    for( final String extrapolationID : extrapolationsIDs )
    {
      final int extrapolationIndex = tupleResult.indexOfComponent( extrapolationID );
      if( extrapolationIndex != -1 )
        newRow.setValue( extrapolationIndex, record.getValue( extrapolationIndex ) );
    }

    newRow.setValue( keyComponentIndex, newWidth );

    tupleResult.add( newRow );

    return newRow;
  }

  private double askForFactor( final Shell shell, final String title, final TupleResult tupleResult, final int index )
  {
    final int keyComponentIndex = getInterpolationComponentIndex( tupleResult );
    if( keyComponentIndex == -1 )
      return Double.NaN;

    final String componentLabel = ComponentUtilities.getComponentLabel( tupleResult.getComponent( keyComponentIndex ) );

    final IRecord record1 = tupleResult.get( index );
    final IRecord record2 = tupleResult.get( index + 1 );

    final double d1 = getNumberValue( record1, keyComponentIndex );
    final double d2 = getNumberValue( record2, keyComponentIndex );

    if( Doubles.isNaN( d1, d2 ) )
      return Double.NaN;

    final double min = Math.min( d1, d2 );
    final double max = Math.max( d1, d2 );

    final double initialValue = (max + min) / 2; //$NON-NLS-1$

    final double userInput = askForDouble( shell, title, componentLabel, min, max, initialValue );
    if( Double.isNaN( userInput ) )
      return Double.NaN;

    return (userInput - min) / (max - min);
  }

  private double getNumberValue( final IRecord record, final int indexOfComponent )
  {
    final Object value = record.getValue( indexOfComponent );

    if( value instanceof Number )
      return ((Number)value).doubleValue();

    return Double.NaN;
  }

  private int getInterpolationComponentIndex( final TupleResult tupleResult )
  {
    final IInterpolationHandler interpolationHandler = tupleResult.getInterpolationHandler();
    if( interpolationHandler == null )
      return -1;

    final String interpolationComponent = interpolationHandler.getInterpolationComponent();
    return tupleResult.indexOfComponent( interpolationComponent );
  }

  private double askForDouble( final Shell shell, final String title, final String componentLabel, final double min, final double max, final double initialValue )
  {
    final IInputValidator validator = new IInputValidator()
    {
      @Override
      public String isValid( final String newText )
      {
        final double newValue = NumberUtils.parseQuietDouble( newText );
        if( Double.isNaN( newValue ) )
          return Messages.getString( "AddRowHandler.1" ); //$NON-NLS-1$

        if( newValue <= min || newValue >= max )
          return String.format( Messages.getString( "AddRowHandler.2" ), min, max ); //$NON-NLS-1$

        return null;
      }
    };

    final String initialText = String.format( "%.4f", initialValue ); //$NON-NLS-1$

    final String dialogMessage = String.format( Messages.getString( "AddRowHandler.3" ), componentLabel );

    final InputDialog inputDialog = new InputDialog( shell, title, dialogMessage, initialText, validator ); //$NON-NLS-1$
    if( inputDialog.open() == Window.CANCEL )
      return Double.NaN;

    final String value = inputDialog.getValue();
    return NumberUtils.parseQuietDouble( value );
  }
}