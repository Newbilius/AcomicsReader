package newbilius.com.online_comics_reader;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import butterknife.ButterKnife;
import butterknife.OnClick;
import newbilius.com.online_comics_reader.Tools.FirebaseEventsHelper;
import newbilius.com.online_comics_reader.Tools.LinksHelpers;

public class InformationActivity extends AppCompatActivity {

    private FirebaseEventsHelper firebaseEventsHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);
        ButterKnife.bind(this);
        firebaseEventsHelper=new FirebaseEventsHelper(this);
        firebaseEventsHelper.openAboutPage();
    }

    @OnClick(R.id.buttonGoToSite)
    public void onClickGoToSite() {
        LinksHelpers.goToUrl(this, "https://acomics.ru/");
    }

    @OnClick(R.id.buttonSendEmail)
    public void onClickSendEmail() {
        LinksHelpers.sendEmail(this,
                "newbilius@gmail.com",
                "Android soft: " + getString(R.string.app_name));
    }

    @OnClick(R.id.buttonGoToStore)
    public void onClickGoToStore() {
        LinksHelpers.goToUrl(this, "market://search?q=pub:%D0%94%D0%BC%D0%B8%D1%82%D1%80%D0%B8%D0%B9+%D0%9C%D0%BE%D0%B8%D1%81%D0%B5%D0%B5%D0%B2");
    }
}
