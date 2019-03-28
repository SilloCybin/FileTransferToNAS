import java.io.*;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class SendToCloud {

    private static String HOST_ADDR;
    private static final String USERNAME = "";
    private static final String PASSWORD = "";

    public static void main(String[] args) {

        if (!isConnectedToVPN() && !isConnectedToLAN() ){

            System.out.println("This computer can't establish a secure connection to x.x.x.x. . A secure connection must have been established in order to transfer desired files to the NAS");

        } else {

            System.out.println("Secure connection to x.x.x.x ("+HOST_ADDR+") has been checked. Proceeding to file listing.");

            File folder = new File("/Users/cedricdevries/Desktop/NAS");
            DBOperations dbOps = new DBOperations();
            FTPOperations ftpOps;

            ListFiles listFiles = new ListFiles();
            List<File> fileList = listFiles.listAllFiles(folder);
            List<File> checkedFileList = new ArrayList<>();

            if (!fileList.isEmpty()) {

                System.out.println("Files in local storage folder listed. Proceeding to database checking.");

                try {
                    checkedFileList = dbOps.dbCheck(fileList, HOST_ADDR);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (!checkedFileList.isEmpty()) {

                    try {
                        dbOps.addFilesInfoToDB(checkedFileList);
                        System.out.println("Files info sent to DB. Proceeding to file transfer.");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    try {
                        ftpOps = new FTPOperations(HOST_ADDR, USERNAME, PASSWORD);
                        ftpOps.uploadFileList(checkedFileList);
                        System.out.println("Files sent to NAS");
                        ftpOps.disconnect();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                listFiles.deleteAllFiles(folder);
                System.out.println("Files deleted from local storage folder");

            } else {
                System.out.println("No files to be sent");
            }
        }
    }

    private static Boolean isConnectedToVPN(){

        Boolean connectedToVPN = false;
        Enumeration netIntEnum = null;

        try {
            netIntEnum = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e1) {
            e1.printStackTrace();
        }

        while (netIntEnum.hasMoreElements()) {

            NetworkInterface n = (NetworkInterface) netIntEnum.nextElement();
            Enumeration netAddrEnum = n.getInetAddresses();

            while (netAddrEnum.hasMoreElements() && !connectedToVPN) {

                String test = netAddrEnum.nextElement().toString();

                if (test.substring(0, 8).equals("/10.8.0.")){
                    System.out.println("Found NAS in VPN network");
                    HOST_ADDR = "10.8.0.1";
                    connectedToVPN = true;
                }
            }
        }

        return connectedToVPN;
    }

    private static Boolean isConnectedToLAN(){

        Boolean connectedToLocalNetwork = false;
        Boolean gotaresponse = false ;
        ArrayList<String> pingResultLines = new ArrayList<>();
        String inputLine;
        String cmd = "ping -c 2 x.x.x.x";
        int k = 0;

        try {

            Runtime r = Runtime.getRuntime();
            Process p = r.exec(cmd);
            InputStream is = p.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);

            while ((inputLine=br.readLine()) != null && k <= 2) {

                pingResultLines.add(inputLine);
                k++;
            }

            br.close();

        } catch (IOException e) {
            System.out.println(e);
        }

        if (!pingResultLines.isEmpty()) {

            for (int j = 1; j < 3; j++) {

                if (pingResultLines.get(j).substring(0, 1).equals("6") && !gotaresponse) {

                    System.out.println("Found NAS in local network");
                    HOST_ADDR = "192.168.1.81";
                    gotaresponse = true;
                    connectedToLocalNetwork = true;
                }
            }
        }

        return connectedToLocalNetwork;
    }
}
