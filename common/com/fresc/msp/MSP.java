package com.fresc.msp;

import com.fresc.msp.network.PacketHandler;
import com.fresc.msp.proxy.CommonProxy;

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
@Mod(modid=MSP.MOD_ID, version=MSP.MOD_VERSION, name=MSP.MOD_NAME, useMetadata=true)
@NetworkMod(channels = { MSP.MOD_ID }, clientSideRequired = false, serverSideRequired = false, packetHandler = PacketHandler.class)
public class MSP
{
  
  public static final String MOD_ID = "msp";
  
  public static final String MOD_VERSION = "1.0.0";
  
  public static final String MOD_NAME = "Minecraft Script Pack";
  
  public static final String MOD_CLASS_NAME = "com.fresc.msp.MSP";
  
  public static final String CLIENT_PROXY = "com.fresc.msp.proxy.ClientProxy";
  
  public static final String SERVER_PROXY = "com.fresc.msp.proxy.CommonProxy";
  
  @Instance(MOD_CLASS_NAME)
  public static MSP instance;
  
  @SidedProxy(modId=MOD_ID, clientSide=CLIENT_PROXY, serverSide=SERVER_PROXY)
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
