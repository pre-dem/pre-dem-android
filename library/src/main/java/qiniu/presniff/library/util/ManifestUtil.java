package qiniu.presniff.library.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Misty on 5/22/17.
 */

public class ManifestUtil {

    public static final String APP_IDENTIFIER_KEY = "qiniu.presniff.crash.httplibrary.appIdentifier";

    public static final String APP_IDENTIFIER_PATTERN = "[0-9a-f]+";

    public static final int APP_IDENTIFIER_LENGTH = 32;

    private static final Pattern appIdentifierPattern = Pattern.compile(APP_IDENTIFIER_PATTERN, Pattern.CASE_INSENSITIVE);

    public static String getAppIdentifier(Context context){
        return getManifestString(context, APP_IDENTIFIER_KEY);
    }

    public static String getManifestString(Context context, String key){
        return getBundle(context).getString(key);
    }

    public static String sanitizeAppIdentifier(String appIdentifier) throws IllegalArgumentException{
        if (appIdentifier == null) {
            throw new IllegalArgumentException("App ID must not be null.");
        }

        String sAppIdentifier = appIdentifier.trim();

        Matcher matcher = appIdentifierPattern.matcher(sAppIdentifier);

        if (sAppIdentifier.length() != APP_IDENTIFIER_LENGTH) {
            throw new IllegalArgumentException("App ID length must be " + APP_IDENTIFIER_LENGTH + " characters.");
        } else if (!matcher.matches()) {
            throw new IllegalArgumentException("App ID must match regex pattern /" + APP_IDENTIFIER_PATTERN + "/i");
        }

        return sAppIdentifier;
    }

    public static Bundle getBundle(Context context){
        Bundle bundle;
        try {
            bundle = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA).metaData;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
        return bundle;
    }
}
