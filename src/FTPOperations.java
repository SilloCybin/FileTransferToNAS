import java.io.PrintWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

public class FTPOperations {

    private FTPClient ftp = new FTPClient();
    private static final String CLOUD_ROOT_DIR = "/home/Cedric/media/Other/";

    public FTPOperations(String host, String user, String pwd) throws Exception{

        int reply;

        ftp.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
        ftp.connect(host);
        reply = ftp.getReplyCode();

        if (!FTPReply.isPositiveCompletion(reply)) {

            ftp.disconnect();
            throw new Exception("Exception in connecting to FTP Server");

        } else {

            ftp.login(user, pwd);
            ftp.setFileType(FTP.BINARY_FILE_TYPE);
            ftp.enterLocalPassiveMode();
        }
    }

    public void uploadFileList(List<File> checkedFileList) throws Exception {

        for(File file: checkedFileList) {

            String filename = file.getName();
            String localFileFullName = file.toPath().toString();
            String servDir = CLOUD_ROOT_DIR + localFileFullName.substring(33, (localFileFullName.length()-filename.length()));

            try (InputStream input = new FileInputStream(new File(localFileFullName))) {

                if(!this.ftp.changeWorkingDirectory(servDir)){
                    this.ftp.makeDirectory(servDir);
                }

                this.ftp.changeWorkingDirectory(servDir);
                this.ftp.storeFile(filename, input);
            }
        }
    }

    public void disconnect(){
        if (this.ftp.isConnected()) {
            try {
                this.ftp.logout();
                this.ftp.disconnect();
            } catch (IOException f) {

            }
        }
    }
}