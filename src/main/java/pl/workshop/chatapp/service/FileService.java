package pl.workshop.chatapp.service;

import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils; // dodaj dependency commons-io jeśli nie ma

@Service
public class FileService {

    public void deleteRoomFiles(Long roomId) {
        File roomDir = new File("./uploads/room-" + roomId);
        if (roomDir.exists() && roomDir.isDirectory()) {
            try {
                FileUtils.deleteDirectory(roomDir);
            } catch (IOException e) {
                // log error
            }
        }
    }

    public void deleteFile(String fileUrl) {
        // opcjonalnie
    }
}
