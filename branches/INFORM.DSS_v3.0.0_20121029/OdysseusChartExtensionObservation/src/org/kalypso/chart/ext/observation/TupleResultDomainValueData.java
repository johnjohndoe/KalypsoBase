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
 *  ---------------------------------------------------------------------------*/package org.kalypso.chart.ext.observation;

import java.net.URL;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import org.kalypso.observation.IObservation;
import org.kalypso.observation.result.IComponent;
import org.kalypso.observation.result.IRecord;
import org.kalypso.observation.result.ITupleResultChangedListener;
import org.kalypso.observation.result.TupleResult;
import org.kalypso.ogc.gml.om.ObservationFeatureFactory;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree_impl.model.feature.FeatureHelper;

import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.data.DataRange;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.layer.IParameterContainer;

/**
 * Provides access to the observation data. Constructed from {@link IParameterContainer}.<br/>
 * The components of the underlying observatio are defined by the parameters:
 * <ul>
 * <li>domainComponentId</li>
 * <li>targetComponentId</li>
 * </ul>
 * <br/>
 * There are several ways to access the observation:<br/>
 * Load from an external gml:
 * <ul>
 * <li>href: ocation of the gml resource, resolved via the given context URL.</li>
 * <li>observationId: feature id within that gml, must be an observation feature.</li>
 * <li></li>
 * </ul>
 * From an already existing feature, provide via the chart model:
 * <ul>
 * <li>featureKey: String. Key, where to get the feature from the ChartModel.</li>
 * <li>propertyName: QName. If non null, the observation feature is found at that property of the given feature. Else the given feature must be an observation itself.</li>
 * </ul>
 *
 * @author Gernot
 */
public class TupleResultDomainValueData<T_domain, T_target>
{
  private static final String PARAMETER_DOMAIN_COMPONENT_ID = "domainComponentId"; //$NON-NLS-1$

  private static final String PARAMETER_TARGET_COMPONENT_ID = "targetComponentId"; //$NON-NLS-1$

  /**
   * @deprecated use targetComponentId instead. Still present for backwards compatibility.
   */
  @Deprecated
  private static final String PARAMETER_VALUE_COMPONENT_ID = "valueComponentId"; //$NON-NLS-1$

  private static final String PARAMETER_FEATURE_KEY = "featureKey"; //$NON-NLS-1$

  private static final String PARAMETER_PROPERTY_NAME = "propertyName"; //$NON-NLS-1$

  private static final String PARAMETER_HREF = "href"; //$NON-NLS-1$

  private static final String PARAMETER_OBSERVATION_ID = "observationId"; //$NON-NLS-1$

  private final ITupleResultChangedListener m_changeListener = new ITupleResultChangedListener()
  {
    @Override
    public void valuesChanged( final ValueChange[] changes )
    {
      handleObservationChanged();
    }

    @Override
    public void recordsChanged( final IRecord[] records, final TYPE type )
    {
      handleObservationChanged();
    }

    @Override
    public void componentsChanged( final IComponent[] components, final TYPE type )
    {
      handleObservationChanged();
    }
  };

  final private String m_domainComponentName;

  final private String m_targetComponentName;

  private IObservation<TupleResult> m_observation;

  private boolean m_isOpen = false;

  private final IObservationProvider m_provider;

  private TupleResultLineLayer m_layer;

  public TupleResultDomainValueData( final IObservationProvider provider, final String domainComponentName, final String targetComponentName )
  {
    m_provider = provider;
    m_domainComponentName = domainComponentName;
    m_targetComponentName = targetComponentName;
  }

  void setLayer( final TupleResultLineLayer layer )
  {
    m_layer = layer;
  }

  public IObservation<TupleResult> getObservation( )
  {
    open();

    return m_observation;
  }

  private void open( )
  {
    if( m_isOpen )
      return;

    m_observation = m_provider.getObservation();

    if( m_observation != null )
      m_observation.getResult().addChangeListener( m_changeListener );

    // FIXME: add listener and update layer if observation changes

    m_isOpen = true;
  }

  public IDataRange<T_domain> getDomainRange( )
  {
    final T_domain[] domainValues = getDomainValues();

    return DataRange.createFromComparable( domainValues );
  }

  public IDataRange<T_target> getTargetRange( )
  {
    return DataRange.createFromComparable( getTargetValues() );
  }

  public T_domain[] getDomainValues( )
  {
    return (T_domain[])getValues( m_domainComponentName );
  }

  // TODO: bad, against the philosophy fof the chart layers: instead, iterate through values!
  private Object[] getValues( final String compName )
  {
    open();

    if( !m_isOpen || m_observation == null )
      return new Object[] {};

    final int iComp = m_observation.getResult().indexOfComponent( compName );
    if( iComp < 0 )
      return new Object[] {};

    final Object[] objArr = new Object[m_observation.getResult().size()];
    int index = 0;

    for( final IRecord record : m_observation.getResult() )
      objArr[index++] = getValue( record, iComp );

    return objArr;
  }

  public Object getValue( final IRecord record, final int componentIndex )
  {
    final Object objVal = record.getValue( componentIndex );
    if( objVal instanceof XMLGregorianCalendar )
      return ((XMLGregorianCalendar)objVal).toGregorianCalendar();

    return objVal;
  }

  public Object getDomainValue( final IRecord record )
  {
    final int index = getDomainComponentIndex();
    if( index == -1 )
      return null;

    return getValue( record, index );
  }

  public Object getTargetValue( final IRecord record )
  {
    final int index = getTargetComponentIndex();
    if( index == -1 )
      return null;

    return getValue( record, index );
  }

  public T_target[] getTargetValues( )
  {
    return (T_target[])getValues( m_targetComponentName );
  }

  public void close( )
  {
    m_provider.dispose();

    if( m_observation != null )
    {
      m_observation.getResult().removeChangeListener( m_changeListener );
      m_observation = null;
    }

    m_isOpen = false;
    m_layer = null;
  }

  public boolean isOpen( )
  {
    return m_isOpen;
  }

  public String getDomainComponentName( )
  {
    return m_domainComponentName;
  }

  public String getTargetComponentName( )
  {
    return m_targetComponentName;
  }

  protected void handleObservationChanged( )
  {
    if( m_layer != null )
      m_layer.onObservationChanged();
  }

  private int getComponentIndex( final String componentID )
  {
    final IObservation<TupleResult> observation = getObservation();
    if( observation == null )
      return -1;

    final TupleResult result = observation.getResult();
    return result.indexOfComponent( componentID );
  }

  // TODO: cache
  int getDomainComponentIndex( )
  {
    return getComponentIndex( m_domainComponentName );
  }

  // TODO: cache
  int getTargetComponentIndex( )
  {
    return getComponentIndex( m_targetComponentName );
  }

  private IComponent getComponent( final String componentID )
  {
    final IObservation<TupleResult> observation = getObservation();
    if( observation == null )
      return null;

    final int componentIndex = getComponentIndex( componentID );
    if( componentIndex == -1 )
      return null;

    final TupleResult result = observation.getResult();
    return result.getComponent( componentIndex );
  }

  // TODO: cache
  public IComponent getDomainComponent( )
  {
    return getComponent( m_domainComponentName );
  }

  // TODO: cache
  public IComponent getTargetComponent( )
  {
    return getComponent( m_targetComponentName );
  }

  public static TupleResultDomainValueData< ? , ? > fromContainer( final IParameterContainer pc, final URL context, final IChartModel model )
  {
    // REMARK: parameter name used in old, fifferent layer provider implementation; should not be used any more
    final String valueComponentId = pc.getParameterValue( PARAMETER_VALUE_COMPONENT_ID, null );

    final String targetComponentId = pc.getParameterValue( PARAMETER_TARGET_COMPONENT_ID, valueComponentId );
    final String domainComponentId = pc.getParameterValue( PARAMETER_DOMAIN_COMPONENT_ID, null );
    if( domainComponentId == null || targetComponentId == null )
      return null;

    final IObservationProvider provider = getObservationProvider( pc, context, model );
    if( provider == null )
      return null;

    return new TupleResultDomainValueData<>( provider, domainComponentId, targetComponentId );
  }

  private static IObservationProvider getObservationProvider( final IParameterContainer pc, final URL context, final IChartModel model )
  {
    final IObservation<TupleResult> observation = getObservationByFeatureKey( pc, model );
    if( observation != null )
      return new DefaultObservationProvider( observation );

    // try to find loaded observation (from GFT)
    final String href = pc.getParameterValue( PARAMETER_HREF, null ); //$NON-NLS-1$
    final String observationId = pc.getParameterValue( PARAMETER_OBSERVATION_ID, null ); //$NON-NLS-1$
    if( href == null || observationId == null )
      return null;

    return new GmlWorkspaceObservationProvider( context, href, observationId );
  }

  private static IObservation<TupleResult> getObservationByFeatureKey( final IParameterContainer pc, final IChartModel model )
  {
    final String featureKey = pc.getParameterValue( PARAMETER_FEATURE_KEY, null ); //$NON-NLS-1$
    final String propertyNameStr = pc.getParameterValue( PARAMETER_PROPERTY_NAME, null ); //$NON-NLS-1$
    final QName propertyName = propertyNameStr == null ? null : QName.valueOf( propertyNameStr );

    final Feature baseFeature = (Feature)model.getData( featureKey );
    final Feature feature;
    if( propertyName == null )
    {
      feature = baseFeature;
    }
    else if( baseFeature != null )
    {
      feature = FeatureHelper.getFeature( baseFeature.getWorkspace(), baseFeature.getProperty( propertyName ) );
    }
    else
      feature = null;

    if( feature == null )
      return null;

    return ObservationFeatureFactory.toObservation( feature );
  }
}