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
package org.kalypso.ogc.gml.featureview.control.composite;

import java.util.List;

import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.kalypso.commons.i18n.ITranslator;
import org.kalypso.gmlschema.annotation.IAnnotation;
import org.kalypso.ogc.gml.featureview.control.FeatureComposite;
import org.kalypso.template.featureview.ControlType;
import org.kalypso.template.featureview.TabFolder;

/**
 * @author Gernot Belger
 */
public class TabFolderCompositionControl extends AbstractFeatureCompositionControl
{
  /**
   * These settings are used locally to remember the last selected tab-folder.<br/>
   * Static because we want to keep the currently selected tab over selection of features.
   */
  // TODO: should be more individual i.e. remeber selection for each different folder (but how to distinguish??)
  private final static IDialogSettings SETTINGS = new DialogSettings( "bla" ); //$NON-NLS-1$

  private static final String STR_SETTINGS_TAB = "tabIndex"; //$NON-NLS-1$

  private final TabFolder m_folderType;

  public TabFolderCompositionControl( final TabFolder folderType, final FeatureComposite featureComposite, final IAnnotation annotation, final ITranslator translator )
  {
    super( featureComposite, annotation, translator );

    m_folderType = folderType;
  }

  @Override
  public Control createControl( final FormToolkit toolkit, final Composite parent, final int style )
  {
    final org.eclipse.swt.widgets.TabFolder tabFolder = new org.eclipse.swt.widgets.TabFolder( parent, style );

    /* create the tab items */
    final List<org.kalypso.template.featureview.TabFolder.TabItem> tabItem = m_folderType.getTabItem();
    for( final org.kalypso.template.featureview.TabFolder.TabItem tabItemType : tabItem )
      createItem( tabFolder, tabItemType );

    /* restore previous selected tab */
    final String selectedTabStr = SETTINGS.get( STR_SETTINGS_TAB );
    final int selectedTab = selectedTabStr == null ? 0 : Integer.parseInt( selectedTabStr );
    if( selectedTab < tabFolder.getTabList().length )
    {
      tabFolder.setSelection( selectedTab );
    }

    tabFolder.addSelectionListener( new SelectionAdapter()
    {
      @Override
      public void widgetSelected( final SelectionEvent e )
      {
        handleFolderSelectionChanged( tabFolder.getSelectionIndex() );
      }
    } );

    return tabFolder;
  }

  protected void handleFolderSelectionChanged( final int selectionIndex )
  {
    SETTINGS.put( STR_SETTINGS_TAB, selectionIndex );
  }

  private void createItem( final org.eclipse.swt.widgets.TabFolder tabFolder, final org.kalypso.template.featureview.TabFolder.TabItem tabItemType )
  {
    final String label = tabItemType.getTabLabel();
    final String itemLabel = getAnnotation( IAnnotation.ANNO_LABEL, label );

    final ControlType control = tabItemType.getControl().getValue();

    final TabItem item = new TabItem( tabFolder, SWT.NONE );
    item.setText( translate( itemLabel ) );

    final Control tabControl = createControl( tabFolder, SWT.NONE, control );

    // ?? This seems to be breaking FeatureView's with observations. in this case control of parent will be used
    // FIXME: The parent if a TabItem MUST be the TabFolder! Everything else is just nonsense
    try
    {
      item.setControl( tabControl );
    }
    catch( final Exception e )
    {
      item.setControl( tabControl.getParent() );
    }
  }
}