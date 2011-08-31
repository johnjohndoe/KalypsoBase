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
package org.kalypso.model.wspm.ui.profil.wizard.landuse.pages;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.kalypso.model.wspm.core.IWspmPointProperties;
import org.kalypso.model.wspm.core.gml.IWspmProject;
import org.kalypso.model.wspm.core.gml.classifications.IClassificationClass;
import org.kalypso.model.wspm.core.gml.classifications.IWspmClassification;
import org.kalypso.model.wspm.ui.profil.wizard.landuse.utils.ILanduseShapeDataProvider;
import org.kalypso.model.wspm.ui.view.table.handler.ClassificationLabelProvider;
import org.kalypso.ogc.gml.om.table.celleditor.ComboBoxViewerCellEditor;

/**
 * @author kuch
 */
public class ClassificationEditingSupport extends EditingSupport
{

  private final ILanduseShapeDataProvider m_provider;

  private ComboBoxViewerCellEditor m_editor;

  private IClassificationClass[] m_classes;

  @SuppressWarnings("deprecation")
  public ClassificationEditingSupport( final ColumnViewer viewer, final ILanduseShapeDataProvider provider, final String type )
  {
    super( viewer );

    m_provider = provider;

    try
    {
      final IWspmProject project = provider.getWspmModel();
      final IWspmClassification classification = project.getClassificationMember();

      final Composite parent = (Composite) viewer.getControl();

      if( IWspmPointProperties.POINT_PROPERTY_BEWUCHS_CLASS.equals( type ) )
      {
        m_classes = classification.getVegetationClasses();
        m_editor = new ComboBoxViewerCellEditor( new ArrayContentProvider(), new ClassificationLabelProvider(), m_classes, parent, SWT.READ_ONLY | SWT.DROP_DOWN );
      }
      else if( IWspmPointProperties.POINT_PROPERTY_ROUGHNESS_CLASS.equals( type ) )
      {
        m_classes = classification.getRoughnessClasses();
        m_editor = new ComboBoxViewerCellEditor( new ArrayContentProvider(), new ClassificationLabelProvider(), m_classes, parent, SWT.READ_ONLY | SWT.DROP_DOWN );
      }
      else
        throw new UnsupportedOperationException();
    }
    catch( final Exception e )
    {
      e.printStackTrace();
    }

  }

  /**
   * @see org.eclipse.jface.viewers.EditingSupport#getCellEditor(java.lang.Object)
   */
  @Override
  protected CellEditor getCellEditor( final Object element )
  {
    return m_editor;
  }

  /**
   * @see org.eclipse.jface.viewers.EditingSupport#canEdit(java.lang.Object)
   */
  @Override
  protected boolean canEdit( final Object element )
  {
    return true;
  }

  /**
   * @see org.eclipse.jface.viewers.EditingSupport#getValue(java.lang.Object)
   */
  @Override
  protected Object getValue( final Object element )
  {
    if( element instanceof Map.Entry )
    {
      @SuppressWarnings("rawtypes")
      final Map.Entry entry = (Map.Entry) element;
      final Object objValue = entry.getValue();
      if( objValue instanceof String )
      {
        final String strValue = (String) objValue;
        if( StringUtils.isEmpty( strValue ) )
          return null;

        for( final IClassificationClass clazz : m_classes )
        {
          if( clazz.getName().equals( strValue ) )
            return clazz;
        }
      }
    }

    return null;
  }

  /**
   * @see org.eclipse.jface.viewers.EditingSupport#setValue(java.lang.Object, java.lang.Object)
   */
  @Override
  protected void setValue( final Object element, final Object value )
  {
    if( !(value instanceof IClassificationClass) )
      return;

    final IClassificationClass clazz = (IClassificationClass) value;

    if( element instanceof Map.Entry )
    {
      @SuppressWarnings("rawtypes")
      final Map.Entry entry = (Map.Entry) element;
      entry.setValue( clazz.getName() );

      getViewer().refresh( entry );
    }
  }

}
