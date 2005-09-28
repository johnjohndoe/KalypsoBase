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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.kalypso.contribs.eclipse.jface.wizard.ArrayChooserPage;
import org.kalypso.metadoc.configuration.IPublishingConfiguration;
import org.kalypso.metadoc.impl.AbstractExporter;
import org.kalypso.simulation.ui.KalypsoSimulationUIPlugin;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureList;

/**
 * Can be extended by exporters which work on features. It provides a feature-selection wizard page.
 * 
 * @author schlienger
 */
public abstract class AbstractFeatureExporter extends AbstractExporter
{
  private ArrayChooserPage m_page = null;

  /**
   * @see org.kalypso.metadoc.IExportableObjectFactory#createWizardPages(org.kalypso.metadoc.configuration.IPublishingConfiguration)
   */
  public IWizardPage[] createWizardPages( final IPublishingConfiguration configuration ) throws CoreException
  {
    if( m_page == null )
    {
      final FeatureList features = (FeatureList)getFromSupplier( "features" );
      final FeatureList selectedFeatures = (FeatureList)getFromSupplier( "selectedFeatures" );
      final String nameProperty = (String)getFromSupplier( "propertyName" );

      if( features == null )
        return new IWizardPage[0];

      final FeatureItem[] items = new FeatureItem[features.size()];
      final List selItems = new ArrayList( selectedFeatures.size() );
      for( int count = 0; count < features.size(); count++ )
      {
        final Feature f = (Feature)features.get( count );

        final FeatureItem fi = new FeatureItem( f, (String)f.getProperty( nameProperty ) );
        items[count] = fi;

        if( selectedFeatures.contains( f ) )
          selItems.add( fi );
      }

      final Object[] array = selItems.toArray();

      if( features.size() == 0 )
      {
        m_page = new ArrayChooserPage(
            items,
            array,
            array,
            "chooseFeatures",
            "Warnung: es konnte keine Vorhersagepegel gefunden werden.\nPrüfen Sie ob Berechnungen richtig durchgeführt wurden.",
            AbstractUIPlugin.imageDescriptorFromPlugin( KalypsoSimulationUIPlugin.getID(),
                "icons/wizban/bericht_wiz.gif" ) );
      }
      else
      {
        m_page = new ArrayChooserPage( items, array, array, "chooseFeatures",
            "Für diese Vorhersagepegel werden die Berichte erzeugt:", AbstractUIPlugin.imageDescriptorFromPlugin(
                KalypsoSimulationUIPlugin.getID(), "icons/wizban/bericht_wiz.gif" ) );
      }
    }

    // create the list of pages
    final List pages = new ArrayList();

    // add our feature selection page
    pages.add( m_page );

    // let subclasses insert their specific pages
    contributeWizardPages( pages, configuration );

    return (IWizardPage[])pages.toArray( new IWizardPage[pages.size()] );
  }

  /**
   * Must be implemented by subclasses in order to insert their own pages
   */
  protected abstract void contributeWizardPages( List pages, IPublishingConfiguration configuration )
      throws CoreException;

  /**
   * @return selected features
   */
  public Feature[] getSelectedFeatures()
  {
    if( m_page == null )
      return new Feature[0];

    final Object[] objects = m_page.getChoosen();
    final Feature[] features = new Feature[objects.length];
    for( int i = 0; i < features.length; i++ )
      features[i] = ( (FeatureItem)objects[i] ).getFeature();

    return features;
  }

  /**
   * Used as content for the array chooser
   * 
   * @author schlienger
   */
  private final static class FeatureItem
  {
    private final Feature m_feature;

    private final String m_name;

    public FeatureItem( final Feature feature, final String name )
    {
      m_feature = feature;
      m_name = name;
    }

    public Feature getFeature()
    {
      return m_feature;
    }

    public String getName()
    {
      return m_name;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
      return getName();
    }
  }
}
