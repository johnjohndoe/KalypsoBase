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
package org.kalypso.contribs.eclipse.jface.preference;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * A DropDown control is associated with this field editor so that the user can choose from an existing list of
 * elements. An option is provided to allow the user to enter an non-existing element.
 * 
 * @author schlienger
 */
public class ComboStringFieldEditor extends FieldEditor
{
  private final boolean m_readOnly;

  private final String[] m_items;

  private Combo m_combo = null;

  private final String m_tooltipText;

  /**
   * Constructor
   * 
   * @param readOnly if true, the user cannot enter a non existing value
   * @param items the list of items that the combo contains
   */
  public ComboStringFieldEditor( final String name, final String labelText, final String tooltipText, final Composite parent, final boolean readOnly, final String[] items )
  {
    assert items != null;
    assert tooltipText != null;
    
    m_tooltipText = tooltipText;
    m_readOnly = readOnly;
    m_items = items;
    
    init( name, labelText );
    createControl( parent );
  }

  /**
   * @see org.eclipse.jface.preference.FieldEditor#adjustForNumColumns(int)
   */
  @Override
  protected void adjustForNumColumns( int numColumns )
  {
    if( m_combo != null )
      ((GridData) m_combo.getLayoutData()).horizontalSpan = numColumns - 1;
  }

  /**
   * @see org.eclipse.jface.preference.FieldEditor#doFillIntoGrid(org.eclipse.swt.widgets.Composite, int)
   */
  @Override
  protected void doFillIntoGrid( Composite parent, int numColumns )
  {
    final Label lbl = getLabelControl( parent );
    lbl.setToolTipText( m_tooltipText );
    
    final Combo combo = getComboControl( parent );
    final GridData gd = new GridData( GridData.FILL_HORIZONTAL );
    gd.horizontalSpan = numColumns - 1;
    gd.grabExcessHorizontalSpace = true;
    combo.setLayoutData( gd );
  }

  /**
   * Returns this field editor's combo control.
   * 
   * @param parent
   *          the parent control
   * @return the combo control
   */
  public Combo getComboControl( final Composite parent )
  {
    if( m_combo == null )
    {
      int style = SWT.DROP_DOWN;
      if( m_readOnly )
        style &= SWT.READ_ONLY;

      m_combo = new Combo( parent, style );
      m_combo.setItems( m_items );
      m_combo.setToolTipText( m_tooltipText );
    }
    else
      checkParent( m_combo, parent );
    
    return m_combo;
  }

  /**
   * @see org.eclipse.jface.preference.FieldEditor#doLoad()
   */
  @Override
  protected void doLoad( )
  {
    if( m_combo != null )
    {
      final String value = getPreferenceStore().getString( getPreferenceName() );
      m_combo.setText( value );
    }
  }

  /**
   * @see org.eclipse.jface.preference.FieldEditor#doLoadDefault()
   */
  @Override
  protected void doLoadDefault( )
  {
    if( m_combo != null )
    {
      final String value = getPreferenceStore().getDefaultString( getPreferenceName() );
      m_combo.setText( value );
    }
  }

  /**
   * @see org.eclipse.jface.preference.FieldEditor#doStore()
   */
  @Override
  protected void doStore( )
  {
    if( m_combo != null )
      getPreferenceStore().setValue( getPreferenceName(), m_combo.getText() );
  }

  /**
   * @see org.eclipse.jface.preference.FieldEditor#getNumberOfControls()
   */
  @Override
  public int getNumberOfControls( )
  {
    return 2;
  }
}
