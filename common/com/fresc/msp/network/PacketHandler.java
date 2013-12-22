package com.fresc.msp.network;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

public class PacketHandler implements IPacketHandler
{

  @Override
  public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player)
  {
    try
    {
      PacketMSP.wrap(packet).process(manager, EntityPlayer.class.cast(player));
    } catch (InstantiationException e)
    {
      e.printStackTrace();
    } catch (IllegalAccessException e)
    {
      e.printStackTrace();
    } catch (IOException e)
    {
      e.printStackTrace();
    }
  }

}
