package qiniu.predem.android.probe;

import java.util.ArrayList;
import java.util.List;

import qiniu.predem.android.config.Configuration;

/**
 * Created by Misty on 17/6/15.
 */

public class GlobalConfig {
    //不需要收集的域名
    protected static final List<String> ExcludeDomains = new ArrayList<>();
    private static final String TAG = "GlobalConfig";

    static {
        ExcludeDomains.add(Configuration.domain);
    }

    /**
     * 不包含
     *
     * @param domain
     * @return
     */
    public static boolean isExcludeHost(String domain) {
        for (String bean : ExcludeDomains) {
            if (match(bean, domain)) {
                return true;
            }
        }
        return false;
    }

    protected static boolean match(String pattern, String domain) {
        if (pattern == null){
            return false;
        }
        int index = 0;
        for (String part : pattern.split("\\*")) {
            if (part.length() == 0) continue;
            index = domain.indexOf(part, index);
            if (index < 0) return false;
            index += part.length();
        }
        return true;
    }
}
