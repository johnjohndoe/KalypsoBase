package org.kalypso.calculation.chain.binding;

import java.net.MalformedURLException;

import javax.xml.namespace.QName;

import org.apache.commons.httpclient.URIException;
import org.eclipse.core.resources.IContainer;
import org.kalypso.calculation.chain.ModelConnectorUrlCatalog;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree_impl.model.feature.FeatureBindingCollection;

public interface ICalculationChainMember extends Feature, Comparable<ICalculationChainMember>
{
  public final static QName QNAME = new QName( ModelConnectorUrlCatalog.NS_CCHAIN, "Calculation" );

  public final static QName QNAME_PROP_TYPE_ID = new QName( ModelConnectorUrlCatalog.NS_CCHAIN, "typeID" );

  public final static QName QNAME_PROP_ORDINAL_NUMBER = new QName( ModelConnectorUrlCatalog.NS_CCHAIN, "ordinalNumber" );

  public final static QName QNAME_PROP_CALCULATION_CASE_FOLDER = new QName( ModelConnectorUrlCatalog.NS_CCHAIN, "calculationFolder" );

  public final static QName QNAME_PROP_USE_ANT_LAUNCHER = new QName( ModelConnectorUrlCatalog.NS_CCHAIN, "useAntLauncher" );

  public final static QName QNAME_PROP_INPUTS = new QName( ModelConnectorUrlCatalog.NS_CCHAIN, "input" );

  public final static QName QNAME_PROP_OUTPUTS = new QName( ModelConnectorUrlCatalog.NS_CCHAIN, "output" );

  public FeatureBindingCollection<ICalculationChainMemberEntry> getInputs( );

  public FeatureBindingCollection<ICalculationChainMemberEntry> getOutputs( );

  public void addInput( final ICalculationChainMemberEntry entry );

  public void addOutput( final ICalculationChainMemberEntry entry );

  public void setTypeID( final String value );

  public String getTypeID( );

  public int getOrdinalNumber( );

  public void setOrdinalNumber( final int value );

  public boolean getUseAntLauncher( );

  public void setUseAntLauncher( final boolean value );
  
  public IContainer getCalculationCaseFolder( ) throws URIException, MalformedURLException;

  public void setCalculationCaseFolder( IContainer container ) throws URIException;
}
