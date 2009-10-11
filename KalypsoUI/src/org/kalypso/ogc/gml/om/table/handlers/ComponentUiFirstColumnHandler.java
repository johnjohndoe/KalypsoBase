/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
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
package org.kalypso.ogc.gml.om.table.handlers;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Table;
import org.kalypso.observation.result.IRecord;

/**
 * This is the handler for a invisible, empty column. Used to fill the first column of a table with a dummy column, in
 * order to align all columns correctly.
 * 
 * @author Gernot Belger
 */
public class ComponentUiFirstColumnHandler implements IComponentUiHandler
{
  /**
   * @see org.kalypso.ogc.gml.om.table.handlers.IComponentUiHandler#createCellEditor(org.eclipse.swt.widgets.Table)
   */
  public CellEditor createCellEditor( final Table table )
  {
    return null;
  }

  /**
   * @see org.kalypso.ogc.gml.om.table.handlers.IComponentUiHandler#formatValue(java.lang.Object)
   */
  public Object doGetValue( final IRecord record )
  {
    return null;
  }

  /**
   * @see org.kalypso.ogc.gml.om.table.handlers.IComponentUiHandler#setValue(org.kalypso.observation.result.IRecord,
   *      java.lang.Object)
   */
  public void doSetValue( final IRecord record, final Object value )
  {
  }

  /**
   * @see org.kalypso.ogc.gml.om.table.handlers.IComponentUiHandler#getColumnLabel()
   */
  public String getColumnLabel( )
  {
    return "-"; //$NON-NLS-1$
  }

  /**
   * @see org.kalypso.ogc.gml.om.table.handlers.IComponentUiHandler#getColumnStyle()
   */
  public int getColumnStyle( )
  {
    return SWT.CENTER;
  }

  /**
   * @see org.kalypso.ogc.gml.om.table.handlers.IComponentUiHandler#getColumnWidth()
   */
  public int getColumnWidth( )
  {
    return 0;
  }

  /**
   * @see org.kalypso.ogc.gml.om.table.handlers.IComponentUiHandler#getColumnWidthPercent()
   */
  public int getColumnWidthPercent( )
  {
    return -1;
  }

  /**
   * @see org.kalypso.ogc.gml.om.table.handlers.IComponentUiHandler#getIdentity()
   */
  public String getIdentity( )
  {
    return getClass().getName();
  }

  /**
   * @see org.kalypso.ogc.gml.om.table.handlers.IComponentUiHandler#getStringRepresentation(org.kalypso.observation.result.IRecord)
   */
  public String getStringRepresentation( final IRecord value )
  {
    return ""; //$NON-NLS-1$
  }

  /**
   * @see org.kalypso.ogc.gml.om.table.handlers.IComponentUiHandler#isEditable()
   */
  public boolean isEditable( )
  {
    return false;
  }

  /**
   * @see org.kalypso.ogc.gml.om.table.handlers.IComponentUiHandler#isMoveable()
   */
  public boolean isMoveable( )
  {
    return false;
  }

  /**
   * @see org.kalypso.ogc.gml.om.table.handlers.IComponentUiHandler#isResizeable()
   */
  public boolean isResizeable( )
  {
    return false;
  }

  /**
   * @see org.kalypso.ogc.gml.om.table.handlers.IComponentUiHandler#getImage(org.kalypso.observation.result.IRecord)
   */
  public Image getImage( final IRecord record )
  {
    return null;
  }

  /**
   * @see org.kalypso.ogc.gml.om.table.handlers.IComponentUiHandler#parseValue(java.lang.String)
   */
  public Object parseValue( final String text )
  {
    return null;
  }

  /**
   * @see org.kalypso.ogc.gml.om.table.handlers.IComponentUiHandler#setValue(org.kalypso.observation.result.IRecord,
   *      java.lang.Object)
   */
  public void setValue( final IRecord record, final Object value )
  {
  }

}
