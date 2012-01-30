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
package org.kalypso.model.wspm.ui.profil.wizard.landuse.runnables;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.PlatformUI;
import org.kalypso.commons.java.io.FileUtilities;
import org.kalypso.model.wspm.ui.i18n.Messages;
import org.kalypso.model.wspm.ui.profil.wizard.landuse.model.ILanduseModel;
import org.kalypso.model.wspm.ui.profil.wizard.landuse.model.LanduseProperties;
import org.kalypso.model.wspm.ui.profil.wizard.landuse.utils.ILanduseShapeDataProvider;

/**
 * @author Dirk Kuch
 */
public class ImportLanduseShapeRunnable implements IRunnableWithProgress
{
  private final ILanduseShapeDataProvider m_handler;

  private final ILanduseModel m_roughnessMapping;

  private final ILanduseModel m_vegetationMapping;

  public ImportLanduseShapeRunnable( final ILanduseShapeDataProvider handler, final ILanduseModel roughnessMapping, final ILanduseModel vegetationMapping )
  {
    m_handler = handler;
    m_roughnessMapping = roughnessMapping;
    m_vegetationMapping = vegetationMapping;
  }

  @Override
  public void run( final IProgressMonitor monitor ) throws InvocationTargetException
  {
    try
    {
      // copy shape file into landuse folder
      final IFolder landuse = getLanduseFolder( monitor );
      final String lnkShapeFile = FilenameUtils.removeExtension( m_handler.getLnkShapeFile() );

      final String baseName = importShapeFile( landuse, lnkShapeFile, monitor );
      writePropertyMappings( m_roughnessMapping, landuse, String.format( "%s.roughness.properties", baseName ), monitor ); // $NON-NLS-1$ //$NON-NLS-1$
      writePropertyMappings( m_vegetationMapping, landuse, String.format( "%s.vegetation.properties", baseName ), monitor ); // $NON-NLS-1$ //$NON-NLS-1$

      buildStyledLayerDescriptor( m_roughnessMapping, landuse, String.format( "%s.roughness.sld", baseName ), monitor ); //$NON-NLS-1$
      buildStyledLayerDescriptor( m_vegetationMapping, landuse, String.format( "%s.vegetation.sld", baseName ), monitor ); //$NON-NLS-1$
    }
    catch( final Exception e )
    {
      e.printStackTrace();
      throw new InvocationTargetException( e );
    }

  }

  private void buildStyledLayerDescriptor( final ILanduseModel mapping, final IFolder landuse, final String fileName, final IProgressMonitor monitor ) throws CoreException
  {
    final LanduseStyledLayerDescriptorBuilder builder = new LanduseStyledLayerDescriptorBuilder( mapping, landuse.getFile( fileName ) );
    builder.execute( monitor );
  }

  private void writePropertyMappings( final ILanduseModel mapping, final IFolder folder, final String fileName, final IProgressMonitor monitor ) throws IOException, CoreException
  {
    final IFile iProperties = folder.getFile( fileName );
    final FileOutputStream outputStream = new FileOutputStream( iProperties.getLocation().toFile() );
    try
    {
      final LanduseProperties properties = mapping.getMapping();
      properties.store( outputStream, null );
    }
    finally
    {
      outputStream.close();
      iProperties.refreshLocal( IResource.DEPTH_INFINITE, monitor );
    }
  }

  private String importShapeFile( final IFolder landuse, final String lnkShapeFile, final IProgressMonitor monitor ) throws CoreException
  {
    if( fileExists( landuse, lnkShapeFile ) )
    {
      final boolean overwrite = MessageDialog.openQuestion( PlatformUI.getWorkbench().getDisplay().getActiveShell(), Messages.getString( "ImportLanduseShapeRunnable.4" ), Messages.getString( "ImportLanduseShapeRunnable.5" ) ); //$NON-NLS-1$ //$NON-NLS-2$
      if( overwrite )
      {
        FileUtilities.copyShapeFileToDirectory( lnkShapeFile, landuse.getLocation().toFile() );
      }
      else
        throw new UnsupportedOperationException(); // TODO
    }
    else
      FileUtilities.copyShapeFileToDirectory( lnkShapeFile, landuse.getLocation().toFile() );

    landuse.refreshLocal( IResource.DEPTH_INFINITE, monitor );

    return FilenameUtils.getBaseName( lnkShapeFile );
  }

  private boolean fileExists( final IFolder landuse, final String lnkShapeFile ) throws CoreException
  {
    final ContainsFileNameVisitor visitor = new ContainsFileNameVisitor( landuse, lnkShapeFile, false );
    landuse.accept( visitor );

    return visitor.containsEqualFileName();
  }

  private IFolder getLanduseFolder( final IProgressMonitor monitor ) throws CoreException
  {
    final IProject project = m_handler.getProject();
    final IFolder dataFolder = project.getFolder( "data" ); // $NON-NLS-1$ //$NON-NLS-1$
    if( !dataFolder.exists() )
      dataFolder.create( true, true, monitor );

    final IFolder landuseFolder = dataFolder.getFolder( "landuse" ); // $NON-NLS-1$ //$NON-NLS-1$
    if( !landuseFolder.exists() )
      landuseFolder.create( true, true, monitor );

    return landuseFolder;
  }

}
