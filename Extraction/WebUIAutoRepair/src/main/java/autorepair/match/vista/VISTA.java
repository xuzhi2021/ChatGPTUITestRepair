package autorepair.match.vista;

import config.Settings;
import org.opencv.core.*;
import org.opencv.features2d.*;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.openqa.selenium.WebDriver;
import utils.UtilsXpath;

import java.io.File;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static config.Settings.OPENCV_DLL_PATH;

public class VISTA {


    static {
        System.load(OPENCV_DLL_PATH + Core.NATIVE_LIBRARY_NAME + ".dll");
    }

    static float nndrRatio = 1.0f;

    public static String retrieveDomNode(String fullScreenPath, String originalImageFile, WebDriver driver) {
        System.out.println("full:" + fullScreenPath);
        System.out.println("part:" + originalImageFile);
        Point point = featureDetectorAndTemplateMatching(fullScreenPath, originalImageFile);
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

}