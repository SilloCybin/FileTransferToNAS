import java.io.File;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.sql.*;

public class DBOperations {

    private static String URL;
    private static final String username = "root";
    private static final String password = "81-C4l1f0rn14";

    public List<File> dbCheck(List<File> fileList, String cloudAddress) throws Exception {

        if (cloudAddress.equals("10.8.0.1")){
            URL = "jdbc:mysql://10.8.0.1:3306/Cloud";
        }
        else {
            URL = "jdbc:mysql://192.168.1.81:3306/Cloud";
        }

        List<File> checkedFileList = new ArrayList<>();

        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection dbCon = DriverManager.getConnection(URL, username, password);
        Statement dbSt = dbCon.createStatement();

        for(File file: fileList){

            BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);

            String dbQuery = " SELECT COUNT(*) AS total FROM files WHERE name = '" + file.getName() +
                                "' AND creation_time = '" + attr.creationTime().toString() +
                                "' AND last_modified = '" + attr.lastModifiedTime().toString() + "'" ;

            ResultSet dbResult = dbSt.executeQuery(dbQuery);
            dbResult.next();
            int count = dbResult.getInt("total");

            if(count == 0){

                checkedFileList.add(file);

                dbQuery = " SELECT COUNT(*) AS total2 FROM files WHERE name = '" + file.getName() + "'" ;
                dbResult = dbSt.executeQuery(dbQuery);
                dbResult.next();
                int count2 = dbResult.getInt("total2");

                if(count2 > 0){
                    dbQuery = "DELETE FROM files WHERE name = '" + file.getName() + "'" ;

                    dbSt.executeUpdate(dbQuery);
                    System.out.println("File " + file.getName() + " is a newer version of a previously loaded file. It will be updated in the database and replace the former version in the NAS.");

                } else {
                    System.out.println("File " + file.getName() + "'s info is ready to get loaded into the database");
                }

            } else {
                System.out.println("Duplicate of file " + file.getName() + " was found in the database. It won't be sent to the NAS.");
            }
        }

        dbSt.close();
        dbCon.close();

        return checkedFileList;
    }

    public void addFilesInfoToDB(List<File> checkedFileList) throws Exception {

        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection dbCon = DriverManager.getConnection(URL, username, password);
        Statement dbSt = dbCon.createStatement();

        for(File file: checkedFileList){

            BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);

            String dbQuery = "INSERT INTO files (name, creation_time, last_modified) VALUES ('" + file.getName() + "', '"
                                                                                            + attr.creationTime().toString() + "', '"
                                                                                            + attr.lastModifiedTime() + "');";
            dbSt.executeUpdate(dbQuery);
            System.out.println("File " + file.getName() + "'s info was added to the database");
        }

        dbSt.close();
        dbCon.close();
    }

}
