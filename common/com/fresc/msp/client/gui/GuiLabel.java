package com.fresc.msp.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;

public class GuiLabel extends GuiComponent
{

  private FontRenderer fontRenderer;
  private String textLabel;
  private int colorLabel;

  public GuiLabel(GuiScreen parent, FontRenderer fontRenderer, int xPosition, int yPosition, String textLabel, int colorLabel)
  {
    super(parent, xPosition, yPosition, 0, 0);
    this.fontRenderer = fontRenderer;
    this.textLabel = textLabel;
    this.colorLabel = colorLabel;
  }

  @Override
  void mouseClicked(int x, int y, int button)
  {
  }

  @Override
  void mouseClickMove(int x, int y, int button, long time)
  {
  }

  @Override
  void mouseMovedOrUp(int x, int y, int button)
  {
  }

  @Override
  protected void validate()
  {
  }

  @Override
  protected void draw(Minecraft client, int mouseX, int mouseY)
  {
    this.drawString(fontRenderer, textLabel, xPosition, yPosition, colorLabel);
  }

}
