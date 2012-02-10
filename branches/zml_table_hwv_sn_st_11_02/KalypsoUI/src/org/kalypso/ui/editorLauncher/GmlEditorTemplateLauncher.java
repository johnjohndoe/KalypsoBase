/*--------------- Kalypso-Header --------------------------------------------------------------------

 This file is part of kalypso.
 Copyright (C) 2004, 2005 by:

 Technical University Hamburg-Harburg (TUHH)
 Institute of River and coastal engineering
 Denickestr. 22
 21073 Hamburg, Germany
 http://www.tuhh.de/wb

 and

 Bjoernsen Consulting Engineers (BCE)
 Maria Trost 3
 56070 Koblenz, Germany
 http://www.bjoernsen.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 E-Mail:
 belger@bjoernsen.de
 schlienger@bjoernsen.de
 v.doemming@tuhh.de

 ---------------------------------------------------------------------------------------------------*/
package org.kalypso.ui.editorLauncher;

import java.io.IOException;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.kalypso.commons.bind.JaxbUtilities;
import org.kalypso.contribs.eclipse.core.resources.StringStorage;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.eclipse.ui.editorinput.StorageEditorInput;
import org.kalypso.template.gistreeview.Gistreeview;
import org.kalypso.template.types.LayerType;
import org.kalypso.template.types.LayerTypeUtilities;
import org.kalypso.template.types.ObjectFactory;

/**
 * Launcher, um ein GML im Baum (GmlEditor) anzusehen.
 * 
 * @author belger
 */
public class GmlEditorTemplateLauncher implements IDefaultTemplateLauncher
{
  /**
   * @see org.kalypso.ui.editorLauncher.IDefaultTemplateLauncher#getFilename()
   */
  @Override
  public String getFilename( )
  {
    return "<Standard Baumansicht>.gmv"; //$NON-NLS-1$
  }

  /**
   * @see org.kalypso.ui.editorLauncher.IDefaultTemplateLauncher#getEditor()
   */
  @Override
  public IEditorDescriptor getEditor( )
  {
    final IWorkbench workbench = PlatformUI.getWorkbench();
    final IEditorRegistry editorRegistry = workbench.getEditorRegistry();
    return editorRegistry.findEditor( "org.kalypso.ui.editor.GmlEditor" ); //$NON-NLS-1$
  }

  /**
   * @see org.kalypso.ui.editorLauncher.IDefaultTemplateLauncher#createInput(org.eclipse.core.resources.IFile)
   */
  @Override
  public IEditorInput createInput( final IFile file ) throws CoreException
  {
    return createInputForGml( file );
  }

  public static IStorageEditorInput createInputForGml( final IFile file ) throws CoreException
  {
    final org.kalypso.template.gistreeview.ObjectFactory gisViewFact = new org.kalypso.template.gistreeview.ObjectFactory();
    final JAXBContext jc = JaxbUtilities.createQuiet( org.kalypso.template.gistreeview.ObjectFactory.class );

    try
    {
      final ObjectFactory typesFac = new ObjectFactory();

      final LayerType type = typesFac.createLayerType();
      LayerTypeUtilities.initLayerType( type, file );

      final Gistreeview gistreeview = gisViewFact.createGistreeview();
      gistreeview.setInput( type );

      final Marshaller marshaller = JaxbUtilities.createMarshaller( jc );
      final StringWriter w = new StringWriter();
      marshaller.marshal( gistreeview, w );
      w.close();

      final String string = w.toString();

      // als StorageInput zurückgeben
      final String basename = FilenameUtils.removeExtension( file.getName() );
      final String gmvName = basename + ".gmv"; //$NON-NLS-1$
      final IFile gmvFile = file.getParent().getFile( new Path( gmvName ) );
      final IPath fullPath = gmvFile.getFullPath();

      return new StorageEditorInput( new StringStorage( string, fullPath ) );
    }
    catch( final JAXBException e )
    {
      throw new CoreException( StatusUtilities.statusFromThrowable( e ) );
    }
    catch( final IOException e )
    {
      throw new CoreException( StatusUtilities.statusFromThrowable( e ) );
    }
  }

}
