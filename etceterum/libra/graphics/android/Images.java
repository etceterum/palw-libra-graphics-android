package etceterum.libra.graphics.android;

import java.io.IOException;

import android.content.ContentResolver;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;


import etceterum.libra.graphics.RGBA;
import etceterum.libra.graphics.image.Image;

public final class Images {
    private Images() {
        // prevent instantiation
    }
    
    public static Image createImage(Bitmap bitmap) {
        return createImage(bitmap, false);
    }
    
    public static Image createImage(Bitmap bitmap, boolean flipY) {
        if (null == bitmap) {
            return null;
        }
        final int width = bitmap.getWidth(), height = bitmap.getHeight();
        try {
            int[] argb = new int[width*height];
            bitmap.getPixels(argb, 0, width, 0, 0, width, height);
            bitmap.recycle();
            final Image image = new Image(RGBA.argbToRGBA(argb, width, height, flipY), width, height);
            return image;
        }
        catch (OutOfMemoryError e) {
            // no-op
        }
        catch (Throwable e) {
            // no-op
        }
        finally {
            bitmap.recycle();
        }
        return null;
    }
    
    public static Image loadImage(Resources res, int resourceId) {
        return loadImage(res, resourceId, false);
    }
    
    public static Image loadImage(Resources res, int resourceId, boolean flipY) {
        return createImage(Bitmaps.loadBitmap(res, resourceId), flipY);
    }
    
    public static Image loadImage(AssetManager assets, String path) {
        return loadImage(assets, path, false);
    }
    
    public static Image loadImage(AssetManager assets, String path, boolean flipY) {
        return createImage(Bitmaps.loadBitmap(assets, path), flipY);
    }
    
    public static Image loadImage(ContentResolver resolver, Uri uri, int hintWidth, int hintHeight, boolean flipY) throws IOException {
        return createImage(Bitmaps.loadBitmap(resolver, uri, hintWidth, hintHeight), flipY);
    }
    
    public static Image loadImage(ContentResolver resolver, Uri uri, int hintWidth, int hintHeight) throws IOException {
        return loadImage(resolver, uri, hintWidth, hintHeight, false);
    }
    
}
