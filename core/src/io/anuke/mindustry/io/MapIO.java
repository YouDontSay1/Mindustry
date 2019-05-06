package io.anuke.mindustry.io;

import io.anuke.arc.files.FileHandle;
import io.anuke.arc.graphics.Color;
import io.anuke.arc.graphics.Pixmap;
import io.anuke.arc.graphics.Pixmap.Format;
import io.anuke.arc.util.Time;
import io.anuke.mindustry.content.Blocks;
import io.anuke.mindustry.game.Team;
import io.anuke.mindustry.maps.Map;
import io.anuke.mindustry.world.*;
import io.anuke.mindustry.world.blocks.Floor;

import java.io.IOException;
import java.io.InputStream;

/** Reads and writes map files. */
public class MapIO{
    private static final int[] pngHeader = {0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};

    public static boolean isImage(FileHandle file){
        try(InputStream stream = file.read(32)){
            for(int i1 : pngHeader){
                if(stream.read() != i1){
                    return false;
                }
            }
            return true;
        }catch(IOException e){
            return false;
        }
    }

    public static Pixmap generatePreview(Map map) throws IOException{
        Time.mark();
        Pixmap floors = new Pixmap(map.width, map.height, Format.RGBA8888);
        Pixmap walls = new Pixmap(map.width, map.height, Format.RGBA8888);
        int black = Color.rgba8888(Color.BLACK);
        int shade = Color.rgba8888(0f, 0f, 0f, 0.5f);
        CachedTile tile = new CachedTile(){
            @Override
            public void setFloor(Floor type){
                floors.drawPixel(x, floors.getHeight() - 1 - y, colorFor(type, Blocks.air, Blocks.air, getTeam()));
            }

            @Override
            public void setOverlay(Block type){
                if(type != Blocks.air)
                    floors.drawPixel(x, floors.getHeight() - 1 - y, colorFor(floor(), Blocks.air, type, getTeam()));
            }

            @Override
            protected void changed(){
                super.changed();
                int c = colorFor(Blocks.air, block(), Blocks.air, getTeam());
                if(c != black){
                    walls.drawPixel(x, floors.getHeight() - 1 - y, c);
                    floors.drawPixel(x, floors.getHeight() - 1 - y + 1, shade);
                }
            }
        };

        floors.drawPixmap(walls, 0, 0);
        walls.dispose();
        //TODO actually generate the preview
        return floors;
    }

    public static Pixmap generatePreview(Tile[][] tiles){
        Pixmap pixmap = new Pixmap(tiles.length, tiles[0].length, Format.RGBA8888);
        for(int x = 0; x < pixmap.getWidth(); x++){
            for(int y = 0; y < pixmap.getHeight(); y++){
                Tile tile = tiles[x][y];
                pixmap.drawPixel(x, pixmap.getHeight() - 1 - y, colorFor(tile.floor(), tile.block(), tile.overlay(), tile.getTeam()));
            }
        }
        return pixmap;
    }

    public static int colorFor(Block floor, Block wall, Block ore, Team team){
        if(wall.synthetic()){
            return team.intColor;
        }
        return Color.rgba8888(wall.solid ? wall.color : ore == Blocks.air ? floor.color : ore.color);
    }

    interface TileProvider{
        Tile get(int x, int y);
    }
}