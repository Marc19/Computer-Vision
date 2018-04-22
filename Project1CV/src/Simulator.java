import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.swing.*;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public class Simulator
{

    public void run(String imageName, String effect) 
    {
        Mat m = Imgcodecs.imread("src/images/"+ imageName + ".png");
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
        	case "dim": result = dim(m);break;
        	case "removeShadow": result = removeShadow(m);break;
        	case "brightenCoat": result = brightenCoat(m);break;
        	case "eliminateWater": result = eliminateWater(m);break;
        	case "combineLondon1": result = combineLondon1(m);break;
        	case "combineLondon2": result = combineLondon2(m);break;
        	default: result=null; System.out.println("No such effect!");return;
        }
       
        Image output = toBufferedImage( result );
        displayImage("Output Image" , output, 0, 200 );
        
    }

	private Mat dim(Mat m) 
    {
    	Mat result = new Mat();
    	result.create(m.size(),m.type());
    	
    	for(int i=0; i<m.rows(); i++)
    	{
    		for(int j=0; j<m.cols(); j++)
    		{	
    			double sum[] = new double[m.channels()];
    			for(int k=0; k<m.channels(); k++)
    			{
    				double newIntensity = saturateCastUchar(m.get(i, j)[k] - 60) ;
    				sum[k] = newIntensity;
    			}
    			result.put(i, j, sum);
    		}
    	}
    	
    	return result;
	}

    private Mat removeShadow(Mat m) 
    {
    	Mat result = new Mat();
    	result.create(m.size(),m.type());
    	
    	for(int i=0; i<m.rows(); i++)
    	{
    		for(int j=0; j<m.cols(); j++)
    		{	
    			double sum[] = new double[m.channels()];
    			for(int k=0; k<m.channels(); k++)
    			{
    				double newIntensity ;
    				if(m.get(i, j)[k] > 160)
    					newIntensity = 160;
    				else
    					newIntensity = m.get(i, j)[k];
    				
    				sum[k] = newIntensity;
    			}
    			result.put(i, j, sum);
    		}
    	}
    	
    	return result;
	}
    
    private Mat brightenCoat(Mat m) 
    {
    	Mat result = new Mat();
    	result.create(m.size(),m.type());
    	
    	for(int i=0; i<m.rows(); i++)
    	{
    		for(int j=0; j<m.cols(); j++)
    		{	
    			double sum[] = new double[m.channels()];
    			
    			double newIntensity;
    			for(int k=0; k<m.channels(); k++)
    			{
					if(m.get(i, j)[k] < 40)
						newIntensity = m.get(i, j)[k] + 20 ;
					else
						newIntensity = m.get(i, j)[k];
    				
    				sum[k] = newIntensity;
    			}
    			result.put(i, j, sum);
    		}
    	}
    	
    	return result;
	}

    private Mat eliminateWater(Mat m) 
	{
    	Mat result = new Mat();
    	result.create(m.size(),m.type());
    	
		Mat edgeDetected = edgeDetection(m);
		Mat widenedWhitePixels = widenWhitePixels(edgeDetected);
		
		for(int i=0; i<widenedWhitePixels.rows(); i++)
    	{
    		for(int j=0; j<widenedWhitePixels.cols(); j++)
    		{	
    			double newIntensities[] = new double[widenedWhitePixels.channels()];
    			
    			double newIntensity;
    			for(int k=0; k<widenedWhitePixels.channels(); k++)
    			{
					if( widenedWhitePixels.get(i, j)[k] == 255 )
						newIntensity = m.get(i, j)[k];
					else
						newIntensity = 0;
    				
    				newIntensities[k] = newIntensity;
    			}
    			result.put(i, j, newIntensities);
    		}
    	}
		
		return result;
	}
    
    private Mat edgeDetection(Mat m) 
    {
    	Mat result = new Mat();
    	result.create(m.size(),m.type());
    	
    	for(int i=0; i<m.rows(); i++)
    	{
    		for(int j=0; j<m.cols(); j++)
    		{	
    			double newIntensities[] = new double[m.channels()];
    			
    			double newIntensity;
    			for(int k=0; k<m.channels(); k++)
    			{
					if( j<m.cols()-1 &&  Math.abs(m.get(i, j)[k] - m.get(i, j+1)[k])> 12)
						newIntensity = 255;
					else
						newIntensity =0;
    				
    				newIntensities[k] = newIntensity;
    			}
    			result.put(i, j, newIntensities);
    		}
    	}
    	
    	return result;
	}

    private Mat widenWhitePixels(Mat m)
    {
    	Mat result = new Mat();
       	result.create(m.size(),m.type());
    	
    	for(int i=0; i<m.rows()-1; i++)
    	{
    		for(int j=0; j<m.cols()-1; j++)
    		{	
    			double newIntensities[] = new double[m.channels()];
    			
    			double newIntensity;
    			for(int k=0; k<m.channels(); k++)
    			{
    				double me =  m.get(i, j)[k];
    				
    				double right = (j==m.cols()-1)? 0 : m.get(i , j+1)[k];
    				double left = (j==0)? 0 : m.get(i , j-1)[k];
    				double top = (i==0)? 0 : m.get(i-1, j)[k];
                    double bottom = (i==m.rows()-1)? 0 : m.get(i+1, j)[k];
                    double bottomRight = (i==m.rows()-1||j==m.cols()-1)? 0 : m.get(i+1 , j+1)[k];
                    double bottomLeft = (i==m.rows()-1||j==0)? 0 : m.get(i+1 , j-1)[k];
                    double topRight = (i==0||j==m.cols()-1)? 0 : m.get(i-1 , j+1)[k];
                    double topLeft = (i==0||j==0)? 0 : m.get(i-1 , j-1)[k];
    				
                    if(me == 255 || right == 255 || left == 255 || top == 255 || bottom == 255 ||
                       bottomRight == 255 || bottomLeft == 255 || topRight == 255 || topLeft == 255)
                    	newIntensity = 255;
                    else
                    	newIntensity = 0;
                    
    				newIntensities[k] = newIntensity;
    			}
    			result.put(i, j, newIntensities);
    		}
    	}
    	
    	return result;
    }
    
	private Mat combineLondon1(Mat m)
    {
    	Mat jb = Imgcodecs.imread("src/images/james.png");
    	Mat background = new Mat();
    	m.copyTo(background);
    	
    	background = scaleDown(background);
    	jb = scaleDown(jb);
    	jb = shiftLeft(jb);
    	
    	Mat result = new Mat();
    	result.create(jb.size(),jb.type());
    	
    	for(int i=0; i<jb.rows(); i++)
    	{
    		for(int j=0; j<jb.cols(); j++)
    		{	
    			double newIntensities[] = new double[jb.channels()];
    			
    			double newIntensity;
    			for(int k=0; k<jb.channels(); k++)
    			{
					if(jb.get(i, j)[k] > 230)
						newIntensity = background.get(i, j)[k];
					else
						newIntensity = jb.get(i, j)[k];
    				
    				newIntensities[k] = newIntensity;
    			}
    			result.put(i, j, newIntensities);
    		}
    	}
    	
    	return result;
    }
    
    private Mat combineLondon2(Mat m)
    {
    	Mat jb = Imgcodecs.imread("src/images/james.png");
    	Mat background = new Mat();
    	m.copyTo(background);
    	
    	background = scaleDown(background);
    	jb = scaleDown(jb);
    	jb = reflectOnY(jb);
    	
    	Mat result = new Mat();
    	result.create(jb.size(),jb.type());
    	
    	for(int i=0; i<jb.rows(); i++)
    	{
    		for(int j=0; j<jb.cols(); j++)
    		{	
    			double newIntensities[] = new double[jb.channels()];
    			
    			double newIntensity;
    			for(int k=0; k<jb.channels(); k++)
    			{
					if(jb.get(i, j)[k] > 230)
						newIntensity = background.get(i, j)[k];
					else
						newIntensity = jb.get(i, j)[k];
    				
    				newIntensities[k] = newIntensity;
    			}
    			result.put(i, j, newIntensities);
    		}
    	}
    	
    	return result;
    }
    
    private Mat shiftLeft(Mat m) 
    {
    	Mat result = new Mat();
    	
    	result.create(m.size(),m.type());
    	
    	for(int i=0; i<m.rows()-1; i++)
    	{
    		for(int j=0; j<m.cols()-1; j++)
    		{	
    			double newIntensities[] = new double[m.channels()];
    			
    			for(int k=0; k<m.channels(); k++)
    			{
    				if(j<m.cols()-1-100)
    				{
    					newIntensities[k] = m.get(i, j+100)[k];
    				}
    				else
    				{
    					newIntensities[k] = 255;
    				}
    					
    			}
				result.put(i, j, newIntensities);
    			
    		}
    	}
    	
    	return result;
	}

    private Mat reflectOnY(Mat m)
    {
    	Mat result = new Mat();
    	
    	result.create(m.size(),m.type());
    	
    	for(int i=0; i<m.rows()-1; i++)
    	{
    		for(int j=0; j<m.cols()/2+1; j++)
    		{	
    			double newIntensitiesLeft[] = new double[m.channels()];
    			double newIntensitiesRight[] = new double[m.channels()];
    			
    			for(int k=0; k<m.channels(); k++)
    			{
    				newIntensitiesRight[k] = m.get(i, j)[k];
    				newIntensitiesLeft[k] = m.get(i, m.cols()-1-j)[k];
    					
    			}
				result.put(i, j, newIntensitiesLeft);
				result.put(i, m.cols()-j, newIntensitiesRight);
    			
    		}
    	}
    	
    	return result;
    }
        
    public static double saturateCastUchar(double x) 
    {
        return x > 255.0 ? 255.0 : (x < 0.0 ? 0.0 : x);
    }

    public void printPixels(Mat m)
    {
    	for(int i=0; i<m.rows(); i++)
    	{
    		for(int j=0; j<m.cols(); j++)
    		{	
    			for(int k=0; k<m.channels(); k++)
    			{
    				System.out.print(m.get(i, j)[k] + "-");
    			}
    			System.out.println();
    		}
    	}
    }
    
    public Mat scaleDown(Mat m)
    {
    	Mat result = new Mat();
    	
    	Size s = new Size(m.width()/2, m.height()/2);
    	result.create(s,m.type());
    	
    	for(int i=0; i<m.rows()-1; i+=2)
    	{
    		for(int j=0; j<m.cols()-1; j+=2)
    		{	
    			double newIntensities[] = new double[m.channels()];
    			
    			double newIntensity;
    			for(int k=0; k<m.channels(); k++)
    			{
    				double me =  m.get(i, j)[k];
    				double right = m.get(i , j+1)[k];
                    double bottom = m.get(i+1, j)[k];
                    double bottomRight = m.get(i+1 , j+1)[k];
    				
                    newIntensity = (me+right+bottom+bottomRight)/4;
                    
    				newIntensities[k] = newIntensity;
    			}
    			result.put((i/2), (j/2), newIntensities);
    		}
    	}
    	
    	return result;
    }
    
    public Mat sharpen(Mat myImage)
    {
    	Mat result = new Mat();
    	
        myImage.convertTo(myImage, CvType.CV_8U); //convert to 8 bit

        int nChannels = myImage.channels(); //create channels
        result.create(myImage.size(),myImage.type());

      //! [basic_method_loop]
        for(int j = 1 ; j < myImage.rows()-1; ++j)
        {
            for(int i = 1 ; i < myImage.cols()-1; ++i)
            {
                double sum[] = new double[nChannels];

                for(int k = 0; k < nChannels; ++k) {

                    double top = -myImage.get(j - 1, i)[k];
                    double bottom = -myImage.get(j + 1, i)[k];
                    double center = (5 * myImage.get(j, i)[k]);
                    double left = -myImage.get(j , i - 1)[k];
                    double right = -myImage.get(j , i + 1)[k];

                    sum[k] = saturateCastUchar(top + bottom + center + left + right);
                }

                result.put(j, i, sum);
            }
        }
      //! [basic_method_loop]

      //! [borders]
        result.row(0).setTo(new Scalar(0));
        result.row(result.rows()-1).setTo(new Scalar(0));
        result.col(0).setTo(new Scalar(0));
        result.col(result.cols()-1).setTo(new Scalar(0));
      //! [borders]

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
 
    public static void main(String[] args)
    {
        // Load the native library.
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        
        Simulator s = new Simulator();
//        s.run("GUC","dim");
//        s.run("calculator","removeShadow");
//        s.run("cameraman","brightenCoat");
//        s.run("lake","eliminateWater");
//        s.run("london1","combineLondon1");
        s.run("london2","combineLondon2");
    }

    
}

