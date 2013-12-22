package com.fresc.msp.client.renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

public class MonospaceFontRenderer
{

  public static final int FONT_HEIGHT = 9;
  public static final int FONT_WIDTH  = 7;

  private static MonospaceFontRenderer instance = null;

  private FontRenderer fontRenderer;
  private ResourceLocation resourceLocation;
  private TextureManager textureManager;

  private float posX;
  private float posY;

  public static MonospaceFontRenderer getInstance()
  {
    if (instance == null)
    {
      Minecraft minecraft = Minecraft.getMinecraft();
      instance = new MonospaceFontRenderer(minecraft.fontRenderer, new ResourceLocation("textures/font/ascii.png"), minecraft.renderEngine);
    }
    return instance;
  }

  private MonospaceFontRenderer(FontRenderer fontRenderer, ResourceLocation resourceLocation, TextureManager textureManager)
  {
    this.fontRenderer = fontRenderer;
    this.resourceLocation = resourceLocation;
    this.textureManager = textureManager;
    textureManager.bindTexture(this.resourceLocation);
  }
  
  public int getTextWidth(String text)
  {
    int length = 0;
    for (int i = 0; i < text.length(); ++i)
    {
      char c0 = text.charAt(i);
      if (c0 == 167 && i + 1 < text.length())
        ++i;
      else
      {
        length += FONT_WIDTH;
      }
    }
    return length;
  }
  
  public int getTextHeight(int numLines)
  {
    return numLines * FONT_HEIGHT;
  }

  private void renderDefaultChar(int par1)
  {
    float f = par1 % 16 * 8;
    float f1 = par1 / 16 * 8;
    this.textureManager.bindTexture(this.resourceLocation);
    float f4 = ((float) FONT_WIDTH - (float) fontRenderer.getCharWidth((char) par1)) / 2f;

    GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
    GL11.glTexCoord2f(f / 128.0F, f1 / 128.0F);
    GL11.glVertex3f(this.posX + f4, this.posY, 0.0F);
    GL11.glTexCoord2f(f / 128.0F, (f1 + 7.99F) / 128.0F);
    GL11.glVertex3f(this.posX + f4, this.posY + 7.99F, 0.0F);
    GL11.glTexCoord2f((f + FONT_WIDTH - 1.0F) / 128.0F, f1 / 128.0F);
    GL11.glVertex3f(this.posX + FONT_WIDTH - 1.0F + f4, this.posY, 0.0F);
    GL11.glTexCoord2f((f + FONT_WIDTH - 1.0F) / 128.0F, (f1 + 7.99F) / 128.0F);
    GL11.glVertex3f(this.posX + FONT_WIDTH - 1.0F + f4, this.posY + 7.99F, 0.0F);
    GL11.glEnd();
  }

  public int drawString(String par1Str, int par2, int par3)
  {
    if (par1Str == null)
    {
      return 0;
    } else
    {
      GL11.glColor3f(1f, 1f, 1f);
      this.posX = par2;
      this.posY = par3;
      this.renderStringAtPos(par1Str);
      return (int) this.posX;
    }
  }

  private void renderCharAtPos(int par1)
  {
    this.renderDefaultChar(par1 + 32);
  }

  private void renderStringAtPos(String par1Str)
  {
    for (int i = 0; i < par1Str.length(); ++i)
    {
      char c0 = par1Str.charAt(i);
      if (c0 == 167 && i + 1 < par1Str.length())
        ++i;
      else
      {
        this.renderCharAtPos(ChatAllowedCharacters.allowedCharacters.indexOf(c0));
        this.posX += FONT_WIDTH;
      }
    }
  }

  public float getPosX()
  {
    return posX;
  }

  public float getPosY()
  {
    return posY;
  }

}
