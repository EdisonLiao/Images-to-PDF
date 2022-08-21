package edison.readpdf.fragment;

import static com.google.zxing.integration.android.IntentIntegrator.REQUEST_CODE;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import edison.readpdf.R;
import edison.readpdf.activity.ReadPdfActivity;

public class ReadLocalPdfFragment extends Fragment {

    private int request_code = 1001;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_read_local_pdf, container, false);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.addPdf).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("application/pdf");
                try {
                    startActivityForResult(intent, request_code);
                } catch (ActivityNotFoundException e) {}
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == request_code && data != null){
            Uri uri = data.getData();
            Intent intent = new Intent(requireContext(), ReadPdfActivity.class);
            intent.putExtra(ReadPdfActivity.PDF_URI,uri);
            requireContext().startActivity(intent);
        }
    }
}
