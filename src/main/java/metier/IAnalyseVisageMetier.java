package metier;

import entities.AnalyseVisage;
import java.util.List;

public interface IAnalyseVisageMetier {

    public AnalyseVisage ajouterAnalyse(AnalyseVisage analyse);

    public AnalyseVisage analyserEtEnregistrer(String nomUtilisateur, String sentiment, double score);

    public AnalyseVisage analyserCameraEtEnregistrer();


    public List<AnalyseVisage> listeDesAnalyses();

    public List<AnalyseVisage> rechercherParMC(String mc);

    public AnalyseVisage getAnalyse(int id);

    public void supprimerAnalyse(int id);
}