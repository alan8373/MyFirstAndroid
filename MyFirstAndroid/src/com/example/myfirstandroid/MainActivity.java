package com.example.myfirstandroid;

import java.io.File;
import java.io.IOException;

import android.media.AudioManager;
import android.media.CamcorderProfile;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.VideoView;
//import com.example.myfirstandroid.CameraPreview;

public class MainActivity extends Activity implements SurfaceHolder.Callback {

	public final static String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
	static final int REQUEST_IMAGE_CAPTURE = 1;
	static final int REQUEST_TAKE_PHOTO = 1;
	
	String mCurrentPhotoPath;
	ImageView mImageView;
	SurfaceView mSurfaceView;
	SurfaceHolder mHolder;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //mImageView = (ImageView) findViewById(R.id.imageView);

        mSurfaceView = (SurfaceView) findViewById(R.id.surface_view);
        mHolder = mSurfaceView.getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        
        //dispatchSettingsIntent();
    }
 
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /* Called when send button clicked */
    public void sendMessage(View view) {
        // Do something in response to button
    	Intent intent = new Intent(this, DisplayMessageActivity.class);
    	EditText editText = (EditText) findViewById(R.id.edit_message);
    	String message = editText.getText().toString();
    	intent.putExtra(EXTRA_MESSAGE, message);
    	startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_search:
            	System.out.print("search");
                dispatchTakePictureIntent();
                return true;
        case R.id.action_settings:
				dispatchSettingsIntent();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void dispatchSettingsIntent() {
        // setup the video view
    	VideoView videoView = (VideoView) findViewById(R.id.videoView);

    	MediaPlayer mp = new MediaPlayer();
    	mp.setDisplay(videoView.getHolder());

    	try {
			mp.setDataSource(
					getApplicationContext(), 
					Uri.parse("android.resource://" + 
							getPackageName() + 
							"/" + 
							R.raw.sample_vid));
		} catch (IllegalArgumentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (SecurityException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalStateException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

    	try {
			mp.prepare();
		} catch (IllegalStateException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

    	mp.start();

    	// Create an instance of front-facing Camera
        Camera mCamera = Camera.open(1);

        mCamera.setDisplayOrientation(90);

        // video recording sequence - ORDER IS IMPORTANT!
        mCamera.unlock();

        MediaRecorder mr = new MediaRecorder();

        mr.setPreviewDisplay(mHolder.getSurface());

        mr.setCamera(mCamera);
        mr.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mr.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        mr.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_720P));

        try {
        	Log.d("tag", "External Storage State = " + Environment.getExternalStorageState());
        	File path = 
        		Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
        	if (!path.exists()) {
        		path.mkdirs();
        	}
        	File file = new File(path.getAbsolutePath() + "/mv_" + System.currentTimeMillis() + ".mp4");
        	if (!file.canWrite()) {
        		Log.d("tag", "Can't write to file - " + file.getAbsolutePath());
        	}
        	file.delete();
        	mr.setOutputFile(file.getAbsolutePath());
        	Log.d("tag", "setOutputFile:" + file);
			//mr.setOutputFile(createImageFile().getAbsolutePath());
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}

        try {
			mr.prepare();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}

        mr.start();

        try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        mr.stop();
        mr.reset();
        mr.release();
        mCamera.stopPreview();
        mCamera.release();

        // stop the MediaPlayer
        mp.stop();
    }

    private void dispatchTakePictureIntent() {
    	Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    	if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.parse(mCurrentPhotoPath)); //fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
    	}
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String imageFileName = "thumb";
        String state = Environment.getExternalStorageState();
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        storageDir.mkdir();
        File image = File.createTempFile(
            imageFileName,  /* prefix */
            ".mp4",         /* suffix */
            storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
        	Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            File f = new File(mCurrentPhotoPath);
            Uri contentUri = Uri.fromFile(f);
            mediaScanIntent.setData(contentUri);
            this.sendBroadcast(mediaScanIntent);

            int targetW = mImageView.getWidth();
            int targetH = mImageView.getHeight();

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
            mImageView.setImageBitmap(bitmap);
        }
    }

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		
	}
}
