/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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
package org.kalypso.zml.core.table.model.interpolation;

import org.kalypso.commons.exception.CancelVisitorException;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.TupleModelDataSet;
import org.kalypso.ogc.sensor.metadata.ITimeseriesConstants;
import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.ogc.sensor.status.KalypsoStati;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;
import org.kalypso.ogc.sensor.timeseries.datasource.DataSourceHandler;
import org.kalypso.ogc.sensor.visitor.ITupleModelValueContainer;
import org.kalypso.ogc.sensor.visitor.ITupleModelVisitor;
import org.kalypso.repository.IDataSourceItem;
import org.kalypso.zml.core.table.model.references.ZmlValues;

/**
 * update all values between between existing stuetzstellen and set 'em to the previous stuetstellen value<br>
 * <br>
 * REMARK: don't use for interpolationg values from manual zml table edits. in this case we can't handle all
 * {@link IDataSourceItem.SOURCE_INTERPOLATED_WECHMANN_VALUE} as stuetzstelle! (continous interpolation from changed
 * value to the next real stuetzstelle! see {@link ContinuedInterpolatedValueEditingStrategy})
 * 
 * <pre>
 *                                 ( update too  )
 *         x ----0--0--0------- x -----0---0--0-- x
 *      stuetz                stuetz            stuetz
 *      stelle                stelle            stelle
 *        s_1                  s_2              s_n
 * 
 * </pre>
 * 
 * @author Dirk Kuch
 */
public class ContinousInterpolatedValueVisitor implements ITupleModelVisitor
{
  private final MetadataList m_metadata;

  private TupleModelDataSet m_eStuetzstelle;

  private TupleModelDataSet m_vStuetzstelle;

  private final boolean m_handleFilledValuesAsStuetzstelle;

  public ContinousInterpolatedValueVisitor( final MetadataList metadata )
  {
    this( metadata, true );
  }

  /**
   * @param handleFilledValuesAsStuetzstelle
   *          filled wechmann values are generated from the psi time series repository adapter. Normally we handle
   *          theses values like stuetzstellen
   */
  public ContinousInterpolatedValueVisitor( final MetadataList metadata, final boolean handleFilledValuesAsStuetzstelle )
  {
    m_metadata = metadata;
    m_handleFilledValuesAsStuetzstelle = handleFilledValuesAsStuetzstelle;
  }

  @Override
  public void visit( final ITupleModelValueContainer container ) throws SensorException, CancelVisitorException
  {
    final TupleModelDataSet wechmannE = container.getDataSetFor( m_metadata, ITimeseriesConstants.TYPE_WECHMANN_E );
    final TupleModelDataSet wechmannV = container.getDataSetFor( m_metadata, ITimeseriesConstants.TYPE_WECHMANN_SCHALTER_V );
    if( Objects.isNull( wechmannE, wechmannV ) )
      throw new CancelVisitorException();

    if( isStuetzstelle( wechmannE ) )
    {
      m_eStuetzstelle = wechmannE;
    }
    else if( m_eStuetzstelle != null )
    {
      apply( container, m_eStuetzstelle );
    }
    else
    {
      apply( container, getMissing( container, ITimeseriesConstants.TYPE_WECHMANN_E ) );
    }

    if( isStuetzstelle( wechmannV ) )
    {
      m_vStuetzstelle = wechmannV;
    }
    else if( m_vStuetzstelle != null )
    {
      apply( container, m_vStuetzstelle );
    }
    else
    {
      apply( container, getMissing( container, ITimeseriesConstants.TYPE_WECHMANN_SCHALTER_V ) );
    }
  }

  private TupleModelDataSet getMissing( final ITupleModelValueContainer container, final String valueAxisType )
  {
    final IAxis valueAxis = AxisUtils.findAxis( container.getAxes(), valueAxisType );

    return new TupleModelDataSet( valueAxis, 0.0, KalypsoStati.BIT_OK, IDataSourceItem.SOURCE_MISSING );
  }

  private void apply( final ITupleModelValueContainer container, final TupleModelDataSet value ) throws SensorException
  {
    final IAxis valueAxis = value.getValueAxis();
    final IAxis statusAxis = AxisUtils.findStatusAxis( container.getAxes(), valueAxis );
    final IAxis dataSourceAxis = AxisUtils.findDataSourceAxis( container.getAxes(), valueAxis );

    final DataSourceHandler handler = new DataSourceHandler( m_metadata );

    container.set( valueAxis, value.getValue() );
    container.set( statusAxis, KalypsoStati.BIT_OK );
    container.set( dataSourceAxis, handler.addDataSource( IDataSourceItem.SOURCE_INTERPOLATED_WECHMANN_VALUE, IDataSourceItem.SOURCE_INTERPOLATED_WECHMANN_VALUE ) );
  }

  private boolean isStuetzstelle( final TupleModelDataSet value )
  {
    if( value.getValue() == null )
      return false;

    final boolean stuetzstelle = ZmlValues.isStuetzstelle( value.getStatus(), value.getSource() );

    if( m_handleFilledValuesAsStuetzstelle )
    {
      // value was set before (from psi) -> this is good enough for us
      if( !stuetzstelle && IDataSourceItem.SOURCE_INTERPOLATED_WECHMANN_VALUE.equals( value.getSource() ) )
        return true;
    }

    return stuetzstelle;
  }
}
