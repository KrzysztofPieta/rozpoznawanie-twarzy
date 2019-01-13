package damianrolewsk.com.detektortwarzy;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private static  final int PERMISSION_REQUEST=0;
    private static final int RESULT_LOAD_IMAGE =2;
    static final int CAM_REQUEST=1;

    /*elementy Main_activity*/
    ImageView obraz;
    Button button;
    Button camera;
    Bitmap myBitmap;
    Paint myRectPaint = new Paint();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*ramka*/

        myRectPaint.setStrokeWidth(5);
        myRectPaint.setColor(Color.RED);
        myRectPaint.setStyle(Paint.Style.STROKE);

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST );
        }

        obraz = findViewById(R.id.imageView);
        button = findViewById(R.id.button);
        camera = findViewById(R.id.button2);

        /*funkcja uzycia przycisku galerii*/
        button.setOnClickListener((e)-> {

            Intent i = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(i,RESULT_LOAD_IMAGE);

        });

        /*obsluga przycisku aparatu*/
        camera.setOnClickListener((v)->{

            Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            File file = getFile();
            camera_intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
            startActivityForResult(camera_intent, CAM_REQUEST);


        });

    }


    private File getFile() {
        File folder = new File("sdcard/camera_app");

        if(!folder.exists()){
            folder.mkdir();
        }

        File image_file = new File(folder, "cam_image.jpg");
        return image_file;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,Intent data) {

        switch (requestCode) {
            case RESULT_LOAD_IMAGE:
                if (resultCode == RESULT_OK) {
                    Uri selectedImage = data.getData();
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};
                    Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                    cursor.moveToFirst();
                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String picturePath = cursor.getString(columnIndex);
                    cursor.close();
                    obraz.setImageBitmap(BitmapFactory.decodeFile(picturePath));
                    myBitmap = BitmapFactory.decodeFile(picturePath);





                    Bitmap tempBitmap = Bitmap.createBitmap(myBitmap.getWidth(), myBitmap.getHeight(), Bitmap.Config.RGB_565);
                    Canvas tempCanvas = new Canvas(tempBitmap);
                    tempCanvas.drawBitmap(myBitmap, 0, 0, null);

                    FaceDetector faceDetector = new
                            FaceDetector.Builder(getApplicationContext()).setTrackingEnabled(false)
                            .build();

                    Frame frame = new Frame.Builder().setBitmap(myBitmap).build();
                    SparseArray<Face> faces = faceDetector.detect(frame);

                    for(int i=0; i<faces.size(); i++) {
                        Face thisFace = faces.valueAt(i);
                        float x1 = thisFace.getPosition().x;
                        float y1 = thisFace.getPosition().y;
                        float x2 = x1 + thisFace.getWidth();
                        float y2 = y1 + thisFace.getHeight();
                        tempCanvas.drawRoundRect(new RectF(x1, y1, x2, y2), 2, 2, myRectPaint);
                    }
                    obraz.setImageDrawable(new BitmapDrawable(getResources(),tempBitmap));

                    break;
                }

                /*wykrywanie z aparatu*/
            case CAM_REQUEST:
                String path = "sdcard/camera_app/cam_image.jpg"; /* sciezka do zrobionego zdjecia*/

                myBitmap = BitmapFactory.decodeFile(path); /* przetworzenie sciezki na mape bitowa*/

                Bitmap tempBitmap = Bitmap.createBitmap(myBitmap.getWidth(), myBitmap.getHeight(), Bitmap.Config.RGB_565); /*pobieranie parametrow obrazu*/
                Canvas tempCanvas = new Canvas(tempBitmap);
                tempCanvas.drawBitmap(myBitmap, 0, 0, null);

                /*tworzenie obiektu facedetectora*/
                FaceDetector faceDetector = new
                        FaceDetector.Builder(getApplicationContext()).setTrackingEnabled(false)
                        .build();

                Frame frame = new Frame.Builder().setBitmap(myBitmap).build();
                SparseArray<Face> faces = faceDetector.detect(frame);

                /*wylapywanie wspolrzednych twarzy i przypisywanie ich ramce*/
                for(int i=0; i<faces.size(); i++) {
                    Face thisFace = faces.valueAt(i);
                    float x1 = thisFace.getPosition().x;
                    float y1 = thisFace.getPosition().y;
                    float x2 = x1 + thisFace.getWidth();
                    float y2 = y1 + thisFace.getHeight();
                    tempCanvas.drawRoundRect(new RectF(x1, y1, x2, y2), 2, 2, myRectPaint);
                }
                /*ustawienie obrazy z ramka na nasz imageView*/
                obraz.setImageDrawable(new BitmapDrawable(getResources(),tempBitmap));



                break;

            default :
                break;
        }

    }
}
