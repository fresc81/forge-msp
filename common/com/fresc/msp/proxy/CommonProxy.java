package com.fresc.msp.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetworkManager;
import net.minecraft.server.MinecraftServer;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.compiler.LuaC;
import org.luaj.vm2.lib.BaseLib;
import org.luaj.vm2.lib.Bit32Lib;
import org.luaj.vm2.lib.MathLib;
import org.luaj.vm2.lib.OsLib;
import org.luaj.vm2.lib.PackageLib;
import org.luaj.vm2.lib.StringLib;
import org.luaj.vm2.lib.TableLib;
import org.luaj.vm2.lib.jse.LuajavaLib;

import com.fresc.msp.api.ClientForgeLib;
import com.fresc.msp.api.ForgeLib;
import com.fresc.msp.api.ServerForgeLib;
import com.fresc.msp.command.ScriptCommand;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppedEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.relauncher.Side;

public abstract class CommonProxy
{
  
  protected final static Path SCRIPT_ROOT_PATH = Paths.get("./scripts");

  // defines the script engine for the dedicated/internal server
  protected Globals serverGlobals = null;

  protected CommonProxy()
  {
  }
  
  public void preInit(FMLPreInitializationEvent event)
  {
  }
  
  public void initializeServerScriptEngine()
  {
    finalizeServerScriptEngine();
    serverGlobals = new Globals();
    initializeGlobals(Side.SERVER, serverGlobals);
  }
  
  protected void finalizeServerScriptEngine()
  {
    if (serverGlobals != null)
    {
      finalizeGlobals(serverGlobals);
      serverGlobals = null;
    }
  }
  
  public void initializeClientScriptEngine()
  {
  }

  protected void finalizeClientScriptEngine()
  {
  }

  public void init(FMLInitializationEvent event)
  {
    registerEvents();
  }
  
  protected void registerEvents()
  {
  }
  
  public void postInit(FMLPostInitializationEvent event)
  {
  }
  
  public void serverStarted(FMLServerStartedEvent event)
  {
    initializeServerScriptEngine();
  }
  
  public void serverStopped(FMLServerStoppedEvent event)
  {
    finalizeServerScriptEngine();
  }
  
  protected void initializeGlobals(final Side side, final Globals globals)
  {
    globals.load(new BaseLib()
    {
      
      /*
       * allow loading scripts from in scripts folder
       * 
       * (non-Javadoc)
       * @see org.luaj.vm2.lib.BaseLib#findResource(java.lang.String)
       */
      @Override
      public InputStream findResource(String filename)
      {
        InputStream resource = null;
        try
        {
          Path scriptRootPath = Files.createDirectories(getScriptRootPath(side));
          Path scriptPath = scriptRootPath.resolve(filename).toAbsolutePath();
          resource = Files.newInputStream(scriptPath, StandardOpenOption.READ);
        } catch (IOException e)
        {
          resource = super.findResource(filename);
        }
        return resource;
      }
      
    });
    globals.load(new PackageLib());
    globals.load(new OsLib());
    globals.load(new Bit32Lib());
    globals.load(new MathLib());
    globals.load(new StringLib());
    globals.load(new TableLib());
    globals.load(new LuajavaLib());
    globals.load(getForgeLib(side));
    LuaC.install();
    globals.compiler = LuaC.instance;
  }
  
  private LuaValue getForgeLib(Side side)
  {
    ForgeLib forgeLib = null;
    switch (side)
    {
    case CLIENT:
      forgeLib = new ClientForgeLib();
      break;
      
    case SERVER:
      forgeLib = new ServerForgeLib();
      break;

    default:
      break;
    }
    return forgeLib;
  }
  
  public Path getScriptRootPath(Side side)
  {
    if (side == null)
      return SCRIPT_ROOT_PATH;
    
    switch (side)
    {
    case CLIENT:
      return SCRIPT_ROOT_PATH.resolve("client");
    case SERVER:
      return SCRIPT_ROOT_PATH.resolve("server");
    default:
      return SCRIPT_ROOT_PATH;
    }
  }
  
  protected void finalizeGlobals(Globals globals)
  {
  }
  
  public void runScriptServerSide(String name, String code, INetworkManager manager, EntityPlayer player)
  {
    // only ops are allowed to run scripts on the server
    MinecraftServer minecraftServer = MinecraftServer.getServer();
    if (!minecraftServer.getConfigurationManager().isPlayerOpped(player.username))
    {
      System.out.printf("non op user is trying to run server side script %s with content:\n%s\n", name, code);
      throw new WrongUsageException("commands.generic.permission");
    }
    
    if (serverGlobals != null)
    {
      System.out.printf("running server side script %s with content:\n%s\n", name, code);
      try {
        serverGlobals.loadString(code, name).call();
      } catch (Exception ex)
      {
        ex.printStackTrace();
      }
    }
  }
  
  public void runScriptClientSide(String name, String code)
  {
  }

  public void serverStarting(FMLServerStartingEvent event)
  {
    event.registerServerCommand(new ScriptCommand(event.getServer()));
  }

  public void serverStopping(FMLServerStoppingEvent event)
  {
    
  }

}
