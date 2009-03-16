package org.kalypso.calculation.chain.binding;

public interface IKalypsoModelConnector
{

  public enum MODELSPEC_CONNECTOR_NA_WSPM
  {
    NA_Model,
    NA_StatisticalReport,
    NA_RiverCode,
    WSPM_Model,
    WSPM_RunoffEventID;
  }

  public enum MODELSPEC_CONNECTOR_WSPM_FM
  {
    WSPM_TinFile,
    FM_Model,
    OPT_DeleteExistingRunoffEvents,
    WSPM_TinReference;
  }

  public enum MODELSPEC_CONNECTOR_FM_RM
  {
    FM_Model,
    FM_EventsFolder,
    RM_Model,
    RM_InputRasterFolder;
  }
}
