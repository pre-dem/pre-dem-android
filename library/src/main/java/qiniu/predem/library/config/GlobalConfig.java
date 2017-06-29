package qiniu.predem.library.config;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Misty on 5/18/17.
 */

public class GlobalConfig {
    private static final String TAG = "GlobalConfig";

    //不需要收集的域名
    protected static final List<String> ExcludeDomains = new ArrayList<>();
    static {
        ExcludeDomains.add("hriygkee.bq.cloudappl.com");
        ExcludeDomains.add("jkbkolos.bq.cloudappl.com");
    }

    //想要收集的域名
    protected static final List<String> IncludeDomains = new ArrayList<>();
    static {
        IncludeDomains.add("www.baidu.com");
    }

    public static boolean isExcludeHost(String domain) {
        for (String bean: ExcludeDomains) {
            if (match(bean, domain)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isIncludeHost(String domain) {
        for (String bean: IncludeDomains) {
            if (match(bean, domain)) {
                return true;
            }
        }
        return false;
    }

    protected static boolean match(String pattern, String domain) {
        int index = 0;
        for (String part: pattern.split("\\*")) {
            if (part.length() == 0) continue;
            index = domain.indexOf(part, index);
            if (index < 0) return false;
            index += part.length();
        }
        return true;
    }
}
