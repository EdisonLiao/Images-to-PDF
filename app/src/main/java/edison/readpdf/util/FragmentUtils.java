package edison.readpdf.util;

import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;

import edison.readpdf.R;
import edison.readpdf.fragment.AddImagesFragment;
import edison.readpdf.fragment.ExceltoPdfFragment;
import edison.readpdf.fragment.ExtractTextFragment;
import edison.readpdf.fragment.HistoryFragment;
import edison.readpdf.fragment.ImageToPdfFragment;
import edison.readpdf.fragment.InvertPdfFragment;
import edison.readpdf.fragment.MergeFilesFragment;
import edison.readpdf.fragment.PdfToImageFragment;
import edison.readpdf.fragment.QrBarcodeScanFragment;
import edison.readpdf.fragment.RemoveDuplicatePagesFragment;
import edison.readpdf.fragment.RemovePagesFragment;
import edison.readpdf.fragment.SplitFilesFragment;
import edison.readpdf.fragment.ViewFilesFragment;
import edison.readpdf.fragment.ZipToPdfFragment;
import edison.readpdf.fragment.texttopdf.TextToPdfFragment;

import static edison.readpdf.util.Constants.ADD_WATERMARK;
import static edison.readpdf.util.Constants.ADD_WATERMARK_KEY;
import static edison.readpdf.util.Constants.BUNDLE_DATA;
import static edison.readpdf.util.Constants.ROTATE_PAGES;
import static edison.readpdf.util.Constants.ROTATE_PAGES_KEY;

public class FragmentUtils {

    private final Context mContext;

    public FragmentUtils(Context mContext) {
        this.mContext = mContext;
    }

    public String getFragmentName(Fragment fragment) {
        String name = mContext.getString(R.string.app_name);
        if (fragment instanceof ImageToPdfFragment) {
            name = mContext.getString(R.string.images_to_pdf);
        } else if (fragment instanceof TextToPdfFragment) {
            name = mContext.getString(R.string.text_to_pdf);
        } else if (fragment instanceof QrBarcodeScanFragment) {
            name = mContext.getString(R.string.qr_barcode_pdf);
        } else if (fragment instanceof ExceltoPdfFragment) {
            name = mContext.getString(R.string.excel_to_pdf);
        } else if (fragment instanceof ViewFilesFragment) {
            name = checkViewFilesFragmentCode(fragment.getArguments());
        } else if (fragment instanceof HistoryFragment) {
            name = mContext.getString(R.string.history);
        } else if (fragment instanceof ExtractTextFragment) {
            name = mContext.getString(R.string.extract_text);
        } else if (fragment instanceof AddImagesFragment) {
            name = mContext.getString(R.string.add_images);
        } else if (fragment instanceof MergeFilesFragment) {
            name = mContext.getString(R.string.merge_pdf);
        } else if (fragment instanceof SplitFilesFragment) {
            name = mContext.getString(R.string.split_pdf);
        } else if (fragment instanceof InvertPdfFragment) {
            name = mContext.getString(R.string.invert_pdf);
        } else if (fragment instanceof RemoveDuplicatePagesFragment) {
            name = mContext.getString(R.string.remove_duplicate);
        } else if (fragment instanceof RemovePagesFragment) {
            name = fragment.getArguments() != null ?
                    fragment.getArguments().getString(BUNDLE_DATA) : null;
        } else if (fragment instanceof PdfToImageFragment) {
            name = mContext.getString(R.string.pdf_to_images);
        } else if (fragment instanceof ZipToPdfFragment) {
            name = mContext.getString(R.string.zip_to_pdf);
        }
        return name;
    }

    public boolean handleFragmentBottomSheetBehavior(Fragment fragment) {
        boolean bottomSheetBehaviour = false;
        if (fragment instanceof InvertPdfFragment) {
            bottomSheetBehaviour = ((InvertPdfFragment) fragment).checkSheetBehaviour();
            if (bottomSheetBehaviour) ((InvertPdfFragment) fragment).closeBottomSheet();
        } else if (fragment instanceof MergeFilesFragment) {
            bottomSheetBehaviour = ((MergeFilesFragment) fragment).checkSheetBehaviour();
            if (bottomSheetBehaviour) ((MergeFilesFragment) fragment).closeBottomSheet();
        } else if (fragment instanceof RemoveDuplicatePagesFragment) {
            bottomSheetBehaviour = ((RemoveDuplicatePagesFragment) fragment).checkSheetBehaviour();
            if (bottomSheetBehaviour) ((RemoveDuplicatePagesFragment) fragment).closeBottomSheet();
        } else if (fragment instanceof RemovePagesFragment) {
            bottomSheetBehaviour = ((RemovePagesFragment) fragment).checkSheetBehaviour();
            if (bottomSheetBehaviour) ((RemovePagesFragment) fragment).closeBottomSheet();
        } else if (fragment instanceof AddImagesFragment) {
            bottomSheetBehaviour = ((AddImagesFragment) fragment).checkSheetBehaviour();
            if (bottomSheetBehaviour) ((AddImagesFragment) fragment).closeBottomSheet();
        } else if (fragment instanceof PdfToImageFragment) {
            bottomSheetBehaviour = ((PdfToImageFragment) fragment).checkSheetBehaviour();
            if (bottomSheetBehaviour) ((PdfToImageFragment) fragment).closeBottomSheet();
        } else if (fragment instanceof SplitFilesFragment) {
            bottomSheetBehaviour = ((SplitFilesFragment) fragment).checkSheetBehaviour();
            if (bottomSheetBehaviour) ((SplitFilesFragment) fragment).closeBottomSheet();
        }
        return bottomSheetBehaviour;
    }

    /**
     * Checks the arguments of the ViewFilesFragment
     * to determine the name of the fragment.
     * @param arguments A Bundle containing the args of the fragment.
     * @return The name of the fragment.
     */
    private String checkViewFilesFragmentCode(Bundle arguments) {
        if (arguments != null) {
            int code = arguments.getInt(BUNDLE_DATA);
            if (code == ROTATE_PAGES) {
                return ROTATE_PAGES_KEY;
            } else if (code == ADD_WATERMARK) {
                return ADD_WATERMARK_KEY;
            }
        }
        return mContext.getString(R.string.viewFiles);
    }
}