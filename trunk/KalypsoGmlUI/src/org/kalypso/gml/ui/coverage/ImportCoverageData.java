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
package org.kalypso.gml.ui.coverage;

import java.io.File;
import java.net.URL;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.kalypso.commons.databinding.swt.FileAndHistoryData;
import org.kalypso.commons.java.util.AbstractModelObject;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.contribs.eclipse.jface.dialog.DialogSettingsUtils;
import org.kalypso.gml.ui.coverage.imports.CoverageFormats;
import org.kalypso.gml.ui.coverage.imports.ICoverageImporter;
import org.kalypsodeegree.KalypsoDeegreePlugin;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree_impl.gml.binding.commons.ICoverage;
import org.kalypsodeegree_impl.gml.binding.commons.ICoverageCollection;

/**
 * @author Gernot Belger
 */
public class ImportCoverageData extends AbstractModelObject
{
  static final String PROPERTY_SOURCE_FILES = "sourceFiles"; //$NON-NLS-1$

  static final String PROPERTY_DATA_FOLDER = "dataFolder"; //$NON-NLS-1$

  public static final String PROPERTY_SOURCE_SRS = "sourceSRS"; //$NON-NLS-1$

  private FileAndHistoryData m_sourceFile = new FileAndHistoryData( "sourceFiles" ); //$NON-NLS-1$

  private IContainer m_dataContainer;

  // TODO: maybe we should allow general feature collections that allow for substitutes of _Coverage
  private ICoverageCollection m_coverageContainer;

  private boolean m_allowUserChangeDataFolder;

  private ICoverage[] m_newCoverages;

  private String m_sourceSRS = KalypsoDeegreePlugin.getDefault().getCoordinateSystem();

  public void initFromSelection( final IStructuredSelection selection )
  {
    final Object firstElement = selection.getFirstElement();

    final ICoverageCollection cc = findCoverageCollection( firstElement );

    // Choose target folder
    final URL gmlContext = cc.getWorkspace().getContext();
    final IFile gmlFile = ResourceUtilities.findFileFromURL( gmlContext );
    final IContainer gmlFolder = gmlFile == null ? null : gmlFile.getParent();

    init( cc, gmlFolder, true );
  }

  private static ICoverageCollection findCoverageCollection( final Object firstElement )
  {
    if( firstElement instanceof Feature )
    {
      final Feature fate = (Feature)firstElement;
      return (ICoverageCollection)fate.getAdapter( ICoverageCollection.class );
    }

    return null;
  }

  /**
   * @param dataFolder
   *          The new data files get imported into this folder. If <code>null</code>, the user will be asked for the
   *          folder.
   * @param allowUserChangeDataFolder
   *          If <code>false</code>, the entry field for the grid folder is hidden Resets to <code>true</code>, if
   *          'gridFolder' is null..
   */
  public void init( final ICoverageCollection coverages, final IContainer dataContainer, final boolean allowUserChangeDataFolder )
  {
    m_coverageContainer = coverages;
    m_dataContainer = dataContainer;
    m_allowUserChangeDataFolder = dataContainer == null ? true : allowUserChangeDataFolder;
  }

  public void loadSettings( final IDialogSettings settings )
  {
    if( settings == null )
      return;

    m_sourceFile.init( settings );
    m_sourceSRS = DialogSettingsUtils.getString( settings, PROPERTY_SOURCE_SRS, m_sourceSRS );

    if( m_dataContainer == null )
    {
      // REMARK: only load data container from settings, if it was not explicitely set from outside.
      final String dataFolderPath = DialogSettingsUtils.getString( settings, PROPERTY_DATA_FOLDER, getDataContainerPath() );
      if( dataFolderPath != null )
        setDataContainerPath( dataFolderPath );
    }
  }

  public void storeSettings( final IDialogSettings settings )
  {
    if( settings == null )
      return;

    m_sourceFile.storeSettings( settings );
    settings.put( PROPERTY_SOURCE_SRS, m_sourceSRS );
    settings.put( PROPERTY_DATA_FOLDER, getDataContainerPath() );
  }

  public void setSourceFile( final FileAndHistoryData sourceFile )
  {
    m_sourceFile = sourceFile;
  }

  public FileAndHistoryData getSourceFile( )
  {
    return m_sourceFile;
  }

  public String getSourceSRS( )
  {
    return m_sourceSRS;
  }

  public void setSourceSRS( final String srs )
  {
    final String oldValue = m_sourceSRS;

    m_sourceSRS = srs;

    firePropertyChange( PROPERTY_SOURCE_SRS, oldValue, srs );
  }

  public IContainer getDataContainer( )
  {
    return m_dataContainer;
  }

  public String getDataContainerPath( )
  {
    if( m_dataContainer == null )
      return null;

    return m_dataContainer.getFullPath().toPortableString();
  }

  public ICoverageCollection getCoverageContainer( )
  {
    return m_coverageContainer;
  }

  public File[] getSelectedFiles( )
  {
    final File file = m_sourceFile.getFile();
    if( file == null )
      return new File[0];

    return new File[] { file };
  }

  public boolean isChangeDataFolderAllowed( )
  {
    return m_allowUserChangeDataFolder;
  }

  public void setNewCoverages( final ICoverage[] newCoverages )
  {
    m_newCoverages = newCoverages;
  }

  public void setDataContainerPath( final String path )
  {
    if( StringUtils.isBlank( path ) )
      m_dataContainer = null;
    else
      m_dataContainer = ResourceUtilities.findContainerFromPath( Path.fromOSString( path ) );
  }

  public ICoverage[] getNewCoverages( )
  {
    return m_newCoverages;
  }

  /**
   * Gets all underlying source files. Can be multiple files e.g. in case of Shape
   */
  public File[] getRealSourceFiles( final File sourceFile )
  {
    /* ask underlying importer implementation for real files */
    final ICoverageImporter importer = CoverageFormats.findImporter( sourceFile );
    return importer.getSourceFiles( sourceFile );
  }
}