package com.android.coreintelli.contacts;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class HomeMainActivity extends AppCompatActivity {
    private TextInputLayout textInputLayout_name, textInputLayout_emai, textInputLayout_number, textInputLayout_group;
    private EditText editText_name, editText_email, editText_number, editText_group;
    private ImageButton imageButton_contacts_image, imageButton_contacts_imports;
    private Button button_concel, button_save;

    private AlertDialog levelDialog;

    // Strings to Show In Dialog with Radio Buttons
    final CharSequence[] chooseimage = {" Capture "," Galley "};

    private Context context = HomeMainActivity.this;
    private String mCurrentPhotoPath;

    private Bitmap mBitmap;
    private final static int PERMISSION_REQUEST_CODE_READ_CONTACTS=001;
    private final static int PERMISSION_REQUEST_CODE_WRITE_CONTACTS=002;
    private final static int RESULT_LOAD_IMG_FROM_GALLERY=003;
    private final static int CAMERA_REQUEST_CODE=004;
    private final static int RESULT_LOAD_CONTACTS=005;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        fab.setVisibility(View.GONE);

        initializeViews();


    }

    private void initializeViews() {
        imageButton_contacts_image = findViewById(R.id.id_contact_image_imageButton);
        imageButton_contacts_imports = findViewById(R.id.id_contacts_imports_imageView);

        textInputLayout_name = findViewById(R.id.id_contact_name_txtIPLayout);
        textInputLayout_emai = findViewById(R.id.id_contact_email_txtIPLayout);
        textInputLayout_number = findViewById(R.id.id_contact_number_txtIPLayout);
        textInputLayout_group = findViewById(R.id.id_contact_group_txtIPLayout);

        editText_name = findViewById(R.id.id_contact_name_edt);
        editText_email = findViewById(R.id.id_contact_email_edt);
        editText_number = findViewById(R.id.id_contact_number_edt);
        editText_group = findViewById(R.id.id_contact_group_edt);

        button_save = findViewById(R.id.id_contacts_save_button);
        button_concel = findViewById(R.id.id_contacts_cancel_button);


        onlClickViews();

    }

    private void onlClickViews() {
        imageButton_contacts_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Creating and Building the Dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Choose Image From");
                builder.setSingleChoiceItems(chooseimage, -1, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {

                        switch(item)
                        {
                            case 0:
                                // Your code when first option seletced
                                captureImage();

                                break;
                            case 1:

                                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                                intent.setType("*/*");
                                intent.addCategory(Intent.CATEGORY_OPENABLE);
                                try {
                                    //startActivityForResult(chooser, FILE_SELECT_CODE);
                                    startActivityForResult(Intent.createChooser(intent, "Select a File to Upload"), RESULT_LOAD_IMG_FROM_GALLERY);
                                } catch (Exception ex) {
                                    System.out.println("browseClick :" + ex);//android.content.ActivityNotFoundException ex
                                }
                                break;
                        }
                        levelDialog.dismiss();
                    }
                });
                levelDialog = builder.create();
                levelDialog.show();




            }
        });

        imageButton_contacts_imports.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                try {
                    //startActivityForResult(chooser, FILE_SELECT_CODE);
                    startActivityForResult(Intent.createChooser(intent, "Select a File to Upload"), RESULT_LOAD_CONTACTS);
                } catch (Exception ex) {
                    System.out.println("browseClick :" + ex);//android.content.ActivityNotFoundException ex
                }

            }
        });
        button_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!hasPhoneContactsPermission(Manifest.permission.WRITE_CONTACTS))
                {
                    requestPermission(Manifest.permission.WRITE_CONTACTS, PERMISSION_REQUEST_CODE_WRITE_CONTACTS);
                }else
                {
                    saveContactIntoPhone();
                }
            }
        });
        button_concel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, HomeMainActivity.class);
                startActivity(intent);
            }
        });
    }

    private void captureImage() {
        {
//                captureImage();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
    
                    requestPermissions(new String[]{Manifest.permission.CAMERA},
                            CAMERA_REQUEST_CODE);
                }else{
                    takePicture();
                }
            }else{
                takePicture();

            }

//                dispatchTakePictureIntent();

        }
    }

    private void takePicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
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
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.contacts.android.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
            }
        }

    }
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }


    // Check whether user has phone contacts manipulation permission or not.
    private boolean hasPhoneContactsPermission(String permission)
    {
        boolean ret = false;

        // If android sdk version is bigger than 23 the need to check run time permission.
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            // return phone read contacts permission grant status.
            int hasPermission = ContextCompat.checkSelfPermission(getApplicationContext(), permission);
            // If permission is granted then return true.
            if (hasPermission == PackageManager.PERMISSION_GRANTED) {
                ret = true;
            }
        }else
        {
            ret = true;
        }
        return ret;
    }

    // Request a runtime permission to app user.
    private void requestPermission(String permission, int requestCode)
    {
        String requestPermissionArray[] = {permission};
        ActivityCompat.requestPermissions(this, requestPermissionArray, requestCode);
    }

    // After user select Allow or Deny button in request runtime permission dialog
    // , this method will be invoked.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        int length = grantResults.length;

        if(length > 0)
        {
            int grantResult = grantResults[0];

            if(grantResult == PackageManager.PERMISSION_GRANTED) {

                if(requestCode==PERMISSION_REQUEST_CODE_READ_CONTACTS)
                {
                    // If user grant read contacts permission.
//                    readPhoneContacts();
                }else if(requestCode==PERMISSION_REQUEST_CODE_WRITE_CONTACTS)
                {
                    // If user grant write contacts permission then start add phone contact activity.
                    saveContactIntoPhone();
                }
            }else
            {
                Toast.makeText(getApplicationContext(), "You denied permission.", Toast.LENGTH_LONG).show();
            }
            if(grantResult==PackageManager.PERMISSION_GRANTED){
                if(requestCode==CAMERA_REQUEST_CODE){
                    captureImage();
                }
            }else{
                Toast.makeText(getApplicationContext(), "You denied permission.", Toast.LENGTH_LONG).show();

            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

            if (requestCode == RESULT_LOAD_IMG_FROM_GALLERY) {
                // Getting the uri of the picked photo
                Uri selectedImage = data.getData();

                assert selectedImage != null;
                String imageString = selectedImage.toString();
                System.out.println("string uri path:" + imageString);

                System.out.println("File name:" + selectedImage);
                String mimeType = getContentResolver().getType(selectedImage);

                if (mimeType != null &&
                        (mimeType.equalsIgnoreCase("image/jpeg")
                                || mimeType.equalsIgnoreCase("image/jpg")
                                || mimeType.equalsIgnoreCase("image/png"))) {

                    try {
                        mBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage));
                        imageButton_contacts_image.setImageBitmap(mBitmap);

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                // Creating bitmap of the selected image from its inputstream
//            mBitmap = BitmapFactory.decodeStream(imageStream);

                // Getting reference to ImageView
                // Setting Bitmap to ImageButton
            }

        if(requestCode==CAMERA_REQUEST_CODE){

            mBitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);
            imageButton_contacts_image.setImageBitmap(mBitmap);

        }
        if(requestCode==RESULT_LOAD_CONTACTS){
            Uri fileLoad = data.getData();

            assert fileLoad != null;
            String imageString = fileLoad.toString();
            System.out.println("string uri path:" + imageString);

            System.out.println("File name:" + fileLoad);
            String mimeType = getContentResolver().getType(fileLoad);
            Toast.makeText(context,"Functionality not developed!!",Toast.LENGTH_LONG).show();
        }


    }

    private void saveContactIntoPhone() {
        if(TextUtils.isEmpty(editText_name.getText().toString())){
            editText_name.setError("Name is empty");
        }else if(TextUtils.isEmpty(editText_number.getText().toString())){
            editText_email.setError("Email is empty");

        }else {

            ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
            int rawContactID = ops.size();

            ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                    .build());
            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, editText_name.getText().toString())
                    .build());
            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, editText_number.getText().toString())
                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                    .build());

/*
        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, etHomePhone.getText().toString())
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_HOME).build());
*/

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            if(mBitmap!=null){    // If an image is selected successfully
                mBitmap.compress(Bitmap.CompressFormat.PNG , 75, stream);

                // Adding insert operation to operations list
                // to insert Photo in the table ContactsContract.Data
                ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID)
                        .withValue(ContactsContract.Data.IS_SUPER_PRIMARY, 1)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Photo.PHOTO,stream.toByteArray())
                        .build());

                try {
                    stream.flush();
                }catch (IOException e) {
                    e.printStackTrace();
                }

            }
            if(!TextUtils.isEmpty(editText_email.getText().toString())) {
                ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Email.ADDRESS, editText_email.getText().toString())
                        .withValue(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_HOME)
                        .build());
            }

            if (!TextUtils.isEmpty(editText_group.getText().toString())) {
                ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Email.DATA10, editText_group.getText().toString())
                        .withValue(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK)
                        .build());
            }
            try {
                getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
                Toast.makeText(getBaseContext(), "Contact is successfully added", Toast.LENGTH_SHORT).show();

                Intent intent=new Intent(context,HomeMainActivity.class);
                startActivity(intent);


            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (OperationApplicationException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
