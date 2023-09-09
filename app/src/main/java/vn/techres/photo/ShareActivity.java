package vn.techres.photo;

import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.io.File;

import vn.techres.line.R;
import vn.techres.photo.utils.Utils;

public class ShareActivity extends AppCompatActivity implements View.OnClickListener {

    ImageView myImg, btnShare, btnDelete, btnBack;
    File file;
    String path;
    boolean isCreation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        myImg = findViewById(R.id.myImg);
        isCreation = getIntent().getExtras().getBoolean("isCreation");
        path = getIntent().getExtras().getString("path");
        file = new File(path);

        Glide.with(this).load(Uri.fromFile(file)).into(myImg);

        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(this);
        btnShare = findViewById(R.id.btnShare);
        btnShare.setOnClickListener(this);
        btnDelete = findViewById(R.id.btnDelete);
        btnDelete.setOnClickListener(this);

        LinearLayout adContainer = findViewById(R.id.banner_container);


    }

    @Override
    public void onBackPressed() {
        if (isCreation) {
            super.onBackPressed();
        } else {
            Utils.gotoMain(ShareActivity.this);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnBack:
                onBackPressed();
                break;

            case R.id.btnShare:

                Utils.mShare(path, ShareActivity.this);
                break;

            case R.id.btnDelete:

                mDelete(file);
                break;
        }
    }


    public void mDelete(final File mFile) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(ShareActivity.this);
        alertDialog.setTitle("Delete");
        alertDialog.setMessage("Are you Sure, You want to Delete this Image?");
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (mFile.exists()) {
                    boolean del = mFile.delete();
                    onBackPressed();
                }
            }
        });
        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alertDialog.show();
    }

}