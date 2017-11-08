package newbilius.com.online_comics_reader.Tools;

import android.content.Context;

import java.io.File;

import newbilius.com.online_comics_reader.SimpleComicsReaderApplication;

public class PicassoCacheHelper {
    public static long getSize() {
        Context context = SimpleComicsReaderApplication.getAppContext();
        long size = 0;
        try {
            File cache = new File(context.getCacheDir(), "picasso-cache");
            size = dirSize(cache);
        } catch (Exception ignored) {

        }
        return size;
    }

    public static void deleteCache() {
        Context context = SimpleComicsReaderApplication.getAppContext();
        try {
            File cache = new File(context.getApplicationContext().getCacheDir(), "picasso-cache");
            deleteFilesRecursive(cache);
        } catch (Exception ignored) {

        }
    }

    private static long dirSize(File dir) {
        if (dir.exists()) {
            long result = 0;
            File[] fileList = dir.listFiles();
            for (File aFileList : fileList) {
                if (aFileList.isDirectory()) {
                    result += dirSize(aFileList);
                } else {
                    result += aFileList.length();
                }
            }
            return result; // return the file size
        }
        return 0;
    }

    private static void deleteFilesRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteFilesRecursive(child);
        fileOrDirectory.delete();
    }
}
