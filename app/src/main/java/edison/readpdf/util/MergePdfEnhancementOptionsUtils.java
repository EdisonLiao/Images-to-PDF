package edison.readpdf.util;

import android.content.Context;

import java.util.ArrayList;

import edison.readpdf.R;
import edison.readpdf.model.EnhancementOptionsEntity;

public class MergePdfEnhancementOptionsUtils {
    private static class SingletonHolder {
        static final MergePdfEnhancementOptionsUtils INSTANCE = new MergePdfEnhancementOptionsUtils();
    }

    public static MergePdfEnhancementOptionsUtils getInstance() {
        return MergePdfEnhancementOptionsUtils.SingletonHolder.INSTANCE;
    }

    public ArrayList<EnhancementOptionsEntity> getEnhancementOptions(Context context) {
        ArrayList<EnhancementOptionsEntity> options = new ArrayList<>();

        options.add(new EnhancementOptionsEntity(
                context, R.drawable.baseline_enhanced_encryption_24, R.string.set_password));
        return options;
    }
}