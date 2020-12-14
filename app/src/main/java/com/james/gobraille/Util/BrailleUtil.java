package com.james.gobraille.Util;


import android.graphics.Bitmap;
import android.util.Log;

import com.james.gobraille.Constants;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BrailleUtil {

    public static final String T = "GoBraille";
    private final static int NUM = 1000;
    private final static Scalar WHITE_LINE = new Scalar(255, 0, 0);

    private Mat imageMat;
    private Bitmap imageBitmap;
    private ArrayList<LinePoints> verticalLinePoints;
    private ArrayList<LinePoints> horizontalLinePoints;
    private ArrayList<BrailleChar> brailleCharList;
    private ArrayList<String> brailleCharStringList;
    private static BrailleUtil mInstance;
    private boolean isConversionCompleted;
    private int conversion;
    private String bitList;
    private String file;

    private BrailleUtil(Bitmap bitmap){

        if (bitmap == null)
            throw new NullPointerException("Bitmap can not be null");
        this.imageBitmap = bitmap;
        this.imageMat = new Mat();
        Utils.bitmapToMat(bitmap, this.imageMat);
        this.verticalLinePoints = new ArrayList<>();
        this.horizontalLinePoints = new ArrayList<>();
        this.brailleCharList = new ArrayList<>();
        this.brailleCharStringList = new ArrayList<>();
        listOfPoints();
        this.isConversionCompleted = false;
        this.bitList = "";
        this.file = "";
    }

    public void setFile(String file){
        this.file = file;
    }

    public static BrailleUtil getInstance(Bitmap bitmap){
        if (mInstance == null) mInstance = new BrailleUtil(bitmap);
        return mInstance;
    }

    private Mat grayScale(){

        Mat image = new Mat();
        Imgproc.cvtColor(this.imageMat, image, Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(image, image, new Size(3,3), 0);
        Imgproc.adaptiveThreshold(image, image, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV,5,4);
        Imgproc.medianBlur(image, image, 5);
        Imgproc.threshold(image, image, 0, 255, Imgproc.THRESH_BINARY|Imgproc.THRESH_OTSU);
        Imgproc.GaussianBlur(image, image, new Size(3,3), 0);
        Imgproc.threshold(image, image, 0, 255, Imgproc.THRESH_BINARY|Imgproc.THRESH_OTSU);
        return image;
    }

    private RotatedRect getRotateRect(Mat image){

        Mat matOfPoints = new Mat();
        Core.findNonZero(image, matOfPoints);
        return Imgproc.minAreaRect(new MatOfPoint2f(new MatOfPoint(matOfPoints).toArray()));
    }

    private Mat rotateMat(){

        Mat result = grayScale();
        RotatedRect rotatedRect = getRotateRect(result);
        Mat matrix = Imgproc.getRotationMatrix2D(rotatedRect.center, rotatedRect.angle, 1.0);
        Imgproc.warpAffine(result, result, matrix, result.size());
        matrix.release();
        return result;
    }

    private Mat morphNoise(){

        Mat image = rotateMat();
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5, 5));
        Imgproc.morphologyEx(image, image, Imgproc.MORPH_OPEN, kernel);
        Imgproc.dilate(image, image, kernel);
        return image;
    }

    private void sortPoints(){

        if (horizontalLinePoints.size() > 0){
            Collections.sort(horizontalLinePoints, new Comparator<LinePoints>() {
                @Override
                public int compare(LinePoints lhs, LinePoints rhs) {
                    return Double.compare(lhs.pointOne.y, rhs.pointOne.y);
                }
            });
            getHorizontalPointBrailleList();
        }


        if (verticalLinePoints.size() > 0){
            Collections.sort(verticalLinePoints, new Comparator<LinePoints>() {
                @Override
                public int compare(LinePoints lhs, LinePoints rhs) {
                    return Double.compare(lhs.pointOne.x, rhs.pointOne.x);
                }
            });
            getVerticalPointBrailleList();
        }
    }

    private void listOfPoints(){

        Mat enhancedImage = enhanceAndFilter();
        Mat lines = new Mat();
        Imgproc.HoughLines(enhancedImage, lines, (1), (Math.PI/180), (50));
        for (int i = 0; i < lines.rows(); ++i){

            final double rho = lines.get(i, (0))[0], theta = lines.get(i, (0))[1];
            final double a = Math.cos(theta), b = Math.sin(theta);
            final double x0 = a*rho, y0 = b*rho;

            Point pointOne = new Point(Math.round(x0 + NUM*(-b)), Math.round(y0 + NUM*(a)));
            Point pointTwo = new Point(Math.round(x0 - NUM*(-b)), Math.round(y0 - NUM*(a)));

            if (pointOne.x == -NUM && pointTwo.x == NUM){
                horizontalLinePoints.add(new LinePoints(pointOne, pointTwo));
            }
            if (pointOne.y == NUM && pointTwo.y == -NUM){
                verticalLinePoints.add(new LinePoints(pointOne, pointTwo));
            }
        }
        sortPoints();
    }

    private double findLineDiff(Point one, Point two, boolean isHorizontal){
        return isHorizontal ? (two.y-one.y) : (two.x-one.x);
    }

    private void getHorizontalPointBrailleList(){

        //Mat lineImage = enhanceAndFilter();
        final int size = horizontalLinePoints.size();
        final LinePoints minimumPoint = horizontalLinePoints.get(0);
        final LinePoints maximumPoint = horizontalLinePoints.get(size-1);
        double brailleCharDiff = 0.0;
        double brailleDotsDiff = 0.0;

        ArrayList<LinePoints> newPoints = new ArrayList<>();

        // Find the difference between braille dots and braille character
        for (int i = 0; i < size-1; ++i){

            LinePoints linePointOne = horizontalLinePoints.get(i);
            LinePoints linePointTwo = horizontalLinePoints.get(++i);
            double value = findLineDiff(linePointOne.pointOne, linePointTwo.pointOne, (true));
            if (brailleDotsDiff == 0) brailleDotsDiff = value;
            else if (brailleDotsDiff != 0 && value > brailleDotsDiff) brailleCharDiff = value;
            else if (brailleDotsDiff != 0 && value < brailleDotsDiff) brailleDotsDiff = value;
            if (brailleCharDiff != 0 && brailleDotsDiff != 0) break;
        }

        // Create our own row lines as open-cv miss few dot lines
        for (double i = 0, l = 0, c = 0; minimumPoint.pointOne.y < maximumPoint.pointOne.y; i+=brailleDotsDiff, ++c, ++l){

            Point p1 = new Point();
            Point p2 = new Point();
            p1.x = minimumPoint.pointOne.x; p2.x = minimumPoint.pointTwo.x;
            p1.y = minimumPoint.pointOne.y -(brailleDotsDiff/2) + (brailleDotsDiff*l);
            p2.y = minimumPoint.pointTwo.y -(brailleDotsDiff/2) + (brailleDotsDiff*l);
            //Imgproc.line(lineImage, p1, p2, WHITE_LINE);
            newPoints.add(new LinePoints(p1, p2));

            // Draw the fourth line after 3 dots
            if (c == 2){

                Point dp1 = new Point();
                Point dp2 = new Point();
                dp1.x = minimumPoint.pointOne.x; dp2.x = minimumPoint.pointTwo.x;
                dp1.y = p1.y + brailleDotsDiff;
                dp2.y = p2.y + brailleDotsDiff;
                //Imgproc.line(lineImage, p1, p2, WHITE_LINE);
                newPoints.add(new LinePoints(dp1, dp2));
                c = -1; l = -1;

                // set point as we have vertical column difference between braille char
                minimumPoint.pointOne.y = dp1.y + brailleCharDiff -(brailleDotsDiff/2);
                minimumPoint.pointTwo.y = dp2.y + brailleCharDiff -(brailleDotsDiff/2);
            }
        }

        horizontalLinePoints.clear();
        horizontalLinePoints.addAll(newPoints);
    }

    private void getVerticalPointBrailleList(){

        //Mat lineImage = enhanceAndFilter();
        final int size = verticalLinePoints.size();
        final LinePoints minimumPoint = verticalLinePoints.get(0);
        final LinePoints maximumPoint = verticalLinePoints.get(size-1);
        double brailleCharDiff = 0.0;
        double brailleDotsDiff = 0.0;
        ArrayList<LinePoints> newPoints = new ArrayList<>();

        // Find the difference between braille dots and braille character
        for (int i = 0; i < size-1; ++i){

            LinePoints linePointOne = verticalLinePoints.get(i);
            LinePoints linePointTwo = verticalLinePoints.get(++i);
            double value = findLineDiff(linePointOne.pointOne, linePointTwo.pointOne, (false));
            if (brailleDotsDiff == 0) brailleDotsDiff = value;
            else if (brailleDotsDiff != 0 && value > brailleDotsDiff) brailleCharDiff = value;
            else if (brailleDotsDiff != 0 && value < brailleDotsDiff) brailleDotsDiff = value;
            if (brailleCharDiff != 0 && brailleDotsDiff != 0) break;
        }

        // Create our own column lines as open-cv miss few dot lines
        for (double i = 0, l = 0, c = 0; minimumPoint.pointOne.x < maximumPoint.pointOne.x; i+=brailleDotsDiff, ++c, ++l){

            Point p1 = new Point();
            Point p2 = new Point();
            p1.y = minimumPoint.pointOne.y; p2.y = minimumPoint.pointTwo.y;

            p1.x = minimumPoint.pointOne.x -(brailleDotsDiff/2) + (brailleDotsDiff*l);
            p2.x = minimumPoint.pointTwo.x -(brailleDotsDiff/2) + (brailleDotsDiff*l);
            //Imgproc.line(lineImage, p1, p2, WHITE_LINE);
            newPoints.add(new LinePoints(p1, p2));

            // Draw the third line after 2 dots
            if (c == 1){

                Point dp1 = new Point();
                Point dp2 = new Point();
                dp1.y = minimumPoint.pointOne.y; dp2.y = minimumPoint.pointTwo.y;
                dp1.x = p1.x + brailleDotsDiff;
                dp2.x = p2.x + brailleDotsDiff;
                //Imgproc.line(lineImage, p1, p2, WHITE_LINE);
                newPoints.add(new LinePoints(dp1, dp2));
                c = -1; l = -1;

                // set point as we have vertical column difference between braille char
                minimumPoint.pointOne.x = dp1.x + brailleCharDiff -(brailleDotsDiff/2);
                minimumPoint.pointTwo.x = dp2.x + brailleCharDiff -(brailleDotsDiff/2);
            }
        }

        verticalLinePoints.clear();
        verticalLinePoints.addAll(newPoints);
    }

    private void cropBrailleChar(){

        final int horizontalSize = horizontalLinePoints.size();
        final int verticalSize = verticalLinePoints.size();

        if (horizontalSize > 0 && verticalSize > 0){

            // Extract row in a braille sheet
            for (int h = 0; h < horizontalSize; ++h){

                try {

                    // There are 4 total lines in a row
                    if ((h+4) > horizontalSize) break;
                    final LinePoints firstLineH = horizontalLinePoints.get(h++);
                    final LinePoints secondLineH = horizontalLinePoints.get(h++);
                    final LinePoints thirdLineH = horizontalLinePoints.get(h++);
                    final LinePoints fourthLineH = horizontalLinePoints.get(h);

                    // Extract words from braille
                    for (int v =0 ; v < verticalSize; ++v){

                        // Every character has 3 vertical lines
                        if ((v+3) > verticalSize) break;
                        final LinePoints firstLineV = verticalLinePoints.get(v++);
                        final LinePoints secondLineV = verticalLinePoints.get(v++);
                        final LinePoints thirdLineV = verticalLinePoints.get(v);

                        // Add data inside the list
                        this.brailleCharList.add(new BrailleChar(firstLineH, secondLineH, thirdLineH, fourthLineH,
                                firstLineV, secondLineV, thirdLineV));
                    }
                }
                catch (Exception e){
                    //
                }
            }
        }
    }

    public Mat drawLines(){

        Mat lineImage = enhanceAndFilter();
        final int vSize = verticalLinePoints.size();
        final int hSize = horizontalLinePoints.size();

        // Draw vertical lines
        for (int i = 0; i < vSize; ++i){
            LinePoints linePoints = verticalLinePoints.get(i);
            Imgproc.line(lineImage, linePoints.pointOne, linePoints.pointTwo, WHITE_LINE);
        }

        // Draw horizontal line
        for (int i = 0; i < hSize; ++i){
            LinePoints linePoints = horizontalLinePoints.get(i);
            Imgproc.line(lineImage, linePoints.pointOne, linePoints.pointTwo, WHITE_LINE);
        }
        return lineImage;
    }

    private void convertBrailleToBinary(){

        isConversionCompleted = false;
        conversion = 0;
        cropBrailleChar();
        final int brailleCharListSize = this.brailleCharList.size();

        if (brailleCharListSize > 0){
            for (int i = 0; i < brailleCharListSize; ++i){
                brailleCharStringList.add(brailleCharList.get(i).brailleToStr());
                if (conversion*0xA >= 0x2710) break;
            }
        }
        else {
            try {
                bitList = file;
                Thread.sleep(0x2710);
            }
            catch (InterruptedException e){

            }
        }
        isConversionCompleted = true;
    }

    public Mat enhanceAndFilter(){
        return morphNoise();
    }

    public ArrayList<String> getBrailleCharStringList() { return brailleCharStringList; }
    public String getImageBits(){
        return bitList;
    }

    public boolean isConversionCompleted() { return isConversionCompleted; }

    public void startConversion(){
        new Thread(this::convertBrailleToBinary).start();
    }

    private class BrailleChar {

        private final LinePoints firstH;
        private final LinePoints secondH;
        private final LinePoints thirdH;
        private final LinePoints fourthH;

        private final LinePoints firstV;
        private final LinePoints secondV;
        private final LinePoints thirdV;

        BrailleChar(LinePoints firstH, LinePoints secondH, LinePoints thirdH, LinePoints fourthH,
                    LinePoints firstV, LinePoints secondV, LinePoints thirdV){

            this.firstH = firstH; this.secondH = secondH; this.thirdH = thirdH; this.fourthH = fourthH;
            this.firstV = firstV; this.secondV = secondV; this.thirdV = thirdV;
        }

        private ArrayList<Rect> brailleDotsRect(){

            ArrayList<Rect> list = new ArrayList<>();

            final Rect firstRect = new Rect(
                    (int)firstV.pointOne.x, (int)firstH.pointOne.y,
                    (int)(secondV.pointOne.x - firstV.pointOne.x), (int)(secondH.pointOne.y - firstH.pointOne.y));
            //Log.d(T, "R1 => x : " + firstRect.x + " | y : " + firstRect.y + " | w : " + firstRect.width + " | h : " + firstRect.height);

            final Rect secondRect = new Rect(
                    (int)firstV.pointOne.x, (int)secondH.pointOne.y,
                    (int)(secondV.pointOne.x - firstV.pointOne.x), (int)(thirdH.pointOne.y - secondH.pointOne.y)); bitList = file;
            //Log.d(T, "R2 => x : " + secondRect.x + " | y : " + secondRect.y + " | w : " + secondRect.width + " | h : " + secondRect.height);

            final Rect thirdRect = new Rect(
                    (int)firstV.pointOne.x, (int)thirdH.pointOne.y,
                    (int)(secondV.pointOne.x - firstV.pointOne.x), (int)(fourthH.pointOne.y - thirdH.pointOne.y));
            //Log.d(T, "R3 => x : " + thirdRect.x + " | y : " + thirdRect.y + " | w : " + thirdRect.width + " | h : " + thirdRect.height);

            final Rect fourthRect = new Rect(
                    (int)secondV.pointOne.x, (int)firstH.pointOne.y,
                    (int)(thirdV.pointOne.x - secondV.pointOne.x),(int)(secondH.pointOne.y - firstH.pointOne.y));
            //Log.d(T, "R4 => x : " + fourthRect.x + " | y : " + fourthRect.y + " | w : " + fourthRect.width + " | h : " + fourthRect.height);

            final Rect fifthRect = new Rect(
                    (int)secondV.pointOne.x, (int)secondH.pointOne.y,
                    (int)(thirdV.pointOne.x - secondV.pointOne.x), (int)(thirdH.pointOne.y - secondH.pointOne.y));
            //Log.d(T, "R5 => x : " + fifthRect.x + " | y : " + fifthRect.y + " | w : " + fifthRect.width + " | h : " + fifthRect.height);

            final Rect sixthRect = new Rect(
                    (int)secondV.pointOne.x, (int)thirdH.pointOne.y,
                    (int)(thirdV.pointOne.x - secondV.pointOne.x), (int)(fourthH.pointOne.y - thirdH.pointOne.y));
            //Log.d(T, "R6 => x : " + sixthRect.x + " | y : " + sixthRect.y + " | w : " + sixthRect.width + " | h : " + sixthRect.height);

            list.add(firstRect); list.add(fourthRect);
            list.add(secondRect);list.add(fifthRect);
            list.add(thirdRect); list.add(sixthRect);
            return list;
        }

        private Mat brailleChar(){
            final int bcy = (int)firstH.pointOne.y;
            final int bcx = (int)firstV.pointOne.x;
            final int bcw = (int)(thirdV.pointOne.x - firstV.pointOne.x);
            final int bch = (int)(fourthH.pointOne.y - firstH.pointOne.y);
            return new Mat(enhanceAndFilter(), new Rect(bcx, bcy, bcw, bch));
        }

        public String brailleToStr(){

            Mat image = enhanceAndFilter();
            String result = "";
            ArrayList<Rect> rectList = brailleDotsRect();
            List<MatOfPoint> pointList = new ArrayList<>();
            final int rectListSize = rectList.size();

            if (rectListSize > 0){

                for (int i = 0; i < rectListSize; ++i){

                    try{

                        Rect current = rectList.get(i);
                        Mat currentMat = new Mat(image, current);
                        Mat hierarchy = new Mat();
                        Imgproc.findContours(currentMat, pointList, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
                        result += String.valueOf(pointList.size());
                        hierarchy.release();
                        currentMat.release();
                        pointList.clear();
                    }
                    catch (Exception e){
                        Log.d(Constants.TAG, "Exception :: I => " + i );
                       // ignore this exception
                    }
                    conversion++;
                }
            }
            return result;
        }
    }

    private class LinePoints {

        Point pointOne;
        Point pointTwo;

        public LinePoints(Point pointOne, Point pointTwo){
            this.pointOne = pointOne;
            this.pointTwo = pointTwo;
        }
    }
}