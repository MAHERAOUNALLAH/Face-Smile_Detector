package entities;

import javax.swing.table.AbstractTableModel;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TableModele extends AbstractTableModel {

    private List<AnalyseVisage> analyses = new ArrayList<>();

    private String[] titres = {
            "ID",
            "Utilisateur",
            "Sentiment",
            "Score",
            "Date analyse"
    };

    @Override
    public int getRowCount() {
        return analyses.size();
    }

    @Override
    public int getColumnCount() {
        return titres.length;
    }

    @Override
    public String getColumnName(int column) {
        return titres[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        AnalyseVisage analyse = analyses.get(rowIndex);

        switch (columnIndex) {
            case 0:
                return analyse.getId();

            case 1:
                return analyse.getNomUtilisateur();

            case 2:
                return analyse.getSentiment();

            case 3:
                return analyse.getScore();

            case 4:
                if (analyse.getDateAnalyse() == null) {
                    return "";
                }

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                return analyse.getDateAnalyse().format(formatter);

            default:
                return null;
        }
    }

    public void charger(List<AnalyseVisage> analyses) {
        this.analyses = analyses;
        fireTableDataChanged();
    }
}