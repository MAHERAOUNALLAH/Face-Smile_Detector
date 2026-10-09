package dao;

import entities.AnalyseVisage;
import java.util.List;

public interface IAnalyseVisageDao {
    AnalyseVisage addAnalyse(AnalyseVisage analyse);
    List<AnalyseVisage> listAnalyses();
    List<AnalyseVisage> analysesParMC(String mc);
    AnalyseVisage getAnalyse(int id);
    void deleteAnalyse(int id);
}
