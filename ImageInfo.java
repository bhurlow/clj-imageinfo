/*
 * Taken from kickjava.com/src/imageinfo/ImageInfo.java.htm
 *
 * Original copyright:
 *
 * A Java class to determine image width, height and color depth for
 * a number of image file formats.
 *
 * Written by Marco Schmidt 
 * <http://www.geocities.com/marcoschmidt.geo/contact.html>.
 *
 * Contributed to the Public Domain.
 *
 * Last modification 2003-07-28
 * Read more: http://kickjava.com/src/imageinfo/ImageInfo.java.htm
 */
package imageinfo;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;


/**
 *
 * @author achoy
 */
public class ImageInfo {

    public static final int FORMAT_GIF = 1;
    
    private int width;
    private int height;
    private int bitsPerPixel;
    private int format;
    private InputStream  in;
    private boolean collectComments = false;
    private boolean determineNumberOfImages;
    private int numberOfImages;
    private int tryCheckImages;

    public ImageInfo()
    {
        tryCheckImages = 5;
    }
    
    public boolean check() {
        format = -1;
        width = -1;
        height = -1;
        bitsPerPixel = -1;
         
        try
        {
            int b1 = read() & 0xff;
            int b2 = read() & 0xff;
            if (b1 == 0x47 && b2 == 0x49) {
                return checkGif();
            }
            return false;
        }
        catch (IOException ioe)
        {
            return false;
        }
    }
    
    private boolean checkGif() throws IOException 
    {
        final byte[] GIF_MAGIC_87A = {0x46, 0x38, 0x37, 0x61};
        final byte[] GIF_MAGIC_89A = {0x46, 0x38, 0x39, 0x61};
        byte[] a = new byte[11]; // 4 from the GIF signature + 7 from the global header
        if (read(a) != 11) {
            return false;
        }
        if ((!equals(a, 0, GIF_MAGIC_89A, 0, 4))
                && (!equals(a, 0, GIF_MAGIC_87A, 0, 4))) {
            return false;
        }
        format = FORMAT_GIF;
        width = getShortLittleEndian(a, 4);
        height = getShortLittleEndian(a, 6);
        int flags = a[8] & 0xff;
        bitsPerPixel = ((flags >> 4) & 0x07) + 1;
        if (!determineNumberOfImages) {
            return true;
        }
        // skip global color palette
        if ((flags & 0x80) != 0) {
            int tableSize = (1 << ((flags & 7) + 1)) * 3;
            skip(tableSize);
        }
        numberOfImages = 0;
        int blockType;
        int lWidth, lHeight;
        do {
            blockType = read();
            switch (blockType) {
                case (0x2c): // image separator
                {
                        
                    if (read(a, 0, 9) != 9) {
                        return false;
                    }
                    lWidth = getShortLittleEndian(a, 4);
                    lHeight = getShortLittleEndian(a, 6);                  
                    System.out.println("-----");
                    System.out.println("LWIDTH" + lWidth);
                    System.out.println("LHEITGHT" + lHeight);
                    if (lWidth != width) width = lWidth;
                    if (lHeight != height) height = lHeight;    
                    flags = a[8] & 0xff;
                    int localBitsPerPixel = (flags & 0x07) + 1;
                    if (localBitsPerPixel > bitsPerPixel) {
                        bitsPerPixel = localBitsPerPixel;
                    }
                    if ((flags & 0x80) != 0) {
                        skip((1 << localBitsPerPixel) * 3);
                    }
                    skip(1); // initial code length
                    int n;
                    do {
                        n = read();
                        if (n > 0) {
                            skip(n);
                        } else if (n == -1) {
                            return false;
                        }
                    } while (n > 0);
                    numberOfImages++;
                    
                    // try up to tryCheckImages images
                    if (tryCheckImages > 0 && numberOfImages >= tryCheckImages)
                        return true;

                    break;
                }
                case (0x21): // extension
                {
                    int extensionType = read();
                    if (collectComments && extensionType == 0xfe) {
                        StringBuffer sb = new StringBuffer();
                        int n;
                        do {
                            n = read();
                            if (n == -1) {
                                return false;
                            }
                            if (n > 0) {
                                for (int i = 0; i < n; i++) {
                                    int ch = read();
                                    if (ch == -1) {
                                        return false;
                                    }
                                    sb.append((char) ch);
                                }
                            }
                        } while (n > 0);
                    } else {
                        int n;
                        do {
                            n = read();
                            if (n > 0) {
                                skip(n);
                            } else if (n == -1) {
                                return false;
                            }
                        } while (n > 0);
                    }
                    break;
                }
                case (0x3b): // end of file
                {
                    break;
                }
                default: {
                    return false;
                }
            }            
        } while (blockType != 0x3b);
        return true;
    }
    
    private boolean equals(byte[] a1, int offs1, byte[] a2, int offs2, int num) {
        while (num-- > 0) {
            if (a1[offs1++] != a2[offs2++]) {
                return false;
            }
        }
        return true;
    }

    private int getShortBigEndian(byte[] a, int offs) {
         return
             (a[offs] & 0xff) << 8 | 
             (a[offs + 1] & 0xff);
    }

    private int getShortLittleEndian(byte[] a, int offs) {
        return (a[offs] & 0xff) | (a[offs + 1] & 0xff) << 8;     
    }

    private int read() throws IOException
    {
        if (in != null) return in.read();
        return -1;
    }
    
    private int read(byte[] a) throws IOException
    {
        if (in != null) return in.read(a);
        return -1;
    }
    
    private int read(byte[] a, int offset, int num) throws IOException
    {
        if (in != null) return in.read(a, offset, num);
        return -1;
    }
    
    private void skip(int num) throws IOException {
        while (num > 0) {
            long result = 0;
            if (in != null) {
                result = in.skip(num);
            } else {
                //result = din.skipBytes(num);
            }
            if (result > 0) {
                num -= result;
            }
        }
    }
    
    public void setInput(InputStream inStream) {
        in = inStream;
    }
    
    public void printVerbose(String sourceName)
    {
        System.out.println(
                "SourceName=" + sourceName +
                "; Width=" + width + 
                "; Height=" + height + 
                "; BitsPerPixel=" + bitsPerPixel
        );
    }
    
    public void processImage(String sourceName, InputStream in)
    {
        determineNumberOfImages = true;
        setInput(in);
        if (!check())
        {
            System.out.println("Invalid GIF image");
            return;
        }
        printVerbose(sourceName);
    }
    
    /**
     * @param args the command line arguments
     * 
     * Example command line:  x.gif https://website/image.gif
     */

    public static void main(String[] args) {
        
        args[0] = "tiny.gif";
        // args[0] = "keyboard.gif";
        ImageInfo imageInfo = new ImageInfo();
        if (args.length > 0)
        {
            int index = 0;
            while (index < args.length)
            {
                String name = args[index++];
                InputStream in = null;
                System.out.println("STARTING WITH YOOOO: >>" + name);
                try
                {
                    if (name.startsWith("http://") || name.startsWith("https://"))
                        in = new URL(name).openConnection().getInputStream();
                    else
                        in = new FileInputStream(name);
                    
                    imageInfo.processImage(name, in);
                }
               catch (Exception e)
                {
                    System.out.println("Error reading file: " + e.getMessage());
                }
                finally
                {
                    try
                    {
                        if (in != null)
                            in.close();
                    }
                    catch (Exception e) {}
                }
            }
        }
    }
    
}
