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
package org.kalypso.ogc.gml.featureview.control;

import org.eclipse.core.runtime.Assert;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.ogc.gml.featureview.IFeatureChangeListener;
import org.kalypso.ogc.gml.featureview.dialog.CreateFeaturePropertyDialog;
import org.kalypso.ogc.gml.featureview.dialog.IFeatureDialog;
import org.kalypso.ogc.gml.featureview.dialog.JumpToFeatureDialog;
import org.kalypso.ogc.gml.featureview.dialog.NotImplementedFeatureDialog;
import org.kalypso.ogc.gml.gui.IFeatureDialogFactory;
import org.kalypso.ui.internal.i18n.Messages;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.GMLWorkspace;

/**
 * Dialog factory for {@link org.kalypso.gmlschema.property.relation.IRelationType}.
 * 
 * @author Gernot Belger
 */
public class RelationFeatureDialogFactory implements IFeatureDialogFactory
{
  private final IFeatureChangeListener m_listener;

  public RelationFeatureDialogFactory( final IFeatureChangeListener listener )
  {
    m_listener = listener;
  }

  @Override
  public IFeatureDialog createFeatureDialog( final Feature feature, final IPropertyType ftp )
  {
    Assert.isTrue( ftp instanceof IRelationType );

    final IRelationType rt = (IRelationType)ftp;
    if( rt.isList() )
    {
      // it is a list of features or links to features or mixed
      return new JumpToFeatureDialog( m_listener, feature, rt );
    }

    // it is not a list
    final Object property = feature.getProperty( rt );
    final Feature linkedFeature;

    if( property == null )
    {
      if( !rt.isInlineAble() || rt.isLinkAble() )
        return new NotImplementedFeatureDialog( Messages.getString( "org.kalypso.ogc.gml.featureview.dialog.NotImplementedFeatureDialog.implemented" ), "..." ); //$NON-NLS-1$  //$NON-NLS-2$

      return new CreateFeaturePropertyDialog( m_listener, feature, rt );
    }

    if( property instanceof String ) // link auf ein Feature mit FeatureID
    {
      if( ((String)property).length() < 1 )
        return new CreateFeaturePropertyDialog( m_listener, feature, rt );

      final GMLWorkspace workspace = feature.getWorkspace();
      linkedFeature = workspace.getFeature( (String)property );
    }
    else if( property instanceof Feature )
      linkedFeature = (Feature)property;
    else
      return new NotImplementedFeatureDialog( Messages.getString( "org.kalypso.ogc.gml.featureview.control.ButtonFeatureControl.keinelement" ), Messages.getString( "org.kalypso.ogc.gml.featureview.control.ButtonFeatureControl.leer" ) ); //$NON-NLS-1$ //$NON-NLS-2$

    return new JumpToFeatureDialog( m_listener, linkedFeature, null );
  }
}