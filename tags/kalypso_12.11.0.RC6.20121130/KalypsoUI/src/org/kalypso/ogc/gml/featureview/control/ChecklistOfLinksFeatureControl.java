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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.kalypso.commons.command.ICommand;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.ogc.gml.command.AddLinkCommand;
import org.kalypso.ogc.gml.command.CompositeCommand;
import org.kalypso.ogc.gml.command.RemoveMemberCommand;
import org.kalypso.ui.editor.gmleditor.part.GMLLabelProvider;
import org.kalypso.ui.internal.i18n.Messages;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureList;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree.model.feature.IXLinkedFeature;
import org.kalypsodeegree_impl.model.feature.FeatureLinkUtils;
import org.kalypsodeegree_impl.model.feature.search.IReferenceCollectorStrategy;

/**
 * Support the following parameters:
 * <ul>
 * <li>showSelectButtons : boolean - if true, 'selectAll' and 'deselecAll' buttons are shown
 * </ul>
 * 
 * @author Gernot Belger
 */
public class ChecklistOfLinksFeatureControl extends AbstractFeatureControl
{
  private final class ChangeCheckstateAction extends Action
  {
    private final boolean m_checkState;

    public ChangeCheckstateAction( final String text, final boolean checkState )
    {
      super( text );

      m_checkState = checkState;
    }

    @Override
    public void run( )
    {
      final IStructuredSelection selection = (IStructuredSelection)getViewer().getSelection();
      changeCheckState( selection.toArray(), m_checkState );
    }
  }

  private CheckboxTableViewer m_linkChecklist;

  public ChecklistOfLinksFeatureControl( final Feature feature, final IPropertyType ftp )
  {
    super( feature, ftp );
  }

  public StructuredViewer getViewer( )
  {
    return m_linkChecklist;
  }

  /**
   * @return Always <code>true</code>
   */
  @Override
  public boolean isValid( )
  {
    // can never be invalid
    return true;
  }

  @Override
  public Control createControl( final Composite parent, final int style )
  {
    m_linkChecklist = CheckboxTableViewer.newCheckList( parent, style | SWT.MULTI | SWT.FULL_SELECTION );
    m_linkChecklist.setContentProvider( new ArrayContentProvider() );
    m_linkChecklist.setLabelProvider( new GMLLabelProvider() );

    m_linkChecklist.addCheckStateListener( new ICheckStateListener()
    {
      @Override
      public void checkStateChanged( final CheckStateChangedEvent event )
      {
        final IXLinkedFeature[] elements = new IXLinkedFeature[] { (IXLinkedFeature)event.getElement() };
        handleCheckStateChanged( elements, event.getChecked() );
      }
    } );

    /* Configure context menu */
    final MenuManager manager = new MenuManager();
    manager.add( new ChangeCheckstateAction( Messages.getString( "ChecklistOfLinksFeatureControl.0" ), true ) ); //$NON-NLS-1$
    manager.add( new ChangeCheckstateAction( Messages.getString( "ChecklistOfLinksFeatureControl.1" ), false ) ); //$NON-NLS-1$

    final Table table = m_linkChecklist.getTable();
    table.setMenu( manager.createContextMenu( table ) );

    updateControl();

    return table;
  }

  protected void changeCheckState( final Object[] objects, final boolean check )
  {
    final Collection<IXLinkedFeature> toToggle = new ArrayList<>();

    for( final Object object : objects )
    {
      final boolean checked = m_linkChecklist.getChecked( object );
      if( checked != check )
      {
        toToggle.add( (IXLinkedFeature)object );
      }
    }

    final IXLinkedFeature[] elementsToCheck = toToggle.toArray( new IXLinkedFeature[toToggle.size()] );
    if( elementsToCheck.length > 0 )
    {
      handleCheckStateChanged( elementsToCheck, check );
    }
  }

  @Override
  public void updateControl( )
  {
    /* Set all referencable features as input */
    final Feature feature = getFeature();
    final IRelationType rt = (IRelationType)getFeatureTypeProperty();
    final GMLWorkspace workspace = feature.getWorkspace();

    final IReferenceCollectorStrategy strategy = ComboFeatureControl.createSearchStrategy( workspace, feature, rt );
    final IXLinkedFeature[] features = strategy.collectReferences();

    m_linkChecklist.setCheckedElements( new Object[0] );
    m_linkChecklist.setInput( features );

    /* check all currently set links */
    final FeatureList linkList = (FeatureList)feature.getProperty( rt );
    for( final Object object : linkList )
    {
      final IXLinkedFeature xLink = FeatureLinkUtils.asXLink( feature, rt, object );
      m_linkChecklist.setChecked( xLink, true );
    }
  }

  protected void handleCheckStateChanged( final IXLinkedFeature[] elementsToCheck, final boolean checked )
  {
    final Feature feature = getFeature();

    final ICommand changeCommand = createCommand( feature, elementsToCheck, checked );

    /* Fire the feature change. */
    fireFeatureChange( changeCommand );
  }

  private ICommand createCommand( final Feature feature, final IXLinkedFeature[] elementsToCheck, final boolean checked )
  {
    final IRelationType rt = (IRelationType)getFeatureTypeProperty();

    /* The composite command stores all commands, to be executed. */
    final CompositeCommand compositeCommand = new CompositeCommand( "Edit list of links" ); //$NON-NLS-1$

    /* Create the commands. */
    if( checked )
    {
      for( final IXLinkedFeature checkedElement : elementsToCheck )
      {
        final String href = checkedElement.getHref();
        compositeCommand.addCommand( new AddLinkCommand( feature, rt, -1, href ) );
      }
    }
    else
    {
      for( final IXLinkedFeature checkedElement : elementsToCheck )
        createRemoveCommand( compositeCommand, feature, rt, checkedElement );
    }

    return compositeCommand;
  }

  private void createRemoveCommand( final CompositeCommand compositeCommand, final Feature feature, final IRelationType rt, final IXLinkedFeature checkedElement )
  {
    final Feature targetFeature = checkedElement.getFeature();

    final FeatureList listOfLinks = (FeatureList)feature.getProperty( rt );

    for( final Object object : listOfLinks )
    {
      final IXLinkedFeature xlink = FeatureLinkUtils.asXLink( feature, rt, object );

      if( xlink == null )
      {
        // should never happen, null object in list
        continue;
      }

      // REMARK: we remove all elements that link to the checked element, i.e. multiple links in one list are not
      // supported
      final boolean sameOrLinksTo = FeatureLinkUtils.isSameOrLinkTo( targetFeature, object );
      if( sameOrLinksTo )
      {
        compositeCommand.addCommand( new RemoveMemberCommand( feature, rt, object ) );
      }
    }
  }
}