# Exhausted Monster Spawners

.. is a [Forge] mod for Minecraft 1.20.1 primarily for limiting the number of
mobs created by a monster spawner, introducing other ways to recharge them,
such as by using spawn eggs which drop from slain mobs.

It's inspired by [Enhanced Mob Spawners], whose unsupported Forge branch it was
forked from, but by now all of its code has been gutted and entirely rewritten
pretty much from scratch.

[Forge]: https://github.com/MinecraftForge/MinecraftForge
[Enhanced Mob Spawners]: https://github.com/andersblomqvist/enhanced-mob-spawners

## Features

- All features are fully configurable. The following numbers are just defaults.
- After spawning a total of 64 mobs, a spawner turns empty and inactive.
- Spawn eggs can "refill" it, adding 16 to the number of mobs it will spawn.
- Harvesting a spawner with Silk Touch yields you an empty one.
- Breaking one gives you eggs and experience depending on the remaining mobs.
- Mobs have a 1 in 500 chance of dropping a spawn egg on kill, more with Looting.
- Using Silk Touch increases this to 1 in 20, but clears all other mob drops.
