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
package org.kalypso.model.wspm.ui.view.table.handler;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ObjectUtils;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Table;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.model.wspm.core.gml.classifications.IClassificationClass;
import org.kalypso.model.wspm.core.gml.classifications.IRoughnessClass;
import org.kalypso.model.wspm.core.gml.classifications.IWspmClassification;
import org.kalypso.model.wspm.core.gml.classifications.helper.WspmClassifications;
import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypso.observation.result.IRecord;
import org.kalypso.ogc.gml.om.table.celleditor.ComboBoxViewerCellEditor;

/**
 * Handles roughness class values.
 * 
 * @author Dirk Kuch
 */
public class RoughnessClassUiHandler extends AbstractComponentClassUiHandler
{
  private final IProfil m_profile;

  public RoughnessClassUiHandler( final int component, final boolean editable, final boolean resizeable, final boolean moveable, final String columnLabel, final int columnWidth, final int columnWidthPercent, final IProfil profile )
  {
    super( component, editable, resizeable, moveable, columnLabel, SWT.NONE, columnWidth, columnWidthPercent, "%s", "%s", "" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    m_profile = profile;
  }

  /**
   * @see org.kalypso.ogc.gml.om.table.handlers.IComponentUiHandler#createCellEditor(org.eclipse.swt.widgets.Table)
   */
  @Override
  public CellEditor createCellEditor( final Table table )
  {
    return new ComboBoxViewerCellEditor( new ArrayContentProvider(), new ClassificationLabelProvider(), getRoughnessClasses(), table, SWT.READ_ONLY | SWT.DROP_DOWN );
  }

  @Override
  public String getStringRepresentation( final IRecord record )
  {
    final Object value = record.getValue( getComponent() );
    if( Objects.isNull( value ) )
      return super.getStringRepresentation( record );

    final IWspmClassification classification = WspmClassifications.getClassification( m_profile );
    final IRoughnessClass clazz = classification.findRoughnessClass( value.toString() );
    if( Objects.isNotNull( clazz ) )
    {
      final List<String> parameters = new ArrayList<String>();
      append( parameters, "ks=%.2f", clazz.getKsValue() );
      append( parameters, "kst=%.2f", clazz.getKstValue() );

      return getStringRepresentation( clazz.getDescription(), parameters );
    }

    return super.getStringRepresentation( record );
  }

  private IRoughnessClass[] getRoughnessClasses( )
  {
    final IWspmClassification classification = WspmClassifications.getClassification( m_profile );
    if( Objects.isNull( classification ) )
      return new IRoughnessClass[] {};

    final IRoughnessClass[] roughnesses = classification.getRoughnessClasses();

    return roughnesses;
  }

  @Override
  public Object doGetValue( final IRecord record )
  {
    final Object value = record.getValue( getComponent() );
    if( Objects.isNull( value ) )
      return null; //$NON-NLS-1$

    final IWspmClassification classification = WspmClassifications.getClassification( m_profile );
    return classification.findRoughnessClass( (String) value );
  }

  @Override
  public void doSetValue( final IRecord record, final Object value )
  {
    if( value instanceof IClassificationClass )
    {
      final IClassificationClass clazz = (IClassificationClass) value;
      setValue( record, clazz.getName() );
    }
  }

  @Override
  public Object parseValue( final String text )
  {
    return text;
  }

  @Override
  public void setValue( final IRecord record, final Object value )
  {
    final int index = getComponent();
    final Object oldValue = record.getValue( index );

    if( !ObjectUtils.equals( value, oldValue ) )
      record.setValue( getComponent(), value );
  }
}