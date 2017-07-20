package com.servicos.estatica.belluno.controller;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import com.servicos.estatica.belluno.app.ControlledScreen;
import com.servicos.estatica.belluno.properties.MarkLineChartProperty;
import com.servicos.estatica.belluno.util.HoverDataChart;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.util.Duration;

public class PaginaInicialController implements Initializable, ControlledScreen {

	@FXML
	private LineChart<String, Number> chartTemp;
	@FXML
	private CategoryAxis xAxis;
	@FXML
	private NumberAxis yAxis;
	
	private static Timeline chartAnimation;
	private static XYChart.Series<String, Number> tempSeries;
	private static DateTimeFormatter horasFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
	
	private static Double tempReator = new Double(0);
	
	final ObservableList<XYChart.Series<String, Number>> plotValuesList = FXCollections.observableArrayList();
	final List<Node> valueMarks = new ArrayList<>();
	
	ScreensController myController;

	@Override
	public void setScreenParent(ScreensController screenPage) {
		myController = screenPage;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		configLineChart();
		
	}
	
	private void configLineChart() {
		yAxis.setAutoRanging(false);
		yAxis.setLowerBound(0);
		yAxis.setUpperBound(70);
		yAxis.setTickUnit(10);

		chartAnimation = new Timeline();
		chartAnimation.getKeyFrames()
				.add(new KeyFrame(Duration.millis(5000), (ActionEvent actionEvent) -> plotTemp()));
		chartAnimation.setCycleCount(Animation.INDEFINITE);

		tempSeries = new XYChart.Series<String, Number>();
		tempSeries.getData().add(new XYChart.Data<>(horasFormatter.format(LocalDateTime.now()), 20));
		plotValuesList.add(tempSeries);
		chartTemp.setData(plotValuesList);

	}
	
	private void plotTemp() {
		final XYChart.Data<String, Number> data = new XYChart.Data<>(horasFormatter.format(LocalDateTime.now()),
				tempReator);
		Node mark = new HoverDataChart(1, tempReator);
		if (!MarkLineChartProperty.getMark())
			mark.setVisible(Boolean.FALSE);
		valueMarks.add(mark);
		data.setNode(mark);
		tempSeries.getData().add(data);
		//saveTemp();
	}

}