/** This file is part of Kalypso
 *
 *  Copyright (c) 2012 by
 *
 *  Björnsen Beratende Ingenieure GmbH, Koblenz, Germany (Bjoernsen Consulting Engineers), http://www.bjoernsen.de
 *  Technische Universität Hamburg-Harburg, Institut für Wasserbau, Hamburg, Germany
 *  (Technical University Hamburg-Harburg, Institute of River and Coastal Engineering), http://www.tu-harburg.de/wb/
 *
 *  Kalypso is free software: you can redistribute it and/or modify it under the terms
 *  of the GNU Lesser General Public License (LGPL) as published by the Free Software
 *  Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  Kalypso is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 *  warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with Kalypso.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kalypso.ui.wizard.gml;

import javax.xml.namespace.QName;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ViewerFilter;
import org.kalypso.commons.databinding.swt.FileAndHistoryData;
import org.kalypso.commons.java.util.AbstractModelObject;
import org.kalypso.ui.addlayer.internal.util.AddLayerUtils;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree_impl.model.feature.gmlxpath.GMLXPath;

/**
 * @author Gernot Belger
 */
public class GmlFileImportData extends AbstractModelObject
{
  public static final String PROPERTY_SELECTION = "selectedElement"; //$NON-NLS-1$

  private static final String PROPERTY_GML_FILE = "gmlFile"; //$NON-NLS-1$

  public static final String PROPERTY_WORKSPACE = "workspace"; //$NON-NLS-1$

  private Object m_selectedElement;

  private GMLXPath m_rootPath;

  private IProject m_selectedProject;

  private ViewerFilter m_filter;

  private QName[] m_validQnames = new QName[0];

  private boolean m_validAllowFeature = true;

  private boolean m_validAllowFeatureAssociation = true;

  private final FileAndHistoryData m_gmlFile = new FileAndHistoryData( PROPERTY_GML_FILE );

  private GMLWorkspace m_workspace;

  public void init( final IDialogSettings settings )
  {
    m_gmlFile.init( settings );
  }

  public void storeSettings( final IDialogSettings settings )
  {
    m_gmlFile.storeSettings( settings );
  }

  public void setSelectedElement( final Object selectedElement )
  {
    final Object oldValue = m_selectedElement;

    m_selectedElement = selectedElement;

    firePropertyChange( PROPERTY_SELECTION, oldValue, selectedElement );
  }

  public Object getSelectedElement( )
  {
    return m_selectedElement;
  }

  public void setRootPath( final GMLXPath rootPath )
  {
    m_rootPath = rootPath;
  }

  public GMLXPath getRootPath( )
  {
    return m_rootPath;
  }

  /** If set, all selected elements must substitute one of the given qnames. */
  public void setValidQNames( final QName[] validQnames )
  {
    m_validQnames = validQnames;
  }

  /** Determines what kind of objects may be selected */
  public void setValidKind( final boolean allowFeature, final boolean allowFeatureAssociation )
  {
    m_validAllowFeature = allowFeature;
    m_validAllowFeatureAssociation = allowFeatureAssociation;
  }

  public QName[] getValidQnames( )
  {
    return m_validQnames;
  }

  public boolean isValidAllowFeature( )
  {
    return m_validAllowFeature;
  }

  public boolean isValidAllowFeatureAssociation( )
  {
    return m_validAllowFeatureAssociation;
  }

  public FileAndHistoryData getGmlFile( )
  {
    return m_gmlFile;
  }

  public void setWorkspace( final GMLWorkspace workspace )
  {
    final GMLWorkspace oldValue = m_workspace;

    m_workspace = workspace;

    firePropertyChange( PROPERTY_WORKSPACE, oldValue, workspace );

    if( oldValue != null )
      oldValue.dispose();

    if( workspace == null )
      setSelectedElement( null );
    else
      setSelectedElement( workspace.getRootFeature() );
  }

  public GMLWorkspace getWorkspace( )
  {
    return m_workspace;
  }

  public String getSourcePath( final IPath mapPath )
  {
    if( m_workspace == null )
      return null;

    final IPath path = m_gmlFile.getPath();

    return AddLayerUtils.makeRelativeOrProjectRelative( mapPath, path );
  }

  /** If set to non-<code>null</code>, only files from within this project may be selected. */
  public void setProjectSelection( final IProject project )
  {
    m_selectedProject = project;
  }

  public IProject getSelectedProject( )
  {
    return m_selectedProject;
  }

  public ViewerFilter getFilter( )
  {
    return m_filter;
  }

  public void setViewerFilter( final ViewerFilter filter )
  {
    m_filter = filter;
  }
}