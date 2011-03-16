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
package de.openali.odysseus.chart.factory.config;

import java.net.URL;
import java.util.Calendar;

import org.apache.xmlbeans.GDuration;

import de.openali.odysseus.chart.factory.config.parameters.impl.AxisDirectionParser;
import de.openali.odysseus.chart.factory.config.parameters.impl.AxisPositionParser;
import de.openali.odysseus.chart.factory.provider.IAxisProvider;
import de.openali.odysseus.chart.factory.provider.IAxisRendererProvider;
import de.openali.odysseus.chart.factory.util.AxisUtils;
import de.openali.odysseus.chart.factory.util.IReferenceResolver;
import de.openali.odysseus.chart.framework.logging.impl.Logger;
import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.data.IDataOperator;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.data.impl.ComparableDataRange;
import de.openali.odysseus.chart.framework.model.data.impl.DataRangeRestriction;
import de.openali.odysseus.chart.framework.model.exception.ConfigurationException;
import de.openali.odysseus.chart.framework.model.layer.IParameterContainer;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.DIRECTION;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.POSITION;
import de.openali.odysseus.chart.framework.model.mapper.impl.AxisAdjustment;
import de.openali.odysseus.chart.framework.model.mapper.registry.IMapperRegistry;
import de.openali.odysseus.chart.framework.model.mapper.registry.impl.DataOperatorHelper;
import de.openali.odysseus.chart.framework.model.mapper.renderer.IAxisRenderer;
import de.openali.odysseus.chart.framework.model.style.IStyleSet;
import de.openali.odysseus.chartconfig.x020.AxisDateRangeType;
import de.openali.odysseus.chartconfig.x020.AxisDurationRangeType;
import de.openali.odysseus.chartconfig.x020.AxisNumberRangeRestrictionType;
import de.openali.odysseus.chartconfig.x020.AxisNumberRangeType;
import de.openali.odysseus.chartconfig.x020.AxisRendererType;
import de.openali.odysseus.chartconfig.x020.AxisStringRangeType;
import de.openali.odysseus.chartconfig.x020.AxisType;
import de.openali.odysseus.chartconfig.x020.AxisType.PreferredAdjustment;
import de.openali.odysseus.chartconfig.x020.ChartType;
import de.openali.odysseus.chartconfig.x020.ChartType.Mappers;
import de.openali.odysseus.chartconfig.x020.MapperType;
import de.openali.odysseus.chartconfig.x020.PositionType;
import de.openali.odysseus.chartconfig.x020.ReferencableType;
import de.openali.odysseus.chartconfig.x020.ReferencingType;
import de.openali.odysseus.chartconfig.x020.ScreenAxisType;

/**
 * @author Dirk Kuch
 */
public class ChartMapperFactory extends AbstractChartFactory
{
  public ChartMapperFactory( final IChartModel model, final IReferenceResolver resolver, final IExtensionLoader loader, final URL context )
  {
    super( model, resolver, loader, context );
  }

  public void build( final ChartType chartType )
  {
    final Mappers mappers = chartType.getMappers();

    if( mappers != null )
    {
      final AxisType[] axisTypes = mappers.getAxisArray();
      for( final AxisType axisType : axisTypes )
      {
        addAxis( axisType, chartType );
      }

      final ScreenAxisType[] screenAxesTypes = mappers.getScreenAxisArray();
      for( final ScreenAxisType screenAxisType : screenAxesTypes )
      {
        addScreenAxis( screenAxisType );
      }
    }
  }

  /**
   * creates a concrete IAxis-Implementation from an AbstractAxisType derived from a ChartConfiguration, sets the
   * corresponding renderer and adds both to a given Chart
   */
  public IAxis addAxis( final AxisType axisType, final ReferencableType... baseTypes )
  {
    final IMapperRegistry mapperRegistry = getModel().getMapperRegistry();
    if( axisType != null )
    {
      // wenn die Achse schon da ist, dann muss man sie nicht mehr
      // erzeugen
      if( mapperRegistry.getAxis( axisType.getId() ) != null )
        return null;

      final String axisProviderId = axisType.getProvider().getEpid();
      if( axisProviderId != null && !axisProviderId.trim().isEmpty() )
        try
        {
          final IAxisProvider axisProvider = getLoader().getExtension( IAxisProvider.class, axisProviderId );
          if( axisProvider != null )
          {
            final String axisId = axisType.getId();
            final POSITION axisPosition = getAxisPosition( axisType.getPosition() );
            final IParameterContainer container = createParameterContainer( axisId, axisType.getProvider() );
            final Class< ? > dataClass = getAxisDataClass( axisType );

            String[] valueList = null;
            if( axisType.isSetStringRange() )
              valueList = axisType.getStringRange().getValueSet().getValueArray();

            axisProvider.init( getModel(), axisId, container, getContext(), dataClass, axisPosition, valueList );

            final IAxis axis = axisProvider.getAxis();
            axis.setData( ChartFactory.AXIS_PROVIDER_KEY, axisProvider ); // Provider in Element setzen - fürs speichern
// benötigt
            axis.setData( CONFIGURATION_TYPE_KEY, axisType ); // save configuration type so it can be used for saving to
// chartfile
            axis.setDirection( getAxisDirection( axisType ) );
            axis.setLabel( axisType.getLabel() );
            axis.setPreferredAdjustment( getAxisAdjustment( axisType ) );
            axis.setNumericRange( getAxisRange( axis, axisType ) );
            axis.setRangeRestriction( getRangeRestriction( axisType ) );
            axis.setVisible( axisType.getVisible() );

            mapperRegistry.addMapper( axis );

            /**
             * Renderer nur erzeugen, wenn es noch keinen für die Achse gibt
             */
            final ReferencingType rendererRef = axisType.getRendererRef();
            IAxisRenderer axisRenderer = findRenderer( mapperRegistry.getAxes(), AxisUtils.getIdentifier( rendererRef ) );

            if( axisRenderer != null )
            {
              /** exists? so assign axis renderer */
              axis.setRenderer( axisRenderer );
            }
            else
            {
              final AxisRendererType rendererType = (AxisRendererType) getResolver().resolveReference( AxisUtils.getIdentifier( rendererRef ) );
              if( rendererType != null )
              {
                final String providerId = rendererType.getProvider().getEpid();
                final IAxisRendererProvider axisRendererProvider;
                // Hack due older kod-files with this malformed renderer-id still exists
                if( "de.openali.odysseus.chart.ext.test.axisrenderer.provider.GenericNumberAxisRendererProvider".equals( providerId ) ) //$NON-NLS-1$
                  axisRendererProvider = getLoader().getExtension( IAxisRendererProvider.class, "de.openali.odysseus.chart.ext.base.axisrenderer.provider.GenericNumberAxisRendererProvider" ); //$NON-NLS-1$
                else
                  axisRendererProvider = getLoader().getExtension( IAxisRendererProvider.class, providerId );
                final String rendererTypeId = rendererType.getId();
                // TODO global style set
                final IStyleSet styleSet = StyleFactory.createStyleSet( rendererType.getStyles(), baseTypes, getContext() );
                final IParameterContainer parameterContainer = createParameterContainer( rendererTypeId, rendererType.getProvider() );

                // // Hack to get rid of older kod-files with this malformed renderer-id
                // if(
// "de.openali.odysseus.chart.ext.test.axisrenderer.provider.GenericNumberAxisRendererProvider".equals( arpId ) )
                // arp.init( model,
// "de.openali.odysseus.chart.ext.base.axisrenderer.provider.GenericNumberAxisRendererProvider", rpc,
                // context, styleSet );
                // else

                axisRendererProvider.init( getModel(), rendererTypeId, parameterContainer, getContext(), styleSet );

                try
                {
                  axisRenderer = axisRendererProvider.getAxisRenderer();
                  axisRenderer.setData( ChartFactory.AXISRENDERER_PROVIDER_KEY, axisRendererProvider );
                  // save configuration type so it can be used for saving to chart file
                  axisRenderer.setData( CONFIGURATION_TYPE_KEY, rendererType );

                  axis.setRenderer( axisRenderer );
                }
                catch( final ConfigurationException e )
                {
                  e.printStackTrace();
                }
              }
            }

            return axis;
          }
          else
            Logger.logError( Logger.TOPIC_LOG_CONFIG, "Axis could not be created. EPID was: " + axisProviderId );

        }
        catch( final ConfigurationException e )
        {
          e.printStackTrace();
        }
      else
        Logger.logError( Logger.TOPIC_LOG_CONFIG, "AxisProvider " + axisProviderId + " not known" );
    }
    else
      Logger.logError( Logger.TOPIC_LOG_GENERAL, "AxisFactory: given axis is NULL." );

    return null;
  }

  public IAxis addScreenAxis( final ScreenAxisType screenAxisType )
  {
    final IMapperRegistry mapperRegistry = getModel().getMapperRegistry();
    if( screenAxisType != null )
    {
      /* screen axis already exists? */
      if( mapperRegistry.getAxis( screenAxisType.getId() ) != null )
        return null;

      final String providerId = screenAxisType.getProvider().getEpid();
      if( providerId != null && !providerId.trim().isEmpty() )
      {
        final IAxisProvider provider = getLoader().getExtension( IAxisProvider.class, providerId );

        final IAxis screenAxis = provider.getScreenAxis( screenAxisType.getId(), getAxisPosition( screenAxisType.getPosition() ) );
        mapperRegistry.addMapper( screenAxis );

        return screenAxis;
      }
    }

    return null;
  }

  private DIRECTION getAxisDirection( final AxisType at )
  {
    final AxisDirectionParser app = new AxisDirectionParser();
    final String direction = at.getDirection().toString();
    final DIRECTION dir = app.stringToLogical( direction );
    return dir;
  }

  private POSITION getAxisPosition( final PositionType.Enum positionType )
  {
    final AxisPositionParser app = new AxisPositionParser();
    final String position = positionType.toString();
    final POSITION pos = app.stringToLogical( position );

    return pos;
  }

  private AxisAdjustment getAxisAdjustment( final AxisType at )
  {
    AxisAdjustment aa = null;
    if( at.isSetPreferredAdjustment() )
    {
      final PreferredAdjustment pa = at.getPreferredAdjustment();

      aa = new AxisAdjustment( pa.getBefore(), pa.getRange(), pa.getAfter(),pa.getFixMinRange()==null?0.0:pa.getFixMinRange(),pa.getFixMaxRange()==null?Double.MAX_VALUE: pa.getFixMaxRange());
    }
    else
      aa = new AxisAdjustment( 0, 1, 0 );
    return aa;
  }

  private DataRangeRestriction<Number> getRangeRestriction( final AxisType at )
  {
    if( at.isSetAxisNumberRangeRestriction() )
    {
      final AxisNumberRangeRestrictionType rangeType = at.getAxisNumberRangeRestriction();
      final Number min = rangeType.getAbsoluteMinValue();
      final Number max = rangeType.getAbsoluteMaxValue();
      final Number rangeMin = rangeType.getMinRange();
      final Number rangeMax = rangeType.getMaxRange();
      final boolean fixMinValue = rangeType.getFixMinValue();
      final boolean fixMaxValue = rangeType.getFixMaxValue();
      return new DataRangeRestriction<Number>( min == null ? -Double.MAX_VALUE : min, max == null ? Double.MAX_VALUE : max, rangeMin == null ? 0.0 : rangeMin, rangeMax == null ? Double.MAX_VALUE
          : rangeMax, fixMinValue, fixMaxValue );
    }
    else if( at.isSetAxisDateRangeRestriction() )
    {
      // TODO:return new DataRangeRestriction<Number>( ?,?,?,?);
      return new DataRangeRestriction<Number>( null, null, null, null, false, false );
    }
    else
      return null;
  }

  /**
   * creates the axis range from the xml element
   */
  private IDataRange<Number> getAxisRange( final IAxis axis, final AxisType at )
  {
    final Number min;
    final Number max;
    final DataOperatorHelper dataOperatorHelper = new DataOperatorHelper();

    if( at.isSetDateRange() )
    {
      final AxisDateRangeType range = at.getDateRange();
      final IDataOperator<Calendar> dataOperator = axis.getDataOperator( Calendar.class );
      final Calendar minValue = range.getMinValue();
      min = dataOperator.logicalToNumeric( minValue );
      final Calendar maxValue = range.getMaxValue();
      max = dataOperator.logicalToNumeric( maxValue );
    }
    else if( at.isSetNumberRange() )
    {
      final AxisNumberRangeType range = at.getNumberRange();
      min = range.getMinValue();
      max = range.getMaxValue();
    }
    else if( at.isSetStringRange() )
    {
      final AxisStringRangeType range = at.getStringRange();
      min = range.getMinValue();
      max = range.getMaxValue();
    }
    else if( at.isSetDurationRange() )
    {
      final AxisDurationRangeType range = at.getDurationRange();
      final IDataOperator<Calendar> dataOperator = dataOperatorHelper.getDataOperator( Calendar.class );// axis.getDataOperator(
// Calendar.class );
      final GDuration minDur = range.getMinValue();
      final Calendar now = Calendar.getInstance();
      final Calendar minValue = addDurationToCal( now, minDur );
      min = dataOperator.logicalToNumeric( minValue );
      final GDuration maxDur = range.getMaxValue();
      final Calendar maxValue = addDurationToCal( now, maxDur );
      max = dataOperator.logicalToNumeric( maxValue );
    }
    else
    {
      min = null;
      max = null;
    }
    final IDataRange<Number> range = new ComparableDataRange<Number>( new Number[] { min, max } );
    return range;
  }

  private Calendar addDurationToCal( final Calendar cal, final GDuration dur )
  {
    final int sign = dur.getSign();
    cal.add( Calendar.YEAR, sign * dur.getYear() );
    cal.add( Calendar.MONTH, sign * dur.getMonth() );
    cal.add( Calendar.DAY_OF_MONTH, sign * dur.getDay() );
    cal.add( Calendar.HOUR_OF_DAY, sign * dur.getHour() );
    cal.add( Calendar.MINUTE, sign * dur.getMinute() );
    cal.add( Calendar.SECOND, sign * dur.getSecond() );
    cal.add( Calendar.MILLISECOND, (int) (sign * dur.getFraction().doubleValue()) );
    return cal;
  }

  public Class< ? > getAxisDataClass( final AxisType at )
  {
    if( at.isSetDateRange() || at.isSetDurationRange() )
      return Calendar.class;
    else if( at.isSetStringRange() )
      return String.class;
    else
      return Number.class;
  }

  private IAxisRenderer findRenderer( final IAxis[] axes, final String rendererID )
  {
    for( final IAxis axis : axes )
    {
      final IAxisRenderer renderer = axis.getRenderer();
      if( renderer != null && renderer.getId().equals( rendererID ) )
        return renderer;
    }
    return null;
  }

  public void addMapper( final MapperType type, final ReferencableType... baseTypes )
  {
    if( type instanceof AxisType )
      addAxis( (AxisType) type, baseTypes );
    else if( type instanceof ScreenAxisType )
      addScreenAxis( (ScreenAxisType) type );
  }
}
