package com.fresc.msp.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.fresc.msp.MSP;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetworkManager;

public class PacketRunScript extends PacketMSP
{
  
  private String name;
  private String script;
  
  public PacketRunScript()
  {
    this("", "");
  }
  
  public PacketRunScript(String name, String script)
  {
    super(PacketType.RUN_SCRIPT);
    this.name = name;
    this.script = script;
  }
  
  public String getName()
  {
    return name;
  }
  
  public void setName(String name)
  {
    this.name = name;
  }
  
  public String getScript()
  {
    return script;
  }
  
  public void setScript(String script)
  {
    this.script = script;
  }

  @Override
  protected void write(DataOutputStream dos) throws IOException
  {
    dos.writeUTF(name);
    dos.writeUTF(script);
  }

  @Override
  protected void read(DataInputStream dis) throws IOException
  {
    name = dis.readUTF();
    script = dis.readUTF();
  }
  
  @Override
  protected void process(INetworkManager manager, EntityPlayer entityPlayer)
  {
    MSP.proxy.runScriptServerSide(name, script, manager, entityPlayer);
  }
  
}
