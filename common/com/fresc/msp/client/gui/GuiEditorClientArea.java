package com.fresc.msp.client.gui;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.LinkedList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.ChatAllowedCharacters;

import org.lwjgl.input.Keyboard;

import com.fresc.msp.client.renderer.MonospaceFontRenderer;

public class GuiEditorClientArea extends GuiComponent
{

  // TODO: implement selection
  
  private static final int COLOR_BACKGROUND = 0x88000000;

  private static final int COLOR_CURSOR = 0xFFAAAAAA;

  private final GuiScreenEditor editor;
  
  private int clientWidth;
  
  private int clientHeight;
  
  private StringBuilder buffer;
  
  private int cursorInBuffer;
  private int cursorX;
  private int cursorY;

  public GuiEditorClientArea(GuiScreenEditor editor, int x, int y, int width, int height)
  {
    super(editor, x, y, width, height);
    this.editor = editor;
    this.xPosition = x;
    this.yPosition = y;
    this.width = width;
    this.height = height;
    this.clientWidth = width;
    this.clientHeight = height;
    this.buffer = new StringBuilder();
    this.cursorInBuffer = 0;
    this.cursorX = 0;
    this.cursorY = 0;
    validate();
  }
  
  @Override
  public GuiScreenEditor getParent()
  {
    return GuiScreenEditor.class.cast(super.getParent());
  }
  
  public String[] getBufferLines()
  {
    LinkedList<String> lines = new LinkedList<String>();
    StringBuilder line = new StringBuilder();
    
    for (int i = 0; i < buffer.length(); i++)
    {
      char chr = buffer.charAt(i);
      if (chr == '\n')
      {
        lines.add(line.toString());
        line.setLength(0);
      } else
      {
        line.append(chr);
      }
    }
    lines.add(line.toString());
    return lines.toArray(new String[lines.size()]);
  }

  @Override
  protected void draw(Minecraft client, int x, int y)
  {
    // TODO: find out how to use GL scissor for client area
    //GL11.glEnable(GL11.GL_SCISSOR_TEST);
    //GL11.glScissor(0, 0, 0, 0);
    
    // background
    Gui.drawRect(xPosition, yPosition, xPosition+width, yPosition+height, COLOR_BACKGROUND);
    
    float xProgress = editor.getHorizontalProgress();
    float yProgress = editor.getVerticalProgress();
    
    // draw only visible part of the client area
    float xPos = Math.max(0, (xProgress * (clientWidth - width + 10)));
    float yPos = Math.max(0, (yProgress * (clientHeight - height + 10)));
    
    MonospaceFontRenderer monospaceFontRenderer = editor.getMonospaceFontRenderer();
    String[] lines = getBufferLines();
    int lineNumber = 0;
    for (String line : lines)
      monospaceFontRenderer.drawString(
          line,
          Math.round(xPosition + 5 - xPos),
          Math.round(yPosition + 5 - yPos + (lineNumber++ * MonospaceFontRenderer.FONT_HEIGHT))
      );
    
    drawCursor(xPos, yPos);
    
    // cleanup scissor
    //GL11.glDisable(GL11.GL_SCISSOR_TEST);
  }

  private void drawCursor(float xPos, float yPos)
  {
    int xOffset = Math.round(xPosition + 5 - xPos) + (cursorX*MonospaceFontRenderer.FONT_WIDTH);
    int yOffset = Math.round(yPosition + 5 - yPos) + (cursorY*MonospaceFontRenderer.FONT_HEIGHT);
    this.drawVerticalLine(xOffset, yOffset-2, yOffset-2+MonospaceFontRenderer.FONT_HEIGHT, COLOR_CURSOR);
  }

  @Override
  protected void validate()
  {
    MonospaceFontRenderer monospaceFontRenderer = editor.getMonospaceFontRenderer();
    
    String[] lines = getBufferLines();
    int longestLine = 0;
    for (String line : lines)
      longestLine = Math.max(longestLine, monospaceFontRenderer.getTextWidth(line));
    
    this.clientWidth = Math.max(width, longestLine);
    this.clientHeight = Math.max(height, monospaceFontRenderer.getTextHeight(lines.length));
    
    this.editor.validate();
    scrollToCursor();
  }
  
  private void scrollToCursor()
  {
    String[] lines = getBufferLines();
    int longestLine = 0;
    for (String line : lines)
      longestLine = Math.max(longestLine, line.length());
    
    float dx = (float) cursorX / (float) longestLine;
    float dy = (float) cursorY / (float) lines.length;
    
    this.editor.setHorizontalProgress(dx);
    this.editor.setVerticalProgress(dy);
  }

  public void keyTyped(char chr, int key)
  {
    int x0, x1;
    switch (key)
    {
    case Keyboard.KEY_BACK:
      if (cursorInBuffer > 0)
      {
        --cursorX;
        if (cursorX < 0)
        {
          --cursorY;
          cursorX = getLineWidth(cursorY);
        }
        buffer.deleteCharAt(--cursorInBuffer);
        validate();
      }
      break;

    case Keyboard.KEY_RETURN:
      buffer.insert(cursorInBuffer++, '\n');
      cursorX = 0;
      ++cursorY;
      validate();
      break;
    
    case Keyboard.KEY_DELETE:
      if (cursorInBuffer < buffer.length())
      {
        buffer.deleteCharAt(cursorInBuffer);
        validate();
      }
      break;

    case Keyboard.KEY_LEFT:
      if (cursorInBuffer > 0)
      {
        --cursorInBuffer;
        --cursorX;
        if (cursorX < 0)
        {
          --cursorY;
          cursorX = getLineWidth(cursorY);
        }
        scrollToCursor();
      }
      break;

    case Keyboard.KEY_RIGHT:
      if (cursorInBuffer < buffer.length())
      {
        ++cursorInBuffer;
        ++cursorX;
        if (cursorX > getLineWidth(cursorY))
        {
          cursorX = 0;
          ++cursorY;
        }
        scrollToCursor();
      }
      break;

    case Keyboard.KEY_UP:
      if (cursorY > 0)
      {
        --cursorY;
        x0 = cursorX;
        x1 = getLineWidth(cursorY);
        cursorX = Math.min(cursorX, x1);
        cursorInBuffer -= x0 + (x1-cursorX) + 1;
        scrollToCursor();
      }
      break;

    case Keyboard.KEY_DOWN:
      if (cursorY < getBufferLines().length-1)
      {
        x0 = cursorX;
        x1 = getLineWidth(cursorY);
        ++cursorY;
        cursorX = Math.min(cursorX, getLineWidth(cursorY));
        cursorInBuffer += cursorX + (x1-x0) + 1;
        scrollToCursor();
      }
      break;

    case Keyboard.KEY_HOME:
      cursorInBuffer -= cursorX;
      cursorX = 0;
      scrollToCursor();
      break;

    case Keyboard.KEY_END:
      cursorInBuffer += getLineWidth(cursorY) - cursorX;
      cursorX = getLineWidth(cursorY);
      scrollToCursor();
      break;

    default:
      if (ChatAllowedCharacters.isAllowedCharacter(chr))
      {
        buffer.insert(cursorInBuffer++, chr);
        ++cursorX;
        validate();
      }
      break;
    }
    
  }
  
  private int getLineWidth(int index)
  {
    String[] lines = getBufferLines();
    if ((index >= 0) && (index < lines.length))
      return lines[index].length();
    return 0;
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

  public GuiScreenEditor getEditor()
  {
    return editor;
  }

  public int getClientHeight()
  {
    return clientHeight;
  }
  
  public void setClientHeight(int clientHeight)
  {
    this.clientHeight = Math.max(height, clientHeight);
    this.validate();
  }
  
  public int getClientWidth()
  {
    return clientWidth;
  }
  
  public void setClientWidth(int clientWidth)
  {
    this.clientWidth = Math.max(width, clientWidth);
    this.validate();
  }

  public void load(InputStream inputStream, boolean append) throws IOException
  {
    cursorX = 0;
    cursorY = 0;
    cursorInBuffer = 0;
    
    if (!append)
      this.clear();
    
    InputStreamReader reader = null;
    try
    {
      reader = new InputStreamReader(inputStream, Charset.forName("ascii"));
      BufferedReader breader = new BufferedReader(reader);
      
      String line;
      
      while ((line = breader.readLine()) != null)
        buffer.append(line).append('\n');
      
      validate();
      
    } finally
    {
      
      if (reader != null)
        reader.close();
      
    }
  }

  public void save(OutputStream outputStream) throws IOException
  {
    OutputStreamWriter writer = null;
    try
    {
      writer = new OutputStreamWriter(outputStream, Charset.forName("ascii"));
      BufferedWriter bwriter = new BufferedWriter(writer);
      
      bwriter.write(buffer.toString());
      bwriter.flush();
      
    } finally {
      
      if (writer != null)
        writer.close();
      
    }
    writer.close();
  }

  public void clear()
  {
    buffer.setLength(0);
    cursorInBuffer = 0;
    cursorX = 0;
    cursorY = 0;
    validate();
  }

}
