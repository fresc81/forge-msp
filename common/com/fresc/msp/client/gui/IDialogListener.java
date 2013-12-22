package com.fresc.msp.client.gui;

import net.minecraft.client.gui.GuiScreen;

public interface IDialogListener
{

  void confirmClicked(GuiScreen dialog, boolean okay);

}
