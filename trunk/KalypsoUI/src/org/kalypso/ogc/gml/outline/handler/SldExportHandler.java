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
package org.kalypso.ogc.gml.outline.handler;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISources;
import org.kalypso.contribs.java.xml.XMLUtilities;
import org.kalypso.ogc.gml.map.handlers.MapHandlerUtils;
import org.kalypso.ogc.gml.outline.nodes.IThemeNode;
import org.kalypsodeegree.xml.Marshallable;

/**
 * @author Gernot Belger
 */
public class SldExportHandler extends AbstractHandler
{
  /**
   * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
   */
  @Override
  public Object execute( final ExecutionEvent event ) throws ExecutionException
  {
    /* Get the context. */
    final IEvaluationContext context = (IEvaluationContext) event.getApplicationContext();
    final Shell shell = (Shell) context.getVariable( ISources.ACTIVE_SHELL_NAME );
    final IStructuredSelection selection = (IStructuredSelection) context.getVariable( ISources.ACTIVE_CURRENT_SELECTION_NAME );
    final Object firstElement = selection.getFirstElement();
    final Marshallable elementToExport = getMarshallable( firstElement );
    if( elementToExport == null )
    {
      final String msg = String.format( "Selection must adapt to '%s'", Marshallable.class.getName() );
      throw new ExecutionException( msg );
    }

    final String fileName = guessFileName( firstElement );
    final File file = askForFile( shell, fileName );
    if( file == null )
      return null;

    writeFile( file, elementToExport, "UTF-8" ); //$NON-NLS-1$
    return null;
  }

  private Marshallable getMarshallable( final Object element )
  {
    if( element instanceof Marshallable )
      return (Marshallable) element;

    if( element instanceof IAdaptable )
      return (Marshallable) ((IAdaptable) element).getAdapter( Marshallable.class );

    return null;
  }

  private File askForFile( final Shell shell, final String fileName )
  {
    /* Open file dialog and save the file */
    final String[] filterExtensions = new String[] { "*.sld" }; //$NON-NLS-1$
    final String[] filterNames = new String[] { "Styled Layer Descriptors (*.sld)" }; //$NON-NLS-1$
    final File file = MapHandlerUtils.showSaveFileDialog( shell, "SLD-Export", fileName, getClass().getName(), filterExtensions, filterNames ); //$NON-NLS-1$
    if( file == null )
      return null;

    return file;
  }

  private String guessFileName( final Object element )
  {
    if( element instanceof IThemeNode )
      return ((IThemeNode) element).getLabel();

    return null;
  }

  private void writeFile( final File file, final Marshallable element, final String encoding ) throws ExecutionException
  {
    OutputStream out = null;
    try
    {
      out = new BufferedOutputStream( new FileOutputStream( file ) );

      final String xmlHeader = XMLUtilities.createXMLHeader( encoding );
      IOUtils.write( xmlHeader, out, encoding );
      final String sldAsXML = element.exportAsXML();
      IOUtils.write( sldAsXML, out, encoding );

      out.close();
    }
    catch( final IOException e )
    {
      throw new ExecutionException( "Failed to write sld", e ); //$NON-NLS-1$
    }
    finally
    {
      IOUtils.closeQuietly( out );
    }
  }
}
