/*
 * --------------- Kalypso-Header --------------------------------------------------------------------
 * 
 * This file is part of kalypso. Copyright (C) 2004, 2005 by:
 * 
 * Technical University Hamburg-Harburg (TUHH) Institute of River and coastal engineering Denickestr. 22 21073 Hamburg,
 * Germany http://www.tuhh.de/wb
 * 
 * and
 * 
 * Bjoernsen Consulting Engineers (BCE) Maria Trost 3 56070 Koblenz, Germany http://www.bjoernsen.de
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Contact:
 * 
 * E-Mail: belger@bjoernsen.de schlienger@bjoernsen.de v.doemming@tuhh.de
 * 
 * ---------------------------------------------------------------------------------------------------
 */
package org.kalypso.ogc.gml.featureview.dialog;

import java.util.Collection;

import javax.xml.namespace.QName;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.kalypso.contribs.eclipse.core.runtime.PluginUtilities;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.types.ITypeHandler;
import org.kalypso.i18n.Messages;
import org.kalypso.ogc.gml.command.FeatureChange;
import org.kalypso.ogc.gml.typehandler.ZmlInlineTypeHandler;
import org.kalypso.ogc.sensor.view.ObservationViewerDialog;
import org.kalypso.ui.KalypsoGisPlugin;
import org.kalypsodeegree.model.feature.Feature;

/**
 * @author kuepfer
 */
public class ZmlInlineFeatureDialog implements IFeatureDialog
{
  private final Feature m_feature;

  private final IPropertyType m_ftp;

  private FeatureChange m_change = null;

  private static ITypeHandler m_typeHandler;

  public ZmlInlineFeatureDialog( final Feature feature, final IPropertyType ftp, final ITypeHandler handler )
  {
    m_feature = feature;
    m_ftp = ftp;
    m_typeHandler = handler;
  }

  /**
   * @see org.kalypso.ogc.gml.featureview.dialog.IFeatureDialog#open(org.eclipse.swt.widgets.Shell)
   */
  @Override
  public int open( final Shell shell )
  {
    m_change = null;

    if( !(m_typeHandler instanceof ZmlInlineTypeHandler) )
      return Window.CANCEL;

    final ZmlInlineTypeHandler inlineTypeHandler = (ZmlInlineTypeHandler) m_typeHandler;
    final QName typeName = inlineTypeHandler.getTypeName();

    // Dies ist ein h�sslicher hack!
    // TODO Definition eines Extension points f�r den ObservationViewerDialog damit f�r jeden TypeHandler
    // der Dialog configuriert werden kann, oder ist dies hier anders gedacht ?? CK
    // Extension Point m�sste z.B. im eine Methode wie newObservation(Shell parent) im Interface haben
    // die eine IObservation zur�ck gibt
    final int buttonControls;
    if( !(typeName.getLocalPart().equals( "ZmlInlineIdealKcWtLaiType" )) ) //$NON-NLS-1$
    {
      buttonControls = ObservationViewerDialog.BUTTON_NEW | ObservationViewerDialog.BUTTON_REMOVE | ObservationViewerDialog.BUTTON_EXEL_IMPORT | ObservationViewerDialog.BUTTON_EXEL_EXPORT;
    }
    else
    {
      buttonControls = ObservationViewerDialog.BUTTON_NEW_IDEAL_LANDUSE | ObservationViewerDialog.BUTTON_REMOVE | ObservationViewerDialog.BUTTON_EXEL_IMPORT
      | ObservationViewerDialog.BUTTON_EXEL_EXPORT;
    }

    final ObservationViewerDialog dialog = new ObservationViewerDialog( shell, false, buttonControls, inlineTypeHandler.getAxisTypes() );
    final IDialogSettings dialogSettings = PluginUtilities.getDialogSettings( KalypsoGisPlugin.getDefault(), getClass().getName() );
    dialog.setDialogSettings( dialogSettings );

    final Object o = m_feature.getProperty( m_ftp );
    dialog.setInput( o );

    final int open = dialog.open();
    if( !(open == Window.OK) )
      return open;

    final Object newValue = dialog.getInput();
    m_change = new FeatureChange( m_feature, m_ftp, newValue );
    return open;
  }

  /**
   * @see org.kalypso.ogc.gml.featureview.dialog.IFeatureDialog#collectChanges(java.util.Collection)
   */
  @Override
  public void collectChanges( final Collection<FeatureChange> c )
  {
    if( m_change != null )
      c.add( m_change );
  }

  /**
   * @see org.kalypso.ogc.gml.featureview.dialog.IFeatureDialog#getLabel()
   */
  @Override
  public String getLabel( )
  {
    return Messages.getString("org.kalypso.ogc.gml.featureview.dialog.ZmlInlineFeatureDialog.diagram"); //$NON-NLS-1$
  }
}