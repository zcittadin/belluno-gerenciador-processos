package com.servicos.estatica.belluno.controller;

import java.net.URL;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import com.servicos.estatica.belluno.dao.LeituraDAO;
import com.servicos.estatica.belluno.model.Leitura;
import com.servicos.estatica.belluno.model.Processo;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

public class ChartScreenController implements Initializable {

	@FXML
	private LineChart<String, Number> chartTemp;
	@FXML
	private CategoryAxis xAxis;
	@FXML
	private NumberAxis yAxis;

	private static XYChart.Series<String, Number> tempSeries;
	private static DateTimeFormatter dataHoraFormatter = DateTimeFormatter.ofPattern("HH:mm:ss - dd/MM/yy");

	final ObservableList<XYChart.Series<String, Number>> plotValuesList = FXCollections.observableArrayList();
	private static LeituraDAO LeituraDAO = new LeituraDAO();
	private static List<Leitura> leituras;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		configLineChart();
	}

	public void setContext(Processo processo) {
		leituras = LeituraDAO.findLeiturasByProcesso(processo);
		if (!leituras.isEmpty()) {
			for (Leitura leitura : leituras) {
				plotTemp(leitura.getTemp(), leitura.getDtProc());
			}
		}
	}

	private void configLineChart() {
		yAxis.setAutoRanging(false);
		yAxis.setLowerBound(0);
		yAxis.setUpperBound(1050);
		yAxis.setTickUnit(100);
		tempSeries = new XYChart.Series<String, Number>();
		plotValuesList.add(tempSeries);
		chartTemp.setData(plotValuesList);

	}

	private void plotTemp(Double temperatura, Date dtProc) {
		final XYChart.Data<String, Number> data = new XYChart.Data<>(
				dataHoraFormatter.format(dtProc.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()),
				temperatura);
		// Node mark = new HoverDataChart(1, temperatura);
		// if (!MarkLineChartProperty.getMark())
		// mark.setVisible(Boolean.FALSE);
		// valueMarks.add(mark);
		// data.setNode(mark);
		tempSeries.getData().add(data);
	}

}
