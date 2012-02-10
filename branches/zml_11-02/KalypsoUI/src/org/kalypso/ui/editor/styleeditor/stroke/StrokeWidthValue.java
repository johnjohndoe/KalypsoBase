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
package org.kalypso.ui.editor.styleeditor.stroke;

import org.eclipse.swt.widgets.Spinner;
import org.kalypso.ui.editor.styleeditor.binding.IStyleInput;
import org.kalypso.ui.editor.styleeditor.binding.InputWithContextObservableValue;
import org.kalypsodeegree.filterencoding.FilterEvaluationException;
import org.kalypsodeegree.graphics.sld.Stroke;

/**
 * @author Gernot Belger
 */
public class StrokeWidthValue extends InputWithContextObservableValue<Stroke, Integer>
{
  public StrokeWidthValue( final IStyleInput<Stroke> input )
  {
    super( input, Integer.class );
  }

  @Override
  protected Integer getValueFromData( final Stroke data )
  {
    try
    {
      return (int) (data.getWidth( null ) * 10.0);
    }
    catch( final FilterEvaluationException e )
    {
      e.printStackTrace();
      return 100;
    }
  }

  @Override
  protected void setValueToInput( final Stroke data, final Integer value )
  {
    if( value == null )
      data.setWidth( 1.0 );
    else
      data.setWidth( value / 10.0 );
  }

  public void configureSpinner( final Spinner spinner )
  {
    spinner.setMinimum( 0 );
    spinner.setMaximum( 1000 );
    spinner.setDigits( 1 );
    spinner.setIncrement( 10 );
    spinner.setPageIncrement( 100 );
  }
}