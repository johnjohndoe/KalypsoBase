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
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.Wizard;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.contribs.eclipse.jface.operation.RunnableContextHelper;
import org.kalypso.contribs.java.util.Arrays;
import org.kalypso.gml.ui.jface.FeatureSelectionPage;
import org.kalypso.ogc.gml.selection.FeatureSelectionHelper;
import org.kalypso.ogc.gml.selection.IFeatureSelection;
import org.kalypso.shape.deegree.IShapeDataFactory;
import org.kalypso.util.swt.StatusDialog2;
import org.kalypsodeegree.model.feature.Feature;

/**
 * @author Gernot Belger
 */
public class ExportShapeWizard extends Wizard
{
  private final ExportShapePage m_exportShapePage;

  private final FeatureSelectionPage m_selectFeaturesPage;

  public ExportShapeWizard( final IFeatureSelection featureSelection, final String fileName )
  {
    final Feature[] featureArray = FeatureSelectionHelper.getFeatures( featureSelection );
    m_selectFeaturesPage = new FeatureSelectionPage( "festureSelection", featureArray, null, featureArray, 1 );
    m_selectFeaturesPage.setTitle( "Choose Features" );
    m_selectFeaturesPage.setDescription( "Please choose features for export." );

    addPage( m_selectFeaturesPage );

    m_exportShapePage = new ExportShapePage( "exportShapePage", fileName );
    addPage( m_exportShapePage );

    setNeedsProgressMonitor( true );
  }

  /**
   * @see org.eclipse.jface.wizard.Wizard#performFinish()
   */
  @Override
  public boolean performFinish( )
  {
    final Charset shapeCharset = m_exportShapePage.getCharset();
    final String coordinateSystem = m_exportShapePage.getCoordinateSystem();
    final String shapeFileBase = m_exportShapePage.getShapeFileBase();
    final boolean doWritePrj = m_exportShapePage.isWritePrj();

    final Object[] choosen = m_selectFeaturesPage.getChoosen();
    final Feature[] chosenFeatures = Arrays.castArray( choosen, new Feature[choosen.length] );

    final IShapeDataFactory shapeDataFactory = createDataFactory( chosenFeatures, shapeCharset, coordinateSystem );

    final ICoreRunnableWithProgress operation = new ExportShapeOperation( shapeFileBase, shapeDataFactory, doWritePrj );

    final IWizardContainer container = getContainer();
    final IStatus status = RunnableContextHelper.execute( container, true, true, operation );

    if( !status.isOK() )
      new StatusDialog2( getShell(), status, getWindowTitle() ).open();
// ErrorDialog.openError( getShell(), getWindowTitle(), "Failed to write shape file", status );
    return status.isOK();
  }

  protected IShapeDataFactory createDataFactory( final Feature[] chosenFeatures, final Charset shapeCharset, final String coordinateSystem )
  {
    return new StandardShapeDataFactory( chosenFeatures, shapeCharset, coordinateSystem );
  }

}
