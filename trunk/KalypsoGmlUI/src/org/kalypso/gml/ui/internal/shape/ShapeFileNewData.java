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
package org.kalypso.gml.ui.internal.shape;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.kalypso.gml.ui.i18n.Messages;
import org.kalypso.ogc.gml.serialize.ShapeSerializer;
import org.kalypso.shape.ShapeType;
import org.kalypso.shape.dbf.DBFField;
import org.kalypso.shape.dbf.DBaseException;
import org.kalypso.shape.dbf.FieldType;
import org.kalypso.shape.dbf.IDBFField;

/**
 * @author Gernot Belger
 */
public class ShapeFileNewData
{
  private ShapeType m_type = ShapeType.POINT;

  private Charset m_charset = ShapeSerializer.getShapeDefaultCharset();

  private final List<DBFFieldBean> m_fields = new ArrayList<>();

  private IFile m_shapeFile = null;

  public ShapeFileNewData( )
  {
    try
    {
      m_fields.add( new DBFFieldBean( new DBFField( Messages.getString( "ShapeFileNewData_0" ), FieldType.C, (short) 128, (short) 0 ) ) ); //$NON-NLS-1$
      m_fields.add( new DBFFieldBean( new DBFField( Messages.getString( "ShapeFileNewData_1" ), FieldType.N, (short) 20, (short) 10 ) ) ); //$NON-NLS-1$
    }
    catch( final DBaseException e )
    {
      e.printStackTrace();
    }
  }

  public void init( final IDialogSettings settings )
  {
    if( settings == null )
      return;

    // TODO Auto-generated method stub

  }

  public void storeSettings( final IDialogSettings settings )
  {
    if( settings == null )
      return;
    // TODO Auto-generated method stub

  }

  public void setType( final ShapeType type )
  {
    m_type = type;
  }

  public ShapeType getType( )
  {
    return m_type;
  }

  public void setCharset( final Charset charset )
  {
    m_charset = charset;
  }

  public Charset getCharset( )
  {
    return m_charset;
  }

  public IDBFField[] getFields( )
  {
    return m_fields.toArray( new IDBFField[m_fields.size()] );
  }

  public void setShapeFile( final IFile shapeFile )
  {
    m_shapeFile = shapeFile;
  }

  public IFile getShpFile( )
  {
    return m_shapeFile;
  }

  public List<DBFFieldBean> getFieldList( )
  {
    return m_fields;
  }
}