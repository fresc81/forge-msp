package com.fresc.msp.proxy;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStoppedEvent;

public class ServerProxy extends CommonProxy
{
  
  // the server runs only one script engine for the dedicated server
  
  public ServerProxy()
  {
  }
  
  @Override
  public void preInit(FMLPreInitializationEvent event)
  {
    super.preInit(event);
  }
  
  @Override
  public void init(FMLInitializationEvent event)
  {
    super.init(event);
  }
  
  @Override
  protected void registerEvents()
  {
    super.registerEvents();
    
  }
  
  @Override
  public void postInit(FMLPostInitializationEvent event)
  {
    super.postInit(event);
  }
  
  @Override
  public void serverStarted(FMLServerStartedEvent event)
  {
    super.serverStarted(event);
  }
  
  @Override
  public void serverStopped(FMLServerStoppedEvent event)
  {
    super.serverStopped(event);
  }
  
}
