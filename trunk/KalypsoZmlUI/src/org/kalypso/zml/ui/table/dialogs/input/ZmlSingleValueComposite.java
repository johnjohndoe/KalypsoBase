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
package org.kalypso.zml.ui.table.dialogs.input;

import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.eclipse.swt.layout.LayoutHelper;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.zml.core.table.model.IZmlModelColumn;
import org.kalypso.zml.ui.KalypsoZmlUI;
import org.kalypso.zml.ui.table.base.widgets.EnhancedComboViewer;
import org.kalypso.zml.ui.table.base.widgets.rules.DateWidgetRule;
import org.kalypso.zml.ui.table.base.widgets.rules.TimeWidgetRule;
import org.kalypso.zml.ui.table.model.IZmlTableColumn;

/**
 * @author Dirk Kuch
 */
public class ZmlSingleValueComposite extends Composite
{
  private final ZmlSingleValueModel m_model;

  private final IZmlTableColumn m_column;

  public ZmlSingleValueComposite( final Composite parent, final FormToolkit toolkit, final IZmlTableColumn column, final ZmlSingleValueModel model )
  {
    super( parent, SWT.NULL );
    m_column = column;
    m_model = model;

    setLayout( LayoutHelper.createGridLayout() );

    render( toolkit );
  }

  private void render( final FormToolkit toolkit )
  {
    final Composite base = toolkit.createComposite( this );
    base.setLayout( LayoutHelper.createGridLayout( 2 ) );
    base.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, false ) );

    final ZmlSingleValueRow[] rows = m_model.getRows();
    for( final ZmlSingleValueRow row : rows )
    {

      renderRow( base, toolkit, row );
    }

  }

  private void renderRow( final Composite base, final FormToolkit toolkit, final ZmlSingleValueRow row )
  {
    try
    {
      final Date date = row.getDate();
      final Date[] existing = getExistingDateValues();

      final EnhancedComboViewer viewerDay = new EnhancedComboViewer( base, toolkit, new DateWidgetRule() );
      viewerDay.setInput( getDayAnchors( existing ) );

      final EnhancedComboViewer viewerTime = new EnhancedComboViewer( base, toolkit, new TimeWidgetRule() );
      viewerTime.setFilter( new ViewerFilter()
      {
        @Override
        public boolean select( final Viewer viewer, final Object parentElement, final Object element )
        {
          // TODO Auto-generated method stub
          return false;
        }
      } );

      viewerDay.setInput( existing );
    }
    catch( final SensorException e )
    {
      KalypsoZmlUI.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
    }
  }

  private Date[] getDayAnchors( final Date[] existing )
  {
    final Set<Date> anchors = new TreeSet<Date>();

    for( final Date date : existing )
    {
      final Calendar calendar = Calendar.getInstance();
      calendar.setTime( date );
      calendar.set( Calendar.HOUR_OF_DAY, 0 );
      calendar.set( Calendar.MINUTE, 0 );
      calendar.set( Calendar.MILLISECOND, 0 );

      anchors.add( calendar.getTime() );
    }

    return anchors.toArray( new Date[] {} );
  }

  private Date[] getExistingDateValues( ) throws SensorException
  {
    final Set<Date> existing = new TreeSet<Date>();

    final IZmlModelColumn columnModel = m_column.getModelColumn();
    final IAxis axis = columnModel.getIndexAxis();
    final ITupleModel model = columnModel.getTupleModel();

    for( int index = 0; index < model.size(); index++ )
    {
      final Object object = model.get( index, axis );
      if( object instanceof Date )
      {
        existing.add( (Date) object );
      }
    }

    return existing.toArray( new Date[] {} );
  }
}
