package com.fresc.msp.api;

import cpw.mods.fml.relauncher.Side;

public final class ClientForgeLib extends ForgeLib
{

  @Override
  public Side side()
  {
    return Side.CLIENT;
  }

}
