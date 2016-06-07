package cam;

import facerecog.FaceRecognition;
import gui.AverageFace;
import gui.EigenFaces;
import gui.FaceLibrary;
import gui.menu.MainMenu;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.activation.MimetypesFileTypeMap;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;

public class FaceRecog extends JFrame
{
	public static final int WIDTH = 1000;
	
	public static final int HEIGHT = 650;

	public static final String LIBRARY_PATH = "./sample/";
	
	/**
	 * The main.
	 * @param args
	 */
	public static void main(String[] args)
	{
		final FaceRecog faceRecog = new FaceRecog();
		
		faceRecog.setVisible(true);
	}
	
	/**
	 * Grab all the images
	 * @param the folder name
	 */
	public ArrayList<BufferedImage> getImages(final String folder)
	{
		final File dir = new File(folder);

		final ArrayList<BufferedImage> images = new ArrayList<BufferedImage>();
		
		if (dir.exists() && dir.isDirectory())
		{
			File[] files = dir.listFiles(); //get all the files in the folder
			
			for (File file: files)
			{
				if (isImage(file)) //if it is an image add to the arraylist
				{
					images.add(getImage(file));
				}
			}
		}
		
		return images;
	}
	
	/**
	 * get an image from the file
	 * @param filename
	 */
	public BufferedImage getImage(final File file)
	{
		BufferedImage img = null;
		try 
		{
			img = ImageIO.read(file);
		} catch (IOException e) { }
		
		return img;
	}
	
	/**
	 * check if its a valid image
	 * @param a file name
	 */
	public boolean isImage(final File file)
	{
		MimetypesFileTypeMap fileTypeMap = new MimetypesFileTypeMap();
		fileTypeMap.addMimeTypes("image png tif jpg jpeg bmp");
		
		String mimetype = fileTypeMap.getContentType(file);
		
        String type = mimetype.split("/")[0];
        
        if(type.equals("image"))
        {
            return true;     
        }
        
        return false;
	}
	
	/**
	 * The Constructor.
	 */
	public FaceRecog()
	{
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(WIDTH, HEIGHT);
		setLocationRelativeTo(null);
		
		final FaceRecognition faceProcessor = new FaceRecognition();
		
		JTabbedPane pane = new JTabbedPane();
		

		final camFacial camFace = new camFacial(faceProcessor);
			
		pane.add("Facial Recognition", camFace);
		
		final MainMenu menu = new MainMenu(faceProcessor, pane);		
		setJMenuBar(menu);
		
		setContentPane(pane);
	}
}
