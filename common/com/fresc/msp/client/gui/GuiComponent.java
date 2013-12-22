package com.fresc.msp.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;

public abstract class GuiComponent extends Gui
{
  
  protected final GuiScreen parent;

  protected int xPosition;
  
  protected int yPosition;
  
  protected int width;
  
  protected int height;
  
  public GuiComponent(GuiScreen parent, int xPosition, int yPosition, int width, int height)
  {
    super();
    this.parent = parent;
    this.xPosition = xPosition;
    this.yPosition = yPosition;
    this.width = width;
    this.height = height;
  }
  
  public GuiScreen getParent()
  {
    return parent;
  }

  public int getXPos()
  {
    return xPosition;
  }
  
  public void setXPos(int xPosition)
  {
    this.xPosition = xPosition;
    validate();
  }

  public int getYPos()
  {
    return yPosition;
  }

  public void setYPos(int yPosition)
  {
    this.yPosition = yPosition;
    validate();
  }

  public int getWidth()
  {
    return width;
  }
  
  public void setWidth(int width)
  {
    this.width = width;
    validate();
  }
  
  public int getHeight()
  {
    return height;
  }
  
  public void setHeight(int height)
  {
    this.height = height; 
    validate();
  }
  
  public boolean containsMousePointer(int mouseX, int mouseY)
  {
    return
        (mouseX >= xPosition) &&
        (mouseX <= xPosition+width) &&
        (mouseY >= yPosition) &&
        (mouseY <= yPosition+height)
        ;
  }
  
  public int clampHorizontal(int x)
  {
    return Math.min(xPosition+width, Math.max(x, xPosition));
  }
  
  public int clampVertical(int y)
  {
    return Math.min(yPosition+height, Math.max(y, yPosition));
  }
  
  abstract void mouseClicked(int x, int y, int button);
  
  abstract void mouseClickMove(int x, int y, int button, long time);
  
  abstract void mouseMovedOrUp(int x, int y, int button);
  
  protected abstract void validate();
  
  protected abstract void draw(Minecraft client, int mouseX, int mouseY);
  
}
