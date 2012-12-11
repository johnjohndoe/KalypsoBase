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

import java.awt.Insets;
import java.net.URL;
import java.util.Calendar;

import org.apache.xmlbeans.GDuration;

import de.openali.odysseus.chart.factory.config.parameters.impl.AxisPositionParser;
import de.openali.odysseus.chart.factory.config.resolver.ChartTypeResolver;
import de.openali.odysseus.chart.factory.provider.IAxisProvider;
import de.openali.odysseus.chart.factory.provider.IAxisRendererProvider;
import de.openali.odysseus.chart.factory.util.AxisUtils;
import de.openali.odysseus.chart.factory.util.IReferenceResolver;
import de.openali.odysseus.chart.framework.exception.MalformedValueException;
import de.openali.odysseus.chart.framework.logging.impl.Logger;
import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.data.DataRange;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.data.impl.DataRangeRestriction;
import de.openali.odysseus.chart.framework.model.exception.ConfigurationException;
import de.openali.odysseus.chart.framework.model.layer.IParameterContainer;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.DIRECTION;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.POSITION;
import de.openali.odysseus.chart.framework.model.mapper.impl.AxisAdjustment;
import de.openali.odysseus.chart.framework.model.mapper.registry.IAxisRegistry;
import de.openali.odysseus.chart.framework.model.mapper.renderer.IAxisRenderer;
import de.openali.odysseus.chart.framework.model.style.IStyleSet;
import de.openali.odysseus.chart.framework.model.style.ITextStyle;
import de.openali.odysseus.chart.framework.util.img.ChartLabelRendererFactory;
import de.openali.odysseus.chart.framework.util.img.TitleTypeBean;
import de.openali.odysseus.chartconfig.x020.AbstractStyleType;
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
import de.openali.odysseus.chartconfig.x020.DirectionType.Enum;
import de.openali.odysseus.chartconfig.x020.PositionType;
import de.openali.odysseus.chartconfig.x020.ReferencableType;
import de.openali.odysseus.chartconfig.x020.ReferencingType;
import de.openali.odysseus.chartconfig.x020.ScreenAxisType;
import de.openali.odysseus.chartconfig.x020.TextStyleType;
import de.openali.odysseus.chartconfig.x020.TitleType;
//import de.openali.odysseus.chartconfig.x020.MapperType;

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
    final IAxisRegistry mapperRegistry = getModel().getAxisRegistry();
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

            String[] valueList = null;
            if( axisType.isSetStringRange() )
              valueList = axisType.getStringRange().getValueSet().getValueArray();

            axisProvider.init( getModel(), axisId, container, getContext(), axisPosition, valueList );

            final IAxis axis = axisProvider.getAxis();

            // Provider in Element setzen - fürs speichern benötigt
            axis.setData( ChartFactory.AXIS_PROVIDER_KEY, axisProvider );

            // save configuration type so it can be used for saving to chartfile
            axis.setData( CONFIGURATION_TYPE_KEY, axisType );

            axis.setDirection( getAxisDirection( axisType.getDirection() ) );

            final TitleType[] titles = axisType.isSetLabels() ? axisType.getLabels().getTitleTypeArray() : new TitleType[] {};
            final ChartTypeResolver chartTypeResolver = ChartTypeResolver.getInstance();
            for( final TitleType title : titles )
            {
              try
              {
                final AbstractStyleType styleType = chartTypeResolver.findStyleType( title.getStyleref(), getContext() );
                final ITextStyle style = StyleFactory.createTextStyle( (TextStyleType)styleType );
                final TitleTypeBean titleBean = StyleHelper.getTitleTypeBean( axis.getPosition(), title, style );
                axis.addLabel( titleBean );

              }
              catch( final Throwable t )
              {
                t.printStackTrace();
              }
            }
            if( axisType.isSetLabel() )
              axis.addLabel( ChartLabelRendererFactory.getAxisLabelType( axis.getPosition(), axisType.getLabel(), new Insets( 1, 1, 1, 1 ), null ) );
            axis.setPreferredAdjustment( getAxisAdjustment( axisType ) );
            axis.setNumericRange( getAxisRange( axis, axisType ) );
            axis.setRangeRestriction( getRangeRestriction( axisType ) );
            axis.setVisible( axisType.getVisible() );

            mapperRegistry.addAxis( axis );

            final ReferencingType rendererRef = axisType.getRendererRef();
            final AxisRendererType rendererType = (AxisRendererType)getResolver().resolveReference( AxisUtils.getIdentifier( rendererRef ) );
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

              axisRendererProvider.init( getModel(), rendererTypeId, parameterContainer, getContext(), styleSet );

              try
              {
                final IAxisRenderer axisRenderer = axisRendererProvider.getAxisRenderer( axis.getPosition() );
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

            return axis;
          }
          else
            Logger.logError( Logger.TOPIC_LOG_CONFIG, "Axis could not be created. EPID was: " + axisProviderId ); //$NON-NLS-1$

        }
        catch( final ConfigurationException e )
        {
          e.printStackTrace();
        }
      else
        Logger.logError( Logger.TOPIC_LOG_CONFIG, "AxisProvider " + axisProviderId + " not known" ); //$NON-NLS-1$ //$NON-NLS-2$
    }
    else
      Logger.logError( Logger.TOPIC_LOG_GENERAL, "AxisFactory: given axis is NULL." ); //$NON-NLS-1$

    return null;
  }

  public IAxis addScreenAxis( final ScreenAxisType screenAxisType )
  {
    if( screenAxisType == null )
      return null;

    final IAxisRegistry mapperRegistry = getModel().getAxisRegistry();

    /* screen axis already exists? */
    if( mapperRegistry.getAxis( screenAxisType.getId() ) != null )
      return null;

    final String providerId = screenAxisType.getProvider().getEpid();
    if( providerId == null || providerId.trim().isEmpty() )
      return null;

    // FIXME: does the provider really make sense for the screen axis?!

    final IAxisProvider provider = getLoader().getExtension( IAxisProvider.class, providerId );
    if( provider == null )
    {
      System.out.println( "Axisprovider not found : " + providerId );
      return null;
    }
    final IAxis screenAxis = provider.getScreenAxis( screenAxisType.getId(), getAxisPosition( screenAxisType.getPosition() ) );

    // FIXME: what about the 'position' declared in the xml?

    /* DIRECTION */
    screenAxis.setDirection( getAxisDirection( screenAxisType.getDirection() ) );

    mapperRegistry.addAxis( screenAxis );

    return screenAxis;
  }

  private DIRECTION getAxisDirection( final Enum directionType )
  {
    if( directionType == null )
      return DIRECTION.POSITIVE;

    final String direction = directionType.toString();
    return DIRECTION.valueOf( direction );
  }

  private POSITION getAxisPosition( final PositionType.Enum positionType )
  {
    final AxisPositionParser app = new AxisPositionParser();
    final String position = positionType.toString();
    return app.stringToLogical( position );
  }

  private AxisAdjustment getAxisAdjustment( final AxisType at )
  {
    if( at.isSetPreferredAdjustment() )
    {
      final PreferredAdjustment pa = at.getPreferredAdjustment();

      final Number minValue = pa.getFixMinRange() == null ? 0.0 : pa.getFixMinRange();
      final Number maxValue = pa.getFixMaxRange() == null ? Double.MAX_VALUE : pa.getFixMaxRange();

      return new AxisAdjustment( pa.getBefore(), pa.getRange(), pa.getAfter(), minValue, maxValue );
    }
    else
      return new AxisAdjustment( 0, 1, 0 );
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
      return new DataRangeRestriction<>( min == null ? -Double.MAX_VALUE : min, max == null ? Double.MAX_VALUE : max, rangeMin == null ? 0.0 : rangeMin, rangeMax == null ? Double.MAX_VALUE : rangeMax, fixMinValue, fixMaxValue );
    }
    else if( at.isSetAxisDateRangeRestriction() )
    {
      // TODO:return new DataRangeRestriction<Number>( ?,?,?,?);
      return new DataRangeRestriction<>( null, null, null, null, false, false );
    }
    else
      return null;
  }

  /**
   * creates the axis range from the xml element
   */
  @SuppressWarnings( { "unchecked", "rawtypes" } )
  private IDataRange<Double> getAxisRange( final IAxis axis, final AxisType at )
  {
    // final DataOperatorHelper dataOperatorHelper = new DataOperatorHelper();

    if( at.isSetDateRange() )
    {
      final AxisDateRangeType range = at.getDateRange();
      // final IDataOperator<Calendar> dataOperator = axis.getDataOperator( Calendar.class );
      final Calendar minValue = range.getMinValue();
      final Calendar maxValue = range.getMaxValue();

      final Double min = axis.logicalToNumeric( minValue );
      final Double max = axis.logicalToNumeric( maxValue );
      return new DataRange( min, max );// DataRange.createFromComparable( min, max );
    }
    else if( at.isSetNumberRange() )
    {
      final AxisNumberRangeType range = at.getNumberRange();

      final Number min = range.getMinValue();
      final Number max = range.getMaxValue();
      return new DataRange( min, max );// DataRange.createFromComparable( min, max );
    }
    else if( at.isSetStringRange() )
    {
      try
      {
        final AxisStringRangeType range = at.getStringRange();
        // final IDataOperator<Calendar> dataOperator = axis.getDataOperator( Calendar.class );
        final String minValue = range.getMinValue();
        final String maxValue = range.getMaxValue();

        final Double min = axis.logicalToNumeric( axis.xmlStringToLogical( minValue ) );
        final Double max = axis.logicalToNumeric( axis.xmlStringToLogical( maxValue ) );

        return new DataRange<>( min, max );// DataRange.createFromComparable( min, max );
      }
      catch( final MalformedValueException ex )
      {
        ex.printStackTrace();
        return new DataRange( null, null );// DataRange.createFromComparable( null, null );
      }
    }
    else if( at.isSetDurationRange() )
    {
      final AxisDurationRangeType range = at.getDurationRange();
      // final IDataOperator<Calendar> dataOperator = dataOperatorHelper.getDataOperator( Calendar.class );
      final GDuration minDur = range.getMinValue();
      final Calendar now = Calendar.getInstance();
      final Calendar minValue = addDurationToCal( now, minDur );
      final GDuration maxDur = range.getMaxValue();
      final Calendar maxValue = addDurationToCal( now, maxDur );

      final Double min = axis.logicalToNumeric( minValue );
      final Double max = axis.logicalToNumeric( maxValue );
      return new DataRange<>( min, max );
    }
    else
    {
      return new DataRange( null, null );// DataRange.createFromComparable( null, null );
    }
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
    cal.add( Calendar.MILLISECOND, (int)(sign * dur.getFraction().doubleValue()) );
    return cal;
  }

//  public void addMapper( final MapperType type, final ReferencableType... baseTypes )
//  {
//    if( type instanceof AxisType )
//      addAxis( (AxisType)type, baseTypes );
//    else if( type instanceof ScreenAxisType )
//      addScreenAxis( (ScreenAxisType)type );
//  }
}