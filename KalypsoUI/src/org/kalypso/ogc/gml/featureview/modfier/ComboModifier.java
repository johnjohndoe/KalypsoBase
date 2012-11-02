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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxViewerCellEditor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.IValuePropertyType;
import org.kalypso.gmlschema.property.PropertyUtils;
import org.kalypso.ui.internal.i18n.Messages;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree_impl.model.feature.gmlxpath.GMLXPath;

/**
 * A modifier which handles feature-value-properties which are enumerations: shows a combo-box as cell-editor.
 * 
 * @author Gernot Belger
 */
public class ComboModifier extends AbstractFeatureModifier
{
  public static final String NO_LINK_STRING = Messages.getString( "org.kalypso.ogc.gml.featureview.modfier.ComboModifier.0" ); //$NON-NLS-1$

  private final Map<Object, String> m_comboEntries;

  private ComboBoxViewerCellEditor m_comboBoxCellEditor;

  public ComboModifier( final GMLXPath propertyPath, final IValuePropertyType ftp )
  {
    init( propertyPath, ftp, new HashMap<String, String>() );

    m_comboEntries = createComboEntries( ftp );
  }

  /**
   * Finds the entries to which will be displayed in the combo box.<br>
   * Intended to be overwritten by implementors.<br>
   * This method is called in the constructor, so be carfeul not to acces any members here.
   */
  protected Map<Object, String> createComboEntries( final IValuePropertyType ftp )
  {
    return PropertyUtils.createComboEntries( ftp );
  }

  protected Map<Object, String> getComboEntries( )
  {
    return m_comboEntries;
  }

  @Override
  public CellEditor createCellEditor( final Composite parent )
  {
    final LabelProvider lp = createLabelProvider();

    m_comboBoxCellEditor = new ComboModifierCellEditor( parent, SWT.READ_ONLY | SWT.DROP_DOWN )
    {
      @Override
      protected void doSetValue( final Object value )
      {
        // always reset input in order to allow implementors change input depending on selected feature
        setInput( getComboInput() );

        super.doSetValue( value );
      }
    };

    m_comboBoxCellEditor.setActivationStyle( ComboBoxViewerCellEditor.DROP_DOWN_ON_MOUSE_ACTIVATION | ComboBoxViewerCellEditor.DROP_DOWN_ON_KEY_ACTIVATION
        | ComboBoxViewerCellEditor.DROP_DOWN_ON_PROGRAMMATIC_ACTIVATION | ComboBoxViewerCellEditor.DROP_DOWN_ON_TRAVERSE_ACTIVATION );
    m_comboBoxCellEditor.setContenProvider( new ArrayContentProvider() );
    m_comboBoxCellEditor.setLabelProvider( lp );
    return m_comboBoxCellEditor;
  }

  protected LabelProvider createLabelProvider( )
  {
    final Map<Object, String> comboEntries = m_comboEntries;
    return new LabelProvider()
    {
      @Override
      public String getText( final Object element )
      {
        return comboEntries.get( element );
      }
    };
  }

  /**
   * Will be called before every activation of the cell editor.
   */
  protected Object getComboInput( )
  {
    return m_comboEntries.keySet().toArray( new Object[m_comboEntries.keySet().size()] );
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
    // besser: abhängig vom IPropertyType etwas machen
    final IPropertyType ftp = getPropertyType();
    final Object fprop = f.getProperty( ftp );

    if( fprop == null )
      return NO_LINK_STRING;

    return m_comboEntries.get( fprop );
  }
}