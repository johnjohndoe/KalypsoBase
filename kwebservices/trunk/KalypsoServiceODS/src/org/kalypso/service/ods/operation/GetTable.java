package org.kalypso.service.ods.operation;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.kalypso.chart.factory.configuration.ChartConfigurationLoader;
import org.kalypso.chart.factory.configuration.ChartFactory;
import org.kalypso.chart.factory.configuration.exception.ConfigurationException;
import org.kalypso.chart.framework.model.IChartModel;
import org.kalypso.chart.framework.model.data.IDataContainer;
import org.kalypso.chart.framework.model.data.ITabularDataContainer;
import org.kalypso.chart.framework.model.impl.ChartModel;
import org.kalypso.chart.framework.model.layer.IChartLayer;
import org.kalypso.chart.framework.model.layer.ILayerManager;
import org.kalypso.service.ods.IODSOperation;
import org.kalypso.service.ods.util.ODSChartManipulation;
import org.kalypso.service.ods.util.ODSConfigurationLoader;
import org.kalypso.service.ods.util.ODSScene;
import org.kalypso.service.ogc.RequestBean;
import org.kalypso.service.ogc.ResponseBean;
import org.kalypso.service.ogc.exception.OWSException;

public class GetTable implements IODSOperation
{

  DateFormat m_dateFormat = null;

  NumberFormat m_numberFormat = null;

  @SuppressWarnings( { "unused", "unchecked" })
  public void operate( RequestBean requestBean, ResponseBean responseBean ) throws OWSException
  {

    final String sceneId = requestBean.getParameterValue( "SCENE" );
    final ODSConfigurationLoader ocl = ODSConfigurationLoader.getInstance();
    final ODSScene scene = ocl.getSceneById( sceneId );
    final ChartConfigurationLoader cl = new ChartConfigurationLoader( scene.getChartConfiguration() );

    final IChartModel model = new ChartModel();
    final String reqName = requestBean.getParameterValue( "NAME" );
    try
    {
      ChartFactory.configureChartModel( model, cl, reqName, null );
    }
    catch( final ConfigurationException e )
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    final ILayerManager lm = model.getLayerManager();
    ODSChartManipulation.setLayerVisibility( lm, requestBean );

    // Formate werden hier gesetzt, damit beim Debuggen mehrere Einstellungen ausprobiert werden können;
    m_dateFormat = new SimpleDateFormat( "yy-MM-dd hh:mm" );
    m_numberFormat = new DecimalFormat();

    final IChartLayer[] layers = lm.getLayers();

    responseBean.setContentType( "text/html" );
    OutputStreamWriter writer = null;
    try
    {
      writer = new OutputStreamWriter( responseBean.getOutputStream() );
      for( final IChartLayer layer : layers )
      {
        if( layer.isVisible() )
          createTable( layer, writer );
      }
      // writer.write(tableHtml);
      writer.close();
    }
    catch( final IOException e )
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  private void createTable( IChartLayer<Object, Object> layer, OutputStreamWriter writer )
  {

    final IDataContainer<Object, Object> dc = layer.getDataContainer();
    if( dc != null && dc instanceof ITabularDataContainer )
    {
      dc.open();
      final ITabularDataContainer<Object, Object> tdc = (ITabularDataContainer<Object, Object>) dc;
      final Object[] dv = tdc.getDomainValues();
      final Object[] tv = tdc.getTargetValues();

      try
      {
        writer.write( "<table class='ods_table'>" );

        // TableHeader
        writer.write( "<tr><td colspan='2'>" + layer.getTitle() + "</td></tr>" );

        // Zellen-Header
        writer.write( "<tr>" );
        writer.write( "<td>" + layer.getDomainAxis().getLabel() + "</td>" );
        writer.write( "<td>" + layer.getTargetAxis().getLabel() + "</td>" );
        writer.write( "</tr>" );

        for( int i = 0; i < dv.length; i++ )
        {
          writer.write( "<tr>" );
          writer.write( "<td>" );
          writeStringValue( dv[i], writer );
          writer.write( "</td>" );
          writer.write( "<td>" );
          writeStringValue( tv[i], writer );
          writer.write( "</td>" );
          writer.write( "</tr>" );
        }
        writer.write( "</table>" );
      }
      catch( final IOException e )
      {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }

  private void writeStringValue( Object value, OutputStreamWriter writer )
  {
    if( value instanceof Calendar )
    {
      try
      {
        writer.write( m_dateFormat.format( ((Calendar) value).getTime() ) );
      }
      catch( final IOException e )
      {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    else if( value instanceof Number )
    {

      try
      {
        writer.write( m_numberFormat.format( (value) ) );
      }
      catch( final IOException e )
      {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }

}
