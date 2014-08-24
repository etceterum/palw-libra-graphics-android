package etceterum.libra.graphics.android;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import android.content.ContentResolver;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Pair;

public final class Bitmaps {
    public static final boolean NOISY = false;
    public static final int MAX_ATTEMPT_COUNT = 3;
    
    private Bitmaps() {
        // prevent instantiation
    }
    
    // from http://tkcodesharing.blogspot.com/2008/05/working-with-textures-in-androids.html
    public static ByteBuffer createRGBABuffer(Bitmap bitmap) 
    { 
        final int width = bitmap.getWidth(), height = bitmap.getHeight();
        final ByteBuffer bb = ByteBuffer.allocateDirect(bitmap.getHeight()*bitmap.getWidth()*(Integer.SIZE/8)); 
        bb.order(ByteOrder.BIG_ENDIAN); 
        final IntBuffer ib = bb.asIntBuffer(); 
        // Convert ARGB -> RGBA 
        for (int y = height - 1; y > -1; --y) { 
            for (int x = 0; x < width; ++x) { 
                int pix = bitmap.getPixel(x, height - y - 1); 
                int alpha = ((pix >> 24) & 0xFF); 
                int red = ((pix >> 16) & 0xFF); 
                int green = ((pix >> 8) & 0xFF); 
                int blue = ((pix) & 0xFF); 
                ib.put(red << 24 | green << 16 | blue << 8 | alpha); 
            } 
        } 
        bb.rewind(); 
        return bb; 
    }
    
    public static Pair<Integer, Integer> getBitmapDimensions(String path) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        final Bitmap bitmap = BitmapFactory.decodeFile(path, options);
        if (null != bitmap) {
            bitmap.recycle();
        }
        if (NOISY) {
            Log.i("com.etceterum.wallpaper.photoalbum.Bitmaps", "O=" + options.outWidth + "," + options.outHeight);
        }
        return new Pair<Integer, Integer>(options.outWidth, options.outHeight);
    }
    
    public static Pair<Integer, Integer> getBitmapDimensions(Resources res, int resourceId) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        final Bitmap bitmap = BitmapFactory.decodeResource(res, resourceId, options);
        if (null != bitmap) {
            bitmap.recycle();
        }
        if (NOISY) {
            Log.i("com.etceterum.wallpaper.photoalbum.Bitmaps", "O=" + options.outWidth + "," + options.outHeight);
        }
        return new Pair<Integer, Integer>(options.outWidth, options.outHeight);
    }
    
    public static Bitmap loadBitmap(String path) {
        return BitmapFactory.decodeFile(path);
    }
    
    public static Bitmap loadBitmap(String path, int hintWidth, int hintHeight) {
        // get native bitmap dimensions
        final Pair<Integer, Integer> dims = getBitmapDimensions(path);
        // figure out scale factor
        final int scaleX = (int)Math.ceil(dims.first/(double)hintWidth), scaleY = (int)Math.ceil(dims.second/(double)hintHeight);
        if (NOISY) {
            Log.i("com.etceterum.wallpaper.photoalbum.Bitmaps", "S=" + scaleX + "," + scaleY);
        }
        final int scale = (int)Math.ceil(Math.max(scaleX, scaleY));
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = scale;
        final Bitmap bitmap = BitmapFactory.decodeFile(path, options);
        if (NOISY) {
            Log.i("com.etceterum.wallpaper.photoalbum.Bitmaps", "B=" + bitmap);
        }
        return bitmap;
    }
    
    public static Bitmap loadBitmap(Resources res, int resourceId) {
        return BitmapFactory.decodeResource(res, resourceId);
    }
    
    public static Bitmap loadBitmap(AssetManager assets, String path) {
        try {
            return BitmapFactory.decodeStream(assets.open(path));
        }
        catch (IOException e) {
            return null;
        }
    }
    
    public static Bitmap loadBitmap(Resources res, int resourceId, int hintWidth, int hintHeight) {
        // get native bitmap dimensions
        final Pair<Integer, Integer> dims = getBitmapDimensions(res, resourceId);
        // figure out scale factor
        final int scaleX = (int)Math.ceil(dims.first/(double)hintWidth), scaleY = (int)Math.ceil(dims.second/(double)hintHeight);
        if (NOISY) {
            Log.i("com.etceterum.wallpaper.photoalbum.Bitmaps", "S=" + scaleX + "," + scaleY);
        }
        final int scale = (int)Math.max(scaleX, scaleY);
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = scale;
        final Bitmap bitmap = BitmapFactory.decodeResource(res, resourceId, options);
        if (NOISY) {
            Log.i("com.etceterum.wallpaper.photoalbum.Bitmaps", "B=" + bitmap);
        }
        return bitmap;
    }
    
    // http://stackoverflow.com/questions/3647993/android-bitmaps-loaded-from-gallery-are-rotated-in-imageview
    // returns orientation in degrees, or null on failure
    public static Integer getBitmapOrientation(ContentResolver resolver, Uri uri) {
        // it's on the external media.
        final Cursor cursor = resolver.query(uri, new String[] { MediaStore.Images.ImageColumns.ORIENTATION }, null, null, null);
        if (null == cursor) {
            return null;
        }

        if (cursor.getCount() != 1) {
            return null;
        }

        cursor.moveToFirst();
        final int orientation = cursor.getInt(0);
        cursor.close();
        return orientation;
    }

    public static Pair<Integer, Integer> getBitmapDimensions(ContentResolver resolver, Uri uri) throws IOException {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        final InputStream stream = resolver.openInputStream(uri);
        final Bitmap bitmap = BitmapFactory.decodeStream(stream, null, options);
        if (null != bitmap) {
            bitmap.recycle();
        }
        stream.close();
        return new Pair<Integer, Integer>(options.outWidth, options.outHeight);
    }
    
    public static Bitmap loadBitmap(ContentResolver resolver, Uri uri, int maxWidth, int maxHeight) throws IOException {
        // get orientation
        final Integer orientation = getBitmapOrientation(resolver, uri);
        // get native bitmap dimensions
        final Pair<Integer, Integer> dims = getBitmapDimensions(resolver, uri);
        final int scaleX = (int)Math.ceil(dims.first/(float)maxWidth), scaleY = (int)Math.ceil(dims.second/(float)maxHeight);
        int scale = Math.max(1, Math.max(scaleX, scaleY));
        //int scale = Bits.getSmallestPowerOfTwoGreaterThanOrEqualTo(Math.max(1, Math.max(scaleX, scaleY)));
        
        Bitmap decodedBitmap = null;
        for (int attempt = 0; attempt < MAX_ATTEMPT_COUNT; ++attempt, ++scale) {
            //Log.i(Bitmaps.class.getName(), "FIXME: loading bitmap: [" + dims.first + "x" + dims.second + "] -> ~[" + maxWidth + "x" + maxHeight + "], using scale " + scale);
            try {
                decodedBitmap = loadBitmap(resolver, uri, scale);
            }
            catch (Throwable e) {
                assert null == decodedBitmap;
                // no-op
            }
            if (null != decodedBitmap) {
                break;
            }
        }
        
        if (null == decodedBitmap) {
            return null;
        }
        
        //Log.i(Bitmaps.class.getName(), "FIXME --> decoded bitmap is [" + decodedBitmap.getWidth() + "x" + decodedBitmap.getHeight() + "]");
        
        //final BitmapFactory.Options options = new BitmapFactory.Options();
        //options.inSampleSize = scale;
        //final InputStream stream = resolver.openInputStream(uri);
        //final Bitmap decodedBitmap = BitmapFactory.decodeStream(stream, null, options);
        //stream.close();
        
        Bitmap finalBitmap = decodedBitmap;
        if (null != orientation && orientation%360 != 0) {
            final Matrix matrix = new Matrix();
            matrix.postRotate(orientation);

            try {
                finalBitmap = Bitmap.createBitmap(decodedBitmap, 0, 0, decodedBitmap.getWidth(), decodedBitmap.getHeight(), matrix, true);
            }
            catch (Throwable e) {
                finalBitmap = decodedBitmap;
            }
            if (finalBitmap != decodedBitmap) {
                decodedBitmap.recycle();
            }
        }
        //Log.i(Bitmaps.class.getName(), "FIXME --> final bitmap is [" + finalBitmap.getWidth() + "x" + finalBitmap.getHeight() + "]");
        return finalBitmap;
    }
    
    private static Bitmap loadBitmap(ContentResolver resolver, Uri uri, int scale) throws IOException {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = scale;
        final InputStream stream = resolver.openInputStream(uri);
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(stream, null, options);
        }
        finally {
            stream.close();
        }
        return bitmap;
    }
    
}
