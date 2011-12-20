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
package org.kalypso.model.wspm.ui.profil.wizard.landuse.utils;

import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.model.wspm.core.gml.classifications.IClassificationClass;
import org.kalypso.model.wspm.ui.profil.wizard.landuse.model.ILanduseModel;
import org.kalypso.model.wspm.ui.view.table.handler.ClassificationLabelProvider;
import org.kalypso.ogc.gml.om.table.celleditor.ComboBoxViewerCellEditor;

/**
 * @author Dirk Kuch
 */
@SuppressWarnings("deprecation")
public class LandMappingEditingSupport extends EditingSupport
{
  private final ILanduseModel m_model;

  @SuppressWarnings("deprecation")
  public LandMappingEditingSupport( final ColumnViewer viewer, final ILanduseModel model )
  {
    super( viewer );
    m_model = model;
    final Composite parent = (Composite) viewer.getControl();

  }

  /**
   * @see org.eclipse.jface.viewers.EditingSupport#getCellEditor(java.lang.Object)
   */
  @Override
  protected CellEditor getCellEditor( final Object element )
  {

    final Composite parent = (Composite) getViewer().getControl();
    /** on-the-fly generation - because classification classes can change (roughness or vegetation classes!) */
    return new ComboBoxViewerCellEditor( new ArrayContentProvider(), new ClassificationLabelProvider(), m_model.getClasses(), parent, SWT.READ_ONLY | SWT.DROP_DOWN );
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
    @SuppressWarnings("rawtypes")
    final Entry entry = LanduseMappingLabelProvider.toEntry( element );

    final Object objValue = entry.getValue();
    if( objValue instanceof String )
    {
      final String strValue = (String) objValue;
      if( StringUtils.isEmpty( strValue ) )
        return null;

      for( final IClassificationClass clazz : m_model.getClasses() )
      {
        if( clazz.getName().equals( strValue ) )
          return clazz;
      }
    }

    return null;
  }

  /**
   * @see org.eclipse.jface.viewers.EditingSupport#setValue(java.lang.Object, java.lang.Object)
   */
  @SuppressWarnings("unchecked")
  @Override
  protected void setValue( final Object element, final Object value )
  {
    @SuppressWarnings("rawtypes")
    final Entry entry = LanduseMappingLabelProvider.toEntry( element );
    if( Objects.isNull( entry ) )
      return;

    if( !(value instanceof IClassificationClass) )
      return;

    final IClassificationClass clazz = (IClassificationClass) value;
    entry.setValue( clazz.getName() );

    getViewer().refresh( entry );
  }

}
