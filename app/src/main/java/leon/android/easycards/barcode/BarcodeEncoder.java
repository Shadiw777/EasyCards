package leon.android.easycards.barcode;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;

/**
 * Created by prongbang on 7/15/2016.
 */
public class BarcodeEncoder {

    public static Bitmap genBarcode128(String code, int width, int height) {
        Bitmap bmp = null;
        try {
            BitMatrix bitMatrix = new Code128Writer().encode(code, BarcodeFormat.CODE_128, width, height, null);
            bmp = BarcodeEncoder.toBitmap(bitMatrix);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return bmp;
    }

    private static Bitmap toBitmap(BitMatrix bitMatrix) {
        int height = bitMatrix.getHeight();
        int width = bitMatrix.getWidth();
        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
            }
        }
        return bmp;
    }

}
