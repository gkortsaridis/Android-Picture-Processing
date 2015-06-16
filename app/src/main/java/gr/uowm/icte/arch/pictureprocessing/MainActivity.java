package gr.uowm.icte.arch.pictureprocessing;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends ActionBarActivity {
    Button b1;
    ImageView iv;
    TextView ResultTxt;
    private static final int TAKE_PHOTO_CODE = 0;

    LongOperation lo;

    int[][] Rs;
    int[][] Gs;
    int[][] Bs;
    int Ravg , Gavg , Bavg;

    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_main);

        System.gc();

        b1=(Button)findViewById(R.id.button);
        iv=(ImageView)findViewById(R.id.imageView);

        ResultTxt = (TextView) findViewById(R.id.resultTxt);



        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
            }
        });
    }


    private void takePhoto(){

        System.gc();
        final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(getTempFile(this)) );
        startActivityForResult(intent, TAKE_PHOTO_CODE);
    }

    private File getTempFile(Context context){
        //it will return /sdcard/image.tmp
        final File path = new File( Environment.getExternalStorageDirectory(), context.getPackageName() );
        if(!path.exists()){
            path.mkdir();
        }
        return new File(path, "image.tmp");
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {


        if (resultCode == RESULT_OK) {
            //Log.i("requestCode",requestCode+"");
            switch(requestCode){
                case TAKE_PHOTO_CODE:
                    final File file = getTempFile(this);
                    try {
                        Bitmap captureBmp = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.fromFile(file));
                        // do whatever you want with the bitmap (Resize, Rename, Add To Gallery, etc)

                        lo = new LongOperation(captureBmp);
                        lo.execute("");



                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    private class LongOperation extends AsyncTask<String, Void, String> {

        Bitmap bp;

        LongOperation(Bitmap image){
            bp = image;
        }

        @Override
        protected String doInBackground(String... params) {

            System.gc();
            Rs = new int[2][2];
            Gs = new int[2][2];
            Bs = new int[2][2];

            int Xmax = bp.getWidth();
            int Ymax = bp.getHeight();

            Log.i("Width : ",""+Xmax);
            Log.i("Height : ",""+Ymax);

            Rs = new int[Xmax][Ymax];
            Gs = new int[Xmax][Ymax];
            Bs = new int[Xmax][Ymax];

            for(int x=0; x<Xmax; x++) {
                for (int y = 0; y < Ymax; y++) {
                    int colour = bp.getPixel(x, y);

                    Rs[x][y] = Color.red(colour);
                    Bs[x][y] = Color.blue(colour);
                    Gs[x][y] = Color.green(colour);
                }
            }

            Ravg = Gavg = Bavg=0;

            for(int x=0; x<Xmax; x++) {
                for (int y = 0; y < Ymax; y++) {
                    Ravg += Rs[x][y];
                    Gavg += Gs[x][y];
                    Bavg += Bs[x][y];
                }
            }

            Ravg = Ravg/(Xmax*Ymax);
            Gavg = Gavg/(Xmax*Ymax);
            Bavg = Bavg/(Xmax*Ymax);
            Log.i("Finished","RBG Process");
            return "R: "+Ravg+" G: "+Gavg+" B : "+Bavg+"\n"+"Width : "+Xmax+" Height : "+Ymax;
        }

        @Override
        protected void onPostExecute(String result) {

            dialog.cancel();
            ResultTxt.setText(result);
            iv.setImageBitmap(bp);
        }

        @Override
        protected void onPreExecute() {

            dialog = new ProgressDialog(MainActivity.this);
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setMessage("Your image is being processed...");
            dialog.setIndeterminate(true);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();

        }


    }


}