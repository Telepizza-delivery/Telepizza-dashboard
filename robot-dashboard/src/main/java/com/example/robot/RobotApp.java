package com.example.robot;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class RobotApp extends Application {
    private DashboardController controller;

    @Override
    public void start(Stage primaryStage) throws Exception {
        controller = new DashboardController();  // assign to field, not local var
        Scene scene = new Scene(controller.buildUI(), 900, 700);

        primaryStage.setTitle("Robot Delivery Dashboard");
        primaryStage.setScene(scene);
        primaryStage.show();

        controller.startMqtt();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        if (controller != null) {
            controller.stopMqtt();  // we'll add this to DashboardController
        }
        System.exit(0);
    }



    public static void main(String[] args) {
        launch(args);
    }
}
