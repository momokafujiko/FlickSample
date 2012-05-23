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
     * ���݈ʒu�̂P���̉摜��\������
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
     * ���݈ʒu�̈�O�̉摜��\������
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
			//�ǂݍ��ݗp�̃I�v�V�����I�u�W�F�N�g�𐶐�
			BitmapFactory.Options options = new BitmapFactory.Options();
			//���̒l��true�ɂ���Ǝ��ۂɂ͉摜��ǂݍ��܂��A
			//�摜�̃T�C�Y��񂾂����擾���邱�Ƃ��ł��܂��B
			options.inJustDecodeBounds = true;

			//�摜�t�@�C���ǂݍ���
			//�����ł͏�L�̃I�v�V������true�̂��ߎ��ۂ�
			//�摜�͓ǂݍ��܂�Ȃ��ł��B
			BitmapFactory.decodeFile(path, options);

			//�ǂݍ��񂾃T�C�Y��options.outWidth��options.outHeight��
			//�i�[�����̂ŁA���̒l����ǂݍ��ލۂ̏k�ڂ��v�Z���܂��B
			//���̃T���v���ł͂ǂ�ȑ傫���̉摜�ł�HVGA�Ɏ��܂�T�C�Y��
			//�v�Z���Ă��܂��B
			int scaleW = options.outWidth / 380 + 1;
			int scaleH = options.outHeight / 420 + 1;

			//�k�ڂ͐����l�ŁA2�Ȃ�摜�̏c���̃s�N�Z������1/2�ɂ����T�C�Y�B
			//3�Ȃ�1/3�ɂ����T�C�Y�œǂݍ��܂�܂��B
			int scale = Math.max(scaleW, scaleH);

			//���x�͉摜��ǂݍ��݂����̂�false���w��
			options.inJustDecodeBounds = false;

			//����v�Z�����k�ڒl���w��
			options.inSampleSize = scale;

			//����Ŏw�肵���k�ڂŉ摜��ǂݍ��߂܂��B
			//�������e�ʂ��������Ȃ�̂ň����₷���ł��B
			bmp = BitmapFactory.decodeFile(path, options);
		}
		return bmp;
	}
}