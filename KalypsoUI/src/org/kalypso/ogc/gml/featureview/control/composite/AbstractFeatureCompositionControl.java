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
package org.kalypso.ogc.gml.featureview.control.composite;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;
import org.kalypso.commons.i18n.I10nString;
import org.kalypso.commons.i18n.ITranslator;
import org.kalypso.gmlschema.annotation.AnnotationUtilities;
import org.kalypso.gmlschema.annotation.IAnnotation;
import org.kalypso.ogc.gml.featureview.control.FeatureComposite;
import org.kalypso.template.featureview.ControlType;
import org.kalypso.template.featureview.LayoutType;

/**
 * @author Gernot Belger
 */
public abstract class AbstractFeatureCompositionControl implements IFeatureCompositionControl
{
  private final FeatureComposite m_featureComposite;

  private final IAnnotation m_annotation;

  private final ITranslator m_translator;

  public AbstractFeatureCompositionControl( final FeatureComposite featureComposite, final IAnnotation annotation, final ITranslator translator )
  {
    m_featureComposite = featureComposite;
    m_annotation = annotation;
    m_translator = translator;
  }

  protected final Control createControl( final Composite composite, final int elementStyle, final ControlType value )
  {
    return m_featureComposite.createControl( composite, elementStyle, value, m_translator );
  }

  protected final String translate( final String text )
  {
    return new I10nString( text, m_translator ).getValue();
  }

  protected final String getAnnotation( final String annotationKey, final String overrideValue )
  {
    return AnnotationUtilities.getAnnotation( m_annotation, overrideValue, annotationKey );
  }

  protected static final Layout createLayout( final LayoutType layoutType )
  {
    if( layoutType instanceof org.kalypso.template.featureview.GridLayout )
    {
      final org.kalypso.template.featureview.GridLayout gridLayoutType = (org.kalypso.template.featureview.GridLayout)layoutType;
      final GridLayout layout = new GridLayout();
      layout.horizontalSpacing = gridLayoutType.getHorizontalSpacing();
      layout.verticalSpacing = gridLayoutType.getVerticalSpacing();
      layout.makeColumnsEqualWidth = gridLayoutType.isMakeColumnsEqualWidth();
      layout.marginHeight = gridLayoutType.getMarginHeight();
      layout.marginWidth = gridLayoutType.getMarginWidth();
      layout.marginTop = gridLayoutType.getMarginTop();
      layout.marginLeft = gridLayoutType.getMarginLeft();
      layout.marginRight = gridLayoutType.getMarginRight();
      layout.marginBottom = gridLayoutType.getMarginBottom();
      layout.numColumns = gridLayoutType.getNumColumns();

      return layout;
    }

    return null;
  }
}