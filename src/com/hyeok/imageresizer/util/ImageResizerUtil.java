package com.hyeok.imageresizer.util;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Created by GwonHyeok on 15. 2. 8..
 */
public class ImageResizerUtil {
    private static ImageResizerUtil instance;

    private ImageResizerUtil() {

    }

    public synchronized static ImageResizerUtil getInstance() {
        if (instance == null) {
            instance = new ImageResizerUtil();
        }
        return instance;
    }

    public BufferedImage createResizedCopy(Image originalImage,
                                           int scaledWidth, int scaledHeight,
                                           boolean preserveAlpha) {
        int imageType = preserveAlpha ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
        BufferedImage scaledBI = new BufferedImage(scaledWidth, scaledHeight, imageType);
        Graphics2D g = scaledBI.createGraphics();
        if (preserveAlpha) {
            g.setComposite(AlphaComposite.Src);
        }
        g.drawImage(originalImage, 0, 0, scaledWidth, scaledHeight, null);
        g.dispose();
        return scaledBI;
    }
}
