package widget;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;

import java.io.File;
import java.io.IOException;

import httpwork.HttpDownloadCallback;
import httpwork.HttpUtils;
import okhttp3.Call;
import utils.BitmapUtils;

/**
 * Created by chenglin on 2018-1-20.
 */

public class LongImageView extends WebView {

    public LongImageView(Context context) {
        super(context);
        init();
    }

    public LongImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        WebSettings webSettings = getSettings();
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);//去掉系统难看的缩放按钮
    }

    /**
     * 加载长图，是利用WebView实现的。
     * 一定要设置此控件的宽度，如果不设置，默认就是屏幕宽度
     *
     * @param file      本地图片文件
     * @param showWidth 当前控件显示的宽度
     */
    public void load(final File file, final int showWidth) {
        if (file == null || !file.exists()) {
            return;
        }

        int pictureWidth = BitmapUtils.getBitmapWidthHeight(file.getAbsolutePath())[0];
        float scale = (showWidth * 1f / pictureWidth * 1f) * 100f;
        setInitialScale((int) scale);

        loadUrl("file:" + file.getAbsolutePath());
    }

    /**
     * 加载长图，是利用WebView实现的。
     * 一定要设置此控件的宽度，如果不设置，默认就是屏幕宽度
     *
     * @param url       网络图片URL
     * @param showWidth 当前控件显示的宽度
     */
    public void load(final String url, final int showWidth) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        if (!url.trim().toLowerCase().startsWith("http")) {
            return;
        }

        HttpUtils.downloadFile(url, true, new HttpDownloadCallback() {
            @Override
            public void onSuccess(String filePath) {
                if (isFinish()) {
                    return;
                }
                load(new File(filePath), showWidth);
            }

            @Override
            public void onProgress(Call call, long fileTotalSize, long fileDowningSize, int percent) {

            }

            @Override
            public void onFailure(IOException e) {

            }
        });

    }

    private boolean isFinish() {
        if (getContext() instanceof Activity) {
            Activity activity = (Activity) getContext();
            return activity.isFinishing();
        }
        return true;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        try {
            loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
            clearHistory();
            ((ViewGroup) getParent()).removeView(this);
            destroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}