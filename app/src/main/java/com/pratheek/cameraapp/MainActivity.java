package com.pratheek.cameraapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    static final int CAMERA_PERMISSION_CODE = 101;
    static final int CAMERA_REQUEST_CODE = 102;
    static final int GALLERY_REQUEST_CODE = 105;
    static final int VIDEO_CAPTURE_REQUEST_CODE = 103;

    private ImageView selectedImage;
    String currentPhotoPath;
    String currentVideoPath;
    String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Attaches UI elements to the backend
        selectedImage = findViewById(R.id.displayImageView);
        ImageView cameraBtn = findViewById(R.id.cameraBtn);
        ImageView videoBtn = findViewById(R.id.videoBtn);
        Button galleryBtn = findViewById(R.id.galleryBtn);

        cameraBtn.setOnClickListener(view -> getCameraPermissions(CAMERA_REQUEST_CODE));

        videoBtn.setOnClickListener(view -> getCameraPermissions(VIDEO_CAPTURE_REQUEST_CODE));

        galleryBtn.setOnClickListener(view -> {
            /*
            * Intents are normally used to move between Activities within an application or to pass the action to another application.
            * In this case, we are passing the action of accessing the saved images to the gallery app
            */
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE);
        });
    }

    //Method to check if permission to access the camera is given. If yes, it opens the camera. If not, it asks for the permission
    public void getCameraPermissions(int requestCode) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        } else {
            if (requestCode == CAMERA_REQUEST_CODE)
                dispatchTakePictureIntent();
            else if (requestCode == VIDEO_CAPTURE_REQUEST_CODE)
                dispatchTakeVideoIntent();
        }
    }

    /*
    *This method handles the aftermath of the permission request.
    *  If the permission is given, it opens the app, else it sends a messages asking the user to give the permission.
    */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == CAMERA_PERMISSION_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                dispatchTakePictureIntent();
            }else {
                //Toasts are temporary displays which show a message for a certain period of time and then fades away. Duration can be adjusted by changing LENGTH_SHORT below.
                Toast.makeText(this, "Camera Permission is Required to Use camera.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /*
    * Intents are normally used to move between Activities within an application or to pass the action to another application.
    * In this case, we are passing the action of taking a photo to the camera app
    * This method sends the intent to the camera app while also creating a file to save the picture taken.
    */
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        // Create the File where the photo should go
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            // Error occurred while creating the File
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
        // Continue only if the File was successfully created
        if (photoFile != null) {
            Uri photoURI = FileProvider.getUriForFile(this,
                    "com.example.android.camerafileprovider",
                    photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
        }
    }

    //Method to create an a file to save the image with a name and extension.
    @SuppressLint("SimpleDateFormat")
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    /*
     * Intents are normally used to move between Activities within an application or to pass the action to another application.
     * In this case, we are passing the action of taking a video to the camera app
     * This method sends the intent to the camera app while also creating a file to save the video taken.
     */
    private void dispatchTakeVideoIntent() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

        File videoFile = null;
        try {
            videoFile = createVideoFile();
        } catch (IOException ex) {
            // Error occurred while creating the File
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
        if (videoFile != null) {
            Uri videoUri = FileProvider.getUriForFile(this,
                    "com.example.android.camerafileprovider",
                    videoFile);
            takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, videoUri);
            startActivityForResult(takeVideoIntent, VIDEO_CAPTURE_REQUEST_CODE);
        }
    }

    //Method to create an a file to save the image with a name and extension.
    @SuppressLint("SimpleDateFormat")
    private File createVideoFile() throws IOException {
        // Create an video file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String videoFileName = "MP4_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File video = File.createTempFile(
                videoFileName,  /* prefix */
                ".mp4",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentVideoPath = video.getAbsolutePath();
        return video;
    }

    /*
    * Once an intent is passed to an external application, if the action was temporary, then after it is completed, our app opens again.
    * The below method is used to perform certain operations once the user returns to our app
    */
    @SuppressLint("SimpleDateFormat")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //If user return from Camera app after taking a photo, save the image and attach it to the ImageView to be displayed.
        if (requestCode == CAMERA_REQUEST_CODE) {
            // Checks if user returns to our app after successfully performing the required action.
            if (resultCode == Activity.RESULT_OK) {
                File f = new File(currentPhotoPath);
                selectedImage.setImageURI(Uri.fromFile(f));
                Log.d(TAG, "Absolute Url of Image is " + Uri.fromFile(f));

                /* These 4 lines of code are used to inform the gallery app or any other application that is listening
                that a new image file has been created and it can be accessed by them.*/
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri contentUri = Uri.fromFile(f);
                mediaScanIntent.setData(contentUri);
                this.sendBroadcast(mediaScanIntent);
            }

        }

        //If user return from Camera app after taking a video, save the image and attach it to the ImageView to be displayed.
        if (requestCode == VIDEO_CAPTURE_REQUEST_CODE) {
            // Checks if user returns to our app after successfully performing the required action.
            if (resultCode == Activity.RESULT_OK) {
                File f = new File(currentVideoPath);
                Bitmap thumbnailBM = ThumbnailUtils.createVideoThumbnail(f.getAbsolutePath(), MediaStore.Images.Thumbnails.MINI_KIND);
                selectedImage.setImageBitmap(thumbnailBM);
                Log.d(TAG, "Absolute Url of Video is " + Uri.fromFile(f));

                /* These 4 lines of code are used to inform the gallery app or any other application that is listening
                that a new video file has been created and it can be accessed by them.*/
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri contentUri = Uri.fromFile(f);
                mediaScanIntent.setData(contentUri);
                this.sendBroadcast(mediaScanIntent);
            }
        }

        //If user return from Gallery app after choosing a file, save the image and attach it to the ImageView to be displayed.
        if (requestCode == GALLERY_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Uri contentUri = data != null ? data.getData() : null;
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String imageFileName = "JPEG_" + timeStamp + "." + getFileExt(contentUri);
                Log.d("tag", "onActivityResult: Gallery Image Uri:  " + imageFileName);
                selectedImage.setImageURI(contentUri);
            }
        }
    }

    //Method to get external file's location.
    private String getFileExt(Uri contentUri) {
        ContentResolver c = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(c.getType(contentUri));
    }
}