package com.fresc.msp.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.GuiYesNo;

public class GuiScreenFileDialog extends GuiYesNo
{
  GuiTextField filename;
  
  public GuiScreenFileDialog(GuiScreen parentScreen)
  {
    super(parentScreen, "Enter Filename:", "", "Okay", "Cancel", Integer.MIN_VALUE);
  }

  @Override
  public void initGui()
  {
    super.initGui();
    filename = new GuiTextField(fontRenderer, this.width / 2 - 155, 90, 310, 20);
    
  }
  
  @Override
  public void onGuiClosed()
  {
    super.onGuiClosed();
  }
  
  @Override
  protected void keyTyped(char par1, int par2)
  {
    super.keyTyped(par1, par2);
    filename.textboxKeyTyped(par1, par2);
  }
  
  @Override
  protected void mouseClicked(int par1, int par2, int par3)
  {
    super.mouseClicked(par1, par2, par3);
    filename.mouseClicked(par1, par2, par3);
  }
  
  @Override
  public void drawScreen(int mouseX, int mouseY, float partialTicks)
  {
    super.drawScreen(mouseX, mouseY, partialTicks);
    filename.drawTextBox();
  }
  
  @Override
  protected void actionPerformed(GuiButton par1GuiButton)
  {
    super.actionPerformed(par1GuiButton);
  }

  public String getFilename()
  {
    return filename.getText();
  }
  
}
