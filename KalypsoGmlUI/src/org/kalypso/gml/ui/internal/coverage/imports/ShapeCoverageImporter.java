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
package org.kalypso.gml.ui.internal.coverage.imports;

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.kalypso.contribs.java.io.FilePattern;
import org.kalypso.gml.processes.tin.ShapeTriangulatedSurfaceConverter;
import org.kalypso.gml.ui.i18n.Messages;
import org.kalypsodeegree.model.geometry.GM_TriangulatedSurface;

/**
 * Imports a .shp file as coverage.
 * 
 * @author Holger Albert
 */
public class ShapeCoverageImporter extends AbstractTriangulatedSurfaceCoverageImporter
{
  @Override
  public File[] getSourceFiles( final File sourceFile )
  {
    final File parent = sourceFile.getParentFile();

    final String shapeName = sourceFile.getName();
    final String baseName = FilenameUtils.removeExtension( shapeName );

    /* we accept all files with the same base name, so .xml and .prj files are copied as well. Should be ok for almost all cases */

    final String pattern = baseName + ".*"; //$NON-NLS-1$

    final WildcardFileFilter filter = new WildcardFileFilter( pattern );

    return parent.listFiles( (FilenameFilter)filter );
  }

  @Override
  public FilePattern getFilePattern( )
  {
    return new FilePattern( "*.shp", Messages.getString( "ShapeCoverageImporter_0" ) ); //$NON-NLS-1$ //$NON-NLS-2$
  }

  @Override
  protected GM_TriangulatedSurface readInputData( final File dataFile, final String crs, final IProgressMonitor monitor ) throws CoreException, MalformedURLException
  {
    final ShapeTriangulatedSurfaceConverter converter = new ShapeTriangulatedSurfaceConverter( crs );
    return converter.convert( dataFile.toURI().toURL(), monitor );
  }
}