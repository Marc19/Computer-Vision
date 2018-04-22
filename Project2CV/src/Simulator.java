import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import  java.util.Collections;

import javax.swing.*;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;
import java.util.List;

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
        	case "plotHistogram": result = plotHistogramAndCumulativeHistogram(m);break;
        	case "gaussianFilter": result = gaussianFilter(m);break;
        	case "meanFilter": result = meanFilter(m);break;
        	case "medianFilter": result = medianFilter(m);break;
        	case "medianFilterUsingHistogram": result = medianFilterUsingHistogram(m);break;
        	case "contrastStretching": result = contrastStretching(m);break;
        	case "histogramEqualization": result = histogramEqualization(m);break;
        	case "revealMystery": result = revealMystery(m);break;
        	default: result=null; System.out.println("No such effect!");return;
        }
       
        Image output = toBufferedImage( result );
        displayImage("Output Image" , output, 0, 200 );
        
    }

    public void addHistogram(Mat m, int[] histogram, double stepHistogram, String when) 
    {
        Mat result = new Mat();
    	Size size = new Size(1024, 512);
    	result.create(size, m.type());
        if(m.empty())
        {
            System.out.println("Error opening image");
            return;
        }
       
        
        blackenBackground(result);
        plotHistogram(result, histogram, stepHistogram);
       
        Image output = toBufferedImage( result );
        displayImage("Histogram Image "+ when , output, 0, 200 );
        
    }
    
    private Mat plotHistogramAndCumulativeHistogram(Mat m)
    {
    	Mat result = new Mat();
    	Size size = new Size(1024, 512);
    	result.create(size, m.type());
    	
    	int[] histogram = histogram(m);
    	int[] cumulativeHistogram = cumulativeHistogram(m);
    	int maxIntensity = getMax(histogram);
    	double maxIntensityDouble = (double) maxIntensity;
    	
    	int totalNumberOfPixels = m.rows()*m.cols();
    	double totalNumberOfPixelsDouble = (double) totalNumberOfPixels;
    	double stepHistogram = (512/maxIntensityDouble);
    	double stepCumulativeHistogram = (512/totalNumberOfPixelsDouble);
    	    	
    	blackenBackground(result);
    	plotHistogram(result,histogram,stepHistogram);
    	plotCumulativeHistogram(result,cumulativeHistogram,stepCumulativeHistogram);
    	
    	return result;
    }
    
    private Mat blackenBackground(Mat m)
    {
    	for(int i=0; i<=m.rows()-1; i++)
    	{
    		for(int j=0; j<=m.cols()-1; j++)
    		{	
    			double newIntensities[] = new double[m.channels()];
    			
    			double newIntensity;
    			for(int k=0; k<m.channels(); k++)
    			{
    				newIntensity = 0;
    				newIntensities[k] = newIntensity;
    			}
    			m.put(i, j, newIntensities);
    		}
    	}
    	
    	return m;
    }
    
    private Mat plotHistogram(Mat m, int[] histogram, double stepHistogram)
    {
    	int intensityCounter = 0;
		for(int j=0; j<=m.cols()-4; j+=4)
		{	
			double newIntensities1[] = new double[m.channels()];
			double newIntensities2[] = new double[m.channels()];
			
			int numberOfPixels = histogram[intensityCounter];
			double steps = numberOfPixels*stepHistogram;
			
			for(int i=m.rows()-1,c=0; c<steps; i--,c++)
			{
				
				double newIntensity1 = m.get(i, j)[0] + 50;
				double newIntensity2 = m.get(i, j+1)[0] + 50;
				
				newIntensities1[0] = newIntensity1; newIntensities1[1] = newIntensity1; newIntensities1[2] = newIntensity1;
				newIntensities2[0] = newIntensity2; newIntensities2[1] = newIntensity2; newIntensities2[2] = newIntensity2;
				
				m.put(i, j, newIntensities1);
				m.put(i, j+1, newIntensities2);
				
			}
			
			intensityCounter++;
		}
		
		return m;
    }
    
    private Mat plotCumulativeHistogram(Mat m, int[] cumulativeHistogram, double stepCumulativeHistogram)
    {
    	int intensityCounter = 0;
		for(int j=0; j<=m.cols()-4; j+=4)
		{	
			double newIntensities1[] = new double[m.channels()];
			double newIntensities2[] = new double[m.channels()];
			
			int numberOfPixels = cumulativeHistogram[intensityCounter];
			double steps = numberOfPixels*stepCumulativeHistogram;
			
			for(int i=m.rows()-1,c=0; c<steps; i--,c++)
			{
				
				double newIntensity1 = m.get(i, j)[0] + 100;
				double newIntensity2 = m.get(i, j+1)[0] + 100;
				
				newIntensities1[0] = newIntensity1; newIntensities1[1] = newIntensity1; newIntensities1[2] = newIntensity1;
				newIntensities2[0] = newIntensity2; newIntensities2[1] = newIntensity2; newIntensities2[2] = newIntensity2;
				
				m.put(i, j, newIntensities1);
				m.put(i, j+1, newIntensities2);
				
			}
			
			intensityCounter++;
		}
		
		return m;
    }
    
    private Mat gaussianFilter(Mat m)
    {
    	Mat result = new Mat();
       	result.create(m.size(),m.type());
       	
       	int[] histogram = histogram(m);
    	int maxIntensity = getMax(histogram);
    	double maxIntensityDouble = (double) maxIntensity;
    	double stepHistogram = (512/maxIntensityDouble);
    	addHistogram(m, histogram, stepHistogram, "before");
    	
    	for(int i=0; i<=m.rows()-1; i++)
    	{
    		for(int j=0; j<=m.cols()-1; j++)
    		{	
    			double newIntensities[] = new double[m.channels()];
    			
    			double newIntensity;
    			for(int k=0; k<m.channels(); k++)
    			{
    				double pix1 = (i<=1||j<=1)? 0 : m.get(i-2, j-2)[k];
    				double pix2 = (i<=1||j==0)? 0 : m.get(i-2, j-1)[k];
    				double pix3 = (i<=1)? 0 : m.get(i-2, j)[k];
    				double pix4 = (i<=1||j==m.cols()-1)? 0 : m.get(i-2, j+1)[k];
    				double pix5 = (i<=1||j>=m.cols()-2)? 0 : m.get(i-2, j+2)[k];
    				
    				double pix6 = (i==0||j<=1)? 0 : m.get(i-1 , j-2)[k];
    				double pix7 = (i==0||j==0)? 0 : m.get(i-1 , j-1)[k];
    				double pix8 = (i==0)? 0 : m.get(i-1, j)[k];
    				double pix9 = (i==0||j==m.cols()-1)? 0 : m.get(i-1 , j+1)[k];
    				double pix10 = (i==0||j>=m.cols()-2)? 0 : m.get(i-1 , j+2)[k];
    				
    				double pix11 = (j<=1)? 0 : m.get(i , j-2)[k];
    				double pix12 = (j==0)? 0 : m.get(i , j-1)[k];
    				double pix13 =  m.get(i, j)[k];
    				double pix14 = (j==m.cols()-1)? 0 : m.get(i , j+1)[k];
    				double pix15 = (j>=m.cols()-2)? 0 : m.get(i , j+2)[k];
    				
    				double pix16 = (i==m.rows()-1||j<=1)? 0 : m.get(i+1 , j-2)[k];
    				double pix17 = (i==m.rows()-1||j==0)? 0 : m.get(i+1 , j-1)[k];
    				double pix18 = (i==m.rows()-1)? 0 : m.get(i+1, j)[k];
    				double pix19 = (i==m.rows()-1||j==m.cols()-1)? 0 : m.get(i+1 , j+1)[k];
    				double pix20 = (i==m.rows()-1||j>=m.cols()-2)? 0 : m.get(i+1 , j+2)[k];
    				
    				double pix21 = (i>=m.rows()-2||j<=1)? 0 : m.get(i+2, j-2)[k];
    				double pix22 = (i>=m.rows()-2||j==0)? 0 : m.get(i+2, j-1)[k];
    				double pix23 = (i>=m.rows()-2)? 0 : m.get(i+2, j)[k];
    				double pix24 = (i>=m.rows()-2||j==m.cols()-1)? 0 : m.get(i+2, j+1)[k];
    				double pix25 = (i>=m.rows()-2||j>=m.cols()-2)? 0 : m.get(i+2, j+2)[k];
    				
    				
                    newIntensity = (pix1*1+pix2*4+pix3*7+pix4*4+pix5*1+pix6*4+pix7*16+pix8*26+pix9*16+
                    		pix10*4+pix11*7+pix12*26+pix13*40+pix14*26+pix15*7+pix16*4+pix17*16+pix18*26+
                    		pix19*16+pix20*4+pix21*1+pix22*4+pix23*7+pix24*4+pix25*1)/272;
                    
    				newIntensities[k] = newIntensity;
    			}
    			result.put(i, j, newIntensities);
    		}
    	}
    	
    	histogram = histogram(result);
    	maxIntensity = getMax(histogram);
    	maxIntensityDouble = (double) maxIntensity;
    	stepHistogram = (512/maxIntensityDouble);
    	addHistogram(m, histogram, stepHistogram, "after");
    	return result;
    }

    private Mat meanFilter(Mat m)
    {
    	Mat result = new Mat();
       	result.create(m.size(),m.type());
    	
       	int[] histogram = histogram(m);
    	int maxIntensity = getMax(histogram);
    	double maxIntensityDouble = (double) maxIntensity;
    	double stepHistogram = (512/maxIntensityDouble);
    	addHistogram(m, histogram, stepHistogram, "before");
       	
    	for(int i=0; i<=m.rows()-1; i++)
    	{
    		for(int j=0; j<=m.cols()-1; j++)
    		{	
    			double newIntensities[] = new double[m.channels()];
    			
    			double newIntensity;
    			for(int k=0; k<m.channels(); k++)
    			{
    				double pix1 = (i<=1||j<=1)? 0 : m.get(i-2, j-2)[k];
    				double pix2 = (i<=1||j==0)? 0 : m.get(i-2, j-1)[k];
    				double pix3 = (i<=1)? 0 : m.get(i-2, j)[k];
    				double pix4 = (i<=1||j==m.cols()-1)? 0 : m.get(i-2, j+1)[k];
    				double pix5 = (i<=1||j>=m.cols()-2)? 0 : m.get(i-2, j+2)[k];
    				
    				double pix6 = (i==0||j<=1)? 0 : m.get(i-1 , j-2)[k];
    				double pix7 = (i==0||j==0)? 0 : m.get(i-1 , j-1)[k];
    				double pix8 = (i==0)? 0 : m.get(i-1, j)[k];
    				double pix9 = (i==0||j==m.cols()-1)? 0 : m.get(i-1 , j+1)[k];
    				double pix10 = (i==0||j>=m.cols()-2)? 0 : m.get(i-1 , j+2)[k];
    				
    				double pix11 = (j<=1)? 0 : m.get(i , j-2)[k];
    				double pix12 = (j==0)? 0 : m.get(i , j-1)[k];
    				double pix13 =  m.get(i, j)[k];
    				double pix14 = (j==m.cols()-1)? 0 : m.get(i , j+1)[k];
    				double pix15 = (j>=m.cols()-2)? 0 : m.get(i , j+2)[k];
    				
    				double pix16 = (i==m.rows()-1||j<=1)? 0 : m.get(i+1 , j-2)[k];
    				double pix17 = (i==m.rows()-1||j==0)? 0 : m.get(i+1 , j-1)[k];
    				double pix18 = (i==m.rows()-1)? 0 : m.get(i+1, j)[k];
    				double pix19 = (i==m.rows()-1||j==m.cols()-1)? 0 : m.get(i+1 , j+1)[k];
    				double pix20 = (i==m.rows()-1||j>=m.cols()-2)? 0 : m.get(i+1 , j+2)[k];
    				
    				double pix21 = (i>=m.rows()-2||j<=1)? 0 : m.get(i+2, j-2)[k];
    				double pix22 = (i>=m.rows()-2||j==0)? 0 : m.get(i+2, j-1)[k];
    				double pix23 = (i>=m.rows()-2)? 0 : m.get(i+2, j)[k];
    				double pix24 = (i>=m.rows()-2||j==m.cols()-1)? 0 : m.get(i+2, j+1)[k];
    				double pix25 = (i>=m.rows()-2||j>=m.cols()-2)? 0 : m.get(i+2, j+2)[k];
    				
    				
                    newIntensity = (pix1+pix2+pix3+pix4+pix5+pix6+pix7+pix8+pix9+
                    		pix10+pix11+pix12+pix13+pix14+pix15+pix16+pix17+pix18+
                    		pix19+pix20+pix21+pix22+pix23+pix24+pix25)/25;
                    
    				newIntensities[k] = newIntensity;
    			}
    			result.put(i, j, newIntensities);
    		}
    	}
    	
    	histogram = histogram(result);
    	maxIntensity = getMax(histogram);
    	maxIntensityDouble = (double) maxIntensity;
    	stepHistogram = (512/maxIntensityDouble);
    	addHistogram(m, histogram, stepHistogram, "after");
    	return result;
    }

    private Mat medianFilter(Mat m)
    {
    	long startTime = System.currentTimeMillis();
    	
    	Mat result = new Mat();
       	result.create(m.size(),m.type());
    	
    	for(int i=0; i<=m.rows()-1; i++)
    	{
    		for(int j=0; j<=m.cols()-1; j++)
    		{	
    			double newIntensities[] = new double[m.channels()];
    			
    			double newIntensity;
    			for(int k=0; k<m.channels(); k++)
    			{
    				double pix1 = (i<=1||j<=1)? -1 : m.get(i-2, j-2)[k];
    				double pix2 = (i<=1||j==0)? -1 : m.get(i-2, j-1)[k];
    				double pix3 = (i<=1)? -1 : m.get(i-2, j)[k];
    				double pix4 = (i<=1||j==m.cols()-1)? -1 : m.get(i-2, j+1)[k];
    				double pix5 = (i<=1||j>=m.cols()-2)? -1 : m.get(i-2, j+2)[k];
    				
    				double pix6 = (i==0||j<=1)? -1 : m.get(i-1 , j-2)[k];
    				double pix7 = (i==0||j==0)? -1 : m.get(i-1 , j-1)[k];
    				double pix8 = (i==0)? -1 : m.get(i-1, j)[k];
    				double pix9 = (i==0||j==m.cols()-1)? -1 : m.get(i-1 , j+1)[k];
    				double pix10 = (i==0||j>=m.cols()-2)? -1 : m.get(i-1 , j+2)[k];
    				
    				double pix11 = (j<=1)? -1 : m.get(i , j-2)[k];
    				double pix12 = (j==0)? -1 : m.get(i , j-1)[k];
    				double pix13 =  m.get(i, j)[k];
    				double pix14 = (j==m.cols()-1)? -1 : m.get(i , j+1)[k];
    				double pix15 = (j>=m.cols()-2)? -1 : m.get(i , j+2)[k];
    				
    				double pix16 = (i==m.rows()-1||j<=1)? -1 : m.get(i+1 , j-2)[k];
    				double pix17 = (i==m.rows()-1||j==0)? -1 : m.get(i+1 , j-1)[k];
    				double pix18 = (i==m.rows()-1)? -1 : m.get(i+1, j)[k];
    				double pix19 = (i==m.rows()-1||j==m.cols()-1)? -1 : m.get(i+1 , j+1)[k];
    				double pix20 = (i==m.rows()-1||j>=m.cols()-2)? -1 : m.get(i+1 , j+2)[k];
    				
    				double pix21 = (i>=m.rows()-2||j<=1)? -1 : m.get(i+2, j-2)[k];
    				double pix22 = (i>=m.rows()-2||j==0)? -1 : m.get(i+2, j-1)[k];
    				double pix23 = (i>=m.rows()-2)? -1 : m.get(i+2, j)[k];
    				double pix24 = (i>=m.rows()-2||j==m.cols()-1)? -1 : m.get(i+2, j+1)[k];
    				double pix25 = (i>=m.rows()-2||j>=m.cols()-2)? -1 : m.get(i+2, j+2)[k];
    				
    				ArrayList<Double> pixels = new ArrayList<>();
    				pixels.add(pix1);pixels.add(pix2);pixels.add(pix3);pixels.add(pix4);pixels.add(pix5);
    				pixels.add(pix6);pixels.add(pix7);pixels.add(pix8);pixels.add(pix9);pixels.add(pix10);
    				pixels.add(pix11);pixels.add(pix12);pixels.add(pix13);pixels.add(pix14);pixels.add(pix15);
    				pixels.add(pix16);pixels.add(pix17);pixels.add(pix18);pixels.add(pix19);pixels.add(pix20);
    				pixels.add(pix21);pixels.add(pix22);pixels.add(pix23);pixels.add(pix24);pixels.add(pix25);
    				
    				removeNegatives(pixels);
    				Collections.sort(pixels);
    				int middle = pixels.size()/2;
    				
                    newIntensity = pixels.get(middle);
                    
    				newIntensities[k] = newIntensity;
    			}
    			result.put(i, j, newIntensities);
    		}
    	}
    	
    	long endTime   = System.currentTimeMillis();
    	long totalTime = endTime - startTime;
    	System.out.println(totalTime + " milliseconds");
    	return result;
    }

    private Mat medianFilterUsingHistogram(Mat m)
    {
    	long startTime = System.currentTimeMillis();
    	
    	int[] histogram = histogram(m);
    	int minThreshold = getMinThreshold(histogram);
    	int maxThreshold = getMaxThreshold(histogram);
    	
    	Mat result = new Mat();
       	result.create(m.size(),m.type());
    	
    	for(int i=0; i<=m.rows()-1; i++)
    	{
    		for(int j=0; j<=m.cols()-1; j++)
    		{	
    			double newIntensities[] = new double[m.channels()];
    			
    			double newIntensity;
    			for(int k=0; k<m.channels(); k++)
    			{
    				if( m.get(i, j)[k] <= minThreshold || m.get(i, j)[k] >= maxThreshold)
    				{
    					double pix1 = (i<=1||j<=1)? -1 : m.get(i-2, j-2)[k];
	    				double pix2 = (i<=1||j==0)? -1 : m.get(i-2, j-1)[k];
	    				double pix3 = (i<=1)? -1 : m.get(i-2, j)[k];
	    				double pix4 = (i<=1||j==m.cols()-1)? -1 : m.get(i-2, j+1)[k];
	    				double pix5 = (i<=1||j>=m.cols()-2)? -1 : m.get(i-2, j+2)[k];
	    				
	    				double pix6 = (i==0||j<=1)? -1 : m.get(i-1 , j-2)[k];
	    				double pix7 = (i==0||j==0)? -1 : m.get(i-1 , j-1)[k];
	    				double pix8 = (i==0)? -1 : m.get(i-1, j)[k];
	    				double pix9 = (i==0||j==m.cols()-1)? -1 : m.get(i-1 , j+1)[k];
	    				double pix10 = (i==0||j>=m.cols()-2)? -1 : m.get(i-1 , j+2)[k];
	    				
	    				double pix11 = (j<=1)? -1 : m.get(i , j-2)[k];
	    				double pix12 = (j==0)? -1 : m.get(i , j-1)[k];
	    				double pix13 =  m.get(i, j)[k];
	    				double pix14 = (j==m.cols()-1)? -1 : m.get(i , j+1)[k];
	    				double pix15 = (j>=m.cols()-2)? -1 : m.get(i , j+2)[k];
	    				
	    				double pix16 = (i==m.rows()-1||j<=1)? -1 : m.get(i+1 , j-2)[k];
	    				double pix17 = (i==m.rows()-1||j==0)? -1 : m.get(i+1 , j-1)[k];
	    				double pix18 = (i==m.rows()-1)? -1 : m.get(i+1, j)[k];
	    				double pix19 = (i==m.rows()-1||j==m.cols()-1)? -1 : m.get(i+1 , j+1)[k];
	    				double pix20 = (i==m.rows()-1||j>=m.cols()-2)? -1 : m.get(i+1 , j+2)[k];
	    				
	    				double pix21 = (i>=m.rows()-2||j<=1)? -1 : m.get(i+2, j-2)[k];
	    				double pix22 = (i>=m.rows()-2||j==0)? -1 : m.get(i+2, j-1)[k];
	    				double pix23 = (i>=m.rows()-2)? -1 : m.get(i+2, j)[k];
	    				double pix24 = (i>=m.rows()-2||j==m.cols()-1)? -1 : m.get(i+2, j+1)[k];
	    				double pix25 = (i>=m.rows()-2||j>=m.cols()-2)? -1 : m.get(i+2, j+2)[k];
    				
    				
	    				ArrayList<Double> pixels = new ArrayList<>();
	    				pixels.add(pix1);pixels.add(pix2);pixels.add(pix3);pixels.add(pix4);pixels.add(pix5);
	    				pixels.add(pix6);pixels.add(pix7);pixels.add(pix8);pixels.add(pix9);pixels.add(pix10);
	    				pixels.add(pix11);pixels.add(pix12);pixels.add(pix13);pixels.add(pix14);pixels.add(pix15);
	    				pixels.add(pix16);pixels.add(pix17);pixels.add(pix18);pixels.add(pix19);pixels.add(pix20);
	    				pixels.add(pix21);pixels.add(pix22);pixels.add(pix23);pixels.add(pix24);pixels.add(pix25);
	    				
	    				removeNegatives(pixels);
	    				Collections.sort(pixels);
	    				int middle = pixels.size()/2;
	    				
	                    newIntensity = pixels.get(middle);
	                    
	    				newIntensities[k] = newIntensity;
    				}
    				else
    				{
    					newIntensity = m.get(i, j)[k];
    					newIntensities[k] = newIntensity;
    				}
    			}
    			result.put(i, j, newIntensities);
    		}
    	}
    	
    	long endTime   = System.currentTimeMillis();
    	long totalTime = endTime - startTime;
    	System.out.println(totalTime + " milliseconds");
    	return result;
    }

    private int getMinThreshold(int[] histogram)
    {
    	int minThreshold = 0;
    	
    	for(int i=0; i<histogram.length; i++)
    	{
    		if(histogram[i] != 0)
    			continue;
    		
    		minThreshold = i;
    		break;
    	}
    	
    	return minThreshold;
    }

    private int getMaxThreshold(int[] histogram)
    {
    	int maxThreshold = histogram.length-1;
    	
    	for(int i=histogram.length-1; i>=0; i--)
    	{
    		if(histogram[i] != 0)
    			continue;
    		
    		maxThreshold = i;
    		break;
    	}
    	
    	return maxThreshold;
    }
    
    private void removeNegatives(ArrayList<Double> l)
    {
    	for(int i=0; i<l.size(); i++)
    	{
    		if(l.get(i) == -1)
    		{
    			l.remove(i);
    			i--;
    		}
    	}
    }
    
    private Mat contrastStretching(Mat m)
    {
    	Mat result = new Mat();
       	result.create(m.size(),m.type());
    	
       	int[] histogram = histogram(m);
    	int maxIntensity = getMax(histogram);
    	double maxIntensityDouble = (double) maxIntensity;
    	double stepHistogram = (512/maxIntensityDouble);
    	addHistogram(m, histogram, stepHistogram, "before");
       	
       	double min=m.get(0, 0)[0], max = m.get(0, 0)[0];
       	
    	for(int i=0; i<=m.rows()-1; i++)
    	{
    		for(int j=0; j<=m.cols()-1; j++)
    		{	
    			for(int k=0; k<m.channels(); k++)
    			{
    				double pix = m.get(i, j)[k];
    				
    				if(pix<min)
    					min = pix;
    				
    				if(pix>max)
    					max = pix;
    			}
    		}
    	}
    	
    	for(int i=0; i<=m.rows()-1; i++)
    	{
    		for(int j=0; j<=m.cols()-1; j++)
    		{	
    			double newIntensities[] = new double[m.channels()];
    			
    			double newIntensity;
    			for(int k=0; k<m.channels(); k++)
    			{
    				double pix = m.get(i, j)[k];
    				
                    newIntensity = (pix-min)*((255-0)/(max-min))+0;
                    
    				newIntensities[k] = newIntensity;
    			}
    			result.put(i, j, newIntensities);
    		}
    	}    	
    	
    	histogram = histogram(result);
    	maxIntensity = getMax(histogram);
    	maxIntensityDouble = (double) maxIntensity;
    	stepHistogram = (512/maxIntensityDouble);
    	addHistogram(m, histogram, stepHistogram, "after");
    	return result;
    }
    
    private Mat	histogramEqualization(Mat m)
    {
    	int[] cumulativeHistogram = cumulativeHistogram(m);
    	int numberOfPixels = m.rows()*m.cols();
    	
    	int[] histogram = histogram(m);
    	int maxIntensity = getMax(histogram);
    	double maxIntensityDouble = (double) maxIntensity;
    	double stepHistogram = (512/maxIntensityDouble);
    	addHistogram(m, histogram, stepHistogram, "before");
    	
    	Mat result = new Mat();
       	result.create(m.size(),m.type());
    	
    	for(int i=0; i<=m.rows()-1; i++)
    	{
    		for(int j=0; j<=m.cols()-1; j++)
    		{	
    			double newIntensities[] = new double[m.channels()];
    			
    			double newIntensity;
    			for(int k=0; k<m.channels(); k++)
    			{    			
    				Double pix = new Double(m.get(i, j)[k]);
    				int f= cumulativeHistogram[pix.intValue()];
    				
                    newIntensity = f*255/(numberOfPixels);
                    
    				newIntensities[k] = newIntensity;
    			}
    			result.put(i, j, newIntensities);
    		}
    	}
    	
    	histogram = histogram(result);
    	maxIntensity = getMax(histogram);
    	maxIntensityDouble = (double) maxIntensity;
    	stepHistogram = (512/maxIntensityDouble);
    	addHistogram(m, histogram, stepHistogram, "after");
    	return result;
    }
   
    private int[] histogram(Mat m)
    {
    	int[] result = new int[256];
    	
    	for(int i=0; i<=m.rows()-1; i++)
    	{
    		for(int j=0; j<=m.cols()-1; j++)
    		{	
				Double pix = new Double(m.get(i, j)[0]);
				result[pix.intValue()] +=1;	
    		}
    	}
    	
    	return result;
    }
   
    private int[] cumulativeHistogram(Mat m)
    {
    	int[] cumulativeResult = new int[256];
    	int[] result = histogram(m);
    	
    	for(int i=0; i<256; i++)
    		cumulativeResult[i] = result[i]+ ((i>0)?cumulativeResult[i-1]:0);
    	
    	
    	return cumulativeResult;
    }
    
    private Mat revealMystery(Mat m)
    {
    	Mat tree = Imgcodecs.imread("src/images/tree.png");
    	
    	Mat result = new Mat();
       	result.create(m.size(),m.type());
    	
    	for(int i=0; i<=m.rows()-1; i++)
    	{
    		for(int j=0; j<=m.cols()-1; j++)
    		{	
    			double newIntensities[] = new double[m.channels()];
    			
    			double newIntensity;
    			for(int k=0; k<m.channels(); k++)
    			{    		
    				double pixM = m.get(i, j)[k];
    				double pixT = tree.get(i, j)[k];
    				
    				if(pixM > pixT)
    					newIntensity = pixM-pixT;
    				else 
    					newIntensity = 255;
    					
    					if(newIntensity > 2 && newIntensity != 255)
    						newIntensity += 60;
                    
    				newIntensities[k] = newIntensity;
    			}
    			result.put(i, j, newIntensities);
    		}
    	}
    	
    	return result;
    }
    
    private int getMax(int []l)
    {
    	int max = -1;
    	
    	for(int i=0; i<l.length; i++)
    	{
    		if(l[i] > max)
    			max = l[i];
    	}
    	
    	return max;
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
        //s.run("cameraman","plotHistogram");
        //s.run("bat","plotHistogram");
        //s.run("fog","plotHistogram");
//        s.run("fognoise","plotHistogram");
        //s.run("cameraman","gaussianFilter");
//        s.run("cameraman","meanFilter");
//        s.run("fognoise","medianFilter");
//        s.run("fognoise","medianFilterUsingHistogram");
//        s.run("frostfog","contrastStretching");
//        s.run("frostfog","histogramEqualization");
//        s.run("treeM","revealMystery");
        
    }

}

