package autorepair.match.vista2;

import autorepair.state.datacollect.PreDomNodeInfo;
import config.Settings;
import jdk.nashorn.internal.runtime.arrays.ArrayLikeIterator;
import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.features2d.*;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import utils.UtilsXpath;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

import static config.Settings.OPENCV_DLL_PATH;

public class VISTA2 {


    static {
        System.load(OPENCV_DLL_PATH + Core.NATIVE_LIBRARY_NAME + ".dll");
    }

    static float nndrRatio = 1.0f;
    public static List<String> getCandidates(String oldFullScreenPath, String newFullScreenPath,PreDomNodeInfo preDomNodeInfo, WebDriver driver,int size) throws IOException{
        BufferedImage img = ImageIO.read(new File(oldFullScreenPath));
        String originalImageFile = oldFullScreenPath.substring(0, oldFullScreenPath.lastIndexOf("\\")) + "original" + preDomNodeInfo.getElementId() + ".png";
        getScaledSubImage(img, preDomNodeInfo, originalImageFile);
        List<String> re=featureDetectorAndTemplateMatching2(newFullScreenPath,originalImageFile,driver,size);
//        List<Point> points = featureDetectorAndTemplateMatching2(newFullScreenPath, originalImageFile);
////        if (point == null) return "";
//        ArrayList<String> re=new ArrayList<>();
//        for (Point p:points
//             ) {
//            try {
//                if (re.contains(UtilsXpath.getXPathFromLocation(p, driver)))
//                    continue;
//            }catch(JavascriptException e){
//                continue;
//            }
//            re.add(UtilsXpath.getXPathFromLocation(p, driver));
//            if(re.size()==50){
//                System.out.println("VISTA2 generated "+ re.size()+" candidates ( expected size:50");
//                break;
//            }
//        }
        System.out.println("VISTA2 generated "+ re.size()+" candidates");
        return re;
    }
    public static String retrieveDomNode(String oldFullScreenPath, String newFullScreenPath,PreDomNodeInfo preDomNodeInfo, WebDriver driver) throws IOException {
        BufferedImage img = ImageIO.read(new File(oldFullScreenPath));
        String originalImageFile = oldFullScreenPath.substring(0, oldFullScreenPath.lastIndexOf("\\")) + "original" + preDomNodeInfo.getElementId() + ".png";
        getScaledSubImage(img, preDomNodeInfo, originalImageFile);

        Point point = featureDetectorAndTemplateMatching(newFullScreenPath, originalImageFile);
        if (point == null) return "";
        return UtilsXpath.getXPathFromLocation(point, driver);
    }

    public static Point featureDetectorAndTemplateMatching(String imageFile, String templateFile) {
        Point result = null;
        Set<Point> allMatches = new HashSet<Point>();


        boolean isPresent = runFeatureDetection(templateFile, imageFile, allMatches);
        System.out.println(isPresent);
        if (isPresent) {
            result = templateMatchingBestResult(templateFile, imageFile);
        }
        System.out.println(result);
        return result;
    }
    public static List<String> featureDetectorAndTemplateMatching2(String imageFile, String templateFile, WebDriver driver,int size) {
        List<String> result = null;
        Set<Point> allMatches = new HashSet<Point>();


        boolean isPresent = runFeatureDetection(templateFile, imageFile, allMatches);
        System.out.println(isPresent);
        if (isPresent) {
            result = templateMatchingBestResult2(templateFile, imageFile,driver,size);
        }
        System.out.println(result);
        return result;
    }

    public static void getScaledSubImage(BufferedImage img, PreDomNodeInfo preDomNodeInfo, String visualLocator) throws IOException {
        int scale = 5;
        org.openqa.selenium.Point elementCoordinates = null;
        elementCoordinates = new org.openqa.selenium.Point(preDomNodeInfo.getX(), preDomNodeInfo.getY());
        int width = preDomNodeInfo.getWidth();
        int height = preDomNodeInfo.getHeight();
        Rectangle rect = new Rectangle(width, height);
        BufferedImage subImage = null;

        int min_offset_x = Math.min(preDomNodeInfo.getX(), img.getWidth() - rect.width - preDomNodeInfo.getX());
        int min_offset_y = Math.min(preDomNodeInfo.getY(), img.getHeight() - rect.height - preDomNodeInfo.getY());
        int offset = Math.min(min_offset_x, min_offset_y);

        offset = offset / scale;
        try {
            if (preDomNodeInfo.getTagName().equals("option")) {

//                WebElement thisShouldBeTheSelect = element.findElement(By.xpath(".."));
//                new Actions(driver).moveToElement(thisShouldBeTheSelect).perform();

//                elementCoordinates = thisShouldBeTheSelect.getLocation();
                subImage = img.getSubimage(elementCoordinates.x - offset, elementCoordinates.y - offset, 2 * offset + rect.width, 2 * offset + rect.height);
            } else {
                subImage = img.getSubimage(elementCoordinates.x - offset, elementCoordinates.y - offset, 2 * offset + rect.width, 2 * offset + rect.height);
            }
        } catch (RasterFormatException e) {
            System.err.println("[LOG]\tWARNING: " + e.getMessage());
        }

        ImageIO.write(subImage, "png", new File(visualLocator));
        subImage.flush();

    }

    /*
     * Run the FAST and SIFT feature detector algorithms on the two input images and
     * try to match the features found in @object image into the @scene image
     *
     */
    public static boolean runFeatureDetection(String templ, String img, Set<Point> allMatches) {
        boolean res = true;
        try {
            boolean sift = siftDetector(templ, img);
            boolean fast = fastDetector(templ, img, allMatches);
            res = sift || fast;
        } catch (CvException cvException) {
            cvException.printStackTrace();
        }
        return res;
    }

    /*
     * Run the FAST feature detector algorithms on the two input images and try to
     * match the features found in @object image into the @scene image
     *
     */
    private static boolean fastDetector(String object, String scene, Set<Point> allMatches) {
        System.out.println(object);
        System.out.println(scene);
        Mat objectImage = Highgui.imread(object, Highgui.CV_LOAD_IMAGE_GRAYSCALE);
        Mat sceneImage = Highgui.imread(scene, Highgui.CV_LOAD_IMAGE_GRAYSCALE);
        MatOfKeyPoint objectKeyPoints = new MatOfKeyPoint();
        FeatureDetector featureDetector = FeatureDetector.create(FeatureDetector.FAST);
        featureDetector.detect(objectImage, objectKeyPoints);
        MatOfKeyPoint objectDescriptors = new MatOfKeyPoint();
        DescriptorExtractor descriptorExtractor = DescriptorExtractor.create(DescriptorExtractor.SIFT);
        descriptorExtractor.compute(objectImage, objectKeyPoints, objectDescriptors);
        Mat outputImage = new Mat(objectImage.rows(), objectImage.cols(), Highgui.CV_LOAD_IMAGE_COLOR);
        Scalar newKeypointColor = new Scalar(255, 0, 0);
        Features2d.drawKeypoints(objectImage, objectKeyPoints, outputImage, newKeypointColor, 0);
        MatOfKeyPoint sceneKeyPoints = new MatOfKeyPoint();
        MatOfKeyPoint sceneDescriptors = new MatOfKeyPoint();
        featureDetector.detect(sceneImage, sceneKeyPoints);
        descriptorExtractor.compute(sceneImage, sceneKeyPoints, sceneDescriptors);
        List<MatOfDMatch> matches = new LinkedList<MatOfDMatch>();
        DescriptorMatcher descriptorMatcher = DescriptorMatcher.create(DescriptorMatcher.FLANNBASED);
        descriptorMatcher.knnMatch(objectDescriptors, sceneDescriptors, matches, 2);
        LinkedList<DMatch> goodMatchesList = new LinkedList<DMatch>();
        for (int i = 0; i < matches.size(); i++) {
            MatOfDMatch matofDMatch = matches.get(i);
            DMatch[] dmatcharray = matofDMatch.toArray();
            DMatch m1 = dmatcharray[0];
            DMatch m2 = dmatcharray[1];
            if (m1.distance <= m2.distance * nndrRatio) {
                goodMatchesList.addLast(m1);
            }
        }
        if (goodMatchesList.size() == 0) {
            return false;
        }
        int min_accepted_matches = (int) (objectKeyPoints.toList().size() * 0.3);
        if (goodMatchesList.size() > min_accepted_matches) {
            return true;
        } else {
            return false;
        }
    }

    /*
     * Run the SIFT feature detector algorithms on the two input images and try to
     * match the features found in @object image into the @scene image
     *
     */
    private static boolean siftDetector(String object, String scene) {
        Mat objectImage = Highgui.imread(object, Highgui.CV_LOAD_IMAGE_GRAYSCALE);
        Mat sceneImage = Highgui.imread(scene, Highgui.CV_LOAD_IMAGE_GRAYSCALE);
        MatOfKeyPoint objectKeyPoints = new MatOfKeyPoint();
        FeatureDetector featureDetector = FeatureDetector.create(FeatureDetector.SIFT);
        featureDetector.detect(objectImage, objectKeyPoints);
        MatOfKeyPoint objectDescriptors = new MatOfKeyPoint();
        DescriptorExtractor descriptorExtractor = DescriptorExtractor.create(DescriptorExtractor.SIFT);
        descriptorExtractor.compute(objectImage, objectKeyPoints, objectDescriptors);
        Mat outputImage = new Mat(objectImage.rows(), objectImage.cols(), Highgui.CV_LOAD_IMAGE_COLOR);
        Scalar newKeypointColor = new Scalar(255, 0, 0);
        Features2d.drawKeypoints(objectImage, objectKeyPoints, outputImage, newKeypointColor, 0);
        MatOfKeyPoint sceneKeyPoints = new MatOfKeyPoint();
        MatOfKeyPoint sceneDescriptors = new MatOfKeyPoint();
        featureDetector.detect(sceneImage, sceneKeyPoints);
        descriptorExtractor.compute(sceneImage, sceneKeyPoints, sceneDescriptors);
        List<MatOfDMatch> matches = new LinkedList<MatOfDMatch>();
        DescriptorMatcher descriptorMatcher = DescriptorMatcher.create(DescriptorMatcher.FLANNBASED);
        descriptorMatcher.knnMatch(objectDescriptors, sceneDescriptors, matches, 2);
        LinkedList<DMatch> goodMatchesList = new LinkedList<DMatch>();
        for (MatOfDMatch matofDMatch : matches) {
            DMatch[] dmatcharray = matofDMatch.toArray();
            DMatch m1 = dmatcharray[0];
            DMatch m2 = dmatcharray[1];
            if (m1.distance <= m2.distance * nndrRatio) {
                goodMatchesList.addLast(m1);
            }
        }
        if (goodMatchesList.size() == 0) {
            return false;
        }
        int min_accepted_matches = (int) (objectKeyPoints.toList().size() * 0.3);
        if (goodMatchesList.size() > min_accepted_matches) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Run the TM_CCOEFF_NORMED template matching algorithm when normalization has
     * been applied to the results. Returns the center of the rectangle where the
     * best match has been found.
     *
     * @param templateFile
     * @param imageFile
     * @return
     */
    private static Point templateMatchingBestResult(String templateFile, String imageFile) {
        Mat img = Highgui.imread(imageFile);
        Mat templ = Highgui.imread(templateFile);
        int result_cols = img.cols() - templ.cols() + 1;
        int result_rows = img.rows() - templ.rows() + 1;
        Mat result = new Mat(result_rows, result_cols, CvType.CV_32FC1);
        Imgproc.matchTemplate(img, templ, result, Imgproc.TM_CCOEFF_NORMED);
        Core.normalize(result, result, 0, 1, Core.NORM_MINMAX, -1, new Mat());
        List<Point> matches = new LinkedList<Point>();
        for (int i = 0; i < result_rows; i++) {
            for (int j = 0; j < result_cols; j++) {
                if (result.get(i, j)[0] >= 0.99) {
                    matches.add(new Point(i, j));
                }
            }
        }
        if (matches.size() == 0) {
            System.err.println("[LOG]\tWARNING: No matches found!");
        } else if (matches.size() > 1) {
            System.err.println("[LOG]\tWARNING: Multiple matches: " + matches.size());
        }
        Core.MinMaxLocResult mmr = Core.minMaxLoc(result);
        Point matchLoc = mmr.maxLoc;
        /* Return the center. */
        return new Point(matchLoc.x + templ.cols() / 2, matchLoc.y + templ.rows() / 2);
    }


    private static List<String> templateMatchingBestResult2(String templateFile, String imageFile, WebDriver driver,int size) {
//        List<Point> result = null;

        Mat img = Highgui.imread(imageFile);
        Mat templ = Highgui.imread(templateFile);
        int result_cols = img.cols() - templ.cols() + 1;
        int result_rows = img.rows() - templ.rows() + 1;
        Mat result = new Mat(result_rows, result_cols, CvType.CV_32FC1);
        Imgproc.matchTemplate(img, templ, result, Imgproc.TM_CCOEFF_NORMED);
        Core.normalize(result, result, 0, 1, Core.NORM_MINMAX, -1, new Mat());
        List<Point> matches = new LinkedList<Point>();
        for (int i = 0; i < result_rows; i++) {
            for (int j = 0; j < result_cols; j++) {
                matches.add(new Point(j, i));
            }
        }
        Collections.sort(matches, new Comparator<Point>() {
            public int compare(Point p1, Point p2) {
                return -Double.compare(result.get((int)p1.y,(int)p1.x)[0], result.get((int)p2.y,(int)p2.x)[0]);
            }
        });
        //int n = Math.min(matches.size(), size*2000);//if n is too small, the size of result might be lower than expected size
        int n = matches.size();
        System.out.println("n="+n+", match size: "+matches.size());
        ArrayList<String> re=new ArrayList<>();
        List<Point> topMatches = new ArrayList<Point>(n);
        Core.MinMaxLocResult mmr = Core.minMaxLoc(result);
        Point matchLoc = mmr.maxLoc;
        /* Return the center. */
        Point p0= new Point(matchLoc.x + templ.cols() / 2, matchLoc.y + templ.rows() / 2);
        re.add(UtilsXpath.getXPathFromLocation(p0, driver));
        for (int i = 0; i < n; i++) {
//            System.out.print(result.get((int)matches.get(i).y,(int)matches.get(i).x)[0]);
//            System.out.print(", ");
            topMatches.add(matches.get(i));
            Point p=matches.get(i);
            Point p_center=new Point(p.x+ templ.cols() / 2, p.y + templ.rows() / 2);
            try {
                if (re.contains(UtilsXpath.getXPathFromLocation(p_center, driver)))
                    continue;
            }catch(JavascriptException e){
                continue;
            }
            re.add(UtilsXpath.getXPathFromLocation(p_center, driver));
            System.out.println("similarity calculated by vista：");
            System.out.println(result.get((int)p.y,(int)p.x)[0]);
            if(re.size()==size){
                System.out.println("VISTA2 generated "+ re.size()+" candidates ( expected size:"+size);
                break;
            }
        }
        System.out.println("VISTA2 generated "+ re.size()+" candidates");

        return re;
    }

}
