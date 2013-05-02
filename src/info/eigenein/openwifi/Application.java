package info.eigenein.openwifi;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

@ReportsCrashes(
        formKey = "",
        mode = ReportingInteractionMode.DIALOG,
        resDialogText = R.string.crash_dialog_text,
        resDialogCommentPrompt = R.string.crash_dialog_comment_prompt,
        mailTo = "reports@openwifi.info")
public class Application extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();

        ACRA.init(this);
    }
}
