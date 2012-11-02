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
package org.kalypso.ui.editor.gistableeditor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPartSite;
import org.kalypso.commons.command.DefaultCommandManager;
import org.kalypso.commons.command.ICommand;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.ogc.gml.featureview.IFeatureChangeListener;
import org.kalypso.ogc.gml.table.LayerTableViewer;
import org.kalypso.ui.editor.AbstractWorkbenchPart;
import org.kalypso.util.command.JobExclusiveCommandTarget;
import org.kalypsodeegree.model.feature.Feature;

/**
 * {@link org.eclipse.ui.IViewPart} implementation that shows a .gft file.
 * 
 * @author Gernot Belger
 */
public class GttViewPart extends AbstractWorkbenchPart implements IViewPart
{
  public static final String ID = "org.kalypso.ui.editor.gistableeditor.GttViewPart"; //$NON-NLS-1$

  private final JobExclusiveCommandTarget m_commandTarget = new JobExclusiveCommandTarget( new DefaultCommandManager(), null );

  private final IFeatureChangeListener m_fcl = new IFeatureChangeListener()
  {
    @Override
    public void featureChanged( final ICommand changeCommand )
    {
    }

    @Override
    public void openFeatureRequested( final Feature feature, final IPropertyType ftp )
    {
    }
  };

  private final GmlTablePartDelegate m_delegate = new GmlTablePartDelegate();

  @Override
  public IViewSite getViewSite( )
  {
    return (IViewSite)getSite();
  }

  @Override
  public void init( final IViewSite site )
  {
    setSite( site );
  }

  @Override
  public void init( final IViewSite site, final IMemento memento )
  {
    init( site );
  }

  @Override
  public void saveState( final IMemento memento )
  {
    // do nothing
  }

  @Override
  public void createPartControl( final Composite parent )
  {
    final IWorkbenchPartSite site = getSite();

    m_delegate.createControl( parent, m_commandTarget, m_fcl, site );

    setSourceProvider( new GmlTableSourceProvider( site, getLayerTable() ) );

// final ISelectionProvider selectionProvider = m_delegate.getSelectionProvider();

// final MenuManager menuManager = m_delegate.getMenuManager();
// menuManager.setRemoveAllWhenShown( true );
// site.registerContextMenu( menuManager, selectionProvider );
  }

  @Override
  public void setFocus( )
  {
    final LayerTableViewer layerTable = m_delegate.getLayerTable();
    if( layerTable == null )
      return;

    final Control control = layerTable.getControl();
    if( control == null || control.isDisposed() )
      return;

    control.setFocus();
  }

  @Override
  public final void loadInternal( final IProgressMonitor monitor, final IStorageEditorInput input ) throws CoreException
  {
    m_delegate.load( input, monitor );
  }

  @Override
  protected void doSaveInternal( final IProgressMonitor monitor, final IFile file )
  {
    throw new UnsupportedOperationException();
  }

  public LayerTableViewer getLayerTable( )
  {
    return m_delegate.getLayerTable();
  }

// private ILayerTableInput getTableInput( )
// {
// return m_delegate.getTableInput();
// }
//
// private void saveData( final IProgressMonitor monitor ) throws CoreException
// {
// getLayerTable().saveData( monitor );
// }
//
// private IFeatureSelectionManager getSelectionManager( )
// {
// return getLayerTable().getSelectionManager();
// }
}