package expo.modules.medialibrary;

import android.content.ContentValues;
import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.io.IOException;
import java.net.URLConnection;

import org.unimodules.core.Promise;

import static expo.modules.medialibrary.MediaLibraryConstants.ERROR_IO_EXCEPTION;
import static expo.modules.medialibrary.MediaLibraryConstants.ERROR_UNABLE_TO_LOAD_PERMISSION;
import static expo.modules.medialibrary.MediaLibraryConstants.ERROR_UNABLE_TO_SAVE;
import static expo.modules.medialibrary.MediaLibraryConstants.EXTERNAL_CONTENT;
import static expo.modules.medialibrary.MediaLibraryUtils.queryAssetInfo;
import static expo.modules.medialibrary.MediaLibraryUtils.safeCopyFile;

class CreateAsset extends AsyncTask<Void, Void, Void> {
  private final Context mContext;
  private final Uri mUri;
  private final Promise mPromise;

  CreateAsset(Context context, String uri, Promise promise) {
    mContext = context;
    mUri = Uri.parse(uri);
    mPromise = promise;
  }

  private File createAssetFile() throws IOException {
    File localFile = new File(mUri.getPath());
    File destDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
    File destFile = safeCopyFile(localFile, destDir);

    if (!destDir.exists() || !destFile.isFile()) {
      mPromise.reject(ERROR_UNABLE_TO_SAVE, "Could not create asset record. Related file is not existing.");
      return null;
    }
    return destFile;
  }

  @Override
  protected Void doInBackground(Void... params) {
    try {
      File asset = createAssetFile();
      if (asset == null) {
        return null;
      }

      MediaScannerConnection.scanFile(mContext,
          new String[]{asset.getPath()},
          null,

          new MediaScannerConnection.OnScanCompletedListener() {
            @Override
            public void onScanCompleted(String path, Uri uri) {
              if (uri == null) {
                mPromise.reject(ERROR_UNABLE_TO_SAVE, "Could not add image to gallery.");
                return;
              }
              String mimeType = URLConnection.guessContentTypeFromName(path);

              if (mimeType != null && mimeType.startsWith("video")) {
                updateVideoAttributes(path);
              }

              final String selection = MediaStore.Images.Media.DATA + "=?";
              final String[] args = {path};
              queryAssetInfo(mContext, selection, args, false, mPromise);
            }
          });
    } catch (IOException e) {
      mPromise.reject(ERROR_IO_EXCEPTION, "Unable to copy file into external storage.", e);
    } catch (SecurityException e) {
      mPromise.reject(ERROR_UNABLE_TO_LOAD_PERMISSION,
          "Could not get asset: need READ_EXTERNAL_STORAGE permission.", e);
    }
    return null;
  }

  private void updateVideoAttributes(String path) {
    ContentValues contentValues = new ContentValues();
    MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();

    metadataRetriever.setDataSource(path);
    String width = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
    String height = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
    String duration = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

    contentValues.put(MediaStore.Video.VideoColumns.WIDTH, width);
    contentValues.put(MediaStore.Video.VideoColumns.HEIGHT, height);
    contentValues.put(MediaStore.Video.VideoColumns.DURATION, duration);

    mContext.getContentResolver().update(EXTERNAL_CONTENT, contentValues, MediaStore.MediaColumns.DATA + "=?", new String[]{path});
  }
}
