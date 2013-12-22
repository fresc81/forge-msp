package com.fresc.msp.client.gui;

import java.util.ArrayList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public class GuiToolbar extends GuiComponent
{

  private static final int COLOR_BACKGROUND = 0xFF000000;
  
  private final IButtonProvider buttonProvider;
  
  private final int firstId;
  
  private int nextId;

  private int buttonWidth;

  private int buttonHeight;

  private ArrayList<GuiButton> buttons;
  private ArrayList<IToolbarListener> listeners;

  public GuiToolbar(GuiScreen parent, IButtonProvider buttonProvider, int xPosition, int yPosition, int width, int height, int nextId, int buttonWidth, int buttonHeight)
  {
    super(parent, xPosition, yPosition, width, height);
    this.buttonProvider = buttonProvider;
    this.firstId =  nextId;
    this.nextId = nextId;
    this.buttonWidth = buttonWidth;
    this.buttonHeight = buttonHeight;
    this.buttons = new ArrayList<GuiButton>();
    this.listeners = new ArrayList<IToolbarListener>();
    validate();
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
  protected void draw(Minecraft client, int x, int y)
  {
    drawRect(xPosition, yPosition, xPosition+width, yPosition+height, COLOR_BACKGROUND);
  }
  
  public IButtonProvider getButtonProvider()
  {
    return buttonProvider;
  }
  
  public ArrayList<GuiButton> getButtons()
  {
    return buttons;
  }
  
  public GuiButton createButton(String title, IToolbarListener listener)
  {
    // reuse freed button ids
    int id = -1;
    for (int i = 0; (id == -1) && (i < this.buttons.size()); i++)      
      if (this.buttons.get(i) == null)
        id = firstId + i;
    
    // if no reusable id found allocate new id
    boolean newId = false;
    if (id == -1)
    {
      id = nextId++;
      newId = true;
    }
    
    int x = xPosition + 2 + ((id - firstId) * (buttonWidth+2));
    int y = yPosition + 2;
    
    GuiButton button = new GuiButton(id, x, y, buttonWidth, buttonHeight, title);
    if (newId)
    {
      this.buttons.add(button.id - firstId, button);
      this.buttonProvider.registerButton(button);
      this.listeners.add(button.id - firstId, listener);
    } else
    {
      this.buttons.set(button.id - firstId, button);
      this.buttonProvider.registerButton(button);
      this.listeners.set(button.id - firstId, listener);
    }
    validate();
    return button;
  }
  
  public void deleteButton(GuiButton button)
  {
    if (buttons.contains(button))
    {
      this.buttons.set(button.id - firstId, null);
      this.buttonProvider.unregisterButton(button);
      this.listeners.set(button.id - firstId, null);
      validate();
    }
  }

  void actionPerformed(GuiButton button)
  {
    for (GuiButton item : buttons)
      if ((item != null) && (item.id == button.id))
      {
        IToolbarListener listener = listeners.get(item.id - firstId);
        if (listener != null)
          listener.actionPerformed(button);
      }
  }

}
