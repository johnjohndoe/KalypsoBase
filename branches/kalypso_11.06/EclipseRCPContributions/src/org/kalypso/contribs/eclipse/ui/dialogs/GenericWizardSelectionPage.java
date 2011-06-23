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
package org.kalypso.contribs.eclipse.ui.dialogs;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.dialogs.ImportExportPage;
import org.eclipse.ui.wizards.IWizardCategory;
import org.eclipse.ui.wizards.IWizardRegistry;
import org.kalypso.contribs.eclipse.jface.viewers.ViewerUtilities;

/**
 * @author Gernot Belger
 */
@SuppressWarnings("restriction")
public class GenericWizardSelectionPage extends ImportExportPage
{
  private static final String STORE_SELECTED_WIZARD_ID = "STORE_SELECTED_EXPORT_WIZARD_ID"; //$NON-NLS-1$

  private static final String STORE_EXPANDED_CATEGORIES = "STORE_EXPANDED_EXPORT_CATEGORIES"; //$NON-NLS-1$

  class MyCategorizedWizardSelectionTree extends CategorizedWizardSelectionTree
  {
    protected MyCategorizedWizardSelectionTree( final IWizardCategory categories, final String msg )
    {
      super( categories, msg );
    }

    @Override
    protected Composite createControl( final Composite parent )
    {
      return super.createControl( parent );
    }

    @Override
    protected TreeViewer getViewer( )
    {
      return super.getViewer();
    }
  }

  MyCategorizedWizardSelectionTree m_exportTree;

  private final IWizardRegistry m_wizardRegistry;

  private final String m_settingsName;

  private final String m_message;

  private final String m_description;

  private final GenericWizardFilter m_treeFilter = new GenericWizardFilter();

  public GenericWizardSelectionPage( final IWizardRegistry registry, final IStructuredSelection currentSelection, final String settingsName, final String message, final String description )
  {
    super( PlatformUI.getWorkbench(), currentSelection );

    m_settingsName = settingsName;
    m_message = message;
    m_description = description;

    m_wizardRegistry = registry;
  }

  public void setFilter( final IWizardFilter filter )
  {
    m_treeFilter.setFilter( filter );

    if( m_exportTree != null )
      ViewerUtilities.refresh( m_exportTree.getViewer(), true );
  }

  @Override
  protected Composite createTreeViewer( final Composite parent )
  {
    // FIXME: filter wizards...
    final IWizardCategory root = m_wizardRegistry.getRootCategory();

    m_exportTree = new MyCategorizedWizardSelectionTree( root, m_message );
    final Composite exportComp = m_exportTree.createControl( parent );
    final TreeViewer viewer = m_exportTree.getViewer();
    viewer.addSelectionChangedListener( new ISelectionChangedListener()
    {
      @SuppressWarnings("synthetic-access")
      @Override
      public void selectionChanged( final SelectionChangedEvent event )
      {
        listSelectionChanged( event.getSelection() );
      }
    } );
    viewer.addDoubleClickListener( new IDoubleClickListener()
    {
      @SuppressWarnings("synthetic-access")
      @Override
      public void doubleClick( final DoubleClickEvent event )
      {
        treeDoubleClicked( event );
      }
    } );

    viewer.addFilter( m_treeFilter );

    setTreeViewer( viewer );

    return exportComp;
  }

  @Override
  public void saveWidgetValues( )
  {
    storeExpandedCategories( m_settingsName + STORE_EXPANDED_CATEGORIES, m_exportTree.getViewer() );
    storeSelectedCategoryAndWizard( m_settingsName + STORE_SELECTED_WIZARD_ID, m_exportTree.getViewer() );
    super.saveWidgetValues();
  }

  @Override
  protected void restoreWidgetValues( )
  {
    final IWizardCategory exportRoot = m_wizardRegistry.getRootCategory();
    expandPreviouslyExpandedCategories( m_settingsName + STORE_EXPANDED_CATEGORIES, exportRoot, m_exportTree.getViewer() );
    selectPreviouslySelected( m_settingsName + STORE_SELECTED_WIZARD_ID, exportRoot, m_exportTree.getViewer() );
    super.restoreWidgetValues();
  }

  @Override
  protected void updateMessage( )
  {
    setMessage( m_description );
    super.updateMessage();
  }
}