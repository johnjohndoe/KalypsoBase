/*--------------- Kalypso-Header ----------------------------------------

 This file is part of kalypso.
 Copyright (C) 2004, 2005 by:

 University of Technology Hamburg-Harburg (TUHH)
 Institute of River and Coastal Engineering
 Denickestr. 22
 21073 Hamburg, Germany
 http://www.tuhh.de/wb

 and

 Bjoernsen Consulting Engineers (BCE)
 Maria Trost 3
 56070 Koblenz, Germany
 http://www.bjoernsen.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

 Contact:

 E-Mail:
 g.belger@bjoernsen.de
 m.schlienger@bjoernsen.de
 v.doemming@tuhh.de

 ------------------------------------------------------------------------*/
package org.kalypso.ogc.gml.gui;

import java.text.ParseException;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.eclipse.jface.viewers.LabelProvider;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.types.IMarshallingTypeHandler;
import org.kalypso.gmlschema.types.MarshallingTypeRegistrySingleton;
import org.kalypso.ogc.gml.featureview.IFeatureChangeListener;
import org.kalypso.ogc.gml.featureview.IFeatureModifier;
import org.kalypso.ogc.gml.featureview.dialog.IFeatureDialog;
import org.kalypso.ogc.gml.featureview.dialog.TimeserieLinkFeatureDialog;
import org.kalypso.ogc.gml.featureview.modfier.ButtonModifier;
import org.kalypso.ogc.gml.selection.IFeatureSelectionManager;
import org.kalypso.ogc.sensor.deegree.ObservationLinkHandler;
import org.kalypso.template.featureview.Button;
import org.kalypso.template.featureview.CompositeType;
import org.kalypso.template.featureview.ControlType;
import org.kalypso.template.featureview.GridDataType;
import org.kalypso.template.featureview.GridLayout;
import org.kalypso.template.featureview.ObjectFactory;
import org.kalypso.template.featureview.Text;
import org.kalypso.zml.obslink.TimeseriesLinkType;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree_impl.model.feature.gmlxpath.GMLXPath;

/**
 * @author bce
 */
public class TimeseriesLinkGuiTypeHandler extends LabelProvider implements IGuiTypeHandler
{
  @Override
  public IFeatureDialog createFeatureDialog( final Feature feature, final IPropertyType ftp )
  {
    return new TimeserieLinkFeatureDialog( feature, ftp );
  }

  @Override
  public Class< ? > getValueClass( )
  {
    return TimeseriesLinkType.class;
  }

  @Override
  public QName getTypeName( )
  {
    return ObservationLinkHandler.PROPERTY_NAME;
  }

  @Override
  public String getText( final Object element )
  {
    if( element == null )
      return ""; //$NON-NLS-1$

    final String href = ((TimeseriesLinkType) element).getHref();
    return href == null ? "" : href; //$NON-NLS-1$
  }

  @Override
  public JAXBElement< ? extends ControlType> createFeatureviewControl( final IPropertyType property, final ObjectFactory factory )
  {
    final CompositeType composite = factory.createCompositeType();

    final GridLayout layout = factory.createGridLayout();
    layout.setNumColumns( 2 );
    layout.setMakeColumnsEqualWidth( false );
    layout.setMarginWidth( 0 );
    composite.setLayout( factory.createGridLayout( layout ) );
    composite.setStyle( "SWT.NONE" ); //$NON-NLS-1$

    // Text
    final Text text = factory.createText();
    text.setStyle( "SWT.BORDER" ); //$NON-NLS-1$
    text.setProperty( property.getQName() );

    final GridDataType textData = factory.createGridDataType();
    textData.setHorizontalAlignment( "GridData.FILL" ); //$NON-NLS-1$
    textData.setGrabExcessHorizontalSpace( Boolean.TRUE );
    text.setLayoutData( factory.createGridData( textData ) );

    // Knopf
    final Button button = factory.createButton();
    final GridDataType buttonData = factory.createGridDataType();
    button.setStyle( "SWT.PUSH" ); //$NON-NLS-1$
    button.setProperty( property.getQName() );

    buttonData.setHorizontalAlignment( "GridData.BEGINNING" ); //$NON-NLS-1$
    button.setLayoutData( factory.createGridData( buttonData ) );

    final List<JAXBElement< ? extends ControlType>> control = composite.getControl();
    control.add( factory.createText( text ) );
    control.add( factory.createButton( button ) );

    return factory.createComposite( composite );
  }

  @Override
  public IFeatureModifier createFeatureModifier( final GMLXPath propertyPath, final IPropertyType ftp, final IFeatureSelectionManager selectionManager, final IFeatureChangeListener fcl, final String format )
  {
    return new ButtonModifier( propertyPath, ftp, fcl );
  }

  @Override
  public boolean isGeometry( )
  {
    return true;
  }

  @Override
  public Object parseText( final String text, final String formatHint ) throws ParseException
  {
    // Standard is to use the parseType method from the corresponding marhsalling type handler
    // In future, this should be directly implemented at this point
    final IMarshallingTypeHandler marshallingHandler = MarshallingTypeRegistrySingleton.getTypeRegistry().getTypeHandlerForTypeName( getTypeName() );
    return marshallingHandler.parseType( text );
  }
}
