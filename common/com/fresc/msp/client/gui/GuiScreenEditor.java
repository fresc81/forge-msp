package com.fresc.msp.client.gui;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import org.lwjgl.input.Keyboard;

import com.fresc.msp.client.renderer.MonospaceFontRenderer;

public class GuiScreenEditor extends GuiScreen implements IButtonProvider
{
  
  private static final int GUIID_HSCROLL = 0;

  private static final int GUIID_VSCROLL = 1;

  private static final int GUIID_TOOLS   = 2;

  private MonospaceFontRenderer monospaceFontRenderer;
  
  private GuiToolbar tools;
  
  private GuiEditorClientArea client;
  
  private GuiScrollBar hScroll;
  
  private GuiScrollBar vScroll;
  
  private GuiComponent draggedComponent;
  
  private boolean initialized;
  
  private LinkedHashSet<IDialogListener> dialogListeners;

  private GuiScreen previousScreen;
  
  public GuiScreenEditor()
  {
    super();
    dialogListeners = new LinkedHashSet<IDialogListener>();
    initGui();
    //initialized = false;
  }
  
  @SuppressWarnings("unchecked")
  public List<GuiButton> getButtons()
  {
    GuiButton[] buttons = new GuiButton[buttonList.size()];
    buttonList.toArray(buttons);
    return Collections.unmodifiableList(Arrays.asList(buttons));
  }
  
  public Minecraft getClient()
  {
    return mc;
  }
  
  public FontRenderer getFontRenderer()
  {
    return fontRenderer;
  }
  
  public MonospaceFontRenderer getMonospaceFontRenderer()
  {
    return monospaceFontRenderer;
  }
  
  public GuiComponent getDraggedComponent()
  {
    return draggedComponent;
  }
  
  public GuiToolbar getTools()
  {
    return tools;
  }
  
  public float getHorizontalProgress()
  {
    return hScroll.getValue();
  }
  
  public void setHorizontalProgress(float value)
  {
    if (hScroll != null)
    {
      hScroll.setValue(value);
    }
  }
  
  public float getVerticalProgress()
  {
    return vScroll.getValue();
  }
  
  public void setVerticalProgress(float value)
  {
    if (vScroll != null)
    {
      vScroll.setValue(value);
    }
  }
  
  private void updateScrollbars()
  {
    if (hScroll != null)
    {
      int clientWidth = hScroll.getWidth();
      hScroll.setHandleSize(Math.round(((float)clientWidth / (float)client.getClientWidth()) * (float)clientWidth));
    }

    if (vScroll != null)
    {
      int clientHeight = vScroll.getHeight();
      vScroll.setHandleSize(Math.round(((float)clientHeight / (float)client.getClientHeight()) * (float)clientHeight));
    }
  }
  
  public void validate()
  {
    updateScrollbars();
  }

  @Override
  protected void keyTyped(char chr, int key)
  {
    client.keyTyped(chr, key);
  }
  
  @Override
  protected void actionPerformed(GuiButton button)
  {
    switch (button.id)
    {
    case GUIID_HSCROLL:
    case GUIID_VSCROLL:
      break;
      
    default:
      this.tools.actionPerformed(button);
      break;
    }
  }
  
  @Override
  public void confirmClicked(boolean par1, int par2)
  {
    System.out.println(par1 ? "Okay clicked" : "Cancel clicked");
    GuiScreen dialog = mc.currentScreen;
    mc.displayGuiScreen(this);
    
    for (IDialogListener listener : dialogListeners)
      listener.confirmClicked(dialog, par1);
  }
  
  @Override
  protected void mouseClicked(int x, int y, int button)
  {
    super.mouseClicked(x, y, button);
    
    // dispatch mouse events...
    if (client.containsMousePointer(x, y))
    {
      draggedComponent = client;
      draggedComponent.mouseClicked(draggedComponent.clampHorizontal(x), draggedComponent.clampVertical(y), button);
    } else if (tools.containsMousePointer(x, y))
    {
      draggedComponent = tools;
      draggedComponent.mouseClicked(draggedComponent.clampHorizontal(x), draggedComponent.clampVertical(y), button);
    } else if (hScroll.containsMousePointer(x, y))
    {
      draggedComponent = hScroll;
      draggedComponent.mouseClicked(draggedComponent.clampHorizontal(x), draggedComponent.clampVertical(y), button);
    } else if (vScroll.containsMousePointer(x, y))
    {
      draggedComponent = vScroll;
      draggedComponent.mouseClicked(draggedComponent.clampHorizontal(x), draggedComponent.clampVertical(y), button);
    }
    else
      draggedComponent = null;
    
  }
  
  @Override
  protected void mouseClickMove(int x, int y, int button, long time)
  {
    super.mouseClickMove(x, y, button, time);
    
    // dispatch mouse events...
    if (draggedComponent != null)
      draggedComponent.mouseClickMove(draggedComponent.clampHorizontal(x), draggedComponent.clampVertical(y), button, time);
    
  }
  
  @Override
  protected void mouseMovedOrUp(int x, int y, int button)
  {
    super.mouseMovedOrUp(x, y, button);
    
    // dispatch mouse events...
    if (draggedComponent != null)
    {
      draggedComponent.mouseMovedOrUp(draggedComponent.clampHorizontal(x), draggedComponent.clampVertical(y), button);
      draggedComponent = null;
    }
    
  }
  
  @Override
  public void setWorldAndResolution(Minecraft mc, int width, int height)
  {
    this.mc = mc;
    this.fontRenderer = mc.fontRenderer;
    this.width = width;
    this.height = height;
    
    this.initGui();
    
    int toolsX = 0, toolsY = 0, toolsWidth = width, toolsHeight = 20;
    int clientX = 0, clientY = toolsHeight, clientWidth = width - 15, clientHeight = height - 15 - toolsHeight;
    int hSliderX = 0, hSliderY = clientHeight+toolsHeight, hSliderWidth = clientWidth, hSliderHeight = 15;
    int vSliderX = clientWidth, vSliderY = toolsHeight, vSliderWidth = 15, vSliderHeight = clientHeight;
    
    tools.setXPos(toolsX); tools.setYPos(toolsY);
    tools.setWidth(toolsWidth); tools.setHeight(toolsHeight);
    
    client.setXPos(clientX); client.setYPos(clientY);
    client.setWidth(clientWidth); client.setHeight(clientHeight);
    
    hScroll.setXPos(hSliderX); hScroll.setYPos(hSliderY);
    hScroll.setWidth(hSliderWidth); hScroll.setHeight(hSliderHeight);
    
    vScroll.setXPos(vSliderX); vScroll.setYPos(vSliderY);
    vScroll.setWidth(vSliderWidth); vScroll.setHeight(vSliderHeight);
    
    Keyboard.enableRepeatEvents(true);
    validate();
  }

  @Override
  public void initGui()
  {
    if (!initialized)
    {
      this.buttonList.clear();
      this.monospaceFontRenderer = MonospaceFontRenderer.getInstance();
      
      int toolsX = 0, toolsY = 0, toolsWidth = width, toolsHeight = 20;
      int clientX = 0, clientY = toolsHeight, clientWidth = width - 15, clientHeight = height - 15 - toolsHeight;
      int hSliderX = 0, hSliderY = clientHeight+toolsHeight, hSliderWidth = clientWidth, hSliderHeight = 15;
      int vSliderX = clientWidth, vSliderY = toolsHeight, vSliderWidth = 15, vSliderHeight = clientHeight;
      
      this.tools = new GuiToolbar(this, this, toolsX, toolsY, toolsWidth, toolsHeight, GUIID_TOOLS, 40, 15);
      this.client = new GuiEditorClientArea(this, clientX, clientY, clientWidth, clientHeight);
      this.hScroll = new GuiScrollBar(this, this, GUIID_HSCROLL, hSliderX, hSliderY, hSliderWidth, hSliderHeight, hSliderWidth, true, 0f, 1f);
      this.vScroll = new GuiScrollBar(this, this, GUIID_VSCROLL, vSliderX, vSliderY, vSliderWidth, vSliderHeight, vSliderHeight, false, 0f, 1f);
      
      this.draggedComponent = null;
      initialized = true;
    }
  }
  
  @Override
  public void onGuiClosed()
  {
    Keyboard.enableRepeatEvents(false);
  }
  
  @Override
  public void drawScreen(int x, int y, float partialTickTime)
  {
    drawDefaultBackground();
    
    this.client.draw(mc, x, y);
    this.tools.draw(mc, x, y);
    this.hScroll.draw(mc, x, y);
    this.vScroll.draw(mc, x, y);
    
    super.drawScreen(x, y, partialTickTime);
  }
  
  @Override
  public void drawDefaultBackground()
  {
    drawWorldBackground(0);
  }
  
  @Override
  public boolean doesGuiPauseGame()
  {
    return false;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void registerButton(GuiButton button)
  {
    this.buttonList.add(button);
  }
  
  @Override
  public void unregisterButton(GuiButton button)
  {
    this.buttonList.remove(button);
  }
  
  public void clear()
  {
    client.clear();
  }

  public void load(InputStream inputStream, boolean append) throws IOException
  {
    client.load(inputStream, append);
  }
  
  public void save(OutputStream outputStream) throws IOException
  {
    client.save(outputStream);
  }
  
  public void addDialogListener(IDialogListener listener)
  {
    dialogListeners.add(listener);
  }
  
  public void removeDialogListener(IDialogListener listener)
  {
    dialogListeners.remove(listener);
  }

  public void toggleVisibility()
  {
    if (mc == null)
      mc = Minecraft.getMinecraft();
    
    if (isVisible())
    {
      
      mc.displayGuiScreen(previousScreen);
      previousScreen = null;
      
    } else
    {
      
      previousScreen = mc.currentScreen;
      mc.displayGuiScreen(this);
      
    }
  }

  private boolean isVisible()
  {
    return mc.currentScreen == this;
  }
  
}
