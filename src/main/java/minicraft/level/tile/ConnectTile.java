package minicraft.level.tile;


import minicraft.gfx.Sprite;
import minicraftmodsapiinterface.*;

// IMPORTANT: This tile should never be used for anything, it only exists to allow tiles right next to the edge of the world to connect to it
public class ConnectTile extends Tile {
    private static Sprite sprite = new Sprite(30, 30, 3);

    public ConnectTile() {
        super("connector tile", sprite);
    }

    @Override
    public boolean mayPass(ILevel level, int x, int y, IEntity e) {
        return false;
    }

    @Override
    public boolean maySpawn() {
        return false;
    }
}
