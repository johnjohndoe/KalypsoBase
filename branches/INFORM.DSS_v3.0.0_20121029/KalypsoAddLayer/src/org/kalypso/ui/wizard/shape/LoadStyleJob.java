/** This file is part of Kalypso
 *
 *  Copyright (c) 2012 by
 *
 *  Björnsen Beratende Ingenieure GmbH, Koblenz, Germany (Bjoernsen Consulting Engineers), http://www.bjoernsen.de
 *  Technische Universität Hamburg-Harburg, Institut für Wasserbau, Hamburg, Germany
 *  (Technical University Hamburg-Harburg, Institute of River and Coastal Engineering), http://www.tu-harburg.de/wb/
 *
 *  Kalypso is free software: you can redistribute it and/or modify it under the terms
 *  of the GNU Lesser General Public License (LGPL) as published by the Free Software
 *  Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  Kalypso is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 *  warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with Kalypso.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kalypso.ui.wizard.shape;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.ui.KalypsoAddLayerPlugin;
import org.kalypso.ui.i18n.Messages;
import org.kalypsodeegree.graphics.sld.FeatureTypeStyle;
import org.kalypsodeegree.graphics.sld.Layer;
import org.kalypsodeegree.graphics.sld.Style;
import org.kalypsodeegree.graphics.sld.StyledLayerDescriptor;
import org.kalypsodeegree.xml.XMLTools;
import org.kalypsodeegree_impl.graphics.sld.SLDFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Loads style in background and updates the data afterwards.
 *
 * @author Gernot Belger
 */
public class LoadStyleJob extends Job
{
  private final ImportShapeFileData m_data;

  public LoadStyleJob( final ImportShapeFileData data )
  {
    super( Messages.getString("LoadStyleJob_0") ); //$NON-NLS-1$

    m_data = data;
  }

  @Override
  protected IStatus run( final IProgressMonitor monitor )
  {
    try
    {
      final Object[] styles = loadStyles();
      m_data.setStyles( styles );
      return Status.OK_STATUS;
    }
    catch( final CoreException e )
    {
      // FIXME:better error handling:show error in status composite
      e.printStackTrace();
      return e.getStatus();
    }
  }

  private Object[] loadStyles( ) throws CoreException
  {
    final IPath stylePath = m_data.getStyleFile().getPath();
    if( stylePath == null || stylePath.isEmpty() )
      return ImportShapeFileData.EMPTY_STYLES;

    final IFile styleFile = ResourcesPlugin.getWorkspace().getRoot().getFile( stylePath );
    if( styleFile == null || !styleFile.exists() )
      return ImportShapeFileData.EMPTY_STYLES;

    try
    {
      // FIXME: use SLDFactory readStyle instead

      final Document doc = XMLTools.parse( styleFile );
      final Element documentElement = doc.getDocumentElement();
      if( StyledLayerDescriptor.ELEMENT_STYLEDLAYERDESCRIPTOR.equals( documentElement.getLocalName() ) )
      {
        final URL context = ResourceUtilities.createURL( styleFile );
        final StyledLayerDescriptor styledLayerDescriptor = SLDFactory.createStyledLayerDescriptor( context, documentElement );

        final Layer[] layers = styledLayerDescriptor.getLayers();
        final Collection<Style> allStyles = new ArrayList<>();
        for( final Layer layer : layers )
        {
          final Style[] styles = layer.getStyles();
          for( final Style style : styles )
            allStyles.add( style );
        }

        return allStyles.toArray( new Style[allStyles.size()] );
      }
      else if( FeatureTypeStyle.ELEMENT_FEATURETYPESTYLE.equals( documentElement.getLocalName() ) )
        return ImportShapeFileData.FEATURETYPE_STYLES;
      else
        return ArrayUtils.EMPTY_OBJECT_ARRAY;
    }
    catch( final Exception e )
    {
      final IStatus status = new Status( IStatus.ERROR, KalypsoAddLayerPlugin.getId(), Messages.getString("LoadStyleJob_1"), e ); //$NON-NLS-1$
      KalypsoAddLayerPlugin.getDefault().getLog().log( status );
      throw new CoreException( status );
    }
  }

}