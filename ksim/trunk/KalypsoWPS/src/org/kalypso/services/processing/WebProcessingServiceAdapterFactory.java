package org.kalypso.services.processing;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;

public class WebProcessingServiceAdapterFactory implements IAdapterFactory
{

  public Object getAdapter( final Object adaptableObject, final Class adapterType )
  {
    if( adaptableObject instanceof IWebProcessingService )
    {
      if( ITreeContentProvider.class.equals( adapterType ) )
      {
        // TODO return treecontentprovider        
      }
      else if( ILabelProvider.class.equals( adapterType ) )
      {
        // TODO return labelprovider        
      }
    }
    return null;
  }

  public Class[] getAdapterList( )
  {
    return new Class[] { ITreeContentProvider.class, ILabelProvider.class };
  }

}
