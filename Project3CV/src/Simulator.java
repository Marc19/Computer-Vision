import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import sun.applet.Main;

public class Simulator 
{
	public void run(String imageName, String effect) 
    {
        Mat m = Imgcodecs.imread("src/images/"+ imageName + ".jpg");
        Mat result;
        if(m.empty())
        {
            System.out.println("Error opening image");
            return;
        }
       
        Image input = toBufferedImage( m );
        displayImage("Input Image" , input, 0, 200 );
        
        switch(effect)
        {
        	case "generateAnaglyph": result = generateAnaglyph(m);break;
        	case "disparity": result = getCandidates(m,0,1,0,1,1,1);break;
        	default: result=null; System.out.println("No such effect!");return;
        }
       
        Image output = toBufferedImage( result );
        displayImage("Output Image" , output, 0, 200 );
        
    }

	private Mat generateAnaglyph(Mat m) 
    {
    	Mat result = new Mat();
    	Size s = new Size(m.width()/2-3, m.height());
    	result.create(s,m.type());
    	
    	for(int i=0; i<m.rows(); i++)
    	{
    		for(int j=0; j<m.cols()/2 - 3; j++)
    		{	
    			int secondImageJ = j+m.cols()/2 -2;
    			double newIntensities[] = new double[m.channels()];
    			
				newIntensities[0] = m.get(i, j)[0];
				newIntensities[1] = m.get(i, j)[1];
				newIntensities[2] = m.get(i, secondImageJ)[2];
    			
    			result.put(i, j, newIntensities);    		
			}
    	}
    	
    	return result;
	}

	private Mat getCandidates(Mat matSrc, int minX, int maxX, int minY, int maxY, int m, int n)
	{
		Mat mat = new Mat(matSrc.rows(), matSrc.cols(), CvType.CV_8UC1);
		Imgproc.cvtColor(matSrc, mat, Imgproc.COLOR_RGB2GRAY);
		
		Mat result = new Mat();
    	Size s = new Size(mat.width()/2-3, mat.height());
    	result.create(s,mat.type());
    	
		Mat leftImage = getLeftImage(mat);
		Mat rightImage = getRightImage(mat);
		
		for(int i=0; i<leftImage.rows(); i++)
    	{
    		for(int j=0; j<leftImage.cols(); j++)
    		{	
    			double newIntensity;
    			
    			ArrayList<Integer> leftWindow = getWindow(leftImage,i,j,m,n);;
    			ArrayList<Integer> rightWindow;
    			
				int minDifference = 255;
				
				for( int x = minX; x <= maxX; x++ )
				{
					for( int y = minY; y <= maxY; y++)
					{		
						rightWindow = getWindow(rightImage,(x+i),(j+y),m,n);
						int sadValue = (rightWindow == null)?255:calculateSAD(leftWindow,rightWindow);
						
						if( sadValue < minDifference)
							minDifference = sadValue;
						
					}
				}
				
				newIntensity = minDifference;
				
				result.put(i, j, newIntensity); 
			}	
			
    	}
		
		return result;
	}
	
	private int calculateSAD(ArrayList<Integer> leftWindow, ArrayList<Integer> rightWindow) 
	{
		if(leftWindow.size() != rightWindow.size())
			return 255;
		
		int result = 0;
		
		for(int i= 0; i < leftWindow.size(); i++)
			result += Math.abs(leftWindow.get(i) - rightWindow.get(i));
		
		return result;
	}

	private ArrayList<Integer> getWindow(Mat mat, int i, int j, int m, int n)
	{
		if( i<0 || i>mat.rows()-1 || j<0 || j>mat.cols()-1)
			return null;
		
		
		ArrayList<Integer> resultWindow = new ArrayList<>();
		int offsetInM = (m-1)/2;
		int offsetInN = (n-1)/2;
		
		for(int mOffset=0 ; mOffset <= offsetInM; mOffset++)
		{
			int positiveOffsetM = i+mOffset;
			int negativeOffsetM = i-mOffset;
			
			for(int nOffset=0 ; nOffset <= offsetInN; nOffset++)
			{
				int positiveOffsetN = j+nOffset;
				int negativeOffsetN = j-nOffset;
				
				if(mOffset == 0 && nOffset == 0)
				{
					resultWindow.add((int) mat.get(i, j)[0]);
				}
				else if( mOffset == 0)
				{
					if( j < mat.cols()-offsetInN)
						resultWindow.add((int) mat.get(i, positiveOffsetN)[0]);
					if( j > offsetInN )
						resultWindow.add((int) mat.get(i, negativeOffsetN)[0]);
				}
				else if( nOffset == 0 )
				{
					if( i < mat.rows()-offsetInM)
						resultWindow.add((int) mat.get(positiveOffsetM, j)[0]);
					if( i > offsetInM)
						resultWindow.add((int) mat.get(negativeOffsetM, j)[0]);
				}
				else
				{
					if(i < mat.rows()-offsetInM && j < mat.cols()-offsetInN )
						resultWindow.add((int) mat.get(positiveOffsetM, positiveOffsetN)[0]);
					if(i < mat.rows()-offsetInM && j > offsetInN)
						resultWindow.add((int) mat.get(positiveOffsetM, negativeOffsetN)[0]);
					if(i > offsetInM && j < mat.cols()-offsetInN)	
						resultWindow.add((int) mat.get(negativeOffsetM, positiveOffsetN)[0]);
					if(i > offsetInM && j > offsetInN)	
						resultWindow.add((int) mat.get(negativeOffsetM, negativeOffsetN)[0]);
				}
			}
		}
		
		return resultWindow;
	}
	private Mat getLeftImage(Mat m)
	{
		Mat result = new Mat();
    	Size s = new Size(m.width()/2-3, m.height());
    	result.create(s,m.type());
    	
    	for(int i=0; i<m.rows(); i++)
    	{
    		for(int j=0; j<m.cols()/2 - 3; j++)
    		{	
    			double newIntensities[] = new double[m.channels()];
    			double newIntensity;
    			
    			for(int k=0; k<m.channels(); k++)
    			{
    				newIntensity = m.get(i, j)[k];
    				newIntensities[k] = newIntensity;
    			}
    			
    			result.put(i, j, newIntensities);    		
			}
    	}
    	
    	return result;
	}

	private Mat getRightImage(Mat m)
	{
		Mat result = new Mat();
    	Size s = new Size(m.width()/2-3, m.height());
    	result.create(s,m.type());
    	
    	for(int i=0; i<m.rows(); i++)
    	{
    		for(int j=0; j<m.cols()/2 - 3; j++)
    		{	
    			double newIntensities[] = new double[m.channels()];
    			double newIntensity;
    			
    			for(int k=0; k<m.channels(); k++)
    			{
    				int secondImageJ = j+m.cols()/2 -2;
    				newIntensity = m.get(i, secondImageJ)[k];
    				newIntensities[k] = newIntensity;
    			}
    			
    			result.put(i, j, newIntensities);    		
			}
    	}
    	
    	return result;
	}
	
	public Image toBufferedImage(Mat m) 
    {
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if ( m.channels() > 1 ) 
            type = BufferedImage.TYPE_3BYTE_BGR;
        
        int bufferSize = m.channels()*m.cols()*m.rows();
        byte [] b = new byte[bufferSize];
        m.get(0,0,b); // get all the pixels
        BufferedImage image = new BufferedImage(m.cols(),m.rows(), type);
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(b, 0, targetPixels, 0, b.length);
        return image;
    }
   
    public void displayImage(String title, Image img, int x, int y)
    {
        ImageIcon icon = new ImageIcon(img);
        JFrame frame=new JFrame(title);
        JLabel lbl=new JLabel(icon);
        frame.add(lbl);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocation(x, y);
        frame.setVisible(true);
    }
   
    public static void main( String [] args)
    {
    	// Load the native library.
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        
        Simulator s = new Simulator();
//        s.run("stereoImage","generateAnaglyph");
        s.run("stereoImage","disparity");
        
    }
}
