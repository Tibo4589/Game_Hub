package be.kuleuven.gt.gamehub;
import android.graphics.Color;
public class TileColor2048 {
    public static int getColor(int value) {
        switch (value) {
            case 2: return Color.rgb(238, 228, 218);
            case 4: return Color.rgb(237, 224, 200);
            case 8: return Color.rgb(242, 177, 121);
            case 16: return Color.rgb(245, 149, 99);
            case 32: return Color.rgb(246, 124, 95);
            case 64: return Color.rgb(246, 94, 59);
            case 128: return Color.rgb(237, 207, 114);
            case 256: return Color.rgb(237, 204, 97);
            case 512: return Color.rgb(237, 200, 80);
            case 1024: return Color.rgb(237, 197, 63);
            case 2048: return Color.rgb(237, 194, 46);
            case 4096: return Color.rgb(255,102,255);
            case 8192: return Color.rgb(0,0,255);
            case 16384: return Color.rgb(0,255,0);
            default: return Color.WHITE;
        }
    }
}
