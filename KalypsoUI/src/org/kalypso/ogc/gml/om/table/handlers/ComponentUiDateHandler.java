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
package org.kalypso.ogc.gml.om.table.handlers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.lang.ObjectUtils;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.widgets.Table;
import org.kalypso.contribs.java.util.DateUtilities;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.i18n.Messages;
import org.kalypso.observation.result.IRecord;
import org.kalypso.ogc.gml.om.table.celleditor.DateTimeCellEditor;

/**
 * Handles XMLGreogorianCalendar types.
 *
 * @author Dirk Kuch
 */
public class ComponentUiDateHandler extends AbstractComponentUiHandler implements ICellEditorValidator
{
  private final boolean m_useDatePicker;

  public ComponentUiDateHandler( final int component, final boolean editable, final boolean resizeable, final boolean moveable, final String columnLabel, final int columnStyle, final int columnWidth, final int columnWidthPercent, final String displayFormat, final String nullFormat, final String parseFormat )
  {
    super( component, editable, resizeable, moveable, columnLabel, columnStyle, columnWidth, columnWidthPercent, displayFormat, nullFormat, parseFormat );

    // TODO: get from a global preference page?
    m_useDatePicker = false;
  }

  /**
   * @see org.kalypso.ogc.gml.om.table.handlers.IComponentUiHandler#createCellEditor(org.eclipse.swt.widgets.Table)
   */
  public CellEditor createCellEditor( final Table table )
  {
    if( m_useDatePicker )
      return new DateTimeCellEditor( table );

    final TextCellEditor textCellEditor = new TextCellEditor( table );

    textCellEditor.setValidator( this );

    return textCellEditor;
  }

  /**
   * @see org.kalypso.ogc.gml.om.table.handlers.IComponentUiHandler#formatValue(org.kalypso.observation.result.IRecord)
   */
  public Object doGetValue( final IRecord record )
  {
    final Object value = record.getValue( getComponent() );
    if( m_useDatePicker )
      return value;

    if( value == null )
      return ""; //$NON-NLS-1$

    return getStringRepresentation( record );
  }

  /**
   * @see org.kalypso.ogc.gml.om.table.handlers.IComponentUiHandler#setValue(org.kalypso.observation.result.IRecord,
   *      java.lang.Object)
   */
  public void doSetValue( final IRecord record, final Object value )
  {
    if( value == null )
      setValue( record, null );

    // This happens, if we use the TextEditor (m_datePicker == false)
    if( value instanceof String )
    {
      final String valueStr = (String) value;
      if( valueStr.isEmpty() )
        setValue( record, null );
      else
        setValue( record, parseValue( value.toString() ) );

      return;
    }

    setValue( record, value );
  }

  /**
   * @see org.kalypso.ogc.gml.om.table.handlers.IComponentUiHandler#getStringRepresentation(java.lang.Object)
   */
  @Override
  public String getStringRepresentation( final IRecord record )
  {
    final int component = getComponent();

    final Object value = record.getValue( component );

    if( value instanceof XMLGregorianCalendar )
    {
      final XMLGregorianCalendar xmlCal = (XMLGregorianCalendar) value;
      final Date date = DateUtilities.toDate( xmlCal );

      if( date == null )
        return String.format( getNullFormat() );

      final Calendar instance = Calendar.getInstance( KalypsoCorePlugin.getDefault().getTimeZone() );
      instance.setTime( date );

      final String displayFormat = getDisplayFormat();

      return String.format( displayFormat, instance );
    }

    return Messages.get( "org.kalypso.ogc.gml.om.table.handlers.ComponentUiDateHandler.0" ); //$NON-NLS-1$
  }

  /**
   * @see org.kalypso.ogc.gml.om.table.handlers.IComponentUiHandler#parseValue(java.lang.String)
   */
  public Object parseValue( final String text )
  {
    try
    {
      return parseValueInternal( text );
    }
    catch( final ParseException e )
    {
      throw new IllegalArgumentException( e );
    }
  }

  private Object parseValueInternal( final String text ) throws ParseException
  {
    final String parseFormat = getParseFormat();
    // DO NOT set a default format here! either configure correctly the table templates or set the
    // correct default parse format that fits to the default out-format in the default-handler-factory
    if( parseFormat == null )
      return null;

    final SimpleDateFormat simpleDateFormat = new SimpleDateFormat( parseFormat );
    final Date date = simpleDateFormat.parse( text );
    return DateUtilities.toXMLGregorianCalendar( date );
  }

  /**
   * @see org.kalypso.ogc.gml.om.table.handlers.IComponentUiHandler#setValue(org.kalypso.observation.result.IRecord,
   *      java.lang.Object)
   */
  public void setValue( final IRecord record, final Object value )
  {
    final int index = getComponent();
    final Object oldValue = record.getValue( index );

    if( !ObjectUtils.equals( value, oldValue ) )
      record.setValue( index, value );
  }

  /**
   * @see org.eclipse.jface.viewers.ICellEditorValidator#isValid(java.lang.Object)
   */
  @Override
  public String isValid( final Object value )
  {
    try
    {
      parseValueInternal( (String) value );
      return null;
    }
    catch( final ParseException e )
    {
      return String.format( "Expected date and time: %s", getParseFormat() );
    }
  }
}
