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
package org.kalypso.ogc.gml.util;

import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.i18n.Messages;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.IFeatureProperty;

/**
 * Common code for adding features.
 *
 * @author Gernot Belger
 */
public final class AddFeatureHandlerUtil
{
  private AddFeatureHandlerUtil( )
  {
    throw new UnsupportedOperationException();
  }

  public static boolean checkPrecondition( final Shell shell, final IFeatureProperty targetProperty )
  {
    if( !checkMaxCount( targetProperty ) )
    {
      MessageDialog.openInformation( shell, Messages.getString( "org.kalypso.ogc.gml.featureview.control.TableFeatureContol.2" ), Messages.getString( "org.kalypso.ogc.gml.featureview.control.TableFeatureContol.3" ) ); //$NON-NLS-1$ //$NON-NLS-2$
      return false;
    }

    return true;
  }

  /**
   * This function checks, if more features can be added.
   *
   * @return <code>true</code>, if so.
   */
  private static boolean checkMaxCount( final IFeatureProperty targetProperty )
  {
    /* Get the needed properties. */
    final Feature parentFeature = targetProperty.getParentFeature();
    final IRelationType parentRelation = targetProperty.getPropertyType();

    final int maxOccurs = parentRelation.getMaxOccurs();
    if( maxOccurs == IPropertyType.UNBOUND_OCCURENCY )
      return true;

    /* List shoul never exceed max occurs */
    if( parentRelation.isList() )
    {
      final List< ? > list = (List< ? >) parentFeature.getProperty( parentRelation );
      final int size = list.size();

      return size <= maxOccurs;
    }

    /* if not a list: Never add another feature if the reference is already set */
    if( parentFeature.getProperty( parentRelation ) != null )
      return false;

    return true;
  }
}