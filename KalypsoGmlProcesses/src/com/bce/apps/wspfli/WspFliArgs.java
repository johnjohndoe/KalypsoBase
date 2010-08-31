package com.bce.apps.wspfli;

import java.io.File;

/**
 * Liest die Argumente der Anwendung ein und parst sie
 *
 * @author belger
 */
public class WspFliArgs
{
  public final Double rasterSize;

  public final File wspFile;

  public final File dgmFile;

  public final String shapeBase;

  public final Boolean bIso;

  public final double[] grenzen;

  public final boolean bFliTi;

  public final Boolean bDoVolumeCalculation;

  @SuppressWarnings("hiding")
  public WspFliArgs( final File dgmFile, final File wspFile, final String shapeBase, final Double rasterSize, final Boolean bIso, final double[] grenzen, final boolean bFlitTi, final Boolean bDoVolumeCalculation )
  {
    this.dgmFile = dgmFile;
    this.wspFile = wspFile;
    this.shapeBase = shapeBase;
    this.rasterSize = rasterSize;
    this.bIso = bIso;
    this.grenzen = grenzen;
    this.bFliTi = bFlitTi;
    this.bDoVolumeCalculation = bDoVolumeCalculation;
  }
}
