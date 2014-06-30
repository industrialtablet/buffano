package org.mortbay.ijetty.html5webview;

import java.util.Timer;
import java.util.TimerTask;

import org.mortbay.ijetty.AppConstants;
import org.mortbay.ijetty.R;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

public class HTML5WebView extends WebView {
        private final static String TAG = "->HTML5WebView";
	private Context mContext;
	private MyWebChromeClient mWebChromeClient;
	private View mCustomView;
	private FrameLayout mCustomViewContainer;
	private WebChromeClient.CustomViewCallback 	mCustomViewCallback;
	
	private FrameLayout							mContentView;
	private FrameLayout							mBrowserFrameLayout;
	private FrameLayout							mLayout;
	
    static final String LOGTAG = "HTML5WebView";
	    
	private void init(Context context) {
		mContext = context;		
		Activity a = (Activity) mContext;
		
		mLayout = new FrameLayout(context);
		
		mBrowserFrameLayout = (FrameLayout) LayoutInflater.from(a).inflate(R.layout.custom_screen, null);
		mContentView = (FrameLayout) mBrowserFrameLayout.findViewById(R.id.main_content);
		mCustomViewContainer = (FrameLayout) mBrowserFrameLayout.findViewById(R.id.fullscreen_custom_content);
		
		mLayout.addView(mBrowserFrameLayout, COVER_SCREEN_PARAMS);

		mWebChromeClient = new MyWebChromeClient();
	    setWebChromeClient(mWebChromeClient);
	    
	    setWebViewClient(new MyWebViewClient());
	       
	    // Configure the webview
	    WebSettings s = getSettings();
	    s.setBuiltInZoomControls(true);
	    //s.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);//导致显示混乱
	    s.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
	    //s.setUseWideViewPort(true);
	    //s.setLoadWithOverviewMode(true);
	    s.setUseWideViewPort(true);
	    s.setSavePassword(true);
	    s.setSaveFormData(true);
	    s.setJavaScriptEnabled(true);
	    s.setPluginState(PluginState.ON);
	    s.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
	    
	    
	    setInitialScale(1);
	    
	    // enable navigator.geolocation 
	    s.setGeolocationEnabled(true);
	    s.setGeolocationDatabasePath("/data/data/org.mortay.ijetty/databases/");
	    
	    // enable Web Storage: localStorage, sessionStorage
	    s.setDomStorageEnabled(true);
	    mContentView.addView(this);
	}

	public HTML5WebView(Context context) {
		super(context);
		init(context);
	}

	public HTML5WebView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public HTML5WebView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}
	
	public FrameLayout getLayout() {
		return mLayout;
	}
	
    public boolean inCustomView() {
		return (mCustomView != null);
	}
    
    public void hideCustomView() {
		mWebChromeClient.onHideCustomView();
	}

    private class MyWebChromeClient extends WebChromeClient {
		private Bitmap 		mDefaultVideoPoster;
		private View 		mVideoProgressView;


    	@Override
		public void onShowCustomView(View view, WebChromeClient.CustomViewCallback callback)
		{
    		super.onShowCustomView(view, callback);
//    		Log.i(LOGTAG, "here in on ShowCustomView");
//            if (view instanceof FrameLayout) {
//            	Log.i(LOGTAG, "here in on ShowCustomView 1");
//                FrameLayout frame = (FrameLayout) view;
//                if (frame.getFocusedChild() instanceof VideoView) {
//                	Log.i(LOGTAG, "here in on ShowCustomView 2");
//                    VideoView video = (VideoView) frame.getFocusedChild();
//                 //   frame.removeView(video);
//                    //video.start();
//                }
//                else {
//                	Log.i(LOGTAG, "here in on ShowCustomView 3");
//                	//VideoView video = (VideoView) frame.getFocusedChild();
//                	//frame.removeView(video);
//                	//video.start();
//				}
//            }
//			Log.i(LOGTAG, "here in on ShowCustomView");
			
//	        HTML5WebView.this.setVisibility(View.GONE); //����(GONE)
//	        
//	        // if a view already exists then immediately terminate the new one
//	        if (mCustomView != null) {
//	            callback.onCustomViewHidden();
//	            return;
//	        }
//	        
//	        mCustomViewContainer.addView(view);
//	        mCustomView = view;
//	        mCustomViewCallback = callback;
//	        mCustomViewContainer.setVisibility(View.VISIBLE);
		}
		
		@Override
		public void onHideCustomView() {
			//super.onHideCustomView();
//			if (mCustomView == null)
//				return;	       
//			
//			// Hide the custom view.
//			mCustomView.setVisibility(View.GONE);
//			
//			// Remove the custom view from its container.
//			mCustomViewContainer.removeView(mCustomView);
//			mCustomView = null;
//			mCustomViewContainer.setVisibility(View.GONE);
//			mCustomViewCallback.onCustomViewHidden();
//			
//			HTML5WebView.this.setVisibility(View.VISIBLE);
			
	        Log.i(LOGTAG, "set it to webView");
		}
		
		@Override
		public Bitmap getDefaultVideoPoster() {
			Log.i(LOGTAG, "here in on getDefaultVideoPoster");	
			if (mDefaultVideoPoster == null) {
				mDefaultVideoPoster = BitmapFactory.decodeResource(
						getResources(), R.drawable.default_video_poster);
		    }
			return mDefaultVideoPoster;
		}
		
		@Override
		public View getVideoLoadingProgressView() {
			//Log.i(LOGTAG, "here in on getVideoLoadingPregressView");
	        if (mVideoProgressView == null) {
	            LayoutInflater inflater = LayoutInflater.from(mContext);
	            mVideoProgressView = inflater.inflate(R.layout.video_loading_progress, null);
	        }
	        return mVideoProgressView; 
		}
    	
    	 @Override
         public void onReceivedTitle(WebView view, String title) {
            ((Activity) mContext).setTitle(title);
         }

         @Override
         public void onProgressChanged(WebView view, int newProgress) {
        	 ((Activity) mContext).getWindow().setFeatureInt(Window.FEATURE_PROGRESS, newProgress*100);
         }
         
         @Override
         public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
             callback.invoke(origin, true, false);
         }
         
         private long timeout = 60000;  
         //private WebView mWebView;  
         private Timer mTimer;  
         Handler mHandler = new Handler(){  
            @Override  
            public void handleMessage(Message msg) {  
                switch(msg.what){         
                       case AppConstants.MSG_PAGE_TIMEOUT :  
                        //这里对已经显示出页面且加载超时的情况不做处理     
//                          if(mWebView != null && mWebView.getProgress() < 100 &&  mWebView.getContentHeight() )       
//                              load404Page() ;  
                              break ;    
                          }  
                }  
          };  
            
         public class MyWebViewClient extends WebViewClient {  
             @Override  
             public void onPageStarted(WebView view, String url, Bitmap favicon) {  
                  // set url loading time out thread  
                  mTimer = new Timer();  
                  TimerTask tt = new TimerTask() {  
                      @Override  
                      public void run() {  
                          Message m = new Message();  
                          m.what = AppConstants.MSG_PAGE_TIMEOUT ;  
                          mHandler.sendMessage(m);  
                 
                          mTimer.cancel();  
                          mTimer.purge();  
                     }  
                 };  
                 mTimer.schedule(tt, timeout);  
             }  
            
             @Override  
             public void onPageFinished(WebView view, String url) {  
                  mTimer.cancel() ;  
                  mTimer.purge() ;  
             }
         }
    }
	
	private class MyWebViewClient extends WebViewClient {
	    @Override
	    public boolean shouldOverrideUrlLoading(WebView view, String url) {
	    	Log.i(LOGTAG, "shouldOverrideUrlLoading: "+url);
	    	// don't override URL so that stuff within iframe can work properly
	        // view.loadUrl(url);
	        return false;
	    }
	}
	
	static final FrameLayout.LayoutParams COVER_SCREEN_PARAMS =
			//new FrameLayout.LayoutParams(768, 512);
			new FrameLayout.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
}