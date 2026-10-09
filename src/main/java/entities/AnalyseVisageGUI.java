package entities;

import metier.AnalyseVisageMetier;
import metier.IAnalyseVisageMetier;

import javax.swing.*;
import java.awt.*;

public class AnalyseVisageGUI extends JFrame {

    private IAnalyseVisageMetier metier = new AnalyseVisageMetier();

    private TableModele tableModele = new TableModele();
    private JTable table = new JTable(tableModele);

    private JTextField txtNom = new JTextField(15);
    private JTextField txtSentiment = new JTextField(15);
    private JTextField txtScore = new JTextField(10);
    private JTextField txtRecherche = new JTextField(20);

    private JButton btnAjouter = new JButton("Ajouter manuellement");
    private JButton btnCamera = new JButton("Analyser camera");
    private JButton btnRechercher = new JButton("Rechercher");
    private JButton btnActualiser = new JButton("Actualiser");
    private JButton btnSupprimer = new JButton("Supprimer");
    private JButton btnQuitter = new JButton("Quitter");

    public AnalyseVisageGUI() {
        setTitle("Reconnaissance faciale et analyse de sentiment");
        setSize(950, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLayout(new BorderLayout());

        JPanel panelHaut = new JPanel(new BorderLayout());

        JPanel panelSaisie = new JPanel(new GridLayout(2, 4, 10, 10));

        panelSaisie.add(new JLabel("Nom utilisateur :"));
        panelSaisie.add(txtNom);

        panelSaisie.add(new JLabel("Sentiment :"));
        panelSaisie.add(txtSentiment);

        panelSaisie.add(new JLabel("Score :"));
        panelSaisie.add(txtScore);

        panelSaisie.add(btnAjouter);
        panelSaisie.add(btnCamera);

        panelHaut.add(panelSaisie, BorderLayout.CENTER);

        add(panelHaut, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        JPanel panelBas = new JPanel(new FlowLayout());

        panelBas.add(new JLabel("Mot cle :"));
        panelBas.add(txtRecherche);
        panelBas.add(btnRechercher);
        panelBas.add(btnActualiser);
        panelBas.add(btnSupprimer);
        panelBas.add(btnQuitter);

        add(panelBas, BorderLayout.SOUTH);

        chargerTable();

        btnAjouter.addActionListener(e -> ajouterAnalyseManuelle());

        btnCamera.addActionListener(e -> analyserCamera());

        btnRechercher.addActionListener(e -> rechercherAnalyse());

        btnActualiser.addActionListener(e -> chargerTable());

        btnSupprimer.addActionListener(e -> supprimerAnalyse());

        btnQuitter.addActionListener(e -> System.exit(0));

        setVisible(true);
    }

    private void ajouterAnalyseManuelle() {
        String nom = txtNom.getText().trim();
        String sentiment = txtSentiment.getText().trim();
        String scoreText = txtScore.getText().trim();

        if (nom.equals("") || sentiment.equals("") || scoreText.equals("")) {
            JOptionPane.showMessageDialog(this, "Veuillez remplir tous les champs");
            return;
        }


            double score = Double.parseDouble(scoreText.replace(",", "."));

            AnalyseVisage analyse = metier.analyserEtEnregistrer(nom, sentiment, score);

            if (analyse != null) {
                JOptionPane.showMessageDialog(this, "Analyse ajoutee avec succes");

                txtNom.setText("");
                txtSentiment.setText("");
                txtScore.setText("");

                chargerTable();
            } else {
                JOptionPane.showMessageDialog(this, "Erreur : donnees invalides");
            }

        }

    private void analyserCamera() {
        JOptionPane.showMessageDialog(
                this,
                "La camera va s'ouvrir. Place ton visage devant la camera."
        );

        AnalyseVisage analyse = metier.analyserCameraEtEnregistrer();
        //appelle la couche métier pour lancer l’analyse par caméra, enregistrer le résultat dans la base de données, puis récupérer l’objet AnalyseVisage créé.

        if (analyse != null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Analyse camera ajoutee avec succes\n"
                            + "Utilisateur : " + analyse.getNomUtilisateur() + "\n"
                            + "Sentiment : " + analyse.getSentiment() + "\n"
                            + "Score : " + analyse.getScore()
            );

            chargerTable();
        } else {
            JOptionPane.showMessageDialog(this, "Aucun visage detecte");
        }
    }

    private void chargerTable() {
        tableModele.charger(metier.listeDesAnalyses());
    }

    private void rechercherAnalyse() {
        String mc = txtRecherche.getText().trim();
        tableModele.charger(metier.rechercherParMC(mc));
    }

    private void supprimerAnalyse() {
        int ligne = table.getSelectedRow();

        if (ligne == -1) {
            JOptionPane.showMessageDialog(this, "Veuillez selectionner une ligne");
            return;
        }

        int id = (int) tableModele.getValueAt(ligne, 0);

        int confirmation = JOptionPane.showConfirmDialog(
                this,
                "Voulez-vous vraiment supprimer cette analyse ?",
                "Confirmation",
                JOptionPane.YES_NO_OPTION
        );

        if (confirmation == JOptionPane.YES_OPTION) {
            metier.supprimerAnalyse(id);
            chargerTable();
            JOptionPane.showMessageDialog(this, "Analyse supprimee");
        }
    }
}