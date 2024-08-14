module com.rebelrobotics.scoutingapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;
    requires okhttp3;
    requires java.desktop;

    opens com.rebelrobotics.scoutingapp to javafx.fxml;
    exports com.rebelrobotics.scoutingapp;
}