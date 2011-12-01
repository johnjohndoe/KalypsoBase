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
package org.kalypso.ui.editor.sldEditor;

import java.awt.Color;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Image;
import org.kalypso.contribs.java.awt.ColorUtilities;
import org.kalypsodeegree.filterencoding.FilterEvaluationException;
import org.kalypsodeegree.graphics.sld.Fill;
import org.kalypsodeegree.graphics.sld.PolygonColorMapEntry;
import org.kalypsodeegree.graphics.sld.Stroke;

/**
 * @author Thomas Jung
 * @author Gernot Belger
 */
public class PolygonColorMapLabelProvider extends ColorMapLabelProvider
{
  public PolygonColorMapLabelProvider( final TableViewer viewer )
  {
    super( viewer.getTable() );
  }

  /**
   * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
   */
  @Override
  public Image getColumnImage( final Object element, final int columnIndex )
  {
    return null;
  }

  /**
   * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
   */
  @Override
  public String getColumnText( final Object element, final int columnIndex )
  {
    final PolygonColorMapEntry entry = (PolygonColorMapEntry) element;

    final PolygonColorMapContentProvider.PROPS prop = PolygonColorMapContentProvider.PROPS.values()[columnIndex];

    switch( prop )
    {
      case label:
        return entry.getLabel( null );

      case from:
        // TODO: fixed scale is not good; consider examination of all existing values
        return String.format( "%.2f", entry.getFrom( null ) ); //$NON-NLS-1$
      case to:
        return String.format( "%.2f", entry.getTo( null ) ); //$NON-NLS-1$

      case stroke:
        return ""; //$NON-NLS-1$

      case fill:
        return ""; //$NON-NLS-1$

      default:
        throw new IllegalArgumentException();
    }
  }

  /**
   * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object, java.lang.String)
   */
  @Override
  public boolean isLabelProperty( final Object element, final String property )
  {
    try
    {
      PolygonColorMapContentProvider.PROPS.valueOf( property );
      return true;
    }
    catch( final RuntimeException e )
    {
      e.printStackTrace();
      return false;
    }
  }

  @Override
  protected java.awt.Color getAwtColor(Object element, int columnIndex)
  {
    final PolygonColorMapEntry entry = (PolygonColorMapEntry) element;

    final PolygonColorMapContentProvider.PROPS prop = PolygonColorMapContentProvider.PROPS.values()[columnIndex];

    switch( prop )
    {
      case label:
      case from:
      case to:
        return null;

      case stroke:
      {
        try
        {
          final Stroke entryStroke = entry.getStroke();
          final Color stroke = entryStroke.getStroke( null );
          final double opacity = entryStroke.getOpacity( null );
          return ColorUtilities.applyOpacity( stroke, opacity );
        }
        catch( final FilterEvaluationException e )
        {
          e.printStackTrace();
          return null;
        }
      }

      case fill:
      {
        try
        {
          final Fill entryFill = entry.getFill();
          final Color fill = entryFill.getFill( null );
          final double opacity = entryFill.getOpacity( null );
          return ColorUtilities.applyOpacity( fill, opacity );
        }
        catch( final FilterEvaluationException e )
        {
          e.printStackTrace();
          return null;
        }
      }

      default:
        throw new IllegalArgumentException();
    }    
  }
}
