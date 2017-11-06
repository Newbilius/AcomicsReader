package newbilius.com.online_comics_reader;

import android.view.View;

import com.google.android.gms.ads.AdListener;

public class ReadAdListener extends AdListener {
    private View containerView;

    public ReadAdListener(View containerView) {
        this.containerView = containerView;
    }

    @Override
    public void onAdLeftApplication() {
        super.onAdLeftApplication();
    }

    @Override
    public void onAdOpened() {
        super.onAdOpened();
    }

    @Override
    public void onAdLoaded() {
        super.onAdLoaded();
    }

    @Override
    public void onAdClicked() {
        super.onAdClicked();
    }

    @Override
    public void onAdImpression() {
        super.onAdImpression();
    }

    @Override
    public void onAdFailedToLoad(int i) {
        super.onAdFailedToLoad(i);
        containerView.setVisibility(View.GONE);
    }

    @Override
    public void onAdClosed() {
        super.onAdClosed();
        containerView.setVisibility(View.GONE);
    }
}
