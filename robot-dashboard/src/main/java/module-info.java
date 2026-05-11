module com.example.robot {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.eclipse.paho.client.mqttv3;

    opens com.example.robot to javafx.fxml;
    exports com.example.robot;
}
