package cam;

import java.awt.Color;
import cam.GaussianFilter;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.SortedMap;
import java.awt.Point;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;

import facerecog.FaceImage;
import facerecog.FaceRecognition;
import facerecog.Tools;

public class camFacial extends JPanel
{
	FaceRecognition faceRecog;
	BufferedImage webcamDisplay;
	BufferedImage faceFound;
	GaussianFilter filter = new GaussianFilter(7);
	boolean faceWasFound;
	Point oldTopY;
	int oldWidth;
	
	//this is the constructor of the camFacial class that displays the output
	//starts a thread to run the webcam and check it for faces
	//parameters: the facerecognition object
	camFacial(FaceRecognition fr) {
		
		faceRecog = fr;
		faceWasFound = false;
		
		Runnable run = new Runnable() {
		    public void run() {
		    	liveCamera livecam = new liveCamera();
		    	int i = 0;
		    	BufferedImage img1 = new BufferedImage(640, 480, BufferedImage.TYPE_INT_ARGB);
		    	BufferedImage img2 = new BufferedImage(640, 480, BufferedImage.TYPE_INT_ARGB);
		    	while(true) {
		    		if(i==8) {
		    			img1 = livecam.getWebcamImage();
		    		} else if (i==10) {
		    			img2 = livecam.getWebcamImage();
		    			i=-1;
		    			
		    			ArrayList<BufferedImage> theseFaces = findDiff(img1, img2);
			    		getGraphics().drawImage(theseFaces.get(0), 0, 0, null);
			    		getGraphics().drawImage(theseFaces.get(1), theseFaces.get(0).getWidth(), 0, null);
			    		getGraphics().drawImage(theseFaces.get(2), theseFaces.get(0).getWidth(), theseFaces.get(1).getHeight(), null);
		    		}
		    		i++;
		    	}
		    }
		 };
		 new Thread(run).start();
	}
	
	//this function gets called by the drawFaceBox function to test the 3 best sizes of the face
	//parameters: the variable headwidth, the top motion pixel, and the webcam frame
	SortedMap<Double, FaceImage> getFaceAtSize(int headwidth, Point topY, BufferedImage frame) {
		BufferedImage boxedFace = new BufferedImage(headwidth, (int)(headwidth*1.34), BufferedImage.TYPE_INT_ARGB);
		
		int xmin = topY.x-headwidth/2;
		if(xmin < 0) xmin = 0;
		
		int xmax = topY.x+headwidth/2;
		if(xmax > (frame.getWidth()-1)) xmax = frame.getWidth()-1;
		
		int ymax = topY.y;
		if(ymax < 0) ymax = 0;
		
		int ymin = topY.y+boxedFace.getHeight()-1;
		if(ymin > frame.getHeight()-1) ymin = frame.getHeight()-1;
		
		for(int x = xmin; x < xmax; x++) {
			for(int y = ymax; y < ymin; y++){
				boxedFace.setRGB(x - xmin, y - ymax, frame.getRGB(x,y));
			}
		}
		
		BufferedImage scaledFace = scaleTo256(boxedFace, 256, 256);
		
		BufferedImage filterImgBox = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
		filter.filter(scaledFace, filterImgBox);

		return faceRecog.compare(new FaceImage(filterImgBox, ""));
	}
	
	//function that draws the best matched frame from the 3 scales
	//parameters: the headwidth determined, the top pixel, and the webcam frame
	BufferedImage getBestFaceOnScreen(int headwidth, Point topY, BufferedImage frame) {
		BufferedImage boxedFace = new BufferedImage(headwidth, (int)(headwidth*1.34), BufferedImage.TYPE_INT_ARGB);
		
		int xmin = topY.x-headwidth/2;
		if(xmin < 0) xmin = 0;
		
		int xmax = topY.x+headwidth/2;
		if(xmax > (frame.getWidth()-1)) xmax = frame.getWidth()-1;
		
		int ymax = topY.y;
		if(ymax < 0) ymax = 0;
		
		int ymin = topY.y+(int)(headwidth*1.34);
		if(ymin > frame.getHeight()-1) ymin = frame.getHeight()-1;
		
		for(int x = xmin; x < xmax; x++) {
			for(int y = ymax; y < ymin; y++){
				boxedFace.setRGB(x - xmin, y - ymax, frame.getRGB(x,y));
			}
		}
		
		BufferedImage scaledFace = scaleTo256(boxedFace, 256, 256);
		
		BufferedImage filterImgBox = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
		filter.filter(scaledFace, filterImgBox);

		return filterImgBox;
	}
	
	//this function is called by findFaceInImage to determine the best match and draw the red box around
	//the face.
	//parameters: the initial headwidth found, the top motion vector pixel, and the webcam frame
	ArrayList<BufferedImage> drawFaceBox(int headwidth, Point topY, BufferedImage frame) {
		ArrayList<BufferedImage> threeFacesToDisplay = new ArrayList();
		if(topY.y > 400) {
			topY = oldTopY;
		} else {
			oldTopY = topY;
		}
		
		//create 3 possible matches
		if(headwidth < 100) headwidth = 100;
		SortedMap<Double, FaceImage> match1 = getFaceAtSize(headwidth, topY, frame);
		SortedMap<Double, FaceImage> match2 = getFaceAtSize((int)(headwidth*0.9), topY, frame);
		SortedMap<Double, FaceImage> match3 = getFaceAtSize((int)(headwidth*1.1), topY, frame);
		
		//determine best match
		double bestMatchVal = 0;
		BufferedImage bestMatchImg = new BufferedImage(1,1,BufferedImage.TYPE_INT_ARGB);
		int bestHeadwidth = 0;
		
		if(match1.entrySet().iterator().next().getKey() > bestMatchVal) {
			bestMatchVal = match1.entrySet().iterator().next().getKey();
			bestMatchImg = match1.entrySet().iterator().next().getValue();
			bestHeadwidth = headwidth;
		}
		if(match2.entrySet().iterator().next().getKey() > bestMatchVal) {
			bestMatchVal = match2.entrySet().iterator().next().getKey();	
			bestMatchImg = match1.entrySet().iterator().next().getValue();
			bestHeadwidth = (int)(headwidth*0.9);
		}
		if(match3.entrySet().iterator().next().getKey() > bestMatchVal) {
			bestMatchVal = match3.entrySet().iterator().next().getKey();
			bestMatchImg = match1.entrySet().iterator().next().getValue();
			bestHeadwidth = (int)(headwidth*1.1);
		}
		
		if(bestMatchVal > 7000) {
			faceWasFound = true;
		} else {
			faceWasFound = false;
		}
		BufferedImage myFaceMatch = getBestFaceOnScreen(bestHeadwidth, topY, frame);
		//draw box around face
		int xmin = topY.x-bestHeadwidth/2;
		if(xmin < 0) xmin = 0;
		
		int xmax = topY.x+bestHeadwidth/2;
		if(xmax > (frame.getWidth()-1)) xmax = frame.getWidth()-1;
		
		int ymax = topY.y;
		if(ymax < 0) ymax = 0;
		
		int ymin = topY.y+(int)(headwidth*1.34);
		if(ymin > frame.getHeight()-1) ymin = frame.getHeight()-1;

		for(int x = xmin; x < xmax; x++) {
			frame.setRGB(x, topY.y, 16711680);
			frame.setRGB(x, ymin, 16711680);
		}
		for(int y = ymax; y < ymin; y++){
			frame.setRGB(xmax, y, 16711680);
			frame.setRGB(xmin, y, 16711680);
		}
		threeFacesToDisplay.add(frame);
		
		//check the small face vs database, get back list of 3 best matches
		//add best match to threeFacesToDisplay
		threeFacesToDisplay.add(myFaceMatch);
		threeFacesToDisplay.add(bestMatchImg);

		return threeFacesToDisplay;
	}
	
	//this function will scale the face found to the appropriate size
	//parameters: takes in the face found, and the width and height to scale to.
	BufferedImage scaleTo256(BufferedImage src, int w, int h) {
	  BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
	  int x, y;
	  int ww = src.getWidth();
	  int hh = src.getHeight();
	  for (x = 0; x < w; x++) {
		  for (y = 0; y < h; y++) {
			  int col = src.getRGB(x * ww / w, y * hh / h);
			  img.setRGB(x, y, col);
		  }
	  }
	  return img;
	}
	
	//this function is called by findDiff to find the location of differences between frames on screen
	//parameters: the original frame to draw the box on determined by the other difference parameter differences
	ArrayList<BufferedImage> findFaceInImage(BufferedImage original, BufferedImage difference) {
		Point topleftmostY;
		Point toprightmostY;
		Point topmostY;

		topleftmostY = new Point(10000, 10000);
		toprightmostY = new Point(0, 10000);
		int headleft = 1000;
		int headright = 0;
		int headwidth = 0;
		int res = 0;
		if(!faceWasFound) {
			int y = 0;
			while(res <= 200) {
				for(int x = 0; x < difference.getWidth() - 1; x++) {
					Color c = new Color(difference.getRGB(x, y));
					res = (int)(16.0 + 0.257* c.getRed() + 0.504*c.getGreen() + 0.0988*c.getBlue());
					if(res > 200) {
						if(y<topleftmostY.y) {
							topleftmostY.x = x;
							topleftmostY.y = y;
						}
						if(y<=toprightmostY.y && x>toprightmostY.x) {
							toprightmostY.x = x;
							toprightmostY.y = y;
						}
						break;
					}
				}
				y++;
				if(y>=difference.getHeight()-1) {
					break;
				}
			}
			y+=100;
			if(y>difference.getHeight()-1) {
				y = difference.getHeight()-1;
			}
			res=0;
			for(int x = 0; x < difference.getWidth() - 1; x++) {
				Color c = new Color(difference.getRGB(x, y));
				res = (int)(16.0 + 0.257* c.getRed() + 0.504*c.getGreen() + 0.0988*c.getBlue());
				if(res > 100) {
					if(x<headleft) {
						headleft = x;
					}
					if(x>=headleft && x>headright) {
						headright = x;
					}
				}
			}
			topmostY = new Point((topleftmostY.x + toprightmostY.x)/2, topleftmostY.y);
			headwidth = (int)((headright - headleft)*1.4);
		} else {
			topmostY = oldTopY;
			headwidth = oldWidth;
		}
		//now we have leftmost x, rightmost x, and topmost y in the difference frame
		
		if(headwidth <=0) headwidth = 100;
		if(headwidth > 639) headwidth = 639;
		return drawFaceBox(headwidth, topmostY, original);
	}
	
	//the findDiff function called by the main camFacial object as run in a thread.
	//parameters: the two frames to determine the differences, analyze, and draw on
	ArrayList<BufferedImage> findDiff(BufferedImage img1, BufferedImage img2) {
		ArrayList<int[][]> luma = new ArrayList();
		ArrayList<BufferedImage> theseImages= new ArrayList();
		theseImages.add(img1);
		theseImages.add(img2);
		
		//take all images and turn to luma
		for(BufferedImage colourImage: theseImages) {
			int[][] result = new int[colourImage.getWidth()][];
			//fill in each column
			for(int x = 0; x < colourImage.getWidth() - 1; x++) {
				int[] column = new int[colourImage.getHeight()];
				
				for(int y = 0; y < colourImage.getHeight() - 1; y++) {
					Color c = new Color(colourImage.getRGB(x, y));
					column[y] = (int)(16.0 + 0.257* c.getRed() + 0.504*c.getGreen() + 0.0988*c.getBlue());
				}
				result[x] = column;
			}
			luma.add(result);
		}
		
		//find differences between adjacent luma images and put in new array
		int[][] theDiffs = new int[img1.getWidth()][img1.getHeight()];
		
		for(int i = 0; i < luma.size() - 1; i++) {
			int[][] first = luma.get(i);
			int[][] second = luma.get(i+1);
			for(int x = 0; x < first.length-1; x++) {
				for(int y = 0; y < first[0].length-1; y++){
					//find absolute difference; if its big enough, make it BLACK AND WHITE!
					first[x][y] = Math.abs(first[x][y] - second[x][y]);
					if(first[x][y]>25) first[x][y] = 255;
					else {
						first[x][y] = 0;
					}
				}
			}
			theDiffs = first;
		}
		
		//convert back to bufferedimage and return that
		BufferedImage diffImage;

		BufferedImage bufferedImage = new BufferedImage(theDiffs.length, theDiffs[0].length, BufferedImage.TYPE_INT_RGB);
		//need loop to set each pixel
		for(int x = 0; x < theDiffs.length-1; x++) {
			for(int y = 0; y < theDiffs[0].length-1; y++){
				bufferedImage.setRGB(x, y, new Color(theDiffs[x][y], theDiffs[x][y], theDiffs[x][y]).getRGB() );
			}
		}
		diffImage = bufferedImage;
		return findFaceInImage(img2, diffImage);
	}
}

