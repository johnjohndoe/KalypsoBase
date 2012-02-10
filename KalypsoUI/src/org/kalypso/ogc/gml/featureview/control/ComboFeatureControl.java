/*--------------- Kalypso-Header ------------------------------------------

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

 --------------------------------------------------------------------------*/

package org.kalypso.ogc.gml.featureview.control;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.IValuePropertyType;
import org.kalypso.gmlschema.property.PropertyUtils;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.ogc.gml.command.ChangeFeatureCommand;
import org.kalypso.ui.editor.gmleditor.ui.GMLLabelProvider;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree_impl.model.feature.XLinkedFeature_Impl;
import org.kalypsodeegree_impl.model.feature.search.DefaultReferenceCollectorStrategy;
import org.kalypsodeegree_impl.model.feature.search.IReferenceCollectorStrategy;

/**
 * This feature control is a combo box, which just sets the feature-value to the given value when selected.
 * <p>
 * Today only properties with String type are supported.
 * </p>
 * 
 * @author Gernot Belger
 */
public class ComboFeatureControl extends AbstractFeatureControl
{
  private static final Object NULL_LINK = new Object();

  private final ISelectionChangedListener m_listener = new ISelectionChangedListener()
  {
    @Override
    public void selectionChanged( final SelectionChangedEvent event )
    {
      comboSelected( (IStructuredSelection) event.getSelection() );
    }
  };

  private ComboViewer m_comboViewer = null;

  private final Map<Object, String> m_fixedEntries = new LinkedHashMap<Object, String>();

  private final LinkedHashMap<Object, String> m_entries = new LinkedHashMap<Object, String>();

  private boolean m_ignoreNextUpdate = false;

  /**
   * Used for sorting the elements in the combobox.
   */
  private final ViewerComparator m_comparator;

  /**
   * Used for filtering the elements in the combobox.
   */
  private final ViewerFilter m_filter;

  public ComboFeatureControl( final Feature feature, final IPropertyType ftp, final Map<Object, String> entries, final ViewerComparator comparator, final ViewerFilter filter )
  {
    super( feature, ftp );

    if( entries != null )
      m_fixedEntries.putAll( entries );

    m_comparator = comparator;
    m_filter = filter;
  }

  private void updateEntries( final IPropertyType ftp )
  {
    m_entries.clear();

    if( ftp instanceof IValuePropertyType )
    {
      final IValuePropertyType vpt = (IValuePropertyType) ftp;
      final Map<Object, String> createComboEntries = PropertyUtils.createComboEntries( vpt );
      m_entries.putAll( createComboEntries );

      if( vpt.isFixed() )
        m_comboViewer.getControl().setEnabled( false );

      return;
    }

    if( ftp instanceof IRelationType )
    {
      final IRelationType rt = (IRelationType) ftp;
      if( !rt.isInlineAble() && rt.isLinkAble() )
      {
        /* Null entry to delete link if this is allowed */
        if( rt.isNillable() )
          m_entries.put( NULL_LINK, "<kein Link>" ); //$NON-NLS-1$

        /* Find all substituting features. */
        final Feature feature = getFeature();

        final GMLWorkspace workspace = feature.getWorkspace();

        final IReferenceCollectorStrategy strategy = createSearchStrategy( workspace, feature, rt );
        final Feature[] features = strategy.collectReferences();

        final GMLLabelProvider labelProvider = new GMLLabelProvider();

        for( final Feature foundFeature : features )
        {
          final String featureLabel = labelProvider.getText( foundFeature );
          if( foundFeature instanceof XLinkedFeature_Impl )
            m_entries.put( foundFeature, featureLabel );
          else
            m_entries.put( foundFeature.getId(), featureLabel );
        }
      }
    }
  }

  /**
   * Creates the search strategy that determines the list of features for display.<br/>
   * We might later implement an extension-point in order to implement different strategies.
   */
  public static IReferenceCollectorStrategy createSearchStrategy( final GMLWorkspace workspace, final Feature parentFeature, final IRelationType parentRelation )
  {
    return new DefaultReferenceCollectorStrategy( workspace, parentFeature, parentRelation );
  }

  /**
   * @see org.kalypso.ogc.gml.featureview.control.AbstractFeatureControl#dispose()
   */
  @Override
  public void dispose( )
  {
    if( m_comboViewer != null && !m_comboViewer.getControl().isDisposed() )
      m_comboViewer.removeSelectionChangedListener( m_listener );
  }

  @Override
  public Control createControl( final Composite parent, final int style )
  {
    m_comboViewer = new ComboViewer( parent, style );

    m_comboViewer.setContentProvider( new ArrayContentProvider() );

    final Map<Object, String> entries = m_entries;
    m_comboViewer.setLabelProvider( new LabelProvider()
    {
      @Override
      public String getText( final Object element )
      {
        if( entries.containsKey( element ) )
          return entries.get( element );

        return super.getText( element );
      }
    } );

    /* Set the comparator, if any was given. */
    if( m_comparator != null )
      m_comboViewer.setComparator( m_comparator );

    /* Set the filter, if any was given. */
    if( m_filter != null )
      m_comboViewer.addFilter( m_filter );

    m_comboViewer.setInput( m_entries.keySet() );

    m_comboViewer.addSelectionChangedListener( m_listener );

    updateControl();

    return m_comboViewer.getControl();
  }

  protected void comboSelected( final IStructuredSelection selection )
  {
    final Feature feature = getFeature();
    final IPropertyType pt = getFeatureTypeProperty();

    final Object oldValue = getCurrentFeatureValue();
    final Object newSelection = selection.isEmpty() ? null : selection.getFirstElement();
    final Object newValue = newSelection == NULL_LINK ? null : newSelection;

    /* Null check first */
    if( newValue == oldValue )
      return;

    if( (newValue == null && oldValue != null) || !newValue.equals( oldValue ) )
    {
      m_ignoreNextUpdate = true;
      fireFeatureChange( new ChangeFeatureCommand( feature, pt, newValue ) );
    }
  }

  @Override
  public void updateControl( )
  {
    if( m_ignoreNextUpdate )
    {
      m_ignoreNextUpdate = false;
      return;
    }

    final Object currentFeatureValue = getCurrentFeatureValue();

    updateEntries( getFeatureTypeProperty() );

    m_comboViewer.refresh();

    if( currentFeatureValue == null && m_entries.containsKey( NULL_LINK ) )
      m_comboViewer.setSelection( new StructuredSelection( NULL_LINK ), true );
    else
    {
      final String entry = m_entries.get( currentFeatureValue );
      if( entry != null )
        m_comboViewer.setSelection( new StructuredSelection( currentFeatureValue ), true );
    }
  }

  /** Returns the current value of the feature as string. */
  private Object getCurrentFeatureValue( )
  {
    return getFeature().getProperty( getFeatureTypeProperty() );
  }

  @Override
  public boolean isValid( )
  {
    // a radio button is always valid
    return true;
  }

  protected ComboViewer getComboViewer( )
  {
    return m_comboViewer;
  }
}