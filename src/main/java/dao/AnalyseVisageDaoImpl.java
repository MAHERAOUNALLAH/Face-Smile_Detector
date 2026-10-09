package dao;

import entities.AnalyseVisage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AnalyseVisageDaoImpl implements IAnalyseVisageDao {

    private Connection connection;

    public AnalyseVisageDaoImpl() {
                connection = SingletonConnection.getConnection();
    }

    @Override
    public AnalyseVisage addAnalyse(AnalyseVisage analyse) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO t_analyse_visage(nom_utilisateur, sentiment, score) VALUES(?, ?, ?)"
            );

            ps.setString(1, analyse.getNomUtilisateur());
            ps.setString(2, analyse.getSentiment());
            ps.setDouble(3, analyse.getScore());

            ps.executeUpdate();
            return analyse;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public List<AnalyseVisage> listAnalyses() {
        List<AnalyseVisage> analyses = new ArrayList<>();

        try {
            PreparedStatement ps = connection.prepareStatement(
                    "SELECT * FROM t_analyse_visage"
            );

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                AnalyseVisage analyse = new AnalyseVisage();

                analyse.setId(rs.getInt("id"));
                analyse.setNomUtilisateur(rs.getString("nom_utilisateur"));
                analyse.setSentiment(rs.getString("sentiment"));
                analyse.setScore(rs.getDouble("score"));

                if (rs.getTimestamp("date_analyse") != null) {
                    LocalDateTime date = rs.getTimestamp("date_analyse").toLocalDateTime();
                    analyse.setDateAnalyse(date);
                }

                analyses.add(analyse);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return analyses;
    }

    @Override
    public List<AnalyseVisage> analysesParMC(String mc) {
        List<AnalyseVisage> analyses = new ArrayList<>();

        try {
            PreparedStatement ps = connection.prepareStatement(
                    "SELECT * FROM t_analyse_visage WHERE nom_utilisateur LIKE ? OR sentiment LIKE ?"
            );

            ps.setString(1, "%" + mc + "%");
            ps.setString(2, "%" + mc + "%");

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                AnalyseVisage analyse = new AnalyseVisage();

                analyse.setId(rs.getInt("id"));
                analyse.setNomUtilisateur(rs.getString("nom_utilisateur"));
                analyse.setSentiment(rs.getString("sentiment"));
                analyse.setScore(rs.getDouble("score"));

                if (rs.getTimestamp("date_analyse") != null) {
                    LocalDateTime date = rs.getTimestamp("date_analyse").toLocalDateTime();
                    analyse.setDateAnalyse(date);
                }

                analyses.add(analyse);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return analyses;
    }

    @Override
    public AnalyseVisage getAnalyse(int id) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "SELECT * FROM t_analyse_visage WHERE id = ?"
            );

            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                AnalyseVisage analyse = new AnalyseVisage();

                analyse.setId(rs.getInt("id"));
                analyse.setNomUtilisateur(rs.getString("nom_utilisateur"));
                analyse.setSentiment(rs.getString("sentiment"));
                analyse.setScore(rs.getDouble("score"));

                if (rs.getTimestamp("date_analyse") != null) {
                    LocalDateTime date = rs.getTimestamp("date_analyse").toLocalDateTime();
                    analyse.setDateAnalyse(date);
                }

                return analyse;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void deleteAnalyse(int id) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "DELETE FROM t_analyse_visage WHERE id = ?"
            );

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}