package org.wso2.carbon.kernel.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple tokenizer class. Used to parse data.
 */
public class Tokenizer {
    protected char value[];
    protected int max;
    protected int cursor;

    public Tokenizer(String value) {
        this.value = value.toCharArray();
        max = this.value.length;
        cursor = 0;
    }

    private void skipWhiteSpace() {
        char[] val = value;
        int cur = cursor;

        for (; cur < max; cur++) {
            char c = val[cur];
            if ((c == ' ') || (c == '\t') || (c == '\n') || (c == '\r')) {
                continue;
            }
            break;
        }
        cursor = cur;
    }

    public String getToken(String terminals) {
        skipWhiteSpace();
        char[] val = value;
        int cur = cursor;

        int begin = cur;
        for (; cur < max; cur++) {
            char c = val[cur];
            if ((terminals.indexOf(c) != -1)) {
                break;
            }
        }
        cursor = cur;
        int count = cur - begin;
        if (count > 0) {
            skipWhiteSpace();
            while (count > 0 && (val[begin + count - 1] == ' ' || val[begin + count - 1] == '\t')) {
                count--;
            }
            return (new String(val, begin, count));
        }
        return (null);
    }

    public String getEscapedToken(String terminals) {
        char[] val = value;
        int cur = cursor;
        if (cur >= max) {
            return null;
        }
        StringBuffer sb = new StringBuffer();
        char c;
        for (; cur < max; cur++) {
            c = val[cur];
            // this is an escaped char
            if (c == '\\') {
                cur++; // skip the escape char
                if (cur == max) {
                    break;
                }
                c = val[cur]; // include the escaped char
            } else if (terminals.indexOf(c) != -1) {
                break;
            }
            sb.append(c);
        }

        cursor = cur;
        return sb.toString();
    }

    public List<String> getEscapedTokens(String terminals) {
        List<String> result = new ArrayList<String>();
        for (String token = getEscapedToken(terminals); token != null; token = getEscapedToken(terminals)) {
            result.add(token);
            getChar(); // consume terminal
        }
        return result;
    }

    public String getString(String terminals, String preserveEscapes) {
        skipWhiteSpace();
        char[] val = value;
        int cur = cursor;

        if (cur < max) {
            if (val[cur] == '\"') { /* if a quoted string */
                StringBuffer sb = new StringBuffer();
                cur++; /* skip quote */
                char c = '\0';
                int begin = cur;
                for (; cur < max; cur++) {
                    c = val[cur];
                    // this is an escaped char
                    if (c == '\\') {
                        cur++; // skip the escape char
                        if (cur == max) {
                            break;
                        }
                        c = val[cur]; // include the escaped char
                        if (preserveEscapes != null && preserveEscapes.indexOf(c) != -1) {
                            sb.append('\\'); // must preserve escapes for c
                        }
                    } else if (c == '\"') {
                        break;
                    }
                    sb.append(c);
                }
                int count = cur - begin;
                if (c == '\"') {
                    cur++;
                }
                cursor = cur;
                if (count > 0) {
                    skipWhiteSpace();
                    return sb.toString();
                }
            } else { /* not a quoted string; same as token */
                return getToken(terminals);
            }
        }
        return (null);
    }

    public String getString(String terminals) {
        return getString(terminals, null);
    }

    public char getChar() {
        int cur = cursor;
        if (cur < max) {
            cursor = cur + 1;
            return (value[cur]);
        }
        return ('\0'); /* end of value */
    }

    public boolean hasMoreTokens() {
        if (cursor < max) {
            return true;
        }
        return false;
    }
}
