package com.rebelrobotics.scoutingapp;

import com.google.cloud.firestore.Firestore;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.awt.Desktop;
public class HelloController {
    public static String currentMatchKey;
    private static final String TBA_BASE_URL = "https://www.thebluealliance.com/api/v3";
    private static final String TBA_AUTH_KEY = "XXOfRpH6aJwOebCAung9Z0cRhm3TLFoCd1Vm6bdPksWnDiShd6zrp5LBwNpv2lb5";
    private final OkHttpClient httpClient = new OkHttpClient();
    @FXML
    private ListView<String> list;
    private static final String teamNumber="254";
    @FXML
    private TableView<MatchDetail> matchDetailTable;
    @FXML
    private TableView<TeamDetail> redTeams;
    @FXML
    private TableView<TeamDetail> blueTeams;
    private final ObservableList<MatchInfo> matchData = FXCollections.observableArrayList();
    @FXML
    private CheckBox redcoop;
    @FXML
    private CheckBox redensemble;
    @FXML
    private CheckBox redmelody;
    @FXML
    private CheckBox bluecoop;
    @FXML
    private CheckBox blueensemble;
    @FXML
    private CheckBox bluemelody;
    @FXML
    private Label eventName;
    @FXML
    private Label eventDate;
    public HelloController(){
        try {
            Firestore db = FirestoreInitializer.initializeFirestore();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void initialize() {
        // Set up columns for redTeams TableView
        TableColumn<TeamDetail, String> redTeamNumberCol = new TableColumn<>("Team Number");
        redTeamNumberCol.setCellValueFactory(new PropertyValueFactory<>("teamNumber"));

        TableColumn<TeamDetail, String> redTeamNameCol = new TableColumn<>("Team Name");
        redTeamNameCol.setCellValueFactory(new PropertyValueFactory<>("teamName"));

        TableColumn<TeamDetail, String> redTeamSchoolCol = new TableColumn<>("School");
        redTeamSchoolCol.setCellValueFactory(new PropertyValueFactory<>("school"));

        TableColumn<TeamDetail, String> redTeamCityStateCol = new TableColumn<>("City/State");
        redTeamCityStateCol.setCellValueFactory(new PropertyValueFactory<>("cityState"));

        redTeams.getColumns().addAll(redTeamNumberCol, redTeamNameCol, redTeamSchoolCol, redTeamCityStateCol);

// Set up columns for blueTeams TableView
        TableColumn<TeamDetail, String> blueTeamNumberCol = new TableColumn<>("Team Number");
        blueTeamNumberCol.setCellValueFactory(new PropertyValueFactory<>("teamNumber"));

        TableColumn<TeamDetail, String> blueTeamNameCol = new TableColumn<>("Team Name");
        blueTeamNameCol.setCellValueFactory(new PropertyValueFactory<>("teamName"));

        TableColumn<TeamDetail, String> blueTeamSchoolCol = new TableColumn<>("School");
        blueTeamSchoolCol.setCellValueFactory(new PropertyValueFactory<>("school"));

        TableColumn<TeamDetail, String> blueTeamCityStateCol = new TableColumn<>("City/State");
        blueTeamCityStateCol.setCellValueFactory(new PropertyValueFactory<>("cityState"));

        blueTeams.getColumns().addAll(blueTeamNumberCol, blueTeamNameCol, blueTeamSchoolCol, blueTeamCityStateCol);

                // Programmatically create and configure the TableView columns
        TableColumn<MatchDetail, String> matchNumberCol = new TableColumn<>("Match #");
        matchNumberCol.setCellValueFactory(new PropertyValueFactory<>("matchNumber"));
        matchNumberCol.setPrefWidth(52);

        TableColumn<MatchDetail, String> teamScoreCol = new TableColumn<>(teamNumber+"'s Score");
        teamScoreCol.setCellValueFactory(new PropertyValueFactory<>("teamScore"));
        teamScoreCol.setPrefWidth(98);
        TableColumn<MatchDetail, String> opponentScoreCol = new TableColumn<>("Opponent Score");
        opponentScoreCol.setCellValueFactory(new PropertyValueFactory<>("opponentScore"));
        opponentScoreCol.setPrefWidth(111);

        // Add the columns to the TableView
        matchDetailTable.getColumns().add(matchNumberCol);
        matchDetailTable.getColumns().add(teamScoreCol);
        matchDetailTable.getColumns().add(opponentScoreCol);

        // Load the data into the ListView and set up a listener for selection changes
        populateMatchListView(list, teamNumber);

        list.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    try {
                        updateMatchDetails(matchDetailTable, newValue);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
        );
    }
    public JsonObject fetchEventDetails(String eventKey) throws IOException {
        String url = TBA_BASE_URL + "/event/" + eventKey;

        Request request = new Request.Builder()
                .url(url)
                .header("X-TBA-Auth-Key", TBA_AUTH_KEY)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            String responseData = response.body().string();
            return JsonParser.parseString(responseData).getAsJsonObject();
        }
    }

    private void populateMatchListView(ListView<String> listView, String teamNumber) {
        new Thread(() -> {
            try {
                DateTimeFormatter inputFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                DateTimeFormatter outputFormat = DateTimeFormatter.ofPattern("MMMM dd @ hh:mm");

                LocalDateTime dateTime;
                List<MatchInfo> matches = fetchMatchData(teamNumber);
                for (MatchInfo match : matches) {
                    dateTime=LocalDateTime.parse(match.getDateTime(), inputFormat);
                    String matchSummary = dateTime.format(outputFormat) + ": " + match.getResult() + " (" + match.getMatchNumber() + ")";
                    matchData.add(match);
                    listView.getItems().add(matchSummary);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private List<MatchInfo> fetchMatchData(String teamNumber) throws IOException {
        String url = TBA_BASE_URL + "/team/frc" + teamNumber + "/matches/2024"; // Assuming current year is 2024

        Request request = new Request.Builder()
                .url(url)
                .header("X-TBA-Auth-Key", TBA_AUTH_KEY)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            String responseData = response.body().string();
            return parseMatchData(responseData, teamNumber);
        }
    }

    private List<MatchInfo> parseMatchData(String jsonData, String teamNumber) {
        List<MatchInfo> matchList = new ArrayList<>();
        JsonArray matchesArray = JsonParser.parseString(jsonData).getAsJsonArray();

        for (var matchElement : matchesArray) {
            JsonObject matchObject = matchElement.getAsJsonObject();
            int matchNumber = matchObject.get("match_number").getAsInt();
            String matchKey = matchObject.get("key").getAsString();
            String compLevel = matchObject.get("comp_level").getAsString();
            String dateTime = matchObject.get("time").isJsonNull() ? "TBA" :
                    LocalDateTime.ofEpochSecond(matchObject.get("time").getAsLong(), 0, ZoneOffset.UTC)
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

            JsonObject alliances = matchObject.getAsJsonObject("alliances");

            List<TeamDetail> redAllianceTeams = parseAllianceTeams(alliances.getAsJsonObject("red"));
            List<TeamDetail> blueAllianceTeams = parseAllianceTeams(alliances.getAsJsonObject("blue"));

            String teamAlliance = null;
            String opponentAlliance = null;
            if (alliances.getAsJsonObject("blue").getAsJsonArray("team_keys").toString().contains("frc" + teamNumber)) {
                teamAlliance = "blue";
                opponentAlliance = "red";
            } else if (alliances.getAsJsonObject("red").getAsJsonArray("team_keys").toString().contains("frc" + teamNumber)) {
                teamAlliance = "red";
                opponentAlliance = "blue";
            }

            if (teamAlliance != null) {
                int teamScore = alliances.getAsJsonObject(teamAlliance).get("score").getAsInt();
                int opponentScore = alliances.getAsJsonObject(opponentAlliance).get("score").getAsInt();
                boolean won = teamScore > opponentScore;

                String result = won ? "W" : "L";

                MatchInfo matchInfo = new MatchInfo(matchNumber, matchKey, dateTime, result, teamScore, opponentScore, redAllianceTeams, blueAllianceTeams);
                matchList.add(matchInfo);
                System.out.println("key = "+matchKey+"matchnumber="+matchNumber);
            }
        }

        return matchList;
    }

    private List<TeamDetail> parseAllianceTeams(JsonObject alliance) {
        List<TeamDetail> teamDetails = new ArrayList<>();
        JsonArray teamKeys = alliance.getAsJsonArray("team_keys");

        for (JsonElement teamKeyElement : teamKeys) {
            String teamKey = teamKeyElement.getAsString();
            TeamDetail teamDetail = fetchTeamDetail(teamKey);
            if (teamDetail != null) {
                teamDetails.add(teamDetail);
            }
        }

        return teamDetails;
    }
    private TeamDetail fetchTeamDetail(String teamKey) {
        String url = TBA_BASE_URL + "/team/" + teamKey;

        Request request = new Request.Builder()
                .url(url)
                .header("X-TBA-Auth-Key", TBA_AUTH_KEY)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            String responseData = response.body().string();
            JsonObject teamObject = JsonParser.parseString(responseData).getAsJsonObject();

            String teamNumber = teamObject.get("team_number").getAsString();
            String teamName = teamObject.get("nickname").isJsonNull() ? "N/A" : teamObject.get("nickname").getAsString();
            String school = teamObject.get("school_name").isJsonNull() ? "N/A" : teamObject.get("school_name").getAsString();
            String cityState = teamObject.get("city").getAsString() + ", " + teamObject.get("state_prov").getAsString();

            return new TeamDetail(teamNumber, teamName, school, cityState);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }



    private void updateMatchDetails (TableView < MatchDetail > tableView, String selectedItem) throws IOException {
                if (selectedItem != null) {
                    tableView.getItems().clear();
                    redTeams.getItems().clear();
                    blueTeams.getItems().clear();
                    redcoop.setSelected(false);
                    redmelody.setSelected(false);
                    redensemble.setSelected(false);
                    bluecoop.setSelected(false);
                    bluemelody.setSelected(false);
                    blueensemble.setSelected(false);
                    MatchInfo matchInfo = matchData.stream()
                            .filter(m -> {
                                DateTimeFormatter outputFormat = DateTimeFormatter.ofPattern("MMMM dd @ hh:mm");
                                LocalDateTime dateTime = LocalDateTime.parse(m.getDateTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                                String matchSummary = dateTime.format(outputFormat) + ": " + m.getResult() + " (" + m.getMatchNumber() + ")";
                                return matchSummary.equals(selectedItem);
                            })
                            .findFirst()
                            .orElse(null);

                    if (matchInfo != null) {

                        tableView.getItems().add(new MatchDetail(matchInfo.getMatchNumber(),
                                String.valueOf(matchInfo.getTeamScore()),
                                String.valueOf(matchInfo.getOpponentScore())));
                        JsonObject advMatchDataObj = fetchAdvMatchData(matchInfo.matchKey);
                        MatchDataAdv advMatchData = parseMatchData(advMatchDataObj);
                        String eventKey = advMatchDataObj.get("event_key").getAsString();

                        // Fetch event details using the event key
                        JsonObject eventDetails = fetchEventDetails(eventKey);
                        eventName.setText(eventDetails.get("name").getAsString());
                        String eventStartDateRaw = eventDetails.get("start_date").getAsString();
                        String eventEndDateRaw = eventDetails.get("end_date").getAsString();

// Parse the raw dates into LocalDate objects
                        LocalDate eventStartDate = LocalDate.parse(eventStartDateRaw);
                        LocalDate eventEndDate = LocalDate.parse(eventEndDateRaw);

// Define the formatter to use for output
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd yyyy");

// Format the dates
                        String formattedStartDate = eventStartDate.format(formatter);
                        String formattedEndDate = eventEndDate.format(formatter);

// Set the text of the eventDate label
                        eventDate.setText(formattedStartDate + " to " + formattedEndDate);
                        currentMatchKey=matchInfo.matchKey;
                        // Populate red and blue teams
                        populateTeamDetails(redTeams, matchInfo.getRedAllianceTeams());
                        populateTeamDetails(blueTeams, matchInfo.getBlueAllianceTeams());
                        redcoop.setSelected(advMatchData.isRedCoopBonusAchieved());
                        redmelody.setSelected(advMatchData.getRedMelodyBonusAchieved());
                        redensemble.setSelected(advMatchData.getRedEnsembleBonusAchieved());
                        bluecoop.setSelected(advMatchData.isBlueCoopBonusAchieved());
                        bluemelody.setSelected(advMatchData.getBlueMelodyBonusAchieved());
                        blueensemble.setSelected(advMatchData.getBlueEnsembleBonusAchieved());


                    }
                }
            }

            private void populateTeamDetails (TableView < TeamDetail > tableView, List < TeamDetail > teams){
                for (TeamDetail team : teams) {
                    tableView.getItems().add(team);
                }
            }
    @FXML
    public void openMatchLink(ActionEvent event) {
        String matchUrl = "https://thebluealliance.com/match/" +currentMatchKey;

        // Check if the Desktop class is supported (it's not supported on all platforms)
        if (Desktop.isDesktopSupported()) {
            try {
                // Create a URI from the match URL
                URI uri = new URI(matchUrl);

                // Use the desktop's default browser to open the URI
                Desktop.getDesktop().browse(uri);
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
                // Optionally show an error message to the user
            }
        } else {
            System.err.println("Desktop is not supported on this platform.");
        }
    }

    public MatchDataAdv parseMatchData(JsonObject matchJson) {
        long actualTime = matchJson.get("actual_time").getAsLong();
        String compLevel = matchJson.get("comp_level").getAsString();
        String eventKey = matchJson.get("event_key").getAsString();
        String matchKey = matchJson.get("key").getAsString();
        int matchNumber = matchJson.get("match_number").getAsInt();
        long postResultTime = matchJson.get("post_result_time").getAsLong();
        long predictedTime = matchJson.get("predicted_time").getAsLong();
        int setNumber = matchJson.get("set_number").getAsInt();

        JsonObject alliances = matchJson.getAsJsonObject("alliances");
        JsonObject blueAlliance = alliances.getAsJsonObject("blue");
        JsonObject redAlliance = alliances.getAsJsonObject("red");

        int blueScore = blueAlliance.get("score").getAsInt();
        int redScore = redAlliance.get("score").getAsInt();

       List<String> blueTeamsArray = parseTeamKeys(blueAlliance);
        List<String> redTeamsArray = parseTeamKeys(redAlliance);
        String videoKey = null;
        String videoType= null;
        try {
             videoKey = matchJson.getAsJsonArray("videos").get(0).getAsJsonObject().get("key").getAsString();
            videoType = matchJson.getAsJsonArray("videos").get(0).getAsJsonObject().get("type").getAsString();
        }
        catch (IndexOutOfBoundsException e){
            System.out.println("No Video FOund");
        }
        JsonObject blueBreakdown = matchJson.getAsJsonObject("score_breakdown").getAsJsonObject("blue");
        JsonObject redBreakdown = matchJson.getAsJsonObject("score_breakdown").getAsJsonObject("red");

        boolean blueCoopBonusAchieved = blueBreakdown.get("coopertitionBonusAchieved").getAsBoolean();
        boolean redCoopBonusAchieved = redBreakdown.get("coopertitionBonusAchieved").getAsBoolean();
        boolean blueMelodyBonusAchieved = blueBreakdown.get("melodyBonusAchieved").getAsBoolean();
        boolean redMelodyBonusAchieved = redBreakdown.get("melodyBonusAchieved").getAsBoolean();
        boolean blueEnsembleBonusAchieved = blueBreakdown.get("ensembleBonusAchieved").getAsBoolean();
        boolean redEnsembleBonusAchieved = redBreakdown.get("ensembleBonusAchieved").getAsBoolean();
        int blueAutoPoints = blueBreakdown.get("autoPoints").getAsInt();
        int redAutoPoints = redBreakdown.get("autoPoints").getAsInt();

        int blueTeleopPoints = blueBreakdown.get("teleopPoints").getAsInt();
        int redTeleopPoints = redBreakdown.get("teleopPoints").getAsInt();

        int blueTotalPoints = blueBreakdown.get("totalPoints").getAsInt();
        int redTotalPoints = redBreakdown.get("totalPoints").getAsInt();

        return new MatchDataAdv(
                actualTime, compLevel, eventKey, matchKey, matchNumber, postResultTime, predictedTime, setNumber,
                blueScore, redScore, blueTeamsArray, redTeamsArray, videoKey, videoType,
                blueCoopBonusAchieved, redCoopBonusAchieved, redEnsembleBonusAchieved, redMelodyBonusAchieved, blueEnsembleBonusAchieved, blueMelodyBonusAchieved, blueAutoPoints, redAutoPoints,
                blueTeleopPoints, redTeleopPoints, blueTotalPoints, redTotalPoints
        );
    }

    private List<String> parseTeamKeys(JsonObject alliance) {
        JsonArray teamKeysArray = alliance.getAsJsonArray("team_keys");
        List<String> teamKeys = new ArrayList<>();

        for (int i = 0; i < teamKeysArray.size(); i++) {
            String teamKey = teamKeysArray.get(i).getAsString().replace("frc", "");
            teamKeys.add(teamKey);
        }

        return teamKeys;
    }
    public JsonObject fetchAdvMatchData(String matchKey) throws IOException {
        // Construct the URL for the match endpoint
        String url = TBA_BASE_URL + "/match/" + matchKey;

        // Build the HTTP request
        Request request = new Request.Builder()
                .url(url)
                .header("X-TBA-Auth-Key", TBA_AUTH_KEY)  // Set the authorization header
                .build();

        // Execute the request and get the response
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            // Parse the response body to a JsonObject
            String responseData = response.body().string();
            return JsonParser.parseString(responseData).getAsJsonObject();
        }
    }

    public class MatchDataAdv {
        private final long actualTime;
        private final String compLevel;
        private final String eventKey;
        private final String matchKey;
        private final int matchNumber;
        private final long postResultTime;
        private final long predictedTime;
        private final int setNumber;
        private final int blueScore;
        private final int redScore;
        private final List<String> blueTeams;
        private final List<String> redTeams;
        private final String videoKey;
        private final String videoType;
        private final boolean blueCoopBonusAchieved;
        private final boolean redCoopBonusAchieved;
        private final boolean redEnsembleBonusAchieved;
        private final boolean blueEnsembleBonusAchieved;
        private final boolean blueMelodyBonusAchieved;
        private final boolean redMelodyBonusAchieved;
        private final int blueAutoPoints;
        private final int redAutoPoints;
        private final int blueTeleopPoints;
        private final int redTeleopPoints;
        private final int blueTotalPoints;
        private final int redTotalPoints;

        public boolean getRedEnsembleBonusAchieved() {
            return redEnsembleBonusAchieved;
        }

        public boolean getRedMelodyBonusAchieved() {
            return redMelodyBonusAchieved;
        }
        public boolean getBlueEnsembleBonusAchieved() {
            return blueEnsembleBonusAchieved;
        }

        public boolean getBlueMelodyBonusAchieved() {
            return blueMelodyBonusAchieved;
        }

        public MatchDataAdv(long actualTime, String compLevel, String eventKey, String matchKey, int matchNumber, long postResultTime, long predictedTime, int setNumber,
                            int blueScore, int redScore, List<String> blueTeams, List<String> redTeams, String videoKey, String videoType,
                            boolean blueCoopBonusAchieved, boolean redCoopBonusAchieved, boolean redEnsembleBonusAchieved, boolean redMelodyBonusAchieved, boolean blueEnsembleBonusAchieved, boolean blueMelodyBonusAchieved, int blueAutoPoints, int redAutoPoints,
                            int blueTeleopPoints, int redTeleopPoints, int blueTotalPoints, int redTotalPoints) {
            this.actualTime = actualTime;
            this.compLevel = compLevel;
            this.eventKey = eventKey;
            this.matchKey = matchKey;
            this.matchNumber = matchNumber;
            this.postResultTime = postResultTime;
            this.predictedTime = predictedTime;
            this.setNumber = setNumber;
            this.blueScore = blueScore;
            this.redScore = redScore;
            this.blueTeams = blueTeams;
            this.redTeams = redTeams;
            this.videoKey = videoKey;
            this.videoType = videoType;
            this.blueCoopBonusAchieved = blueCoopBonusAchieved;
            this.redCoopBonusAchieved = redCoopBonusAchieved;
            this.redEnsembleBonusAchieved = redEnsembleBonusAchieved;
            this.redMelodyBonusAchieved = redMelodyBonusAchieved;
            this.blueEnsembleBonusAchieved = blueEnsembleBonusAchieved;
            this.blueMelodyBonusAchieved = blueMelodyBonusAchieved;
            this.blueAutoPoints = blueAutoPoints;
            this.redAutoPoints = redAutoPoints;
            this.blueTeleopPoints = blueTeleopPoints;
            this.redTeleopPoints = redTeleopPoints;
            this.blueTotalPoints = blueTotalPoints;
            this.redTotalPoints = redTotalPoints;
        }

        public long getActualTime() {
            return actualTime;
        }

        public String getCompLevel() {
            return compLevel;
        }

        public String getEventKey() {
            return eventKey;
        }

        public String getMatchKey() {
            return matchKey;
        }

        public int getMatchNumber() {
            return matchNumber;
        }

        public long getPostResultTime() {
            return postResultTime;
        }

        public long getPredictedTime() {
            return predictedTime;
        }

        public int getSetNumber() {
            return setNumber;
        }

        public int getBlueScore() {
            return blueScore;
        }

        public int getRedScore() {
            return redScore;
        }

        public List<String> getBlueTeams() {
            return blueTeams;
        }

        public List<String> getRedTeams() {
            return redTeams;
        }

        public String getVideoKey() {
            return videoKey;
        }

        public String getVideoType() {
            return videoType;
        }

        public boolean isBlueCoopBonusAchieved() {
            return blueCoopBonusAchieved;
        }

        public boolean isRedCoopBonusAchieved() {
            return redCoopBonusAchieved;
        }

        public int getBlueAutoPoints() {
            return blueAutoPoints;
        }

        public int getRedAutoPoints() {
            return redAutoPoints;
        }

        public int getBlueTeleopPoints() {
            return blueTeleopPoints;
        }

        public int getRedTeleopPoints() {
            return redTeleopPoints;
        }

        public int getBlueTotalPoints() {
            return blueTotalPoints;
        }

        public int getRedTotalPoints() {
            return redTotalPoints;
        }
    }


            public static class TeamDetail {
                private final SimpleStringProperty teamNumber;
                private final SimpleStringProperty teamName;
                private final SimpleStringProperty school;
                private final SimpleStringProperty cityState;

                public TeamDetail(String teamNumber, String teamName, String school, String cityState) {
                    this.teamNumber = new SimpleStringProperty(teamNumber);
                    this.teamName = new SimpleStringProperty(teamName);
                    this.school = new SimpleStringProperty(school);
                    this.cityState = new SimpleStringProperty(cityState);
                }

                public String getTeamNumber() {
                    return teamNumber.get();
                }

                public String getTeamName() {
                    return teamName.get();
                }

                public String getSchool() {
                    return school.get();
                }

                public String getCityState() {
                    return cityState.get();
                }
            }


    public static class MatchInfo {
        private final int matchNumber;
        private final String matchKey;
        private final String dateTime;
        private final String result;
        private final int teamScore;
        private final int opponentScore;
        private final List<TeamDetail> redAllianceTeams;
        private final List<TeamDetail> blueAllianceTeams;

        public MatchInfo(int matchNumber, String matchKey, String dateTime, String result, int teamScore, int opponentScore,
                         List<TeamDetail> redAllianceTeams, List<TeamDetail> blueAllianceTeams) {
            this.matchNumber = matchNumber;
            this.matchKey = matchKey;
            this.dateTime = dateTime;
            this.result = result;
            this.teamScore = teamScore;
            this.opponentScore = opponentScore;
            this.redAllianceTeams = redAllianceTeams;
            this.blueAllianceTeams = blueAllianceTeams;
        }

        public int getMatchNumber() {
            return matchNumber;
        }

        public String getMatchKey() {
            return matchKey;
        }

        public String getDateTime() {
            return dateTime;
        }

        public String getResult() {
            return result;
        }

        public int getTeamScore() {
            return teamScore;
        }

        public int getOpponentScore() {
            return opponentScore;
        }

        public List<TeamDetail> getRedAllianceTeams() {
            return redAllianceTeams;
        }

        public List<TeamDetail> getBlueAllianceTeams() {
            return blueAllianceTeams;
        }
    }
    public static class MatchDetail {
        private final SimpleStringProperty matchNumber;
        private final SimpleStringProperty teamScore;
        private final SimpleStringProperty opponentScore;

        public MatchDetail(int matchNumber, String teamScore, String opponentScore) {
            this.matchNumber = new SimpleStringProperty(String.valueOf(matchNumber));
            this.teamScore = new SimpleStringProperty(teamScore);
            this.opponentScore = new SimpleStringProperty(opponentScore);
        }

        public String getMatchNumber() {
            return matchNumber.get();
        }

        public String getTeamScore() {
            return teamScore.get();
        }

        public String getOpponentScore() {
            return opponentScore.get();
        }
    }


}