import db.DBController;
import db.DBModel;
import javafx.application.Application;
import javafx.stage.Stage;
import views.DBView;

public class BasketballDB extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception{
        // Initialize Database
//        db.H2DB hMain = new db.H2DB();
//        hMain.init();

        DBModel model = new DBModel();
        DBController controller = new DBController(model);
        DBView view = new DBView(controller, primaryStage);

        model.addObserver(view);

        // Window Properties
        primaryStage.setTitle("Basketball Database");
        primaryStage.setHeight(550);
        primaryStage.setWidth(800);
        primaryStage.setResizable(false);

        primaryStage.show();
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}
