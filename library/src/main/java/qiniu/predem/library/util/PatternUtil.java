package qiniu.predem.library.util;

import java.util.regex.Pattern;

/**
 * Created by Misty on 5/18/17.
 */

public class PatternUtil {
    public static final Pattern IP_Pattern = Pattern.compile("^[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+$");
    public static final Pattern MIMETYPE_Pattern = Pattern.compile("[a-zA-Z0-9]+/[a-zA-Z0-9]+");
}
