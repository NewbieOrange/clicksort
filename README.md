# ClickSort

ClickSort is a Bukkit plugin which makes it very easy to sort inventories (player/chest/dispenser) with a simple
mouse click.

## Installation

Copy ClickSort.jar into your plugins/ folder.  Restart/reload your server.  Done.

## Building

If you want to build ClickSort yourself, you will need Maven.

1a) Get a copy of dhutils: "git clone https://github.com/desht/dhutils.git"

1b) Build dhutils.  In the dhutils top-level directory, type: "mvn clean install"

2a) Download ClickSort: "git clone https://github.com/desht/clicksort.git"

2b) Build ClickSort. In the top-level directory, type: "mvn clean install"

This should give you a copy of ClickSort.jar under the target/ directory.

To open the project in Eclipse, do one of the following:

1) (Recommended) Install the m2e plugin, and import the project (File -> Import -> Existing Maven Project)
2) Alternatively use 'mvn eclipse:eclipse' to create the .project and .classpath files.

## Usage

Detailed documentation is available at bukkitdev: http://dev.bukkit.org/server-mods/clicksort/

## License

Clicksort by Des Herriott is licensed under the [Gnu GPL v3](http://www.gnu.org/licenses/gpl-3.0.html).
