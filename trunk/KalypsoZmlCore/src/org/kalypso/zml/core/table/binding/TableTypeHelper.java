package org.kalypso.zml.core.table.binding;

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

import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import jregex.Pattern;
import jregex.RETokenizer;

import org.apache.commons.lang.NotImplementedException;
import org.eclipse.swt.SWT;
import org.kalypso.zml.core.table.schema.AbstractColumnType;
import org.kalypso.zml.core.table.schema.AlignmentType;
import org.kalypso.zml.core.table.schema.CellStyleType;
import org.kalypso.zml.core.table.schema.ColumnHeaderPropertyName;
import org.kalypso.zml.core.table.schema.ColumnHeaderPropertyType;
import org.kalypso.zml.core.table.schema.ColumnPropertyType;
import org.kalypso.zml.core.table.schema.DataColumnType;
import org.kalypso.zml.core.table.schema.HeaderEntry;
import org.kalypso.zml.core.table.schema.IndexColumnType;
import org.kalypso.zml.core.table.schema.StylePropertyName;
import org.kalypso.zml.core.table.schema.StylePropertyType;
import org.kalypso.zml.core.table.schema.StyleReferenceType;
import org.kalypso.zml.core.table.schema.ZmlTableType;

/**
 * @author Dirk Kuch
 */
public final class TableTypeHelper
{
  private static final QName PROPERTY_NAME = new QName( "name" ); //$NON-NLS-1$

  public static final Pattern PATTERN_CLONED_COLUMN_IDENTIFIER = new Pattern( ".*\\(\\d+\\)$" ); //$NON-NLS-1$

  public static final Pattern PATTERN_CLONED_COLUMN_TOKENIZER = new Pattern( "\\(\\d+\\)$" ); //$NON-NLS-1$

  private TableTypeHelper( )
  {
  }

  public static int toSWT( final AlignmentType alignment )
  {
    if( alignment == null )
      return SWT.LEFT;

    if( AlignmentType.LEFT.toString().equals( alignment.toString() ) )
      return SWT.LEFT;
    else if( AlignmentType.CENTER.toString().equals( alignment.toString() ) )
      return SWT.CENTER;
    else if( AlignmentType.RIGHT.toString().equals( alignment.toString() ) )
      return SWT.RIGHT;

    return SWT.LEFT;
  }

  public static AbstractColumnType finColumn( final ZmlTableType tableType, final String identifier )
  {
    final List<JAXBElement< ? extends AbstractColumnType>> columns = tableType.getColumns().getAbstractColumn();
    for( final JAXBElement< ? extends AbstractColumnType> columnType : columns )
    {
      final AbstractColumnType column = columnType.getValue();
      if( column.getId().equals( identifier ) )
        return column;
    }

    return null;
  }

  public static AbstractColumnType cloneColumn( final AbstractColumnType base )
  {
    if( base instanceof DataColumnType )
    {
      final DataColumnType data = (DataColumnType) base;
      final DataColumnType clone = new DataColumnType();

      copyBasicSettings( base, clone );

      clone.setIndexAxis( data.getIndexAxis() );
      clone.setValueAxis( data.getValueAxis() );

      return clone;
    }
    else if( base instanceof IndexColumnType )
    {
      final IndexColumnType clone = new IndexColumnType();
      copyBasicSettings( base, clone );

      return clone;
    }

    return null;
  }

  private static void copyBasicSettings( final AbstractColumnType source, final AbstractColumnType target )
  {
    final List<ColumnPropertyType> targetProperties = target.getProperty();
    targetProperties.clear();

    final List<ColumnPropertyType> srcProperties = source.getProperty();
    for( final ColumnPropertyType property : srcProperties )
    {
      targetProperties.add( property );
    }
  }

  public static AbstractColumnType findColumnType( final ZmlTableType tableType, final String identifier )
  {
    final List<JAXBElement< ? extends AbstractColumnType>> columnTypes = tableType.getColumns().getAbstractColumn();

    String id = identifier;

    /** cloned, multiple column entry?!? like W(1) or W(3) */
    if( PATTERN_CLONED_COLUMN_IDENTIFIER.matches( identifier ) )
    {
      final RETokenizer tokenizer = new RETokenizer( PATTERN_CLONED_COLUMN_TOKENIZER, identifier );
      id = tokenizer.nextToken();
    }

    for( final JAXBElement< ? extends AbstractColumnType> columnType : columnTypes )
    {
      final AbstractColumnType column = columnType.getValue();
      if( column.getId().equals( id ) )
        return column;
    }

    return null;
  }

  public static String findProperty( final CellStyleType style, final StylePropertyName property )
  {
    final List<StylePropertyType> properties = style.getProperty();
    for( final StylePropertyType prop : properties )
    {
      final String propertyName = TableTypeHelper.getPropertyName( prop );
      if( property.value().equals( propertyName ) )
        return prop.getValue();
    }

    return null;
  }

  public static StylePropertyType findPropertyType( final CellStyleType style, final StylePropertyName property )
  {
    final List<StylePropertyType> properties = style.getProperty();
    for( final StylePropertyType prop : properties )
    {
      final String propertyName = TableTypeHelper.getPropertyName( prop );
      if( property.value().equals( propertyName ) )
        return prop;
    }

    return null;
  }

  public static String findProperty( final HeaderEntry type, final ColumnHeaderPropertyName property )
  {
    final List<ColumnHeaderPropertyType> properties = type.getProperty();
    for( final ColumnHeaderPropertyType prop : properties )
    {
      final String propertyName = TableTypeHelper.getPropertyName( prop );
      if( property.value().equals( propertyName ) )
        return prop.getValue();
    }

    return null;
  }

  private static String getPropertyName( final ColumnHeaderPropertyType property )
  {
    final Map<QName, String> attributes = property.getOtherAttributes();

    return attributes.get( PROPERTY_NAME );
  }

  public static String getPropertyName( final StylePropertyType property )
  {
    final Map<QName, String> attributes = property.getOtherAttributes();

    return attributes.get( PROPERTY_NAME );
  }

  public static String getPropertyName( final ColumnPropertyType property )
  {
    final Map<QName, String> attributes = property.getOtherAttributes();

    return attributes.get( PROPERTY_NAME );
  }

  public static int toSWTFontWeight( final String fontWeight )
  {
    if( fontWeight == null )
      return SWT.NORMAL;

    int weight = SWT.NULL;

    if( fontWeight.toUpperCase().contains( "NORMAL" ) ) //$NON-NLS-1$
      weight |= SWT.NORMAL;

    if( fontWeight.toUpperCase().contains( "BOLD" ) ) //$NON-NLS-1$
      weight |= SWT.BOLD;

    if( fontWeight.toUpperCase().contains( "ITALIC" ) ) //$NON-NLS-1$
      weight |= SWT.ITALIC;

    return weight;
  }

  public static CellStyleType resolveReference( final StyleReferenceType reference )
  {
    if( reference == null )
      return null;

    final Object objReference = reference.getReference();
    if( objReference instanceof CellStyleType )
      return (CellStyleType) objReference;

    final String url = reference.getUrl();
    if( url != null )
      throw new NotImplementedException();

    return null;
  }

  public static StylePropertyType cloneProperty( final StylePropertyType source )
  {
    final StylePropertyType clone = new StylePropertyType();
    clone.setValue( source.getValue() );
    clone.getOtherAttributes().putAll( source.getOtherAttributes() );

    return clone;
  }

  public static CellStyleType cloneStyleType( final CellStyleType style )
  {
    final CellStyleType clone = new CellStyleType();
    clone.setId( style.getId() );
    clone.setBaseStyle( style.getBaseStyle() );
    final List<StylePropertyType> baseProperties = clone.getProperty();

    final List<StylePropertyType> properties = style.getProperty();
    for( final StylePropertyType property : properties )
    {
      baseProperties.add( cloneProperty( property ) );
    }

    return clone;
  }

}
