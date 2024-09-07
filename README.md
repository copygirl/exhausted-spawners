# Exhausted Spawners

.. is a [Forge] mod for Minecraft 1.20.1 primarily for limiting the number of
mobs created by a monster spawner, introducing other ways to recharge them,
such as by using spawn eggs which drop from slain mobs.

It's inspired by [Enhanced Mob Spawners], whose unsupported Forge branch it was
forked from, but by now all of its code has been gutted and entirely rewritten
pretty much from scratch.

[Forge]: https://github.com/MinecraftForge/MinecraftForge
[Enhanced Mob Spawners]: https://github.com/andersblomqvist/enhanced-mob-spawners

## Features

The mod is fully configurable. The following numbers are just defaults.

- After spawning a total of 64 mobs, a spawner turns empty and inactive.
- Spawn eggs can "refill" it, adding 4 to the number of mobs it will spawn.
- Harvesting a spawner with Silk Touch drops itself and spawn eggs.  
  The number of eggs depends on the remaining mobs in the spawner.
- Killing a mob with Silk Touch has a 15% chance of dropping its spawn egg.  
  However, doing so prevents you from getting any of its normal drops.

## Motivation

This mod is perfectly usable on its own. However it makes a lot more sense to
combine with [Forgery]'s `weapons_accept_silk` tweak to be able to harvest eggs
with weapons rather than just tools, and [KubeJS] for adding recipes and other
uses for spawn eggs.

Similar to Silk Touch for blocks, using it to kill mobs will reduce clutter in
your inventory while going exploring or caving, as any of the usual mob drops
are instead replaced by the occasional spawn egg drop that can later be thrown
into a spawner or machine to get any items you might want.

[Forgery]: https://github.com/FalsehoodMC/Fabrication
[KubeJS]: https://github.com/KubeJS-Mods/KubeJS
