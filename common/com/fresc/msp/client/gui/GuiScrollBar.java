package com.fresc.msp.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public class GuiScrollBar extends GuiComponent
{
  
  private class GuiScrollBarHandle extends GuiButton
  {

    private GuiScrollBarHandle(int id, int x, int y, int width, int height)
    {
      super(id, x, y, width, height, "");
    }

    public void setPosition(int x, int y)
    {
      this.xPosition = x;
      this.yPosition = y;
    }

    public void setSize(int width, int height)
    {
      this.width = width;
      this.height = height;
    }
    
    @Override
    public void drawButton(Minecraft mc, int x, int y)
    {
      int clampedX0 = clampHorizontal(xPosition);
      int clampedY0 = clampVertical(yPosition);
      int clampedX1 = clampHorizontal(xPosition+width);
      int clampedY1 = clampVertical(yPosition+height);
      int color = COLOR_INNER_ACTIVE;
      
      if (!active)
      {
        switch (getHoverState(x >= clampedX0 && y >= clampedY0 && x < clampedX1 && y < clampedY1))
        {
        case 0:
          color = COLOR_OUTER;
          break;
        case 1:
          color = COLOR_INNER_INACTIVE;
          break;
        case 2:
          color = COLOR_INNER_HOVERED;
          break;
        }
      }
      
      drawRect(clampedX0, clampedY0, clampedX1, clampedY1, color);
    }
    
  }

  private static final int COLOR_INNER_INACTIVE = 0xFFAAAAAA;

  private static final int COLOR_INNER_HOVERED = 0xFFEEEEEE;

  private static final int COLOR_INNER_ACTIVE = 0xFFFFFFFF;

  private static final int COLOR_OUTER = 0xFF888888;

  private static final int COLOR_BORDER = 0xFF000000;

  private int handleSize;
  
  private boolean horizontal;
  
  private float value;
  
  private float maxValue;

  private boolean visible;

  private boolean active;
  
  private int lastActivePos;
  
  private GuiScrollBarHandle handle;

  public GuiScrollBar(GuiScreen parent, IButtonProvider buttonProvider, int id, int x, int y, int width, int height, int handleSize, boolean horizontal, float value, float maxValue)
  {
    super(parent, x, y, width, height);
    this.handleSize = handleSize;
    this.horizontal = horizontal;
    this.value = value;
    this.maxValue = maxValue;
    this.visible = true;
    this.active = false;
    this.handle = new GuiScrollBarHandle(id, x, y, handleSize, handleSize);
    buttonProvider.registerButton(handle);
    validate();
  }
  
  @Override
  void mouseClicked(int x, int y, int button)
  {
    active = true;
    lastActivePos = horizontal ? x : y;
  }
  
  @Override
  void mouseClickMove(int x, int y, int button, long time)
  {
    if (active)
    {
      int diff = horizontal ? x - lastActivePos : y - lastActivePos;
      int min = horizontal ? xPosition : yPosition;
      int max = (horizontal ? width : height) - handleSize;
      value += ((float) diff) / (float)(max - min);
      clampValue();
      lastActivePos = horizontal ? x : y;
    }
  }

  private void clampValue()
  {
    value = (Float.isNaN(value) || Float.isInfinite(value)) ? 0 : Math.min(maxValue, Math.max(0, value));
  }

  @Override
  void mouseMovedOrUp(int x, int y, int button)
  {
    active = false;
    lastActivePos = 0;
  }
  
  @Override
  public boolean containsMousePointer(int mouseX, int mouseY)
  {
    // mouse over handle?
    if (horizontal)
      return
          (mouseX >= this.xPosition+this.getOffset()) &&
          (mouseX <= this.xPosition+this.getOffset()+this.handleSize) &&
          (mouseY >= this.yPosition) &&
          (mouseY <= this.yPosition+this.height);
    else
      return
          (mouseX >= this.xPosition) &&
          (mouseX <= this.xPosition+this.width) &&
          (mouseY >= this.yPosition+this.getOffset()) &&
          (mouseY <= this.yPosition+this.getOffset()+handleSize);
  }
  
  @Override
  protected void draw(Minecraft client, int x, int y)
  {
    if (this.visible)
    {
      
      if (this.horizontal)
        this.drawHorizontal(client, x, y);
      else
        this.drawVertical(client, x, y);
      
      this.drawBorder();
    }
  }
  
  private void drawBorder()
  {
    this.drawHorizontalLine(this.xPosition,              this.xPosition+this.width-1, this.yPosition,               COLOR_BORDER); // top
    this.drawHorizontalLine(this.xPosition,              this.xPosition+this.width-1, this.yPosition+this.height-1, COLOR_BORDER); // bottom
    this.drawVerticalLine  (this.xPosition,              this.yPosition,              this.yPosition+this.height-1, COLOR_BORDER); // left
    this.drawVerticalLine  (this.xPosition+this.width-1, this.yPosition,              this.yPosition+this.height-1, COLOR_BORDER); // right
  }

  private void drawHorizontal(Minecraft client, int x, int y)
  {
    // outer
    Gui.drawRect(this.xPosition, this.yPosition, this.xPosition+this.width, this.yPosition+this.height, COLOR_OUTER);
    
    // inner
    handle.setPosition(this.xPosition + this.getOffset(), this.yPosition);
    handle.setSize(this.handleSize, this.height);
    handle.drawButton(client, x, y);
  }

  private void drawVertical(Minecraft client, int x, int y)
  {
    // outer
    Gui.drawRect(this.xPosition, this.yPosition, this.xPosition+this.width, this.yPosition+this.height, COLOR_OUTER);
    
    // inner
    handle.setPosition(this.xPosition, this.yPosition + this.getOffset());
    handle.setSize(this.width, this.handleSize);
    handle.drawButton(client, x, y);
  }

  private int getOffset()
  {
    int min = horizontal ? xPosition : yPosition;
    int max = (horizontal ? xPosition + width : yPosition + height) - handleSize;
    return Math.round(((max - min) / maxValue) * value);
  }

  @Override
  protected void validate()
  {
  }
  
  public boolean isHorizontal()
  {
    return horizontal;
  }
  
  public void setHorizontal(boolean horizontal)
  {
    this.horizontal = horizontal;
    validate();
  }
  
  public float getValue()
  {
    return value;
  }
  
  public void setValue(float value)
  {
    this.value = value;
    clampValue();
    validate();
  }
  
  public int getHandleSize()
  {
    return handleSize;
  }
  
  public void setHandleSize(int handleSize)
  {
    this.handleSize = Math.max(20, handleSize);
    validate();
  }
  
  public float getMaxValue()
  {
    return maxValue;
  }
  
  public void setMaxValue(float maxValue)
  {
    this.maxValue = maxValue;
    validate();
  }
  
  public boolean isVisible()
  {
    return visible;
  }
  
  public void setVisible(boolean visible)
  {
    this.visible = visible;
    validate();
  }
  
  public GuiButton getHandle()
  {
    return handle;
  }
  
}
