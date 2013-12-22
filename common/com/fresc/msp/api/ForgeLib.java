package com.fresc.msp.api;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.util.ChatMessageComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.Event;
import net.minecraftforge.event.ForgeSubscribe;

import org.luaj.vm2.LuaClosure;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.VarArgFunction;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import com.fresc.msp.MSP;

import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

public abstract class ForgeLib extends VarArgFunction implements ITickHandler
{
  private static final int INIT     = 0;
  private static final int ON       = 1;
  private static final int OFF      = 2;
  private static final int ON_TICK  = 3;
  private static final int OFF_TICK = 4;
  private static final int PRINT    = 5;
  private static final int SIDE     = 6;
  private static final int RESET    = 7;
  
  private static final String[] NAMES =
  {
    "on",
    "off",
    "onTick",
    "offTick",
    "print",
    "side",
    "reset"
  };
  
  private static final EnumSet<TickType> TICKS = EnumSet.allOf(TickType.class);
  
  private HashMap< Class<? extends Event>, HashSet<LuaClosure> > eventHandlers = null;
  
  private HashSet<LuaClosure> tickHandlers = null;
  
  public Varargs invoke(Varargs args) {
    try {
      switch ( opcode ) {
      
      case INIT:
      {
        LuaValue env = args.arg(2);
        LuaTable t = new LuaTable();
        bind( t, getClass(), NAMES, ON );
        env.set("forge", t);
        env.get("package").get("loaded").set("forge", t);
        return t;
      }
      
      case ON:
      {
        Class<?> eventClass = Class.class.cast(args.checkuserdata(1, Class.class));
        if (!Event.class.isAssignableFrom(eventClass))
          throw new LuaError("the supplied class is not a subclass of net.minecraftforge.event.Event");
        
        LuaClosure eventHandler = args.checkclosure(2);
        on(eventClass.asSubclass(Event.class), eventHandler);
        return NIL;
      }
      
      case OFF:
      {
        Class<?> eventClass = Class.class.cast(args.checkuserdata(1, Class.class));
        if (!Event.class.isAssignableFrom(eventClass))
          throw new LuaError("the supplied class is not a subclass of net.minecraftforge.event.Event");
        
        LuaClosure eventHandler = args.checkclosure(2);
        off(eventClass.asSubclass(Event.class), eventHandler);
        return NIL;
      }
      
      case ON_TICK:
      {
        LuaClosure eventHandler = args.checkclosure(1);
        onTick(eventHandler);
        return NIL;
      }
      
      case OFF_TICK:
      {
        LuaClosure eventHandler = args.checkclosure(1);
        offTick(eventHandler);
        return NIL;
      }
      
      case PRINT:
      {
        StringBuilder buffer = new StringBuilder();
        int len = args.narg()+1;
        for (int i = 1; i < len; i++)
          buffer.append(args.arg(i).tojstring());
        print(buffer.toString());
        return NIL;
      }
      
      case SIDE:
      {
        return LuaValue.valueOf(side().toString());
      }
      
      case RESET:
      {
        reset();
        return NIL;
      }
      
      default:
        throw new LuaError("not yet supported: "+this);
        
      }
    } catch (LuaError e) {
      throw e;
    } catch (Exception e) {
      throw new LuaError(e);
    }
  }
  
  private HashMap<Class<? extends Event>,HashSet<LuaClosure>> getEventHandlers()
  {
    if (eventHandlers == null)
    {
      eventHandlers = new HashMap< Class<? extends Event>, HashSet<LuaClosure> >();
      MinecraftForge.EVENT_BUS.register(this);
    }
    return eventHandlers;
  }
  
  private HashSet<LuaClosure> getTickHandlers()
  {
    if (tickHandlers == null)
    {
      tickHandlers = new HashSet<LuaClosure>();
      TickRegistry.registerTickHandler(this, side());
    }
    return tickHandlers;
  }
  
  public abstract Side side();
  
  public void on(Class<? extends Event> eventClass, LuaClosure eventHandler)
  {
    HashMap<Class<? extends Event>, HashSet<LuaClosure>> eventHandlers = getEventHandlers();
    HashSet<LuaClosure> listeners = eventHandlers.get(eventClass);
    if (listeners == null)
      eventHandlers.put(eventClass, listeners = new HashSet<LuaClosure>());
    
    listeners.add(eventHandler);
  }
  
  public void off(Class<? extends Event> eventClass, LuaClosure eventHandler)
  {
    HashMap<Class<? extends Event>, HashSet<LuaClosure>> eventHandlers = getEventHandlers();
    HashSet<LuaClosure> listeners = eventHandlers.get(eventClass);
    if (listeners != null)
      listeners.remove(eventHandler);
    
    if (listeners.isEmpty())
      eventHandlers.remove(eventClass);
  }
  
  private void onTick(LuaClosure eventHandler)
  {
    HashSet<LuaClosure> tickHandlers = getTickHandlers();
    tickHandlers.add(eventHandler);
  }

  private void offTick(LuaClosure eventHandler)
  {
    HashSet<LuaClosure> tickHandlers = getTickHandlers();
    tickHandlers.remove(eventHandler);
  }

  public void print(String string)
  {
    ChatMessageComponent messageComponent = ChatMessageComponent.createFromText(string);
    switch (side())
    {
    case CLIENT:
      Minecraft.getMinecraft().thePlayer.sendChatToPlayer(messageComponent);
      break;
      
    case SERVER:
      {
        ServerConfigurationManager configurationManager = MinecraftServer.getServer().getConfigurationManager();
        for (String username : configurationManager.getAllUsernames())
        {
          EntityPlayerMP player = configurationManager.getPlayerForUsername(username);
          player.sendChatToPlayer(messageComponent);
        }
      }
      break;
    }
  }
  
  public void reset()
  {
    
    if (eventHandlers != null)
    {
      MinecraftForge.EVENT_BUS.unregister(this);
      eventHandlers.clear();
      eventHandlers = null;
    }

    if (tickHandlers != null)
    {
      // TODO: memory leak - cannot unregister tick handler
      tickHandlers.clear();
      tickHandlers = null;
    }
    
    switch (side())
    {
    case CLIENT:
      MSP.proxy.initializeClientScriptEngine();
      break;
      
    case SERVER:
      
      MSP.proxy.initializeServerScriptEngine();
      break;
    }    
  }
  
  @ForgeSubscribe()
  public void fire(Event event)
  {
    try
    {
      HashMap<Class<? extends Event>, HashSet<LuaClosure>> eventHandlers = getEventHandlers();
      Class<? extends Event> eventClass = event.getClass();
      for (Entry<Class<? extends Event>, HashSet<LuaClosure>> entry : eventHandlers.entrySet())
        if (entry.getKey().isAssignableFrom(eventClass))
          for (LuaClosure eventListener : entry.getValue())
            eventListener.call(CoerceJavaToLua.coerce(event));
    } catch (Exception e)
    {
      e.printStackTrace();
    }
  }
  
  @Override
  public String getLabel()
  {
    return getClass().getName();
  }
  
  @Override
  public EnumSet<TickType> ticks()
  {
    return TICKS;
  }

  @Override
  public void tickStart(EnumSet<TickType> type, Object... tickData)
  {
    try
    {
      for (LuaClosure tickHandler : getTickHandlers())
        tickHandler.call(LuaValue.valueOf(true), CoerceJavaToLua.coerce(type), CoerceJavaToLua.coerce(tickData));
    } catch (Exception e)
    {
      e.printStackTrace();
    }
  }
  
  @Override
  public void tickEnd(EnumSet<TickType> type, Object... tickData)
  {
    try
    {
      for (LuaClosure tickHandler : getTickHandlers())
        tickHandler.call(LuaValue.valueOf(false), CoerceJavaToLua.coerce(type), CoerceJavaToLua.coerce(tickData));
    } catch (Exception e)
    {
      e.printStackTrace();
    }
  }
  
}
