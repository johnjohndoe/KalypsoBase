package org.kalypso.zml.core.table.rules.impl;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.zml.core.KalypsoZmlCore;
import org.kalypso.zml.core.table.binding.rule.ZmlRule;
import org.kalypso.zml.core.table.model.references.IZmlValueReference;

/**
 * @author Dirk Kuch
 */
public class ZmlRuleDataSource extends AbstractZmlTableRule
{
  public static final String ID = "org.kalypso.zml.ui.core.rule.data.source"; //$NON-NLS-1$

  private static final ColorRegistry COLOR_REGISTRY = new ColorRegistry();

  private static List<Integer> COLORS = new ArrayList<Integer>();

  /**
   * @see org.kalypso.zml.ui.core.rules.IZmlTableRule#getIdentifier()
   */
  @Override
  public String getIdentifier( )
  {
    return ID;
  }

  /**
   * @see org.kalypso.zml.ui.core.rules.IZmlTableRule#apply(org.kalypso.zml.ui.table.provider.ZmlValueReference)
   */
  @Override
  protected boolean doApply( final ZmlRule rule, final IZmlValueReference reference )
  {
    try
    {
      final String source = reference.getDataSource();

      return StringUtils.isNotEmpty( source );
    }
    catch( final Throwable t )
    {
      KalypsoZmlCore.getDefault().getLog().log( StatusUtilities.statusFromThrowable( t ) );
    }

    return false;
  }

// /**
// * @see
// org.kalypso.zml.core.table.rules.impl.AbstractZmlTableRule#update(org.kalypso.zml.core.table.binding.rule.ZmlRule,
// * org.kalypso.zml.core.table.model.references.IZmlValueReference, java.lang.String)
// */
// @Override
// public String update( final ZmlRule rule, final IZmlValueReference reference, final String text ) throws
// SensorException
// {
// final String dataSource = reference.getDataSource();
// final IRepositoryItem item = findItem( dataSource );
// if( Objects.isNull( item ) )
// return String.format( "%s (%s)", text, dataSource );
//
// final String name = item.getName();
//
// return String.format( "%s (%s)", text, name );
// }

// private IRepositoryItem findItem( final String dataSource )
// {
// final IRepositoryRegistry registry = KalypsoRepository.getDefault().getRepositoryRegistry();
// final IRepository[] repositories = registry.getRepositories();
//
// for( final IRepository repository : repositories )
// {
// try
// {
// final IRepositoryItem item = RepositoryUtils.findEquivalentItem( repository, dataSource );
// if( Objects.isNotNull( item ) )
// return item;
// }
// catch( final RepositoryException e )
// {
// e.printStackTrace();
// }
// }
//
// return null;
// }

  /**
   * @see org.kalypso.zml.core.table.rules.impl.AbstractZmlTableRule#getBackground(org.kalypso.zml.core.table.model.references.IZmlValueReference)
   */
  @Override
  public Color getBackground( final IZmlValueReference reference ) throws SensorException
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

  private RGB getRGB( )
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
