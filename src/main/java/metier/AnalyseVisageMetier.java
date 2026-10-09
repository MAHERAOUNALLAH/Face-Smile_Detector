package metier;

import dao.AnalyseVisageDaoImpl;
import dao.IAnalyseVisageDao;
import entities.AnalyseVisage;

import java.util.List;

public class AnalyseVisageMetier implements IAnalyseVisageMetier {

    private IAnalyseVisageDao dao = new AnalyseVisageDaoImpl();
    private OpenCVAnalyseService openCVAnalyseService = new OpenCVAnalyseService();

    @Override
    public AnalyseVisage ajouterAnalyse(AnalyseVisage analyse) {
        return dao.addAnalyse(analyse);
    }

    @Override
    public AnalyseVisage analyserEtEnregistrer(String nomUtilisateur, String sentiment, double score) {
        AnalyseVisage analyse = new AnalyseVisage();

        analyse.setNomUtilisateur(nomUtilisateur);
        analyse.setSentiment(sentiment);
        analyse.setScore(score);

        return ajouterAnalyse(analyse);
    }

    @Override
    public AnalyseVisage analyserCameraEtEnregistrer() {
        AnalyseVisage resultat = openCVAnalyseService.analyserDepuisCamera();

        return ajouterAnalyse(resultat);
    }

    @Override
    public List<AnalyseVisage> listeDesAnalyses() {
        return dao.listAnalyses();
    }

    @Override
    public List<AnalyseVisage> rechercherParMC(String mc) {
        if (mc == null) {
            mc = "";
        }

        return dao.analysesParMC(mc);
    }

    @Override
    public AnalyseVisage getAnalyse(int id) {
        if (id <= 0) {
            return null;
        }

        return dao.getAnalyse(id);
    }

    @Override
    public void supprimerAnalyse(int id) {
        if (id > 0) {
            dao.deleteAnalyse(id);
        }
    }
}