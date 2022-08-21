package edison.readpdf.ads;

public interface IAdActionListener<AD> {

    void onNativeAdLoadSuccess(AD ad);

    void onNativeAdLoadFail(String msg, int errorCode);
}
