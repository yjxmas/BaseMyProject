package cheerly.mybaseproject.utils;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import cheerly.mybaseproject.base.BaseActivity;
import cheerly.mybaseproject.listener.MyCallback;

/**
 * Created by chenglin on 2017-5-24.
 */

public class BitmapUtils {
    /**
     * 返回图片宽高数组，第0个是宽，第1个是高
     */
    public static int[] getBitmapWidthHeight(final String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();

        /**
         * 最关键在此，把options.inJustDecodeBounds = true;
         * 这里再decodeFile()，返回的bitmap为空，但此时调用options.outHeight时，已经包含了图片的高了
         */
        options.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(path, options); // 此时返回的bitmap为null
        int[] size = new int[2];
        size[0] = options.outWidth;
        size[1] = options.outHeight;
        return size;
    }

    /**
     * 把View 生成bitmap
     */
    public static Bitmap createBitmapByView(View view) {
        //打开图像缓存
        view.setDrawingCacheEnabled(true);
        //必须调用measure和layout方法才能成功保存可视组件的截图到png图像文件
        //测量View大小
        if (view.getHeight() <= 0) {
            view.measure(View.MeasureSpec.makeMeasureSpec(view.getWidth(), View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        } else if (view.getHeight() > 0) {
            view.measure(View.MeasureSpec.makeMeasureSpec(view.getWidth(), View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(view.getHeight(), View.MeasureSpec.EXACTLY));
        }
        //发送位置和尺寸到View及其所有的子View
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        Bitmap bitmap = null;
        try {
            //获得可视组件的截图
            bitmap = view.getDrawingCache();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * 从view 得到图片
     */
    public static Bitmap getBitmapFromView(View view) {
        view.destroyDrawingCache();
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.setDrawingCacheEnabled(true);
        return view.getDrawingCache(true);
    }

    public static String getPathByUri(Context context, Uri uri) {
        Activity activity = null;
        if (context instanceof Activity) {
            activity = (Activity) context;
        }

        if (activity == null) {
            return null;
        }

        String path = null;
        if (uri == null) {
            return path;
        }
        if ("content".equals(uri.getScheme())) {
            String[] projection = {MediaStore.Images.Media.DATA};
            Cursor cursor = activity.managedQuery(uri, projection, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            path = cursor.getString(column_index);
        } else if ("file".equals(uri.getScheme())) {
            path = uri.getPath();
        }
        return path;
    }

    /**
     * 此方法仅仅用于计算createScaledBitmap() 方法获取Bitmap 时的缩放比例。
     * 目的是为了防止OOM
     */
    private static int getOptionSize(final float size) {
        int optionSize = 1;
        if (size >= 2f && size < 4f) {
            optionSize = 2;
        } else if (size >= 4f && size < 8f) {
            optionSize = 4;
        } else if (size >= 8f && size < 16) {
            optionSize = 8;
        } else if (size >= 16f) {
            optionSize = 16;
        }
        return optionSize;
    }

    /**
     * 保存图片到sdcard
     */
    public static void saveBitmap(final Bitmap bmp, final MyCallback callback) {
        if (callback != null) {
            callback.onPrepare();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                String fileName = System.currentTimeMillis() + ".jpg";
                File file = new File(SDCardUtils.getPath(), fileName);
                try {
                    FileOutputStream fos = new FileOutputStream(file);
                    bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    fos.flush();
                    fos.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();

                    if (callback != null) {
                        callback.onError();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    if (callback != null) {
                        callback.onError();
                    }
                }
                String path = file.getAbsolutePath();

                if (callback != null) {
                    callback.onSucceed(path);
                }

            }
        }).start();

    }


    /**
     * 按原图比例缩放裁剪图片
     *
     * @param activity   BaseActivity
     * @param imageUri   图片的Uri
     * @param imageWidth 想要被缩放到的target图片宽度，我会根据此宽度和原图的比例，去计算target图片高度
     * @param callback   回调监听
     */
    public static void createScaledBitmap(final BaseActivity activity, final Uri imageUri, int imageWidth, final MyCallback callback) {
        final String imagePath = getPathByUri(activity, imageUri);
        createScaledBitmap(activity, imagePath, imageWidth, callback);
    }

    /**
     * 按原图比例缩放裁剪图片
     *
     * @param activity   BaseActivity
     * @param imagePath  图片的路径
     * @param imageWidth 想要被缩放到的target图片宽度，我会根据此宽度和原图的比例，去计算target图片高度
     * @param callback   回调监听
     */
    public static void createScaledBitmap(final BaseActivity activity, final String imagePath, final int imageWidth, final MyCallback callback) {
        if (callback == null) {
            throw new NullPointerException("MyCallback must not null");
        }
        callback.onPrepare();

        if (TextUtils.isEmpty(imagePath) || !(new File(imagePath).exists())) {
            callback.onError();
            return;
        }

        //原图宽度大于传入的宽度才压缩，否则不压缩，直接返回原图路径
        final int[] size = getBitmapWidthHeight(imagePath);
        if (size[0] <= imageWidth) {
            callback.onSucceed(imagePath);
            return;
        }

        new Thread() {
            @Override
            public void run() {
                super.run();

                int imageHeight = (int) (size[1] * 1f * imageWidth * 1f / size[0] * 1f);
                float scale = size[0] * 1f / imageWidth * 1f;

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = getOptionSize(scale);
                Bitmap targetBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeFile(imagePath, options), imageWidth, imageHeight, true);

                saveBitmap(targetBitmap, new MyCallback() {
                    @Override
                    public void onPrepare() {

                    }

                    @Override
                    public void onSucceed(final Object path) {
                        if (activity != null && !activity.isFinishing()) {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    callback.onSucceed(path);
                                }
                            });
                        }
                    }

                    @Override
                    public void onError() {
                        if (activity != null && !activity.isFinishing()) {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    callback.onError();
                                }
                            });
                        }
                    }
                });
            }
        }.start();
    }

    /**
     * 返回bitmap 所占内存的大小
     */
    public static int getBitmapBytes(Bitmap bitmap) {
        return bitmap.getByteCount();
    }
}
