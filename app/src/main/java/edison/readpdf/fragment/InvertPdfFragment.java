package edison.readpdf.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.airbnb.lottie.LottieAnimationView;
import com.dd.morphingbutton.MorphingButton;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import edison.readpdf.R;
import edison.readpdf.adapter.FilesListAdapter;
import edison.readpdf.adapter.MergeFilesAdapter;
import edison.readpdf.database.DatabaseHelper;
import edison.readpdf.interfaces.BottomSheetPopulate;
import edison.readpdf.interfaces.OnBackPressedInterface;
import edison.readpdf.interfaces.OnPDFCreatedInterface;
import edison.readpdf.util.BottomSheetCallback;
import edison.readpdf.util.BottomSheetUtils;
import edison.readpdf.util.CommonCodeUtils;
import edison.readpdf.util.DialogUtils;
import edison.readpdf.util.FileUtils;
import edison.readpdf.util.InvertPdf;
import edison.readpdf.util.MorphButtonUtility;
import edison.readpdf.util.PermissionsUtils;
import edison.readpdf.util.RealPathUtil;
import edison.readpdf.util.StringUtils;

import static android.app.Activity.RESULT_OK;
import static edison.readpdf.util.Constants.REQUEST_CODE_FOR_WRITE_PERMISSION;
import static edison.readpdf.util.Constants.WRITE_PERMISSIONS;

public class InvertPdfFragment extends Fragment implements MergeFilesAdapter.OnClickListener,
        FilesListAdapter.OnFileItemClickedListener, BottomSheetPopulate, OnPDFCreatedInterface, OnBackPressedInterface {

    private Activity mActivity;
    private String mPath;
    private MorphButtonUtility mMorphButtonUtility;
    private FileUtils mFileUtils;
    private BottomSheetUtils mBottomSheetUtils;
    private static final int INTENT_REQUEST_PICK_FILE_CODE = 10;
    private MaterialDialog mMaterialDialog;
    private BottomSheetBehavior mSheetBehavior;

    @BindView(R.id.lottie_progress)
    LottieAnimationView mLottieProgress;
    @BindView(R.id.selectFile)
    MorphingButton selectFileButton;
    @BindView(R.id.invert)
    MorphingButton invertPdfButton;
    @BindView(R.id.bottom_sheet)
    LinearLayout layoutBottomSheet;
    @BindView(R.id.upArrow)
    ImageView mUpArrow;
    @BindView(R.id.downArrow)
    ImageView mDownArrow;
    @BindView(R.id.layout)
    RelativeLayout mLayout;
    @BindView(R.id.recyclerViewFiles)
    RecyclerView mRecyclerViewFiles;
    @BindView(R.id.view_pdf)
    Button mViewPdf;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_invert_pdf, container, false);
        ButterKnife.bind(this, rootView);
        mSheetBehavior = BottomSheetBehavior.from(layoutBottomSheet);
        mSheetBehavior.setBottomSheetCallback(new BottomSheetCallback(mUpArrow, isAdded()));
        mLottieProgress.setVisibility(View.VISIBLE);
        mBottomSheetUtils.populateBottomSheetWithPDFs(this);
        getRuntimePermissions();

        resetValues();
        return rootView;
    }

    @OnClick(R.id.viewFiles)
    void onViewFilesClick(View view) {
        mBottomSheetUtils.showHideSheet(mSheetBehavior);
    }

    /**
     * Displays file chooser intent
     */
    @OnClick(R.id.selectFile)
    public void showFileChooser() {
        startActivityForResult(mFileUtils.getFileChooser(),
                INTENT_REQUEST_PICK_FILE_CODE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) throws NullPointerException {
        if (data == null || resultCode != RESULT_OK || data.getData() == null)
            return;
        if (requestCode == INTENT_REQUEST_PICK_FILE_CODE) {
            //Getting Absolute Path
            String path = RealPathUtil.getInstance().getRealPath(getContext(), data.getData());
            setTextAndActivateButtons(path);
        }
    }

    //Inverts colors in PDF
    @OnClick(R.id.invert)
    public void parse() {
        new InvertPdf(mPath, this).execute();
    }


    private void resetValues() {
        mPath = null;
        mMorphButtonUtility.initializeButton(selectFileButton, invertPdfButton);
    }

    private void setTextAndActivateButtons(String path) {
        mPath = path;
        // Remove stale "View PDF" button
        mViewPdf.setVisibility(View.GONE);
        mMorphButtonUtility.setTextAndActivateButtons(path,
                selectFileButton, invertPdfButton);
    }

    @Override
    public void onPopulate(ArrayList<String> paths) {
        CommonCodeUtils.getInstance().populateUtil(mActivity, paths,
                this, mLayout, mLottieProgress, mRecyclerViewFiles);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (Activity) context;
        mMorphButtonUtility = new MorphButtonUtility(mActivity);
        mFileUtils = new FileUtils(mActivity);
        mBottomSheetUtils = new BottomSheetUtils(mActivity);
    }

    @Override
    public void onItemClick(String path) {
        mSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        setTextAndActivateButtons(path);
    }

    @Override
    public void onFileItemClick(String path) {
        mFileUtils.openFile(requireContext(),path, FileUtils.FileType.e_PDF);
    }

    private void viewPdfButton(String path) {
        mViewPdf.setVisibility(View.VISIBLE);
        mViewPdf.setOnClickListener(v -> mFileUtils.openFile(requireContext(),path, FileUtils.FileType.e_PDF));
    }

    @Override
    public void onPDFCreationStarted() {
        mMaterialDialog = DialogUtils.getInstance().createAnimationDialog(mActivity);
        mMaterialDialog.show();
    }

    @Override
    public void onPDFCreated(boolean isNewPdfCreated, String path) {
        mMaterialDialog.dismiss();
        if (!isNewPdfCreated) {
            StringUtils.getInstance().showSnackbar(mActivity, R.string.snackbar_invert_unsuccessful);
            return;
        }
        new DatabaseHelper(mActivity).insertRecord(path, mActivity.getString(R.string.snackbar_invert_successfull));
        StringUtils.getInstance().getSnackbarwithAction(mActivity, R.string.snackbar_pdfCreated)
                .setAction(R.string.snackbar_viewAction,
                        v -> mFileUtils.openFile(requireContext(),path, FileUtils.FileType.e_PDF)).show();
        viewPdfButton(path);
        resetValues();
    }

    public void closeBottomSheet() {
        CommonCodeUtils.getInstance().closeBottomSheetUtil(mSheetBehavior);
    }

    public boolean checkSheetBehaviour() {
        return CommonCodeUtils.getInstance().checkSheetBehaviourUtil(mSheetBehavior);
    }

    /***
     * check runtime permissions for storage and camera
     ***/
    private void getRuntimePermissions() {
        PermissionsUtils.getInstance().requestRuntimePermissions(this,
                    WRITE_PERMISSIONS,
                    REQUEST_CODE_FOR_WRITE_PERMISSION);
    }
}