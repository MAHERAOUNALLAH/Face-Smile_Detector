package metier;

import entities.AnalyseVisage;
import nu.pattern.OpenCV;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.HighGui;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class OpenCVAnalyseService {

    private CascadeClassifier faceDetector;
    private CascadeClassifier smileDetector;
    private ReconnaissanceVisageService reconnaissanceService;

    public OpenCVAnalyseService() {
        try {
            OpenCV.loadLocally();

            String faceCascade = copierResourceVersFichierTemp(
                    "cascades/haarcascade_frontalface_default.xml",
                    ".xml"
            );

            String smileCascade = copierResourceVersFichierTemp(
                    "cascades/haarcascade_smile.xml",
                    ".xml"
            );

            faceDetector = new CascadeClassifier(faceCascade);
            smileDetector = new CascadeClassifier(smileCascade);
            reconnaissanceService = new ReconnaissanceVisageService();

            if (faceDetector.empty()) {
                System.out.println("Erreur : le fichier haarcascade_frontalface_default.xml n'est pas charge");
            }

            if (smileDetector.empty()) {
                System.out.println("Erreur : le fichier haarcascade_smile.xml n'est pas charge");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public AnalyseVisage analyserDepuisCamera() {
        VideoCapture camera = new VideoCapture(0);
        // on ouvre la camera
        if (!camera.isOpened()) {
            System.out.println("Erreur : impossible d'ouvrir la camera");
            return null;
        }

        Mat image = new Mat();
        // On crée une matrice OpenCV qui va contenir chaque image capturée par la caméra.
        int compteurDetection = 0;

        try {
            while (true) {
                boolean success = camera.read(image);
                // On lit une image depuis la caméra et on la stocke dans la variable image.

                if (!success || image.empty()) {
                    continue;
                }

                Core.flip(image, image, 1);
                // pretraitement
                Mat gris = new Mat();
                Imgproc.cvtColor(image, gris, Imgproc.COLOR_BGR2GRAY);

                Imgproc.equalizeHist(gris, gris);

                MatOfRect visages = new MatOfRect();
                faceDetector.detectMultiScale(gris, visages);
                // On utilise le fichier Haar Cascade pour détecter les visages dans l'image grise.

                for (Rect visage : visages.toArray()) {
                    // nswaer il rectangle autour du visage detecte
                    Imgproc.rectangle(
                            image,
                            visage,
                            new Scalar(0, 255, 0),
                            2
                    );

                    Mat zoneVisageGris = new Mat(gris, visage);
                    // reconnaissance faciale
                    String nomUtilisateur = reconnaissanceService.reconnaitre(zoneVisageGris);
                    // fonction analyser sentiment
                    ResultatSentiment resultatSentiment = analyserSentiment(zoneVisageGris);

                    String sentiment = resultatSentiment.getSentiment();
                    double score = resultatSentiment.getScore();

                    Imgproc.putText(
                            image,
                            nomUtilisateur + " - " + sentiment + " (" + score + ")",
                            new Point(visage.x, visage.y - 10),
                            Imgproc.FONT_HERSHEY_SIMPLEX,
                            0.7,
                            new Scalar(0, 255, 0),
                            2
                    );

                    compteurDetection++;

                    /*
                     * On attend quelques frames pour eviter
                     * d'enregistrer une detection accidentelle.
                     */
                    if (compteurDetection >= 20) {
                        camera.release();
                        HighGui.destroyAllWindows();

                        return new AnalyseVisage(
                                nomUtilisateur,
                                sentiment,
                                score
                        );
                    }
                }

                HighGui.imshow("Camera - Reconnaissance faciale", image);

                int touche = HighGui.waitKey(30);

                // 27 = touche ESC
                if (touche == 27) {
                    break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        camera.release();
        HighGui.destroyAllWindows();

        return null;
    }

    private ResultatSentiment analyserSentiment(Mat zoneVisageGris) {

        int largeur = zoneVisageGris.width();
        int hauteur = zoneVisageGris.height();

        /*
         * On analyse seulement la partie basse du visage,
         * car le sourire se trouve normalement dans la zone bouche.
         */
        Rect zoneBoucheRect = new Rect(
                0,
                hauteur / 2,
                largeur,
                hauteur / 2
        );

        Mat zoneBouche = new Mat(zoneVisageGris, zoneBoucheRect);

        MatOfRect sourires = new MatOfRect();

        smileDetector.detectMultiScale(
                zoneBouche,
                sourires,
                1.8,
                35,
                0,
                new Size(35, 20),
                new Size()
        );

        Rect[] souriresDetectes = sourires.toArray();

        if (souriresDetectes.length == 0) {
            return new ResultatSentiment("Neutre", 45.0);
        }

        double plusGrandRatio = 0.0;

        for (Rect sourire : souriresDetectes) {
            /*
             * Correction importante :
             * zoneBouche est un Mat, donc il n'a pas area().
             * On utilise zoneBoucheRect.area().
             */
            double ratio = sourire.area() / zoneBoucheRect.area();

            if (ratio > plusGrandRatio) {
                plusGrandRatio = ratio;
            }
        }

        /*
         * Si le sourire detecte est trop petit,
         * on considere que c'est un faux positif.
         */
        if (plusGrandRatio < 0.08) {
            return new ResultatSentiment("Neutre", 50.0);
        }

        double score = 60.0 + plusGrandRatio * 300.0;

        if (score > 98.0) {
            score = 98.0;
        }

        score = Math.round(score * 100.0) / 100.0;

        return new ResultatSentiment("Heureux", score);
    }

    private String copierResourceVersFichierTemp(String cheminResource, String extension) throws Exception {
        InputStream inputStream = getClass()
                .getClassLoader()
                .getResourceAsStream(cheminResource);

        if (inputStream == null) {
            throw new RuntimeException("Fichier introuvable : " + cheminResource);
        }

        File tempFile = File.createTempFile("opencv_", extension);
        tempFile.deleteOnExit();

        Files.copy(inputStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        return tempFile.getAbsolutePath();
    }

    private static class ResultatSentiment {

        private String sentiment;
        private double score;

        public ResultatSentiment(String sentiment, double score) {
            this.sentiment = sentiment;
            this.score = score;
        }

        public String getSentiment() {
            return sentiment;
        }

        public double getScore() {
            return score;
        }
    }
}