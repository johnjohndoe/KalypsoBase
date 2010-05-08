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
package org.kalypso.gml.ui.commands.exportshape;

import java.nio.charset.Charset;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.wizard.Wizard;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.contribs.eclipse.ui.progress.ProgressUtilities;
import org.kalypso.ogc.gml.selection.IFeatureSelection;
import org.kalypso.ogc.gml.serialize.ShapeSerializer;
import org.kalypso.shape.deegree.IShapeDataFactory;

/**
 * @author belger
 */
public class ExportShapeWizard extends Wizard
{
  private final IFeatureSelection m_featureSelection;

  private final ExportShapePage m_exportShapePage;

  public ExportShapeWizard( final IFeatureSelection featureSelection, final String fileName )
  {
    m_featureSelection = featureSelection;

    m_exportShapePage = new ExportShapePage( "exportShapePage", fileName );
    addPage( m_exportShapePage );
  }

  /**
   * @see org.eclipse.jface.wizard.Wizard#performFinish()
   */
  @Override
  public boolean performFinish( )
  {
    // TODO: show list of features to user?

    // FIXME: should fetch charset from dialog
    final Charset shapeCharset = ShapeSerializer.getShapeDefaultCharset();

    final String shapeFileBase = m_exportShapePage.getShapeFileBase();
    final IShapeDataFactory shapeDataFactory = getDataFactory();

    final ICoreRunnableWithProgress operation = new ExportShapeOperation( shapeCharset, shapeFileBase, shapeDataFactory );
    final IStatus status = ProgressUtilities.busyCursorWhile( operation );
    ErrorDialog.openError( getShell(), getWindowTitle(), "Failed to write shape file", status );
    return status.isOK();
  }

  private IShapeDataFactory getDataFactory( )
  {
    return new StandardShapeDataFactory( m_featureSelection );
  }

}
