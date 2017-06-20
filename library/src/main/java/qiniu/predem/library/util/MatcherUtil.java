package qiniu.predem.library.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Misty on 17/6/15.
 */

public class MatcherUtil {
    public static final Pattern IP_Pattern = Pattern.compile("^[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+$");
    public static final Pattern MIMETYPE_Pattern = Pattern.compile("[a-zA-Z0-9]+/[a-zA-Z0-9]+");

    public static final String MATCH_TRACE_IP = "(?<=From )(?:[0-9]{1,3}\\.){3}[0-9]{1,3}";
    public static final String MATCH_PING_IP = "(?<=from ).*(?=: icmp_seq=1 ttl=)";
    public static final String MATCH_PING_TIME = "(?<=time=).*?ms";

    public static Matcher traceMatcher(String str) {
        Pattern patternTrace = Pattern.compile(MATCH_TRACE_IP);
        return patternTrace.matcher(str);
    }

    public static Matcher ipMatcher(String str) {
        Pattern patternIp = Pattern.compile(MATCH_PING_IP);
        return patternIp.matcher(str);
    }

    public static String getIpFromTraceMatcher(Matcher m) {
        String pingIp = m.group();
        int start = pingIp.indexOf('(');
        if (start >= 0) {
            pingIp = pingIp.substring(start + 1);
        }
        return pingIp;
    }

    public static Matcher timeMatcher(String str) {
        Pattern patternTime = Pattern.compile(MATCH_PING_TIME);
        return patternTime.matcher(str);
    }
}
