package org.kalypso.model.wspm.core.result;

public interface IStationResult
{
  String[] getComponentIds( );

  String getName( );

  String getComponentName( final String componentId );

  Number getComponentValue( final String componentId );
}