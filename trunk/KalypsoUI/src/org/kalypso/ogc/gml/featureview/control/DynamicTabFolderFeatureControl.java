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
package org.kalypso.ogc.gml.featureview.control;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolder2Listener;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.kalypso.commons.command.ICommand;
import org.kalypso.contribs.eclipse.jface.action.ActionHyperlink;
import org.kalypso.contribs.eclipse.jface.action.DelegateAction;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.ogc.gml.featureview.IFeatureChangeListener;
import org.kalypso.ogc.gml.mapmodel.CommandableWorkspace;
import org.kalypso.ui.ImageProvider;
import org.kalypso.ui.editor.actions.INewScope;
import org.kalypso.ui.editor.actions.NewScopeFactory;
import org.kalypso.ui.internal.i18n.Messages;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureList;
import org.kalypsodeegree.model.feature.GMLWorkspace;

/**
 * @author Gernot Belger
 */
public class DynamicTabFolderFeatureControl extends AbstractFeatureControl
{
  private static final String DATA_ADD_ITEM = "addItem"; //$NON-NLS-1$

  private static final Image ADD_IMAGE = ImageProvider.IMAGE_FEATURE_NEW.createImage();

  private CTabFolder m_tabFolder;

  private final IFeatureComposite m_parentComposite;

  private int m_currentSelection = -1;

  private final CTabFolder2Listener m_tabFolderListener = new CTabFolder2Adapter()
  {
    @Override
    public void close( final CTabFolderEvent event )
    {
      final boolean doClose = handleTabClosed( (CTabItem)event.item );
      event.doit = doClose;
    }
  };

  private final SelectionListener m_tabSelectionListener = new SelectionAdapter()
  {
    @Override
    public void widgetSelected( final SelectionEvent e )
    {
      e.doit = handleTabSelected( (CTabItem)e.item );
    }
  };

  private boolean m_showClose;

  private INewScope m_newScope;

  public DynamicTabFolderFeatureControl( final IFeatureComposite parentComposite, final Feature feature, final IRelationType rt )
  {
    super( feature, rt );

    m_parentComposite = parentComposite;
  }

  @Override
  public void dispose( )
  {
    destroyAllItems();

    super.dispose();
  }

  @Override
  public Control createControl( final FormToolkit toolkit, final Composite parent, final int style )
  {
    m_showClose = (style & SWT.CLOSE) != 0;

    m_tabFolder = new CTabFolder( parent, style & ~SWT.CLOSE );

    m_tabFolder.addCTabFolder2Listener( m_tabFolderListener );
    m_tabFolder.addSelectionListener( m_tabSelectionListener );

    m_tabFolder.setBorderVisible( true );
    m_tabFolder.setSimple( false );
    m_tabFolder.setMRUVisible( true );
    m_tabFolder.setUnselectedCloseVisible( true );

    if( toolkit == null )
      m_tabFolder.setSelectionBackground( m_tabFolder.getDisplay().getSystemColor( SWT.COLOR_DARK_GRAY ) );
    else
    {
      m_tabFolder.setSelectionBackground( toolkit.getColors().getColor( IFormColors.TB_BG ) );
      toolkit.adapt( m_tabFolder );
    }

    updateControl();
    return m_tabFolder;
  }

  @Override
  public boolean isValid( )
  {
    return true;
  }

  @Override
  public void updateControl( )
  {
    // REMARK: save current selection here before changing anything
    final int currentSelection = m_currentSelection;

    if( m_tabFolder == null || m_tabFolder.isDisposed() )
      return;

    final Feature feature = getFeature();
    if( feature == null )
    {
      destroyAllItems();
      return;
    }

    final GMLWorkspace workspace = feature.getWorkspace();
    final IPropertyType featureTypeProperty = getFeatureTypeProperty();
    final Object property = feature.getProperty( featureTypeProperty );
    if( !(property instanceof FeatureList) )
    {
      destroyAllItems();
      return;
    }

    final FeatureList featureList = (FeatureList)property;

    if( hasObsoleteItems( featureList ) )
    {
      // As soon as the list changed, we need to destroy everything, else
      // some artifacts remain.
      destroyAllItems();
    }
    else
      destroyAddItem();

    final CommandableWorkspace cmdWorkspace = new CommandableWorkspace( workspace )
    {
      @Override
      public void postCommand( final ICommand command ) throws Exception
      {
        DynamicTabFolderFeatureControl.this.fireFeatureChange( command );
      }
    };

    m_newScope = NewScopeFactory.createPropertyScope( featureList, cmdWorkspace, null );

    // Destroy or add
    int count = 0;
    for( final Iterator< ? > iterator = featureList.iterator(); iterator.hasNext(); )
    {
      final Object object = iterator.next();

      final FeatureTabItem featureItem = getFeatureItem( count );
      final Object tabObject = featureItem == null ? null : featureItem.getFeatureObject();

      if( object.equals( tabObject ) )
      {
        featureItem.updateControl();
      }
      else
      {
        final FeatureTabItem newFeatureItem = createItem( count, workspace, object );
        newFeatureItem.updateControl();
      }

      count++;
    }

    /* Add 'add' item, if adding is allowed */
    createAddItem();

    /* Select, else nothing is visible. Also keep old selection to avoid strange effects. */
    if( m_tabFolder.getItemCount() > 0 )
    {
      if( currentSelection < 0 )
        m_tabFolder.setSelection( 0 );
      else if( currentSelection < m_tabFolder.getItemCount() )
        m_tabFolder.setSelection( currentSelection );
    }
  }

  private void destroyAddItem( )
  {
    if( m_tabFolder.isDisposed() )
      return;

    final CTabItem[] items = m_tabFolder.getItems();
    for( final CTabItem tabItem : items )
    {
      if( tabItem.getData( DATA_ADD_ITEM ) != null )
      {
        final Control control = tabItem.getControl();
        tabItem.dispose();
        control.dispose();
      }
    }
  }

  private void createAddItem( )
  {
    // FIXME: also consider maxOccurency etc. here
    if( !m_showClose )
      return;

    final CTabItem addItem = new CTabItem( m_tabFolder, SWT.NONE );
    addItem.setData( DATA_ADD_ITEM, new Object() );
    addItem.setShowClose( false );
    addItem.setImage( ADD_IMAGE );

    /* Content */
    final Group tabContent = new Group( m_tabFolder, SWT.NONE );
    tabContent.setText( Messages.getString( "DynamicTabFolderFeatureControl_0" ) ); //$NON-NLS-1$
    GridLayoutFactory.swtDefaults().applyTo( tabContent );
    addItem.setControl( tabContent );

    final IAction[] actions = m_newScope.createActions();
    for( final IAction action : actions )
    {
      final IAction delegateAction = new DelegateAction( action )
      {
        @Override
        public void runWithEvent( final Event event )
        {
          super.runWithEvent( event );

          if( event.doit )
            setCurrentSelection( 0 );
        }
      };

      ActionHyperlink.createHyperlink( null, tabContent, SWT.NONE, delegateAction );
    }
  }

  protected void setCurrentSelection( final int i )
  {
    m_tabFolder.setSelection( i );
    m_currentSelection = i;
  }

  private FeatureTabItem getFeatureItem( final int index )
  {
    if( index < m_tabFolder.getItemCount() )
      return FeatureTabItem.get( m_tabFolder.getItem( index ) );

    return null;
  }

  private FeatureTabItem createItem( final int index, final GMLWorkspace workspace, final Object featureObject )
  {
    final CTabItem tabItem = new CTabItem( m_tabFolder, SWT.NONE, index );
    tabItem.setShowClose( m_showClose );

    final FeatureTabItem featureTabItem = new FeatureTabItem( tabItem, workspace, featureObject );

    /* Delegate any events to the next higher level */
    final IFeatureControl featureControl = featureTabItem.createFeatureConrol( m_parentComposite );
    featureControl.addChangeListener( new IFeatureChangeListener()
    {
      @Override
      public void featureChanged( final ICommand changeCommand )
      {
        fireFeatureChange( changeCommand );
      }

      @Override
      public void openFeatureRequested( final Feature featureToOpen, final IPropertyType ftpToOpen )
      {
        fireOpenFeatureRequested( featureToOpen, ftpToOpen );
      }
    } );

    return featureTabItem;
  }

  private boolean hasObsoleteItems( final FeatureList featureList )
  {
    final CTabItem[] items = m_tabFolder.getItems();

    final Collection<Object> objects = new ArrayList<>();

    for( final CTabItem tabItem : items )
    {
      if( tabItem.getData( DATA_ADD_ITEM ) == null )
      {
        final FeatureTabItem wrapper = FeatureTabItem.get( tabItem );
        final Object featureObject = wrapper.getFeatureObject();
        objects.add( featureObject );
      }
    }

    final Collection<Object> allFeatureItems = new ArrayList<>( featureList );

    return !allFeatureItems.equals( objects );
  }

  private void destroyAllItems( )
  {
    if( m_tabFolder.isDisposed() )
      return;

    final CTabItem[] items = m_tabFolder.getItems();
    for( final CTabItem tabItem : items )
    {
      final FeatureTabItem wrapper = FeatureTabItem.get( tabItem );
      if( wrapper != null )
        wrapper.destroy();
      else
      {
        final Control control = tabItem.getControl();
        tabItem.dispose();
        control.dispose();
      }
    }
  }

  protected boolean handleTabClosed( final CTabItem item )
  {
    final FeatureTabItem featureItem = FeatureTabItem.get( item );

    // ask user for deletion
    final String message = String.format( Messages.getString( "DynamicTabFolderFeatureControl_1" ), item.getText() ); //$NON-NLS-1$
    final boolean doDelete = MessageDialog.openConfirm( m_tabFolder.getShell(), Messages.getString( "DynamicTabFolderFeatureControl_2" ), message ); //$NON-NLS-1$
    if( !doDelete )
      return false;

    // delete feature
    final ICommand command = featureItem.deleteFeature();
    fireFeatureChange( command );

    return true;
  }

  protected boolean handleTabSelected( final CTabItem item )
  {
    m_currentSelection = m_tabFolder.indexOf( item );

    return true;
  }
}