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
package org.kalypso.ui.editor.styleeditor;

import org.eclipse.swt.widgets.Control;
import org.kalypso.commons.eclipse.jface.viewers.ITypedTabList;
import org.kalypso.contribs.eclipse.swt.widgets.ControlUtils;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.ogc.gml.IKalypsoStyleListener;
import org.kalypso.ui.editor.styleeditor.binding.IStyleInput;
import org.kalypsodeegree_impl.filterencoding.PropertyName;
import org.kalypsodeegree_impl.model.feature.gmlxpath.GMLXPath;
import org.kalypsodeegree_impl.model.feature.gmlxpath.GMLXPathException;
import org.kalypsodeegree_impl.model.feature.gmlxpath.GMLXPathUtilities;

public class StyleEditorHelper
{
// TODO: move to SLD stuff and rename to SLD-helper
  public static IPropertyType getFeatureTypeProperty( final IFeatureType ft, final PropertyName propName )
  {
    try
    {
      final GMLXPath path = propName.getPath();
      final Object object = GMLXPathUtilities.query( path, ft );
      if( object instanceof IPropertyType )
        return (IPropertyType) object;
    }
    catch( final GMLXPathException e )
    {
      e.printStackTrace();
    }

    return null;
  }

  public static <DATA> void addListInputRefresher( final Control control, final IStyleInput<DATA> input, final ITypedTabList<DATA> listInput )
  {
    final Runnable runnable = new Runnable()
    {
      @Override
      public void run( )
      {
        if( !control.isDisposed() )
        {
          listInput.refresh();
        }
      }
    };

    input.addStyleListener( new IKalypsoStyleListener()
    {
      @Override
      public void styleChanged( )
      {
        ControlUtils.exec( control, runnable );
      }
    } );
  }
}