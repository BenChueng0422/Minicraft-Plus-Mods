// 
// Decompiled by Procyon v0.6-prerelease
// 

package com.mojang.ld22.level.tile;

import com.mojang.ld22.entity.ItemEntity;
import com.mojang.ld22.item.ResourceItem;
import com.mojang.ld22.item.resource.Resource;
import com.mojang.ld22.item.ToolType;
import com.mojang.ld22.item.ToolItem;
import com.mojang.ld22.item.Item;
import com.mojang.ld22.entity.Player;
import com.mojang.ld22.entity.Mob;
import com.mojang.ld22.entity.Entity;
import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.level.Level;
import com.mojang.ld22.gfx.Screen;

public class SandTile extends Tile
{
    public SandTile(final int id) {
        super(id);
        this.connectsToSand = true;
    }
    
    @Override
    public void render(final Screen screen, final Level level, final int x, final int y) {
        final int col = Color.get(level.sandColor + 2, level.sandColor, level.sandColor - 110, level.sandColor - 110);
        final int transitionColor = Color.get(level.sandColor - 110, level.sandColor, level.sandColor - 110, level.dirtColor);
        final boolean u = !level.getTile(x, y - 1).connectsToSand;
        final boolean d = !level.getTile(x, y + 1).connectsToSand;
        final boolean l = !level.getTile(x - 1, y).connectsToSand;
        final boolean r = !level.getTile(x + 1, y).connectsToSand;
        final boolean steppedOn = level.getData(x, y) > 0;
        if (!u && !l) {
            if (!steppedOn) {
                screen.render(x * 16 + 0, y * 16 + 0, 0, col, 0);
            }
            else {
                screen.render(x * 16 + 0, y * 16 + 0, 35, col, 0);
            }
        }
        else {
            screen.render(x * 16 + 0, y * 16 + 0, (l ? 11 : 12) + (u ? 0 : 1) * 32, transitionColor, 0);
        }
        if (!u && !r) {
            screen.render(x * 16 + 8, y * 16 + 0, 1, col, 0);
        }
        else {
            screen.render(x * 16 + 8, y * 16 + 0, (r ? 13 : 12) + (u ? 0 : 1) * 32, transitionColor, 0);
        }
        if (!d && !l) {
            screen.render(x * 16 + 0, y * 16 + 8, 2, col, 0);
        }
        else {
            screen.render(x * 16 + 0, y * 16 + 8, (l ? 11 : 12) + (d ? 2 : 1) * 32, transitionColor, 0);
        }
        if (!d && !r) {
            if (!steppedOn) {
                screen.render(x * 16 + 8, y * 16 + 8, 3, col, 0);
            }
            else {
                screen.render(x * 16 + 8, y * 16 + 8, 35, col, 0);
            }
        }
        else {
            screen.render(x * 16 + 8, y * 16 + 8, (r ? 13 : 12) + (d ? 2 : 1) * 32, transitionColor, 0);
        }
    }
    
    @Override
    public void tick(final Level level, final int x, final int y) {
        final int d = level.getData(x, y);
        if (d > 0) {
            level.setData(x, y, d - 1);
        }
        if (this.random.nextInt(40) != 0) {
            return;
        }
        int xn = x;
        int yn = y;
        if (this.random.nextBoolean()) {
            xn += this.random.nextInt(2) * 2 - 1;
        }
        else {
            yn += this.random.nextInt(2) * 2 - 1;
        }
        if (level.getTile(xn, yn) == Tile.dirt) {
            level.setTile(xn, yn, this, 0);
        }
    }
    
    @Override
    public void steppedOn(final Level level, final int x, final int y, final Entity entity) {
        if (entity instanceof Mob) {
            level.setData(x, y, 10);
        }
    }
    
    @Override
    public boolean mayPass(final Level level, final int x, final int y, final Entity e) {
        return e.canWalk();
    }
    
    @Override
    public boolean interact(final Level level, final int xt, final int yt, final Player player, final Item item, final int attackDir) {
        if (item instanceof ToolItem) {
            final ToolItem tool = (ToolItem)item;
            if (tool.type == ToolType.shovel && player.payStamina(4 - tool.level)) {
                level.setTile(xt, yt, Tile.dirt, 0);
                level.add(new ItemEntity(new ResourceItem(Resource.sand), xt * 16 + this.random.nextInt(5) + 8, yt * 16 + this.random.nextInt(5) + 8));
                level.add(new ItemEntity(new ResourceItem(Resource.sand), xt * 16 + this.random.nextInt(5) + 8, yt * 16 + this.random.nextInt(5) + 8));
                level.add(new ItemEntity(new ResourceItem(Resource.sand), xt * 16 + this.random.nextInt(5) + 8, yt * 16 + this.random.nextInt(5) + 8));
                level.add(new ItemEntity(new ResourceItem(Resource.sand), xt * 16 + this.random.nextInt(5) + 8, yt * 16 + this.random.nextInt(5) + 8));
                level.add(new ItemEntity(new ResourceItem(Resource.sand), xt * 16 + this.random.nextInt(5) + 8, yt * 16 + this.random.nextInt(5) + 8));
                level.add(new ItemEntity(new ResourceItem(Resource.sand), xt * 16 + this.random.nextInt(5) + 8, yt * 16 + this.random.nextInt(5) + 8));
                return true;
            }
        }
        return false;
    }
}