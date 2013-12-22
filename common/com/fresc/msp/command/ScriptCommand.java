package com.fresc.msp.command;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetworkManager;
import net.minecraft.server.MinecraftServer;

import com.fresc.msp.MSP;

import cpw.mods.fml.relauncher.Side;

public class ScriptCommand extends CommandBase
{

  private final MinecraftServer server;

  public ScriptCommand(MinecraftServer server)
  {
    this.server = server;
  }

  @Override
  public String getCommandName()
  {
    return "script";
  }

  @Override
  public String getCommandUsage(ICommandSender sender)
  {
    return "commands.script.usage";
  }

  @Override
  public void processCommand(ICommandSender sender, String[] args)
  {
    if (args.length == 0)
    {
        throw new WrongUsageException("commands.script.usage");
    } else
    {
      Path scriptRootPath = MSP.proxy.getScriptRootPath(Side.SERVER);
      Path path = scriptRootPath.resolve(args[0]);
      if (Files.exists(path))
      {
          String code;
          try
          {
            code = new String(Files.readAllBytes(path));
            INetworkManager manager = null;
            EntityPlayer player = server.getConfigurationManager().getPlayerForUsername(sender.getCommandSenderName());
            MSP.proxy.runScriptServerSide(args[0], code, manager, player);
            return;
          } catch (IOException e)
          {
            throw new WrongUsageException("commands.script.usage");
          }
      }
    }
    throw new WrongUsageException("commands.script.usage");
  }
  
}
