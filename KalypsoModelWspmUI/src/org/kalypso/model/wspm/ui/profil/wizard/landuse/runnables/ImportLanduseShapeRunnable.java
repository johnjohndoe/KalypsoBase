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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.kalypso.commons.java.io.FileUtilities;
import org.kalypso.contribs.eclipse.core.resources.FolderUtilities;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.model.wspm.ui.profil.wizard.landuse.model.ILanduseModel;
import org.kalypso.model.wspm.ui.profil.wizard.landuse.model.LanduseProperties;

/**
 * @author Dirk Kuch
 */
public class ImportLanduseShapeRunnable implements ICoreRunnableWithProgress
{
  private final ILanduseModel m_roughnessMapping;

  private final ILanduseModel m_vegetationMapping;

  private final IFolder m_landuseFolder;

  private final String m_lnkShapeFile;

  private final String m_shapeSRS;

  public ImportLanduseShapeRunnable( final IFolder landuseFolder, final String lnkShapeFile, final String shapeSRS, final ILanduseModel roughnessMapping, final ILanduseModel vegetationMapping )
  {
    m_landuseFolder = landuseFolder;
    m_lnkShapeFile = lnkShapeFile;
    m_shapeSRS = shapeSRS;
    m_roughnessMapping = roughnessMapping;
    m_vegetationMapping = vegetationMapping;
  }

  @Override
  public IStatus execute( final IProgressMonitor monitor ) throws InvocationTargetException
  {
    try
    {
      FolderUtilities.mkdirs( m_landuseFolder );

      // copy shape file into landuse folder
      final String baseName = importShapeFile( monitor );

      writePropertyMappings( m_roughnessMapping, String.format( "%s.roughness.properties", baseName ), monitor ); //$NON-NLS-1$ //$NON-NLS-1$
      writePropertyMappings( m_vegetationMapping, String.format( "%s.vegetation.properties", baseName ), monitor ); //$NON-NLS-1$ //$NON-NLS-1$

      buildStyledLayerDescriptor( m_roughnessMapping, String.format( "%s.roughness.sld", baseName ), monitor ); //$NON-NLS-1$
      buildStyledLayerDescriptor( m_vegetationMapping, String.format( "%s.vegetation.sld", baseName ), monitor ); //$NON-NLS-1$

      return Status.OK_STATUS;
    }
    catch( final OperationCanceledException e )
    {
      return Status.CANCEL_STATUS;
    }
    catch( final Exception e )
    {
      e.printStackTrace();
      throw new InvocationTargetException( e );
    }

  }

  private void buildStyledLayerDescriptor( final ILanduseModel mapping, final String fileName, final IProgressMonitor monitor ) throws CoreException
  {
    final LanduseStyledLayerDescriptorBuilder builder = new LanduseStyledLayerDescriptorBuilder( mapping, m_landuseFolder.getFile( fileName ) );
    builder.execute( monitor );
  }

  private void writePropertyMappings( final ILanduseModel mapping, final String fileName, final IProgressMonitor monitor ) throws IOException, CoreException
  {
    final IFile iProperties = m_landuseFolder.getFile( fileName );
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

  private String importShapeFile( final IProgressMonitor monitor ) throws CoreException, IOException
  {
    final File landuseDir = m_landuseFolder.getLocation().toFile();
    FileUtilities.copyShapeFileToDirectory( m_lnkShapeFile, landuseDir );

    // FIXME: write prj file as well!
    final String baseName = FilenameUtils.getBaseName( m_lnkShapeFile );
    final File prjFile = new File( landuseDir, baseName + ".prj" ); //$NON-NLS-1$

    FileUtils.writeStringToFile( prjFile, m_shapeSRS );

    m_landuseFolder.refreshLocal( IResource.DEPTH_INFINITE, monitor );

    return baseName;
  }
}