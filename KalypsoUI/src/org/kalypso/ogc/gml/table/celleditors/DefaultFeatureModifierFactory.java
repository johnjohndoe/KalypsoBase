/*--------------- Kalypso-Header --------------------------------------------------------------------

 This file is part of kalypso.
 Copyright (C) 2004, 2005 by:

 Technical University Hamburg-Harburg (TUHH)
 Institute of River and coastal engineering
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
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 E-Mail:
 belger@bjoernsen.de
 schlienger@bjoernsen.de
 v.doemming@tuhh.de

 ---------------------------------------------------------------------------------------------------*/
package org.kalypso.ogc.gml.table.celleditors;

import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.IValuePropertyType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.ogc.gml.featureview.IFeatureChangeListener;
import org.kalypso.ogc.gml.featureview.IFeatureModifier;
import org.kalypso.ogc.gml.featureview.modfier.ButtonModifier;
import org.kalypso.ogc.gml.featureview.modfier.ComboBoxModifier;
import org.kalypso.ogc.gml.featureview.modfier.StringModifier;
import org.kalypso.ogc.gml.gui.GuiTypeRegistrySingleton;
import org.kalypso.ogc.gml.gui.IGuiTypeHandler;
import org.kalypso.ogc.gml.selection.IFeatureSelectionManager;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree_impl.model.feature.gmlxpath.GMLXPath;

/**
 * @author Belger
 */
public class DefaultFeatureModifierFactory implements IFeatureModifierFactory
{
  @Override
  public IFeatureModifier createFeatureModifier( final GMLXPath propertyPath, final IPropertyType ftp, final String format, final IFeatureSelectionManager selectionManager, final IFeatureChangeListener fcl )
  {
    if( ftp == null )
      return null;

    if( ftp instanceof IValuePropertyType )
    {
      final IValuePropertyType vpt = (IValuePropertyType)ftp;
      if( vpt.isGeometry() || !vpt.getQName().equals( Feature.QN_NAME ) && vpt.isList() )
        return new ButtonModifier( propertyPath, vpt, fcl );

      final IGuiTypeHandler typeHandler = GuiTypeRegistrySingleton.getTypeRegistry().getTypeHandlerFor( vpt );
      if( typeHandler != null )
        return typeHandler.createFeatureModifier( propertyPath, ftp, selectionManager, fcl, format );
      return new StringModifier( propertyPath, vpt, format );
    }
    if( ftp instanceof IRelationType )
    {
      final IRelationType rpt = (IRelationType)ftp;
      if( !rpt.isInlineAble() && rpt.isLinkAble() && !rpt.isList() )
        return new ComboBoxModifier( propertyPath, rpt );
      else
        return new ButtonModifier( propertyPath, rpt, fcl );
    }
    throw new UnsupportedOperationException();
  }
}