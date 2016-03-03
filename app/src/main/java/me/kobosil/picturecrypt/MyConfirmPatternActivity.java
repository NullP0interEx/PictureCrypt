package me.kobosil.picturecrypt;

import android.app.Activity;
import android.content.Intent;

import java.util.List;

import me.zhanghai.android.patternlock.ConfirmPatternActivity;
import me.zhanghai.android.patternlock.PatternUtils;
import me.zhanghai.android.patternlock.PatternView;

/**
 * Created by roman on 01.03.2016.
 */
public class MyConfirmPatternActivity extends ConfirmPatternActivity {

    @Override
    protected boolean isStealthModeEnabled() {
        // TODO: Return the value from SharedPreferences.
        return false;
    }

    @Override
    protected boolean isPatternCorrect(List<PatternView.Cell> pattern) {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("pattern", PatternUtils.patternToSha1String(pattern));
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
        return true;
    }

    @Override
    protected void onForgotPassword() {

        //startActivity(new Intent(this, YourResetPatternActivity.class));

        // Finish with RESULT_FORGOT_PASSWORD.
        super.onForgotPassword();
    }
}