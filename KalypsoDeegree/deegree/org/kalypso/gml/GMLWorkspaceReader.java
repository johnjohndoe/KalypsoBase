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
package org.kalypso.gml;

import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree_impl.io.sax.marshaller.AbstractXMLReader;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 * @author Andreas von D�mming
 */
public class GMLWorkspaceReader extends AbstractXMLReader
{
  @Override
  public void parse( final InputSource input ) throws SAXException
  {
    if( input == null || !(input instanceof GMLWorkspaceInputSource) )
      throw new SAXException( "inputSource is null or not of type: " + GMLWorkspaceInputSource.class.getName() );

    final ContentHandler handler = getContentHandler();
    final GMLWorkspace workspace = ((GMLWorkspaceInputSource) input).getGMLWorkspace();

    final GMLSAXFactory factory = new GMLSAXFactory( this, workspace.getGMLSchema().getGMLVersion() );

    handler.startDocument();
    factory.process( workspace );
    handler.endDocument();
  }


  @Override
  public void parse( final String systemId )
  {
    throw new UnsupportedOperationException();
  }
}
