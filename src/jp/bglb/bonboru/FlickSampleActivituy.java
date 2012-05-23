package jp.bglb.bonboru;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ViewSwitcher.ViewFactory;

public class FlickSampleActivituy extends Activity implements ViewFactory {
	public static final String TAG = "touch_event";
	public static final int THRESHOLD = 200;
	private String[] fileList;
	private int position = 0;
	private GestureDetector myGestureDetector;
	private ImageSwitcher switcher;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        fileList = getFileList();
        myGestureDetector = new GestureDetector(this, new OnGestureListener() {
            private boolean event = true;
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                 Log.d(TAG, "onSingletapUp");
                 event = true;
                 return false;
            }
           
            @Override
            public void onShowPress(MotionEvent e) {
                 Log.d(TAG, "onShowPress");
            }
           
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                      float distanceY) {
                 Log.d(TAG, "onScroll");
                 event = true;
                 return false;
            }
           
            @Override
            public void onLongPress(MotionEvent e) {
                 // do nothing
                 Log.d(TAG, "onLongPress");
            }
           
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                      float velocityY) {
                 Log.d(TAG, "onFling");
                 if (Math.abs(e2.getX() - e1.getX()) > THRESHOLD) {
                      if (velocityX < 0) {
                           showPrevious();
                      } else {
                           showNext();
                      }
                      event = true;
                      return true;
                 }
                 return false;
            }
           
            @Override
            public boolean onDown(MotionEvent e) {
                 Log.d(TAG, "onDown");
                 if (event) {
                      event = false;
                      return true;
                 } else {
                      return false;
                 }
            }
       });
        
        switcher = (ImageSwitcher) findViewById(R.id.switcher);
        switcher.setFactory(this);
        switcher.setOnTouchListener(new OnTouchListener() {
        	@Override
			public boolean onTouch(View v, MotionEvent event) {
				return myGestureDetector.onTouchEvent(event);
			}
		});
    }

    /**
     * 現在位置の１つ次の画像を表示する
     */
     private void showPrevious() {
          position -= 1;
          if (position < 0) {
               position = fileList.length - 1;
          }
          switcher.setInAnimation(this, R.anim.right_in);
          switcher.setOutAnimation(this, R.anim.left_out);
         
          switcher.setImageDrawable(new BitmapDrawable(loadImage(fileList[position])));
     }
    
     public void onShowPrevious(View view) {
          showPrevious();
     }
    
     /**
     * 現在位置の一つ前の画像を表示する
     */
     private void showNext() {
          position += 1;
          if (position >= fileList.length) {
               position = 0;
          }
          switcher.setInAnimation(this, R.anim.left_in);
          switcher.setOutAnimation(this, R.anim.right_out);
          switcher.setImageDrawable(new BitmapDrawable(loadImage(fileList[position])));
     }
    
    
	@Override
	public View makeView() {
        ImageView image = new ImageView(this);
        image.setScaleType(ImageView.ScaleType.FIT_XY);
        image.setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        image.setImageBitmap(loadImage(fileList[position]));
        return image;
	}
	
	private String[] getFileList() {
		Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;  
		Cursor c = this.managedQuery(uri, null, null, null, null);  
		String[] fileList = new String[c.getCount()];
		int k = 0;
		while(c.moveToNext()) {
			fileList[k] = c.getString(c.getColumnIndexOrThrow("_id"));
			k++;
		}
		return fileList;
	}
	
	private Bitmap loadImage(String fileName) {
		String[] projection = {MediaStore.Images.Media.DATA};
		String[] selectionArgs = {fileName};
		Cursor c = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
											  projection, "_id = ?", selectionArgs, null);
		Bitmap bmp = null;
		if (c.moveToFirst()) {
			String path = c.getString(0);
			//読み込み用のオプションオブジェクトを生成
			BitmapFactory.Options options = new BitmapFactory.Options();
			//この値をtrueにすると実際には画像を読み込まず、
			//画像のサイズ情報だけを取得することができます。
			options.inJustDecodeBounds = true;

			//画像ファイル読み込み
			//ここでは上記のオプションがtrueのため実際の
			//画像は読み込まれないです。
			BitmapFactory.decodeFile(path, options);

			//読み込んだサイズはoptions.outWidthとoptions.outHeightに
			//格納されるので、その値から読み込む際の縮尺を計算します。
			//このサンプルではどんな大きさの画像でもHVGAに収まるサイズを
			//計算しています。
			int scaleW = options.outWidth / 380 + 1;
			int scaleH = options.outHeight / 420 + 1;

			//縮尺は整数値で、2なら画像の縦横のピクセル数を1/2にしたサイズ。
			//3なら1/3にしたサイズで読み込まれます。
			int scale = Math.max(scaleW, scaleH);

			//今度は画像を読み込みたいのでfalseを指定
			options.inJustDecodeBounds = false;

			//先程計算した縮尺値を指定
			options.inSampleSize = scale;

			//これで指定した縮尺で画像を読み込めます。
			//もちろん容量も小さくなるので扱いやすいです。
			bmp = BitmapFactory.decodeFile(path, options);
		}
		return bmp;
	}
}