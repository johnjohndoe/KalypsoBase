/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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
import javax.xml.namespace.QName;

import org.kalypso.core.jaxb.TemplateUtilities;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.ogc.gml.featureview.control.ChecklistOfLinksFeatureviewControlFactory;
import org.kalypso.template.featureview.ControlType;
import org.kalypso.template.featureview.Extensioncontrol;
import org.kalypso.template.featureview.GridDataType;
import org.kalypso.template.featureview.GridLayout;
import org.kalypso.template.featureview.Group;
import org.kalypsodeegree.model.feature.Feature;

/**
 * Creates feature controls for lists of linked features
 * 
 * @author Gernot Belger
 */
public class LinkedListFeatureControlMaker extends AbstractValueControlMaker
{
  public LinkedListFeatureControlMaker( final boolean addValidator )
  {
    super( addValidator );
  }

  @Override
  protected JAXBElement< ? extends ControlType> createControlType( final Feature feature, final IFeatureType ft, final IPropertyType pt, final GridDataType griddata )
  {
    if( !(pt instanceof IRelationType) )
      return null;

    final IRelationType rt = (IRelationType)pt;
    if( rt.isInlineAble() )
      return null;

    if( !rt.isLinkAble() )
      return null;

    if( !pt.isList() )
      return null;

    final QName qname = rt.getQName();

    /* Create the UI components */
    final GridDataType listData = TemplateUtilities.OF_FEATUREVIEW.createGridDataType();
    listData.setHorizontalAlignment( "SWT.FILL" ); //$NON-NLS-1$
    listData.setVerticalAlignment( "SWT.FILL" ); //$NON-NLS-1$
    listData.setGrabExcessHorizontalSpace( true );

    final Extensioncontrol extensioncontrol = TemplateUtilities.OF_FEATUREVIEW.createExtensioncontrol();
    extensioncontrol.setEnabled( true );
    extensioncontrol.setExtensionId( ChecklistOfLinksFeatureviewControlFactory.class.getName() );
    extensioncontrol.setLayoutData( TemplateUtilities.OF_FEATUREVIEW.createGridData( listData ) );
    extensioncontrol.setStyle( "SWT.BORDER" ); //$NON-NLS-1$
    extensioncontrol.setVisible( true );
    extensioncontrol.setProperty( qname );

    final GridLayout groupLayout = TemplateUtilities.OF_FEATUREVIEW.createGridLayout();

    final Group group = TemplateUtilities.OF_FEATUREVIEW.createGroup();
    group.setLayout( TemplateUtilities.OF_FEATUREVIEW.createGridLayout( groupLayout ) );
    group.setStyle( "SWT.NONE" ); //$NON-NLS-1$

    group.getControl().add( TemplateUtilities.OF_FEATUREVIEW.createExtensioncontrol( extensioncontrol ) );

    return TemplateUtilities.OF_FEATUREVIEW.createGroup( group );
  }
}
