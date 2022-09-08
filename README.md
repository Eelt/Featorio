# Featorio — Minecraft Features your way
Featorio's goal is to allow users to add and remove features from Minecraft world generation without the use of datapacks.

In an increasingly data-driven world, JSONs baked into jars at publish time are not as customizable as the tomls in your config folder. At the same time—for modders—these JSONs are a boon as you no longer need to hard-code all the "templating" around your features. You can have a dynamic amount of features, you can have 50 variants of the same feature and not need to program the config for all 50 of them, or come up with some advanced custom config system for your mod just to allow this.

Featorio does this for you. In your config folder, you'll have a Featorio folder after running Minecraft with Featorio installed. In your Featorio folder you'll have folders for the actions you want to take. You can add "additions" (features you'll add to Minecraft world/level generation) in your additions folder. Removals will be in the removals folder, etc.