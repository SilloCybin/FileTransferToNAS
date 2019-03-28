import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ListFiles {

    private List<File> fileList = new ArrayList<>();

    public List<File> listAllFiles(File folder) {

        File[] fileArray = folder.listFiles();

        for (File file : fileArray) {
            if (file.isDirectory()) {
                listAllFiles(file);
            } else {
                if(!file.isHidden()) {
                    fileList.add(file);
                }
            }
        }
        return fileList;
    }

    // Method to be called when program is done with sending files
    public void deleteAllFiles(File folder) {

        File[] fileArray = folder.listFiles();;

        for (File file : fileArray) {
            if (file.isDirectory()) {
                deleteAllFiles(file);
            } else {
                if(!file.isHidden()) {
                    file.delete();
                }
            }
        }
    }
}


