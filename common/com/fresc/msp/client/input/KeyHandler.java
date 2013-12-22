package com.fresc.msp.client.input;

import java.util.EnumSet;

import org.lwjgl.input.Keyboard;

import com.fresc.msp.proxy.ClientProxy;

import net.minecraft.client.settings.KeyBinding;
import cpw.mods.fml.client.registry.KeyBindingRegistry;
import cpw.mods.fml.common.TickType;

public class KeyHandler extends KeyBindingRegistry.KeyHandler
{

  private static final int DEFAULT_KEY = Keyboard.KEY_F9;

  private static final String LABEL = "Script Editor";

  private static final EnumSet<TickType> TICKS = EnumSet.of(TickType.CLIENT);
  
  private static final KeyBinding[] KEY_BINDINGS = new KeyBinding[] {
    new KeyBinding(LABEL, DEFAULT_KEY)
  };
  
  private static final boolean[] REPEAT_FLAGS = new boolean[] {
    true
  };

  private final ClientProxy clientProxy;
  
  public KeyHandler(ClientProxy clientProxy)
  {
    super(KEY_BINDINGS, REPEAT_FLAGS);
    this.clientProxy = clientProxy;
  }
  
  public ClientProxy getClientProxy()
  {
    return clientProxy;
  }

  @Override
  public String getLabel()
  {
    return LABEL;
  }

  @Override
  public void keyDown(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd, boolean isRepeat)
  {
  }

  @Override
  public void keyUp(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd)
  {
    if (tickEnd)
      clientProxy.getEditor().toggleVisibility();
  }

  @Override
  public EnumSet<TickType> ticks()
  {
    return TICKS;
  }

}
