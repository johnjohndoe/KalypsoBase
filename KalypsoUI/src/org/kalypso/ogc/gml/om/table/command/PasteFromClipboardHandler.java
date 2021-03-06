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
package org.kalypso.ogc.gml.om.table.command;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.StringTokenizer;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.kalypso.contribs.eclipse.core.runtime.IStatusCollector;
import org.kalypso.contribs.eclipse.core.runtime.StatusCollector;
import org.kalypso.core.status.StatusDialog;
import org.kalypso.observation.result.IRecord;
import org.kalypso.observation.result.TupleResult;
import org.kalypso.ogc.gml.om.table.LastLineContentProvider;
import org.kalypso.ogc.gml.om.table.LastLineLabelProvider;
import org.kalypso.ogc.gml.om.table.TupleResultContentProvider;
import org.kalypso.ogc.gml.om.table.handlers.IComponentUiHandler;
import org.kalypso.ui.KalypsoGisPlugin;
import org.kalypso.ui.internal.i18n.Messages;

/**
 * Pastes the contents of the clipboard to the TupleResult
 * 
 * @author Dejan Antanaskovic
 */
public class PasteFromClipboardHandler extends AbstractHandler
{
//  private final Collection<IFeatureChangeListener> m_changelisteners = new ArrayList<>();

  String trstring = null;

  TupleResultContentProvider resultContentProvider = null;

  TupleResult m_tupleResult;

  @Override
  public Object execute( final ExecutionEvent event )
  {
    final Shell shell = HandlerUtil.getActiveShell( event );

    try
    {
      trstring = (String)Toolkit.getDefaultToolkit().getSystemClipboard().getContents( this ).getTransferData( DataFlavor.stringFlavor );
      // if clipboard content is not text or that content is empty, pop error message
      if( trstring == null || trstring.trim().length() == 0 )
      {
        MessageDialog.openError( shell, Messages.getString( "org.kalypso.ogc.gml.om.table.command.PasteFromClipboardHandler.0" ), Messages.getString( "org.kalypso.ogc.gml.om.table.command.PasteFromClipboardHandler.1" ) ); //$NON-NLS-1$ //$NON-NLS-2$
        return null;
      }
    }
    catch( final Exception e )
    {
      MessageDialog.openError( shell, Messages.getString( "org.kalypso.ogc.gml.om.table.command.PasteFromClipboardHandler.0" ), Messages.getString( "org.kalypso.ogc.gml.om.table.command.PasteFromClipboardHandler.1" ) ); //$NON-NLS-1$ //$NON-NLS-2$
      return null;
    }

    m_tupleResult = ToolbarCommandUtils.findTupleResult( event );

    final TableViewer tupleResultViewer = ToolbarCommandUtils.findTableViewer( event );
    if( tupleResultViewer == null )
    {
      MessageDialog.openError( shell, Messages.getString( "org.kalypso.ogc.gml.om.table.command.PasteFromClipboardHandler.0" ), Messages.getString( "org.kalypso.ogc.gml.om.table.command.PasteFromClipboardHandler.5" ) ); //$NON-NLS-1$ //$NON-NLS-2$
      return null;
    }

    final IContentProvider contentProvider = tupleResultViewer.getContentProvider();
    if( contentProvider instanceof TupleResultContentProvider )
      resultContentProvider = (TupleResultContentProvider)contentProvider;
    else if( contentProvider instanceof LastLineContentProvider )
    {
      final IStructuredContentProvider wrappedProvider = ((LastLineContentProvider)contentProvider).getWrappedProvider();
      if( wrappedProvider instanceof TupleResultContentProvider )
        resultContentProvider = (TupleResultContentProvider)wrappedProvider;
    }

    if( resultContentProvider == null )
      return null;

    int firstIndex = m_tupleResult.size();
    final IStructuredSelection selection = (IStructuredSelection)tupleResultViewer.getSelection();
    if( !selection.isEmpty() )
    {
      final Object firstElement = selection.getFirstElement();
      firstIndex = m_tupleResult.indexOf( firstElement );
      if( selection.size() > 1 )
      {
        m_tupleResult.removeAll( selection.toList() );

        final int indexToSelect = Math.min( firstIndex, m_tupleResult.size() - 1 );
        if( indexToSelect != -1 )
          tupleResultViewer.setSelection( new StructuredSelection( m_tupleResult.get( indexToSelect ) ) );
      }

    }
    parseRecords( shell, firstIndex );

    return null;
  }

  IRecord[] parseRecords( final Shell shell, final int firstIndex )
  {
    final Collection<IRecord> records = new ArrayList<>();

    final IStatusCollector stati = new StatusCollector( KalypsoGisPlugin.getId() );

    final StringTokenizer st1 = new StringTokenizer( trstring, "\n" ); //$NON-NLS-1$

    int ordinalNumber = 1;

    while( st1.hasMoreTokens() )
    {
      final String line = st1.nextToken();
      if( line.startsWith( LastLineLabelProvider.DUMMY_ELEMENT_TEXT ) )
        break;
      final IRecord record = m_tupleResult.createRecord();
      IRecord filledRecord = setValuesToRecordFromParsedLine( ordinalNumber, line, stati, record );
      if( filledRecord != null )
      {
        records.add( record );
      }
      ordinalNumber++;
    }

    m_tupleResult.addAll( firstIndex, records );

    // TODO: move error handling out of this method
    if( stati.size() > 0 )
    {
      final IStatus[] array = stati.toArray( new IStatus[stati.size()] );
      final MultiStatus multiStatus = new MultiStatus( KalypsoGisPlugin.getId(), -1, array, Messages.getString( "org.kalypso.ogc.gml.om.table.command.PasteFromClipboardHandler.6" ), null ); //$NON-NLS-1$
      if( !multiStatus.isOK() )
      {
        final int open = new StatusDialog( shell, multiStatus, Messages.getString( "org.kalypso.ogc.gml.om.table.command.PasteFromClipboardHandler.7" ), new String[] { IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL }, 0 ).open(); //$NON-NLS-1$
        if( open != Window.OK )
          return null;
      }
    }

    return records.toArray( new IRecord[records.size()] );
  }

  private IRecord setValuesToRecordFromParsedLine( final int ordinalNumber, final String line, final IStatusCollector stati, final IRecord record )
  {
    /* parse values */
    final String[] tokens = StringUtils.split( line, '\t' );
    final Object[] values = new Object[tokens.length];
    final IStatus[] parseLog = new IStatus[tokens.length];

    int parseErrors = 0;

    for( int i = 0; i < tokens.length; i++ )
    {
      final String token = tokens[i];
      try
      {
        final IComponentUiHandler compHandler = resultContentProvider.getHandler( "" + i ); //$NON-NLS-1$
        if( compHandler != null )
        {
          values[i] = compHandler.parseValue( token );
          record.setValue( i, values[i], true );
        }
        else{
          parseErrors++;
        }
      }
      catch( final Exception e )
      {
        final String msg = Messages.getString( "org.kalypso.ogc.gml.om.table.command.PasteFromClipboardHandler.8", ordinalNumber, e.getLocalizedMessage() ); //$NON-NLS-1$
        parseLog[i] = new Status( IStatus.ERROR, KalypsoGisPlugin.PLUGIN_ID, msg, e );

        parseErrors++;
      }
    }

    /* ignore lines that cannot be parsed (e.g. header lines) */
//    if( parseErrors == tokens.length )
    if( parseErrors > 1 || tokens.length < m_tupleResult.getComponents().length - 1 )
    {
      stati.add( IStatus.INFO, "Line %d: skipped", null, ordinalNumber );
      return null;
    }

    /* add remaining errors to log */
    for( final IStatus element : parseLog )
    {
      if( element != null )
        stati.add( element );
    }

    return record;
  }

}
