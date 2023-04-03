module com.example.sec_lab_3 {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.sec_lab_3 to javafx.fxml;
    exports com.example.sec_lab_3;
}