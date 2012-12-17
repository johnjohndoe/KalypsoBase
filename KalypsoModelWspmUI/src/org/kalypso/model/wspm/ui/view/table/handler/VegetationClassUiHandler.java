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
package org.kalypso.model.wspm.ui.view.table.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ObjectUtils;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxViewerCellEditor;
import org.eclipse.jface.viewers.ICellEditorListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.widgets.Table;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.contribs.eclipse.jface.dialog.DialogSettingsUtils;
import org.kalypso.model.wspm.core.gml.classifications.IClassificationClass;
import org.kalypso.model.wspm.core.gml.classifications.IVegetationClass;
import org.kalypso.model.wspm.core.gml.classifications.IWspmClassification;
import org.kalypso.model.wspm.core.gml.classifications.helper.WspmClassifications;
import org.kalypso.model.wspm.core.profil.IProfile;
import org.kalypso.model.wspm.ui.KalypsoModelWspmUIPlugin;
import org.kalypso.observation.result.IRecord;
import org.kalypso.ogc.gml.om.table.handlers.AbstractComponentUiHandler;

/**
 * Handles vegetation class values.
 * 
 * @author Dirk Kuch
 * @author Holger Albert
 */
public class VegetationClassUiHandler extends AbstractComponentUiHandler
{
  private final IProfile m_profile;

  private final IDialogSettings m_dialogSettings;

  public VegetationClassUiHandler( final int component, final boolean editable, final boolean resizeable, final boolean moveable, final String columnLabel, final String columnTooltip, final int columnWidth, final int columnWidthPercent, final IProfile profile )
  {
    super( component, editable, resizeable, moveable, columnLabel, columnTooltip, SWT.NONE, columnWidth, columnWidthPercent, "%s", "%s", "" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    m_profile = profile;
    m_dialogSettings = DialogSettingsUtils.getDialogSettings( KalypsoModelWspmUIPlugin.getDefault(), getClass().getCanonicalName() );
  }

  @Override
  public CellEditor createCellEditor( final Table table )
  {
    final ComboBoxViewerCellEditor cellEditor = new ComboBoxViewerCellEditor( table, SWT.READ_ONLY | SWT.DROP_DOWN );
    cellEditor.setContentProvider( new ArrayContentProvider() );
    cellEditor.setLabelProvider( new ClassificationLabelProvider() );
    cellEditor.setInput( getVegetationClasses() );
    cellEditor.addListener( new ICellEditorListener()
    {
      @Override
      public void editorValueChanged( final boolean oldValidState, final boolean newValidState )
      {
      }

      @Override
      public void cancelEditor( )
      {
      }

      @Override
      public void applyEditorValue( )
      {
        handleApplyEditorValue( cellEditor.getValue() );
        cellEditor.setInput( getVegetationClasses() );
      }
    } );

    final CCombo control = (CCombo)cellEditor.getControl();
    control.setVisibleItemCount( 20 );

    return cellEditor;
  }

  @Override
  public String getStringRepresentation( final IRecord record )
  {
    final Object value = record.getValue( getComponent() );
    if( Objects.isNull( value ) )
      return super.getStringRepresentation( record );

    final IWspmClassification classification = WspmClassifications.getClassification( m_profile );
    final IVegetationClass clazz = classification.findVegetationClass( value.toString() );
    if( Objects.isNotNull( clazz ) )
      return clazz.getLabelWithValues();

    return super.getStringRepresentation( record );
  }

  @Override
  public Object doGetValue( final IRecord record )
  {
    final Object value = record.getValue( getComponent() );
    if( Objects.isNull( value ) )
      return null; //$NON-NLS-1$

    final IWspmClassification classification = WspmClassifications.getClassification( m_profile );
    return classification.findVegetationClass( (String)value );
  }

  @Override
  public void doSetValue( final IRecord record, final Object value )
  {
    if( value instanceof IClassificationClass )
    {
      final IClassificationClass clazz = (IClassificationClass)value;
      setValue( record, clazz.getName() );
    }
  }

  @Override
  public Object parseValue( final String text )
  {
    return text;
  }

  @Override
  public void setValue( final IRecord record, final Object value )
  {
    final int index = getComponent();
    final Object oldValue = record.getValue( index );

    if( !ObjectUtils.equals( value, oldValue ) )
      record.setValue( getComponent(), value );
  }

  protected Object[] getVegetationClasses( )
  {
    /* Get the classification. */
    final IWspmClassification classification = WspmClassifications.getClassification( m_profile );
    if( Objects.isNull( classification ) )
      return new IVegetationClass[] {};

    /* Get the vegetation classes. */
    final IVegetationClass[] vegetationClasses = classification.getVegetationClasses();

    /* Sort the vegetation classes. */
    Arrays.sort( vegetationClasses, new VegetationClassComparator() );

    /* Find the most used vegetation classes and put them up the list. */
    final String[] names = FavoritesUtilities.getNames( vegetationClasses );

    /* Find the most used items. */
    final FavoriteItem[] usedItems = FavoritesUtilities.findMostUsedItems( names, m_dialogSettings );
    if( usedItems.length == 0 )
      return vegetationClasses;

    /* Add the most used classes. */
    final List<Object> adjustedClasses = new ArrayList<>();
    for( final FavoriteItem usedItem : usedItems )
    {
      final IVegetationClass usedClass = classification.findVegetationClass( usedItem.getName() );
      if( usedClass != null )
        adjustedClasses.add( usedClass );
    }

    /* Add a separator. */
    adjustedClasses.add( "----------" ); //$NON-NLS-1$

    /* Add all classes. */
    for( final IVegetationClass vegetationClass : vegetationClasses )
      adjustedClasses.add( vegetationClass );

    return adjustedClasses.toArray( new Object[] {} );
  }

  protected void handleApplyEditorValue( final Object value )
  {
    if( !(value instanceof IVegetationClass) )
      return;

    final IVegetationClass vegetationClass = (IVegetationClass)value;
    final String name = vegetationClass.getName();
    FavoritesUtilities.updateDialogSettings( m_dialogSettings, name );
  }
}