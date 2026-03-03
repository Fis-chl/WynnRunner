# Wynnrunner
Extension mod to Wynntils focused around the collection of data from Lootrunning - a highly randomised endgame activity present in Wynncraft.

## Data storage:
All lootruns for every character you have on Wynncraft are stored under `/.minecraft/wynnrunner/lrdata/`. Each JSON file will begin with your character's ID as reported by Wynntils followed by a unique ID representing the lootrun itself.

## Contributing
This repo uses **Spotless** to ensure high quality and consistent formatting. We are using the same settings as Wynntils. Please run the `spotlessApply` gradle task before pushing to origin :)