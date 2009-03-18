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
package org.kalypso.ogc.gml.schemaeditor;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.ISources;
import org.kalypso.commons.xml.NSPrefixProvider;
import org.kalypso.commons.xml.NSUtilities;
import org.kalypso.gmlschema.IGMLSchema;
import org.kalypso.gmlschema.annotation.IAnnotation;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.IValuePropertyType;
import org.kalypso.gmlschema.property.restriction.EnumerationRestriction;
import org.kalypso.gmlschema.property.restriction.IRestriction;

/**
 * @author kimwerner
 */
public class ExportI18nPropertiesHandler extends AbstractHandler
{

  private Formatter m_formatter = null;

  private void formatInternal( final String prefix, final String kind, final String key, final String val )
  {
    if( val == null || key == null )
      return;
    if( val.equals( key ) )
      return;
    if( key != "" )
      m_formatter.format( "%s_%s_%s=%s%n", prefix, key, kind, val );
  }

  /**
   * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
   */
  @Override
  public Object execute( ExecutionEvent event ) throws ExecutionException
  {

    try
    {
      final IEvaluationContext context = (IEvaluationContext) event.getApplicationContext();

      final GMLSchemaEditor editor = (GMLSchemaEditor) context.getVariable( ISources.ACTIVE_EDITOR_NAME );

      final IGMLSchema schema = editor.getSchema();

      FileDialog dlg = new FileDialog( editor.getEditorSite().getShell() );

      dlg.setFilterExtensions( new String[] { "*.properties", "*.*" } );
      dlg.setFilterNames( new String[] { "Property Files", "all Files" } );
      dlg.setOverwrite( true );
      final String[] url = schema.getContext().getFile().split( "\\/" );

      dlg.setFileName(url[url.length-1].split("\\.")[0] );

      dlg.open();
      String fName = dlg.getFileName();
      if( fName == null )
        return null;
      if( !fName.contains( "." ) )
        fName = fName + ".properties";
      m_formatter = new Formatter( new File( dlg.getFilterPath() + "\\" + fName ) );

      final NSPrefixProvider nsMapper = NSUtilities.getNSProvider();

      HashMap<String, String> prefixMap = new HashMap<String, String>();

      final IFeatureType[] featureTypes = schema.getAllFeatureTypes();
      for( IFeatureType featureType : featureTypes )
      {
        QName ftName = featureType.getQName();
        String ftNamespaceURI = ftName.getNamespaceURI();
        String ftLocalPart = ftName.getLocalPart();
        String ftPrefix = nsMapper.getPreferredPrefix( ftNamespaceURI, null );
        prefixMap.put( ftNamespaceURI, ftPrefix );

        IAnnotation ftAnno = featureType.getAnnotation();
        String ftLabel = ftAnno.getLabel();
        String ftDescripion = ftAnno.getDescription();

        formatInternal( ftPrefix, "label", ftLocalPart, ftLabel );
        formatInternal( ftPrefix, "description", ftLocalPart, ftDescripion );

        final IPropertyType[] properties = featureType.getProperties();
        for( IPropertyType propertyType : properties )
        {
          QName ptName = propertyType.getQName();

          IAnnotation pAnno = propertyType.getAnnotation();

          String pLabel = pAnno.getLabel();
          String pTooltip = pAnno.getTooltip();
          String prefix = ftPrefix + "_" + ftName.getLocalPart() + "_" + ptName.getNamespaceURI();

          formatInternal( prefix, "label", ptName.getLocalPart(), pLabel );

          formatInternal( prefix, "tooltip", ptName.getLocalPart(), pTooltip );

          if( propertyType instanceof IValuePropertyType )
          {
            final IValuePropertyType vpt = (IValuePropertyType) propertyType;
            final IRestriction[] restrictions = vpt.getRestriction();
            for( IRestriction restriction : restrictions )
            {
              if( restriction instanceof EnumerationRestriction )
              {
                final EnumerationRestriction enumRest = (EnumerationRestriction) restriction;
                final Map<Object, IAnnotation> map = enumRest.getMapping();
                for( final Object obj : map.keySet() )
                {
                  String key = obj.toString();
                  IAnnotation anno = map.get( obj );
                  String aLabel = anno.getLabel();
                  String aTooltip = anno.getTooltip();
                  formatInternal( ftPrefix, "label", key, aLabel );
                  formatInternal( ftPrefix, "tooltip", key, aTooltip );

                }

              }
            }
          }
        }

      }

      // TODO: write prefix map

      m_formatter.close();

      return null;
    }
    catch( final FileNotFoundException e )
    {
      e.printStackTrace();

      throw new ExecutionException( "Failed to export schema", e );
    }
  }
}
