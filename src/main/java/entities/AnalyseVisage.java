package entities;

import java.time.LocalDateTime;

public class AnalyseVisage {
    private int id;
    private String nomUtilisateur;
    private String sentiment;
    private double score;
    private LocalDateTime dateAnalyse;

    public AnalyseVisage() {
    }

    public AnalyseVisage(String nomUtilisateur, String sentiment, double score) {
        this.nomUtilisateur = nomUtilisateur;
        this.sentiment = sentiment;
        this.score = score;
    }



    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNomUtilisateur() {
        return nomUtilisateur;
    }

    public void setNomUtilisateur(String nomUtilisateur) {
        this.nomUtilisateur = nomUtilisateur;
    }

    public String getSentiment() {
        return sentiment;
    }

    public void setSentiment(String sentiment) {
        this.sentiment = sentiment;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public LocalDateTime getDateAnalyse() {
        return dateAnalyse;
    }

    public void setDateAnalyse(LocalDateTime dateAnalyse) {
        this.dateAnalyse = dateAnalyse;
    }
}