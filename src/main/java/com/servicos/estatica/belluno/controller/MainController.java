package com.servicos.estatica.belluno.controller;

import java.io.IOException;
import java.net.URL;
import java.time.Clock;
import java.util.Optional;
import java.util.ResourceBundle;

import com.servicos.estatica.belluno.properties.CurrentScreenProperty;
import com.servicos.estatica.belluno.util.EstaticaInfoUtil;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import zan.inc.custom.components.ImageViewResizer;

public class MainController extends EstaticaInfoUtil implements Initializable {

	public static String screenInicioID = "inicio";
	public static String screenInicioFile = "/com/servicos/estatica/belluno/app/PaginaInicial.fxml";
	public static String screenConsultaID = "consulta";
	public static String screenConsultaFile = "/com/servicos/estatica/belluno/app/Consulta.fxml";

	@FXML
	private AnchorPane mainPane;
	@FXML
	private Pane centralPane;
	@FXML
	private ImageView imgCliente;
	@FXML
	private ImageView imgExit;
	@FXML
	private Button btInicio;
	@FXML
	private Button btConsultas;
	@FXML
	private Clock clock;

	private static ImageViewResizer imgClienteResizer;
	private static ImageViewResizer imgExitResizer;

	ScreensController mainContainer = new ScreensController();

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
		btInicio.hoverProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if(newValue)
					btInicio.setStyle("-fx-background-color: #000000;-fx-text-fill: #FFFFFF;");
				else
					btInicio.setStyle(null);
				
			}
		});
		
		btConsultas.hoverProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if(newValue)
					btConsultas.setStyle("-fx-background-color: #000000;-fx-text-fill: #FFFFFF;");
				else
					btConsultas.setStyle(null);
				
			}
		});
		
		initEstaticaInfo();
		
		imgCliente.setImage(new Image("/com/servicos/estatica/belluno/style/belluno.png"));
		imgClienteResizer = new ImageViewResizer(imgCliente, 126, 70);
		imgClienteResizer.setLayoutX(16);
		imgClienteResizer.setLayoutY(6);
		imgExitResizer = new ImageViewResizer(imgExit, 70, 71);
		imgExitResizer.setLayoutX(50);
		imgExitResizer.setLayoutY(633);
		mainPane.getChildren().addAll(imgClienteResizer, imgExitResizer);

		mainContainer.loadScreen(screenInicioID, screenInicioFile);
		mainContainer.loadScreen(screenConsultaID, screenConsultaFile);
		CurrentScreenProperty.setScreen(screenInicioID);

		mainContainer.setScreen(screenInicioID);
		centralPane.getChildren().addAll(mainContainer);

	}
	
	@FXML
	private void openInicial() {
		mainContainer.setScreen(screenInicioID);
	}

	@FXML
	private void openConsultas() {
		mainContainer.setScreen(screenConsultaID);
	}

	@FXML
	private void handleImgClienteAction() throws IOException {
		Stage stage;
		Parent root;
		stage = new Stage();
		root = FXMLLoader.load(getClass().getResource("/com/servicos/estatica/belluno/app/ClienteInfo.fxml"));
		stage.setScene(new Scene(root));
		stage.setTitle("Informações sobre o cliente");
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.initOwner(imgCliente.getScene().getWindow());
		stage.setResizable(Boolean.FALSE);
		stage.showAndWait();
	}
	
	@FXML
	private void exit() {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Confirmar encerramento");
		alert.setHeaderText("Deseja realmente sair do sistema?");
		Optional<ButtonType> result = alert.showAndWait();
		if (result.get() == ButtonType.OK) {
			Platform.exit();
		}
	}

}
