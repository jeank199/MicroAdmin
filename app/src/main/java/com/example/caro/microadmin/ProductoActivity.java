package com.example.caro.microadmin;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AlertDialog;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import java.io.File;
import java.io.FileNotFoundException;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;


public class ProductoActivity extends AppCompatActivity {

    private String APP_DIRECTORY="MicroAdmin/";
    private String MEDIA_DIRECTORY= APP_DIRECTORY+"Imagenes";
    private final int MY_PERMISSIONS = 100;
    private final int PHOTO_CODE = 200;
    private final int SELECT_PICTURE = 300;
    private ImageView mSetimageView;
    private FloatingActionButton mbuttonImage;
    private RelativeLayout mRlView;

    private String mpath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_producto);


        mSetimageView = (ImageView) findViewById(R.id.producto_placeholder);
        mbuttonImage =(FloatingActionButton) findViewById(R.id.bt_subir_imagen);
        mRlView = (RelativeLayout) findViewById(R.id.layout_img);
        if(myRequestStoragePermission())
            mbuttonImage.setEnabled(true);
        else
            mbuttonImage.setEnabled(false);


        mbuttonImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showOptions();
            }
        });
    }

    private void showOptions() {
        final CharSequence[] options = {"Tomar Foto","Elegir de la galeria","Cancelar"};
        final AlertDialog.Builder builder = new AlertDialog.Builder(ProductoActivity.this);
        builder.setTitle("Elegir Opción");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(options[which]=="Tomar Foto"){
                    OpenCamera();
                }else if(options[which]=="Elegir de la galeria"){
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(intent.createChooser(intent,"Selecciona app de imagen"),SELECT_PICTURE);
                }else if(options[which]=="Cancelar"){
                    dialog.dismiss();
                }

            }
        });
        builder.show();
    }

    private void OpenCamera() {
        File file = new File(Environment.getExternalStorageDirectory(), MEDIA_DIRECTORY);
        boolean thisDirectoryExist = file.exists();

        if (!thisDirectoryExist) {

            thisDirectoryExist = file.mkdirs();
        }
        if (thisDirectoryExist) {

            Long timestamp = System.currentTimeMillis() / 1000;
            String pictureName = timestamp.toString() + ".jpg";
            mpath = Environment.getExternalStorageDirectory() + File.separator
                    + MEDIA_DIRECTORY + File.separator + pictureName;
            File newFile = new File(mpath);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(newFile));
            startActivityForResult(intent, PHOTO_CODE);
        }
    }


    private boolean myRequestStoragePermission() {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if((checkSelfPermission(WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED)&&
                (checkSelfPermission(CAMERA)== PackageManager.PERMISSION_GRANTED))
            return true;
        if((shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)) ||
                (shouldShowRequestPermissionRationale(CAMERA))){
            Snackbar.make(mRlView,"Los permisos son necesarios para usar la app",
                    Snackbar.LENGTH_INDEFINITE).setAction(android.R.string.ok,new View.OnClickListener(){
                @TargetApi(Build.VERSION_CODES.M)
                @Override
                public void onClick(View v){
                    requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE,CAMERA}, MY_PERMISSIONS);
                }
            }).show();

        }else{
            requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE,CAMERA}, MY_PERMISSIONS);
        }
        return false;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            switch (requestCode){
                case PHOTO_CODE:
                    MediaScannerConnection.scanFile(this, new String[]{mpath}, null,
                            new MediaScannerConnection.OnScanCompletedListener() {
                                @Override
                                public void onScanCompleted(String path, Uri uri) {
                                    Log.i("ExternalStorage", "Scanned" + path + ":");
                                    Log.i("ExternalStorage","-> Uri"+uri );
                                }
                            });

                    //Bitmap bitmap = BitmapFactory.decodeFile(mpath);
                    BitmapFactory.Options opts = new BitmapFactory.Options ();
                    opts.inSampleSize = 2;   // for 1/2 the image to be loaded
                    Bitmap thumb = Bitmap.createScaledBitmap (BitmapFactory.decodeFile(mpath, opts), 400, 400, false);
                    mSetimageView.setImageBitmap(thumb);
                    break;
                case SELECT_PICTURE:
                    Uri path = data.getData();
                    mSetimageView.setImageURI(path);
                    mSetimageView.setImageBitmap(reduceBitmap(this, path.toString(), 400,400));

                    break;

            }
        }
    }

    public static Bitmap reduceBitmap(Context contexto, String uri,
                                      int maxAncho, int maxAlto) {
        try {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(contexto.getContentResolver()
                    .openInputStream(Uri.parse(uri)), null, options);
            options.inSampleSize = (int) Math.max(
                    Math.ceil(options.outWidth / maxAncho),
                    Math.ceil(options.outHeight / maxAlto));
            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeStream(contexto.getContentResolver()
                    .openInputStream(Uri.parse(uri)), null, options);
        } catch (FileNotFoundException e) {
            Toast.makeText(contexto, "Fichero/recurso no encontrado",
                    Toast.LENGTH_LONG).show();
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == MY_PERMISSIONS){
            if(grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(ProductoActivity.this,"Permisos aceptados",Toast.LENGTH_SHORT).show();
                mbuttonImage.setEnabled(true);
            }else{
                showExplanation();
            }
        }
    }


    private void showExplanation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ProductoActivity.this);
        builder.setTitle("Permisos denegados");
        builder.setMessage("Para usar las funciones de la app, necesita aceptar los permisos");
        builder.setPositiveButton("Aceptar",new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog,int which){
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package",getPackageName(),null);
                intent.setData(uri);
                startActivity(intent);
            }
        });
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog,int which){
                dialog.dismiss();
                finish();
            }
        });
        builder.show();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("file_path",mpath);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mpath = savedInstanceState.getString("file_path");
    }



}

