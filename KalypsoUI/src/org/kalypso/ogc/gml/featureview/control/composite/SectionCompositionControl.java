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
package org.kalypso.ogc.gml.featureview.control.composite;

import javax.xml.bind.JAXBElement;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.kalypso.commons.i18n.ITranslator;
import org.kalypso.contribs.eclipse.swt.SWTUtilities;
import org.kalypso.gmlschema.annotation.IAnnotation;
import org.kalypso.ogc.gml.featureview.control.FeatureComposite;
import org.kalypso.template.featureview.ControlType;
import org.kalypso.template.featureview.Section;

/**
 * @author Gernot Belger
 */
public class SectionCompositionControl extends AbstractFeatureCompositionControl
{
  private final Section m_sectionType;

  public SectionCompositionControl( final Section sectionType, final FeatureComposite featureComposite, final IAnnotation annotation, final ITranslator translator )
  {
    super( featureComposite, annotation, translator );

    m_sectionType = sectionType;
  }

  @Override
  public Control createControl( final FormToolkit toolkit, final Composite parent, final int style )
  {
    final org.eclipse.ui.forms.widgets.Section section = createSection( toolkit, parent, style );

    final String title = m_sectionType.getTitle();
    section.setText( translate( title ) );

    final String description = m_sectionType.getDescription();
    if( description != null )
      section.setDescription( translate( description ) );

    final JAXBElement< ? extends ControlType> controlElement = m_sectionType.getControl();

    final ControlType control = controlElement.getValue();
    final int elementStyle = SWTUtilities.createStyleFromString( control.getStyle() );
    final Control childControl = createControl( section, elementStyle, control );
    section.setClient( childControl );

    return section;
  }

  private org.eclipse.ui.forms.widgets.Section createSection( final FormToolkit toolkit, final Composite parent, final int style )
  {
    if( toolkit == null )
      return new org.eclipse.ui.forms.widgets.Section( parent, style );

    return toolkit.createSection( parent, style );
  }
}
