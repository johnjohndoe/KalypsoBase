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

package org.kalypso.zml.core.table.rules.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.xml.namespace.QName;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.zml.core.KalypsoZmlCore;
import org.kalypso.zml.core.table.binding.CellStyle;
import org.kalypso.zml.core.table.binding.TableTypes;
import org.kalypso.zml.core.table.binding.rule.ZmlCellRule;
import org.kalypso.zml.core.table.model.references.IZmlModelCell;
import org.kalypso.zml.core.table.model.references.IZmlModelValueCell;
import org.kalypso.zml.core.table.rules.AbstractZmlCellRuleImplementation;
import org.kalypso.zml.core.table.schema.CellStyleType;
import org.kalypso.zml.core.table.schema.StylePropertyName;
import org.kalypso.zml.core.table.schema.StylePropertyType;

/**
 * @author Dirk Kuch
 */
public class ZmlRuleDataSource extends AbstractZmlCellRuleImplementation
{
  public static final String ID = "org.kalypso.zml.ui.core.rule.data.source"; //$NON-NLS-1$

  protected static final ColorRegistry COLOR_REGISTRY = new ColorRegistry();

  private static List<Integer> COLORS = new ArrayList<>();

  @Override
  public String getIdentifier( )
  {
    return ID;
  }

  @Override
  protected boolean doApply( final ZmlCellRule rule, final IZmlModelCell reference )
  {
    try
    {
      if( !(reference instanceof IZmlModelValueCell) )
        return false;

      final IZmlModelValueCell cell = (IZmlModelValueCell) reference;
      final String source = cell.getDataSource();

      return StringUtils.isNotEmpty( source );
    }
    catch( final Throwable t )
    {
      KalypsoZmlCore.getDefault().getLog().log( StatusUtilities.statusFromThrowable( t ) );
    }

    return false;
  }

  @Override
  public CellStyle getCellStyle( final ZmlCellRule rule, final IZmlModelCell reference )
  {

    if( !(reference instanceof IZmlModelValueCell) )
      return null;

    final CellStyleType styleType = new CellStyleType();
    styleType.setId( "RuleDataSourceBackgroundStyle" ); //$NON-NLS-1$

    try
    {
      final Color background = getBackground( (IZmlModelValueCell) reference );
      final List<StylePropertyType> properties = styleType.getProperty();

      final StylePropertyType property = new StylePropertyType();
      final Map<QName, String> attributes = property.getOtherAttributes();
      attributes.put( TableTypes.PROPERTY_NAME, StylePropertyName.BACKGROUND_COLOR.value() );
      property.setValue( String.format( "%2x%2x%2x", background.getRed(), background.getGreen(), background.getBlue() ) ); //$NON-NLS-1$
      properties.add( property );
    }
    catch( final Exception e )
    {
      e.printStackTrace();
    }

    return new CellStyle( styleType );
  }

  protected Color getBackground( final IZmlModelValueCell reference ) throws SensorException
  {
    final String source = reference.getDataSource();
    if( StringUtils.isEmpty( source ) )
      return null;

    final Color color = COLOR_REGISTRY.get( source );
    if( Objects.isNotNull( color ) )
      return color;

    final RGB rgb = getRGB();
    COLOR_REGISTRY.put( source, rgb );

    return COLOR_REGISTRY.get( source );
  }

  protected RGB getRGB( )
  {
    final int size = COLOR_REGISTRY.getKeySet().size() + 1;

    final Random random = new Random();
    final int generated = random.nextInt( Double.valueOf( 2048 / size ).intValue() );

    final int color = findColor( generated % 360 );
    if( color < 0 || color > 360 )
      return new RGB( 128, 0.3f, 0.7f );

    return new RGB( color, 0.3f, 0.7f );
  }

  private int findColor( final int value )
  {
    if( !COLORS.contains( value ) )
    {
      COLORS.add( value );

      return value;
    }

    int ptr = findColor( value, 13 );
    if( ptr != -1 )
      return ptr;

    ptr = findColor( 0, 13 );
    if( ptr != -1 )
      return ptr;

    ptr = findColor( value, 5 );
    if( ptr != -1 )
      return ptr;

    ptr = findColor( 0, 5 );
    if( ptr != -1 )
      return ptr;

    ptr = findColor( value, 1 );
    if( ptr != -1 )
      return ptr;

    ptr = findColor( 0, 1 );
    if( ptr != -1 )
      return ptr;

    return value;
  }

  private int findColor( final int value, final int increment )
  {
    int ptr = value;

    while( ptr < 360 )
    {
      ptr += increment;
      if( !COLORS.contains( ptr ) )
      {
        COLORS.add( ptr );
        return ptr;
      }
    }

    return -1;
  }
}
