/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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
import java.util.List;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TableViewerEditor;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ControlEditor;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.kalypso.contribs.eclipse.jface.viewers.DefaultTableViewer;
import org.kalypso.contribs.eclipse.swt.SWTUtilities;
import org.kalypso.contribs.eclipse.swt.custom.ExcelTableCursor;
import org.kalypso.contribs.eclipse.swt.custom.ExcelTableCursor.ADVANCE_MODE;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.observation.IObservation;
import org.kalypso.observation.result.IComponent;
import org.kalypso.observation.result.IRecord;
import org.kalypso.observation.result.ITupleResultChangedListener;
import org.kalypso.observation.result.TupleResult;
import org.kalypso.ogc.gml.command.ChangeFeatureCommand;
import org.kalypso.ogc.gml.om.ObservationFeatureFactory;
import org.kalypso.ogc.gml.om.table.TupleResultCellModifier;
import org.kalypso.ogc.gml.om.table.TupleResultContentProvider;
import org.kalypso.ogc.gml.om.table.TupleResultLabelProvider;
import org.kalypso.ogc.gml.om.table.handlers.IComponentUiHandlerProvider;
import org.kalypso.template.featureview.ColumnDescriptor;
import org.kalypso.template.featureview.Toolbar;
import org.kalypso.template.featureview.Toolbar.MenuContribution;
import org.kalypso.ui.KalypsoUIExtensions;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree_impl.model.feature.FeatureHelper;

/**
 * @author Gernot Belger
 */
public class TupleResultFeatureControl extends AbstractToolbarFeatureControl implements ITupleResultChangedListener
{
  private final List<ModifyListener> m_listener = new ArrayList<>( 10 );

  private final IComponentUiHandlerProvider m_handlerProvider;

  private DefaultTableViewer m_viewer;

  private ViewerFilter m_viewerFilter;

  private TupleResultContentProvider m_tupleResultContentProvider;

  private TupleResultLabelProvider m_tupleResultLabelProvider;

  private TupleResult m_tupleResult;

  /** TRICK: in order to suppress refresh after our own changes we set this flag. */
  private int m_ignoreNextUpdateControl = 0;

  private ExcelTableCursor m_cursor;

  private ControlEditor m_controlEditor;

  public TupleResultFeatureControl( final Feature feature, final IPropertyType ftp, final IComponentUiHandlerProvider handlerProvider, final boolean showToolbar )
  {
    super( feature, ftp, showToolbar, SWT.HORIZONTAL | SWT.FLAT );

    m_handlerProvider = handlerProvider;
  }

  @Override
  public Control createControl( final Composite parent, final int style )
  {
    final Composite composite = new Composite( parent, SWT.NONE );
    GridLayoutFactory.fillDefaults().spacing( 0, 0 ).applyTo( composite );

    if( getToolbarManager() != null )
      getToolbarManager().createControl( composite );

    m_viewer = new TupleResultTableViewer( composite, style );

    // dem Editor beibringen, nur dann eine Zelle zu editieren, wenn der EditMode aktiviert ist
    final ColumnViewerEditorActivationStrategy eas = new ColumnViewerEditorActivationStrategy( m_viewer )
    {
      @Override
      protected boolean isEditorActivationEvent( final ColumnViewerEditorActivationEvent event )
      {
        if( !isEditMode() )
          return false;
        return super.isEditorActivationEvent( event );
      }

    };

    TableViewerEditor.create( m_viewer, eas, ColumnViewerEditor.DEFAULT );

    final Table table = m_viewer.getTable();
    table.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

    m_tupleResultContentProvider = new TupleResultContentProvider( m_handlerProvider );
    m_tupleResultLabelProvider = new TupleResultLabelProvider( m_tupleResultContentProvider );

    if( m_viewerFilter != null )
      m_viewer.addFilter( m_viewerFilter );

    final ICellModifier tupleResultCellModifier = new TupleResultCellModifier( m_tupleResultContentProvider );

    m_viewer.setContentProvider( m_tupleResultContentProvider );
    m_viewer.setLabelProvider( m_tupleResultLabelProvider );

    m_viewer.setCellModifier( tupleResultCellModifier );
    m_viewer.setInput( null );

    updateControl();

    if( getToolbarManager() != null )
      hookExecutionListener( m_viewer, getToolbarManager() );

    if( isEditMode() )
    {
      m_cursor = new ExcelTableCursor( m_viewer, SWT.BORDER_DASH, ADVANCE_MODE.DOWN, true );
      m_controlEditor = new ControlEditor( m_cursor );
      m_controlEditor.grabHorizontal = true;
      m_controlEditor.grabVertical = true;

      m_cursor.setVisible( true );
      m_cursor.setEnabled( true );
    }

    return composite;
  }

  @Override
  public void dispose( )
  {
    m_tupleResultContentProvider.dispose();
    m_tupleResultLabelProvider.dispose();

    if( m_tupleResult != null )
    {
      m_tupleResult.removeChangeListener( this );
      m_tupleResult = null;
    }

    if( getToolbarManager() != null )
    {
      getToolbarManager().dispose();
      getToolbarManager().removeAll();
    }

    super.dispose();
  }

  @Override
  public void updateControl( )
  {
    if( m_ignoreNextUpdateControl > 0 )
    {
      m_ignoreNextUpdateControl--;
      return;
    }

    final Feature feature = getObservationFeature();
    if( m_tupleResult != null )
      m_tupleResult.removeChangeListener( this );

    final IObservation<TupleResult> obs = feature == null ? null : ObservationFeatureFactory.toObservation( feature );
    m_tupleResult = obs == null ? null : obs.getResult();

    if( m_tupleResult != null )
      m_tupleResult.addChangeListener( this );

    m_viewer.setInput( m_tupleResult );
  }

  /**
   * Returns the observation.
   * <p>
   * If the given property is a relation type, get the feature from that property, else directly use the given feature of this control.
   * </p>
   */
  private Feature getObservationFeature( )
  {
    final Feature feature = getFeature();
    final IPropertyType ftp = getFeatureTypeProperty();

    return getObservationFeature( feature, ftp );
  }

  static Feature getObservationFeature( final Feature feature, final IPropertyType ftp )
  {
    if( ftp instanceof IRelationType )
    {
      final Object property = feature.getProperty( ftp );
      return FeatureHelper.getFeature( feature.getWorkspace(), property );
    }

    return feature;
  }

  @Override
  public boolean isValid( )
  {
    return true;
  }

  @Override
  public void addModifyListener( final ModifyListener l )
  {
    m_listener.add( l );
  }

  @Override
  public void removeModifyListener( final ModifyListener l )
  {
    m_listener.remove( l );
  }

  @Override
  public void valuesChanged( final ValueChange[] changes )
  {
    fireChanges( false );
    // fireModified();
  }

  @Override
  public void recordsChanged( final IRecord[] records, final TYPE type )
  {
    fireChanges( false );
    // fireModified();
  }

  /**
   * @see org.kalypso.observation.result.ITupleResultChangedListener#componentsChanged(org.kalypso.observation.result.IComponent[], org.kalypso.observation.result.ITupleResultChangedListener.TYPE)
   */
  @Override
  public void componentsChanged( final IComponent[] components, final TYPE type )
  {
    fireChanges( true );
  }

  private void fireChanges( final boolean definitionChanged )
  {
    final Feature obsFeature = getObservationFeature();
    final IFeatureType obsFT = obsFeature.getFeatureType();
    final IRelationType resultDefPT = (IRelationType)obsFT.getProperty( ObservationFeatureFactory.OM_RESULTDEFINITION );
    final IPropertyType resultPT = obsFT.getProperty( ObservationFeatureFactory.OM_RESULT );

    final Feature rd = ObservationFeatureFactory.buildRecordDefinition( obsFeature, resultDefPT, m_tupleResult.getComponents(), m_tupleResult.getSortComponents(), m_tupleResult.getOrdinalNumberComponent() );

    final String strResult = ObservationFeatureFactory.serializeResultAsString( m_tupleResult );

    // PROBLEM: we have 2 changes, so we get entries to the undo queue here
    // TODO: refaktor so that we may send multiple changes at one go
    if( definitionChanged )
    {
      m_ignoreNextUpdateControl++;
      fireFeatureChange( new ChangeFeatureCommand( obsFeature, resultDefPT, rd ) );
    }

    m_ignoreNextUpdateControl++;
    fireFeatureChange( new ChangeFeatureCommand( obsFeature, resultPT, strResult ) );
  }

  /**
   * must be called before createControl() is called!
   */
  public void setViewerFilter( final ViewerFilter filter )
  {
    m_viewerFilter = filter;
  }

  private static IComponentUiHandlerProvider createHandler( final org.kalypso.template.featureview.TupleResult editorType )
  {
    final String columnProviderId = editorType.getColumnProviderId();

    final ColumnDescriptor[] descriptors = editorType.getColumnDescriptor().toArray( new ColumnDescriptor[] {} );
    if( descriptors.length == 0 )
      return KalypsoUIExtensions.createComponentUiHandlerProvider( columnProviderId );

    return new TupleResultFeatureControlHandlerProvider( descriptors );
  }

  public static TupleResultFeatureControl create( final org.kalypso.template.featureview.TupleResult editorType, final Feature feature, final IPropertyType ftp )
  {
    final IComponentUiHandlerProvider provider = createHandler( editorType );

    final Toolbar toolbar = editorType.getToolbar();
    final TupleResultFeatureControl tfc = new TupleResultFeatureControl( feature, ftp, provider, toolbar != null );

    if( toolbar == null )
      return tfc;
    final List<Toolbar.Command> commands = toolbar.getCommand();
    for( final Toolbar.Command command : commands )
    {
      final String commandId = command.getCommandId();
      final int style = SWTUtilities.createStyleFromString( command.getStyle() );
      tfc.addToolbarItem( commandId, style );
    }

    final List<MenuContribution> contributionUris = toolbar.getMenuContribution();
    for( final MenuContribution contribution : contributionUris )
      tfc.addToolbarItems( contribution.getUri() );

    return tfc;
  }

  boolean isEditMode( )
  {
    /**
     * TODO check if table is editable
     */
    return true;
  }
}