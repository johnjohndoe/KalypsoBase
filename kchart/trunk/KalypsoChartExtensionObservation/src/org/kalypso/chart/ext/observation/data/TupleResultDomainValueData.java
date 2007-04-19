package org.kalypso.chart.ext.observation.data;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import javax.xml.datatype.XMLGregorianCalendar;

import org.kalypso.chart.framework.logging.Logger;
import org.kalypso.chart.framework.model.data.IDataContainer;
import org.kalypso.chart.framework.model.data.IDataRange;
import org.kalypso.chart.framework.model.data.impl.ComparableDataRange;
import org.kalypso.chart.framework.util.math.ComparableNumber;
import org.kalypso.observation.IObservation;
import org.kalypso.observation.result.IComponent;
import org.kalypso.observation.result.IRecord;
import org.kalypso.observation.result.TupleResult;
import org.kalypso.ogc.gml.om.ObservationFeatureFactory;
import org.kalypso.ogc.gml.serialize.GmlSerializer;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.GMLWorkspace;

public class TupleResultDomainValueData<T_domain extends Comparable, T_target extends Comparable> implements IDataContainer<T_domain, T_target> {

	
	final private String m_observationId;
	final private String m_domainComponentName;
	final private String m_targetComponentName;
	final private String m_href;
	final private URL m_context;
	private TupleResult m_result;
	private IComponent m_domainComponent;
	private IComponent m_targetComponent;
	private List<T_domain> m_domainValues;
	private List<T_target> m_targetValues;
	private IObservation<TupleResult> m_observation;
	private boolean m_isOpen=false;


	public TupleResultDomainValueData(URL context, String href, String observationId, String domainComponentName, String targetComponentName)
	{
		m_context=context;
		m_href=href;
		m_observationId=observationId;
		m_domainComponentName=domainComponentName;
		m_targetComponentName=targetComponentName;
	}
	

	/**
	 * sets all dynamically created data variables to null and m_isOpen to false;
	 */
	public void close() {
		m_result=null;
		m_domainComponent=null;
		m_targetComponent=null;
		m_domainValues=null;
		m_targetValues=null;
		m_observation=null;
		m_isOpen=false;
	}

	public boolean isOpen() {
		return m_isOpen;
	}

	public void open() {
		  if (!m_isOpen)
		  {
		      try
		      {
		        GMLWorkspace workspace = GmlSerializer.createGMLWorkspace( new URL(m_context, m_href), null );
		        Feature feature = workspace.getFeature( m_observationId );
		        if( feature != null )
		        {
		          Logger.trace( "Found feature: " + feature.getId() );
		        }
		        m_observation = ObservationFeatureFactory.toObservation( feature );
	
	
		        m_result = m_observation.getResult();
		
		          // ResultComponent rausfinden
		          final IComponent[] comps = m_result.getComponents();
		          final TreeMap<String, IComponent> map = new TreeMap<String, IComponent>();
		          for( int i = 0; i < comps.length; i++ )
		          {
		            IComponent comp = comps[i];
		            Logger.trace( "Got component: " + comp.getName() );
		            if( comp.getName().compareTo( m_domainComponentName ) == 0 )
		            	m_domainComponent=comp;
		            else if( comp.getName().compareTo( m_targetComponentName ) == 0 )
		            	m_targetComponent=comp;
		            if( map.size() == 2 )
		              break;
		          }
		          
		          if (m_domainComponent != null && m_targetComponent!= null)
		          {
		          
			          //Value-Listen aufbauen
			          m_domainValues=new ArrayList<T_domain>();
			          m_targetValues=new ArrayList<T_target>();
			          for (int i=0; i<m_result.size();i++)
			          {
			        	  IRecord record = m_result.get(i);
			        	  /**
			        	   * Hier muss ich prüfen, ob das ein XMLGregorianCalendar ist und ihn bei Bedarf in 
			        	   * einen Calendar umwandeln; Problem ist, dass der XMLGregorianCalendar nicht 
			        	   * Comparable implementiert
			        	   * 
			        	   * Außerdem müssen Numbers in ComparableNumbers umgewandelt werden, damit sie mit der 
			        	   * ComparableNumberAxis verwendet werden können 
			        	   */
			        	  T_domain domainValue=null;
			        	  Object domainValueObj=record.getValue(m_domainComponent);
			        	  if (domainValueObj instanceof XMLGregorianCalendar)
			        		  domainValue= (T_domain) ((XMLGregorianCalendar) domainValueObj).toGregorianCalendar();
			        	  else if (domainValueObj instanceof Number)
			        		  domainValue= (T_domain) (new ComparableNumber((Number) domainValueObj));
			        	  else
			        		  domainValue= (T_domain) domainValueObj;
			        	  
			        	  
			        	  
			        	  T_target targetValue=null;
			        	  Object targetValueObj=record.getValue(m_targetComponent);
			        	  if (targetValueObj instanceof XMLGregorianCalendar)
			        		  targetValue= (T_target) ((XMLGregorianCalendar) targetValueObj).toGregorianCalendar();
			        	  else if (targetValueObj instanceof Number)
			        		  targetValue= (T_target) (new ComparableNumber((Number) targetValueObj));
			        	  else
			        		  targetValue= (T_target) targetValueObj;
			        		  
			        	  m_domainValues.add(domainValue);
			        	  m_targetValues.add(targetValue);
			          }
		          }
		      }
		      catch( MalformedURLException e )
		      {
		        // TODO Auto-generated catch block
		        e.printStackTrace();
		      }
		      catch( Exception e )
		      {
		        // TODO Auto-generated catch block
		        e.printStackTrace();
		      }
		  }
	      m_isOpen=true;
	}


	public IDataRange<T_domain> getDomainRange() {
		return new ComparableDataRange<T_domain>(m_domainValues);
	}


	public IDataRange<T_target> getTargetRange() {
		return new ComparableDataRange<T_target>(m_targetValues);
	}

	public List getDomainValues()
	{
		return m_domainValues;
	}
	
	
	public List getTargetValues()
	{
		return m_targetValues;
	}
	
	public TupleResult getTupleResult()
	{
		return m_result;
	}
	
}
