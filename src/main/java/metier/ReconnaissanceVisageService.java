package metier;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class ReconnaissanceVisageService {

    private static final int LARGEUR_VISAGE = 100;
    private static final int HAUTEUR_VISAGE = 100;

    /*
     * Plus le seuil est petit, plus la reconnaissance est stricte.
     * Si le programme affiche toujours "Inconnu", augmente vers 75 ou 85.
     * Si le programme reconnait trop facilement une mauvaise personne, baisse vers 50.
     */
    private static final double SEUIL_RECONNAISSANCE = 65.0;

    private List<ModeleVisage> modeles = new ArrayList<>();
    private CascadeClassifier faceDetector;

    public ReconnaissanceVisageService() {
        try {
            String faceCascade = copierResourceVersFichierTemp(
                    "cascades/haarcascade_frontalface_default.xml",
                    ".xml"
            );

            faceDetector = new CascadeClassifier(faceCascade);

            if (faceDetector.empty()) {
                System.out.println("Erreur : cascade visage non chargee");
            }

            chargerDataset();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String reconnaitre(Mat visageGris) {
        if (modeles.isEmpty()) {
            System.out.println("Aucun modele visage charge depuis dataset");
            return "Inconnu";
        }

        Mat visagePrepare = preparerVisage(visageGris);

        String meilleurNom = "Inconnu";
        double meilleurScore = Double.MAX_VALUE;

        for (ModeleVisage modele : modeles) {
            double score = calculerDifference(visagePrepare, modele.getImageVisage());

            if (score < meilleurScore) {
                meilleurScore = score;
                meilleurNom = modele.getNomPersonne();
            }
        }

        System.out.println("Meilleur score reconnaissance = " + meilleurScore + " pour " + meilleurNom);

        if (meilleurScore <= SEUIL_RECONNAISSANCE) {
            return meilleurNom;
        }

        return "Inconnu";
    }

    private void chargerDataset() {
        File dossierDataset = new File("dataset");

        if (!dossierDataset.exists() || !dossierDataset.isDirectory()) {
            System.out.println("Dossier dataset introuvable");
            return;
        }

        File[] dossiersPersonnes = dossierDataset.listFiles();

        if (dossiersPersonnes == null) {
            return;
        }

        for (File dossierPersonne : dossiersPersonnes) {
            if (!dossierPersonne.isDirectory()) {
                continue;
            }

            String nomPersonne = dossierPersonne.getName();

            File[] images = dossierPersonne.listFiles();

            if (images == null) {
                continue;
            }

            for (File imageFile : images) {
                String nomFichier = imageFile.getName().toLowerCase();

                if (!nomFichier.endsWith(".jpg")
                        && !nomFichier.endsWith(".jpeg")
                        && !nomFichier.endsWith(".png")) {
                    continue;
                }

                Mat image = Imgcodecs.imread(imageFile.getAbsolutePath());

                if (image.empty()) {
                    System.out.println("Image non lisible : " + imageFile.getAbsolutePath());
                    continue;
                }

                Mat visage = extraireVisageDepuisImage(image);

                if (visage == null || visage.empty()) {
                    System.out.println("Aucun visage detecte dans : " + imageFile.getName());
                    continue;
                }

                Mat visagePrepare = preparerVisage(visage);

                modeles.add(new ModeleVisage(nomPersonne, visagePrepare));

                System.out.println("Image ajoutee au modele : " + nomPersonne + " -> " + imageFile.getName());
            }
        }

        System.out.println("Nombre total de modeles charges : " + modeles.size());
    }

    private Mat extraireVisageDepuisImage(Mat image) {
        Mat gris = new Mat();

        if (image.channels() == 3) {
            Imgproc.cvtColor(image, gris, Imgproc.COLOR_BGR2GRAY);
        } else {
            gris = image.clone();
        }

        Imgproc.equalizeHist(gris, gris);

        MatOfRect visages = new MatOfRect();
        faceDetector.detectMultiScale(gris, visages);

        Rect[] tableauxVisages = visages.toArray();

        if (tableauxVisages.length == 0) {
            return null;
        }

        Rect plusGrandVisage = tableauxVisages[0];

        for (Rect visage : tableauxVisages) {
            if (visage.area() > plusGrandVisage.area()) {
                plusGrandVisage = visage;
            }
        }

        return new Mat(gris, plusGrandVisage);
    }

    private Mat preparerVisage(Mat visageGris) {
        Mat visagePrepare = new Mat();

        Imgproc.resize(
                visageGris,
                visagePrepare,
                new Size(LARGEUR_VISAGE, HAUTEUR_VISAGE)
        );

        Imgproc.equalizeHist(visagePrepare, visagePrepare);

        return visagePrepare;
    }

    private double calculerDifference(Mat visageCamera, Mat visageDataset) {
        Mat difference = new Mat();

        Core.absdiff(visageCamera, visageDataset, difference);

        Scalar moyenne = Core.mean(difference);

        return moyenne.val[0];
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

    private static class ModeleVisage {

        private String nomPersonne;
        private Mat imageVisage;

        public ModeleVisage(String nomPersonne, Mat imageVisage) {
            this.nomPersonne = nomPersonne;
            this.imageVisage = imageVisage;
        }

        public String getNomPersonne() {
            return nomPersonne;
        }

        public Mat getImageVisage() {
            return imageVisage;
        }
    }
}