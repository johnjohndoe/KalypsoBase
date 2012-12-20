/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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

package org.kalypso.ui.wizard.wfs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.naming.OperationNotSupportedException;

import org.apache.commons.io.IOUtils;
import org.deegree.datatypes.QualifiedName;
import org.deegree.ogcwebservices.wfs.capabilities.WFSFeatureType;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbench;
import org.kalypso.ogc.gml.IKalypsoLayerModell;
import org.kalypso.ogc.gml.loader.WfsLoader;
import org.kalypso.ui.ImageProvider;
import org.kalypso.ui.KalypsoServiceConstants;
import org.kalypso.ui.action.AddThemeCommand;
import org.kalypso.ui.i18n.Messages;
import org.kalypso.ui.wizard.AbstractDataImportWizard;
import org.kalypsodeegree.filterencoding.ElseFilter;
import org.kalypsodeegree.filterencoding.Filter;
import org.kalypsodeegree.filterencoding.visitor.TransformSRSVisitor;
import org.kalypsodeegree.tools.FilterUtilites;
import org.kalypsodeegree_impl.filterencoding.ComplexFilter;
import org.kalypsodeegree_impl.filterencoding.FeatureFilter;

/**
 * @author Kuepferle, doemming
 */
public class ImportWfsSourceWizard extends AbstractDataImportWizard
{
  private ImportWfsWizardPage m_importWFSPage;

  private List<String> m_catalog;

  private ImportWfsFilterWizardPage m_filterWFSPage;

  @Override
  public boolean performFinish( )
  {
    final IKalypsoLayerModell mapModell = getMapModel();
    if( mapModell == null )
      return true;

    try
    {
      final WFSFeatureType[] layers = m_importWFSPage.getChoosenFeatureLayer();
      for( final WFSFeatureType featureType : layers )
      {
        final Filter complexFilter = m_importWFSPage.getFilter( featureType );
        final Filter simpleFilter = m_filterWFSPage.getFilter();
        final Filter mergedFilter = FilterUtilites.mergeFilters( complexFilter, simpleFilter );

        final String xml = buildXml( featureType, mergedFilter );

        // TODO here the featurePath is set to featureMember because this is
        // the top feature of the GMLWorkspace
        // it must be implemented to only set the name of the feature
        // (relative path of feature)

        final StringBuffer source = new StringBuffer();
        final QualifiedName qNameFT = featureType.getName();
        source.append( "#" ).append( WfsLoader.KEY_URL ).append( "=" ).append( m_importWFSPage.getUri() ); //$NON-NLS-1$ //$NON-NLS-2$
        source.append( "#" ).append( WfsLoader.KEY_FEATURETYPE ).append( "=" ).append( qNameFT.getLocalName() ); //$NON-NLS-1$ //$NON-NLS-2$
        final String namespaceURI = qNameFT.getNamespace().toString();
        if( namespaceURI != null && namespaceURI.length() > 0 )
          source.append( "#" ).append( WfsLoader.KEY_FEATURETYPENAMESPACE ).append( "=" ).append( namespaceURI ); //$NON-NLS-1$ //$NON-NLS-2$

        if( xml != null )
          source.append( "#" ).append( WfsLoader.KEY_FILTER ).append( "=" ).append( xml ); //$NON-NLS-1$ //$NON-NLS-2$
        if( m_filterWFSPage.doFilterMaxFeatures() )
        {
          final int maxfeatures = m_filterWFSPage.getMaxFeatures();
          source.append( "#" ).append( WfsLoader.KEY_MAXFEATURE ).append( "=" ).append( Integer.toString( maxfeatures ) ); //$NON-NLS-1$ //$NON-NLS-2$
        }

        final String featurePath = "featureMember[" + qNameFT.getLocalName() + "]"; //$NON-NLS-1$ //$NON-NLS-2$
        String title = featureType.getTitle();
        if( title == null || title.isEmpty() )
          title = qNameFT.getLocalName();
        final AddThemeCommand command = new AddThemeCommand( mapModell, title, "wfs", featurePath, source.toString() ); //$NON-NLS-1$
        postCommand( command, null );
      }
    }
    catch( final OperationNotSupportedException e )
    {
      e.printStackTrace();
      m_filterWFSPage.setErrorMessage( e.getMessage() );
      return false;
    }

    return true;
  }

  private String buildXml( final WFSFeatureType featureType, final Filter mergedFilter )
  {
    if( mergedFilter instanceof ComplexFilter )
      return transformToRemoteCRS( (ComplexFilter) mergedFilter, featureType.getDefaultSRS().toString() );

    if( mergedFilter instanceof FeatureFilter )
      return ((FeatureFilter) mergedFilter).toXML().toString();

    if( mergedFilter instanceof ElseFilter )
      return ((ElseFilter) mergedFilter).toXML().toString();

    return null;
  }

  @Override
  public void init( final IWorkbench workbench, final IStructuredSelection selection )
  {
    final InputStream is = getClass().getResourceAsStream( "resources/kalypsoOWS.catalog" ); //$NON-NLS-1$
    try
    {
      // read service catalog file
      readCatalog( is );
      is.close();
    }
    catch( final IOException e )
    {
      e.printStackTrace();
      m_catalog = new ArrayList<>();
    }
    finally
    {
      IOUtils.closeQuietly( is );
    }
  }

  @Override
  public void addPages( )
  {
    m_importWFSPage = new ImportWfsWizardPage( "WfsImportPage", Messages.getString( "org.kalypso.ui.wizard.wfs.ImportWfsSourceWizard.0" ), ImageProvider.IMAGE_UTIL_UPLOAD_WIZ ); //$NON-NLS-1$ //$NON-NLS-2$
    m_filterWFSPage = new ImportWfsFilterWizardPage( "WfsImportFilterPage", Messages.getString( "org.kalypso.ui.wizard.wfs.ImportWfsSourceWizard.1" ), ImageProvider.IMAGE_UTIL_IMPORT_WIZARD, getMapModel() ); //$NON-NLS-1$ //$NON-NLS-2$
    addPage( m_importWFSPage );
    addPage( m_filterWFSPage );
  }

  @Override
  public boolean performCancel( )
  {
    dispose();
    return true;
  }

  public List<String> getCatalog( )
  {
    return m_catalog;
  }

  public void readCatalog( final InputStream is ) throws IOException
  {
    final ArrayList<String> catalog = new ArrayList<>();
    final BufferedReader br = new BufferedReader( new InputStreamReader( is ) );
    String line = br.readLine();
    do
    {
      if( line.startsWith( KalypsoServiceConstants.WFS_LINK_TYPE ) )
        catalog.add( line.split( "=" )[1] ); //$NON-NLS-1$

      line = br.readLine();
    }
    while( line != null );

    m_catalog = catalog;
  }

  private String transformToRemoteCRS( final ComplexFilter filter, final String remoteCrs )
  {
    String xml = null;
    final TransformSRSVisitor visitor = new TransformSRSVisitor( remoteCrs );
    visitor.visit( filter.getOperation() );
    xml = filter.toXML().toString();
    return xml;
  }

  public void setCatalog( final List<String> catalog )
  {
    m_catalog = catalog;
  }
}