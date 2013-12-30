package com.fresc.msp;

import com.fresc.msp.network.PacketHandler;
import com.fresc.msp.proxy.CommonProxy;
import com.fresc.msp.util.MSPConfig;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppedEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.network.NetworkMod;

/**
 * Minecraft Script Pack
 * 
 * @author Paul Bottin
 *
 */
@Mod(modid=MSPConfig.MOD_ID, version=MSPConfig.MOD_VERSION, name=MSPConfig.MOD_NAME, useMetadata=true)
@NetworkMod(channels = { MSPConfig.MOD_ID }, clientSideRequired = false, serverSideRequired = false, packetHandler = PacketHandler.class)
public class MSP
{
  
  @Instance(MSPConfig.MOD_CLASS_NAME)
  public static MSP instance;
  
  @SidedProxy(modId=MSPConfig.MOD_ID, clientSide=MSPConfig.CLIENT_PROXY, serverSide=MSPConfig.SERVER_PROXY)
  public static CommonProxy proxy;
  
  @EventHandler
  public void preInit(FMLPreInitializationEvent event)
  {
    proxy.preInit(event);
  }
  
  @EventHandler
  public void init(FMLInitializationEvent event)
  {
    proxy.init(event);
  }
  
  @EventHandler
  public void postInit(FMLPostInitializationEvent event)
  {
    proxy.postInit(event);
  }
  
  @EventHandler
  public void serverStarting(FMLServerStartingEvent event)
  {
    proxy.serverStarting(event);
  }
  
  @EventHandler
  public void serverStopping(FMLServerStoppingEvent event)
  {
    proxy.serverStopping(event);
  }
  
  @EventHandler
  public void serverStarted(FMLServerStartedEvent event)
  {
    proxy.serverStarted(event);
  }
  
  @EventHandler
  public void serverStopped(FMLServerStoppedEvent event)
  {
    proxy.serverStopped(event);
  }
  
}
