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
package org.kalypso.gml.ui.internal.shape;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.kalypso.gml.ui.KalypsoGmlUIPlugin;
import org.kalypso.gml.ui.KalypsoGmlUiImages;
import org.kalypso.gml.ui.i18n.Messages;
import org.kalypso.shape.dbf.DBFField;
import org.kalypso.shape.dbf.DBaseException;
import org.kalypso.shape.dbf.FieldType;

/**
 * @author Gernot Belger
 */
public class AddFieldAction extends Action
{
  private final IObservableList m_fieldList;

  public AddFieldAction( final IObservableList fieldList )
  {
    super( Messages.getString( "AddFieldAction_0" ) ); //$NON-NLS-1$

    final ImageDescriptor image = KalypsoGmlUIPlugin.getImageProvider().getImageDescriptor( KalypsoGmlUiImages.DESCRIPTORS.SHAPE_FILE_NEW_ADD_FIELD );
    setImageDescriptor( image );

    setToolTipText( Messages.getString( "AddFieldAction_1" ) ); //$NON-NLS-1$

    m_fieldList = fieldList;
  }

  /**
   * @see org.eclipse.jface.action.Action#run()
   */
  @Override
  public void run( )
  {
    try
    {
      m_fieldList.add( new DBFFieldBean( new DBFField( Messages.getString( "AddFieldAction_2" ) + m_fieldList.size(), FieldType.N, (short) 20, (short) 10 ) ) ); //$NON-NLS-1$
    }
    catch( final DBaseException e )
    {
      e.printStackTrace();
    }
  }

}
