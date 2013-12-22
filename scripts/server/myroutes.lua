
local redstone = require "redstone"

-- create route in dimension 0 (overworld) from (0,4,32) to (5,4,32)
-- (1) put a block on position (0,4,32) and attach a lever
-- (2) ensure that the block at (5,4,32) is air and the block below (5,3,32) is solid
-- (3) run this script "/script myroutes.lua"
-- (4) toggle the lever on and off, you should see a redstone torch at (5,4,32) when lever is on
redstone.route( 0, 0,4,32, 5,4,32 )
