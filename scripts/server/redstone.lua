
-------------------------
---- REDSTONE  UTILS ----
-------------------------
-- author: Paul Bottin --
-------------------------

local redstone = {}


--- java classes ---

local MinecraftServer = luajava.bindClass("net.minecraft.server.MinecraftServer")
local ForgeDirection = luajava.bindClass("net.minecraftforge.common.ForgeDirection")
local TickType = luajava.bindClass("cpw.mods.fml.common.TickType")
local Block = luajava.bindClass("net.minecraft.block.Block")


--- constant values ---

local serverInstance = MinecraftServer:getServer()
local activeBlockID = Block.blockRedstone.blockID

--- wireless redstone routing ---

local routes = {}

-- returns the index of the route within the routes array or 0 if the route was not found
local function findRoute(route)

  for index, candidate in ipairs(routes) do

    if (candidate.world.provider.dimensionId == route.world.provider.dimensionId)
    and (candidate.src.x == route.src.x) and (candidate.src.y == route.src.y) and (candidate.src.z == route.src.z)
    and (candidate.dst.x == route.dst.x) and (candidate.dst.y == route.dst.y) and (candidate.dst.z == route.dst.z)
    then
      return index
    end

  end

  return 0
end

-- sets the given block to air if it is a redstone torch and state is false and to redstone torch if it is air and state is true
local function setBlockPower (world, x, y, z, state)

  local blockID = world:getBlockId(x, y, z)

  if state and (blockID == 0) then
    world:setBlock(x, y, z, activeBlockID, 0, 3)
  elseif not state and (blockID == activeBlockID) then
    world:setBlock(x, y, z, 0, 0, 3)
  end

end

-- returns true if the block is powered
local function getBlockPower (world, x, y, z)
  return world:getBlockPowerInput(x, y, z) > 0
end

-- update the given route (emit the redstone signal from src to dst)
local function tickRoute (route)

  local active = getBlockPower(route.world, route.src.x, route.src.y, route.src.z)
  setBlockPower(route.world, route.dst.x, route.dst.y, route.dst.z, active)

end

-- adds the given route if it doesn't aleady exists
local function addRoute (route)

  local index = findRoute(route)
  if index == 0 then

    table.insert(routes, route)

    return true
  end

  return false
end

-- remove first route that matches the given route
local function removeRoute (route)

  local index = findRoute(route)
  if index > 0 then

    table.remove(routes, index)

    return true
  end

  return false
end

-- get the world object for the given vanilla dimension id (-1,0,1)
local function worldByDimsionId (dimensionId)
  local world
  for i=1, serverInstance.worldServers.length do

    world = serverInstance.worldServers[i]
    if world.provider.dimensionId == dimensionId then
      return world
    end

  end

  return nil
end


--- ITickHandler emulation ---

function onTickHandler (isTickStart, tickType, tickArgs)

  if isTickStart and tickType:contains(TickType.SERVER) then

    --- update routes ---
    for index, route in ipairs(routes) do

      tickRoute(route)

    end
  end
end

forge.onTick(onTickHandler)


--- external API ---

-- redstone.route - adds a route with the given parameters, each route is added only once
--  dimensionId the dimension id (-1,0,1)
--  x1, y1, z1  the tracked block
--  x2, y2, z2  the block that receives the tracked redstone signal
function redstone.route (dimensionId, x1, y1, z1, x2, y2, z2)

  local world = worldByDimsionId(dimensionId)
  if world ~= nil then

    --- add route if not existing ---
    return addRoute({
      world = world;
      src = {
        x = x1;
        y = y1;
        z = z1
      };
      dst = {
        x = x2;
        y = y2;
        z = z2
      }
    })
  end

  return false
end

-- redstone.unroute - removes the route with the given parameters if it exists
--  dimensionId the dimension id (-1,0,1)
--  x1, y1, z1  the tracked block
--  x2, y2, z2  the block that receives the tracked redstone signal
function redstone.unroute (dimensionId, x1, y1, z1, x2, y2, z2)

  local world = worldByDimsionId(dimensionId)
  if world ~= nil then

    --- remove route if existing ---
    return removeRoute({
      world = world;
      src = {
        x = x1;
        y = y1;
        z = z1
      };
      dst = {
        x = x2;
        y = y2;
        z = z2
      }
    })
  end

  return false
end

print("LUA: redstone module loaded")
return redstone
