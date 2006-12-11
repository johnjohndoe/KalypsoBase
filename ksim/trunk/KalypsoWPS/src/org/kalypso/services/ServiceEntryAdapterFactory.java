package org.kalypso.services;

import net.opengeospatial.ows.CapabilitiesBaseType;

import org.eclipse.core.runtime.IAdapterFactory;

public abstract class ServiceEntryAdapterFactory implements IAdapterFactory
{
  /**
   * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
   */
  public Object getAdapter( final Object adaptableObject, final Class adapterType )
  {
    if( adaptableObject instanceof IOGCWebService )
    {
      return getServiceEntry( (IOGCWebService) adaptableObject );
    }
    else if( adaptableObject instanceof CapabilitiesBaseType )
    {
      return getServiceEntry( (CapabilitiesBaseType) adaptableObject );
    }
    else
    {
      return null;
    }
  }

  protected abstract IServiceEntry getServiceEntry( final IOGCWebService service );

  protected abstract IServiceEntry getServiceEntry( final CapabilitiesBaseType capabilities );

  /**
   * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
   */
  public Class[] getAdapterList( )
  {
    return new Class[] { IServiceEntry.class };
  }
}
