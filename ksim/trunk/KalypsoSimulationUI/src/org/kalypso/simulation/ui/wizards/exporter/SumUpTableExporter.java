/*--------------- Kalypso-Header ------------------------------------------

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

 --------------------------------------------------------------------------*/

package org.kalypso.simulation.ui.wizards.exporter;

import java.net.URL;

import org.apache.commons.configuration.Configuration;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.wizard.IWizardPage;
import org.kalypso.commons.arguments.Arguments;
import org.kalypso.metadoc.IExportableObject;
import org.kalypso.metadoc.configuration.IPublishingConfiguration;
import org.kalypso.metadoc.impl.AbstractExporter;
import org.kalypso.simulation.ui.wizards.exporter.sumuptable.ExportableSumUpTable;

/**
 * Exports a table showing a list of observations with the following structure:
 * 
 * <pre>
 * Observation-Name | Observation-Metadata (some of them) (*) | Max-Value (**) | Time(Max-Value) | Alarmlevel | Value(Alarmlevel) | Time i + 0.p (***) | Time i + 1.p | ... | Time i + n.p
 * </pre>
 * 
 * <ul>
 * <li>(*) the list of metadata to display is defined in the arguments of the exporter
 * <li>(**) the max-value is taken for one axis which is defined in the arguments of the exporter
 * <li>(***) the value of p is also defined in the arguments
 * </ul>
 * <p>
 * This exporter has no wizard pages.
 * <p>
 * Arguments specification:
 * <ul>
 * <li>documentName
 * <li>separator
 * <li>charset
 * <li>delta
 * <li>dateFormat
 * <li>obs...
 * <li>column...
 * <li>axisType
 * <li>timeUnit
 * <li>timeStep
 * </ul>
 * <p>
 * Example:
 * 
 * <pre>
 * </pre>
 * 
 * @author schlienger
 */
public class SumUpTableExporter extends AbstractExporter
{
  /**
   * @see org.kalypso.metadoc.IExportableObjectFactory#createExportableObjects(org.apache.commons.configuration.Configuration)
   */
  public IExportableObject[] createExportableObjects( final Configuration configuration ) throws CoreException
  {
    // from supplier
    final Arguments arguments = (Arguments)getFromSupplier( "arguments" );
    final URL context = (URL)getFromSupplier( "context" );

    return new IExportableObject[]
    { new ExportableSumUpTable( arguments, context ) };
  }

  /**
   * @see org.kalypso.metadoc.IExportableObjectFactory#createWizardPages(org.kalypso.metadoc.configuration.IPublishingConfiguration)
   */
  public IWizardPage[] createWizardPages( final IPublishingConfiguration configuration ) throws CoreException
  {
    return new IWizardPage[0];
  }
}
