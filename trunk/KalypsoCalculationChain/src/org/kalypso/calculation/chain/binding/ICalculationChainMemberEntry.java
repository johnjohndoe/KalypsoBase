package org.kalypso.calculation.chain.binding;

import javax.xml.namespace.QName;

import org.kalypso.calculation.chain.ModelConnectorUrlCatalog;
import org.kalypsodeegree.model.feature.binding.IFeatureWrapper2;

public interface ICalculationChainMemberEntry extends IFeatureWrapper2
{
  public final static QName QNAME = new QName( ModelConnectorUrlCatalog.NS_CCHAIN, "Entry" );

  public final static QName QNAME_PROP_KEY = new QName( ModelConnectorUrlCatalog.NS_CCHAIN, "key" );

  public final static QName QNAME_PROP_VALUE = new QName( ModelConnectorUrlCatalog.NS_CCHAIN, "value" );

  public final static QName QNAME_PROP_IS_RELATIVE_TO_CALC_CASE = new QName( ModelConnectorUrlCatalog.NS_CCHAIN, "isRelativeToCalculationCase" );

  public final static QName QNAME_PROP_IS_OPTIONAL = new QName( ModelConnectorUrlCatalog.NS_CCHAIN, "isOptional" );

  public void setKey( final String value );

  public String getKey( );

  public void setValue( final String value );

  public String getValue( );

  public void setOptional( final boolean value );

  public boolean isOptional( );

  public void setRelativeToCalculationCase( final boolean value );

  public boolean isRelativeToCalculationCase( );
}
