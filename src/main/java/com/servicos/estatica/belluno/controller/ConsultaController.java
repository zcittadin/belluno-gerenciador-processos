package com.servicos.estatica.belluno.controller;

import java.awt.Desktop;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import com.servicos.estatica.belluno.app.ControlledScreen;
import com.servicos.estatica.belluno.dao.LeituraDAO;
import com.servicos.estatica.belluno.dao.ProcessoDAO;
import com.servicos.estatica.belluno.model.Leitura;
import com.servicos.estatica.belluno.model.Processo;
import com.servicos.estatica.belluno.report.builder.ProcessoReportCreator;
import com.servicos.estatica.belluno.util.PeriodFormatter;
import com.servicos.estatica.belluno.util.Toast;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

@SuppressWarnings("rawtypes")
public class ConsultaController implements Initializable, ControlledScreen {

	@FXML
	private Rectangle recConsulta;
	@FXML
	private RadioButton rdIdentificador;
	@FXML
	private RadioButton rdPeriodo;
	@FXML
	private RadioButton rdUltimos;
	@FXML
	private TextField txtIdentificador;
	@FXML
	private DatePicker dtpInicio;
	@FXML
	private DatePicker dtpFinal;
	@FXML
	private Spinner<Integer> spnUltimos;
	@FXML
	private TableView tblConsulta;
	@FXML
	private TableColumn colIdentificador;
	@FXML
	private TableColumn colDhInicial;
	@FXML
	private TableColumn colDhFinal;
	@FXML
	private TableColumn colTempoDecorrido;
	@FXML
	private TableColumn colTempMin;
	@FXML
	private TableColumn colTempMax;
	@FXML
	private TableColumn colGraficos;
	@FXML
	private TableColumn colRelatorios;
	@FXML
	private TableColumn colExcluir;
	@FXML
	private ProgressIndicator progForm;
	@FXML
	private ProgressIndicator progTable;
	@FXML
	private Button btBuscar;

	SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 99, 10);

	ToggleGroup group = new ToggleGroup();

	private static String TOOLTIP_CSS = "-fx-font-size: 8pt; -fx-font-weight: bold; -fx-font-style: normal; ";
	private Tooltip tooltipChart = new Tooltip("Visualizar o gr�fico do processo");
	private Tooltip tooltipReport = new Tooltip("Emitir um relat�rio em PDF");
	private Tooltip tooltipDelete = new Tooltip("Excluir o processo");

	private static LeituraDAO leituraDAO = new LeituraDAO();
	private static ProcessoDAO processoDAO = new ProcessoDAO();
	private static ObservableList<Processo> processos = FXCollections.observableArrayList();

	ScreensController myController;

	@Override
	public void setScreenParent(ScreensController screenPage) {
		myController = screenPage;
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		tooltipChart.setStyle(TOOLTIP_CSS);
		tooltipDelete.setStyle(TOOLTIP_CSS);
		tooltipReport.setStyle(TOOLTIP_CSS);
		spnUltimos.setValueFactory(valueFactory);
		recConsulta.setFill(Color.TRANSPARENT);
		rdIdentificador.setToggleGroup(group);
		rdPeriodo.setToggleGroup(group);
		rdUltimos.setToggleGroup(group);
		consultarRecentes();

	}

	@FXML
	private void selectUltimos() {
		spnUltimos.setDisable(false);
		dtpInicio.getEditor().setText(null);
		dtpFinal.getEditor().setText(null);
		txtIdentificador.setText(null);
		dtpInicio.setDisable(true);
		dtpFinal.setDisable(true);
		txtIdentificador.setDisable(true);
	}

	@FXML
	private void selectPorPeriodo() {
		spnUltimos.setDisable(true);
		spnUltimos.getValueFactory().setValue(10);
		txtIdentificador.setText(null);
		dtpInicio.setDisable(false);
		dtpFinal.setDisable(false);
		txtIdentificador.setDisable(true);
	}

	@FXML
	private void selectPorIdentificador() {
		dtpInicio.getEditor().setText(null);
		dtpFinal.getEditor().setText(null);
		spnUltimos.setDisable(true);
		spnUltimos.getValueFactory().setValue(10);
		dtpInicio.setDisable(true);
		dtpFinal.setDisable(true);
		txtIdentificador.setDisable(false);

	}

	@FXML
	private void consultar() {
		if (!validateFields())
			return;
		progForm.setVisible(true);
		progTable.setVisible(true);
		spnUltimos.setDisable(true);
		rdIdentificador.setDisable(true);
		rdPeriodo.setDisable(true);
		rdUltimos.setDisable(true);
		btBuscar.setDisable(true);
		tblConsulta.setDisable(true);
		tblConsulta.getItems().clear();
		Task<Void> searchTask = new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				if (rdIdentificador.isSelected()) {
					processos = FXCollections.observableList(
							(List<Processo>) processoDAO.findByIdentificadorProcessos(txtIdentificador.getText()));
					return null;
				}
				if (rdPeriodo.isSelected()) {
					processos = FXCollections.observableList((List<Processo>) processoDAO
							.findByPeriodo(dtpInicio.getValue().toString(), dtpFinal.getValue().toString()));
					return null;
				}
				if (rdUltimos.isSelected()) {
					processos = FXCollections
							.observableList((List<Processo>) processoDAO.findLastProcessos(spnUltimos.getValue()));
					return null;
				}
				return null;
			}
		};

		searchTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent arg0) {
				if (!processos.isEmpty()) {
					populateTable();
				}
				progForm.setVisible(false);
				progTable.setVisible(false);
				rdIdentificador.setDisable(false);
				rdPeriodo.setDisable(false);
				rdUltimos.setDisable(false);
				btBuscar.setDisable(false);
				tblConsulta.setDisable(false);
				if (rdIdentificador.isSelected()) {
					txtIdentificador.setDisable(false);
				}
				if (rdPeriodo.isSelected()) {
					dtpInicio.setDisable(false);
					dtpFinal.setDisable(false);
				}
				if (rdUltimos.isSelected()) {
					spnUltimos.setDisable(false);
				}
			}
		});
		searchTask.setOnFailed(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent arg0) {
				progForm.setVisible(false);
				progTable.setVisible(false);
				spnUltimos.setDisable(false);
				rdIdentificador.setDisable(false);
				rdPeriodo.setDisable(false);
				rdUltimos.setDisable(false);
				spnUltimos.setDisable(false);
				btBuscar.setDisable(false);
				tblConsulta.setDisable(false);
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Falha");
				alert.setHeaderText("Ocorreu um erro ao consultar os dados.");
				alert.showAndWait();
			}
		});
		Thread t = new Thread(searchTask);
		t.start();
	}

	private Boolean validateFields() {
		if (rdPeriodo.isSelected()) {
			if ((dtpInicio.getValue() == null) || (dtpFinal.getValue() == null)) {
				Alert alert = new Alert(AlertType.WARNING);
				alert.setTitle("Aten��o");
				alert.setHeaderText("Informe as datas de in�cio e fim do processo.");
				alert.showAndWait();
				dtpInicio.requestFocus();
				return false;
			}
		}
		if (rdIdentificador.isSelected()) {
			if (txtIdentificador.getText() == null) {
				Alert alert = new Alert(AlertType.WARNING);
				alert.setTitle("Aten��o");
				alert.setHeaderText("Informe um identificador para a consulta.");
				alert.showAndWait();
				txtIdentificador.requestFocus();
				return false;
			}
			if (txtIdentificador.getText().trim().equals("")) {
				Alert alert = new Alert(AlertType.WARNING);
				alert.setTitle("Aten��o");
				alert.setHeaderText("Informe um identificador para a consulta.");
				alert.showAndWait();
				txtIdentificador.requestFocus();
				return false;
			}
		}
		return true;

	}

	private void consultarRecentes() {
		progForm.setVisible(true);
		progTable.setVisible(true);
		spnUltimos.setDisable(true);
		rdIdentificador.setDisable(true);
		rdPeriodo.setDisable(true);
		rdUltimos.setDisable(true);
		spnUltimos.setDisable(true);
		btBuscar.setDisable(true);
		tblConsulta.setDisable(true);
		tblConsulta.getItems().clear();
		Task<Void> searchTask = new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				processos = FXCollections
						.observableList((List<Processo>) processoDAO.findLastProcessos(spnUltimos.getValue()));
				return null;
			}
		};

		searchTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent arg0) {
				if (!processos.isEmpty()) {
					populateTable();
				}
				progForm.setVisible(false);
				progTable.setVisible(false);
				spnUltimos.setDisable(false);
				rdIdentificador.setDisable(false);
				rdPeriodo.setDisable(false);
				rdUltimos.setDisable(false);
				btBuscar.setDisable(false);
				tblConsulta.setDisable(false);
			}
		});
		searchTask.setOnFailed(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent arg0) {
				Alert alert = new Alert(AlertType.WARNING);
				alert.setTitle("Aten��o");
				alert.setHeaderText("Informe um identificador para a consulta.");
				alert.showAndWait();
				progForm.setVisible(false);
				progTable.setVisible(false);
				spnUltimos.setDisable(false);
				rdIdentificador.setDisable(false);
				rdPeriodo.setDisable(false);
				rdUltimos.setDisable(false);
				btBuscar.setDisable(false);
				tblConsulta.setDisable(false);
			}
		});
		Thread t = new Thread(searchTask);
		t.start();

	}

	@SuppressWarnings("unchecked")
	private void populateTable() {
		tblConsulta.setItems(processos);

		colIdentificador.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<Processo, String>, ObservableValue<String>>() {
					public ObservableValue<String> call(CellDataFeatures<Processo, String> cell) {
						final Processo p = cell.getValue();
						final SimpleObjectProperty<String> simpleObject = new SimpleObjectProperty<String>(
								p.getIdentificador());
						return simpleObject;
					}
				});
		colDhInicial.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<Processo, String>, ObservableValue<String>>() {
					public ObservableValue<String> call(CellDataFeatures<Processo, String> cell) {
						final Processo p = cell.getValue();
						final SimpleObjectProperty<String> simpleObject;
						if (p.getDhInicial() != null) {
							simpleObject = new SimpleObjectProperty<String>(
									new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss").format(p.getDhInicial()));
						} else {
							simpleObject = new SimpleObjectProperty<String>("Em andamento");
						}
						return simpleObject;
					}
				});
		colDhFinal.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<Processo, String>, ObservableValue<String>>() {
					public ObservableValue<String> call(CellDataFeatures<Processo, String> cell) {
						final Processo p = cell.getValue();
						final SimpleObjectProperty<String> simpleObject;
						if (p.getDhFinal() != null) {
							simpleObject = new SimpleObjectProperty<String>(
									new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss").format(p.getDhFinal()));
						} else {
							simpleObject = new SimpleObjectProperty<String>("Em andamento");
						}
						return simpleObject;
					}
				});
		colTempoDecorrido.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<Processo, String>, ObservableValue<String>>() {
					public ObservableValue<String> call(CellDataFeatures<Processo, String> cell) {
						final Processo p = cell.getValue();
						final SimpleObjectProperty<String> simpleObject;
						if (p.getDhFinal() != null) {
							simpleObject = new SimpleObjectProperty<String>(
									PeriodFormatter.formatPeriod(p.getDhInicial(), p.getDhFinal()));
						} else {
							simpleObject = new SimpleObjectProperty<String>("");
						}
						return simpleObject;
					}
				});
		colTempMin.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<Processo, Integer>, ObservableValue<Integer>>() {
					public ObservableValue<Integer> call(CellDataFeatures<Processo, Integer> cell) {
						final Processo p = cell.getValue();
						final SimpleObjectProperty<Integer> simpleObject = new SimpleObjectProperty<Integer>(
								p.getTempMin());
						return simpleObject;
					}
				});
		colTempMax.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<Processo, Integer>, ObservableValue<Integer>>() {
					public ObservableValue<Integer> call(CellDataFeatures<Processo, Integer> cell) {
						final Processo p = cell.getValue();
						final SimpleObjectProperty<Integer> simpleObject = new SimpleObjectProperty<Integer>(
								p.getTempMax());
						return simpleObject;
					}
				});
		colGraficos.setCellValueFactory(new PropertyValueFactory<>("DUMMY"));
		colRelatorios.setCellValueFactory(new PropertyValueFactory<>("DUMMY"));
		colExcluir.setCellValueFactory(new PropertyValueFactory<>("DUMMY"));

		Callback<TableColumn<Processo, String>, TableCell<Processo, String>> cellGraficoFactory = //
				new Callback<TableColumn<Processo, String>, TableCell<Processo, String>>() {
					@Override
					public TableCell call(final TableColumn<Processo, String> param) {
						final TableCell<Processo, String> cell = new TableCell<Processo, String>() {

							final Button btn = new Button();

							@Override
							public void updateItem(String item, boolean empty) {
								super.updateItem(item, empty);
								if (empty) {
									setGraphic(null);
									setText(null);
								} else {
									btn.setOnAction(event -> {

										try {
											Stage stage;
											Parent root;
											stage = new Stage();
											URL url = getClass()
													.getResource("/com/servicos/estatica/belluno/app/ChartScreen.fxml");
											FXMLLoader fxmlloader = new FXMLLoader();
											fxmlloader.setLocation(url);
											fxmlloader.setBuilderFactory(new JavaFXBuilderFactory());
											root = (Parent) fxmlloader.load(url.openStream());
											stage.setScene(new Scene(root));
											stage.setTitle("Visualiza��o gr�fica do processo");
											stage.initModality(Modality.APPLICATION_MODAL);
											stage.initOwner(txtIdentificador.getScene().getWindow());
											stage.setResizable(Boolean.FALSE);
											((ChartScreenController) fxmlloader.getController())
													.setContext(getTableView().getItems().get(getIndex()));
											stage.showAndWait();
										} catch (IOException e) {
											System.err.println("Erro ao carregar FXML!");
											e.printStackTrace();
										}

									});
									Tooltip.install(btn, tooltipChart);
									btn.setStyle(
											"-fx-graphic: url('com/servicos/estatica/belluno/style/chart_curve.png');");
									btn.setCursor(Cursor.HAND);
									setGraphic(btn);
									setText(null);
								}
							}
						};
						return cell;
					}
				};
		colGraficos.setCellFactory(cellGraficoFactory);

		Callback<TableColumn<Processo, String>, TableCell<Processo, String>> cellReportFactory = //
				new Callback<TableColumn<Processo, String>, TableCell<Processo, String>>() {
					@Override
					public TableCell call(final TableColumn<Processo, String> param) {
						final TableCell<Processo, String> cell = new TableCell<Processo, String>() {

							final Button btn = new Button();

							@Override
							public void updateItem(String item, boolean empty) {
								super.updateItem(item, empty);
								if (empty) {
									setGraphic(null);
									setText(null);
								} else {
									btn.setOnAction(event -> {
										Processo processo = getTableView().getItems().get(getIndex());
										saveReport(processo);
									});
									Tooltip.install(btn, tooltipReport);
									btn.setStyle("-fx-graphic: url('com/servicos/estatica/belluno/style/report.png');");
									btn.setCursor(Cursor.HAND);
									setGraphic(btn);
									setText(null);
								}
							}
						};
						return cell;
					}
				};
		colRelatorios.setCellFactory(cellReportFactory);

		Callback<TableColumn<Processo, String>, TableCell<Processo, String>> cellExcluirFactory = new Callback<TableColumn<Processo, String>, TableCell<Processo, String>>() {
			@Override
			public TableCell call(final TableColumn<Processo, String> param) {
				final TableCell<Processo, String> cell = new TableCell<Processo, String>() {

					final Button btn = new Button();

					@Override
					public void updateItem(String item, boolean empty) {
						super.updateItem(item, empty);
						if (empty) {
							setGraphic(null);
							setText(null);
						} else {
							btn.setOnAction(event -> {
								Alert alert = new Alert(AlertType.CONFIRMATION);
								alert.setTitle("Confirmar cancelamento");
								alert.setHeaderText("Os dados referentes a este processo ser�o perdidos. Confirmar?");
								Optional<ButtonType> result = alert.showAndWait();
								if (result.get() == ButtonType.OK) {
									Processo processo = getTableView().getItems().get(getIndex());
									Task<Void> exclusionTask = new Task<Void>() {
										@Override
										protected Void call() throws Exception {
											leituraDAO.removeLeituras(processo);
											processoDAO.removeProcesso(processo);
											processos.remove(processo);
											tblConsulta.refresh();
											return null;
										}
									};

									exclusionTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
										@Override
										public void handle(WorkerStateEvent arg0) {
											if (!processos.isEmpty()) {
												makeToast("Processo removido com sucesso.");
											}
										}
									});
									Thread t = new Thread(exclusionTask);
									t.start();
								}
							});
							Tooltip.install(btn, tooltipDelete);
							btn.setStyle("-fx-graphic: url('com/servicos/estatica/belluno/style/delete.png');");
							btn.setCursor(Cursor.HAND);
							setGraphic(btn);
							setText(null);
						}
					}
				};
				return cell;
			}
		};
		colExcluir.setCellFactory(cellExcluirFactory);

		colIdentificador.setStyle("-fx-alignment: CENTER;");
		colDhInicial.setStyle("-fx-alignment: CENTER;");
		colDhFinal.setStyle("-fx-alignment: CENTER;");
		colTempoDecorrido.setStyle("-fx-alignment: CENTER;");
		colTempMin.setStyle("-fx-alignment: CENTER;");
		colTempMax.setStyle("-fx-alignment: CENTER;");
		colGraficos.setStyle("-fx-alignment: CENTER;");
		colRelatorios.setStyle("-fx-alignment: CENTER;");
		colExcluir.setStyle("-fx-alignment: CENTER;");
		tblConsulta.getColumns().setAll(colIdentificador, colDhInicial, colDhFinal, colTempoDecorrido, colTempMin,
				colTempMax, colGraficos, colRelatorios, colExcluir);
	}

	public void saveReport(Processo processo) {
		List<Leitura> leituras = leituraDAO.findLeiturasByProcesso(processo);
		processo.setLeituras(leituras);
		Stage stage = new Stage();
		stage.initOwner(tblConsulta.getScene().getWindow());
		FileChooser fileChooser = new FileChooser();
		fileChooser.getExtensionFilters().addAll(new ExtensionFilter("PDF Files", "*.pdf"));
		fileChooser.setTitle("Salvar relat�rio de processo");
		fileChooser.setInitialFileName(processo.getIdentificador() + ".pdf");
		File savedFile = fileChooser.showSaveDialog(stage);
		if (savedFile != null) {
			generatePdfReport(savedFile, processo);
		}
	}

	private void generatePdfReport(File file, Processo processo) {
		progForm.setVisible(true);
		spnUltimos.setDisable(true);
		rdIdentificador.setDisable(true);
		rdPeriodo.setDisable(true);
		rdUltimos.setDisable(true);
		btBuscar.setDisable(true);
		dtpInicio.setDisable(true);
		dtpFinal.setDisable(true);
		txtIdentificador.setDisable(true);
		Task<Integer> reportTask = new Task<Integer>() {
			@Override
			protected Integer call() throws Exception {
				int result = ProcessoReportCreator.build(processo, file.getAbsolutePath(),
						PeriodFormatter.formatPeriod(processo.getDhInicial(), processo.getDhFinal()));
				int maximum = 20;
				for (int i = 0; i < maximum; i++) {
					updateProgress(i, maximum);
				}
				return new Integer(result);
			}
		};

		reportTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				progForm.setVisible(false);
				rdIdentificador.setDisable(false);
				rdPeriodo.setDisable(false);
				rdUltimos.setDisable(false);
				btBuscar.setDisable(false);
				if (rdIdentificador.isSelected()) {
					txtIdentificador.setDisable(false);
				}
				if (rdPeriodo.isSelected()) {
					dtpInicio.setDisable(false);
					dtpFinal.setDisable(false);
				}
				if (rdUltimos.isSelected()) {
					spnUltimos.setDisable(false);
				}
				int r = reportTask.getValue();
				if (r != 1) {
					Toolkit.getDefaultToolkit().beep();
					Alert alert = new Alert(AlertType.ERROR);
					alert.setTitle("Erro");
					alert.setHeaderText("Houve uma falha na emiss�o do relat�rio.");
					alert.showAndWait();
					return;
				}
				Alert alert = new Alert(AlertType.CONFIRMATION);
				alert.setTitle("Conclu�do");
				alert.setHeaderText("Relat�rio emitido com sucesso. Deseja visualizar?");
				Optional<ButtonType> result = alert.showAndWait();
				if (result.get() == ButtonType.OK) {
					try {
						Desktop.getDesktop().open(file);
					} catch (IOException ex) {
						ex.printStackTrace();
					}
				}
			}
		});

		Thread t = new Thread(reportTask);
		t.start();
	}

	private void makeToast(String message) {
		String toastMsg = message;
		int toastMsgTime = 5000;
		int fadeInTime = 600;
		int fadeOutTime = 600;
		Stage stage = (Stage) txtIdentificador.getScene().getWindow();
		Toast.makeToast(stage, toastMsg, toastMsgTime, fadeInTime, fadeOutTime);
	}

}
