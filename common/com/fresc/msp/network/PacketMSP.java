package com.fresc.msp.network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.EnumSet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;

import com.fresc.msp.MSP;

public abstract class PacketMSP
{
  
  public static enum PacketType
  {
    
    RUN_SCRIPT(PacketRunScript.class)
    ;
    
    private Class<? extends PacketMSP> packetClass;

    private PacketType(Class<? extends PacketMSP> packetClass)
    {
      this.packetClass = packetClass;
    }
    
    public static PacketType getByOrdinal(int ordinal)
    {
      if (ordinal >= 0 && ordinal < EnumSet.allOf(PacketType.class).size())
        return values()[ordinal];
      else
        return null;
    }
    
    public PacketMSP createPacket() throws InstantiationException, IllegalAccessException
    {
      return packetClass.newInstance();
    }
    
  }

  private PacketType packetType;
  
  public PacketMSP(PacketType packetType)
  {
    this.packetType = packetType;
  }
  
  public PacketType getPacketType()
  {
    return packetType;
  }

  public Packet250CustomPayload unwrap() throws IOException
  {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    DataOutputStream dos = new DataOutputStream(bos);
    dos.writeInt(this.packetType.ordinal());
    write(dos);
    dos.flush();
    Packet250CustomPayload packet = new Packet250CustomPayload(MSP.MOD_ID, bos.toByteArray());
    return packet;
  }
  
  public static PacketMSP wrap(Packet250CustomPayload packet) throws IOException, InstantiationException, IllegalAccessException
  {
    ByteArrayInputStream bis = new ByteArrayInputStream(packet.data);
    DataInputStream dis = new DataInputStream(bis);
    PacketType packetType = PacketType.getByOrdinal(dis.readInt());
    PacketMSP packetMSP = packetType.createPacket();
    packetMSP.read(dis);
    return packetMSP;
  }

  protected abstract void write(DataOutputStream dos) throws IOException;
  
  protected abstract void read(DataInputStream dis) throws IOException;

  protected abstract void process(INetworkManager manager, EntityPlayer entityPlayer);
  
}
