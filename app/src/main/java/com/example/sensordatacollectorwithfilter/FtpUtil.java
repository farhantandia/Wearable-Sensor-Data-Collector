package com.example.sensordatacollectorwithfilter;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.IOException;
import java.io.InputStream;

public final class FtpUtil {
    public static String uploadFile(String url,
                                     int port,
                                     String username,
                                     String password,
                                     String filename,
                                     String path,
                                     InputStream input ){
        String result = "false";
        FTPClient ftp = new FTPClient();
        ftp.setControlEncoding("GBK");
        try {
            int reply;
            ftp.connect(url, port);
            ftp.login(username, password);

            reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect();
                return "login failed";
            }

            ftp.setRemoteVerificationEnabled(false);
            ftp.enterLocalPassiveMode();
            ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
            ftp.setBufferSize(1024);
            ftp.setControlEncoding("UTF-8");
            ftp.changeWorkingDirectory(path);
            ftp.storeFile(filename, input);
            input.close();
            ftp.logout();

            result = "true";
        } catch (IOException e) {
            result = e.getMessage() + e.toString();
        } finally {
            if (ftp.isConnected()) {
                try {
                    ftp.disconnect();
                } catch (IOException ioe) {
                }
            }
        }
        return result;
    }
}
