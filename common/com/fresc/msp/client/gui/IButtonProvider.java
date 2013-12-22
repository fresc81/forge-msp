package com.fresc.msp.client.gui;

import net.minecraft.client.gui.GuiButton;

public interface IButtonProvider
{
  
  void registerButton(GuiButton button);

  void unregisterButton(GuiButton button);
  
}
