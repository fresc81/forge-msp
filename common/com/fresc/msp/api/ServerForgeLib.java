package com.fresc.msp.api;

import cpw.mods.fml.relauncher.Side;

public final class ServerForgeLib extends ForgeLib
{

  @Override
  public Side side()
  {
    return Side.SERVER;
  }

}
