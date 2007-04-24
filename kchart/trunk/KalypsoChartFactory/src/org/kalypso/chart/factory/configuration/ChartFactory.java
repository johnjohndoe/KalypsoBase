package org.kalypso.chart.factory.configuration;

import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.kalypso.chart.factory.ChartExtensionLoader;
import org.kalypso.chart.factory.configuration.exception.AxisProviderException;
import org.kalypso.chart.factory.configuration.exception.AxisRendererProviderException;
import org.kalypso.chart.factory.configuration.exception.ConfigChartNotFoundException;
import org.kalypso.chart.factory.configuration.exception.ConfigurationException;
import org.kalypso.chart.factory.configuration.exception.LayerProviderException;
import org.kalypso.chart.factory.configuration.exception.MapperProviderException;
import org.kalypso.chart.factory.configuration.exception.StyledElementProviderException;
import org.ksp.chart.factory.AxisRendererType;
import org.ksp.chart.factory.AxisType;
import org.ksp.chart.factory.ChartType;
import org.ksp.chart.factory.LayerType;
import org.ksp.chart.factory.MapperRefType;
import org.ksp.chart.factory.MapperType;
import org.ksp.chart.factory.ProviderType;
import org.ksp.chart.factory.RefType;
import org.ksp.chart.factory.StyleType;
import org.ksp.chart.factory.ChartType.Layers;
import org.ksp.chart.factory.LayerType.Mapper;
import org.ksp.chart.factory.LayerType.Style;
import org.kalypso.chart.factory.provider.IAxisProvider;
import org.kalypso.chart.factory.provider.IAxisRendererProvider;
import org.kalypso.chart.factory.provider.ILayerProvider;
import org.kalypso.chart.factory.provider.IMapperProvider;
import org.kalypso.chart.factory.provider.IStyledElementProvider;
import org.kalypso.chart.factory.util.ChartFactoryUtilities;
import org.kalypso.chart.factory.util.IReferenceResolver;
import org.kalypso.chart.framework.logging.Logger;
import org.kalypso.chart.framework.model.IChartModel;
import org.kalypso.chart.framework.model.layer.IChartLayer;
import org.kalypso.chart.framework.model.mapper.IAxis;
import org.kalypso.chart.framework.model.mapper.IMapper;
import org.kalypso.chart.framework.model.mapper.registry.IMapperRegistry;
import org.kalypso.chart.framework.model.mapper.renderer.IAxisRenderer;
import org.kalypso.chart.framework.model.styles.IStyledElement;
import org.kalypso.chart.framework.model.styles.impl.LayerStyle;



/**
 * @author alibu
 * Creates a chart object from a configuration
 */
public class ChartFactory
{

	public static void configureChartModel( IChartModel model, ChartConfigurationLoader cl, String configChartName, URL context ) throws ConfigurationException
	{
		  ChartType dt=null;
		  // ChartConfig auslesen
		  if( cl != null )
		  {
			  ChartType[] charts = cl.getCharts();
			  for (ChartType chart : charts) {
				if (chart.getId().equals(configChartName))
				{
					dt=chart;
					break;
				}
			}
		  }
		  if( dt == null )
		  {
			  throw new ConfigChartNotFoundException( configChartName );
		  }
		  else
		  {
			  try
			  {
				  doConfiguration( model, cl, dt, context );
			  }
			  catch (ConfigurationException e)
			  {
				  Logger.logError(Logger.TOPIC_LOG_CONFIG, " ChartModel could not be created");
			  }
		  }
	  }

  @SuppressWarnings("unchecked")
private static void doConfiguration( final IChartModel model, final IReferenceResolver rr, final ChartType chartType, final URL context ) throws ConfigurationException
  {
	  
	 Layers layers = chartType.getLayers();
	 RefType[] layerRefArray = layers.getLayerRefArray();
	 for (RefType layerRef : layerRefArray) {
		String ref = layerRef.getRef();
		LayerType layerType = (LayerType) rr.resolveReference(ref);
		if (layerType!=null)
		{
			  // Achsen hinzufügen
			Mapper mapper = layerType.getMapper();
			AxisType domainAxisType = (AxisType) rr.resolveReference(mapper.getDomainAxisRef().getRef());
			addAxis (model.getMapperRegistry(), rr, domainAxisType);
			AxisType targetAxisType = (AxisType) rr.resolveReference(mapper.getTargetAxisRef().getRef());
			addAxis (model.getMapperRegistry(), rr, targetAxisType);
			
			//Restliche Mapper hinzufügen
			MapperRefType[] mapperRefArray = mapper.getMapperRefArray();
			for (MapperRefType mapperRef : mapperRefArray) 
			{
				MapperType mapperType = (MapperType) rr.resolveReference(mapperRef.getRef());
				if (mapperType != null)
				{
        		  addMapper( model.getMapperRegistry(), mapperType );
				}
			}
			
			 //Styles erzeugen
	          LayerStyle ls=createStyle(layerType, rr);
	          
	         //Layer erzeugen
        	ProviderType provider2 = layerType.getProvider();
			final String providerId = provider2.getEpid();
	          
	          try
	          {
	            final ILayerProvider provider = ChartExtensionLoader.createLayerProvider( providerId );
	            if( provider != null )
	            {
	              Logger.logInfo( Logger.TOPIC_LOG_GENERAL, "LayerProvider loaded:" + provider.getClass().toString() );
	              provider.init( model, layerType );
	              IChartLayer icl = provider.getLayer( context );
	              if( icl != null )
	              {
	                Logger.logInfo(Logger.TOPIC_LOG_CONFIG, "Adding Layer " + icl.getTitle() );
	                icl.setStyle( ls );
	                icl.setId( layerType.getId() );
	                icl.setTitle( layerType.getTitle() );
	                icl.setDescription( layerType.getDescription() );
	                icl.setVisible( layerType.getVisible() );
	                model.getLayerManager().addLayer( icl );
	              }
	            }
	            else
	            {
	              Logger.logError( Logger.TOPIC_LOG_GENERAL, "No LayerProvider for " + providerId );
	            }
	          }
	          catch( final CoreException e )
	          {
	            e.printStackTrace();
	          }
	          catch( final LayerProviderException e )
	          {
	            e.printStackTrace();
	          }
		}
      else
    	  Logger.logWarning(Logger.TOPIC_LOG_CONFIG, "a reference to a layer type could not be resolved: "+layerRef.getRef());
	 }
	 
  }

  /**
   */
  private static void addMapper( final IMapperRegistry ar, final MapperType mapperType )
  {
	  
    if( mapperType != null )
    {
      final String mpId = mapperType.getProvider().getEpid();
      if( mpId != null && mpId.length() > 0 )
      {
        try
        {
          final IMapperProvider mp = ChartExtensionLoader.createMapperProvider( mpId );
          mp.init( mapperType );
          final IMapper mapper = mp.getMapper() ;
          ar.addMapper( mapper );
        }
        catch( CoreException e )
        {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        catch( MapperProviderException e )
        {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
      else
      {
        Logger.logError( Logger.TOPIC_LOG_CONFIG, "AxisProvider " + mpId + " not known" );
      }
    }
    else
    {
      Logger.logError( Logger.TOPIC_LOG_GENERAL, "AxisFactory: given axis is NULL." );
    }
  }
  
  
  /**
   * creates a concrete IAxis-Implementation from an AbstractAxisType derived from a ChartConfiguration, sets the
   * corresponding renderer and adds both to a given Chart
   */
  public static void addAxis( final IMapperRegistry ar, final IReferenceResolver rr, final AxisType axisType )
  {
    if( axisType != null )
    {
      final String apId = axisType.getProvider().getEpid();
      if( apId != null && apId.length() > 0 )
      {
        try
        {
          final IAxisProvider ap = ChartExtensionLoader.createAxisProvider( apId );
          if (ap!=null)
          {
          ap.init( axisType );
          final IAxis axis = ap.getAxis() ;
          axis.setRegistry(ar);
          ar.addMapper( axis );

          if( ar.getRenderer( axis ) == null )
          {
        	RefType rendererRef = axisType.getRendererRef();
        	AxisRendererType rendererType = (AxisRendererType) rr.resolveReference(rendererRef.getRef()); 
        	if (rendererType !=null )
        	{
	        	String arpId = rendererType.getProvider().getEpid();
	        	final IAxisRendererProvider arp = ChartExtensionLoader.createAxisRendererProvider( arpId );
	        	
	        	//Überprüfen, ob Renderer- und Axis-DataClass zusammenpassen
	        	if (ChartFactoryUtilities.isSubclassOf(arp.getDataClass(), ap.getDataClass()))
	        	{
		        	arp.init(rendererType);
		            IAxisRenderer axisRenderer = arp.getAxisRenderer();
		            ar.setRenderer( axis.getIdentifier(), axisRenderer );
		            Logger.trace("Adding AxisRenderer for: "+arp.getClass());
	        	}
	        	else
	        	{
	        		Logger.logError(Logger.TOPIC_LOG_CONFIG, "incompatible renderer for axis: "+arp.getDataClass()+ " != "+ap.getDataClass());
	        	}
        	}
          }
          }
          else
          {
        	  Logger.logError(Logger.TOPIC_LOG_CONFIG, "Axis could not be created. EPID was: "+apId);
          }
        }
        catch( CoreException e )
        {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        catch( AxisProviderException e )
        {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } catch (AxisRendererProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
      }
      else
      {
        Logger.logError( Logger.TOPIC_LOG_CONFIG, "AxisProvider " + apId + " not known" );
      }
    }
    else
    {
      Logger.logError( Logger.TOPIC_LOG_GENERAL, "AxisFactory: given axis is NULL." );
    }
  }
  
  
  
  private static LayerStyle createStyle(LayerType l, IReferenceResolver rr)
  {
	  LayerStyle ls=new LayerStyle();
	  Style style = l.getStyle();
	  if (style!=null)
	  {
		  RefType[] styleRefArray = style.getStyleRefArray();
		  for (RefType styleRef : styleRefArray)
		  {
			  StyleType st=(StyleType) rr.resolveReference(styleRef.getRef());
			  ProviderType provider = st.getProvider();
			  if (provider !=null)
			  {
				  IStyledElementProvider sp=null;
				  try {
					sp = ChartExtensionLoader.createStyledElementProvider(provider.getEpid() );
					if (sp!=null)
					{
						sp.init(st);
						IStyledElement styledElement = sp.getStyledElement();
						ls.add(styledElement);
					}
					
				} catch (CoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (StyledElementProviderException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			  }
			  
		  }
	  }
	  return ls;
  }
}