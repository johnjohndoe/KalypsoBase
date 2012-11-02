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
package org.kalypso.ogc.gml.featureview.modfier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.IValuePropertyType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.ogc.gml.command.FeatureChange;
import org.kalypso.ogc.gml.featureview.IFeatureChangeListener;
import org.kalypso.ogc.gml.featureview.control.ButtonFeatureControl;
import org.kalypso.ogc.gml.featureview.dialog.IFeatureDialog;
import org.kalypso.ogc.gml.gui.GuiTypeRegistrySingleton;
import org.kalypso.ogc.gml.gui.IGuiTypeHandler;
import org.kalypso.ogc.gml.table.celleditors.DialogCellEditor;
import org.kalypso.ui.internal.i18n.Messages;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureList;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree_impl.model.feature.gmlxpath.GMLXPath;

/**
 * @author belger
 */
public class ButtonModifier extends AbstractFeatureModifier
{
  private Feature m_feature;

  private final IFeatureChangeListener m_fcl;

  public ButtonModifier( final GMLXPath propertyPath, final IPropertyType ftp, final IFeatureChangeListener fcl )
  {
    init( propertyPath, ftp, new HashMap<String, String>() );

    m_fcl = fcl;
  }

  @Override
  public Object getProperty( final Feature feature )
  {
    m_feature = feature;

    return super.getProperty( feature );
  }

  @Override
  public CellEditor createCellEditor( final Composite parent )
  {
    final IFeatureChangeListener fcl = m_fcl;
    return new DialogCellEditor( parent )
    {
      IFeatureDialog m_featureDialog = null;

      @Override
      protected boolean openDialog( final Control parentControl )
      {
        m_featureDialog = ButtonFeatureControl.chooseDialog( getFeature(), getPropertyType(), fcl );
        return m_featureDialog.open( parentControl.getShell() ) == Window.OK;
      }

      @Override
      protected Object doGetValue( )
      {
        // collect changes from dialog
        final List<FeatureChange> col = new ArrayList<>();
        m_featureDialog.collectChanges( col );
        if( col.size() > 1 ) // TODO support more
          throw new UnsupportedOperationException( "Dialog must provide exactly one change" ); //$NON-NLS-1$
        if( col.size() > 0 )
        {
          final Object change = col.get( 0 );
          if( change instanceof FeatureChange )
            return change;
        }
        return super.doGetValue();
      }
    };
  }

  protected Feature getFeature( )
  {
    return m_feature;
  }

  /**
   * @see org.eclipse.jface.viewers.ICellEditorValidator#isValid(java.lang.Object)
   */
  @Override
  public String isValid( final Object value )
  {
    return null; // null means vaild
  }

  @Override
  public String getLabel( final Feature f )
  {
    // TODO: GUITypeHandler konsequent einsetzen
    // besser: abhängig von der IPropertyType etwas machen
    final IPropertyType ftp = getPropertyType();
    final Object fprop = f.getProperty( ftp );
    final Object value = getProperty( f );
    if( fprop != null )
    {
      if( ftp instanceof IValuePropertyType )
      {
        final IValuePropertyType vpt = (IValuePropertyType)ftp;
        final IGuiTypeHandler handler = GuiTypeRegistrySingleton.getTypeRegistry().getTypeHandlerFor( vpt );
        if( handler != null && value != null )
          return handler.getText( value );
      }
      if( value instanceof Feature )
        return Messages.getString( "org.kalypso.ogc.gml.featureview.modfier.ButtonModifier.element" ); //$NON-NLS-1$
      else if( value instanceof FeatureList )
        return Messages.getString( "org.kalypso.ogc.gml.featureview.modfier.ButtonModifier.elements" ); //$NON-NLS-1$
      else if( value instanceof GM_Object )
        return Messages.getString( "org.kalypso.ogc.gml.featureview.modfier.ButtonModifier.geometry" ); //$NON-NLS-1$
      else if( ftp instanceof IRelationType )
        return Messages.getString( "org.kalypso.ogc.gml.featureview.modfier.ButtonModifier.link" ); //$NON-NLS-1$
    }
    return Messages.getString( "org.kalypso.ogc.gml.featureview.modfier.ButtonModifier.edit" ); //$NON-NLS-1$
  }
}