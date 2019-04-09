package leon.android.easycards.utils;

import android.graphics.Bitmap;

public interface OnPhotoReceivedListener {
    public void getBitmapImage(Bitmap bitmap);

    public void getImagePath(String imagePath);
}
