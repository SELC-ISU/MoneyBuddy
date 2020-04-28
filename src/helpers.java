import java.io.File;

/**
 *  Feel free to throw some bloat-code in here if you want to clean up your functions
 */
public class helpers {
    /**
     * List the databases already associated with the active user. Be sure that the output array is not empty before you start parsing the array.
     * @return the databases already associated with the active user
     */
    public static String[] dbList() {
        File folder = new File(System.getProperty("user.home") + "/.MoneyBuddy/");
        String[] filenames = folder.list();

        if (filenames != null) { // This would sometimes produce a NullPointerException otherwise
            /* Skim the file extensions off the filenames */
            for (int i = 0; i < filenames.length; i++) {
                filenames[i] = filenames[i].substring(0, filenames[i].lastIndexOf('.'));
            }
       }

        return filenames;
    }

    /**
     * Returns true/false based on whether or not there are any occurrences of String 'comparator' in the String[] inputArr
     * @param inputArr the array to check against
     * @param comparator the String to seek in the array
     * @return boolean whether or not the comparator exists in the array
     */
    public static boolean doesArrayContain(String[] inputArr, String comparator) {
        for (int i = 0; i < inputArr.length; i++) {
            if (inputArr[i].equals(comparator)) {
                return true;
            }
        }

        return false;
    }
}
