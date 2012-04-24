/*
** 2011 April 5
**
** The author disclaims copyright to this source code.  In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
*/

package info.ata4.bsplib.util;

import info.ata4.bsplib.vector.Vector3f;
import java.util.ArrayList;

/**
 * Quick'n dirty utility class for frequently used string functions.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class StringUtils {
    
    private StringUtils() {
    }

    /**
     * A faster and simpler character-based string splitter
     * based on http://forums.sun.com/thread.jspa?threadID=766801&start=0&tstart=0
     *
     * @param text parsed input string
     * @param separator separator character
     * @return splitted string array
     */
    public static String[] fastSplit(String text, char separator) {
        ArrayList<String> result = new ArrayList<String>();
        if (text != null && text.length() > 0) {
            int index1 = 0;
            int index2 = text.indexOf(separator);

            while (index2 >= 0) {
                String token = text.substring(index1, index2);
                result.add(token);
                index1 = index2 + 1;
                index2 = text.indexOf(separator, index1);
            }

            result.add(text.substring(index1));
        }

        return result.toArray(new String[result.size()]);
    }
    
    public static int countChar(String haystack, char needle) {
        int count = 0;
        for (int i = 0, l = haystack.length(); i < l; i++) {
            if (haystack.charAt(i) == needle) {
                count++;
            }
        }
        return count;
    }
    
    public static boolean containsChar(String haystack, char needle) {
        for (int i = 0, l = haystack.length(); i < l; i++) {
            if (haystack.charAt(i) == needle) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Simulates the MAKEID macro from the Source SDK. It makes a 4-byte
     * "packed ID" int out of a 4 character string.
     * 
     * @param id 4 character string
     * @return packed integer ID
     */
    public static int makeID(String id) {
        if (id.length() != 4) {
            throw new IllegalArgumentException("String must be exactly 4 characters long");
        }
        
        byte[] bytes = id.getBytes();
        return (bytes[3] << 24) | (bytes[2] << 16) | (bytes[1] << 8) | bytes[0];
    }
    
    /**
     * Decodes a "packed ID" into a 4 character string that was made by 
     * the MAKEID macro from the Source SDK.
     * 
     * @param id packed integer ID
     * @return 4 character string
     */
    public static String unmakeID(int id) {
        byte[] bytes = new byte[] {
            (byte) id,
            (byte) (id >>> 8),
            (byte) (id >>> 16),
            (byte) (id >>> 24)
        };
        return new String(bytes);
    }
    
    /**
     * Parses a Vector3f from a string with space-separated float values.
     *
     * @param string vector in string form, separated by two spaces
     * @exception  NumberFormatException if the string contains unparsable float fields
     * @return Vector3f value of the string
     */
    public static Vector3f parseVector(String string) {
        // remove extra whitespaces
        string = string.replaceAll("\\s{2,}", " ");

        // split string
        String[] costr = fastSplit(string, ' ');

        float x = costr.length > 0 ? Float.parseFloat(costr[0]) : 0;
        float y = costr.length > 1 ? Float.parseFloat(costr[1]) : 0;
        float z = costr.length > 2 ? Float.parseFloat(costr[2]) : 0;

        return new Vector3f(x, y, z);
    }
}
