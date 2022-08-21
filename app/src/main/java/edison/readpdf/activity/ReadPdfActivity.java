package edison.readpdf.activity;

import static edison.readpdf.util.Constants.AUTHORITY_APP;
import static edison.readpdf.util.Constants.REQUEST_CODE_FOR_WRITE_PERMISSION;
import static edison.readpdf.util.Constants.WRITE_PERMISSIONS;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.github.barteksc.pdfviewer.PDFView;

import java.io.File;

import edison.readpdf.R;
import edison.readpdf.util.PermissionsUtils;

public class ReadPdfActivity extends AppCompatActivity {

    private PDFView mPdfView;

    public static final String PDF_PATH = "pdf_path";
    public static final String PDF_URI = "pdf_uri";
    private String mFilePath = "";
    private Uri mUri;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_pdf);
        mPdfView = findViewById(R.id.pdfView);
        mFilePath = getIntent().getStringExtra(PDF_PATH);
        mUri = getIntent().getParcelableExtra(PDF_URI);

        if (!PermissionsUtils.getInstance().checkRuntimePermissions(this, WRITE_PERMISSIONS)) {
            getRuntimePermissions();
        }else {
            openFile();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        openFile();
    }

    private void getRuntimePermissions() {
        PermissionsUtils.getInstance().requestRuntimePermissions(this,
                WRITE_PERMISSIONS,
                REQUEST_CODE_FOR_WRITE_PERMISSION);
    }

    private void openFile(){
        try {
            Uri uri = null;
            if (!TextUtils.isEmpty(mFilePath)) {
                uri = FileProvider.getUriForFile(this, AUTHORITY_APP, new File(mFilePath));
            }else if (mUri != null){
                uri = mUri;
            }

            if (uri != null) {
                mPdfView.fromUri(uri).load();
            }else {
                finish();
            }
        }catch (Exception e){
            finish();
        }
    }
}
