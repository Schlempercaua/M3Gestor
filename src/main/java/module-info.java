module com.caua.madeira {
    // Módulos JavaFX
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
    requires transitive javafx.graphics;
    
    // Módulos Java SE
    requires transitive java.sql;
    requires java.desktop; // para Desktop.open()
    requires com.github.librepdf.openpdf; // OpenPDF
    
    // Exporta os pacotes necessários
    exports com.caua.madeira;
    exports com.caua.madeira.view;
    exports com.caua.madeira.model;
    exports com.caua.madeira.dao;
    exports com.caua.madeira.database;
    
    // Abre pacotes para reflexão
    opens com.caua.madeira to javafx.fxml;
    opens com.caua.madeira.view to javafx.fxml;
}
