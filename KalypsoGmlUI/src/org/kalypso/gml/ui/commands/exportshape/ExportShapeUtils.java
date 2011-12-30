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
package org.kalypso.gml.ui.commands.exportshape;

import java.util.Iterator;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.kalypso.contribs.eclipse.core.runtime.AdapterUtils;
import org.kalypso.gmlschema.annotation.IAnnotation;
import org.kalypso.ogc.gml.IKalypsoTheme;
import org.kalypso.ui.editor.gmleditor.part.FeatureAssociationTypeElement;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree_impl.model.feature.FeatureHelper;

/**
 * @author Gernot Belger
 */
public final class ExportShapeUtils
{
  private ExportShapeUtils( )
  {
    throw new UnsupportedOperationException();
  }

  /**
   * Guesses a file name fro mthe selected element(s). Could be the name of the theme to be exported or the labe of the
   * selected feature.
   */
  public static String guessExportFileName( final ISelection selection )
  {
    if( selection.isEmpty() || !(selection instanceof IStructuredSelection) )
      return null;

    final IStructuredSelection structSel = (IStructuredSelection) selection;
    for( final Iterator< ? > iterator = structSel.iterator(); iterator.hasNext(); )
    {
      final Object selectedElement = iterator.next();
      final IKalypsoTheme theme = AdapterUtils.getAdapter( selectedElement, IKalypsoTheme.class );
      if( theme != null )
        return theme.getLabel();

      final Feature feature = AdapterUtils.getAdapter( selectedElement, Feature.class );
      if( feature != null )
        return FeatureHelper.getAnnotationValue( feature, IAnnotation.ANNO_LABEL );

      final FeatureAssociationTypeElement fate = AdapterUtils.getAdapter( selectedElement, FeatureAssociationTypeElement.class );
      if( fate != null )
      {
        final Feature parentFeature = fate.getOwner();
        if( parentFeature != null )
          return FeatureHelper.getAnnotationValue( parentFeature, IAnnotation.ANNO_LABEL );
      }

      return null;
    }

    return null;
  }
}