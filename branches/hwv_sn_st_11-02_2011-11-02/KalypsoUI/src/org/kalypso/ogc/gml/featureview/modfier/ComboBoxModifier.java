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
package org.kalypso.ogc.gml.featureview.modfier;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxViewerCellEditor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.kalypso.gmlschema.annotation.IAnnotation;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.i18n.Messages;
import org.kalypso.ogc.gml.featureview.control.ComboFeatureControl;
import org.kalypso.ui.editor.gmleditor.ui.GMLLabelProvider;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree_impl.model.feature.FeatureHelper;
import org.kalypsodeegree_impl.model.feature.XLinkedFeature_Impl;
import org.kalypsodeegree_impl.model.feature.gmlxpath.GMLXPath;
import org.kalypsodeegree_impl.model.feature.search.IReferenceCollectorStrategy;

/**
 * A modifier which handles feature-relations: shows a combo-box as cell-editor.
 * 
 * @author Gernot Belger
 */
public class ComboBoxModifier extends AbstractFeatureModifier
{
  private static final String NO_LINK_STRING = Messages.getString( "org.kalypso.ogc.gml.featureview.modfier.ComboBoxModifier.0" ); //$NON-NLS-1$

  final GMLLabelProvider m_cellEditorLabelProvider = new GMLLabelProvider();

  private ComboBoxViewerCellEditor m_comboBoxCellEditor = null;

  private Feature m_feature;

  public ComboBoxModifier( final GMLXPath propertyPath, final IRelationType ftp )
  {
    init( propertyPath, ftp );
  }

  @Override
  public Object getProperty( final Feature feature )
  {
    m_feature = feature;

    final Object property = super.getProperty( feature );

    // HACK/BUGFIX: the xpath resolved value is always a feasture, not the 'stirng' reference id for local featurr
    // references.
    // Revert the value to the string reference for this case.
    if( property instanceof Feature )
    {
      final GMLWorkspace workspace = ((Feature) property).getWorkspace();
      if( workspace == m_feature.getWorkspace() )
        return ((Feature) property).getId();
    }

    return property;
  }

  protected Object refreshInput( )
  {
    /* update input */
    final Collection<Object> input = new ArrayList<Object>();

    final IRelationType rt = (IRelationType) getPropertyType();
    if( !rt.isInlineAble() && rt.isLinkAble() )
    {
      /* Null entry to delete link if this is allowed */
      if( rt.isNillable() )
      {
        input.add( null );
      }

      final GMLWorkspace workspace = m_feature.getWorkspace();

      final IReferenceCollectorStrategy strategy = ComboFeatureControl.createSearchStrategy( workspace, m_feature, rt );
      final Feature[] features = strategy.collectReferences();

      for( final Feature foundFeature : features )
      {
        if( foundFeature instanceof XLinkedFeature_Impl )
          input.add( foundFeature );
        else
          input.add( foundFeature.getId() );
      }
    }

    return input;
  }

  @Override
  public CellEditor createCellEditor( final Composite parent )
  {
    m_comboBoxCellEditor = new ComboBoxViewerCellEditor( parent, SWT.READ_ONLY | SWT.DROP_DOWN )
    {
      @Override
      public void activate( )
      {
        final ViewerFilter filter = createFilter();
        if( filter != null )
          getViewer().setFilters( new ViewerFilter[] { filter } );

        final Object input = refreshInput();
        setInput( input );
      }

      @Override
      public void setInput( final Object input )
      {
        super.setInput( input );

        final Object selected = doGetValue();
        doSetValue( selected );
      }
    };

    m_comboBoxCellEditor.setActivationStyle( ComboBoxViewerCellEditor.DROP_DOWN_ON_KEY_ACTIVATION | ComboBoxViewerCellEditor.DROP_DOWN_ON_MOUSE_ACTIVATION
        | ComboBoxViewerCellEditor.DROP_DOWN_ON_PROGRAMMATIC_ACTIVATION | ComboBoxViewerCellEditor.DROP_DOWN_ON_TRAVERSE_ACTIVATION );

    m_comboBoxCellEditor.setContenProvider( new ArrayContentProvider() );

    m_comboBoxCellEditor.setLabelProvider( new LabelProvider()
    {
      @Override
      public String getText( final Object element )
      {
        return getCellEditorLabel( element );
      }
    } );

    return m_comboBoxCellEditor;
  }

  protected String getCellEditorLabel( final Object element )
  {
    if( element == null )
      return NO_LINK_STRING;

    final Feature foundFeature = findFeature( element );

    return m_cellEditorLabelProvider.getText( foundFeature );
  }

  private Feature findFeature( final Object element )
  {
    if( element instanceof Feature )
      return (Feature) element;

    if( element instanceof String )
    {
      final GMLWorkspace workspace = m_feature.getWorkspace();
      return workspace.getFeature( (String) element );
    }

    throw new IllegalStateException();
  }

  @Override
  public String isValid( final Object value )
  {
    return null; // null means vaild
  }

  @Override
  public String getLabel( final Feature f )
  {
    final IPropertyType ftp = getPropertyType();
    final Object fprop = f.getProperty( ftp );

    if( fprop == null )
      return NO_LINK_STRING;

    if( ftp instanceof IRelationType )
    {
      final Feature resolvedFeature = FeatureHelper.resolveLinkedFeature( f.getWorkspace(), fprop );
      if( resolvedFeature == null )
        return NO_LINK_STRING;

      return FeatureHelper.getAnnotationValue( resolvedFeature, IAnnotation.ANNO_LABEL );
    }

    // we should never reach this code, as the ComboBoxModifier is only used for relation types
    return fprop.toString();
  }

  /**
   * No filter on combo entries by default. Override to implement.
   */
  protected ViewerFilter createFilter( )
  {
    return null;
  }
}