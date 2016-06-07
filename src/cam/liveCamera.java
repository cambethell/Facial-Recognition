 
 
package cam;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;

/**
 * Construct a live camera
 * @param
 */
public class liveCamera {

    private Webcam webcam;
   
    /**
	 * Constructor for liveCamera
	 * @param
	 */
    public liveCamera() {
    	webcam = Webcam.getDefault();
        webcam.setViewSize(new Dimension(640, 480));
        webcam.open();
        
    }
       
    /**
	 * Grab an image from the webcam
	 * @param
	 */
    public BufferedImage getWebcamImage() {
        if (webcam.isOpen()) {
        	return webcam.getImage();
        }
        return new BufferedImage(640, 480, BufferedImage.TYPE_INT_ARGB);
    }
}
