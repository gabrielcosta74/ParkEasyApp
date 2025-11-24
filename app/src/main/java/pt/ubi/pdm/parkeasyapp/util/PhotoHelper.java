package pt.ubi.pdm.parkeasyapp.util;

import android.content.Context;
import android.net.Uri;
import androidx.core.content.FileProvider;
import java.io.File;

public class PhotoHelper {
    public static File createTempImageFile(Context ctx){
        File dir = new File(ctx.getCacheDir(), "images");
        dir.mkdirs();
        return new File(dir, "parking_"+System.currentTimeMillis()+".jpg");
    }
    public static Uri getUriForFile(Context ctx, File f){
        return FileProvider.getUriForFile(ctx, "pt.ubi.pdm.parkeasyapp.fileprovider", f);
    }
}