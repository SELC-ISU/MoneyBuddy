import java.io.File;


public class helpers {
    /**
     * List the databases already associated with the active user
     * @return the databases already associated with the active user
     */
    public static String[] dbList() {
        File folder = new File(System.getProperty("user.home") + "/.MoneyBuddy/");
        String[] filenames = folder.list();

        /* Skim the file extensions off the filenames */
        for (int i = 0; i < filenames.length; i++) {
            filenames[i] = filenames[i].substring(0, filenames[i].lastIndexOf('.'));
        }

        return filenames;
    }
}
