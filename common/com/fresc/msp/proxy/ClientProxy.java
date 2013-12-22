package com.fresc.msp.proxy;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import org.luaj.vm2.Globals;

import com.fresc.msp.client.gui.GuiScreenEditor;
import com.fresc.msp.client.gui.GuiScreenFileDialog;
import com.fresc.msp.client.gui.GuiToolbar;
import com.fresc.msp.client.gui.IDialogListener;
import com.fresc.msp.client.gui.IToolbarListener;
import com.fresc.msp.client.input.KeyHandler;
import com.fresc.msp.network.PacketMSP;
import com.fresc.msp.network.PacketRunScript;

import cpw.mods.fml.client.registry.KeyBindingRegistry;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.relauncher.Side;

public class ClientProxy extends CommonProxy
{
  
  private static enum FileDialogKind
  {
    LOAD,
    SAVE;
  }
  
  private class EditorController implements IToolbarListener, IDialogListener
  {

    private FileDialogKind editorDialogKind = null;
    
    @Override
    public void confirmClicked(GuiScreen dialog, boolean okay)
    {
      if (okay && (dialog instanceof GuiScreenFileDialog) && (editorDialogKind != null))
      {
        final GuiScreenFileDialog fileDialog = GuiScreenFileDialog.class.cast(dialog);
        Path filename = getScriptRootPath(null).resolve(fileDialog.getFilename());
        switch (editorDialogKind)
        {
        
        case LOAD:
          System.out.println("loading "+filename);
          editorDialogKind = null;
          try
          {
            editor.load(new FileInputStream(filename.toFile()), false);
          } catch (FileNotFoundException e)
          {
            e.printStackTrace();
          } catch (IOException e)
          {
            e.printStackTrace();
          }
          break;
        
        case SAVE:
          System.out.println("saving "+filename);
          editorDialogKind = null;
          try
          {
            editor.save(new FileOutputStream(filename.toFile()));
          } catch (FileNotFoundException e)
          {
            e.printStackTrace();
          } catch (IOException e)
          {
            e.printStackTrace();
          }
          break;
        
        }
      }
    }

    @Override
    public void actionPerformed(GuiButton button)
    {
      if (button == newButton)
      {
        // NEW button
        editor.clear();
      } else if (button == loadButton)
      {
        // LOAD button
        editorDialogKind = FileDialogKind.LOAD;
        Minecraft minecraft = Minecraft.getMinecraft();
        GuiScreenFileDialog files = new GuiScreenFileDialog(minecraft.currentScreen);
        minecraft.displayGuiScreen(files);
        
      } else if (button == saveButton)
      {
        // SAVE button
        editorDialogKind = FileDialogKind.SAVE;
        Minecraft minecraft = Minecraft.getMinecraft();
        GuiScreenFileDialog files = new GuiScreenFileDialog(minecraft.currentScreen);
        minecraft.displayGuiScreen(files);
        
      } else if (button == runLocalButton)
      {
        // LOCAL button
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try
        {
          editor.save(bos);
          runScriptClientSide("script.lua", bos.toString());
        } catch (Exception e)
        {
          e.printStackTrace();
        }
      } else if (button == runRemoteButton)
      {
        // REMOTE button
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try
        {
          editor.save(bos);
          String script = bos.toString();
          PacketMSP packet = new PacketRunScript("script.lua", script);
          PacketDispatcher.sendPacketToServer(packet.unwrap());
        } catch (Exception e)
        {
          e.printStackTrace();
        }
      }
    }
    
  }
  
  // the client runs two script engines, one for the client and one for the internal server
  protected Globals clientGlobals = null;
  
  protected GuiScreenEditor editor = null;
  
  private GuiButton newButton;

  private GuiButton loadButton;

  private GuiButton saveButton;

  private GuiButton runLocalButton;

  private GuiButton runRemoteButton;
  
  private EditorController editorController = new EditorController();

  public ClientProxy()
  {
  }
  
  @Override
  public void preInit(FMLPreInitializationEvent event)
  {
    super.preInit(event);
  }
  
  @Override
  public void init(FMLInitializationEvent event)
  {
    super.init(event);
    
  }
  
  @Override
  public void initializeClientScriptEngine()
  {
    finalizeClientScriptEngine();
    clientGlobals = new Globals();
    initializeGlobals(Side.CLIENT, clientGlobals);
  }
  
  @Override
  protected void finalizeClientScriptEngine()
  {
    if (clientGlobals != null)
    {
      finalizeGlobals(clientGlobals);
      clientGlobals = null;
    }
  }
  
  @Override
  protected void registerEvents()
  {
    super.registerEvents();
  }
  
  @Override
  public void postInit(FMLPostInitializationEvent event)
  {
    super.postInit(event);
    
    editor = new GuiScreenEditor();
    editor.addDialogListener(editorController);
    
    GuiToolbar tools = editor.getTools();
    newButton = tools.createButton("New", editorController);
    loadButton = tools.createButton("Load", editorController);
    saveButton = tools.createButton("Save", editorController);
    runLocalButton = tools.createButton("Local", editorController);
    runRemoteButton = tools.createButton("Remote", editorController);
    
    registerKeyHandlers();
  }
  
  protected void registerKeyHandlers()
  {
    KeyBindingRegistry.registerKeyBinding(new KeyHandler(this));
  }

  public GuiScreenEditor getEditor()
  {
    return editor;
  }
  
  @Override
  public void runScriptClientSide(String name, String code)
  {
    System.out.printf("running client side script %s with content:\n%s\n", name, code);
    try {
      clientGlobals.loadString(code, name).call();
    } catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
  
}
