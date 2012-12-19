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
 *  ---------------------------------------------------------------------------*/
package org.kalypso.ogc.gml.featureview.maker;

import javax.xml.bind.JAXBElement;

import org.kalypso.core.jaxb.TemplateUtilities;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.ogc.gml.featureview.control.ButtonFeatureControl;
import org.kalypso.ogc.gml.gui.IFeatureDialogFactory;
import org.kalypso.template.featureview.Button;
import org.kalypso.template.featureview.ControlType;
import org.kalypso.template.featureview.GridDataType;
import org.kalypsodeegree.model.feature.Feature;

/**
 * Displays a simple button to edit the property.
 * 
 * @author Gernot Belger
 */
public class ButtonControlMaker extends AbstractValueControlMaker
{
  public ButtonControlMaker( final boolean addValidator )
  {
    super( addValidator );
  }

  @Override
  protected JAXBElement< ? extends ControlType> createControlType( final Feature feature, final IFeatureType ft, final IPropertyType fpt, final GridDataType griddata )
  {
    /* Do not show elements that have no dialog factory, the user is not able to edit them anyway */
    final IFeatureDialogFactory factory = ButtonFeatureControl.findDialogFactory( null, fpt );
    if( factory == null )
      return null;

    final Button button = TemplateUtilities.OF_FEATUREVIEW.createButton();
    button.setStyle( "SWT.PUSH" ); //$NON-NLS-1$
    button.setProperty( fpt.getQName() );

    griddata.setHorizontalAlignment( "GridData.BEGINNING" ); //$NON-NLS-1$
    griddata.setHorizontalSpan( 2 );

    return TemplateUtilities.OF_FEATUREVIEW.createButton( button );
  }
}
